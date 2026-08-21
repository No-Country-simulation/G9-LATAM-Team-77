# 🎨 FinanceAI — Frontend (Dashboard)

Interfaz interactiva construida con **Astro** y **Tailwind CSS v4** que consume la API del backend de Spring Boot. Permite ingresar datos financieros de un cliente y visualizar el análisis de IA en tiempo real.

---

## 🚀 Inicio rápido (desde cero)

### Requisitos previos

| Herramienta | Versión mínima | Verificar |
|-------------|---------------|-----------|
| Node.js     | `>= 22.12.0`  | `node -v` |
| pnpm        | cualquiera    | `pnpm -v` |

> Si no tienes **pnpm**, instálalo con:
> ```bash
> npm install -g pnpm
> ```

### 1. Instalar dependencias

```bash
cd Frontend
pnpm install
```

### 2. Levantar el servidor de desarrollo

```bash
pnpm run dev
```

El servidor arranca en **`http://localhost:4321`** (o el siguiente puerto libre) con hot-reload activado.  
También es accesible desde la red local gracias al flag `--host` ya incluido en el script.

### 3. Asegurarte de que el backend esté corriendo

El frontend usa `PUBLIC_API_URL` y, para desarrollo local, cae en `http://localhost:8080`. Sin el backend activo:
- El indicador de estado mostrará **"Backend offline"**.
- El botón **"Ejecutar Análisis con IA"** devolverá error de conexión.

---

## 📂 Estructura del proyecto

```
financeai-frontend/
├── src/
│   ├── pages/
│   │   ├── index.astro       # Redirección según estado de autenticación
│   │   ├── login.astro       # Login y registro con catálogos dinámicos
│   │   ├── dashboard.astro   # Resumen persistido + análisis IA
│   │   └── historial.astro   # Historial autenticado de análisis
│   ├── components/
│   │   ├── Header.astro      # Barra de navegación con toggle dark/light
│   │   └── Welcome.astro     # Componente de bienvenida
│   ├── layouts/
│   │   └── Layout.astro      # Layout base (head, fonts, estilos globales)
│   └── styles/
│       └── global.css        # Importación de Tailwind v4 + custom variants
├── public/                   # Archivos estáticos
├── astro.config.mjs          # Configuración de Astro + Tailwind via Vite plugin
├── package.json
└── pnpm-lock.yaml
```

---

## 🛠️ Stack tecnológico

| Tecnología | Rol |
|------------|-----|
| **Astro 7** | Framework SSG/SSR. Cada página es un `.astro` con frontmatter para lógica de servidor y HTML/JS en el cliente |
| **Tailwind CSS v4** | Estilos utilitarios. Integrado via `@tailwindcss/vite` (sin archivo de config separado) |
| **Chart.js 4** | Gráficas de barras para distribución de gastos (cargado desde CDN en `dashboard.astro`) |
| **pnpm** | Gestor de paquetes rápido |
| **Vitest** | Tests (`pnpm test`) |

---

## 🧩 Páginas y funcionalidad

### `/dashboard` — Dashboard principal (`dashboard.astro`)

El corazón del proyecto. Tiene dos paneles:

**Panel izquierdo — Formulario de entrada:**
- **Ingreso mensual** con selector de moneda (USD, MXN, EUR, ARS, COP, CLP, BRL, PEN, GTQ, BOB)
- **Nivel de endeudamiento** (0–100 %)
- **Frecuencia de ahorro** (Alta / Media / Baja)
- **Transacciones dinámicas** — se agregan/eliminan filas con categoría y valor; las categorías ya usadas se deshabilitan en los otros dropdowns

**Panel derecho — Resultados:**
- 🔵 **Score de Salud Financiera** (círculo con valor / 100)
- 🏷️ **Badge de perfil** del cliente
- 📊 **Análisis detallado**: mini-cards con métricas, lista de recomendaciones, tags de categorías detectadas
- 📉 **Gráfica de barras** con distribución de gastos por categoría (Chart.js)
- 🖥️ **JSON colapsable** con la respuesta raw del backend

### `/gastos` — Registro de gastos (`gastos.astro`)

Formulario para registrar un gasto individual:
- Monto, descripción, categoría (Alimentación, Transporte, Salud, Educación, Ocio) y fecha
- Panel lateral con estadísticas del mes y tendencia de gastos

---

## 🔌 Integración con el Backend

El frontend se conecta a `PUBLIC_API_URL` (fallback local `http://localhost:8080`). Entre las llamadas principales están:

### Health check (server-side en el frontmatter de Astro)
```
GET http://localhost:8080/api/v1/health
```
Muestra el estado de conexión en el header del dashboard al cargar la página.

### Análisis financiero con IA (client-side al hacer submit)
```
POST http://localhost:8080/api/v1/analisis-financiero
Content-Type: application/json
```

**Payload enviado:**
```json
{
  "ingreso_mensual": 4500,
  "nivel_endeudamiento": 25,
  "frecuencia_ahorro": "Media",
  "transacciones": [
    { "descripcion": "Supermercado", "valor": 600 },
    { "descripcion": "Streaming", "valor": 150 }
  ]
}
```

La respuesta JSON se renderiza directamente en el panel de resultados.

---

## ⚙️ Scripts disponibles

```bash
pnpm run dev      # Servidor de desarrollo con hot-reload (http://localhost:4321)
pnpm run build    # Build de producción en ./dist/
pnpm run preview  # Preview del build de producción
pnpm test         # Ejecuta los tests con Vitest
```

---

## 🌙 Dark Mode

El layout base aplica dark mode via la clase `.dark` en `<html>`. El Header incluye un botón toggle que persiste la preferencia en `localStorage` y respeta el `prefers-color-scheme` del sistema operativo.

---

## 🐛 Problemas comunes

| Síntoma | Causa probable | Solución |
|---------|---------------|----------|
| `Error de conexión con el backend` | Spring Boot no está corriendo | Iniciar el backend en el puerto 8080 |
| Puerto 4321 ocupado | Otro proceso usa el puerto | Astro elige automáticamente el siguiente disponible |
| Estilos de Tailwind no aparecen | Falta instalar dependencias | `pnpm install` |
| `node: command not found` | Node.js no instalado | Instalar Node.js >= 22.12.0 desde [nodejs.org](https://nodejs.org) |
# Autenticación del MVP

El flujo oficial utiliza correo/contraseña contra el Backend y conserva el JWT en `financeai_token` para las solicitudes protegidas. Google OAuth permanece deshabilitado en la interfaz hasta que el Backend pueda verificar de forma segura el token de Google y emitir su propio JWT; no debe agregarse una sincronización de usuario basada únicamente en datos enviados por el navegador.
