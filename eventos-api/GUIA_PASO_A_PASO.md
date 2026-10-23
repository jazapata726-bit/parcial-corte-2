# Guía del parcial: API de eventos y asistentes

Trabaja en este orden y comprueba el resultado de cada fase antes de seguir. La carpeta correcta es la que contiene **pom.xml**, **Dockerfile** y **src**.

## Fase 1. Abrir y ejecutar en IntelliJ

1. Descarga y extrae `eventos-api-parcial.zip` en una carpeta de tu equipo.
2. Abre IntelliJ IDEA.
3. Selecciona **File > Open**. Si estás en la pantalla inicial, usa **Open**.
4. Selecciona la carpeta **eventos-api** que contiene `pom.xml`. Abre la carpeta completa.
5. Si aparece la pregunta, confía en el proyecto que descargaste e impórtalo como Maven.
6. Espera a que Maven descargue las dependencias. Puedes revisar la pestaña **Maven** del lateral derecho y usar **Reload All Maven Projects** si hace falta.
7. Ve a **File > Project Structure > Project**. Selecciona un **JDK 17**. Si no lo tienes, usa **Download JDK**, elige versión 17 y un distribuidor como Eclipse Temurin.
8. En el panel izquierdo, abre `src > main > java > com > parcial > eventos > EventosApiApplication`.
9. Haz clic en el triángulo verde junto al método `main` y selecciona **Run EventosApiApplication**.
10. Espera el mensaje `Started EventosApiApplication`. El servidor debería indicar el puerto 8080.
11. En tu navegador abre `http://localhost:8080/api/eventos`. Debe aparecer `[]` en una base nueva.
12. Abre `http://localhost:8080/actuator/health`. Debe aparecer `{"status":"UP"}`.

**Qué acabas de hacer:** iniciar el servidor y permitir que JPA cree las tablas en la base H2 local. No necesitas XAMPP para este proyecto.

**Evidencia:** captura de IntelliJ con la aplicación iniciada y captura de la respuesta de salud. Evita cerrar la aplicación mientras haces las pruebas.

## Fase 2. Importar las pruebas en Postman

1. Abre Postman.
2. Haz clic en **Import > Files** y selecciona `postman/Eventos_API.postman_collection.json`.
3. Importa también `postman/Eventos_Local.postman_environment.json`.
4. En el selector de entornos de la parte superior derecha, selecciona **Eventos - Local**.
5. Comprueba que `baseUrl` sea `http://localhost:8080`.
6. Abre la colección. Ejecuta las solicitudes **en el orden de sus números** o usa **Run collection** para ejecutarlas todas.
7. Revisa los resultados de las pruebas de cada solicitud. Los POST iniciales guardan los identificadores automáticamente.

La colección completa termina retirando las inscripciones y borrando los registros que creó. Para capturar los listados con datos, avanza solicitud por solicitud y guarda las evidencias antes de ejecutar las eliminaciones finales.

**Regla de Postman:** el método se selecciona en el desplegable. El campo URL contiene solo la URL; por ejemplo, `http://localhost:8080/api/eventos`. Escribir `GET http://...` en ese campo produce un error de protocolo.

## Fase 3. Entender y comprobar el CRUD

CRUD significa crear, consultar, actualizar y eliminar. Hazlo para las dos entidades.

### Crear un evento manualmente

1. Crea una solicitud y elige **POST**.
2. URL: `http://localhost:8080/api/eventos`.
3. En **Body > raw**, elige **JSON**.
4. Pega:

```json
{
  "nombre": "Conferencia de tecnología",
  "fecha": "2027-01-15T09:00:00",
  "lugar": "Auditorio principal"
}
```

5. Presiona **Send**. Espera **201 Created**.
6. Anota el `id` que devuelve la respuesta.
7. Consulta con **GET** a la misma URL. El evento debe aparecer en la lista.

### Crear un asistente manualmente

Usa **POST** `http://localhost:8080/api/asistentes`, con este cuerpo JSON:

```json
{
  "nombre": "Ana Pérez",
  "correo": "ana@example.com",
  "telefono": "3001234567"
}
```

Debe responder **201 Created**. Guarda el identificador. Si ese correo ya existe, utiliza otro o elimina primero el registro anterior.

