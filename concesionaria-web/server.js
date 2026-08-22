const express = require('express');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 8080;

// URL pública de la API. En Render, API_URL llega como "host:puerto" o solo
// el hostname (variable fromService -> property: host), por eso si no trae
// protocolo se le agrega https://. En local sigue funcionando con el default.
function resolverApiUrl() {
    const raw = process.env.API_URL || 'http://localhost:8081';
    return raw.startsWith('http://') || raw.startsWith('https://')
        ? raw
        : `https://${raw}`;
}

// Sirve un pequeño script que expone la URL de la API al navegador.
// Se genera en cada request para que siempre refleje la variable de entorno actual.
app.get('/config.js', (req, res) => {
    res.type('application/javascript');
    res.send(`window.API_URL = ${JSON.stringify(resolverApiUrl())};`);
});

// Servir archivos estáticos (html, css, js)
app.use(express.static(__dirname));

// Ruta principal redirige a index.html
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'index.html'));
});

// Iniciar servidor de la Vista en el puerto 8080
app.listen(PORT, () => {
    console.log('\n===================================================');
    console.log(`🚀 Servidor de la Vista (Frontend) ejecutándose`);
    console.log(`🌐 Vista en el puerto 8080: http://localhost:${PORT}`);
    console.log(`📡 Consumiendo API en puerto 8081: http://localhost:8081/api`);
    console.log('===================================================\n');
});
