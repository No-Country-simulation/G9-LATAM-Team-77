"""
Module providing a CLI wrapper for the trained classification model.
Accepts JSON input via stdin or direct arguments and returns predictions in camelCase JSON format,
designed to integrate seamlessly with the Java Backend.
"""
import sys
import json
import joblib
import traceback
import pandas as pd

def predict():
    """
    Reads JSON payload from standard input or sys arguments, loads the joblib model,
    executes predictions using both descriptions and monetary values, and prints the result as JSON.
    """
    try:
        # Load the model
        model = joblib.load('modelo_clasificacion_transacciones_team77.joblib')
        
        input_data = sys.stdin.read().strip()
        if not input_data:
            print(json.dumps({"errorMessage": "No input provided."}))
            return
            
        data = json.loads(input_data)
        transactions = data.get("transactions", [])
        
        # Support fallback logic
        if not transactions:
            descriptions = data.get("transactionDescriptions", [])
            transactions = [{"descripcion": desc, "valor_usd": 0.0} for desc in descriptions]
        
        df = pd.DataFrame(transactions)
        
        if 'descripcion' not in df.columns or 'valor_usd' not in df.columns:
            print(json.dumps({"errorMessage": "Missing descripcion or valor_usd in transactions."}))
            return
            
        # Predict
        base_predictions = model.predict(df)
        
        # Output as JSON with camelCase for Java Backend
        output = {
            "predictedCategories": base_predictions.tolist()
        }
        print(json.dumps(output))
        
    except Exception as e:
        print(json.dumps({"errorMessage": str(e), "tracebackDetails": traceback.format_exc()}))

if __name__ == '__main__':
    predict()
