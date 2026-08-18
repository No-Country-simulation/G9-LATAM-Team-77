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
        "Excelente manejo financiero. Considera incrementar tu fondo de inversión o portafolio diversificado.",
        "Revisa periódicamente tu presupuesto para mantener el equilibrio entre gastos, ahorro e inversión.",
    ],
    "En observación": [
        "Tu situación financiera es estable pero requiere atención. Define metas de ahorro concretas para el próximo trimestre.",
        "Establece un fondo de emergencia equivalente a 3 meses de gastos fijos antes de considerar nuevas inversiones.",
    ],
    "En riesgo": [
        "Tu perfil financiero presenta riesgo. Prioriza reducir el nivel de endeudamiento antes de cualquier gasto discrecional.",
        "Crea un presupuesto mensual detallado y elimina gastos no esenciales hasta que tu ratio de compromisos baje del 80%.",
        "Considera asesoría financiera profesional para reestructurar tus deudas actuales.",
    ],
}

# ─────────────────────────────────────────────────────────────────────────────
# RECOMENDACIONES POR ENDEUDAMIENTO
# ─────────────────────────────────────────────────────────────────────────────
def _recs_endeudamiento(nivel_endeudamiento: float) -> list[str]:
    recs = []
    if nivel_endeudamiento > 50:
        recs.append(
            f"Tu nivel de endeudamiento es crítico ({nivel_endeudamiento:.0f}%). "
            "Prioriza liquidar deudas con mayor tasa de interés usando el método 'avalancha': "
            "paga el mínimo en todo y destina el excedente a la deuda más cara."
        )
    elif nivel_endeudamiento > 35:
        recs.append(
            f"Tu endeudamiento del {nivel_endeudamiento:.0f}% está en zona de observación. "
            "Evita nuevos créditos y destina al menos un 10% adicional de tu ingreso a reducir deudas."
        )
    elif nivel_endeudamiento > 20:
        recs.append(
            f"Tu endeudamiento ({nivel_endeudamiento:.0f}%) es manejable. "
            "Mantén un registro mensual y evita que suba por encima del 30%."
        )
    return recs

