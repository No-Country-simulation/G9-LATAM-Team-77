# Documentación del Módulo de Data Science (FinanceAI)

## 1. Funcionamiento de la IA y Modelos Utilizados
El módulo de Data Science contiene un motor de predicción y recomendaciones (`predict.py`) alimentado por dos modelos de Machine Learning entrenados en `train_models.py`.

### A. Modelo de Clasificación de Transacciones
*   **Algoritmo:** Regresión Logística (`LogisticRegression`) apoyado por vectorización de texto TF-IDF (`TfidfVectorizer` con `max_features=5000`).
*   **Funcionalidad:** Toma la descripción de una transacción (texto) y predice a qué categoría de gasto pertenece (ej. "Alimentación", "Transporte").
*   **Implementación:** Se entrena usando una división 80/20 y se exporta usando `joblib` a `transaction_model.pkl`. En el momento de la predicción, el motor utiliza primero un mapeo estático de palabras clave y, si no encuentra coincidencias, utiliza este modelo como *respaldo* (*fallback*).

### B. Modelo de Perfil Financiero
*   **Algoritmo:** Bosque Aleatorio (`RandomForestClassifier` con `n_estimators=100`).
*   **Funcionalidad:** Evalúa múltiples variables financieras del usuario para clasificar su perfil en tres posibles categorías de riesgo: "Saludable", "En observación" o "En riesgo".
*   **Implementación:** Se entrena con 8 variables numéricas. Se guarda en `profile_model.pkl` junto con la lista de las variables (features) utilizadas.

---

## 2. Diccionario de Datos de Entrenamiento

El sistema procesa dos tipos principales de datos basados en los archivos de datos (ej. `spending_patterns_detailed.csv` y los generados en `data/processed/`):

### Transacciones (Crudo/Procesado)
| Columna / Variable | Descripción | Uso en Modelo |
|--------------------|-------------|---------------|
| `descripcion` / `Artículo` | Texto descriptivo del gasto (ej. "Leche", "Uber", "Electricidad"). | Característica (X) para modelo de Transacciones (TF-IDF). |
| `categoria` / `Categoría` | Categoría principal del gasto (ej. "Comestibles", "Transporte"). | Objetivo (y) para modelo de Transacciones. |
| `valor` / `Total Gastado` | Monto de la transacción. | Usado para el cálculo de características de perfil (agregación). |

### Variables del Perfil Financiero (Dataset Procesado)
| Variable | Descripción | Rol en ML |
|----------|-------------|-----------|
| `ingreso_mensual_usd` | Ingresos totales mensuales en USD. | Característica |
| `nivel_endeudamiento` | Porcentaje de endeudamiento sobre los ingresos (0-100). | Característica |
| `gasto_total_usd` | Suma de gastos en el mes. | Característica |
| `ratio_gastos_ingresos` | Relación entre gasto total e ingreso mensual. | Característica |
| `porcentaje_gasto_esencial` | % de los gastos correspondientes a necesidades básicas (Alimentación, Vivienda, etc.). | Característica |
| `porcentaje_gasto_discrecional`| % de gastos en ocio, compras, etc. | Característica |
| `concentracion_categoria_principal` | % del gasto que representa la categoría más alta frente al gasto total. | Característica |
| `cantidad_categorias_utilizadas` | Número de diferentes categorías con al menos 1 gasto en el mes. | Característica |
| `perfil_financiero` | Etiqueta final de riesgo: "Saludable", "En observación", o "En riesgo". | Objetivo (y) |

---

## 3. Diagrama de Flujo: Proceso de Predicción y Reentrenamiento

