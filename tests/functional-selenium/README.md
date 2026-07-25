# Pruebas Funcionales — Selenium + Cucumber

Subproyecto Maven autónomo que ejecuta pruebas de aceptación E2E contra el entorno de QA del Sistema de Gestión de Pasantías UCE.

## Tecnologías

| Herramienta | Versión | Rol |
|---|---|---|
| Cucumber Java | 7.18.0 | Framework BDD — archivos `.feature` en español |
| Selenium Java | 4.21.0 | Automatización del navegador |
| WebDriverManager | 5.8.0 | Gestión automática del chromedriver |
| JUnit 5 Platform | 1.10.2 | Motor de ejecución |
| Python 3 | — | Generación del reporte Markdown |

## Estructura

```
tests/functional-selenium/
├── pom.xml                    # Dependencias Maven
├── mvnw / mvnw.cmd            # Maven Wrapper
├── generate_report.py         # Generador de REPORTE-PRUEBAS.md
├── REPORTE-PRUEBAS.md         # Reporte generado (auto)
└── src/test/
    ├── java/com/uce/functional/
    │   ├── RunCucumberTest.java   # Runner JUnit 5 + Cucumber
    │   ├── Hooks.java             # @Before/@After — capturas PASS/FAIL
    │   └── StepDefinitions.java  # Implementación de los steps
    └── resources/features/
        ├── 01_login.feature
        ├── 02_postulaciones.feature
        ├── 03_horas.feature
        ├── 04_evaluaciones.feature
        └── 05_notificaciones.feature
```

## Casos de Prueba

| # | Feature | Camino éxito | Camino alterno | Excepción |
|---|---------|-------------|----------------|-----------|
| 1 | Login y Navegación | Login estudiante | Login tutor | Credenciales inválidas |
| 2 | Postulaciones | Postulación → PENDIENTE | Coordinador → ACEPTADA/RECHAZADA | Campos vacíos |
| 3 | Horas | Registro → PENDIENTE | Tutor → VALIDADO | Horas negativas (**DEF-API-001**) |
| 4 | Evaluaciones | Tutor califica (0-10) | Nota fuera de rango bloqueada | Estudiante sin permiso |
| 5 | Notificaciones | Marcar como leída | Filtro de leídas | GET sin JWT → 401/403 |

## Ejecución local (requiere Chrome/Chromium en PATH)

```bash
cd tests/functional-selenium

# 1. Ejecutar pruebas
./mvnw clean test -Dmaven.test.failure.ignore=true

# 2. Generar reporte Markdown
python3 generate_report.py
```

## Salidas

- **Capturas**: `target/screenshots/{escenario}-{PASS|FAIL}-{timestamp}.png`
- **JSON de Cucumber**: `target/cucumber.json`
- **Reporte**: `REPORTE-PRUEBAS.md`

## Defectos conocidos documentados

| ID | Descripción | Feature afectada |
|----|-------------|-----------------|
| DEF-API-001 | Ausencia de Bean Validation en `hours-service` — acepta horas negativas o fechas futuras sin error | `03_horas.feature` — Escenario Excepción |

> El test del escenario de excepción en horas **no falla la suite** (`isFailed=false`)
> pero registra en el reporte: _"FALLA ESPERADA — relacionada a DEF-API-001"_.