# ─────────────────────────────────────────────────────────────────────────────
# UTILIDADES
# ─────────────────────────────────────────────────────────────────────────────
def _clasificar_descripcion(descripcion: str, pipeline) -> str:
    """Clasifica con el modelo ML; fallback al mapa manual."""
    desc_lower = descripcion.lower().strip()
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
    Genera recomendaciones específicas basadas en:
    1. Gastos por categoría vs. umbral recomendado
    2. Nivel de endeudamiento
    3. Perfil financiero global
    4. Frecuencia de ahorro
    """
    recomendaciones = []

    # --- 1. Por categoría ---
    gastos_por_categoria: dict[str, float] = {}
    for t in transacciones_categorizadas:
        cat = t.get("categoria", "Otros")
        gastos_por_categoria[cat] = gastos_por_categoria.get(cat, 0) + t.get("valor", 0)

    total_gastos = sum(gastos_por_categoria.values())

    for categoria, total in gastos_por_categoria.items():
        umbral = UMBRALES_CATEGORIA.get(categoria)
        if umbral is None or ingreso_mensual <= 0:
            continue
        pct = total / ingreso_mensual
        recs_cat = RECOMENDACIONES_CATEGORIA.get(categoria, {})
        if pct > umbral:
            msgs = recs_cat.get("alta", [])
        else:
            msgs = recs_cat.get("normal", [])

        for msg in msgs[:1]:  # máximo 1 por categoría
            recomendaciones.append(msg.format(
                pct=pct * 100,
                total=total,
                umbral=umbral * 100,
                ingreso=ingreso_mensual,
            ))

    # --- 2. Categoría dominante (>50% del total de gastos) ---
    if total_gastos > 0 and gastos_por_categoria:
        cat_mayor = max(gastos_por_categoria, key=gastos_por_categoria.get)
        pct_mayor = gastos_por_categoria[cat_mayor] / total_gastos
        if pct_mayor > 0.50:
            recomendaciones.append(
                f"'{cat_mayor}' concentra el {pct_mayor*100:.1f}% de tus gastos totales. "
                "Revisa si esta concentración es intencional y si existen alternativas más económicas."
            )

    # --- 3. Por endeudamiento ---
    recomendaciones.extend(_recs_endeudamiento(nivel_endeudamiento))

    # --- 4. Por perfil financiero ---
    perfil_recs = RECOMENDACIONES_PERFIL.get(perfil_financiero, [])
    recomendaciones.extend(perfil_recs[:1])

    # --- 5. Por frecuencia de ahorro ---
    if frecuencia_ahorro == "Baja":
        recomendaciones.append(
            "Tu frecuencia de ahorro es baja. Automatiza una transferencia al inicio de cada mes "
            "a una cuenta de ahorro separada, aunque sea el 5% de tu ingreso."
        )
    elif frecuencia_ahorro == "Media":
        recomendaciones.append(
            "Tienes una frecuencia de ahorro media. Intenta incrementarla gradualmente: "
            "aumenta un 2% cada trimestre hasta llegar al 20% de tu ingreso."
        )

    # --- 6. Ratio gastos/ingreso ---
    if ingreso_mensual > 0:
        ratio = total_gastos / ingreso_mensual
        if ratio > 0.90:
            recomendaciones.append(
                f"Tus gastos representan el {ratio*100:.1f}% de tu ingreso mensual. "
                "Queda muy poco margen para ahorro. Identifica al menos 2 gastos que puedas reducir este mes."
            )
        elif ratio > 0.75:
            recomendaciones.append(
                f"Tus gastos son el {ratio*100:.1f}% de tu ingreso. "
                "Apunta a no superar el 70% para dejar espacio suficiente al ahorro e imprevistos."
            )

    # Limitar a 6 recomendaciones para no abrumar
    return recomendaciones[:6]


# ─────────────────────────────────────────────────────────────────────────────
# FUNCIÓN PRINCIPAL
# ─────────────────────────────────────────────────────────────────────────────
def predict(input_json: str) -> str:
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

    try:
        req = json.loads(input_json)
        req_type = req.get("type")
        data = req.get("data", {})

        # ── 1. Clasificación de transacción individual ──────────────────────
        if req_type == "transaction":
            model_path = os.path.join(base_dir, "models", "transaction_model.pkl")
            pipeline = joblib.load(model_path) if os.path.exists(model_path) else None
            desc = data.get("descripcion", "")
            categoria = _clasificar_descripcion(desc, pipeline)
            return json.dumps({"status": "success", "prediction": categoria})

        # ── 2. Perfil financiero (modelo ML) ───────────────────────────────
        elif req_type == "profile":
            model_path = os.path.join(base_dir, "models", "profile_model.pkl")
            if not os.path.exists(model_path):
                return json.dumps({"error": "Modelo de perfil no encontrado"})
            clf, features = joblib.load(model_path)
            df_data = {feat: [data.get(feat, 0.0)] for feat in features}
            df = pd.DataFrame(df_data)
            prediction = clf.predict(df)[0]
            return json.dumps({"status": "success", "prediction": prediction})

        # ── 3. Análisis completo ────────────────────────────────────────────
        elif req_type == "full_analysis":
            ingreso_mensual      = float(data.get("ingreso_mensual", 0))
            nivel_endeudamiento  = float(data.get("nivel_endeudamiento", 0))
            frecuencia_ahorro    = str(data.get("frecuencia_ahorro", "Media"))
            transacciones_raw    = data.get("transacciones", [])

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
                desc  = str(t.get("descripcion", ""))
                valor = float(t.get("valor", 0))
                cat   = _clasificar_descripcion(desc, tx_pipeline)
                transacciones_categorizadas.append({
                    "descripcion": desc,
                    "valor": valor,
                    "categoria": cat,
                })
                resumen_gastos[cat] = resumen_gastos.get(cat, 0) + valor

            total_gastos = sum(resumen_gastos.values())

            # Predecir perfil financiero
            perfil_financiero = "En observación"
            probabilidad = 0.70

            if prof_clf is not None and prof_features:
                try:
                    gastos_mensuales = total_gastos
                    ratio_gastos = gastos_mensuales / ingreso_mensual if ingreso_mensual > 0 else 1.0
                    capacidad_ahorro = max(0, ingreso_mensual - gastos_mensuales)
                    tasa_ahorro = capacidad_ahorro / ingreso_mensual if ingreso_mensual > 0 else 0

                    # Gastos esenciales vs discrecionales
                    cats_esenciales = {"Alimentación", "Vivienda", "Salud y bienestar", "Servicios", "Educación", "Transporte"}
                    gasto_esencial = sum(v for k, v in resumen_gastos.items() if k in cats_esenciales)
                    gasto_discrecional = total_gastos - gasto_esencial
                    pct_esencial = gasto_esencial / total_gastos if total_gastos > 0 else 0.5
                    pct_discrecional = gasto_discrecional / total_gastos if total_gastos > 0 else 0.5

                    feat_vals = {
                        "ingreso_mensual_usd": ingreso_mensual,
                        "gastos_mensuales_usd": gastos_mensuales,
                        "cuota_deuda_mensual_usd": ingreso_mensual * nivel_endeudamiento / 100,
                        "ahorro_total_acumulado_usd": capacidad_ahorro * 12,
                        "ratio_ahorro_ingreso_anual": tasa_ahorro,
                        "capacidad_ahorro_mensual_estimada_usd": capacidad_ahorro,
                        "tasa_capacidad_ahorro": tasa_ahorro,
                        "ratio_gastos_ingresos": ratio_gastos,
                        "ratio_compromisos_ingresos": ratio_gastos + (nivel_endeudamiento / 100),
                        "porcentaje_gasto_esencial": pct_esencial,
                        "porcentaje_gasto_discrecional": pct_discrecional,
                        "cantidad_categorias_utilizadas": len(resumen_gastos),
                    }
                    df_row = pd.DataFrame([{f: feat_vals.get(f, 0.0) for f in prof_features}])
                    perfil_financiero = prof_clf.predict(df_row)[0]
                    proba_arr = prof_clf.predict_proba(df_row)[0]
                    probabilidad = float(max(proba_arr))
                except Exception:
                    pass
            else:
                # Lógica de reglas si no hay modelo
                ratio_compromisos = (total_gastos / ingreso_mensual if ingreso_mensual > 0 else 1.0) + (nivel_endeudamiento / 100)
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

            ahorro_estimado = max(0, ingreso_mensual - total_gastos)

            return json.dumps({
                "status": "success",
                "perfil_financiero": perfil_financiero,
                "probabilidad": round(probabilidad, 4),
                "ingreso_mensual": ingreso_mensual,
                "total_gastos": round(total_gastos, 2),
                "ahorro_estimado": round(ahorro_estimado, 2),
                "nivel_endeudamiento": nivel_endeudamiento,
                "frecuencia_ahorro": frecuencia_ahorro,
                "resumen_gastos": {k: round(v, 2) for k, v in resumen_gastos.items()},
                "transacciones_categorizadas": transacciones_categorizadas,
                "categorias_detectadas": list(resumen_gastos.keys()),
                "recomendaciones": recomendaciones,
            }, ensure_ascii=False)

        else:
            return json.dumps({"error": "Tipo inválido. Usa 'transaction', 'profile' o 'full_analysis'"})

    except Exception as e:
        return json.dumps({"error": str(e)})


# ─────────────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    if len(sys.argv) > 1:
        input_data = sys.argv[1]
    else:
        input_data = sys.stdin.read()
    result = predict(input_data)
    sys.stdout.buffer.write(result.encode("utf-8") + b"\n")
    sys.stdout.buffer.flush()
