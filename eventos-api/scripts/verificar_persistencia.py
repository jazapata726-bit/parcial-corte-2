"""Comprueba que reiniciar el contenedor conserva las inscripciones en PostgreSQL."""
import json
import os
import subprocess
import time
import urllib.request

base_url = os.environ.get("API_BASE_URL", "http://localhost:8080").rstrip("/")


def solicitar(metodo, ruta, datos=None):
    cuerpo = json.dumps(datos).encode() if datos is not None else None
    peticion = urllib.request.Request(base_url + ruta, data=cuerpo, method=metodo,
        headers={"Content-Type": "application/json"} if cuerpo else {})
    with urllib.request.urlopen(peticion, timeout=10) as respuesta:
        contenido = respuesta.read()
        return json.loads(contenido) if contenido else None


def esperar_api():
    for _ in range(40):
        try:
            if solicitar("GET", "/actuator/health")["status"] == "UP":
                return
        except Exception:
            time.sleep(1)
    raise RuntimeError("La API no volvió a estar disponible")


def main():
    evento = solicitar("POST", "/api/eventos", {"nombre": "Persistencia PostgreSQL",
        "fecha": "2027-01-15T09:00:00", "lugar": "Laboratorio"})
    asistente = solicitar("POST", "/api/asistentes", {"nombre": "Asistente de prueba",
        "correo": "persistencia." + str(time.time_ns()) + "@example.com"})
    e, a = evento["id"], asistente["id"]
    solicitar("POST", f"/api/eventos/{e}/asistentes/{a}")
    subprocess.run(["docker", "compose", "restart", "api"], check=True)
    esperar_api()
    if solicitar("GET", f"/api/eventos/{e}")["asistentesIds"] != [a]:
        raise RuntimeError("El evento perdió su inscripción")
    if solicitar("GET", f"/api/asistentes/{a}")["eventosIds"] != [e]:
        raise RuntimeError("El asistente perdió su inscripción")
    solicitar("DELETE", f"/api/eventos/{e}")
    solicitar("DELETE", f"/api/asistentes/{a}")
    print("CORRECTO: evento, asistente e inscripción persisten al reiniciar el contenedor")


if __name__ == "__main__":
    main()
