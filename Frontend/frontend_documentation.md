# Documentación Técnica del Frontend — FinanceAI

Documento de arquitectura técnica, mapa de sitio, jerarquía de componentes y manual operativo del cliente web desarrollado con Astro y Tailwind CSS.

---

## 1. Mapa de Sitio y Arquitectura de Rutas

| Ruta | Archivo Astro | Tipo de Renderizado | Guardias de Acceso | Propósito | Componentes Clave |
| :--- | :--- | :---: | :---: | :--- | :--- |
| `/` | `index.astro` | SSR / Redirección | Condicional | Punto de entrada. Redirige a `/dashboard` si hay sesión o a `/login`. | `Layout` |
| `/login` | `login.astro` | SSR + Hidratación Cliente | Público | Autenticación de usuario: Login, Registro, Google OAuth2 y Recuperación de contraseña. | `Layout`, Formularios de Auth, Modales de Recuperación |
| `/dashboard` | `dashboard.astro` | SSR + Scripts de Cliente | Protegido (Token JWT) | Captura de ingresos/gastos (diario, semanal, mensual), cálculo de salud financiera y recomendaciones de IA. | `Layout`, Velocímetro de Score, Tarjetas KPI, Formulario de Movimientos |
| `/historial` | `historial.astro` | SSR + Scripts de Cliente | Protegido (Token JWT) | Visualización analítica histórica, gráfica multi-línea con picos, selector de mes y conversor multidivisa en tiempo real. | `Layout`, Gráfica de Línea Chart.js, Donut Chart, Tabla de Transacciones, Modal de Borrado |
| `/logout` | `logout.astro` | SSR | Público | Pantalla de confirmación y cierre seguro de sesión con difuminado de fondo. | `Layout`, Modal Centrado de Logout |

---

## 2. Sistema de Diseño y Tipografía

### Tipografía
- **Titulares, Logotipo y Etiquetas:** `Josefin Sans` (geométrica, moderna y corporativa).
- **Métricas, Tablas, Formularios y Datos:** `Inter` (alta legibilidad y neutralidad).

### Esquema de Color Dual (Modo Claro y Oscuro)
Configurado a través de clases utilitarias de Tailwind CSS y selector de tema persistente:
- **Modo Claro:**
  - Fondo de página: `bg-gray-50`
  - Tarjetas y paneles: `bg-white`
  - Bordes: `border-gray-200`
  - Textos principales: `text-gray-900`
  - Textos secundarios: `text-gray-500`
- **Modo Oscuro:**
  - Fondo de página: `dark:bg-gray-950`
  - Tarjetas y paneles: `dark:bg-gray-900`
  - Bordes: `dark:border-gray-800`
  - Textos principales: `dark:text-white`
  - Textos secundarios: `dark:text-gray-400`
- **Colores Semánticos de Estado:**
  - Primario / Acción: `blue-600` (`#2563eb`)
  - Éxito / Ingresos / Ahorro: `emerald-600` (`#059669`)
  - Alerta / Advertencia: `amber-500` (`#f59e0b`)
  - Peligro / Gastos / Deuda: `rose-600` (`#e11d48`) o `red-600` (`#dc2626`)

### Regla Estricta de Iconografía SVG (Cero Emojis)
- La interfaz no utiliza emojis crudos para elementos estructurales ni decorativos.
- Todos los indicadores visuales emplean vectores SVG limpios, accesibles y escalables.
- Los campos de texto en formularios cuentan con validación de expresiones regulares para evitar la inserción de emojis en descripciones de transacciones.

---

## 3. Catálogo de Estado y Almacenamiento Local (localStorage)

| Clave | Tipo de Dato | Propósito y Descripción |
| :--- | :---: | :--- |
| `financeai_token` | `string` | Token JWT emitido por el backend para autorización de peticiones en cabecera `Authorization: Bearer <token>`. |
| `financeai_user` | `JSON Object` | Datos de perfil del usuario en sesión (nombre, correo electrónico, identificador). |
| `financeai_preferred_currency` | `string` | Código de la divisa base configurada en el perfil del usuario (ejemplo: `USD`, `MXN`, `EUR`, `CRC`). |
| `financeai_all_transactions` | `JSON Array` | Historial acumulado completo de transacciones para renderizado instantáneo y filtros. |
| `financeai_dashboard_txns` | `JSON Array` | Transacciones activas del período en curso cargadas en el Dashboard. |
| `financeai_history_cleared` | `boolean` | Indicador de borrado intencional de historial por parte del usuario. |
| `financeai_avatar` | `string` | URL del avatar seleccionado de la cuadrícula de 9 avatares predeterminados. |
| `theme` | `string` | Modo de visualización seleccionado (`light` o `dark`). |

