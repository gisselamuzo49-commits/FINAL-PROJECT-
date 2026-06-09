import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuración de la prueba de carga
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // Escala de 0 a 20 usuarios virtuales en 30s
    { duration: '1m', target: 20 },   // Mantiene 20 usuarios virtuales concurrentes durante 1 min
    { duration: '30s', target: 0 },   // Reduce a 0 usuarios en 30s
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],    // Tolerancia menor al 1% de errores en peticiones
    http_req_duration: ['p(95)<400'],  // El 95% de las peticiones debe tardar menos de 400ms
  },
};

// IP/Dominio de destino (configurable por variable de entorno)
const TARGET_HOST = __ENV.TARGET_HOST || 'localhost';

export default function () {
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  // 1. Probar Auth Service (Puerto 8080)
  const authRes = http.get(`http://${TARGET_HOST}:8080/api/auth/hello`, params);
  check(authRes, {
    'Auth Service responde 200': (r) => r.status === 200,
  });

  // 2. Probar Internship Service (Puerto 8081)
  const internshipRes = http.get(`http://${TARGET_HOST}:8081/api/internships`, params);
  check(internshipRes, {
    'Internship Service responde 200': (r) => r.status === 200,
  });

  // 3. Probar User Service (Puerto 8083) - Nuevo
  const userRes = http.get(`http://${TARGET_HOST}:8083/health`, params);
  check(userRes, {
    'User Service responde 200': (r) => r.status === 200,
    'User Service responde con health text': (r) => r.body.includes('user-service is running'),
  });

  // 4. Probar Linkage Service (Puerto 8084) - Nuevo
  const linkageRes = http.get(`http://${TARGET_HOST}:8084/health`, params);
  check(linkageRes, {
    'Linkage Service responde 200': (r) => r.status === 200,
    'Linkage Service responde con health text': (r) => r.body.includes('linkage-service is running'),
  });

  // Tiempo de espera simulado para simular comportamiento humano (1 segundo)
  sleep(1);
}
