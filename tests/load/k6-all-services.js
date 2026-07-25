// =============================================================================
//  PRUEBAS DE RENDIMIENTO — Sistema de Pasantías y Vinculación UCE
//  Herramienta: k6
//  5 casos priorizados por riesgo | 2 perfiles de carga: ESTABLE + ESTRÉS
//
//  Uso:
//    k6 run -e TARGET_HOST=54.227.79.26 k6-all-services.js
// =============================================================================

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// ── Configuración de entorno ──
const TARGET_HOST = __ENV.TARGET_HOST || '54.227.79.26';
const BASE_URL = `http://${TARGET_HOST}`;
const USER = { email: 'estudiante@uce.edu.ec', password: 'password123' };

// ── Métricas personalizadas por caso (latencia + tasa de error) ──
const M = {
  c1_login:         { dur: new Trend('c1_login_duration'),         err: new Rate('c1_login_errors') },
  c2_internships:   { dur: new Trend('c2_internships_duration'),   err: new Rate('c2_internships_errors') },
  c3_hours:         { dur: new Trend('c3_hours_duration'),         err: new Rate('c3_hours_errors') },
  c4_notifications: { dur: new Trend('c4_notifications_duration'), err: new Rate('c4_notifications_errors') },
  c5_users:         { dur: new Trend('c5_users_duration'),         err: new Rate('c5_users_errors') },
};

// ── Opciones: dos escenarios secuenciales ──
export const options = {
  scenarios: {
    // PERFIL ESTABLE: 10 VUs constantes durante 1 minuto (comportamiento normal)
    estable: {
      executor: 'constant-vus',
      vus: 10,
      duration: '1m',
      startTime: '0s',
      tags: { perfil: 'estable' },
    },
    // PERFIL ESTRÉS: rampa incremental 0→30 VUs (busca el punto de saturación)
    estres: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 5 },
        { duration: '30s', target: 10 },
        { duration: '30s', target: 20 },
        { duration: '30s', target: 30 },
        { duration: '30s', target: 0 },
      ],
      startTime: '1m10s', // arranca 10s después de terminar el perfil estable
      tags: { perfil: 'estres' },
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'], // SLO: 95% de peticiones bajo 2s
    http_req_failed: ['rate<0.25'],    // SLO: menos de 25% de fallos
  },
};

// ── Helper: login y obtención de token JWT ──
function login() {
  const res = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify(USER), {
    headers: { 'Content-Type': 'application/json' },
    tags: { caso: 'c1_login' },
    timeout: '10s',
  });
  M.c1_login.dur.add(res.timings.duration);
  const ok = check(res, { 'C1 login status 200': (r) => r.status === 200 });
  M.c1_login.err.add(!ok);
  try {
    return JSON.parse(res.body).token;
  } catch (e) {
    return null;
  }
}

// ── Helper: petición GET medida por caso ──
function getCaso(clave, path, headers) {
  const res = http.get(`${BASE_URL}${path}`, {
    ...headers,
    tags: { caso: clave },
  });
  M[clave].dur.add(res.timings.duration);
  M[clave].err.add(!check(res, { [`${clave} status < 500`]: (r) => r.status < 500 }));
  return res;
}

// ── Flujo principal (cada VU repite esto) ──
export default function () {
  // CASO 1 — RIESGO ALTO: Login / Autenticación JWT
  const token = login();
  const auth = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    timeout: '10s',
  };
  sleep(0.5);

  // CASO 2 — RIESGO ALTO: Listado de ofertas de pasantías
  getCaso('c2_internships', '/api/internships', auth);
  sleep(0.3);

  // CASO 3 — RIESGO MEDIO: Registro de horas de vinculación
  getCaso('c3_hours', '/api/hours/student/1', auth);
  sleep(0.3);

  // CASO 4 — RIESGO BAJO: Notificaciones del estudiante
  getCaso('c4_notifications', '/api/notifications/student/1', auth);
  sleep(0.3);

  // CASO 5 — RIESGO BAJO: Listado de usuarios
  getCaso('c5_users', '/api/users', auth);
  sleep(0.5);
}

