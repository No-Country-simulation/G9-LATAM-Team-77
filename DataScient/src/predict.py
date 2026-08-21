"""
predict.py — Motor de predicción y recomendaciones FinanceAI
=============================================================
Soporta tres tipos de solicitud:
  • type = "transaction"  → clasifica una descripción de gasto
  • type = "profile"      → predice el perfil financiero con el modelo ML
  • type = "full_analysis"→ análisis completo: clasificación de transacciones +
                            perfil + recomendaciones específicas por categoría
"""

import sys
import json
import os

try:
    import joblib
except ImportError:
    joblib = None

try:
    import pandas as pd
except ImportError:
    pd = None

# ─────────────────────────────────────────────────────────────────────────────
# MAPA: subcategoría/descripción → categoría principal
# Basado en mapeo_categorias_subcategorias.csv
# ─────────────────────────────────────────────────────────────────────────────
SUBCATEGORIA_A_CATEGORIA = {
    # Alimentación
    "supermercado": "Alimentación", "restaurante": "Alimentación",
    "cafetería": "Alimentación", "cafeteria": "Alimentación",
    "comida rápida": "Alimentación", "comida rapida": "Alimentación",
    "mercado": "Alimentación", "alimentos": "Alimentación",
    # Transporte
    "combustible": "Transporte", "gasolina": "Transporte",
    "transporte público": "Transporte", "transporte publico": "Transporte",
    "taxi": "Transporte", "uber": "Transporte",
    "mantenimiento vehicular": "Transporte", "bus": "Transporte",
    # Vivienda
    "alquiler": "Vivienda", "renta": "Vivienda",
    "mantenimiento del hogar": "Vivienda", "hogar": "Vivienda",
    # Servicios
    "electricidad": "Servicios", "agua": "Servicios",
    "gas domiciliario": "Servicios", "gas": "Servicios",
    "internet": "Servicios", "telefonía": "Servicios", "telefonia": "Servicios",
    "celular": "Servicios", "teléfono": "Servicios", "telefono": "Servicios",
    # Salud y bienestar
    "consulta médica": "Salud y bienestar", "consulta medica": "Salud y bienestar",
    "odontología": "Salud y bienestar", "odontologia": "Salud y bienestar",
    "farmacia": "Salud y bienestar", "medicamentos": "Salud y bienestar",
    "gimnasio": "Salud y bienestar", "gym": "Salud y bienestar",
    "entrenamiento": "Salud y bienestar", "salud": "Salud y bienestar",
    "médico": "Salud y bienestar", "medico": "Salud y bienestar",
    # Educación
    "matrícula": "Educación", "matricula": "Educación",
    "cursos": "Educación", "curso": "Educación",
    "libros": "Educación", "libro": "Educación",
    "educación": "Educación", "educacion": "Educación",
    "colegio": "Educación", "universidad": "Educación",
    "plataforma de aprendizaje": "Educación",
    # Entretenimiento
    "streaming": "Entretenimiento", "netflix": "Entretenimiento",
    "spotify": "Entretenimiento", "cine": "Entretenimiento",
    "eventos": "Entretenimiento", "videojuegos": "Entretenimiento",
    "juegos": "Entretenimiento", "entretenimiento": "Entretenimiento",
    "ocio": "Entretenimiento", "suscripción": "Entretenimiento",
    "suscripcion": "Entretenimiento",
    # Compras
    "ropa": "Compras", "calzado": "Compras", "zapatos": "Compras",
    "electrónica": "Compras", "electronica": "Compras",
    "vehículo": "Compras", "vehiculo": "Compras",
    # Viajes
    "alojamiento": "Viajes", "hotel": "Viajes",
    "vuelo": "Viajes", "turismo": "Viajes", "viaje": "Viajes",
    "avión": "Viajes", "avion": "Viajes",
    # Cuidado personal
    "higiene": "Cuidado personal", "peluquería": "Cuidado personal",
    "peluqueria": "Cuidado personal", "barbería": "Cuidado personal",
    "barberia": "Cuidado personal", "cuidado de la piel": "Cuidado personal",
    # Regalos
    "regalo": "Regalos", "flores": "Regalos",
    "joyería": "Regalos", "joyeria": "Regalos",
    "juguetes": "Regalos",
}

# ─────────────────────────────────────────────────────────────────────────────
# UMBRALES DE ALERTA por categoría (% del ingreso mensual)
# ─────────────────────────────────────────────────────────────────────────────
UMBRALES_CATEGORIA = {
    "Alimentación":      0.25,   # Hasta 25% del ingreso
    "Transporte":        0.15,   # Hasta 15%
    "Entretenimiento":   0.08,   # Hasta 8%
    "Compras":           0.10,   # Hasta 10%
    "Viajes":            0.10,
    "Cuidado personal":  0.05,
    "Regalos":           0.05,
    "Servicios":         0.12,
    "Salud y bienestar": 0.10,
    "Educación":         0.12,
    "Vivienda":          0.30,
}

