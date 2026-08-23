# 📊 FinanceAI - Asistente Inteligente de Salud Financiera

![Status](https://img.shields.io/badge/Status-MVP%20Validado-brightgreen)
![Fase](https://img.shields.io/badge/Fase-Entrega-blue)
![Backend](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen)
![Data Science](https://img.shields.io/badge/AI%2FML-Python-blue)

> **Nota:** Este repositorio contiene el Producto Mínimo Viable (MVP) de **FinanceAI**. El Frontend, el Backend, la base de datos y los modelos de Machine Learning están implementados e integrados.

## 🎯 El Desafío
En la actualidad, el mercado de fintechs, bancos digitales y plataformas de educación financiera está en constante expansión. Muchas personas tienen acceso a los datos brutos de sus transacciones, pero **tienen gran dificultad para transformar esa información en conocimiento útil** para la toma de decisiones financieras a nivel personal.

## 💡 Nuestro Objetivo
**FinanceAI** es una solución inteligente orientada a usuarios de carteras digitales e instituciones financieras. Nuestro objetivo principal es **analizar el comportamiento financiero de un usuario a partir de sus transacciones para generar una visión completa y clara de su salud financiera.**

Buscamos transformar datos financieros aislados en información procesable que permita a los usuarios:
- Organizar y categorizar automáticamente sus gastos e ingresos.
- Entender exactamente hacia dónde se está dirigiendo su dinero mediante dashboards interactivos.
- Identificar hábitos financieros y perfiles de usuario (ej. *Saludable, En riesgo*).
- Recibir recomendaciones simples y objetivas de mejora.

## 🛠️ Arquitectura y Tecnologías
El proyecto está estructurado en módulos especializados para garantizar escalabilidad, seguridad y eficiencia:

### ⚙️ Backend (Java / Spring Boot)
El núcleo de la aplicación es una API REST robusta que gestiona la lógica de negocio y la seguridad:
- **Framework:** Spring Boot (Java).
- **Seguridad:** Spring Security stateless con autenticación basada en JWT.
- **Base de Datos:** MySQL 8.4 con migraciones Flyway V1 a V6.
- **Documentación:** Swagger / OpenAPI para la exploración y prueba de endpoints.
- **Módulos principales:** autenticación, usuarios, transacciones, catálogos, Dashboard, análisis financiero e historial autenticado.

### 🖥️ Frontend (Astro)
La interfaz consume la API real y ofrece:
- Registro e inicio de sesión con Honduras/HNL disponibles en los catálogos.
- Dashboard financiero, análisis mediante Data Science e historial por usuario.
- Recuperación de contraseña, cierre de sesión y protección de rutas privadas.
- Estados de carga, error y contenido vacío, además de diseño responsive.

### 🧠 Motor de Clasificación (Data Science)
Procesamiento de datos y modelado predictivo:
- **Runtime oficial:** `financeai-data-science` con Python 3.12.
- **Machine Learning:** modelos serializados para categorizar transacciones y determinar el perfil financiero.
- **Integración:** el Backend ejecuta `src/predict.py` mediante un proceso con timeout y manejo seguro de errores.

### 🐳 Infraestructura local
- Docker Compose ejecuta Frontend, Backend con el runtime de Data Science y MySQL.
- MySQL utiliza un volumen persistente y Flyway actualiza el esquema al iniciar.
- El despliegue en **Oracle Cloud Infrastructure (OCI)** permanece como una etapa posterior y no forma parte de este cierre del MVP.

## 🚀 Módulos de la API
Actualmente, la API expone los siguientes controladores principales:
- `AuthController`: registro, inicio de sesión y recuperación segura de contraseña.
- `TransactionController`: Registro y consulta del historial de transacciones.
- `AnalisisController`: Evaluación del perfil financiero y categorización de gastos mediante IA.
- `DashboardController`: Generación de métricas y resúmenes para la interfaz de usuario.
- `UserController`: Administración de perfiles de usuario.
- `CatalogoController`: Consulta de países y monedas disponibles.
- `HistorialAnalisisController`: Consulta autenticada del historial propio de análisis.

## 🗓️ Roadmap del Proyecto
- [x] Construcción y recolección de los conjuntos de datos (Datasets).
- [x] Análisis Exploratorio de Datos (EDA) y limpieza.
- [x] Entrenamiento del modelo de clasificación de transacciones y perfiles financieros.
- [x] Diseño y desarrollo de los endpoints de la API REST (Spring Boot).
- [x] Implementación de capa de seguridad con JWT.
- [x] Documentación de la API con OpenAPI/Swagger.
- [x] Integración del Frontend con la API REST.
- [x] Integración del runtime de Data Science.
- [x] Historial autenticado, Honduras/HNL y recuperación de contraseña.
- [x] Entorno reproducible con Docker Compose.
- [x] Despliegue e integración en la nube (OCI).

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

### Variables de entorno principales

El archivo `.env.docker.example` documenta la configuración de la composición.
Los valores reales deben existir únicamente en `.env.docker`:

| Variable | Uso |
| --- | --- |
| `MYSQL_DATABASE` | Nombre de la base de datos |
| `MYSQL_USER` | Usuario de aplicación de MySQL |
| `MYSQL_PASSWORD` | Contraseña del usuario de aplicación |
| `MYSQL_ROOT_PASSWORD` | Contraseña administrativa del contenedor MySQL |
| `JWT_SECRET` | Firma de los tokens emitidos por el Backend |
| `AUTH_SECRET` | Secreto independiente utilizado por el Frontend |
| `PUBLIC_API_URL` | URL pública del Backend consumida por el navegador |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos por el Backend |
| `DATA_SCIENCE_TIMEOUT_SECONDS` | Tiempo máximo de ejecución del proceso Python |

Para ejecutar el Backend sin Docker también deben configurarse `DB_URL`,
`DB_USERNAME`, `DB_PASSWORD`, `PYTHON_COMMAND` y `DATA_SCIENCE_SCRIPT` de acuerdo
con las rutas y credenciales del entorno local.

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

---
*Desarrollado por el Equipo 77 - Hackathon LATAM G9.*
