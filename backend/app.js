import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import { generateImageFromText } from './imageCreator.js';
import fs from 'fs';
import { promisify } from 'util';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const unlinkAsync = promisify(fs.unlink);

const app = express();
app.use(express.json());

// Servir imágenes públicas
app.use(
  '/images',
  express.static(path.join(__dirname, 'public', 'images'))
);

// Ruta para obtener la API Key
app.get('/get-api-key', function (req, res) {
  const apiKey = process.env.OPENAI_API_KEY;
  if (!apiKey) {
    res.status(500).json({ error: 'API key no disponible' });
    return;
  }

  res.json({ apiKey: apiKey });
});

// Ruta para generar imagen
app.post('/generate-image', function (req, res) {
  const body = req.body;

  if (!body.docId || !body.prompt) {
    res.status(400).json({ error: 'Faltan docId o prompt' });
    return;
  }

  const timestamp = Date.now();
  const safeFilename = body.docId + '_' + timestamp + '.png';

  generateImageFromText(body.prompt, safeFilename)
    .then(function (publicUrl) {
      res.json({ imageUrl: publicUrl });
    })
    .catch(function (err) {
      console.error('Error en /generate-image:', err);
      res.status(500).json({ error: 'Error generando la imagen' });
    });
});

// Ruta para eliminar una imagen del backend
app.post('/delete-image', function (req, res) {
  const body = req.body;
  const relativePath = body.path;

  if (!relativePath) {
    res.status(400).json({ error: 'Falta el parámetro path' });
    return;
  }

  const absolutePath = path.join(__dirname, 'public', relativePath);

  fs.exists(absolutePath, function (exists) {
    if (!exists) {
      res.status(404).json({ error: 'Archivo no encontrado' });
      return;
    }

    unlinkAsync(absolutePath)
      .then(function () {
        res.json({ message: 'Imagen eliminada correctamente' });
      })
      .catch(function (err) {
        console.error('Error al eliminar imagen:', err);
        res.status(500).json({ error: 'Error eliminando la imagen' });
      });
  });
});

export default app;
