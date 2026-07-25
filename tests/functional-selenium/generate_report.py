#!/usr/bin/env python3
"""
generate_report.py — Generador automático de REPORTE-PRUEBAS.md

Lee target/cucumber.json (salida de Cucumber), busca las capturas en
target/screenshots/ y genera el reporte Markdown consolidado con la
estructura requerida por la rúbrica de pruebas funcionales.

Uso:
    cd tests/functional-selenium
    python generate_report.py

Requisito: haber ejecutado las pruebas antes con ./mvnw clean test
"""

import json
import os
import re
import sys
from datetime import datetime
from pathlib import Path

# ── Configuración ──────────────────────────────────────────────────────────────

CUCUMBER_JSON = Path("target/cucumber.json")
SCREENSHOTS_DIR = Path("target/screenshots")
OUTPUT_MD = Path("REPORTE-PRUEBAS.md")

# Defectos conocidos asociados por palabras clave del nombre del escenario
DEFECTOS_CONOCIDOS = {
    "horas negativas": "DEF-API-001 (ausencia de Bean Validation en hours-service)",
    "fecha en el futuro": "DEF-API-001 (ausencia de Bean Validation en hours-service)",
    "intenta registrar": "DEF-API-001 (ausencia de Bean Validation en hours-service)",
}

# Nombres de los 5 casos de prueba (Feature) en orden
CASOS_ESPERADOS = [
    "Login y Navegación de Usuarios",
    "Flujo de Postulaciones a Pasantías",
    "Registro y Validación de Horas de Vinculación",
    "Calificación y Evaluaciones de Pasantías",
    "Gestión de Notificaciones y Seguridad de Acceso",
]

# Objetivos por caso
OBJETIVOS = {
    "Login y Navegación de Usuarios":
        "Verificar que los usuarios (Estudiante y Tutor) pueden autenticarse correctamente y son redirigidos al panel según su rol, y que credenciales incorrectas son rechazadas.",
    "Flujo de Postulaciones a Pasantías":
        "Verificar que un estudiante puede postularse a una oferta (quedando en PENDIENTE), que el coordinador puede cambiar el estado (ACEPTADA/RECHAZADA) y que campos vacíos impiden el envío.",
    "Registro y Validación de Horas de Vinculación":
        "Verificar que un estudiante puede registrar horas válidas (en PENDIENTE), que el tutor puede validarlas (a VALIDADO/RECHAZADO), y que valores inválidos (negativos o fecha futura) son rechazados.",
    "Calificación y Evaluaciones de Pasantías":
        "Verificar que el tutor puede registrar evaluaciones con calificación 0-10, que valores fuera de rango son bloqueados, y que el acceso de estudiantes a las evaluaciones de tutores es denegado.",
    "Gestión de Notificaciones y Seguridad de Acceso":
        "Verificar que un usuario puede marcar notificaciones como leídas, filtrar el listado y que el acceso al API de notificaciones sin token JWT es rechazado con 401/403 por el Gateway.",
}

# ── Funciones auxiliares ───────────────────────────────────────────────────────

def normalizar_nombre(nombre: str) -> str:
    """Normaliza el nombre del escenario igual que Hooks.java."""
    n = re.sub(r"[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\s]", "", nombre)
    n = re.sub(r"\s+", "-", n).lower()
    return n[:80]

def buscar_captura(nombre_escenario: str, resultado: str) -> str | None:
    """Busca en target/screenshots/ el archivo que corresponde al escenario y resultado."""
    prefijo = normalizar_nombre(nombre_escenario) + "-" + resultado.upper()
    if not SCREENSHOTS_DIR.exists():
        return None
    for archivo in sorted(SCREENSHOTS_DIR.iterdir()):
        if archivo.name.startswith(prefijo) and archivo.suffix == ".png":
            return "screenshots/" + archivo.name
    # Fallback: buscar cualquier coincidencia parcial del nombre
    palabras = nombre_escenario.lower().split()[:4]
    for archivo in sorted(SCREENSHOTS_DIR.iterdir()):
        if archivo.suffix == ".png" and all(p in archivo.name.lower() for p in palabras):
            return "screenshots/" + archivo.name
    return None

