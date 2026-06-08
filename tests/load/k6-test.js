import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    auth_hello_test: {
      executor: 'shared-iterations',
      vus: 100,             // 100 concurrent users
      iterations: 25000,     // 25,000 total requests
      maxDuration: '2m',    // timeout at 2 minutes
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'], // Allow up to 5% failure rate (e.g., database locks or rate limits)
    http_req_duration: ['p(95)<500'], // 95% of requests must complete under 500ms
  },
};

// Target base URL (can be customized via environment variable)
// - Local tunnel: 'http://localhost:8080' (Auth Service)
// - Direct VPC IP (from Bastion): 'http://10.0.3.242:8080'
const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';

export default function () {
  const url = `${BASE_URL}/api/auth/hello`;
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.get(url, params);
  
  check(res, {
    'status is 200 (Success)': (r) => r.status === 200,
    'response includes Hello text': (r) => r.body.includes('¡Hola desde el Backend'),
  });
}
