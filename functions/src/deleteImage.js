import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import admin from "firebase-admin";

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const bucket = admin.storage().bucket();

export const deleteImage = onRequest(async (req, res) => {
  logger.log("[deleteImage] 📥 Solicitud recibida");

  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    return res.status(204).send("");
  }

  if (req.method !== "POST") {
    return res.status(405).json({ error: "Método no permitido" });
  }

  const { docId, userId } = req.body;

  if (!docId || !userId) {
    return res.status(400).json({ error: "Faltan docId o userId" });
  }

  const filePath = `entries/${userId}/${docId}.png`;

  try {
    const file = bucket.file(filePath);
    const [exists] = await file.exists();

    if (exists) {
      await file.delete();
      logger.log(`[deleteImage] 🗑️ Imagen eliminada: ${filePath}`);
    } else {
      logger.warn(`[deleteImage] ⚠️ Imagen no encontrada: ${filePath}`);
    }

    const docRef = db.collection("entries").doc(docId);
    const docSnap = await docRef.get();

    if (docSnap.exists) {
      await docRef.update({
        imageUrl: admin.firestore.FieldValue.delete(),
      });
    } else {
      logger.log(`[deleteImage] 📄 Documento ${docId} ya no existe, se omite la actualización`);
    }

    return res.status(200).json({ success: true });
  } catch (error) {
    logger.error("[deleteImage] ❌ Error interno:", error);
    return res.status(500).json({
      success: false,
      error: error.message || "Error eliminando imagen",
    });
  }
});