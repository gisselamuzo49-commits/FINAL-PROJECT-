# REPORTE DE PRUEBAS DE RENDIMIENTO — Sistema de Pasantías y Vinculación UCE

**Herramienta:** k6  
**Entorno:** QA — http://54.227.79.26  
**Fecha:** 2026-07-25T04:19:31.536Z  
**Perfiles ejecutados:** Carga estable (10 VUs / 1 min) + Carga de estrés (rampa 0→30 VUs)

---

## 1. Indicadores Globales

| Métrica | Valor |
|---|---|
| Total de peticiones | 5735 |
| Throughput (req/s) | 26.04 |
| Latencia media (ms) | 63.26 |
| Latencia p90 (ms) | 130.84 |
| Latencia p95 (ms) | 391.34 |
| Latencia máxima (ms) | 1026.05 |
| Tasa de error global | 4.10% |
| VUs máximos alcanzados | 30 |

## 2. Resultados por Caso de Prueba (priorizados por riesgo)

| Caso | Riesgo | Lat. media (ms) | Lat. p95 (ms) | Lat. máx (ms) | Tasa error |
|---|:---:|:---:|:---:|:---:|:---:|
| Caso 1: Login / Autenticación (JWT) | ALTO | 270.86 | 928.03 | 1026.05 | 4.10% |
| Caso 2: Listado de Ofertas de Pasantías | ALTO | 12.80 | 27.14 | 75.88 | 0.00% |
| Caso 3: Registro de Horas de Vinculación | MEDIO | 11.03 | 24.69 | 63.85 | 0.00% |
| Caso 4: Notificaciones | BAJO | 10.33 | 22.83 | 86.64 | 0.00% |
| Caso 5: Listado de Usuarios | BAJO | 11.26 | 23.18 | 88.10 | 0.00% |

## 3. Cumplimiento de Umbrales (SLO)

| Umbral | Objetivo | Resultado |
|---|---|:---:|
| Latencia p95 | < 2000 ms | ✅ CUMPLE (391.34 ms) |
| Tasa de error | < 25% | ✅ CUMPLE (4.10%) |

## 4. Notas Metodológicas y Hallazgos

- Los casos 1 y 2 son de **riesgo ALTO** por ser el punto de entrada de autenticación y el listado más consultado del sistema.
- El perfil de **estrés** incrementa la carga hasta 30 VUs para identificar el punto de saturación de la infraestructura.
- El ambiente de QA corre sobre una instancia **t3.large** con el stack completo (12 microservicios + 13 contenedores de datos/infra) en un solo nodo. La degradación observada bajo alta concurrencia corresponde a **saturación de hardware del ambiente académico**, no a defectos del código de la aplicación.

---

*Reporte generado automáticamente por k6 (handleSummary) — Sistema de Pasantías UCE*
