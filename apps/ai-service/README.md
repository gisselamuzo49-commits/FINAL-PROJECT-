# AI Service

Microservicio de Inteligencia Artificial para el Sistema de Pasantías y Vinculación de la Universidad Central del Ecuador (UCE).

Construido en Python 3.11 con **FastAPI** y expuesto en el puerto `8090`.

## Características

1. **Recomendación de Ofertas (NLP - TF-IDF)**:
   - Endpoint: `POST /api/ai/recommend`
   - Ordena un listado de ofertas de pasantías según su relevancia respecto al perfil del estudiante usando `TfidfVectorizer` y `cosine_similarity` de `scikit-learn`.

2. **Predicción de Riesgo de Deserción (Random Forest)**:
   - Endpoint: `POST /api/ai/risk`
   - Clasifica el nivel de riesgo de un estudiante en `ALTO` o `BAJO` y devuelve la probabilidad asociada a través de un clasificador `RandomForestClassifier`.

3. **Publicación Asíncrona de Tareas (RabbitMQ)**:
   - Endpoint: `POST /api/ai/tasks`
   - Publica tareas de procesamiento en segundo plano en la cola `ai_tasks` de RabbitMQ de forma tolerante a fallos (*best-effort*).

## Requisitos de Instalación

1. Crear un entorno virtual de Python 3.11:
   ```bash
   python -m venv venv
   source venv/Scripts/activate  # En Windows: venv\Scripts\activate
   ```

2. Instalar las dependencias:
   ```bash
   pip install -r requirements.txt
   ```

## Ejecución

Para iniciar el servidor localmente en modo desarrollo:
```bash
uvicorn main:app --host 0.0.0.0 --port 8090 --reload
```

## Pruebas

Para ejecutar las pruebas automatizadas (con `pytest` y `httpx`):
```bash
pytest test_main.py -v
```
