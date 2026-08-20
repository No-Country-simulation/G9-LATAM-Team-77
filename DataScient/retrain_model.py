"""
Module for retraining the transaction classification model.
It handles preprocessing of text and numerical data and creates a pipeline
with TfidfVectorizer, StandardScaler, and LogisticRegression.
"""
import pandas as pd
import joblib
from sklearn.pipeline import Pipeline
from sklearn.compose import ColumnTransformer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler
from sklearn.impute import SimpleImputer

def retrain():
    """
    Load dataset, handle missing values, build preprocessing pipeline,
    train the classification model, and save it as a joblib artifact.
    """
    print("Loading data...")
    df = pd.read_csv('dataset_maestro_transacciones.csv')
    
    # Handle missing values to be robust
    df['descripcion'] = df['descripcion'].fillna('')
    df['categoria'] = df['categoria'].fillna('Desconocida')
    df['valor_usd'] = pd.to_numeric(df['valor_usd'], errors='coerce')
    
    X = df[['descripcion', 'valor_usd']]
    y = df['categoria']
    
    print("Training model...")
    # Preprocessing: Text feature + Numerical feature (Value in USD for normalized perspective)
    preprocessor = ColumnTransformer(
        transformers=[
            ('text', TfidfVectorizer(max_features=5000, stop_words='english'), 'descripcion'),
            ('num', Pipeline([
                ('imputer', SimpleImputer(strategy='median')),
                ('scaler', StandardScaler())
            ]), ['valor_usd'])
        ])
    
    # Basic robust pipeline
    pipeline = Pipeline([
        ('preprocessor', preprocessor),
        ('clf', LogisticRegression(max_iter=1000, class_weight='balanced'))
    ])
    
    pipeline.fit(X, y)
    
    # Save the model
    print("Saving model...")
    joblib.dump(pipeline, 'modelo_clasificacion_transacciones_team77.joblib')
    print("Retraining complete. Model saved.")

if __name__ == '__main__':
    retrain()