### Actualizar

1. Selecciona **PUT**.
2. Usa `http://localhost:8080/api/eventos/ID_REAL` y sustituye `ID_REAL` por el número obtenido.
3. Envía los tres campos del evento cambiando, por ejemplo, su lugar.
4. Espera **200 OK** y confirma el cambio con GET.
5. Repite con `/api/asistentes/ID_REAL`, enviando nombre, correo y teléfono.

### Eliminar

Usa **DELETE** `/api/eventos/ID_REAL` o `/api/asistentes/ID_REAL`. Debe devolver **204 No Content**. El cuerpo queda vacío. Una consulta posterior por ese identificador devuelve **404 Not Found**.

**Evidencias:** para cada entidad, captura POST 201, GET 200, PUT 200 y DELETE 204, mostrando método, URL y respuesta. Conserva datos sin eliminar para la siguiente fase o crea otros.

## Fase 4. Demostrar la relación N:N

1. Crea **dos eventos**, por ejemplo Conferencia Java y Taller Docker.
2. Crea **dos asistentes**, por ejemplo Ana y Luis.
3. Usa sus identificadores reales. Si fueran eventos 1 y 2, y asistentes 1 y 2, las solicitudes serían:

```http
POST http://localhost:8080/api/eventos/1/asistentes/1
POST http://localhost:8080/api/eventos/1/asistentes/2
POST http://localhost:8080/api/eventos/2/asistentes/1
```

4. Estos POST no llevan cuerpo. Cada inscripción nueva devuelve 201.
5. Consulta **GET** `/api/eventos/1/asistentes`: deben aparecer Ana y Luis.
6. Consulta **GET** `/api/asistentes/1/eventos`: deben aparecer los dos eventos.
7. Repite una inscripción existente: debe responder **409 Conflict**.
8. Retira una inscripción con **DELETE** `/api/eventos/1/asistentes/1`: debe responder 204.
9. Comprueba que Ana y el evento siguen existiendo: solo se eliminó su inscripción.

**Qué demuestras:** hay varios asistentes en un evento y varios eventos para una misma persona. Esto es lo que distingue N:N de 1:N.

**Evidencias:** captura de ambos listados y de la inscripción duplicada rechazada.

## Fase 5. Construir y ejecutar con Docker

1. Abre Docker Desktop y espera a que el motor esté iniciado.
2. Detén la API en IntelliJ con el botón rojo de la ventana **Run**.
3. En IntelliJ, abre la pestaña **Terminal**. En Windows puedes usar PowerShell.
4. Comprueba que estás en la carpeta que contiene `Dockerfile` y `pom.xml`.
5. Ejecuta:

```powershell
docker --version
docker build -t eventos-api:1.0 .
```

El punto final indica que Docker debe usar la carpeta actual. La construcción descarga imágenes, compila y ejecuta las pruebas.

6. Cuando termine correctamente, ejecuta:

```powershell
docker run -d --name eventos-api-demo -p 8080:8080 -v eventos_h2:/app/data eventos-api:1.0
docker ps
docker logs -f eventos-api-demo
```

7. Espera `Started EventosApiApplication`. Sal de los logs con `Ctrl+C`.
8. Consulta en el navegador `http://localhost:8080/actuator/health` y en Postman `GET /api/eventos`.
9. Ejecuta de nuevo las pruebas de Postman para comprobar que la API responde desde el contenedor.

**Evidencias:** construcción finalizada sin errores, `docker ps` mostrando el contenedor y una petición HTTP exitosa.

Para detener y retirar el contenedor:

```powershell
docker stop eventos-api-demo
docker rm eventos-api-demo
```

Si el nombre ya existe, esos dos comandos permiten liberar el nombre y repetir `docker run`. El volumen `eventos_h2` conserva los datos.

### Prueba adicional con PostgreSQL

Esta opción inicia la API y la base en dos contenedores:

```powershell
Copy-Item .env.example .env
```

Abre `.env`, reemplaza la contraseña de ejemplo y guarda. Luego:

```powershell
docker compose up -d --build
docker compose ps
docker compose logs -f api
```

