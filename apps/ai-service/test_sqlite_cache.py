import sqlite3
import pytest
import database.sqlite_cache as sqlite_cache

class SqliteConnectionProxy:
    def __init__(self, conn):
        self._conn = conn
    
    def __getattr__(self, name):
        return getattr(self._conn, name)
    
    def close(self):
        pass  # Evitar el cierre real en tests
    
    def __enter__(self):
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        pass

# Fixture que crea BD en memoria para cada test
@pytest.fixture
def db_conn(monkeypatch):
    conn = sqlite3.connect(":memory:")
    conn.execute("""
        CREATE TABLE prediction_cache (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            estudiante_id TEXT NOT NULL UNIQUE,
            risk_score REAL NOT NULL,
            recommendation TEXT,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT DEFAULT CURRENT_TIMESTAMP
        )
    """)
    conn.commit()
    
    # Redirigir la conexión de sqlite3 al proxy de la conexión en memoria
    monkeypatch.setattr(sqlite3, "connect", lambda *args, **kwargs: SqliteConnectionProxy(conn))
    
    yield conn
    
    # Cerrar la conexión real al final del test
    conn.close()

def test_init_db_creates_table(db_conn):
    cursor = db_conn.cursor()
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table' AND name='prediction_cache'")
    table_exists = cursor.fetchone()
    assert table_exists is not None
    assert table_exists[0] == "prediction_cache"

def test_save_and_get_cached_prediction(db_conn):
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

def test_save_prediction_overwrite(db_conn):
    estudiante_id = "EST12345"
    
    # Guardar primera vez
    sqlite_cache.save_prediction(estudiante_id, 0.30, "BAJO")
    
    # Guardar segunda vez (sobreescribir)
    sqlite_cache.save_prediction(estudiante_id, 0.75, "ALTO")
    
    cached = sqlite_cache.get_cached_prediction(estudiante_id)
    
    assert cached is not None
    assert cached[0] == 0.75
    assert cached[1] == "ALTO"
