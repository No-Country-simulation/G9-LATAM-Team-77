# 📘 Guía Definitiva de Despliegue: FinanceAI en Oracle Cloud Infrastructure (OCI)

Esta guía documenta paso a paso el proceso completo, oficial y depurado para aprovisionar la infraestructura en **Oracle Cloud**, configurar la red, el servidor, el dominio gratuito con SSL, la base de datos MySQL local, el backend de Spring Boot con Data Science (Python 3.12) y el frontend de Astro SSR con Google OAuth 2.0.

---

## 🏗️ Arquitectura del Sistema Desplegado

```
                            [ Internet / Usuarios ]
                                       │ (HTTPS :443 / HTTP :80)
                                       ▼
                       ┌───────────────────────────────┐
                       │  Caddy Server (SSL Let's Enc.) │
                       └──────────────┬────────────────┘
                                      │
              ┌───────────────────────┴───────────────────────┐
              │ (Rutas Frontend /*)                           │ (Rutas API /api/v1/*)
              ▼                                               ▼
   ┌──────────────────────┐                       ┌──────────────────────────────┐
   │ Astro SSR (Node.js)  │                       │ Spring Boot 3 (Java 17)      │
   │ Puerto Interno: 4321 │                       │ Puerto Interno: 8080         │
   └──────────┬───────────┘                       └──────────────┬───────────────┘
              │                                                  │
              │ (Auth / Nodemailer)               ┌──────────────┴───────────────┐
              ▼                                   ▼                              ▼
      [ Gmail / Google OAuth ]          [ Python 3.12 (DS-08) ]         [ MySQL 8.0 (OCI VM) ]
                                        (scikit-learn, pandas)          (Persistencia /init.sql)
```

---

## ☁️ Fase 1: Configuración de Red en Oracle Cloud (OCI)

### 1.1 Crear la Red Virtual de la Nube (VCN)
1. En la consola de Oracle Cloud, navega a **Networking ➔ Virtual Cloud Networks**.
2. Haz clic en **Create VCN**:
   * **Name:** `financeai-vcn`
   * **IPv4 CIDR Block:** `10.0.0.0/16`
3. Clic en **Create VCN**.

### 1.2 Crear el Internet Gateway (IGW)
1. Dentro de `financeai-vcn`, ve a **Internet Gateways** en el menú lateral izquierdo.
2. Haz clic en **Create Internet Gateway**:
   * **Name:** `financeai-igw`
3. Clic en **Create Internet Gateway**.

### 1.3 Configurar la Tabla de Rutas (Route Table)
1. En el menú lateral de `financeai-vcn`, ve a **Route Tables** y abre **Default Route Table for financeai-vcn**.
2. Haz clic en **Add Route Rules**:
   * **Target Type:** `Internet Gateway`
   * **Destination CIDR Block:** `0.0.0.0/0`
   * **Target Internet Gateway:** Selecciona `financeai-igw`
3. Clic en **Add Route Rules**.

### 1.4 Configurar la Lista de Seguridad (Security List / Ingress Rules)
1. En el menú lateral de `financeai-vcn`, ve a **Security Lists** y abre **Default Security List for financeai-vcn**.
2. Haz clic en **Add Ingress Rules** y agrega las siguientes reglas para permitir el tráfico público:

| Source CIDR | IP Protocol | Destination Port Range | Descripción |
| :--- | :--- | :--- | :--- |
| `0.0.0.0/0` | TCP | `22` | Acceso SSH |
| `0.0.0.0/0` | TCP | `80` | Tráfico Web HTTP (Caddy / Certificados) |
| `0.0.0.0/0` | TCP | `443` | Tráfico Web Seguro HTTPS |
| `0.0.0.0/0` | TCP | `8080` | API Backend (Opcional) |

3. Clic en **Add Ingress Rules**.