def estado_emoji(status: str) -> str:
    return "✅ PASS" if status == "passed" else "❌ FAIL"

def detectar_defecto(nombre_escenario: str) -> str | None:
    nombre_lower = nombre_escenario.lower()
    for clave, defecto in DEFECTOS_CONOCIDOS.items():
        if clave in nombre_lower:
            return defecto
    return None

def get_scenario_status(scenario: dict) -> str:
    """Devuelve 'passed' o 'failed' evaluando los steps del escenario."""
    for step in scenario.get("steps", []):
        result = step.get("result", {})
        if result.get("status") in ("failed", "skipped", "undefined"):
            return "failed"
    return "passed"

def clasificar_camino(nombre: str) -> str:
    """Determina si el escenario es éxito, alterno o excepción."""
    n = nombre.lower()
    if any(k in n for k in ["éxito", "exito", "correctamente", "exitosamente", "válido", "valido"]):
        return "exito"
    if any(k in n for k in ["alterno", "cambia", "coordinador", "tutor valida", "filtro", "filtrado", "leídas"]):
        return "alterno"
    return "excepcion"

# ── Parser de cucumber.json ─────────────────────────────────────────────────────

def parsear_cucumber_json() -> list[dict]:
    """Lee cucumber.json y devuelve una lista de features con sus escenarios."""
    if not CUCUMBER_JSON.exists():
        print(f"[ERROR] No se encontró {CUCUMBER_JSON}. Ejecuta ./mvnw clean test primero.")
        sys.exit(1)

    with open(CUCUMBER_JSON, encoding="utf-8") as f:
        datos = json.load(f)

    features = []
    for feature in datos:
        nombre_feature = feature.get("name", "Desconocido")
        escenarios = []
        for elemento in feature.get("elements", []):
            tipo = elemento.get("type", "")
            if tipo == "background":
                continue
            nombre_esc = elemento.get("name", "")
            status = get_scenario_status(elemento)
            steps = [s.get("name", "") for s in elemento.get("steps", [])]
            escenarios.append({
                "nombre": nombre_esc,
                "status": status,
                "steps": steps,
            })
        features.append({
            "nombre": nombre_feature,
            "escenarios": escenarios,
        })
    return features

# ── Generador de Markdown ──────────────────────────────────────────────────────

