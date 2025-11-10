import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as logger from "firebase-functions/logger";
import admin from "firebase-admin";
import OpenAI from "openai";

// 1️⃣ Definimos el secreto seguro almacenado en Firebase
const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");

// 2️⃣ Inicializamos Firebase Admin solo una vez
if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const bucket = admin.storage().bucket();

/**
 * Procesa la solicitud de generación de imagen.
 * @param {Object} req - Petición HTTP
 * @param {Object} res - Respuesta HTTP
 */
async function processRequest(req, res) {
  try {
    const prompt = req.body.prompt;
    const docId = req.body.docId;
    const userId = req.body.userId;

    if (!prompt || !docId || !userId) {
      res.status(400).send({ error: "Faltan parámetros requeridos" });
      return;
    }

    // 3️⃣ Inicializamos OpenAI con la clave segura
    const openai = new OpenAI({
      apiKey: OPENAI_API_KEY.value()
    });

    let result = null;

    // 4️⃣ Intento con modelo principal
    try {
      result = await openai.images.generate({
        model: "gpt-image-1",
        prompt: prompt,
        size: "1024x1024",
        response_format: "b64_json"
      });
    } catch (e) {
      logger.warn("[generateImage] Fallback a dall-e-3: " + e.message);
      result = await openai.images.generate({
        model: "dall-e-3",
        prompt: prompt,
        size: "1024x1024",
        response_format: "b64_json"
      });
    }

    if (!result || !result.data || !result.data[0] || !result.data[0].b64_json) {
      res.status(500).send({ error: "Respuesta inválida de OpenAI" });
      return;
    }

    // 5️⃣ Guardamos la imagen en Firebase Storage
    const imageBase64 = result.data[0].b64_json;
    const imageBuffer = Buffer.from(imageBase64, "base64");
    const filename = "entries/" + userId + "/" + docId + ".png";
    const file = bucket.file(filename);

    await file.save(imageBuffer, {
      metadata: { contentType: "image/png" },
      resumable: false
    });

    // 6️⃣ Generamos una URL firmada de acceso
    const urls = await file.getSignedUrl({
      action: "read",
      expires: Date.now() + 365 * 24 * 60 * 60 * 1000 // 1 año
    });

    const url = urls[0];

    // 7️⃣ Guardamos la URL en Firestore
    await db.collection("entries").doc(docId).update({
      imageUrl: url,
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    res.status(200).json({ success: true, imageUrl: url });
  } catch (error) {
    logger.error("[generateImage] ❌ Error interno: " + error.message);
    res.status(500).send({
      success: false,
      error: error.message || "Error interno en generateImage"
    });
  }
}

/**
 * Controlador principal de la función HTTPS
 * @param {Object} req
 * @param {Object} res
 */
function handleGenerateImage(req, res) {
  logger.log("[generateImage] 📥 Solicitud recibida");

  // Permitir CORS
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }

  if (req.method !== "POST") {
    res.status(405).send({ error: "Método no permitido" });
    return;
  }

  processRequest(req, res);
}

// 8️⃣ Exportamos la función HTTPS (sin lambdas)
export const generateImage = onRequest(
  { secrets: [OPENAI_API_KEY] },
  function (req, res) {
    handleGenerateImage(req, res);
  }
);
