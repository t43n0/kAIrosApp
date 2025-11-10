import { onRequest } from "firebase-functions/v2/https";
import { defineSecret } from "firebase-functions/params";
import * as logger from "firebase-functions/logger";
import admin from "firebase-admin";
import OpenAI from "openai";

const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();

/**
 * Procesa la solicitud de análisis semanal de entradas.
 * @param {Object} req - Petición HTTP
 * @param {Object} res - Respuesta HTTP
 */
async function processAnalyzeWeek(req, res) {
  try {
    if (req.method !== "POST") {
      res.status(405).json({ error: "Método no permitido. Usa POST." });
      return;
    }

    const userId = req.body.userId;
    const prePrompt = req.body.prePrompt;
    const model = req.body.model;

    if (!userId) {
      res.status(400).json({ error: "Falta el userId en la solicitud." });
      return;
    }

    // Calcular fechas de inicio y fin de la semana actual
    const now = new Date();
    const monday = new Date(now);
    monday.setDate(now.getDate() - ((now.getDay() + 6) % 7));
    monday.setHours(0, 0, 0, 0);
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    sunday.setHours(23, 59, 59, 999);

    // Obtener entradas del usuario de Firestore
    const entriesSnap = await db.collection("entries")
      .where("userId", "==", userId)
      .where("timestamp", ">=", monday)
      .where("timestamp", "<=", sunday)
      .get();

    if (entriesSnap.empty) {
      res.status(404).json({ error: "No se encontraron entradas para esta semana." });
      return;
    }

    const entries = [];
    entriesSnap.forEach(function (doc) {
      const data = doc.data();
      const text = data.content || data.text || "";
      if (text && text.trim() !== "") {
        entries.push(text);
      }
    });

    if (entries.length === 0) {
      res.status(400).json({ error: "No hay texto válido para analizar." });
      return;
    }

    const prompt = entries.map(function (t, i) { return (i + 1) + ". " + t; }).join("\n");

    const openai = new OpenAI({
      apiKey: OPENAI_API_KEY.value()
    });

    const completion = await openai.chat.completions.create({
      model: model || "gpt-4o-mini",
      messages: [
        { role: "system", content: "Eres un asistente especializado en psicología emocional y bienestar." },
        { role: "user", content: prompt }
      ],
      max_tokens: 1000,
      temperature: 0.7
    });

    const analysisText =
      (completion.choices &&
        completion.choices[0] &&
        completion.choices[0].message &&
        completion.choices[0].message.content) ||
      "No se pudo generar el análisis.";

    res.status(200).json({ analysis: analysisText });
  } catch (error) {
    logger.error("[analyzeWeek] ❌ Error interno: " + error.message);
    res.status(500).json({ error: error.message });
  }
}



/**
 * Maneja la solicitud HTTP para el análisis semanal.
 * @param {Object} req - Petición HTTP
 * @param {Object} res - Respuesta HTTP
 */
function handleAnalyzeWeek(req, res) {
  logger.log("[analyzeWeek] 📥 Solicitud recibida");
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }

  processAnalyzeWeek(req, res);
}

export const analyzeWeek = onRequest(
  { secrets: [OPENAI_API_KEY] },
  function (req, res) {
    handleAnalyzeWeek(req, res);
  }
);