# ─────────────────────────────────────────────────────────────────────────────
# RECOMENDACIONES ESPECÍFICAS POR CATEGORÍA
# ─────────────────────────────────────────────────────────────────────────────
RECOMENDACIONES_CATEGORIA = {
    "Alimentación": {
        "alta": [
            "Tu gasto en alimentación ({pct:.1f}% del ingreso) supera el umbral recomendado del 25%. "
            "Considera planificar un menú semanal y hacer una sola compra grande en el supermercado para reducir gastos.",
            "El gasto en comida puede reducirse cocinando en casa al menos 5 días a la semana; "
            "esto puede representar un ahorro de hasta el 40% respecto a comer fuera.",
        ],
        "normal": [
            "Tu gasto en alimentación es razonable. Mantén el hábito de planificar tus compras con anticipación."
        ],
    },
    "Transporte": {
        "alta": [
            "Estás destinando {pct:.1f}% de tu ingreso a transporte, por encima del 15% recomendado. "
            "Evalúa combinar transporte público con tu vehículo o usar aplicaciones de carpooling.",
            "Revisa si el mantenimiento preventivo de tu vehículo está al día; "
            "un vehículo bien mantenido consume hasta 20% menos de combustible.",
        ],
        "normal": [
            "Tu gasto en transporte es adecuado. Considera registrar el consumo de combustible para detectar variaciones."
        ],
    },
    "Entretenimiento": {
        "alta": [
            "Tu gasto en entretenimiento ({pct:.1f}% del ingreso) es elevado para el umbral recomendado del 8%. "
            "Revisa tus suscripciones activas y cancela las que usas menos de 2 veces al mes.",
            "Consolida servicios de streaming: muchos planes familiares cuestan lo mismo que dos suscripciones individuales.",
        ],
        "normal": [
            "Tu gasto en entretenimiento está controlado. Asegúrate de revisar anualmente tus suscripciones activas."
        ],
    },
    "Compras": {
        "alta": [
            "Estás gastando {pct:.1f}% de tu ingreso en compras discrecionales. "
            "Implementa la regla de las 72 horas: espera 3 días antes de realizar compras no planificadas mayores.",
            "Distingue entre necesidades y deseos. Las compras de ropa y electrónica pueden diferirse fácilmente 1-2 meses.",
        ],
        "normal": [
            "Tus compras están dentro de un rango aceptable. Recuerda que las ofertas son buenas solo si ya planeabas comprar el artículo."
        ],
    },
    "Vivienda": {
        "alta": [
            "Tu gasto en vivienda ({pct:.1f}% del ingreso) supera el 30% recomendado. "
            "Evalúa si hay oportunidad de renegociar el alquiler o reducir costos de mantenimiento.",
            "Considera si compartir vivienda temporalmente podría liberarte capital para cancelar deudas.",
        ],
        "normal": [
            "Tu gasto en vivienda es proporcional a tu ingreso. Revisa si puedes refinanciar en mejores condiciones."
        ],
    },
    "Servicios": {
        "alta": [
            "Tus gastos en servicios ({pct:.1f}% del ingreso) son elevados. "
            "Revisa el consumo de electricidad y agua; pequeños cambios de hábito pueden reducirlos hasta un 15%.",
            "Compara proveedores de internet y telefonía; el mercado suele ofrecer mejores planes a clientes que negocian.",
        ],
        "normal": [
            "Tus gastos en servicios básicos son razonables. Registra variaciones mes a mes para detectar anomalías."
        ],
    },
    "Salud y bienestar": {
        "alta": [
            "Estás invirtiendo {pct:.1f}% de tu ingreso en salud. Evalúa si un seguro médico podría ser más rentable "
            "que los gastos directos actuales.",
        ],
        "normal": [
            "Tu gasto en salud es adecuado. Prioriza la medicina preventiva para evitar gastos mayores en el futuro."
        ],
    },
    "Educación": {
        "alta": [
            "Tu inversión en educación es del {pct:.1f}% del ingreso. Es una inversión de largo plazo, "
            "pero asegúrate de que no comprometa tus gastos esenciales.",
        ],
        "normal": [
            "Tu gasto en educación es sostenible. Busca becas, cupones o plataformas gratuitas como complemento."
        ],
    },
    "Viajes": {
        "alta": [
            "Estás destinando {pct:.1f}% del ingreso a viajes. Planifica tus viajes con 3-6 meses de anticipación "
            "para aprovechar tarifas más económicas.",
            "Considera crear un fondo específico de viajes para no afectar tu presupuesto mensual.",
        ],
        "normal": [
            "Tus gastos en viajes son moderados. Reserva con anticipación para conseguir mejores precios."
        ],
    },
    "Cuidado personal": {
        "alta": [
            "Tu gasto en cuidado personal ({pct:.1f}%) es superior al recomendado. "
            "Evalúa qué servicios puedes hacer en casa y cuáles realmente necesitan profesional.",
        ],
        "normal": [
            "Tus gastos de cuidado personal son adecuados."
        ],
    },
    "Regalos": {
        "alta": [
            "El gasto en regalos ({pct:.1f}%) está por encima del promedio. "
            "Establece un presupuesto fijo anual para regalos y planifica con anticipación fechas importantes.",
        ],
        "normal": [
            "Tu gasto en regalos es razonable. Considera alternativas creativas y personalizadas que suelen ser más valoradas."
        ],
    },
}

