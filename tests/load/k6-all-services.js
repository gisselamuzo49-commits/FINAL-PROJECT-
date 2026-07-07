import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 20,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.25'],
    http_req_duration: ['p(95)<2000'],
  },
};

const TARGET_HOST = __ENV.TARGET_HOST || 'localhost';
const BASE_URL = `http://${TARGET_HOST}`;

export default function () {
  const endpoints = [
    `${BASE_URL}`,
    `${BASE_URL}/api/users/health`,
    `${BASE_URL}/api/linkage/health`,
    `${BASE_URL}/api/hours/health`,
    `${BASE_URL}/api/evaluation/health`,
    `${BASE_URL}/api/internships`,
  ];

  const url = endpoints[Math.floor(Math.random() * endpoints.length)];
  const res = http.get(url, { timeout: '10s' });

  check(res, {
    'status is valid': (r) => r.status < 500 || r.status === 401,
    'response time < 2s': (r) => r.timings.duration < 2000,
  });

  sleep(0.5);
}
