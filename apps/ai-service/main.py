from fastapi import FastAPI, status
from pydantic import BaseModel, Field
from typing import List, Dict, Any
from models.recommender import TFIDFRecommender
from models.risk_predictor import RiskPredictor
from workers.rabbitmq_worker import RabbitMQClient

app = FastAPI(
    title="AI Service — Sistema de Pasantías UCE",
    description="Microservicio de Inteligencia Artificial para la Gestión de Pasantías y Vinculación con la Sociedad (NLP con TF-IDF, predicción de riesgo con Random Forest, cola asíncrona con RabbitMQ).",
    version="1.0.0",
)

# Initialize models and clients
recommender = TFIDFRecommender()
risk_predictor = RiskPredictor()
rabbitmq_client = RabbitMQClient()

# Schemas
class OfertaItem(BaseModel):
    id: str
    descripcion: str

class RecommendRequest(BaseModel):
    estudianteId: str
    perfil: str
    ofertas: List[OfertaItem]

class RecommendResponseItem(BaseModel):
    id: str
    score: float

class RecommendResponse(BaseModel):
    estudianteId: str
    recomendaciones: List[RecommendResponseItem]

class RiskRequest(BaseModel):
    estudianteId: str
    horasValidadas: float
    horasPendientes: float
    horasRechazadas: float
    diasSinActividad: float

class RiskResponse(BaseModel):
    estudianteId: str
    riesgo: str
    probabilidad: float

class TaskRequest(BaseModel):
    tipo: str
    payload: Dict[str, Any]

# Endpoints
@app.get("/health")
def health():
    return {"status": "ok", "service": "ai-service"}

@app.post("/api/ai/recommend", response_model=RecommendResponse)
def recommend(req: RecommendRequest):
    if not req.ofertas:
        return RecommendResponse(estudianteId=req.estudianteId, recomendaciones=[])
    
    # Map pydantic list of models to list of dicts for recommender
    ofertas_list = [{"id": o.id, "descripcion": o.descripcion} for o in req.ofertas]
    results = recommender.recommend(req.perfil, ofertas_list)
    
    # Map results to output schema
    recs = [RecommendResponseItem(id=r["id"], score=r["score"]) for r in results]
    return RecommendResponse(estudianteId=req.estudianteId, recomendaciones=recs)

@app.post("/api/ai/risk", response_model=RiskResponse)
def risk(req: RiskRequest):
    res = risk_predictor.predict(
        horas_validadas=req.horasValidadas,
        horas_pendientes=req.horasPendientes,
        horas_rechazadas=req.horasRechazadas,
        dias_sin_actividad=req.diasSinActividad
    )
    return RiskResponse(
        estudianteId=req.estudianteId,
        riesgo=res["riesgo"],
        probabilidad=res["probabilidad"]
    )

@app.post("/api/ai/tasks")
def publish_task(req: TaskRequest):
    success = rabbitmq_client.publish({"tipo": req.tipo, "payload": req.payload})
    if success:
        return {"queued": True}
    else:
        return {"queued": False, "reason": "RabbitMQ unavailable"}
