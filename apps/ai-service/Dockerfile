FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
RUN useradd --system --create-home appuser && chown -R appuser:appuser /app
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /root/.cache
USER appuser
EXPOSE 8090
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8090/health || exit 1
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8090"]

