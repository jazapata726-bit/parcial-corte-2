# Resultados de verificación

| Archivo | Resultado y alcance |
| --- | --- |
| [01-pruebas-java.txt](01-pruebas-java.txt) | 14 pruebas locales de integración; 0 fallos y 0 errores |
| [02-postman-resumen.json](02-postman-resumen.json) | JAR local: 33 solicitudes y 47 comprobaciones; 0 fallos |
| [03-validacion-aws.txt](03-validacion-aws.txt) | Validación estática de la plantilla y del script de arranque |
| [04-postman-aws.json](04-postman-aws.json) | Informe completo de Newman ejecutado sobre la URL pública de AWS |
| [05-aws-resumen.json](05-aws-resumen.json) | AWS: 33 solicitudes y 47 comprobaciones; 0 fallos; salud UP |

La API pública es http://13.59.121.203. Las pruebas usan datos de demostración y limpian los registros que crearon. El informe de AWS permite revisar cada petición, respuesta y comprobación.

El registro de arranque de EC2 observado muestra PostgreSQL Healthy y el contenedor de la API Started. Las consultas HTTP finales comprueban que el servicio responde desde internet.

La persistencia después de detener y reiniciar el JAR se comprobó en local con H2. El reinicio de los contenedores en AWS no se realizó en esta verificación.

Al publicar los archivos, GitHub Actions construirá el Dockerfile y ejecutará la colección con PostgreSQL, además de comprobar el reinicio del contenedor. Su resultado debe consultarse en la ejecución correspondiente.
