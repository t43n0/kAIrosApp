import OpenAI from 'openai';
import fetch from 'node-fetch';
import fs from 'fs';
import path from 'path';
import dotenv from 'dotenv';
import { fileURLToPath } from 'url';
import { dirname } from 'path';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Inicializa OpenAI
const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY,
});

/**
 * Genera una imagen a partir de texto y la guarda en disco.
 * @param {string} textInput El prompt de texto.
 * @param {string} filename Nombre de archivo deseado (ej. 'entrada_20250325.png').
 * @returns {string} La URL pública relativa donde se sirve la imagen.
 */
const generateImageFromText = async (textInput, filename = 'image.png') => {
  try {
    // 1) Pide la generación a OpenAI
    const response = await openai.images.generate({
      model: 'dall-e-3',
      prompt: textInput,
      n: 1,
      size: '1024x1024',
    });
    const imageUrl = response.data[0].url;

    // 2) Descarga la imagen
    const imageResponse = await fetch(imageUrl);
    const buffer = await imageResponse.buffer();

    // 3) Asegura la carpeta existe: /public/images/generated
    const imageDir = path.join(__dirname, 'public', 'images', 'generated');
    fs.mkdirSync(imageDir, { recursive: true });

    // 4) Guarda el buffer en disco
    const filePath = path.join(imageDir, filename);
    fs.writeFileSync(filePath, buffer);

    console.log('Imagen generada y guardada en disco:', filePath);

    // 5) Devuelve la ruta pública (servida por Express)
    return `/images/generated/${filename}`;
  } catch (error) {
    console.error('Error al generar la imagen:', error);
    throw error;
  }
};

export { generateImageFromText };
