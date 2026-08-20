const express = require('express');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 8080;

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