# ─────────────────────────────────────────────────────────────────────────────
# RECOMENDACIONES POR PERFIL FINANCIERO
# ─────────────────────────────────────────────────────────────────────────────
RECOMENDACIONES_PERFIL = {
    "Saludable": [
        "Tu situación financiera es sólida. Sigue con tus buenos hábitos."
    ],
    "En observación": [
        "Tu situación requiere atención. Aplica las recomendaciones anteriores antes de que empeore."
    ],
    "En riesgo": [
        "Tu situación financiera necesita cambios urgentes. Prioriza las recomendaciones marcadas y considera buscar asesoría adicional."
    ],
}

# ─────────────────────────────────────────────────────────────────────────────
# UTILIDADES
# ─────────────────────────────────────────────────────────────────────────────
def _safe_float(val, default: float = 0.0) -> float:
    """Convierte de manera segura a float con valor por defecto."""
    if val is None:
        return default
    try:
        return float(val)
    except (ValueError, TypeError):
        return default


def _safe_str(val, default: str = "") -> str:
    """Convierte de manera segura a string limpio con valor por defecto."""
    if val is None:
        return default
    return str(val).strip()


def _clasificar_descripcion(descripcion: str, pipeline) -> str:
    """Clasifica con el modelo ML; fallback al mapa manual."""
    desc_lower = _safe_str(descripcion).lower()
    
    if not desc_lower:
        return "Otros"
        
    # Intentar mapa manual primero (más rápido y determinista)
    for keyword, cat in SUBCATEGORIA_A_CATEGORIA.items():
        if keyword in desc_lower:
            return cat
            
    # Fallback al modelo
    if pipeline is not None:
        try:
            return pipeline.predict([descripcion])[0]
        except Exception:
            pass
    return "Otros"


