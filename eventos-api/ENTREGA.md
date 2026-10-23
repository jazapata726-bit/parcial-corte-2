# Entrega del parcial de Programación II

API REST de eventos y asistentes, con arquitectura en capas y relación muchos a muchos. Desplegada con Docker y PostgreSQL en AWS EC2.

| Dato | Valor |
| --- | --- |
| Programa | Ingeniería de Software |
| Semestre | Tercero |
| Asignatura | Programación II |
| Tema | Eventos |
| Entidades | Evento y Asistente |
| Relación | Muchos a muchos (N:N) |
| Repositorio | https://github.com/jazapata726-bit/parcial-corte-2 |
| Despliegue público | http://13.59.121.203 |
| Estado de salud | http://13.59.121.203/actuator/health |
| Eventos | http://13.59.121.203/api/eventos |
| Asistentes | http://13.59.121.203/api/asistentes |
| Plataforma | AWS EC2, región us-east-2 |
| Verificación | 23 de octubre de 2026, hora de Colombia |
| Resultado de salud | HTTP 200, status UP |
| Pruebas sobre AWS | 33 solicitudes y 47 comprobaciones; 0 fallos |
| Pruebas Java locales | 14 pruebas de integración; 0 fallos y 0 errores |

## Archivos de la entrega

- [README](README.md): ejecución, modelo, arquitectura y endpoints.
- [Código fuente](src/main/java/com/parcial/eventos): Controller, Service, Repository y Entity.
- [Dockerfile](Dockerfile) y [Compose](compose.yaml): compilación y ejecución con PostgreSQL.
- [Postman](postman): colección y entornos local y AWS.
- [Diseño y sustentación](docs/DISENO_Y_SUSTENTACION.md).
- [Informe de Postman ejecutado en AWS](evidencias/04-postman-aws.json).
- [Resumen de resultados de AWS](evidencias/05-aws-resumen.json).
- [Plantilla de infraestructura AWS](aws/eventos-stack.json) y [guía de despliegue](aws/DESPLEGAR_AWS.md).

La instancia debe mantenerse en ejecución durante la evaluación. La dirección IPv4 pública puede cambiar si se detiene y vuelve a iniciar la instancia; en ese caso deben actualizarse estos enlaces y el entorno de Postman.
