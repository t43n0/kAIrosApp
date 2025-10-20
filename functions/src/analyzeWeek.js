import { onRequest } from "firebase-functions/v2/https";
import admin from "firebase-admin";
import OpenAI from "openai";
import dotenv from "dotenv";

dotenv.config();

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY
});

export const analyzeWeek = onRequest(async (req, res) => {
  try {
    if (req.method !== "POST") {
      return res.status(405).json({ error: "Método no permitido. Usa POST." });
    }

    const { userId, prePrompt, model } = req.body;

    if (!userId) {
      return res.status(400).json({ error: "Falta el userId en la solicitud." });
    }

    // Calcular fechas de inicio y fin de semana actual
    const now = new Date();
    const monday = new Date(now);
    monday.setDate(now.getDate() - ((now.getDay() + 6) % 7)); // Lunes
    monday.setHours(0, 0, 0, 0);
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    sunday.setHours(23, 59, 59, 999);

    // Obtener las entradas del usuario desde Firestore
    const entriesSnap = await db.collection("entries")
      .where("userId", "==", userId)
      .where("timestamp", ">=", monday)
      .where("timestamp", "<=", sunday)
      .get();

    if (entriesSnap.empty) {
      return res.status(404).json({ error: "No se encontraron entradas para esta semana." });
    }

    const entries = entriesSnap.docs.map(doc => doc.data().content || doc.data().text || "").filter(t => t);

    // Construir prompt para GPT
    const prompt = `
Eres un analista emocional. Analiza los siguientes textos del diario personal de un usuario.
Identifica las emociones predominantes, los patrones de ánimo y ofrece una breve reflexión constructiva.

Instrucciones: ${prePrompt || "Sé empático, profesional y claro."}

Entradas de la semana:
${entries.map((t, i) => `${i + 1}. ${t}`).join("\n")}
`;

    // Llamada a la API de OpenAI
    const completion = await openai.chat.completions.create({
      model: model || "gpt-4o-mini",
      messages: [
        { role: "system", content: "Eres un asistente especializado en psicología emocional y bienestar." },
        { role: "user", content: prompt }
      ],
      max_tokens: 1000,
      temperature: 0.7
    });

    const analysisText = completion.choices[0]?.message?.content || "No se pudo generar el análisis.";

    return res.status(200).json({ analysis: analysisText });

  } catch (error) {
    console.error("Error en analyzeWeek:", error);
    return res.status(500).json({ error: error.message });
  }
});