def _generar_recomendaciones(
    transacciones_categorizadas: list[dict],
    ingreso_mensual: float,
    nivel_endeudamiento: float,
    frecuencia_ahorro: str,
    perfil_financiero: str,
) -> list[str]:
    """
    Genera recomendaciones usando las Matrices 1 y 2 y alertas por categoría.
    """
    recomendaciones = []

    gastos_por_categoria: dict[str, float] = {}
    for t in transacciones_categorizadas:
        cat = t.get("categoria", "Otros")
        gastos_por_categoria[cat] = gastos_por_categoria.get(cat, 0.0) + _safe_float(t.get("valor", 0.0))

    total_gastos = sum(gastos_por_categoria.values())
    ratio_gasto_ingreso = total_gastos / ingreso_mensual if ingreso_mensual > 0 else 1.0

    # --- Alertas por categoría si superan umbral ---
    if ingreso_mensual > 0:
        for cat, gasto in gastos_por_categoria.items():
            umbral = UMBRALES_CATEGORIA.get(cat)
            if umbral and (gasto / ingreso_mensual) > umbral:
                pct = (gasto / ingreso_mensual) * 100.0
                rec_cat_list = RECOMENDACIONES_CATEGORIA.get(cat, {}).get("alta", [])
                if rec_cat_list:
                    recomendaciones.append(rec_cat_list[0].format(pct=pct))

    # --- MATRIZ 1: Variables Individuales ---
    # Gasto/Ingreso
    if ratio_gasto_ingreso <= 0.70:
        estado_gasto = "bien"
        rec_gasto = "¡Vas muy bien! Sigue manteniendo tus gastos por debajo de lo que ganas."
    elif ratio_gasto_ingreso <= 0.90:
        estado_gasto = "regular"
        rec_gasto = "Estás gastando casi todo lo que ganas. Revisa en qué categoría puedes recortar un poco."
    else:
        estado_gasto = "mal"
        rec_gasto = "Reduce gastos en categorías no esenciales (ocio, streaming, etc.) — estás gastando más de lo que ganas."

    # Ahorro
    frecuencia_clean = _safe_str(frecuencia_ahorro, "Media").lower()
    if frecuencia_clean == "alta":
        estado_ahorro = "bien"
        rec_ahorro = "Excelente hábito de ahorro, ¡sigue así!"
    elif frecuencia_clean == "media":
        estado_ahorro = "regular"
        rec_ahorro = "Ahorras una parte pequeña de lo que te sobra. Intenta aumentar el porcentaje poco a poco."
    else:
        estado_ahorro = "mal"
        rec_ahorro = "Empieza a apartar un monto fijo cada mes, aunque sea pequeño."

    # Endeudamiento
    if nivel_endeudamiento <= 20:
        estado_deuda = "bien"
        rec_deuda = "Tu nivel de endeudamiento es manejable, sigue así."
    elif nivel_endeudamiento <= 35:
        estado_deuda = "regular"
        rec_deuda = "Tu deuda es moderada. Evita adquirir nuevos compromisos por ahora."
    else:
        estado_deuda = "mal"
        rec_deuda = "No pidas más préstamos y prioriza pagar lo que ya debes."

    # --- MATRIZ 2: Combinaciones Críticas ---
    mal_count = sum([estado_gasto == "mal", estado_ahorro == "mal", estado_deuda == "mal"])
    
    if mal_count >= 2:
        if mal_count == 3:
            recomendaciones.append("Tu situación financiera necesita atención inmediata en varios frentes. Te recomendamos buscar orientación financiera profesional además de aplicar los cambios sugeridos.")
        else:
            if estado_gasto == "mal" and estado_deuda == "mal":
                recomendaciones.append("Estás gastando más de lo que ganas y además tienes deudas altas. Es momento de hacer un plan de pago urgente y evitar nuevos gastos no esenciales.")
            elif estado_ahorro == "mal" and estado_deuda == "mal":
                recomendaciones.append("No tienes un fondo de respaldo y ya tienes deudas altas — cualquier imprevisto puede complicar tu situación. Prioriza armar un pequeño colchón mientras pagas tu deuda.")
            elif estado_gasto == "mal" and estado_ahorro == "mal":
                recomendaciones.append("Este es el combo más urgente de corregir: gastas más de lo que ganas y no estás ahorrando nada. Empieza por registrar todos tus gastos esta semana para ver exactamente a dónde se va tu dinero.")
    else:
        # Solo aplicar mensajes individuales si no hay una alerta combinada crítica
        recomendaciones.extend([rec_gasto, rec_ahorro, rec_deuda])

    # --- Mensaje general según perfil (Matriz 1 final) ---
    perfil_recs = RECOMENDACIONES_PERFIL.get(perfil_financiero, [])
    if perfil_recs:
        recomendaciones.append(perfil_recs[0])

    return recomendaciones


