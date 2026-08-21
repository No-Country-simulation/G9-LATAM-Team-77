import os

files_to_remove = ['predict.py', 'retrain_model.py', 'test_model.py', 'modelo_clasificacion_transacciones_team77.joblib']
for f in files_to_remove:
    try:
        os.remove(f)
        print(f"Removed {f}")
    except OSError as e:
        print(f"Error removing {f}: {e}")
