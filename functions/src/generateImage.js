import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import admin from "firebase-admin";
import OpenAI from "openai";
import dotenv from "dotenv";

dotenv.config();

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const bucket = admin.storage().bucket();
const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY || process.env.FIREBASE_CONFIG?.openai?.key
});

function handleGenerateImage(req, res) {
  logger.log("[generateImage] 📥 Solicitud recibida");

  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.status(204).send("");
  }
  if (req.method !== "POST") {
    return res.status(405).send({ error: "Método no permitido" });
  }

  processRequest(req, res);
}

async function processRequest(req, res) {
  try {
    const { prompt, docId, userId } = req.body;
    if (!prompt || !docId || !userId) {
      return res.status(400).send({ error: "Faltan parámetros requeridos" });
    }

    let result;
    try {
      // Intento 1: gpt-image-1
      result = await openai.images.generate({
        model: "gpt-image-1",
        prompt: prompt,
        size: "1024x1024",
        response_format: "b64_json"
      });
    } catch (e) {
      logger.warn("[generateImage] Fallback a dall-e-3:", e.message);
      result = await openai.images.generate({
        model: "dall-e-3",
        prompt: prompt,
        size: "1024x1024",
        response_format: "b64_json"
      });
    }

    if (!result?.data?.[0]?.b64_json) {
      return res.status(500).send({ error: "Respuesta inválida de OpenAI" });
    }

    const imageBase64 = result.data[0].b64_json;
    const imageBuffer = Buffer.from(imageBase64, "base64");
    const filename = `entries/${userId}/${docId}.png`; // <- ruta sugerida
    const file = bucket.file(filename);

    await file.save(imageBuffer, {
      metadata: { contentType: "image/png" },
      resumable: false
      // public: false  // por defecto privado; usaremos URL firmada
    });

    const [url] = await file.getSignedUrl({
      action: "read",
      expires: Date.now() + 365 * 24 * 60 * 60 * 1000 // 1 año
    });

    await db.collection("entries").doc(docId).update({
      imageUrl: url,
      generatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    return res.status(200).json({ success: true, imageUrl: url });

  } catch (error) {
    logger.error("[generateImage] ❌ Error interno:", error);
    return res.status(500).send({
      success: false,
      error: error.message || "Error interno en generateImage"
    });
  }
}

export const generateImage = onRequest(handleGenerateImage);
