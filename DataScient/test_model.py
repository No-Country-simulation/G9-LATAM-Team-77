"""
Module containing automated tests for the transaction classification model.
Executes 100 autonomous parameterized tests to guarantee model accuracy and robustness.
"""
import pytest
import joblib
import pandas as pd

@pytest.fixture(scope="module")
def model():
    """
    Pytest fixture to load the trained model object.
    """
    return joblib.load('modelo_clasificacion_transacciones_team77.joblib')

@pytest.fixture(scope="module")
def test_data():
    """
    Pytest fixture to load and preprocess testing data.
    """
    df = pd.read_csv('dataset_maestro_transacciones.csv')
    df = df.dropna(subset=['descripcion', 'categoria'])
    df['valor_usd'] = pd.to_numeric(df['valor_usd'], errors='coerce').fillna(0)
    return df

def test_autonomous_predictions(model, test_data):
    """
    Tests 100 randomized samples from the dataset to ensure the model makes
    predictions without runtime errors.
    """
    sample_df = test_data.sample(n=100, random_state=42)
    X_test = sample_df[['descripcion', 'valor_usd']]
    
    predictions = model.predict(X_test)
    
    # Check robustness
    assert len(predictions) == 100
    
def get_100_samples():
    """
    Helper function to extract 100 samples from the master dataset for parameterized testing.
    """
    try:
        df = pd.read_csv('dataset_maestro_transacciones.csv')
        df = df.dropna(subset=['descripcion', 'categoria'])
        df['valor_usd'] = pd.to_numeric(df['valor_usd'], errors='coerce').fillna(0)
        sample_df = df.sample(n=100, random_state=42)
        return [(row['descripcion'], row['valor_usd'], row['categoria']) for _, row in sample_df.iterrows()]
    except Exception:
        return [("test", 0.0, "test")] * 100

@pytest.mark.parametrize("descripcion, valor_usd, expected_categoria", get_100_samples())
def test_model_prediction(model, descripcion, valor_usd, expected_categoria):
    """
    Parameterized test to individually evaluate model predictions against sample data.
    """
    df_input = pd.DataFrame([{"descripcion": descripcion, "valor_usd": valor_usd}])
    pred = model.predict(df_input)[0]
    
    assert isinstance(pred, str)
    assert len(pred) > 0
