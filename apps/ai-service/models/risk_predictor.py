from sklearn.ensemble import RandomForestClassifier
import numpy as np

class RiskPredictor:
    def __init__(self):
        # Datos sintéticos de entrenamiento (suficiente para demo académica)
        X = np.array([
            [0, 0, 5, 30], [2, 1, 3, 20], [10, 5, 0, 5],
            [20, 10, 0, 2], [1, 0, 8, 45], [15, 3, 1, 7],
            [0, 2, 10, 60], [8, 4, 2, 10], [3, 1, 6, 35],
            [25, 8, 0, 1],
        ])
        y = np.array([1, 1, 0, 0, 1, 0, 1, 0, 1, 0])  # 1=ALTO, 0=BAJO
        self.model = RandomForestClassifier(n_estimators=10, random_state=42)
        self.model.fit(X, y)

    def predict(self, horas_validadas: float, horas_pendientes: float,
                horas_rechazadas: float, dias_sin_actividad: float) -> dict:
        features = np.array([[horas_validadas, horas_pendientes,
                              horas_rechazadas, dias_sin_actividad]])
        pred = self.model.predict(features)[0]
        prob = self.model.predict_proba(features)[0][pred]
        return {"riesgo": "ALTO" if pred == 1 else "BAJO",
                "probabilidad": round(float(prob), 4)}
