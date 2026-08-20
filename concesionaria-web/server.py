import http.server
import socketserver
import os

PORT = 8080
DIRECTORY = os.path.dirname(os.path.abspath(__file__))

class CustomHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

print("\n===================================================")
print("🚀 Servidor Web de la Vista (Frontend) en línea")
print(f"🌐 Vista corriendo en el puerto 8080: http://localhost:{PORT}")
print("📡 Consumiendo API REST de Spring Boot en puerto 8081: http://localhost:8081/api")
print("===================================================\n")

with socketserver.TCPServer(("", PORT), CustomHandler) as httpd:
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServidor web detenido.")
