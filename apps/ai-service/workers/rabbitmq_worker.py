import pika
import os
import json
import logging

logger = logging.getLogger(__name__)

class RabbitMQClient:
    def __init__(self):
        self.host = os.getenv("RABBITMQ_HOST", "localhost")
        self.port = int(os.getenv("RABBITMQ_PORT", "5672"))
        self.user = os.getenv("RABBITMQ_USER", "guest")
        self.password = os.getenv("RABBITMQ_PASSWORD", "guest")
        self.queue = "ai_tasks"

    def publish(self, message: dict) -> bool:
        try:
            creds = pika.PlainCredentials(self.user, self.password)
            params = pika.ConnectionParameters(
                host=self.host, port=self.port,
                credentials=creds,
                connection_attempts=1,
                retry_delay=1,
                socket_timeout=3
            )
            conn = pika.BlockingConnection(params)
            ch = conn.channel()
            ch.queue_declare(queue=self.queue, durable=True)
            ch.basic_publish(
                exchange="",
                routing_key=self.queue,
                body=json.dumps(message),
                properties=pika.BasicProperties(delivery_mode=2)
            )
            conn.close()
            return True
        except Exception as e:
            logger.warning(f"RabbitMQ no disponible: {e}")
            return False