def generar_reporte(features: list[dict]) -> str:
    lineas = []
    ahora = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    lineas.append("# REPORTE DE PRUEBAS FUNCIONALES — Sistema de Pasantías y Vinculación UCE")
    lineas.append("")
    lineas.append(f"**Generado automáticamente el:** {ahora}")
    lineas.append(f"**Entorno:** QA — `http://54.227.79.26`")
    lineas.append(f"**Framework:** Cucumber 7 + Selenium 4 + JUnit 5")
    lineas.append("")
    lineas.append("---")
    lineas.append("")

    # Índice
    lineas.append("## Índice de Casos de Prueba")
    lineas.append("")
    for i, caso in enumerate(CASOS_ESPERADOS, 1):
        lineas.append(f"{i}. [{caso}](#caso-{i})")
    lineas.append("")
    lineas.append("---")
    lineas.append("")

    # Mapa de features por nombre
    features_map = {f["nombre"]: f for f in features}

    tabla_resumen = []

    for caso_num, nombre_caso in enumerate(CASOS_ESPERADOS, 1):
        feature = features_map.get(nombre_caso)
        objetivo = OBJETIVOS.get(nombre_caso, "")

        # Clasificar escenarios por camino
        escenarios_por_camino = {"exito": None, "alterno": None, "excepcion": None}
        if feature:
            for esc in feature["escenarios"]:
                camino = clasificar_camino(esc["nombre"])
                if escenarios_por_camino[camino] is None:
                    escenarios_por_camino[camino] = esc

        # Resultado general del caso
        todos_estados = []
        for esc in (escenarios_por_camino.values() if feature else []):
            if esc:
                todos_estados.append(esc["status"])
        resultado_general = "passed" if all(s == "passed" for s in todos_estados) and todos_estados else "failed"
        if not feature:
            resultado_general = "failed"

        lineas.append(f"## Caso de prueba {caso_num}: {nombre_caso}")
        lineas.append("")
        lineas.append(f"**Objetivo:** {objetivo}")
        lineas.append("")
        lineas.append(f"**Resultado general:** {estado_emoji(resultado_general)}")
        lineas.append("")

        defecto_caso = None
        for camino_key, titulo_camino in [
            ("exito", "Camino de éxito"),
            ("alterno", "Camino alterno"),
            ("excepcion", "Excepción"),
        ]:
            lineas.append(f"### {titulo_camino}")
            lineas.append("")

            esc = escenarios_por_camino[camino_key]
            if esc is None:
                lineas.append("- ⚠️ Escenario no ejecutado o no encontrado en cucumber.json.")
                lineas.append("")
                continue

            lineas.append(f"**Escenario:** {esc['nombre']}")
            lineas.append("")
            lineas.append("**Pasos ejecutados:**")
            for step in esc["steps"]:
                lineas.append(f"- {step}")
            lineas.append("")

            status = esc["status"]
            lineas.append(f"**Resultado:** {estado_emoji(status)}")
            lineas.append("")

            # Captura
            captura = buscar_captura(esc["nombre"], "PASS" if status == "passed" else "FAIL")
            if captura:
                lineas.append(f"**Captura:** ![{esc['nombre']}]({captura})")
            else:
                lineas.append("**Captura:** _No disponible (ejecutar tests para generar capturas)_")
            lineas.append("")

            # Defecto conocido
            defecto = detectar_defecto(esc["nombre"])
            if defecto and status == "failed":
                lineas.append(f"> ⚠️ **FALLA ESPERADA** — relacionada a {defecto}")
                lineas.append("")
                defecto_caso = defecto

        lineas.append("---")
        lineas.append("")

        # Para tabla resumen
        esc_exito = escenarios_por_camino["exito"]
        esc_alterno = escenarios_por_camino["alterno"]
        esc_excepcion = escenarios_por_camino["excepcion"]
        tabla_resumen.append({
            "num": caso_num,
            "nombre": nombre_caso,
            "exito": estado_emoji(esc_exito["status"]) if esc_exito else "—",
            "alterno": estado_emoji(esc_alterno["status"]) if esc_alterno else "—",
            "excepcion": estado_emoji(esc_excepcion["status"]) if esc_excepcion else "—",
            "defecto": defecto_caso or "—",
        })

    # Tabla resumen final
    lineas.append("## Tabla Resumen de Resultados")
    lineas.append("")
    lineas.append("| # | Caso de Prueba | Camino éxito | Camino alterno | Excepción | Defecto relacionado |")
    lineas.append("|---|----------------|:------------:|:--------------:|:---------:|---------------------|")
    for fila in tabla_resumen:
        nombre_corto = fila["nombre"].replace("Flujo de ", "").replace("Gestión de ", "")
        lineas.append(
            f"| {fila['num']} | {nombre_corto} "
            f"| {fila['exito']} "
            f"| {fila['alterno']} "
            f"| {fila['excepcion']} "
            f"| {fila['defecto']} |"
        )
    lineas.append("")
    lineas.append("---")
    lineas.append("")
    lineas.append("*Reporte generado por `generate_report.py` — Sistema de Pasantías UCE*")
    lineas.append("")

    return "\n".join(lineas)

# ── Punto de entrada ──────────────────────────────────────────────────────────

if __name__ == "__main__":
    print(f"[INFO] Leyendo {CUCUMBER_JSON} ...")
    features = parsear_cucumber_json()
    print(f"[INFO] Features encontradas: {len(features)}")

    print("[INFO] Generando REPORTE-PRUEBAS.md ...")
    contenido = generar_reporte(features)

    OUTPUT_MD.write_text(contenido, encoding="utf-8")
    print(f"[OK] Reporte generado: {OUTPUT_MD.resolve()}")
    print(f"[OK] Capturas en:      {SCREENSHOTS_DIR.resolve()}")