Comprueba `/actuator/health` y ejecuta Postman. Para detenerlo conservando los datos: `docker compose down`. Usa esta opción después de detener el contenedor de H2 para que el puerto 8080 quede libre.

## Fase 6. Crear el repositorio en GitHub

1. Ingresa a tu cuenta en <https://github.com>.
2. Haz clic en **+ > New repository**.
3. Nombre sugerido: `eventos-api`.
4. Selecciona la visibilidad que permita al profesor revisar el código. Si eliges privado, debes concederle acceso.
5. Crea el repositorio **vacío**, porque el proyecto ya incluye README y `.gitignore`.
6. Copia la URL HTTPS real del repositorio.
7. En la terminal del proyecto, ejecuta:

```powershell
git init
git add .
git commit -m "Crear API de eventos y asistentes"
git branch -M main
```

8. Sustituye `TU_USUARIO` por tu usuario real antes de ejecutar el siguiente comando:

```powershell
git remote add origin https://github.com/TU_USUARIO/eventos-api.git
git push -u origin main
```

9. Completa el inicio de sesión si Git lo solicita. Si pide identidad del autor, configura tu nombre y correo con `git config user.name` y `git config user.email` dentro de este repositorio y repite el commit.
10. Actualiza la página del repositorio. Debes ver `pom.xml`, `Dockerfile`, `src`, `README.md`, `mvnw`, `mvnw.cmd` y `.mvn/wrapper`.
11. Comprueba que **no** aparezcan `.env`, `data` o `target`. El `.gitignore` ya los excluye.

Si utilizas la opción web **Add file > Upload files**, debes verificar que también se suban los archivos y carpetas cuyo nombre empieza por punto. La opción Git de la terminal los incluye correctamente.

**Evidencia:** página del repositorio con los archivos y la URL visible.

## Fase 7. Preparar AWS

1. Abre tu cuenta de AWS y selecciona la región **US East (Ohio), us-east-2** como opción inicial de esta guía.
2. Revisa tu plan y créditos disponibles en Billing. EC2, EBS e IPv4 pública pueden generar cargos.
3. Localiza `aws/eventos-stack.json` en el proyecto extraído.
4. Lee `aws/DESPLEGAR_AWS.md`. La plantilla crea los recursos propios de este proyecto e incluye el código de despliegue.
5. La base será PostgreSQL en un contenedor con volumen persistente dentro de EC2. No debes crear las tablas a mano; JPA las genera al arrancar.

## Fase 8. Desplegar con CloudFormation

1. Busca **CloudFormation** en el buscador de AWS.
2. Abre **Create stack > With new resources (standard)**.
3. Elige **Choose an existing template > Upload a template file**.
4. Sube `aws/eventos-stack.json`.
5. Pulsa **Next**, escribe `eventos-api-parcial` y conserva `t3.micro` como tipo inicial.
6. Continúa con las opciones predeterminadas y revisa los recursos y el coste según tu cuenta antes de crear la pila.
7. Crea la pila y observa **Events**. El servidor instala Docker, compila, ejecuta pruebas y arranca la API y PostgreSQL.
8. Espera **CREATE_COMPLETE**. El arranque puede tardar varios minutos y dispone de un límite de señal de 30 minutos.
9. En **Outputs**, copia los valores reales de ApiUrl, HealthUrl y EventosUrl.
10. Abre HealthUrl: debe responder `{"status":"UP"}`.
11. Abre EventosUrl: en una base nueva debe responder `[]`.
12. En **EC2 > Instances**, verifica que `eventos-api-parcial` esté Running. En **Actions > Monitor and troubleshoot > Get system log**, puedes revisar el arranque y los contenedores.

**Evidencias:** pila CREATE_COMPLETE, URLs de Outputs, instancia Running, registro de Docker y respuesta de salud. La ruta de despliegue completa y sus soluciones están en `aws/DESPLEGAR_AWS.md`.

## Fase 9. Probar la API publicada

