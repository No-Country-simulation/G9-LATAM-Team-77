import sys
import json
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "src")))
from predict import predict

tests = [
    ("T01_Automatico", {"type": "full_analysis", "ingreso_mensual": 4500, "transacciones": [{"descripcion": "Supermercado", "valor": 420}, {"descripcion": "Combustible", "valor": 300}, {"descripcion": "Streaming", "valor": 40}]}),
    ("T02_Alta", {"type": "full_analysis", "ingreso_mensual": 3000, "frecuencia_ahorro": "Alta", "transacciones": [{"descripcion": "Comida", "valor": 500}]}),
    ("T03_Media", {"type": "full_analysis", "ingreso_mensual": 3000, "frecuencia_ahorro": "Media", "transacciones": [{"descripcion": "Comida", "valor": 500}]}),
    ("T04_Baja", {"type": "full_analysis", "ingreso_mensual": 3000, "frecuencia_ahorro": "Baja", "transacciones": [{"descripcion": "Comida", "valor": 500}]}),
    ("T05_Semanal", {"type": "full_analysis", "ingreso_mensual": 2000, "frecuencia_ahorro": "Semanal", "transacciones": [{"descripcion": "Super", "valor": 200}]}),
    ("T06_Quincenal", {"type": "full_analysis", "ingreso_mensual": 2000, "frecuencia_ahorro": "Quincenal", "transacciones": [{"descripcion": "Super", "valor": 200}]}),
    ("T07_Mensual", {"type": "full_analysis", "ingreso_mensual": 2000, "frecuencia_ahorro": "Mensual", "transacciones": [{"descripcion": "Super", "valor": 200}]}),
    ("T08_Nunca", {"type": "full_analysis", "ingreso_mensual": 2000, "frecuencia_ahorro": "Nunca", "transacciones": [{"descripcion": "Super", "valor": 200}]}),
    ("T09_Minuscula", {"type": "full_analysis", "ingreso_mensual": 2500, "frecuencia_ahorro": "alta", "transacciones": [{"descripcion": "Super", "valor": 200}]}),
    ("T10_Desconocida", {"type": "full_analysis", "ingreso_mensual": 4000, "frecuencia_ahorro": "indefinido", "transacciones": [{"descripcion": "Super", "valor": 200}]}),
    ("T11_Deuda_0", {"type": "full_analysis", "ingreso_mensual": 5000, "nivel_endeudamiento": 0, "transacciones": [{"descripcion": "Super", "valor": 300}]}),
    ("T12_Deuda_25", {"type": "full_analysis", "ingreso_mensual": 5000, "nivel_endeudamiento": 25, "transacciones": [{"descripcion": "Super", "valor": 300}]}),
    ("T13_Deuda_100", {"type": "full_analysis", "ingreso_mensual": 5000, "nivel_endeudamiento": 100, "transacciones": [{"descripcion": "Super", "valor": 300}]}),
    ("T14_Deuda_Desbordada_150", {"type": "full_analysis", "ingreso_mensual": 3000, "nivel_endeudamiento": 150, "transacciones": [{"descripcion": "Super", "valor": 300}]}),
    ("T15_Deuda_Desbordada_500", {"type": "full_analysis", "ingreso_mensual": 2000, "nivel_endeudamiento": 500, "transacciones": [{"descripcion": "Super", "valor": 300}]}),
    ("T16_Deuda_Negativa_Menos25", {"type": "full_analysis", "ingreso_mensual": 3000, "nivel_endeudamiento": -25, "transacciones": [{"descripcion": "Super", "valor": 300}]}),
    ("T17_Deuda_Negativa_Menos100", {"type": "full_analysis", "ingreso_mensual": 3000, "nivel_endeudamiento": -100, "transacciones": [{"descripcion": "Super", "valor": 300}]}),
    ("T18_Transacciones_Vacias", {"type": "full_analysis", "ingreso_mensual": 3000, "transacciones": []}),
    ("T19_Muchos_Gastos_Critico", {"type": "full_analysis", "ingreso_mensual": 1000, "transacciones": [{"descripcion": "Renta", "valor": 800}, {"descripcion": "Supermercado", "valor": 400}, {"descripcion": "Uber", "valor": 200}]}),
    ("T20_Frecuencia_Ocasional", {"type": "full_analysis", "ingreso_mensual": 3500, "frecuencia_ahorro": "ocasional", "transacciones": [{"descripcion": "Farmacia", "valor": 150}]})
]

print("=== EJECUCIÓN DE 20 CASOS DE PRUEBA ===")
for name, payload in tests:
    res = json.loads(predict(json.dumps(payload)))
    status = res.get("status", "error")
    score = res.get("score_financiero", 0)
    perfil = res.get("perfil_financiero", "N/A")
    deuda = res.get("nivel_endeudamiento", "N/A")
    frec = res.get("frecuencia_ahorro", "N/A")
    recs = len(res.get("recomendaciones", []))
    print(f"[{name:28}] -> Status: {status} | Score: {score:3.0f} | Perfil: {perfil:12} | Deuda: {deuda}% | Frecuencia: {frec:7} | Recs: {recs}")
