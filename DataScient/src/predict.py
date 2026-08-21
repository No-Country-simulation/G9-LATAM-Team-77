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
import joblib
import pandas as pd

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
            if nivel_endeudamiento == 0.0 and deuda_total > 0 and ingreso_mensual > 0:
                nivel_endeudamiento = min(100.0, (deuda_total / ingreso_mensual) * 100.0)

            frecuencia_ahorro = _safe_str(
                data.get("frecuencia_ahorro") or data.get("frecuenciaAhorro"),
                "Media"
            )
            if not frecuencia_ahorro:
                frecuencia_ahorro = "Media"

            transacciones_raw = data.get("transacciones") or data.get("transactions") or []
            if not isinstance(transacciones_raw, list):
                transacciones_raw = []

            # Cargar modelos (no críticos si fallan)
            tx_pipeline = None
            tx_model_path = os.path.join(base_dir, "models", "transaction_model.pkl")
            if os.path.exists(tx_model_path):
                try:
                    tx_pipeline = joblib.load(tx_model_path)
                except Exception:
                    pass

            prof_clf = None
            prof_features = []
            prof_model_path = os.path.join(base_dir, "models", "profile_model.pkl")
            if os.path.exists(prof_model_path):
                try:
                    prof_clf, prof_features = joblib.load(prof_model_path)
                except Exception:
                    pass

            # Clasificar cada transacción
            transacciones_categorizadas = []
            resumen_gastos: dict[str, float] = {}

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

            # Predecir perfil financiero
            perfil_financiero = "En observación"
            probabilidad = 0.75

            if prof_clf is not None and prof_features:
                try:
                    gastos_mensuales = total_gastos if total_gastos > 0 else gasto_mensual_directo
                    ratio_gastos = gastos_mensuales / ingreso_mensual if ingreso_mensual > 0 else 1.0
                    capacidad_ahorro = max(0.0, ingreso_mensual - gastos_mensuales)
                    tasa_ahorro = capacidad_ahorro / ingreso_mensual if ingreso_mensual > 0 else 0.0

                    # Gastos esenciales vs discrecionales
                    cats_esenciales = {"Alimentación", "Vivienda", "Salud y bienestar", "Servicios", "Educación", "Transporte"}
                    gasto_esencial = sum(v for k, v in resumen_gastos.items() if k in cats_esenciales)
                    gasto_discrecional = max(0.0, total_gastos - gasto_esencial)
                    pct_esencial = (gasto_esencial / total_gastos) if total_gastos > 0 else 0.5
                    pct_discrecional = (gasto_discrecional / total_gastos) if total_gastos > 0 else 0.5

                    max_cat_gasto = max(resumen_gastos.values()) if resumen_gastos else 0.0
                    concentracion_cat = (max_cat_gasto / total_gastos) if total_gastos > 0 else 0.0

                    feat_vals = {
                        "ingreso_mensual_usd": ingreso_mensual,
                        "gastos_mensuales_usd": gastos_mensuales,
                        "gasto_total_usd": gastos_mensuales,
                        "cuota_deuda_mensual_usd": (ingreso_mensual * nivel_endeudamiento / 100.0) if deuda_total == 0 else deuda_total,
                        "nivel_endeudamiento": nivel_endeudamiento,
                        "ahorro_total_acumulado_usd": (capacidad_ahorro * 12.0) if ahorro_mensual == 0 else (ahorro_mensual * 12.0),
                        "ratio_ahorro_ingreso_anual": tasa_ahorro,
                        "capacidad_ahorro_mensual_estimada_usd": capacidad_ahorro,
                        "tasa_capacidad_ahorro": tasa_ahorro,
                        "ratio_gastos_ingresos": ratio_gastos,
                        "ratio_compromisos_ingresos": ratio_gastos + (nivel_endeudamiento / 100.0),
                        "porcentaje_gasto_esencial": pct_esencial,
                        "porcentaje_gasto_discrecional": pct_discrecional,
                        "concentracion_categoria_principal": concentracion_cat,
                        "cantidad_categorias_utilizadas": len(resumen_gastos),
                    }
                    df_row = pd.DataFrame([{f: feat_vals.get(f, 0.0) for f in prof_features}])
                    perfil_financiero = prof_clf.predict(df_row)[0]
                    if hasattr(prof_clf, "predict_proba"):
                        proba_arr = prof_clf.predict_proba(df_row)[0]
                        probabilidad = float(max(proba_arr))
                except Exception:
                    pass
            else:
                # Lógica de reglas si no hay modelo
                ratio_compromisos = (total_gastos / ingreso_mensual if ingreso_mensual > 0 else 1.0) + (nivel_endeudamiento / 100.0)
                if ratio_compromisos > 1.0 or nivel_endeudamiento > 50:
                    perfil_financiero = "En riesgo"
                    probabilidad = 0.80
                elif ratio_compromisos > 0.80 or nivel_endeudamiento > 35:
                    perfil_financiero = "En observación"
                    probabilidad = 0.72
                else:
                    perfil_financiero = "Saludable"
                    probabilidad = 0.85

            # Generar recomendaciones específicas
            recomendaciones = _generar_recomendaciones(
                transacciones_categorizadas=transacciones_categorizadas,
                ingreso_mensual=ingreso_mensual,
                nivel_endeudamiento=nivel_endeudamiento,
                frecuencia_ahorro=frecuencia_ahorro,
                perfil_financiero=perfil_financiero,
            )

            ahorro_estimado = max(0.0, ingreso_mensual - total_gastos)

            return json.dumps({
                "status": "success",
                "perfil_financiero": perfil_financiero,
                "probabilidad": round(probabilidad, 4),
                "ingreso_mensual": ingreso_mensual,
                "total_gastos": round(total_gastos, 2),
                "ahorro_estimado": round(ahorro_estimado, 2),
                "nivel_endeudamiento": nivel_endeudamiento,
                "frecuencia_ahorro": frecuencia_ahorro,
                "moneda": moneda,
                "resumen_gastos": {k: round(v, 2) for k, v in resumen_gastos.items()},
                "transacciones_categorizadas": transacciones_categorizadas,
                "categorias_detectadas": list(resumen_gastos.keys()),
                "recomendaciones": recomendaciones,
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