### 1.5 Crear la Subred Pública (Public Subnet)
1. En el menú lateral de `financeai-vcn`, ve a **Subnets**.
2. Haz clic en **Create Subnet**:
   * **Name:** `public-subnet-financeai`
   * **Subnet Type:** `Regional`
   * **IPv4 CIDR Block:** `10.0.0.0/24`
   * **Route Table:** `Default Route Table for financeai-vcn`
   * **Subnet Access:** `Public Subnet`
   * **Security List:** `Default Security List for financeai-vcn`
3. Clic en **Create Subnet**.

---

## 🖥️ Fase 2: Aprovisionamiento de la Instancia de Cómputo (VM)

1. En la consola de Oracle Cloud, navega a **Compute ➔ Instances**.
2. Haz clic en **Create Instance**:
   * **Name:** `financeai-server`
   * **Placement / Availability Domain:** Cualquiera disponible (ej. `AD-1`).
   * **Image:** `Canonical Ubuntu 22.04 Minimal` o `Canonical Ubuntu 22.04 LTS`.
   * **Shape:** `VM.Standard.E5.Flex` (AMD) o `VM.Standard.A1.Flex` (Ampere ARM).
     * OCPUs: `1`
     * Memory: `8 GB RAM`
   * **Networking:**
     * Virtual cloud network: `financeai-vcn`
     * Subnet: `public-subnet-financeai`
     * Assign public IPv4 address: `Automatically assign public IPv4 address`
   * **SSH Keys:**
     * Selecciona **Generate a key pair for me** y haz clic en **Save Private Key** (guardar el archivo `ssh-key-*.key` en tu computadora).
3. Haz clic en **Create**.
4. Una vez en estado **Running**, copia la **Public IP Address** (ejemplo: `40.233.25.22`).

---

## 🔒 Fase 3: Configuración del Servidor y Firewall Interno

### 3.1 Conexión SSH desde Windows
Abre PowerShell en tu computadora y conéctate usando tu llave privada:

```powershell
ssh -i "C:\ruta\a\tu\ssh-key-2026-08-23.key" ubuntu@40.233.25.22
```

### 3.2 Desbloqueo del Firewall Interno de Ubuntu (`iptables`)
Ubuntu en Oracle Cloud bloquea puertos web en su firewall local por defecto. Ejecuta en el servidor:

```bash
# Abrir puertos 80, 443 y 8080 en iptables
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT

# Persistir las reglas tras reinicios
sudo apt update && sudo apt install -y netfilter-persistent iptables-persistent
sudo netfilter-persistent save
```

### 3.3 Instalación de Docker y Docker Compose
```bash
# Instalar dependencias previas
sudo apt update && sudo apt install -y curl git apt-transport-https ca-certificates gnupg lsb-release

# Agregar llave y repositorio oficial de Docker
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar Docker Engine y Plugin Compose
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Permitir ejecutar docker sin sudo
sudo usermod -aG docker $USER
newgrp docker
```

---

## 🌐 Fase 4: Dominio Gratuito y DNS (DuckDNS)