# ─────────────────────────────────────────────────────────────────────────────
# FUNCIÓN PRINCIPAL
# ─────────────────────────────────────────────────────────────────────────────
def predict(input_json: str) -> str:
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

    try:
        # 0. Procesamiento Robusto de JSON
        if input_json is None:
            req = {}
        elif isinstance(input_json, dict):
            req = input_json
        else:
            cleaned_input = str(input_json).strip()
            if not cleaned_input:
                req = {}
            else:
                try:
                    req = json.loads(cleaned_input)
                except Exception:
                    req = {}

        if not isinstance(req, dict):
            req = {}

        req_type = req.get("type")
        raw_data = req.get("data")
        if isinstance(raw_data, dict):
            # Combinar datos anidados y de raíz para máxima compatibilidad
            data = {**req, **raw_data}
        else:
            data = req

        # Moneda por defecto (fallback): default a 'USD'
        moneda = data.get("moneda") or req.get("moneda") or "USD"
        moneda = _safe_str(moneda, "USD").upper() if moneda else "USD"

        # Validación explícita de tipos no soportados
        valid_types = {"transaction", "profile", "full_analysis"}
        if req_type and req_type not in valid_types:
            return json.dumps({"error": f"Tipo inválido '{req_type}'. Usa 'transaction', 'profile' o 'full_analysis'"}, ensure_ascii=False)

        # Inferencia automática de tipo si no se especifica
        if not req_type:
            if "descripcion" in data or "description" in data:
                req_type = "transaction"
            else:
                req_type = "full_analysis"

        # ── 1. Clasificación de transacción individual ──────────────────────
        if req_type == "transaction":
            model_path = os.path.join(base_dir, "models", "transaction_model.pkl")
            pipeline = joblib.load(model_path) if os.path.exists(model_path) else None
            desc = _safe_str(data.get("descripcion") or data.get("description") or req.get("descripcion") or "")
            categoria = _clasificar_descripcion(desc, pipeline)
            return json.dumps({
                "status": "success",
                "prediction": categoria,
                "prediccion": categoria,
                "categoria": categoria,
                "moneda": moneda,
            }, ensure_ascii=False)

        # ── 2. Perfil financiero (modelo ML) ───────────────────────────────
        elif req_type == "profile":
            model_path = os.path.join(base_dir, "models", "profile_model.pkl")
            if not os.path.exists(model_path):
                # Fallback basado en reglas si no está el archivo pkl
                ingreso = _safe_float(data.get("ingreso_mensual_usd") or data.get("ingreso_mensual") or data.get("ingresoMensual"), 0.0)
                deuda = _safe_float(data.get("nivel_endeudamiento") or data.get("nivelEndeudamiento") or data.get("cuota_deuda_mensual_usd"), 0.0)
                gasto = _safe_float(data.get("gastos_mensuales_usd") or data.get("gastos_mensuales") or data.get("gastoMensual"), 0.0)
                if deuda > 50 or (ingreso > 0 and gasto > ingreso):
                    prediction = "En riesgo"
                elif deuda > 30 or (ingreso > 0 and gasto > 0.8 * ingreso):
                    prediction = "En observación"
                else:
                    prediction = "Saludable"
                return json.dumps({
                    "status": "success",
                    "prediction": prediction,
                    "perfil_financiero": prediction,
                    "moneda": moneda,
                }, ensure_ascii=False)

            clf, features = joblib.load(model_path)
            feature_aliases = {
                "ingreso_mensual_usd": ["ingreso_mensual_usd", "ingreso_mensual", "ingresoMensual", "ingreso"],
                "gastos_mensuales_usd": ["gastos_mensuales_usd", "gastos_mensuales", "gastoMensual", "gastos", "gasto_total_usd"],
                "gasto_total_usd": ["gasto_total_usd", "gastos_mensuales_usd", "gastos_mensuales", "gastoMensual"],
                "cuota_deuda_mensual_usd": ["cuota_deuda_mensual_usd", "deudaTotal", "deuda_total"],
                "nivel_endeudamiento": ["nivel_endeudamiento", "nivelEndeudamiento", "endeudamiento"],
                "ahorro_total_acumulado_usd": ["ahorro_total_acumulado_usd", "ahorroMensual", "ahorro_mensual"],
                "ratio_gastos_ingresos": ["ratio_gastos_ingresos", "ratio_gastos"],
                "porcentaje_gasto_esencial": ["porcentaje_gasto_esencial"],
                "porcentaje_gasto_discrecional": ["porcentaje_gasto_discrecional"],
                "concentracion_categoria_principal": ["concentracion_categoria_principal"],
                "cantidad_categorias_utilizadas": ["cantidad_categorias_utilizadas"],
            }

            df_data = {}
            for feat in features:
                val = None
                aliases = feature_aliases.get(feat, [feat])
                for alias in aliases:
                    if alias in data:
                        val = data[alias]
                        break
                    elif alias in req:
                        val = req[alias]
                        break
                df_data[feat] = [_safe_float(val, 0.0)]

            df = pd.DataFrame(df_data)
            try:
                prediction = clf.predict(df)[0]
            except Exception:
                prediction = "En observación"

            return json.dumps({
                "status": "success",
                "prediction": prediction,
                "perfil_financiero": prediction,
                "moneda": moneda,
            }, ensure_ascii=False)

        # ── 3. Análisis completo ────────────────────────────────────────────
        elif req_type == "full_analysis":
            ingreso_mensual = _safe_float(
                data.get("ingreso_mensual") or data.get("ingresoMensual") or data.get("ingreso_mensual_usd") or data.get("ingreso"),
                0.0
            )
            gasto_mensual_directo = _safe_float(
                data.get("gastos_mensuales") or data.get("gastoMensual") or data.get("gastos_mensuales_usd") or data.get("gasto_total_usd") or data.get("gastos"),
                0.0
            )
            deuda_total = _safe_float(
                data.get("deuda_total") or data.get("deudaTotal") or data.get("cuota_deuda_mensual_usd"),
                0.0
            )
            ahorro_mensual = _safe_float(
                data.get("ahorro_mensual") or data.get("ahorroMensual") or data.get("ahorro_total_acumulado_usd"),
                0.0
            )
            nivel_endeudamiento = _safe_float(
                data.get("nivel_endeudamiento") or data.get("nivelEndeudamiento") or data.get("endeudamiento"),
                0.0
            )
            frecuencia_ahorro = _safe_str(
                data.get("frecuencia_ahorro") or data.get("frecuenciaAhorro"),
                ""
            )
            # Procesar transacciones
            transacciones_raw = data.get("transacciones") or data.get("transactions") or []
            if not isinstance(transacciones_raw, list):
                transacciones_raw = []

            tx_pipeline = None
            tx_model_path = os.path.join(base_dir, "models", "transaction_model.pkl")
            if os.path.exists(tx_model_path):
                try:
                    tx_pipeline = joblib.load(tx_model_path)
                except Exception:
                    pass

            transacciones_categorizadas = []
            resumen_gastos = {}

            for t in transacciones_raw:
                if not isinstance(t, dict):
                    continue
                desc = _safe_str(t.get("descripcion") or t.get("description") or t.get("categoria") or "")
                valor = _safe_float(t.get("valor") if t.get("valor") is not None else (t.get("monto") if t.get("monto") is not None else t.get("amount")), 0.0)
                cat = _safe_str(t.get("categoria") or t.get("category") or "")
                if not cat:
                    cat = _clasificar_descripcion(desc, tx_pipeline)
                transacciones_categorizadas.append({
                    "descripcion": desc,
                    "valor": valor,
                    "categoria": cat,
                })
                resumen_gastos[cat] = resumen_gastos.get(cat, 0.0) + valor

            total_gastos = sum(resumen_gastos.values())
            if total_gastos == 0.0 and gasto_mensual_directo > 0.0:
                total_gastos = gasto_mensual_directo
                resumen_gastos["Otros"] = gasto_mensual_directo

            # Auto-cálculo de Deuda si no se ingresó o es 0
            if nivel_endeudamiento == 0.0:
                if deuda_total > 0 and ingreso_mensual > 0:
                    nivel_endeudamiento = min(100.0, (deuda_total / ingreso_mensual) * 100.0)
                else:
                    gastos_fijos = sum(v for k, v in resumen_gastos.items() if k in ["Vivienda", "Servicios"])
                    if ingreso_mensual > 0:
                        nivel_endeudamiento = min(100.0, (gastos_fijos / ingreso_mensual) * 100.0)
                    else:
                        nivel_endeudamiento = 0.0
            nivel_endeudamiento = round(nivel_endeudamiento, 1)

            # Auto-cálculo de Frecuencia de Ahorro
            ahorro_estimado = max(0.0, ingreso_mensual - total_gastos)
            ratio_ahorro_ingreso = (ahorro_estimado / ingreso_mensual) if ingreso_mensual > 0 else 0.0
            
            if not frecuencia_ahorro or frecuencia_ahorro in ["Media", "AUTO"]:
                if ratio_ahorro_ingreso >= 0.25:
                    frecuencia_ahorro = "Alta"
                elif ratio_ahorro_ingreso >= 0.08:
                    frecuencia_ahorro = "Media"
                else:
                    frecuencia_ahorro = "Baja"

            # ─────────────────────────────────────────────────────────────────
            # MOTOR DE EXPLICABILIDAD DS-08 Y SCORING OFICIAL PR-01
            # ─────────────────────────────────────────────────────────────────
            # Normalización de periodicidad si se especifica
            periodicidad = _safe_str(data.get("periodicidad") or data.get("periodo"), "mensual").lower()
            factor_mensual = 1.0
            if "semanal" in periodicidad:
                factor_mensual = 4.33
            elif "quincenal" in periodicidad:
                factor_mensual = 2.0

            ingreso_mensual_normalizado = ingreso_mensual * factor_mensual
            total_gastos_normalizado = total_gastos * factor_mensual

            # Puntos Componente 1: Gasto/Ingreso (Máx 34 pts)
            ratio_gastos = (total_gastos_normalizado / ingreso_mensual_normalizado) if ingreso_mensual_normalizado > 0 else 2.0
            if ratio_gastos <= 0.60:
                pts_gasto = 34.0
            elif ratio_gastos <= 0.90:
                pts_gasto = 17.0
            else:
                pts_gasto = 0.0

            # Puntos Componente 2: Ahorro Real (Máx 33 pts)
            excedente = max(0.0, ingreso_mensual - total_gastos)
            ratio_ahorro_ingreso = (excedente / ingreso_mensual) if ingreso_mensual > 0 else 0.0
            tasa_ahorro_pct = round(ratio_ahorro_ingreso * 100.0, 1)

            if ingreso_mensual > 0 and excedente > 0:
                if ratio_ahorro_ingreso >= 0.20:
                    pts_ahorro = 33.0
                elif ratio_ahorro_ingreso >= 0.05:
                    pts_ahorro = 16.0
                else:
                    pts_ahorro = 0.0
            else:
                pts_ahorro = 0.0

            # Puntos Componente 3: Nivel de Endeudamiento (Máx 33 pts)
            tasa_deuda_pct = round(nivel_endeudamiento, 1)
            if nivel_endeudamiento <= 15.0:
                pts_deuda = 33.0
            elif nivel_endeudamiento <= 35.0:
                pts_deuda = 16.0
            else:
                pts_deuda = 0.0

            score_total = max(0.0, min(100.0, pts_gasto + pts_ahorro + pts_deuda))

            # 1.1 Clasificación de Nivel (DS-08)
            if score_total >= 70:
                nivel = "Saludable"
                probabilidad = 0.92
            elif score_total >= 40:
                nivel = "Riesgo"
                probabilidad = 0.75
            else:
                nivel = "Crítico"
                probabilidad = 0.40

            # 1.2 Identificación de Causa Principal (Menor % relativo)
            pilares = {
                "gasto_ingreso": {"pts": pts_gasto, "max": 34.0, "nombre": "tu relación Gasto/Ingreso"},
                "ahorro": {"pts": pts_ahorro, "max": 33.0, "nombre": "tu hábito de ahorro del excedente"},
                "endeudamiento": {"pts": pts_deuda, "max": 33.0, "nombre": "tu nivel de endeudamiento"}
            }
            for p in pilares.values():
                p["pct"] = (p["pts"] / p["max"]) * 100.0 if p["max"] else 0.0

            causa_clave, causa_datos = min(pilares.items(), key=lambda kv: kv[1]["pct"])

            # 1.3 Generación de Mensaje en 3 Capas (Sin emojis, para renderizar con SVGs en Frontend)
            diagnosticos = {
                "Saludable": f"Tu salud financiera está en nivel Saludable ({score_total:.0f}/100). Tus finanzas muestran un equilibrio óptimo entre ingresos, gastos operativos y capacidad de ahorro.",
                "Riesgo": f"Tu salud financiera está en nivel Riesgo ({score_total:.0f}/100). Existen señales de presión en tu presupuesto que requieren ajustes para evitar sobreendeudamiento.",
                "Crítico": f"Tu salud financiera está en nivel Crítico ({score_total:.0f}/100). Tus gastos superan los márgenes de seguridad financiera y se requieren medidas correctivas inmediatas."
            }
            diagnostico_texto = diagnosticos[nivel]

            plantillas_causa = {
                "gasto_ingreso": f"Factor determinante: {causa_datos['nombre']} — estás destinando el {ratio_gastos * 100:.0f}% de tu ingreso a gastos corrientes, limitando tu margen de maniobra.",
                "ahorro": f"Factor determinante: {causa_datos['nombre']} — tu tasa efectiva de ahorro se sitúa en {tasa_ahorro_pct:.0f}%, por debajo del objetivo financiero recomendado.",
                "endeudamiento": f"Factor determinante: {causa_datos['nombre']} — el {tasa_deuda_pct:.0f}% de tus ingresos está comprometido en obligaciones fijas o pasivos."
            }
            causa_texto = plantillas_causa[causa_clave]

            plantillas_accion = {
                ("gasto_ingreso", "Saludable"): "Mantén tu disciplina de presupuesto y destina al menos 5% adicional de tus excedentes a instrumentos de inversión o fondos de liquidez.",
                ("gasto_ingreso", "Riesgo"): "Identifica tus gastos no esenciales (deseos/estilo de vida) y establece un tope de reducción del 10% durante este ciclo.",
                ("gasto_ingreso", "Crítico"): "Aplica un plan de austeridad temporal con recorte del 15% al 20% en partidas prescindibles y lleva registro diario de cada salida de capital.",
                ("ahorro", "Saludable"): "Automatiza el ahorro programado al inicio del período (pagarse a uno mismo primero) antes de ejecutar cualquier gasto corriente.",
                ("ahorro", "Riesgo"): "Establece como meta un fondo de reserva equivalente a 1 mes de gastos antes de realizar compras de discreción.",
                ("ahorro", "Crítico"): "Crea un fondo de contingencia mínimo apartando el 5% de cada entrada de dinero de manera irrevocable.",
                ("endeudamiento", "Saludable"): "Considera amortizaciones anticipadas a capital en créditos de tasa variable para blindar tu patrimonio.",
                ("endeudamiento", "Riesgo"): "Aplica la estrategia de bola de nieve o avalancha para liquidar los pasivos con mayor costo financiero y evita nuevas líneas de crédito.",
                ("endeudamiento", "Crítico"): "Reestructura o consolida tus compromisos financieros a plazos más sostenibles y congela compras diferidas a plazos."
            }
            accion_texto = plantillas_accion.get((causa_clave, nivel), "Mantén el registro estructurado de tus flujos para auditoría continua.")

            # 1.4 Reglas de Alertas 50/30/20 (Tolerancia 5 pp)
            cats_necesidades = {"Vivienda", "Alimentación", "Transporte", "Servicios", "Salud y bienestar", "Educación"}
            cats_deseos = {"Entretenimiento", "Compras", "Cuidado personal", "Regalos", "Viajes", "Otros"}

            gasto_necesidades = sum(v for k, v in resumen_gastos.items() if k in cats_necesidades)
            gasto_deseos = sum(v for k, v in resumen_gastos.items() if k in cats_deseos)

            pct_necesidades = round((gasto_necesidades / ingreso_mensual * 100.0) if ingreso_mensual > 0 else 0.0, 1)
            pct_deseos = round((gasto_deseos / ingreso_mensual * 100.0) if ingreso_mensual > 0 else 0.0, 1)
            pct_ahorro = round((excedente / ingreso_mensual * 100.0) if ingreso_mensual > 0 else 0.0, 1)

            alertas_50_30_20 = []
            if pct_necesidades > 55.0:
                alertas_50_30_20.append({
                    "tipo": "necesidades_excedidas",
                    "mensaje": f"Tus gastos en necesidades básicas representan el {pct_necesidades}% de tu ingreso (referencia recomendada: 50%)."
                })
            if pct_deseos > 35.0:
                alertas_50_30_20.append({
                    "tipo": "deseos_excedidos",
                    "mensaje": f"Tus gastos en estilo de vida y deseos representan el {pct_deseos}% de tu ingreso (referencia recomendada: 30%)."
                })
            if pct_ahorro < 15.0:
                alertas_50_30_20.append({
                    "tipo": "ahorro_insuficiente",
                    "mensaje": f"Tu margen de ahorro actual ({pct_ahorro}%) se encuentra por debajo de la referencia del 20% para estabilidad a mediano plazo."
                })

            # Consejos por categorías detectadas
            consejos_categorias = []
            if "Transporte" in resumen_gastos and resumen_gastos["Transporte"] > (ingreso_mensual * 0.15):
                consejos_categorias.append("El rubro de Transporte supera el 15% de tus ingresos; evalúa optimizar rutas, carpooling o transporte masivo.")
            if "Alimentación" in resumen_gastos and resumen_gastos["Alimentación"] > (ingreso_mensual * 0.25):
                consejos_categorias.append("Tus gastos en Alimentación están por encima del 25%; planifica compras quincenales en supermercado y reduce pedidos a domicilio.")
            if "Entretenimiento" in resumen_gastos and resumen_gastos["Entretenimiento"] > (ingreso_mensual * 0.10):
                consejos_categorias.append("Los consumos en Entretenimiento exceden el 10%; audita suscripciones digitales y establece un presupuesto semanal fijo para ocio.")

            recomendaciones_finales = [
                diagnostico_texto,
                causa_texto,
                accion_texto
            ]
            for al in alertas_50_30_20:
                recomendaciones_finales.append(al['mensaje'])
            for c in consejos_categorias:
                recomendaciones_finales.append(c)

            return json.dumps({
                "status": "success",
                "score_financiero": round(score_total, 0),
                "score": round(score_total, 0),
                "perfil_financiero": nivel,
                "probabilidad": round(probabilidad, 4),
                "ingreso_mensual": ingreso_mensual,
                "total_gastos": round(total_gastos, 2),
                "ahorro_estimado": round(excedente, 2),
                "nivel_endeudamiento": tasa_deuda_pct,
                "frecuencia_ahorro": frecuencia_ahorro,
                "periodicidad": periodicidad,
                "moneda": moneda,
                "explicabilidad_ds08": {
                    "diagnostico": diagnostico_texto,
                    "causa_principal": causa_texto,
                    "accion_recomendada": accion_texto,
                    "pilar_debil": causa_clave,
                    "desglose_pilares": pilares
                },
                "macro_grupos": {
                    "necesidades_basicas": round(gasto_necesidades, 2),
                    "pct_necesidades": pct_necesidades,
                    "estilo_vida": round(gasto_deseos, 2),
                    "pct_estilo_vida": pct_deseos,
                    "ahorro_deuda": round(excedente, 2),
                    "pct_ahorro": pct_ahorro
                },
                "resumen_gastos": {k: round(v, 2) for k, v in resumen_gastos.items()},
                "transacciones_categorizadas": transacciones_categorizadas,
                "categorias_detectadas": list(resumen_gastos.keys()),
                "recomendaciones": recomendaciones_finales,
                "consejos_financieros": recomendaciones_finales
            }, ensure_ascii=False)

    except Exception as e:
        return json.dumps({"error": str(e)}, ensure_ascii=False)


# ─────────────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    if len(sys.argv) > 1:
        input_data = " ".join(sys.argv[1:])
    else:
        try:
            input_data = sys.stdin.read()
        except Exception:
            input_data = "{}"
    result = predict(input_data)
    sys.stdout.buffer.write(result.encode("utf-8") + b"\n")
    sys.stdout.buffer.flush()

