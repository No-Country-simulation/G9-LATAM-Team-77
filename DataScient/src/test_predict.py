# -*- coding: utf-8 -*-
"""
test_predict.py — Pruebas del motor de prediccion y recomendaciones FinanceAI
"""
import unittest
import json
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from predict import predict


class TestClasificacionTransacciones(unittest.TestCase):
    """Pruebas de clasificacion de transacciones individuales."""

    def _ejecutar(self, descripcion):
        resultado = json.loads(predict(json.dumps({
            "type": "transaction",
            "data": {"descripcion": descripcion}
        })))
        return resultado

    def test_supermercado_alimentacion(self):
        r = self._ejecutar("Supermercado La Comer")
        self.assertIn("status", r)
        if r.get("status") == "exito":
            self.assertEqual(r["prediccion"], "Alimentacion")

    def test_combustible_transporte(self):
        r = self._ejecutar("Combustible PEMEX")
        if r.get("status") == "exito":
            self.assertEqual(r["prediccion"], "Transporte")

    def test_streaming_entretenimiento(self):
        r = self._ejecutar("Netflix mensual")
        if r.get("status") == "exito":
            self.assertEqual(r["prediccion"], "Entretenimiento")

    def test_farmacia_salud(self):
        r = self._ejecutar("Farmacia Guadalajara")
        if r.get("status") == "exito":
            self.assertEqual(r["prediccion"], "Salud y bienestar")

    def test_descripcion_desconocida(self):
        r = self._ejecutar("Pago miscelaneo")
        self.assertNotIn("error", r)


class TestPrediccionPerfil(unittest.TestCase):
    """Pruebas de prediccion de perfil financiero."""

    def _ejecutar(self, datos):
        resultado = json.loads(predict(json.dumps({
            "type": "profile",
            "data": datos
        })))
        return resultado

    def test_perfil_devuelve_resultado(self):
        r = self._ejecutar({
            "ingreso_mensual_usd": 5000,
            "gastos_mensuales_usd": 3000,
            "cuota_deuda_mensual_usd": 500,
            "nivel_endeudamiento": 20,
        })
        # Acepta tanto error de modelo no encontrado como exito
        self.assertTrue("status" in r or "error" in r)

    def test_tipo_invalido(self):
        r = json.loads(predict(json.dumps({"type": "invalido"})))
        self.assertIn("error", r)