1. Ingresa a **[DuckDNS](https://www.duckdns.org/)** e inicia sesión.
2. En la sección **Domains**, crea tu subdominio (ej: `financeai-app`).
3. En el campo **IP**, introduce la IP Pública de tu instancia de OCI (ej: `40.233.25.22`).
4. Haz clic en **Update IP**. Tu dominio apuntará a `financeai-app.duckdns.org`.

---

## 🔑 Fase 5: Configuración de Google Cloud Console (OAuth 2.0)

1. Ingresa a **[Google Cloud Console ➔ Credentials](https://console.cloud.google.com/apis/credentials)**.
2. Selecciona tu **ID de cliente de OAuth 2.0** (Aplicación web).
3. Configura exactamente las siguientes URLs:

#### **Orígenes autorizados de JavaScript:**
* `https://financeai-app.duckdns.org`
* `http://financeai-app.duckdns.org`
* `http://localhost:4321`

#### **URIs de redireccionamiento autorizados:**
* `https://financeai-app.duckdns.org/api/auth/callback/google`
* `http://financeai-app.duckdns.org/api/auth/callback/google`
* `http://localhost:4321/api/auth/callback/google`

4. Haz clic en **Guardar**.

---

## 📁 Fase 6: Configuración del Proyecto y Código

### 6.1 Archivo de Variables de Entorno (`.env`)
En el servidor, crea el archivo `~/app/.env`:

```env
# ==========================================
# DOMINIO Y SSL (HTTPS)
# ==========================================
DOMAIN_NAME=financeai-app.duckdns.org

# ==========================================
# FRONTEND (Astro + Auth.js)
# ==========================================
SITE_URL=https://financeai-app.duckdns.org
AUTH_URL=https://financeai-app.duckdns.org
AUTH_SECRET=9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08
AUTH_TRUST_HOST=true
PUBLIC_API_URL=https://financeai-app.duckdns.org

# Google OAuth
GOOGLE_CLIENT_ID=tu-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu-google-client-secret

# ==========================================
# BACKEND (Spring Boot)
# ==========================================
JWT_SECRET=8a9b3c4d5e6f7g8h9i0j1k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7a8b9c0d1e2f3
CORS_ALLOWED_ORIGINS=https://financeai-app.duckdns.org,http://localhost:4321

# Gmail / Notificaciones
GMAIL_USER=tu-correo@gmail.com
GMAIL_PASS=tu-app-password-de-16-caracteres
ADMIN_EMAILS="tu-correo@gmail.com"

# ==========================================
# BASE DE DATOS LOCAL EN ORACLE (MySQL 8)
# ==========================================
MYSQLPORT=3306
MYSQL_DATABASE=railway
MYSQLUSER=root
MYSQL_ROOT_PASSWORD=TuPasswordSeguroDeBD2026!
```

### 6.2 `docker-compose.yml` (Orquestación de 4 Contenedores)
```yaml
services:
  database:
    image: mysql:8.0
    container_name: financeai-db
    restart: always
    env_file:
      - .env
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-RootSecurePass2026!}
      MYSQL_DATABASE: ${MYSQL_DATABASE:-railway}
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/database_schema.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -u root -p$${MYSQL_ROOT_PASSWORD:-RootSecurePass2026!} || exit 1"]
      interval: 5s
      timeout: 5s
      retries: 25
      start_period: 15s
    networks:
      - app-network

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: financeai-backend
    restart: always
    env_file:
      - .env
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://database:3306/${MYSQL_DATABASE:-railway}?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=${MYSQL_ROOT_PASSWORD:-RootSecurePass2026!}
      - SPRING_FLYWAY_BASELINE_ON_MIGRATE=true
      - JWT_SECRET=${JWT_SECRET}
      - PYTHON_COMMAND=/app/DataScient/venv/bin/python
      - DATA_SCIENCE_SCRIPT=/app/DataScient/src/predict.py
      - CORS_ALLOWED_ORIGINS=https://${DOMAIN_NAME:-localhost},http://localhost:4321
      - GMAIL_USER=${GMAIL_USER}
      - GMAIL_PASS=${GMAIL_PASS}
      - ADMIN_EMAILS=${ADMIN_EMAILS}
    depends_on:
      database:
        condition: service_healthy
    networks:
      - app-network

  frontend:
    build:
      context: ./Frontend
      dockerfile: Dockerfile
    container_name: financeai-frontend
    restart: always
    env_file:
      - .env
    environment:
      - SITE_URL=https://${DOMAIN_NAME:-localhost}
      - AUTH_URL=https://${DOMAIN_NAME:-localhost}
      - NEXTAUTH_URL=https://${DOMAIN_NAME:-localhost}
      - AUTH_SECRET=${AUTH_SECRET}
      - NEXTAUTH_SECRET=${AUTH_SECRET}
      - AUTH_TRUST_HOST=true
      - PUBLIC_API_URL=https://${DOMAIN_NAME:-localhost}
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
    depends_on:
      - backend
    networks:
      - app-network

  caddy:
    image: caddy:latest
    container_name: financeai-caddy
    restart: always
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./Caddyfile:/etc/caddy/Caddyfile
      - caddy_data:/data
      - caddy_config:/config
    environment:
      - DOMAIN_NAME=${DOMAIN_NAME}
    depends_on:
      - frontend
      - backend
    networks:
      - app-network

volumes:
  mysql_data:
  caddy_data:
  caddy_config:

networks:
  app-network:
    driver: bridge
```

### 6.3 `Caddyfile` (Proxy Inverso con SSL y Reenvío de Cabeceras)
```caddy
{$DOMAIN_NAME:localhost} {
    # Proxy para el API Backend
    handle /api/v1/* {
        reverse_proxy backend:8080 {
            header_up Host {host}
            header_up X-Real-IP {remote_host}
            header_up X-Forwarded-For {remote_host}
            header_up X-Forwarded-Proto https
            header_up X-Forwarded-Host {host}
            header_up X-Forwarded-Port 443
        }
    }

    # Proxy para el Frontend y Auth.js
    handle {
        reverse_proxy frontend:4321 {
            header_up Host {host}
            header_up X-Real-IP {remote_host}
            header_up X-Forwarded-For {remote_host}
            header_up X-Forwarded-Proto https
            header_up X-Forwarded-Host {host}
            header_up X-Forwarded-Port 443
        }
    }
}
```

### 6.4 `Dockerfile` del Backend (Ubuntu 24.04 + Java 17 + Python 3.12)
```dockerfile
FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    python3 \
    python3-pip \
    python3-venv \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY DataScient/ /app/DataScient/
RUN cd /app/DataScient && \
    python3 -m venv venv && \
    ./venv/bin/pip install --upgrade pip && \
    ./venv/bin/pip install -r requirements.txt

COPY Backend/financeia-backend/ /app/Backend/
RUN chmod +x /app/Backend/mvnw && \
    cd /app/Backend && \
    ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "/app/Backend/target/financeia-backend-0.0.1-SNAPSHOT.jar"]
```

### 6.5 `Frontend/Dockerfile` (Node 22 + pnpm 9 + Astro SSR)
```dockerfile
FROM node:22-alpine AS builder

WORKDIR /app

RUN corepack enable && corepack prepare pnpm@9 --activate

COPY package.json pnpm-lock.yaml* pnpm-workspace.yaml* ./
RUN pnpm install --no-frozen-lockfile

COPY . .

RUN npx astro build

FROM node:22-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV HOST=0.0.0.0
ENV PORT=4321

COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist

EXPOSE 4321

CMD ["node", "./dist/server/entry.mjs"]
```

---

## 🚀 Fase 7: Despliegue y Ejecución

En tu terminal SSH del servidor OCI:

```bash
# 1. Clonar repositorio y cambiar a la rama de trabajo
git clone https://github.com/No-Country-simulation/G9-LATAM-Team-77.git app
cd app
git checkout frontend_fernando

# 2. Configurar variables de entorno
nano .env # (Pega el contenido del paso 6.1)

# 3. Compilar y levantar todos los contenedores
docker compose up -d --build
```

---

## 🛠️ Fase 8: Comandos de Mantenimiento y Logs

* **Ver el estado de salud de todos los contenedores:**
  ```bash
  docker compose ps
  ```
* **Ver logs del Backend en tiempo real:**
  ```bash
  docker compose logs -f backend
  ```
* **Ver logs del Frontend en tiempo real:**
  ```bash
  docker compose logs -f frontend
  ```
* **Ver logs del servidor Caddy (HTTPS/SSL):**
  ```bash
  docker compose logs -f caddy
  ```
* **Verificar respuesta de salud del Backend:**
  ```bash
  curl -i https://financeai-app.duckdns.org/api/v1/health
  ```
* **Actualizar cambios de código desde Git:**
  ```bash
  git pull origin frontend_fernando
  docker compose up -d --build frontend
  ```
