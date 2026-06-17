import pytest
from httpx import AsyncClient, ASGITransport
from main import app

@pytest.fixture
def anyio_backend():
    return 'asyncio'

@pytest.mark.anyio
async def test_health():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok", "service": "ai-service"}

@pytest.mark.anyio
async def test_recommend_con_ofertas():
    payload = {
        "estudianteId": "est1",
        "perfil": "Desarrollador web frontend con experiencia en React y CSS",
        "ofertas": [
            {"id": "of1", "descripcion": "Prácticas de soporte técnico de hardware y redes locales"},
            {"id": "of2", "descripcion": "Pasantías en desarrollo frontend React, maquetación CSS y responsive web design"},
            {"id": "of3", "descripcion": "Desarrollo de bases de datos relacionales en Oracle SQL"}
        ]
    }
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post("/api/ai/recommend", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    assert data["estudianteId"] == "est1"
    recs = data["recomendaciones"]
    assert len(recs) == 3
    # Check that it's ordered by score descending
    assert recs[0]["score"] >= recs[1]["score"]
    assert recs[1]["score"] >= recs[2]["score"]
    # of2 should have the highest score because of React and CSS keywords
    assert recs[0]["id"] == "of2"

@pytest.mark.anyio
async def test_recommend_sin_ofertas():
    payload = {
        "estudianteId": "est1",
        "perfil": "Desarrollador web frontend con experiencia en React y CSS",
        "ofertas": []
    }
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post("/api/ai/recommend", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    assert data["estudianteId"] == "est1"
    assert data["recomendaciones"] == []

@pytest.mark.anyio
async def test_risk_alto():
    # 0 horas validadas, 10 rechazadas, 60 días sin actividad -> Alto riesgo
    payload = {
        "estudianteId": "est2",
        "horasValidadas": 0.0,
        "horasPendientes": 0.0,
        "horasRechazadas": 10.0,
        "diasSinActividad": 60.0
    }
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post("/api/ai/risk", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    assert data["estudianteId"] == "est2"
    assert data["riesgo"] == "ALTO"
    assert "probabilidad" in data

@pytest.mark.anyio
async def test_risk_bajo():
    # 25 horas validadas, 0 rechazadas, 1 día sin actividad -> Bajo riesgo
    payload = {
        "estudianteId": "est3",
        "horasValidadas": 25.0,
        "horasPendientes": 0.0,
        "horasRechazadas": 0.0,
        "diasSinActividad": 1.0
    }
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post("/api/ai/risk", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    assert data["estudianteId"] == "est3"
    assert data["riesgo"] == "BAJO"
    assert "probabilidad" in data

@pytest.mark.anyio
async def test_task_rabbitmq_no_disponible():
    # Test publishing task when RabbitMQ is down
    payload = {
        "tipo": "procesar_perfil",
        "payload": {"id": "123"}
    }
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post("/api/ai/tasks", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    assert data["queued"] is False
    assert data["reason"] == "RabbitMQ unavailable"

