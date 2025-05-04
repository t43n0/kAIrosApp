import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import { generateImageFromText } from './imageCreator.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
app.use(express.json());

// Servir imágenes públicas
app.use(
  '/images',
  express.static(path.join(__dirname, 'public', 'images'))
);

app.get('/get-api-key', (req, res) => {
  const apiKey = process.env.OPENAI_API_KEY; 
  if (!apiKey) {
    return res.status(500).json({ error: 'API key no disponible' });
  }

  res.json({ apiKey });
});

// Ruta para generar imagen
app.post('/generate-image', async (req, res) => {
  const { docId, prompt } = req.body;
  if (!docId || !prompt) {
    return res.status(400).json({ error: 'Faltan docId o prompt' });
  }

  try {
    const timestamp = Date.now();
    const safeFilename = `${docId}_${timestamp}.png`;
    const publicUrl = await generateImageFromText(prompt, safeFilename);
    res.json({ imageUrl: publicUrl });
  } catch (err) {
    console.error('Error en /generate-image:', err);
    res.status(500).json({ error: 'Error generando la imagen' });
  }
});

export default app;