1. En Postman, duplica el entorno **Eventos - Local** y llámalo **Eventos - Nube**.
2. Cambia `baseUrl` por el valor real de ApiUrl que entrega CloudFormation, sin barra al final.
3. Selecciona ese entorno.
4. Ejecuta la colección completa. Ya no debes tener `localhost` en la URL efectiva.
5. Para obtener evidencia con registros, ejecuta las primeras solicitudes de nuevo y captura las consultas antes de las eliminaciones finales.
6. Verifica que ambos CRUD, las tres inscripciones N:N y los errores esperados respondan correctamente.
7. Realiza una nueva consulta después de reiniciar la instancia EC2 desde AWS para comprobar que los datos de PostgreSQL se conservan. Usa registros de prueba que no hayan sido eliminados por la colección.

**Evidencias:** CRUD sobre el dominio público, relación en ambos sentidos y resultados de las pruebas de la colección.

## Fase 10. Completar y entregar

1. Completa `ENTREGA.md` con tu nombre, grupo, docente y enlaces reales.
2. Agrega el enlace del repositorio y la URL pública en el README o en el medio que indique el profesor.
3. Guarda los cambios y publícalos:

```powershell
git add README.md ENTREGA.md
git commit -m "Documentar enlaces de entrega"
git push
```

4. Comprueba que la instancia EC2 siga Running y que HealthUrl responda UP.
5. Abre el repositorio y el enlace público desde otra ventana para confirmar que el profesor podrá acceder.
6. Entrega los dos enlaces y las evidencias solicitadas.

**Lista final:**

- [ ] Evento y Asistente tienen CRUD completo.
- [ ] La relación N:N está demostrada con dos eventos y dos asistentes.
- [ ] El código separa Controller, Service, Repository y Entity.
- [ ] Docker construye la imagen y el contenedor responde.
- [ ] El repositorio contiene el Dockerfile y el README.
- [ ] La instancia EC2 está Running y la API usa PostgreSQL.
- [ ] La URL pública responde a `/actuator/health` y `/api/eventos`.
- [ ] Las pruebas de Postman pasan sobre el dominio público.
- [ ] La instancia estará activa durante la revisión del profesor y sus enlaces seguirán accesibles.
- [ ] `ENTREGA.md` contiene enlaces reales.

## Soluciones rápidas

| Problema | Qué revisar |
| --- | --- |
| `Port 8080 was already in use` | Detén la ejecución anterior de IntelliJ o el contenedor que usa 8080. |
| `Invalid protocol: get http:` | Quita GET/POST/PUT de la caja URL y selecciona el método en el desplegable. |
| IntelliJ no reconoce Spring | Importa `pom.xml` como Maven y recarga sus dependencias. |
| `release version 17 not supported` | Selecciona JDK 17 en Project SDK y Maven Runner JRE. |
| JDK 26 provoca problemas | Usa JDK 17, que es la versión configurada y verificada. |
| 400 al enviar JSON | Selecciona raw > JSON y usa nombres `nombre`, `fecha`, `lugar`, `correo`, `telefono` según la entidad. |
| 404 por identificador | Consulta primero el listado y usa un `id` que exista en esa base. |
| 409 en asistente | El correo ya existe; utiliza otro. |
| 409 en inscripción | Ese asistente ya está inscrito en ese evento. |
| `Conflict. The container name ... is already in use` | Detén y elimina el contenedor anterior o utiliza otro nombre. |
| `Cannot connect to Docker daemon` | Abre Docker Desktop y espera a que el motor quede iniciado. |
| `Could not resolve placeholder DB_URL` | Revisa la configuración de Compose y el perfil `prod`. La plantilla AWS los prepara automáticamente. |
| Driver no acepta URL | `DB_URL` debe comenzar con `jdbc:postgresql://`; revisa el ejemplo. |
| Conexión PostgreSQL rechazada | Revisa el estado del contenedor db, su healthcheck y las variables de Compose. |
| Docker build falla | Busca el primer error de los logs. El Dockerfile ejecuta pruebas y no genera una imagen si fallan. |
| La nube no muestra los datos locales | Cada entorno tiene su propia base. Crea los registros en el entorno que estés probando. |

Fuentes oficiales del procedimiento: <https://docs.aws.amazon.com/AWSCloudFormation/latest/TemplateReference/aws-resource-ec2-instance.html>, <https://docs.aws.amazon.com/AWSCloudFormation/latest/TemplateReference/cfn-signal.html> y <https://aws.amazon.com/free/>.
