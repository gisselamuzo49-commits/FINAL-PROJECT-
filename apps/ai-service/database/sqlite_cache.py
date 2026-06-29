import sqlite3
import json
import os
from datetime import datetime

DB_PATH = os.getenv("SQLITE_DB_PATH", "/app/data/ai_cache.db")

def init_db():
    dir_name = os.path.dirname(DB_PATH)
    if dir_name:
        os.makedirs(dir_name, exist_ok=True)
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
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute(
        "SELECT risk_score, recommendation FROM prediction_cache WHERE estudiante_id = ?",
        (estudiante_id,)
    )
    result = cursor.fetchone()
    conn.close()
    return result

def save_prediction(estudiante_id: str, risk_score: float, recommendation: str):
    conn = sqlite3.connect(DB_PATH)
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