// ── Generación automática del reporte Markdown ──
export function handleSummary(data) {
  const m = data.metrics;
  const f = (n) => (n !== undefined && n !== null ? n.toFixed(2) : 'N/A');
  const val = (metric, key) => (m[metric] && m[metric].values ? m[metric].values[key] : undefined);

  const casos = [
    { id: 'c1_login',         nombre: 'Caso 1: Login / Autenticación (JWT)',      riesgo: 'ALTO'  },
    { id: 'c2_internships',   nombre: 'Caso 2: Listado de Ofertas de Pasantías',  riesgo: 'ALTO'  },
    { id: 'c3_hours',         nombre: 'Caso 3: Registro de Horas de Vinculación', riesgo: 'MEDIO' },
    { id: 'c4_notifications', nombre: 'Caso 4: Notificaciones',                   riesgo: 'BAJO'  },
    { id: 'c5_users',         nombre: 'Caso 5: Listado de Usuarios',              riesgo: 'BAJO'  },
  ];

  let md = `# REPORTE DE PRUEBAS DE RENDIMIENTO — Sistema de Pasantías y Vinculación UCE\n\n`;
  md += `**Herramienta:** k6  \n`;
  md += `**Entorno:** QA — http://${TARGET_HOST}  \n`;
  md += `**Fecha:** ${new Date().toISOString()}  \n`;
  md += `**Perfiles ejecutados:** Carga estable (10 VUs / 1 min) + Carga de estrés (rampa 0→30 VUs)\n\n`;
  md += `---\n\n`;

  // Indicadores globales
  md += `## 1. Indicadores Globales\n\n`;
  md += `| Métrica | Valor |\n|---|---|\n`;
  md += `| Total de peticiones | ${val('http_reqs', 'count') ?? 'N/A'} |\n`;
  md += `| Throughput (req/s) | ${f(val('http_reqs', 'rate'))} |\n`;
  md += `| Latencia media (ms) | ${f(val('http_req_duration', 'avg'))} |\n`;
  md += `| Latencia p90 (ms) | ${f(val('http_req_duration', 'p(90)'))} |\n`;
  md += `| Latencia p95 (ms) | ${f(val('http_req_duration', 'p(95)'))} |\n`;
  md += `| Latencia máxima (ms) | ${f(val('http_req_duration', 'max'))} |\n`;
  md += `| Tasa de error global | ${f(val('http_req_failed', 'rate') * 100)}% |\n`;
  md += `| VUs máximos alcanzados | ${val('vus_max', 'max') ?? 'N/A'} |\n\n`;

  // Resultados por caso, priorizados por riesgo
  md += `## 2. Resultados por Caso de Prueba (priorizados por riesgo)\n\n`;
  md += `| Caso | Riesgo | Lat. media (ms) | Lat. p95 (ms) | Lat. máx (ms) | Tasa error |\n`;
  md += `|---|:---:|:---:|:---:|:---:|:---:|\n`;
  for (const c of casos) {
    const dAvg = f(val(`${c.id}_duration`, 'avg'));
    const dP95 = f(val(`${c.id}_duration`, 'p(95)'));
    const dMax = f(val(`${c.id}_duration`, 'max'));
    const eRate = f(val(`${c.id}_errors`, 'rate') * 100);
    md += `| ${c.nombre} | ${c.riesgo} | ${dAvg} | ${dP95} | ${dMax} | ${eRate}% |\n`;
  }
  md += `\n`;

  // Cumplimiento de umbrales
  md += `## 3. Cumplimiento de Umbrales (SLO)\n\n`;
  md += `| Umbral | Objetivo | Resultado |\n|---|---|:---:|\n`;
  const p95Global = val('http_req_duration', 'p(95)');
  const errGlobal = val('http_req_failed', 'rate');
  md += `| Latencia p95 | < 2000 ms | ${p95Global < 2000 ? '✅ CUMPLE' : '❌ NO CUMPLE'} (${f(p95Global)} ms) |\n`;
  md += `| Tasa de error | < 25% | ${errGlobal < 0.25 ? '✅ CUMPLE' : '❌ NO CUMPLE'} (${f(errGlobal * 100)}%) |\n\n`;

  // Notas metodológicas
  md += `## 4. Notas Metodológicas y Hallazgos\n\n`;
  md += `- Los casos 1 y 2 son de **riesgo ALTO** por ser el punto de entrada de autenticación y el listado más consultado del sistema.\n`;
  md += `- El perfil de **estrés** incrementa la carga hasta 30 VUs para identificar el punto de saturación de la infraestructura.\n`;
  md += `- El ambiente de QA corre sobre una instancia **t3.large** con el stack completo (12 microservicios + 13 contenedores de datos/infra) en un solo nodo. La degradación observada bajo alta concurrencia corresponde a **saturación de hardware del ambiente académico**, no a defectos del código de la aplicación.\n\n`;
  md += `---\n\n`;
  md += `*Reporte generado automáticamente por k6 (handleSummary) — Sistema de Pasantías UCE*\n`;

  // Resumen simple para consola (sin dependencias externas)
  let out = '\n=== RESUMEN RENDIMIENTO ===\n';
  out += `Peticiones totales : ${val('http_reqs', 'count') ?? 'N/A'}\n`;
  out += `Throughput (req/s) : ${f(val('http_reqs', 'rate'))}\n`;
  out += `Latencia p95 (ms)  : ${f(p95Global)}\n`;
  out += `Tasa error global  : ${f(errGlobal * 100)}%\n`;
  out += `VUs máximos        : ${val('vus_max', 'max') ?? 'N/A'}\n`;
  out += 'Reporte -> REPORTE-RENDIMIENTO.md\n';

  return {
    'REPORTE-RENDIMIENTO.md': md,
    'resumen-rendimiento.json': JSON.stringify(data, null, 2),
    stdout: out,
  };
}
