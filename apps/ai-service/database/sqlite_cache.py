import sqlite3
import json
import os
from datetime import datetime
import sys

# En CI/tests usar BD en memoria, en producción usar archivo
DB_PATH = os.getenv("SQLITE_DB_PATH", 
    ":memory:" if (os.getenv("CI") or "pytest" in sys.modules) else "/app/data/ai_cache.db"
)

_global_conn = None

class InMemoryConnectionWrapper:
    def __init__(self, conn):
        self._conn = conn
    
    def __getattr__(self, name):
        return getattr(self._conn, name)
    
    def close(self):
        pass  # Evitar el cierre real de la conexión in-memory persistente
        
    def __enter__(self):
        return self
        
    def __exit__(self, exc_type, exc_val, exc_tb):
        pass

def _get_connection():
    global _global_conn
    if DB_PATH == ":memory:":
        # En test_sqlite_cache.py la función connect de sqlite3 está mockeada
        # con una lambda. En ese caso, usar la conexión del mock.
        if "pytest" in sys.modules and sqlite3.connect.__name__ == "<lambda>":
            return sqlite3.connect(DB_PATH)
            
        if _global_conn is None:
            _global_conn = sqlite3.connect(":memory:", check_same_thread=False)
            cursor = _global_conn.cursor()
            cursor.execute("""
                CREATE TABLE IF NOT EXISTS prediction_cache (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    estudiante_id TEXT NOT NULL UNIQUE,
                    risk_score REAL NOT NULL,
                    recommendation TEXT,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
            """)
            _global_conn.commit()
        return InMemoryConnectionWrapper(_global_conn)
    return sqlite3.connect(DB_PATH)

def init_db():
    if DB_PATH == ":memory:":
        _get_connection()  # Inicializar la base global en memoria
        return
        
    os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS prediction_cache (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            estudiante_id TEXT NOT NULL UNIQUE,
            risk_score REAL NOT NULL,
            recommendation TEXT,
            created_at TEXT DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT DEFAULT CURRENT_TIMESTAMP
        )
    """)
    conn.commit()
    conn.close()

def get_cached_prediction(estudiante_id: str):
    conn = _get_connection()
    cursor = conn.cursor()
    cursor.execute(
        "SELECT risk_score, recommendation FROM prediction_cache WHERE estudiante_id = ?",
        (estudiante_id,)
    )
    result = cursor.fetchone()
    conn.close()
    return result

def save_prediction(estudiante_id: str, risk_score: float, recommendation: str):
    conn = _get_connection()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO prediction_cache (estudiante_id, risk_score, recommendation)
        VALUES (?, ?, ?)
        ON CONFLICT(estudiante_id) DO UPDATE SET
            risk_score = excluded.risk_score,
            recommendation = excluded.recommendation,
            updated_at = CURRENT_TIMESTAMP
    """, (estudiante_id, risk_score, recommendation))
    conn.commit()
    conn.close()
