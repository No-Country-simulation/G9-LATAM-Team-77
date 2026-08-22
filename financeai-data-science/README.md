# 📊 FinanceAI — Capa de Data Science

Este módulo es el **corazón inteligente** de FinanceAI. Se encarga del procesamiento de datos, generación de variables (Feature Engineering), entrenamiento de modelos de Machine Learning y exposición de predicciones que el backend consume.

---

## 📂 Estructura del Proyecto

```
financeai-data-science/
├── data/
│   ├── Diccionario_datos_datasets_Hackaton_Team_77.xlsx   # Diccionario oficial de datos
│   ├── dataset_clasificacion_transacciones.csv            # Dataset para modelo de transacciones
│   ├── dataset_maestro_transacciones.csv                  # Dataset maestro completo
│   └── dataset_perfiles_financieros.csv                   # Dataset para modelo de perfiles
├── models/
│   ├── transaction_model.pkl   # Modelo entrenado: clasificador de transacciones
│   └── profile_model.pkl       # Modelo entrenado: clasificador de perfil financiero
├── notebooks/
│   ├── 01_ETL_construccion_datasets_financieros_v5_semantica_financiera.ipynb
│   └── run_etl.py              # Script ETL automatizado (versión ejecutable)
├── src/
│   ├── predict.py              # Motor de predicción e inferencia (entry point del backend)
│   ├── train_models.py         # Script de entrenamiento de ambos modelos
│   └── test_predict.py         # Pruebas del motor de predicción
├── requirements.txt
└── README.md
```

---

## 🛠️ Tecnologías

| Librería | Versión mínima | Uso |
|---|---|---|
| Python | 3.10+ | Lenguaje base |
| Pandas | ≥ 2.0.0 | Manipulación y limpieza de datos |
| NumPy | ≥ 1.24.0 | Operaciones numéricas |
| Scikit-Learn | ≥ 1.3.0 | Modelos ML (Logistic Regression, Random Forest) |
| Joblib | (incluido con sklearn) | Serialización de modelos `.pkl` |
| Jupyter | ≥ 1.0.0 | Análisis exploratorio e interactivo |
| Requests | ≥ 2.31.0 | Descarga de datos externos en el ETL |

---

## ⚡ Instalación rápida

```bash
# 1. Clona el repositorio (si aún no lo hiciste)
git clone <https://github.com/No-Country-simulation/G9-LATAM-Team-77.git>
cd financeai-data-science

# 2. Crea y activa un entorno virtual
python -m venv venv

# Windows
venv\Scripts\activate

# Linux / macOS
source venv/bin/activate

# 3. Instala las dependencias
pip install -r requirements.txt
```

---

## 🗃️ Diccionario de Datos

### `dataset_clasificacion_transacciones.csv`
Usado para entrenar el modelo que clasifica descripciones de gastos en categorías.

| Campo | Tipo | Descripción | Ejemplo |
|---|---|---|---|
| `descripcion` | `str` | Texto libre que describe el gasto | `"pago netflix mensual"` |
| `categoria` | `str` | Etiqueta de categoría (target) | `"Entretenimiento"` |

**Categorías válidas:**
`Alimentación` · `Transporte` · `Vivienda` · `Servicios` · `Salud y bienestar` · `Educación` · `Entretenimiento` · `Compras` · `Viajes` · `Cuidado personal` · `Regalos`

---

### `dataset_perfiles_financieros.csv`
Usado para entrenar el modelo que predice el perfil financiero de un usuario.

| Campo | Tipo | Descripción | Ejemplo |
|---|---|---|---|
| `ingreso_mensual_usd` | `float` | Ingreso mensual neto en USD | `2500.0` |
| `gastos_mensuales_usd` | `float` | Total de gastos en el mes | `1800.0` |
| `cuota_deuda_mensual_usd` | `float` | Pago mensual de deudas | `300.0` |
| `ahorro_total_acumulado_usd` | `float` | Ahorro acumulado total | `5000.0` |
| `ratio_ahorro_ingreso_anual` | `float` | Tasa de ahorro anual (0–1) | `0.18` |
| `capacidad_ahorro_mensual_estimada_usd` | `float` | Ahorro mensual posible | `400.0` |
| `tasa_capacidad_ahorro` | `float` | Ahorro mensual / ingreso mensual | `0.16` |
| `ratio_gastos_ingresos` | `float` | Gastos / Ingresos (0–1) | `0.72` |
| `ratio_compromisos_ingresos` | `float` | (Gastos + Deudas) / Ingresos | `0.84` |
| `porcentaje_gasto_esencial` | `float` | % del gasto que es esencial (0–1) | `0.65` |
| `porcentaje_gasto_discrecional` | `float` | % del gasto que es discrecional (0–1) | `0.35` |
| `cantidad_categorias_utilizadas` | `int` | Número de categorías distintas usadas | `6` |
| `perfil_financiero` | `str` | Etiqueta de perfil (target) | `"Saludable"` |

