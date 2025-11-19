var admin = require("firebase-admin");
var OpenAI = require("openai");

if (!admin.apps.length) {
  admin.initializeApp();
}

var db = admin.firestore();

/**
 * Handler HTTP para analizar la semana de un usuario.
 * Espera body: { userId, prompt, model }
 * Recibe la apiKey desde index.js (v2 + secrets) como tercer parámetro.
 */

async function handler(req, res, apiKey) {
  if (req.method !== "POST") {
    res.status(405).send("Method Not Allowed");
    return;
  }

  if (!apiKey) {
    console.error("[analyzeWeek] Falta configuración de OpenAI en el servidor.");
    res
      .status(500)
      .json({ error: "Falta configuración de OpenAI en el servidor." });
    return;
  }

  var openai = new OpenAI({ apiKey: apiKey });

  var userId = req.body.userId;
  var prompt = req.body.prompt;
  var model = req.body.model || "gpt-4o";

  if (!userId || !prompt) {
    res.status(400).json({
      error: "userId y prompt son obligatorios."
    });
    return;
  }

  console.log("[analyzeWeek] Petición recibida", {
    userId: userId,
    model: model,
    hasPrompt: !!prompt
  });

  try {
    var entriesText = await obtenerEntradasSemana(userId);
    console.log(
      "[analyzeWeek] Entradas obtenidas, longitud del texto:",
      entriesText ? entriesText.length : 0
    );

    var analysisText = await llamarOpenAI(openai, entriesText, prompt, model);
    console.log("[analyzeWeek] Análisis generado correctamente.");

    res.json({ analysis: analysisText });
  } catch (error) {
    console.error("Error en analyzeWeek:", error);
    res.status(500).json({ error: "Analysis failed" });
  }
}

/**
 * Leer entradas de Firestore y formatearlas
 * Ajusta la colección/campos a tu modelo real.
 */

async function obtenerEntradasSemana(userId) {
  var querySnapshot = await db
    .collection("entries")
    .where("userId", "==", userId)
    .orderBy("timestamp", "asc")
    .get();

  var lines = [];

  querySnapshot.forEach(function (doc) {
    var data = doc.data();
    var fecha = data.formattedDate || "";
    var contenido = data.content || "";
    lines.push(fecha + ": " + contenido);
  });

  return lines.join("\n");
}

/**
 * Llamar a OpenAI con el texto de entradas + prompt largo
 */

async function llamarOpenAI(openai, entriesText, longPrompt, model) {
  var finalPrompt =
    longPrompt + "\n\nEntradas de la semana:\n" + (entriesText || "");

  var response = await openai.chat.completions.create({
    model: model,
    messages: [
      {
        role: "system",
        content: "Eres un asistente que analiza diarios emocionales."
      },
      {
        role: "user",
        content: finalPrompt
      }
    ],
    temperature: 0.7
  });

  if (
    response &&
    response.choices &&
    response.choices.length > 0 &&
    response.choices[0].message &&
    response.choices[0].message.content
  ) {
    return response.choices[0].message.content;
  }

  throw new Error("Respuesta inválida de OpenAI");
}

module.exports = {
  handler: handler
};
