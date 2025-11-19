// functions/src/generateImage.js
var admin = require("firebase-admin");
var OpenAI = require("openai");

if (!admin.apps.length) {
  admin.initializeApp();
}

var db = admin.firestore();
var bucket = admin.storage().bucket();

/**
 * Handler HTTP para generar una imagen a partir de un prompt.
 * Flujo:
 *  - Recibe { userId, docId, prompt }
 *  - Llama a OpenAI (DALL·E) para generar la imagen en base64
 *  - Sube la imagen a Firebase Storage en /diary_images/{userId}/{docId}.png
 *  - Obtiene una URL de lectura
 *  - Actualiza entries/{docId}.imageUrl en Firestore
 *  - Devuelve { imageUrl, storagePath }
 */

async function handler(req, res) {
  try {
    if (req.method !== "POST") {
      res.status(405).send({ error: "Method not allowed" });
      return;
    }

    var apiKey = process.env.OPENAI_API_KEY;

    if (!apiKey) {
      console.error("[generateImage] OPENAI_API_KEY no está definida en el entorno.");
      res.status(500).json({
        error: "Falta configuración de OpenAI en el servidor.",
      });
      return;
    }

    var body = req.body || {};
    var userId = body.userId;
    var docId = body.docId;
    var prompt = body.prompt;

    if (!userId || !docId || !prompt) {
      res.status(400).json({
        error: "userId, docId y prompt son obligatorios.",
      });
      return;
    }

    console.log("[generateImage] Petición recibida", {
      userId: userId,
      docId: docId,
      promptLength: prompt ? prompt.length : 0,
    });

    var client = new OpenAI({ apiKey: apiKey });

    var imageResponse = await client.images.generate({
      model: "gpt-image-1",
      prompt: prompt,
      n: 1,
      size: "1024x1024",
      response_format: "b64_json",
    });

    if (
      !imageResponse ||
      !imageResponse.data ||
      !imageResponse.data[0] ||
      !imageResponse.data[0].b64_json
    ) {
      console.error(
        "[generateImage] Respuesta inesperada de OpenAI:",
        imageResponse
      );
      res.status(500).json({ error: "No se pudo generar la imagen." });
      return;
    }

    var b64Data = imageResponse.data[0].b64_json;
    var buffer = Buffer.from(b64Data, "base64");

    var filePath = "diary_images/" + userId + "/" + docId + ".png";
    var file = bucket.file(filePath);

    await file.save(buffer, {
      contentType: "image/png",
      resumable: false,
    });

    console.log("[generateImage] Imagen subida a Storage en", filePath);

    var signedUrls = await file.getSignedUrl({
      action: "read",
      expires: "2100-01-01",
    });

    var imageUrl = signedUrls && signedUrls[0] ? signedUrls[0] : null;

    if (!imageUrl) {
      console.error("[generateImage] No se pudo obtener signed URL.");
      res.status(500).json({
        error: "No se pudo obtener la URL de descarga de la imagen.",
      });
      return;
    }

    console.log("[generateImage] Signed URL generada:", imageUrl);

    await db.collection("entries").doc(docId).update({
      imageUrl: imageUrl,
    });

    console.log("[generateImage] Firestore actualizado para docId", docId);

    res.status(200).json({
      imageUrl: imageUrl,
      storagePath: filePath,
    });
  } catch (e) {
    if (e.response && e.response.data) {
      console.error("[generateImage] Error de OpenAI:", e.response.data);
    } else {
      console.error("Error en generateImage.handler:", e);
    }
    res.status(500).json({ error: "Error interno al generar la imagen." });
  }
}

module.exports = {
  handler: handler,
};
