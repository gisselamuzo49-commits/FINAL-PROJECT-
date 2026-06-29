import os
import pytest
import sqlite3
import database.sqlite_cache as sqlite_cache

TEST_DB_PATH = os.path.abspath("test_ai_cache.db")

@pytest.fixture(autouse=True)
def setup_test_db():
    # Guardar el path original
    original_path = sqlite_cache.DB_PATH
    # Redefinir al path de prueba
    sqlite_cache.DB_PATH = TEST_DB_PATH
    
    # Inicializar la base de datos
    sqlite_cache.init_db()
    
    yield
    
    # Restaurar y limpiar el archivo de prueba
    sqlite_cache.DB_PATH = original_path
    if os.path.exists(TEST_DB_PATH):
        try:
            os.remove(TEST_DB_PATH)
        except Exception:
            pass

def test_init_db_creates_table():
    conn = sqlite3.connect(TEST_DB_PATH)
    cursor = conn.cursor()
    # Verificar si la tabla existe
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='prediction_cache'")
    table_exists = cursor.fetchone()
    conn.close()
    
    assert table_exists is not None
    assert table_exists[0] == "prediction_cache"

def test_save_and_get_cached_prediction():
    estudiante_id = "EST12345"
    risk_score = 0.85
    recommendation = "ALTO"
    
    # Guardar en caché
    sqlite_cache.save_prediction(estudiante_id, risk_score, recommendation)
    
    # Obtener de caché
    cached = sqlite_cache.get_cached_prediction(estudiante_id)
    
    assert cached is not None
    assert cached[0] == risk_score
    assert cached[1] == recommendation

def test_save_prediction_overwrite():
    estudiante_id = "EST12345"
    
    # Guardar primera vez
    sqlite_cache.save_prediction(estudiante_id, 0.30, "BAJO")
    
    # Guardar segunda vez (sobreescribir)
    sqlite_cache.save_prediction(estudiante_id, 0.75, "ALTO")
    
    cached = sqlite_cache.get_cached_prediction(estudiante_id)
    
    assert cached is not None
    assert cached[0] == 0.75
    assert cached[1] == "ALTO"
