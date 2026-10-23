# Verificaciones realizadas

Fecha: **23 de octubre de 2026**, hora de Colombia.

## Resultados

| Comprobación | Resultado |
| --- | --- |
| Compilación local con Java 17 y Maven 3.9.11 | BUILD SUCCESS |
| JUnit / Spring Boot / MockMvc / H2 | 14 pruebas, 0 fallos, 0 errores, 0 omitidas |
| JAR como servidor HTTP real | Verificado |
| Postman sobre el JAR local | 33 solicitudes, 47 comprobaciones, 0 fallos |
| Persistencia de H2 al detener y reiniciar el JAR | Se conservaron evento, asistente e inscripción |
| Plantilla CloudFormation para us-east-2 | cfn-lint sin errores ni advertencias |
| Arranque de EC2 | Sintaxis Bash válida; UserData dentro del límite de 16 KiB |
| Código incorporado en la plantilla | 27 archivos comparados; SHA-256 verificada |
| Docker en AWS EC2 | Registro observado: PostgreSQL Healthy y contenedor API Started |
| Aplicación pública en AWS | http://13.59.121.203 responde HTTP 200 |
| Salud pública | /actuator/health responde HTTP 200 y status UP |
| CRUD, relación N:N y validaciones en AWS | 33 solicitudes, 47 comprobaciones, 0 fallos |
| Persistencia al reiniciar contenedores en AWS | No ejecutada durante esta verificación |
| GitHub Actions | Preparado; se ejecuta al publicar en main |

## Evidencia de AWS

La aplicación está desplegada en la instancia i-06a08ea4b614cec9b, región us-east-2. El informe de [Newman](../evidencias/04-postman-aws.json) contiene las solicitudes, respuestas y comprobaciones reales de la prueba pública. El [resumen](../evidencias/05-aws-resumen.json) incluye la fecha y hora de verificación en Colombia y las consultas de salud y listado realizadas al finalizar.

La colección crea sus propios eventos y asistentes, comprueba las inscripciones y elimina los registros que creó. Los identificadores son dinámicos y los correos de prueba incluyen un sufijo por ejecución.

## Cobertura

- CRUD completo de Evento y Asistente.
- Relación N:N con dos eventos, dos asistentes y tres inscripciones.
- Consulta y retiro de inscripciones.
- Conservación de eventos, asistentes y otras inscripciones al eliminar una relación o una entidad.
- Validación de campos obligatorios y correo.
- Rechazo de correos e inscripciones duplicados.
- Respuestas HTTP 400, 404 y 409.
- Salud de la aplicación y respuestas JSON.

Las pruebas Java locales usan capas reales y H2, sin simular Repository ni Service. También cubren JSON, fechas, identificadores y campos desconocidos inválidos.

## Repetir las pruebas

Desde PowerShell, en la carpeta del proyecto:

```powershell
.\mvnw.cmd clean verify
```

Con Newman instalado, para comprobar la API de AWS:

```powershell
newman run postman/Eventos_API.postman_collection.json -e postman/Eventos_AWS.postman_environment.json
```

También se pueden importar estos dos archivos en Postman y ejecutar la colección completa en orden.

## Alcance pendiente

Al publicar los archivos, revisar GitHub Actions y guardar su resultado. La comprobación pública documentada acredita la API y su CRUD; no sustituye una captura del estado final de CloudFormation si el docente la solicita. El reinicio de los contenedores en AWS se comprueba por separado; la prueba de reinicio ya realizada corresponde al JAR local con H2.
