var admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

var db = admin.firestore();
var bucket = admin.storage().bucket();

/**
 * Handler principal para borrar una imagen y limpiar Firestore
 */

async function handler(req, res) {
  try {
    if (req.method !== "POST") {
      res.status(405).send("Method Not Allowed");
      return;
    }

    var entryId = req.body.entryId;

    if (!entryId) {
      res.status(400).json({ error: "entryId is required" });
      return;
    }

    await processDelete(entryId, res);
  } catch (error) {
    console.error("Error en deleteImage.handler:", error);
    if (!res.headersSent) {
      res.status(500).json({ error: "Delete image failed" });
    }
  }
}

/**
 * Lógica de borrado: lee el doc, elimina imagen de Storage y limpia imageUrl
 */

async function processDelete(entryId, res) {
  var docRef = db.collection("entries").doc(entryId);
  var snapshot = await docRef.get();

  if (!snapshot.exists) {
    res.status(404).json({ error: "Entry not found" });
    return;
  }

  var data = snapshot.data();
  var imageUrl = data.imageUrl;

  if (!imageUrl) {
    res.status(204).send("");
    return;
  }

  var filePath = extraerPathDeUrl(imageUrl);

  if (!filePath) {
    res
      .status(500)
      .json({ error: "No se pudo extraer el path de la URL de la imagen." });
    return;
  }

  var file = bucket.file(filePath);

  await file.delete();
  await docRef.update({
    imageUrl: admin.firestore.FieldValue.delete()
  });

  res.status(204).send("");
}

/**
 * Ejemplo de extracción de path desde una URL de Storage
 * ADÁPTALO a tu formato real.
 */

function extraerPathDeUrl(url) {
  return url;
}

module.exports = {
  handler: handler
};