**Perfiles válidos:** `Saludable` · `En observación` · `En riesgo`

---

## 🚀 Cómo correr el entrenamiento

Los modelos ya están pre-entrenados en `/models/`. Si necesitas re-entrenarlos (por ejemplo, después de actualizar los datasets), ejecuta:

```bash
# Desde la raíz del módulo, con el entorno virtual activado
python src/train_models.py
```

Esto entrenará **ambos modelos en secuencia** y guardará los archivos `.pkl` en `models/`:
- `models/transaction_model.pkl` — Pipeline TF-IDF + Logistic Regression
- `models/profile_model.pkl` — Random Forest Classifier

Verás en consola la precisión de cada modelo sobre el conjunto de prueba (20%).

### Si quieres correr sólo el ETL primero

```bash
# Genera y descarga los datasets desde fuentes externas
python notebooks/run_etl.py
```

O bien, abre el notebook interactivo:

```bash
jupyter notebook notebooks/01_ETL_construccion_datasets_financieros_v5_semantica_financiera.ipynb
```

---

## 🧠 Motor de Predicción (`src/predict.py`)

El script `predict.py` es el **entry point** que el backend llama vía subproceso. Recibe un JSON por `stdin` o como argumento y devuelve un JSON por `stdout`.

Soporta **tres modos de operación**:

---

### Modo 1: `"transaction"` — Clasificar una transacción

**Input:**
```json
{
  "type": "transaction",
  "data": {
    "descripcion": "pago uber viaje trabajo"
  }
}
```

**Output:**
```json
{
  "status": "success",
  "prediction": "Transporte"
}
```

---

### Modo 2: `"profile"` — Predecir perfil financiero

**Input:**
```json
{
  "type": "profile",
  "data": {
    "ingreso_mensual_usd": 2500.0,
    "gastos_mensuales_usd": 1800.0,
    "cuota_deuda_mensual_usd": 300.0,
    "ahorro_total_acumulado_usd": 5000.0,
    "ratio_ahorro_ingreso_anual": 0.18,
    "capacidad_ahorro_mensual_estimada_usd": 400.0,
    "tasa_capacidad_ahorro": 0.16,
    "ratio_gastos_ingresos": 0.72,
    "ratio_compromisos_ingresos": 0.84,
    "porcentaje_gasto_esencial": 0.65,
    "porcentaje_gasto_discrecional": 0.35,
    "cantidad_categorias_utilizadas": 6
  }
}
```

**Output:**
```json
{
  "status": "success",
  "prediction": "En observación"
}
```

---

### Modo 3: `"full_analysis"` — Análisis completo con recomendaciones

Este es el modo principal que el backend usa. Recibe los datos del usuario más sus transacciones del mes.

**Campos de entrada:**

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `ingreso_mensual` | `float` | ✅ | Ingreso mensual neto en la moneda local |
| `nivel_endeudamiento` | `float` | ✅ | Porcentaje de endeudamiento (0–100) |
| `frecuencia_ahorro` | `str` | ✅ | `"Alta"`, `"Media"` o `"Baja"` |
| `transacciones` | `array` | ✅ | Lista de transacciones del período |
| `transacciones[].descripcion` | `str` | ✅ | Descripción de texto del gasto |
| `transacciones[].valor` | `float` | ✅ | Monto del gasto (positivo) |

**Ejemplo de input:**
```json
{
  "type": "full_analysis",
  "data": {
    "ingreso_mensual": 2000.0,
    "nivel_endeudamiento": 28.0,
    "frecuencia_ahorro": "Media",
    "transacciones": [
      { "descripcion": "supermercado semanal", "valor": 120.0 },
      { "descripcion": "pago netflix", "valor": 15.0 },
      { "descripcion": "gasolina carro", "valor": 80.0 },
      { "descripcion": "alquiler apartamento", "valor": 600.0 },
      { "descripcion": "farmacia medicamentos", "valor": 45.0 }
    ]
  }
}
```

**Campos de salida:**

| Campo | Tipo | Descripción |
|---|---|---|
| `status` | `str` | `"success"` o `"error"` |
| `perfil_financiero` | `str` | `"Saludable"`, `"En observación"` o `"En riesgo"` |
| `probabilidad` | `float` | Confianza del modelo (0–1) |
| `ingreso_mensual` | `float` | Ingreso recibido como input |
| `total_gastos` | `float` | Suma de todas las transacciones |
| `ahorro_estimado` | `float` | `ingreso_mensual - total_gastos` |
| `nivel_endeudamiento` | `float` | Nivel de endeudamiento recibido |
| `frecuencia_ahorro` | `str` | Frecuencia de ahorro recibida |
| `resumen_gastos` | `object` | Gastos agrupados por categoría |
| `transacciones_categorizadas` | `array` | Cada transacción con su categoría asignada |
| `categorias_detectadas` | `array` | Lista de categorías encontradas |
| `recomendaciones` | `array[str]` | Hasta 6 recomendaciones personalizadas |

