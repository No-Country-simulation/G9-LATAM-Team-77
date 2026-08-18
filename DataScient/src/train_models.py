import pandas as pd
import os
import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report

def train_transaction_model(data_path, model_path):
    print("Entrenando modelo de clasificacion de transacciones...")
    df = pd.read_csv(data_path)
    
    # Eliminar filas sin descripcion o categoria
    df = df.dropna(subset=['descripcion', 'categoria'])
    
    X = df['descripcion']
    y = df['categoria']
    
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    pipeline = Pipeline([
        ('tfidf', TfidfVectorizer(max_features=5000)),
        ('clf', LogisticRegression(max_iter=1000, random_state=42))
    ])
    
    pipeline.fit(X_train, y_train)
    
    print("Precision del modelo de transacciones en conjunto de prueba:")
    print(pipeline.score(X_test, y_test))
    
    os.makedirs(os.path.dirname(model_path), exist_ok=True)
    joblib.dump(pipeline, model_path)
    print(f"Modelo de transacciones guardado en: {model_path}")

def train_profile_model(data_path, model_path):
    print("Entrenando modelo de perfil financiero...")
    df = pd.read_csv(data_path)
    
    # Seleccionar variables numericas relevantes para el perfil financiero
    features = [
        'ingreso_mensual_usd', 'nivel_endeudamiento', 
        'gasto_total_usd', 'ratio_gastos_ingresos', 
        'porcentaje_gasto_esencial', 'porcentaje_gasto_discrecional',
        'concentracion_categoria_principal', 'cantidad_categorias_utilizadas'
    ]
    
    df = df.dropna(subset=features + ['perfil_financiero'])
    
    X = df[features]
    y = df['perfil_financiero']
    
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    clf = RandomForestClassifier(n_estimators=100, random_state=42)
    clf.fit(X_train, y_train)
    
    print("Precision del modelo de perfil financiero en conjunto de prueba:")
    print(clf.score(X_test, y_test))
    
    os.makedirs(os.path.dirname(model_path), exist_ok=True)
    joblib.dump((clf, features), model_path)  # Guardar tambien la lista de variables
    print(f"Modelo de perfil guardado en: {model_path}")

if __name__ == "__main__":
    base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    trans_data = os.path.join(base_dir, 'data', 'processed', 'dataset_clasificacion_transacciones.csv')
    trans_model = os.path.join(base_dir, 'models', 'transaction_model.pkl')
    train_transaction_model(trans_data, trans_model)
    
    prof_data = os.path.join(base_dir, 'data', 'processed', 'dataset_perfiles_financieros.csv')
    prof_model = os.path.join(base_dir, 'models', 'profile_model.pkl')
    train_profile_model(prof_data, prof_model)
