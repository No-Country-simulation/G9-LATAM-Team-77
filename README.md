# 📊 FinanceAl - Asistente Inteligente de Salud Financiera

![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow)
![Fase](https://img.shields.io/badge/Fase-Definición%20y%20Objetivos-blue)

> **Nota:** Este repositorio se encuentra en sus fases iniciales de desarrollo. La documentación y la arquitectura se irán actualizando progresivamente a medida que el proyecto avance.

## 🎯 El Desafío
En la actualidad, el mercado de fintechs, bancos digitales y plataformas de educación financiera está en constante expansión. Muchas personas tienen acceso a los datos brutos de sus transacciones, pero **tienen gran dificultad para transformar esa información en conocimiento útil** para la toma de decisiones. 

## 💡 Nuestro Objetivo
**FinanceAl** nace como una solución inteligente orientada a usuarios de carteras digitales e instituciones financieras. Nuestro objetivo principal es **analizar el comportamiento financiero de un usuario a partir de sus transacciones para generar una visión completa y clara de su salud financiera.**

Buscamos transformar datos financieros aislados en información procesable que permita a los usuarios:
- Organizar automáticamente sus gastos e ingresos.
- Entender exactamente hacia dónde se está dirigiendo su dinero.
- Identificar hábitos financieros (tanto positivos como de riesgo).
- Recibir recomendaciones simples y objetivas de mejora.

## 🚀 Alcance del Proyecto (MVP Propuesto)
Para lograr este objetivo, estamos construyendo un Producto Mínimo Viable (MVP) que integrará Inteligencia Artificial, desarrollo Backend y servicios Cloud. 

Las metas de desarrollo incluyen:
1. **Motor de Clasificación (Data Science):** Un modelo de Machine Learning capaz de clasificar automáticamente los gastos en categorías (Alimentación, Transporte, Salud, etc.) e identificar el perfil financiero del usuario (ej. *Saludable, En riesgo*).
2. **API REST (Backend):** Una interfaz de comunicación estructurada que recibirá los datos financieros del usuario, los procesará a través de nuestros modelos predictivos y devolverá los análisis en formato JSON.
3. **Infraestructura Cloud (OCI):** Despliegue de la solución utilizando los servicios de Oracle Cloud Infrastructure (como Object Storage o Compute) para garantizar escalabilidad.

## 🗓️ Próximos Pasos (Roadmap Inicial)
- [ ] Construcción y recolección del conjunto de datos (Dataset).
- [ ] Análisis Exploratorio de Datos (EDA) y limpieza.
- [ ] Entrenamiento del primer modelo de clasificación de gastos.
- [ ] Diseño de los endpoints de la API.
- [ ] Integración en la nube.

---
*Este documento será modificado progresivamente según los avances técnicos del proyecto.*

## Ejecución local con Docker Compose

La composición oficial usa únicamente `Frontend`, `Backend/financeia-backend` y
`financeai-data-science`. Levanta tres servicios: Frontend Astro, Backend Spring
Boot con Python 3.12 y MySQL 8.4.7. MySQL conserva sus datos en el volumen
`financeai_mysql_data` y no publica el puerto 3306 al equipo anfitrión.

### Configuración

1. Copia `.env.docker.example` como `.env.docker`.
2. Reemplaza todos los valores `change-me` con secretos independientes y seguros.
3. Conserva `PUBLIC_API_URL=http://localhost:8080` para que el navegador pueda
   acceder al Backend y `CORS_ALLOWED_ORIGINS=http://localhost:4321` para el
   Frontend local.

`.env.docker` está ignorado por Git y no debe compartirse ni incorporarse a las
imágenes. `AUTH_SECRET` pertenece al Frontend; `JWT_SECRET` pertenece al Backend.

### Comandos

Ejecuta todos los comandos desde la raíz del repositorio:

```powershell
docker compose --env-file .env.docker config
docker compose --env-file .env.docker build
docker compose --env-file .env.docker up -d
docker compose --env-file .env.docker ps
docker compose --env-file .env.docker logs
docker compose --env-file .env.docker restart
docker compose --env-file .env.docker down
```

El Backend queda disponible en `http://localhost:8080` y el Frontend en
`http://localhost:4321`. Flyway aplica automáticamente las migraciones V1 a V6
cuando inicia el Backend después de que MySQL esté saludable.

`docker compose down` elimina los contenedores y la red, pero conserva el volumen
de MySQL. No uses `docker compose down -v` como operación normal: la opción `-v`
elimina el volumen persistente y sus datos.

## Recuperación segura de contraseña

El flujo oficial se inicia desde **¿Olvidaste tu contraseña?** en `/login`:

1. El Frontend envía el correo a `POST /api/v1/auth/forgot-password`.
2. El Backend devuelve siempre la misma confirmación pública, exista o no la
   cuenta, para evitar enumeración de usuarios.
3. Para una cuenta existente genera 32 bytes con `SecureRandom`, guarda solo su
   hash SHA-256 y envía por SMTP un enlace a
   `/reset-password?token=<TOKEN>`.
4. `POST /api/v1/auth/reset-password` consume el token bajo bloqueo de base de
   datos, comprueba expiración y uso, cifra la nueva contraseña con BCrypt e
   invalida los tokens activos del usuario.

Los tokens expiran en 30 minutos de forma predeterminada, son de un solo uso y
nunca se registran ni se almacenan en texto plano. V6 crea
`password_reset_tokens`, su relación con `usuarios` y los índices de consulta.
Existe además un enfriamiento básico por hash de correo y por instancia. Para un
despliegue horizontal debe sustituirse por rate limiting centralizado (por
ejemplo, Redis o un API gateway) que también considere IP.

### Variables SMTP

Configura estas variables en un `.env` local del Backend o en `.env.docker`:

| Variable | Uso |
| --- | --- |
| `MAIL_HOST` | Servidor SMTP |
| `MAIL_PORT` | Puerto SMTP, normalmente 587 con STARTTLS |
| `MAIL_USERNAME` | Usuario de una cuenta SMTP dedicada |
| `MAIL_PASSWORD` | Contraseña o App Password SMTP |
| `MAIL_FROM` | Remitente visible autorizado por el proveedor |
| `MAIL_SMTP_AUTH` | Activa autenticación SMTP |
| `MAIL_STARTTLS` | Activa STARTTLS |
| `FRONTEND_URL` | Base pública usada para construir el enlace |
| `PASSWORD_RESET_EXPIRATION_MINUTES` | Vigencia del token; predeterminado 30 |
| `PASSWORD_RESET_COOLDOWN_SECONDS` | Pausa por correo/instancia; predeterminado 60 |

Para una demostración con Gmail debe usarse una App Password nueva y exclusiva,
nunca una contraseña personal ni una credencial encontrada en otra rama. Los
archivos `.env` y `.env.docker` reales están ignorados por Git; los archivos
`*.example` contienen únicamente placeholders.

En Docker Compose las variables se pasan solo al Backend. Si SMTP no está
configurado, los servicios pueden iniciar, pero el envío real no funcionará. La
prueba automatizada sustituye el servicio de correo y jamás contacta un servidor
externo. La validación manual de entrega requiere configurar SMTP localmente,
solicitar el enlace, cambiar la contraseña, comprobar que la anterior falla y
confirmar que el enlace no puede reutilizarse.