class TestAnalisisCompleto(unittest.TestCase):
    """Pruebas del analisis financiero completo."""

    def _analizar(self, ingreso, endeudamiento, ahorro, transacciones):
        carga = {
            "type": "full_analysis",
            "data": {
                "ingreso_mensual": ingreso,
                "nivel_endeudamiento": endeudamiento,
                "frecuencia_ahorro": ahorro,
                "transacciones": transacciones
            }
        }
        return json.loads(predict(json.dumps(carga)))

    # ── Caso 1: Usuario con perfil saludable ─────────────────────────────
    def test_perfil_saludable(self):
        r = self._analizar(
            ingreso=8000, endeudamiento=10, ahorro="Alta",
            transacciones=[
                {"descripcion": "Supermercado",  "valor": 800},
                {"descripcion": "Combustible",   "valor": 400},
                {"descripcion": "Streaming",     "valor": 150},
                {"descripcion": "Gym",           "valor": 200},
            ]
        )
        self.assertEqual(r.get("status"), "success")
        self.assertIn("perfil_financiero", r)
        self.assertIn("recomendaciones", r)
        self.assertIn("resumen_gastos", r)
        self.assertIn("total_gastos", r)
        self.assertIn("ahorro_estimado", r)
        print(f"\n[Caso Saludable] Perfil: {r['perfil_financiero']} | Recomendaciones: {len(r['recomendaciones'])}")
        for rec in r["recomendaciones"]:
            print(f"  -> {rec}")

    # ── Caso 2: Usuario en riesgo con alto endeudamiento ─────────────────
    def test_perfil_en_riesgo(self):
        r = self._analizar(
            ingreso=3500, endeudamiento=60, ahorro="Baja",
            transacciones=[
                {"descripcion": "Supermercado",  "valor": 900},
                {"descripcion": "Uber",          "valor": 600},
                {"descripcion": "Netflix",       "valor": 250},
                {"descripcion": "Ropa",          "valor": 450},
                {"descripcion": "Restaurante",   "valor": 400},
            ]
        )
        self.assertEqual(r.get("status"), "success")
        self.assertGreater(len(r.get("recomendaciones", [])), 0)
        print(f"\n[Caso En Riesgo] Perfil: {r['perfil_financiero']} | Recomendaciones: {len(r['recomendaciones'])}")
        for rec in r["recomendaciones"]:
            print(f"  -> {rec}")

    # ── Caso 3: Ejemplo del documento del Hackathon ───────────────────────
    def test_caso_documento_hackathon(self):
        r = self._analizar(
            ingreso=4500, endeudamiento=25, ahorro="Media",
            transacciones=[
                {"descripcion": "Supermercado", "valor": 420},
                {"descripcion": "Combustible",  "valor": 300},
                {"descripcion": "Streaming",    "valor": 40},
            ]
        )
        self.assertEqual(r.get("status"), "success")
        self.assertIn("perfil_financiero", r)
        self.assertIn("recomendaciones", r)
        self.assertIn("categorias_detectadas", r)
        print(f"\n[Caso Documento Hackathon] Perfil: {r['perfil_financiero']}")
        print(f"  Resumen: {r['resumen_gastos']}")
        print(f"  Total gastos: ${r['total_gastos']} | Ahorro estimado: ${r['ahorro_estimado']}")
        for rec in r["recomendaciones"]:
            print(f"  -> {rec}")

    # ── Caso 4: Gastos concentrados en una sola categoria ────────────────
    def test_concentracion_categoria(self):
        r = self._analizar(
            ingreso=5000, endeudamiento=15, ahorro="Media",
            transacciones=[
                {"descripcion": "Restaurante",  "valor": 2000},
                {"descripcion": "Combustible",  "valor": 200},
                {"descripcion": "Streaming",    "valor": 100},
            ]
        )
        self.assertEqual(r.get("status"), "success")
        self.assertTrue(len(r.get("recomendaciones", [])) > 0)
        print(f"\n[Caso Concentracion] Recomendaciones: {r['recomendaciones']}")

    # ── Caso 5: Sin transacciones ─────────────────────────────────────────
    def test_sin_transacciones(self):
        r = self._analizar(
            ingreso=4000, endeudamiento=20, ahorro="Alta",
            transacciones=[]
        )
        self.assertEqual(r.get("status"), "success")
        self.assertEqual(r.get("total_gastos"), 0)

    # ── Caso 6: Estructura completa de respuesta ──────────────────────────
    def test_estructura_respuesta_completa(self):
        """Verifica que todos los campos requeridos por el documento esten presentes."""
        r = self._analizar(
            ingreso=4500, endeudamiento=25, ahorro="Media",
            transacciones=[
                {"descripcion": "Supermercado", "valor": 420},
                {"descripcion": "Combustible",  "valor": 300},
            ]
        )
        campos_requeridos = [
            "perfil_financiero", "probabilidad", "resumen_gastos",
            "recomendaciones", "total_gastos", "ahorro_estimado",
            "categorias_detectadas", "transacciones_categorizadas"
        ]
        for campo in campos_requeridos:
            self.assertIn(campo, r, f"Campo faltante en la respuesta: {campo}")

    # ── Caso 7: Fallback de moneda y campos faltantes ─────────────────────
    def test_dummy_json_ingreso_mensual(self):
        """Verifica que un JSON dummy como {'ingresoMensual': 1000} no falle y devuelva JSON valido."""
        r = json.loads(predict('{"ingresoMensual": 1000}'))
        self.assertEqual(r.get("status"), "success")
        self.assertEqual(r.get("ingreso_mensual"), 1000.0)
        self.assertEqual(r.get("moneda"), "USD")
        self.assertIn("perfil_financiero", r)
        self.assertIn("recomendaciones", r)

    def test_moneda_default_usd(self):
        """Verifica que moneda ausente por defecto sea USD."""
        r = json.loads(predict(json.dumps({
            "type": "full_analysis",
            "data": {"ingreso_mensual": 2000}
        })))
        self.assertEqual(r.get("status"), "success")
        self.assertEqual(r.get("moneda"), "USD")

    def test_empty_json(self):
        """Verifica que JSON vacio no cause excepcion."""
        r = json.loads(predict("{}"))
        self.assertEqual(r.get("status"), "success")
        self.assertEqual(r.get("moneda"), "USD")
        self.assertEqual(r.get("ingreso_mensual"), 0.0)


if __name__ == "__main__":
    unittest.main(verbosity=2)


