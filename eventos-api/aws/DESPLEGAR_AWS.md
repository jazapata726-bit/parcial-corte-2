# Desplegar la API en AWS

Despliegue verificado el 23 de octubre de 2026: [API pública](http://13.59.121.203) y [salud UP](http://13.59.121.203/actuator/health). Esta guía describe cómo reproducir la infraestructura.

El archivo `eventos-stack.json` crea la infraestructura y arranca la aplicación con Docker. Incluye una copia comprimida del código necesario para compilarla; no necesita descargar tu repositorio de GitHub.

## Qué crea

| Recurso | Configuración |
| --- | --- |
| EC2 | Amazon Linux 2023, t3.micro por defecto, créditos de CPU standard |
| Disco EBS | 16 GiB, gp3, cifrado |
| Memoria de intercambio | 2 GiB para apoyar la compilación en la instancia pequeña |
| Red | VPC propia, subred pública, gateway y ruta de salida |
| Acceso público | Puerto HTTP 80 para la API |
| PostgreSQL | Contenedor con volumen persistente, sin puerto publicado |
| API | Imagen compilada con el Dockerfile del proyecto |
| Credencial de la base | Contraseña aleatoria creada dentro del servidor |

La plantilla no crea usuarios, claves de acceso ni roles IAM. El puerto SSH está cerrado. La señal de éxito de CloudFormation se envía después de compilar, arrancar los contenedores y comprobar `/actuator/health`.

## Coste y vigencia

Los recursos EC2, EBS y la dirección IPv4 pública pueden generar cargos. Comprueba **Billing > Free Tier / Credits** antes de crear la pila. Las condiciones dependen de tu cuenta. No se garantiza gratuidad. La plantilla no cambia el plan de tu cuenta ni contrata un Savings Plan o una instancia reservada.

## Opción con la consola de AWS

1. Extrae el ZIP del proyecto y localiza `aws/eventos-stack.json`.
2. Ingresa en tu cuenta de AWS. Elige la región **US East (Ohio), us-east-2**, como opción inicial para esta guía.
3. En la búsqueda de servicios escribe **CloudFormation** y abre el servicio.
4. Selecciona **Create stack > With new resources (standard)**.
5. En **Prepare template**, elige **Choose an existing template**.
6. En **Template source**, elige **Upload a template file**.
7. Selecciona `eventos-stack.json` y pulsa **Next**.
8. Stack name: `eventos-api-parcial`.
9. Conserva el parámetro de imagen Amazon Linux. En InstanceType conserva `t3.micro`; `t3.small` tiene más memoria y mayor coste.
10. Pulsa **Next**, deja las opciones de la pila por defecto y pulsa **Next** de nuevo.
11. Revisa el resumen de los recursos y el coste según tu cuenta. Pulsa **Submit / Create stack** cuando corresponda.
12. Revisa **Events**. La creación puede tardar varios minutos mientras se descargan las imágenes, se compila Java y se ejecutan las pruebas.
13. Espera **CREATE_COMPLETE**. El límite de espera de la señal de arranque está configurado en 30 minutos.
14. Abre **Outputs** y copia los valores reales de **ApiUrl**, **HealthUrl** y **EventosUrl**.
15. Abre HealthUrl: debe responder `{"status":"UP"}`.
16. Abre EventosUrl: una base nueva debe responder `[]`.
17. En Postman, cambia `baseUrl` por ApiUrl y ejecuta la colección completa.

La URL de esta configuración usa **HTTP** en el puerto 80. El parcial recibido no exige un dominio propio ni HTTPS. Utiliza únicamente datos de demostración. Si tu profesor exige HTTPS, se debe agregar esa configuración antes de entregar.

## Opción con AWS CloudShell

En CloudShell, usa **Actions > Upload file** para subir `eventos-stack.json`. Luego ejecuta:

```bash
aws cloudformation validate-template --template-body file://eventos-stack.json --region us-east-2
aws cloudformation create-stack --stack-name eventos-api-parcial --template-body file://eventos-stack.json --region us-east-2
```

Para seguir la creación y obtener los enlaces:

```bash
aws cloudformation wait stack-create-complete --stack-name eventos-api-parcial --region us-east-2
aws cloudformation describe-stacks --stack-name eventos-api-parcial --region us-east-2 --query 'Stacks[0].Outputs' --output table
```

Si ya existe una pila con ese nombre, revisa su estado antes de repetir la creación. No borres ni reemplaces pilas de otros proyectos.

## Evidencias para la entrega

1. CloudFormation con la pila en CREATE_COMPLETE.
2. Outputs mostrando las URLs reales.
3. EC2 mostrando la instancia `eventos-api-parcial` en Running.
4. Respuesta de HealthUrl con status UP.
5. CRUD de ambas entidades y relación N:N en Postman sobre ApiUrl.
6. **EC2 > Instances > selecciona la instancia > Actions > Monitor and troubleshoot > Get system log**: el registro incluye la compilación, `docker compose ps` y el mensaje de despliegue terminado.

Completa después `ENTREGA.md` con el enlace de GitHub y ApiUrl. Esos son los enlaces que se envían al profesor, junto con las evidencias que solicite.

## Si la creación falla

- Revisa el primer error de **CloudFormation > Events**.
- Si indica falta de cuota o permisos, corrige esa condición de la cuenta antes de repetir.
- Si falla la señal de ApiInstance, revisa **Get system log**. En el servidor, el registro completo se guarda en `/var/log/eventos-despliegue.log`.
- PostgreSQL no está abierto a internet; el acceso lo realiza la API por la red interna de Docker.
- El enlace público puede cambiar si detienes y vuelves a iniciar la instancia, porque utiliza una IPv4 pública asignada automáticamente. Mantén la instancia en ejecución durante la revisión del profesor.
- Los datos persisten al reiniciar los contenedores y la instancia; terminar la instancia o eliminar la pila elimina su disco. Guarda cualquier información necesaria antes de retirar recursos.

## Actualizar el código de la plantilla

Si modificas `pom.xml`, `Dockerfile`, `compose.yaml` o `src`, vuelve a generar la plantilla antes de un nuevo despliegue:

```bash
python3 scripts/generar_plantilla_aws.py
```

En Windows también puedes usar `py scripts/generar_plantilla_aws.py`. La plantilla generada comprueba la integridad del archivo comprimido antes de extraerlo.

## Retirar los recursos al terminar

Después de la calificación y de guardar las evidencias, puedes eliminar **la pila eventos-api-parcial** desde CloudFormation. Revisa que sea la pila de este proyecto: al eliminarla se retirarán el servidor, el disco con sus datos y la red creada por la plantilla.

## Fuentes oficiales

- [AWS Free Tier](https://aws.amazon.com/free/).
- [Uso de Free Tier en EC2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-free-tier-usage.html).
- [EC2 en CloudFormation](https://docs.aws.amazon.com/AWSCloudFormation/latest/TemplateReference/aws-resource-ec2-instance.html).
- [Señal de arranque cfn-signal](https://docs.aws.amazon.com/AWSCloudFormation/latest/TemplateReference/cfn-signal.html).
- [Instalación de Docker en Amazon Linux 2023](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/create-container-image.html).