---

## 4. Motor Multidivisa y Conversión en Tiempo Real

### Matriz Oficial de Paridad (Base USD)

| Moneda | Código | Símbolo | Equivalencia en USD (1 Unidad) | Tipo de Cambio Calculado |
| :--- | :---: | :---: | :---: | :--- |
| Dólar estadounidense | `USD` | `$` | `$1.00` | 1 USD = $1.00 USD |
| Peso mexicano | `MXN` | `$` | `$0.0591` | 1 USD = $16.92 MXN |
| Euro | `EUR` | `€` | `$1.1687` | 1 USD = €0.8557 EUR |
| Colón costarricense | `CRC` | `₡` | `$0.0022` | 1 USD = ₡454.55 CRC |
| Peso colombiano | `COP` | `$` | `$0.000326` | 1 USD = $3,067.48 COP |
| Peso argentino | `ARS` | `$` | `$0.000667` | 1 USD = $1,499.25 ARS |
| Peso chileno | `CLP` | `$` | `$0.001093` | 1 USD = $914.91 CLP |
| Sol peruano | `PEN` | `S/` | `$0.2982` | 1 USD = S/ 3.35 PEN |

### Mecanismo de Conversión Reactiva
La función `convertAmount(amount, fromCur, toCur)` en `historial.astro` y `currency.ts` aplica la conversión matemática en memoria sin realizar peticiones bloqueantes de red:
$$\text{Monto Destino} = \frac{\text{Monto Origen} \times \text{Valor en USD de Moneda Origen}}{\text{Valor en USD de Moneda Destino}}$$

Al interactuar con los botones de divisa en la vista de Historial:
1. Se recalculan y formatean las tarjetas KPI de Ingresos, Gastos y Balance Neto.
2. Se actualizan las series de datos, etiquetas y escalas del eje Y de la gráfica multi-línea.
3. Se actualizan las cantidades en la leyenda del gráfico de dona.
4. Se convierten los valores individuales de cada fila en la tabla de movimientos.

---

## 5. Funcionalidades UX Interactivas

### Selector de Mes Personalizado Glassmorphic
- Sustituye los selectores nativos del navegador por un componente emergente con efecto translúcido (`backdrop-blur`).
- **Restricción de Negocio:** Bloquea meses y años futuros basándose en la fecha actual del cliente, y limita la selección histórica a un máximo de 24 meses atrás (2 años).

### Modal de Cierre de Sesión Centrado
- Renderizado con desenfoque de fondo profundo (`backdrop-blur-xl`), ocultando la navegación superior y aislando la acción crítica de confirmación.

### Zona de Peligro en Perfil
- El modal de perfil incluye una sección de advertencia de alto contraste para la eliminación irreversible de la cuenta, ejecutando un borrado atómico en base de datos (`DELETE /api/v1/users/profile`).

### Exportación a Excel (.CSV) con Codificación UTF-8 BOM
- Genera un archivo `.csv` estructurado con encabezado informativo, resumen de métricas, desglose por categoría y detalle de transacciones.
- Incluye el byte order mark `\uFEFF` para garantizar la apertura nativa en Microsoft Excel en Windows y macOS sin distorsión de caracteres con acentos.

---

## 6. Guía de Ejecución y Compilación del Frontend

### Requisitos
- Node.js versión 18.0.0 o superior (recomendado Node 20 LTS o 22 LTS).
- Gestor de paquetes `npm` o `pnpm`.

### Comandos de Desarrollo
```bash
# 1. Instalar dependencias
npm install

# 2. Iniciar servidor de desarrollo en puerto 4321
npm run dev

# 3. Compilar para producción (Standalone Node Adapter)
npm run build

# 4. Iniciar servidor compilado
node ./dist/server/entry.mjs
```
