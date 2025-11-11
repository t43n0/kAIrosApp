import { onRequest } from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
import admin from "firebase-admin";

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const bucket = admin.storage().bucket();

/**
 * Procesa la eliminación de la imagen y limpieza del campo en Firestore.
 * @param {Object} req
 * @param {Object} res
 */
async function processDeleteImage(req, res) {
  try {
    if (req.method !== "POST") {
      res.status(405).json({ error: "Método no permitido" });
      return;
    }

    var body = req.body || {};
    var docId = body.docId;
    var userId = body.userId;

    if (!docId || !userId) {
      res.status(400).json({ error: "Faltan docId o userId" });
      return;
    }

    var filePath = "entries/" + userId + "/" + docId + ".png";
    var file = bucket.file(filePath);

    var existsArr = await file.exists();
    var exists = existsArr && existsArr[0] === true;

    if (exists) {
      await file.delete();
      logger.log("[deleteImage] Imagen eliminada: " + filePath);
    } else {
      logger.warn("[deleteImage] Imagen no encontrada: " + filePath);
    }

    var docRef = db.collection("entries").doc(docId);
    var docSnap = await docRef.get();

    if (docSnap && docSnap.exists) {
      await docRef.update({
        imageUrl: admin.firestore.FieldValue.delete()
      });
    } else {
      logger.log("[deleteImage] Documento " + docId + " ya no existe, se omite la actualización");
    }

    res.status(200).json({ success: true });
  } catch (error) {
    logger.error("[deleteImage] Error interno: " + (error && error.message ? error.message : String(error)));
    res.status(500).json({
      success: false,
      error: (error && error.message) ? error.message : "Error eliminando imagen"
    });
  }
}

/**
 * Controlador principal HTTP (sin lambdas)
 * @param {Object} req
 * @param {Object} res
 */
function handleDeleteImage(req, res) {
  logger.log("[deleteImage] Solicitud recibida");

  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
    }

  processDeleteImage(req, res);
}

// Export de la función (tradicional) — añade region si quieres forzar europe-west1
export const deleteImage = onRequest(
  { region: "europe-west1" },
  function (req, res) {
    handleDeleteImage(req, res);
  }
);