```mermaid
graph TD
    subgraph Entrenamiento (train_models.py)
        A[Datos CSV Procesados] --> B(Tfidf + LogisticRegression)
        A --> C(RandomForestClassifier)
        B -->|Guarda| D[transaction_model.pkl]
        C -->|Guarda| E[profile_model.pkl]
    end

    subgraph Inferencia y Predicción (predict.py)
        F[Petición JSON] --> G{Tipo de Solicitud?}
        
        G -->|"transaction"| H[Extraer descripción]
        H --> I{Mapeo Reglas Duras?}
        I -->|Si| J[Retornar Categoría]
        I -->|No| K[Cargar transaction_model.pkl]
        K --> L[Inferencia ML]
        L --> J
        
        G -->|"profile"| M[Extraer variables numéricas]
        M --> N[Cargar profile_model.pkl]
        N --> O[Inferencia ML de Riesgo]
        O --> P[Retornar Perfil]
        
        G -->|"full_analysis"| Q[Calcular Totales y Ratios de Transacciones]
        Q --> R[Cargar Ambos Modelos]
        R --> S[Clasificar Transacciones]
        S --> T[Calcular Perfil Financiero]
        T --> U[Generar Recomendaciones por Matrices/Reglas]
        U --> V[Retornar Resumen y Alertas]
    end
```

---

## 4. Estructura Exacta del JSON Esperado y Devuelto

### A. Endpoint `transaction`
**JSON Esperado:**
```json
{
  "type": "transaction",
  "descripcion": "compra en supermercado walmart",
  "moneda": "USD"
}
```
**JSON Devuelto:**
```json
{
  "status": "success",
  "prediction": "Alimentación",
  "prediccion": "Alimentación",
  "categoria": "Alimentación",
  "moneda": "USD"
}
```

### B. Endpoint `profile`
**JSON Esperado:**
```json
{
  "type": "profile",
  "ingreso_mensual_usd": 3000,
  "gastos_mensuales_usd": 2500,
  "nivel_endeudamiento": 40,
  "moneda": "USD",
  "porcentaje_gasto_esencial": 0.6,
  "porcentaje_gasto_discrecional": 0.4,
  "concentracion_categoria_principal": 0.3,
  "cantidad_categorias_utilizadas": 5
}
```

**JSON Devuelto:**
```json
{
  "status": "success",
  "prediction": "En observación",
  "perfil_financiero": "En observación",
  "moneda": "USD"
}
```

### C. Endpoint `full_analysis`
**JSON Esperado:**
```json
{
  "type": "full_analysis",
  "ingreso_mensual": 4000,
  "deuda_total": 800,
  "ahorro_mensual": 200,
  "frecuencia_ahorro": "Media",
  "transacciones": [
    {
      "descripcion": "Pago de luz",
      "valor": 60
    },
    {
      "descripcion": "Supermercado",
      "valor": 250
    }
  ]
}
```
**JSON Devuelto:**
```json
{
  "status": "success",
  "perfil_financiero": "Saludable",
  "probabilidad": 0.85,
  "ingreso_mensual": 4000.0,
  "total_gastos": 310.0,
  "ahorro_estimado": 3690.0,
  "nivel_endeudamiento": 20.0,
  "frecuencia_ahorro": "Media",
  "moneda": "USD",
  "resumen_gastos": {
    "Servicios": 60.0,
    "Alimentación": 250.0
  },
  "transacciones_categorizadas": [
    {
      "descripcion": "Pago de luz",
      "valor": 60.0,
      "categoria": "Servicios"
    },
    {
      "descripcion": "Supermercado",
      "valor": 250.0,
      "categoria": "Alimentación"
    }
  ],
  "categorias_detectadas": [
    "Servicios",
    "Alimentación"
  ],
  "recomendaciones": [
    "¡Vas muy bien! Sigue manteniendo tus gastos por debajo de lo que ganas.",
    "Ahorras una parte pequeña de lo que te sobra. Intenta aumentar el porcentaje poco a poco.",
    "Tu nivel de endeudamiento es manejable, sigue así.",
    "Tu situación financiera es sólida. Sigue con tus buenos hábitos."
  ]
}
```