**Ejemplo de output:**
```json
{
  "status": "success",
  "perfil_financiero": "En observación",
  "probabilidad": 0.72,
  "ingreso_mensual": 2000.0,
  "total_gastos": 860.0,
  "ahorro_estimado": 1140.0,
  "nivel_endeudamiento": 28.0,
  "frecuencia_ahorro": "Media",
  "resumen_gastos": {
    "Alimentación": 120.0,
    "Entretenimiento": 15.0,
    "Transporte": 80.0,
    "Vivienda": 600.0,
    "Salud y bienestar": 45.0
  },
  "transacciones_categorizadas": [
    { "descripcion": "supermercado semanal", "valor": 120.0, "categoria": "Alimentación" },
    { "descripcion": "pago netflix", "valor": 15.0, "categoria": "Entretenimiento" },
    { "descripcion": "gasolina carro", "valor": 80.0, "categoria": "Transporte" },
    { "descripcion": "alquiler apartamento", "valor": 600.0, "categoria": "Vivienda" },
    { "descripcion": "farmacia medicamentos", "valor": 45.0, "categoria": "Salud y bienestar" }
  ],
  "categorias_detectadas": ["Alimentación", "Entretenimiento", "Transporte", "Vivienda", "Salud y bienestar"],
  "recomendaciones": [
    "Tu gasto en vivienda (30.0% del ingreso) supera el 30% recomendado. Evalúa si hay oportunidad de renegociar el alquiler...",
    "Tienes una frecuencia de ahorro media. Intenta incrementarla gradualmente..."
  ]
}
```

---

## ✅ ¿Qué acepta el motor? ¿Qué no acepta?

### Datos de transacciones

| Caso | ¿Aceptado? | Notas |
|---|---|---|
| Descripción en español con palabras clave conocidas | ✅ | Mapeado directo sin modelo |
| Descripción en español libre / sin keywords | ✅ | El modelo TF-IDF clasifica |
| Descripción vacía `""` | ⚠️ | Devuelve categoría `"Otros"` |
| Descripción en inglés | ⚠️ | Funciona parcialmente (e.g. `"uber"`, `"netflix"`) |
| Valor de transacción negativo | ⚠️ | Técnicamente procesado, pero distorsiona los cálculos |
| Valor de transacción en `0` | ⚠️ | Procesado pero sin impacto en resúmenes |
| Descripción `null` o ausente | ❌ | Lanzará excepción — siempre enviar el campo |

### Datos del usuario (full_analysis)

| Caso | ¿Aceptado? | Notas |
|---|---|---|
| `ingreso_mensual` = 0 o negativo | ❌ | Genera división por cero en los ratios |
| `nivel_endeudamiento` entre 0 y 100 | ✅ | Rango esperado |
| `nivel_endeudamiento` fuera de 0–100 | ⚠️ | Procesado, pero las recomendaciones pueden ser inexactas |
| `frecuencia_ahorro` = `"Alta"`, `"Media"`, `"Baja"` | ✅ | Valores válidos |
| `frecuencia_ahorro` con otro valor | ⚠️ | No genera recomendación de ahorro; sin error |
| Lista `transacciones` vacía `[]` | ⚠️ | Válido; perfil calculado sólo por ingresos y deuda |
| JSON malformado | ❌ | Devuelve `{"error": "<mensaje>"}` |
| `type` con valor distinto a los 3 modos | ❌ | Devuelve `{"error": "Tipo inválido..."}` |

---

## 🧪 Ejecutar las pruebas

```bash
python src/test_predict.py
```

---

## 🔁 Flujo de Trabajo completo

```
1. ETL (notebooks/run_etl.py / notebook)
        ↓
   Genera los CSVs en /data/

2. Entrenamiento (src/train_models.py)
        ↓
   Guarda .pkl en /models/

3. Inferencia (src/predict.py)
        ↓
   Recibe JSON del backend → devuelve JSON con predicción + recomendaciones
```

---

## 🧲 Lógica de clasificación de transacciones

El motor usa un enfoque **híbrido**:

1. **Mapa manual de keywords** (rápido y determinista): Si la descripción contiene una palabra clave conocida (ej. `"netflix"`, `"supermercado"`, `"gasolina"`), se asigna la categoría directamente.
2. **Modelo ML** (fallback): Si no hay match en el mapa, se usa el pipeline TF-IDF + Logistic Regression entrenado.
3. **Fallback final**: Si el modelo no está disponible o falla, devuelve `"Otros"`.

---

## 📐 Umbrales de alerta por categoría

El sistema genera alertas cuando el gasto de una categoría supera estos porcentajes del ingreso mensual:

| Categoría | Umbral recomendado |
|---|---|
| Vivienda | 30% |
| Alimentación | 25% |
| Transporte | 15% |
| Servicios | 12% |
| Educación | 12% |
| Compras | 10% |
| Salud y bienestar | 10% |
| Viajes | 10% |
| Entretenimiento | 8% |
| Cuidado personal | 5% |
| Regalos | 5% |
