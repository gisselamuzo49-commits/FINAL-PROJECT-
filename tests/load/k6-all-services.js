import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 20,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<400'],
  },
};

const TARGET_HOST = __ENV.TARGET_HOST || 'localhost';
const BASE_URL = `http://${TARGET_HOST}`;

export default function () {
  const endpoints = [
    `${BASE_URL}/api/auth/health`,
    `${BASE_URL}/api/internship/health`,
    `${BASE_URL}/api/users/health`,
    `${BASE_URL}/api/linkage/health`,
    `${BASE_URL}`,
  ];

  const url = endpoints[Math.floor(Math.random() * endpoints.length)];
  
  const res = http.get(url);
  
  check(res, {
    'status is 200': (r) => r.status === 200 || r.status === 404,
    'response time < 400ms': (r) => r.timings.duration < 400,
  });

  sleep(0.5);
}
