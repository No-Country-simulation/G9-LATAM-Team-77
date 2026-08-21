import requests
import random
import json

URL = "http://localhost:8080/api/v1/analisis-financiero"

categorias = ["Supermercado", "Combustible", "Streaming", "Educacion", "Salud", "Ocio", "Desconocido"]
monedas = ["USD", "EUR", "ARS", "MXN", "COP", "CLP"]

print("Iniciando bateria de 50 pruebas masivas...")

errores = 0
exitos = 0

for i in range(1, 51):
    # Generar datos aleatorios
    ingreso = random.uniform(500.0, 10000.0) if random.random() > 0.1 else 0.0
    deuda = random.uniform(0.0, ingreso * 1.5)
    
    num_transacciones = random.randint(0, 10)
    transacciones = []
    for _ in range(num_transacciones):
        transacciones.append({
            "descripcion": random.choice(categorias) + " " + str(random.randint(1, 100)),
            "valor": random.uniform(5.0, 500.0)
        })
    
    if random.random() < 0.05:
        payload = {
            "nivelEndeudamiento": deuda,
            "frecuenciaAhorro": random.choice(["Alta", "Media", "Baja", "Nula"]),
            "transacciones": transacciones,
            "moneda": random.choice(monedas)
        }
    else:
        payload = {
            "ingresoMensual": ingreso,
            "nivelEndeudamiento": deuda,
            "frecuenciaAhorro": random.choice(["Alta", "Media", "Baja", "Nula"]),
            "transacciones": transacciones,
            "moneda": random.choice(monedas)
        }

    try:
        print(f"Prueba {i}/50...", end=" ")
        response = requests.post(URL, json=payload, timeout=10)
        
        if response.status_code in [200, 201]:
            print(f"EXITO (Status: {response.status_code})")
            exitos += 1
        elif response.status_code in [400, 422]:
            print(f"VALIDACION CORRECTA (Status: {response.status_code})")
            exitos += 1
        else:
            print(f"ERROR (Status: {response.status_code}) - {response.text[:100]}")
            errores += 1
    except Exception as e:
        print(f"FALLO DE CONEXION: {e}")
        errores += 1

print("\n=== RESUMEN DE PRUEBAS ===")
print(f"Exitosos (incluye errores de validación esperados): {exitos}")
print(f"Errores (Crash, 500 o fallos de conexión): {errores}")
print("==========================")
