package com.dam.kairos.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class WeeklyAnalyzer {

    public interface AnalysisCallback {
        void onSuccess(String analysisText);
        void onError(String errorMessage);
    }

    public static void analyzeUserWeek(String prePrompt, String modelPro, AnalysisCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onError("Usuario no autenticado.");
            return;
        }

        String userId = user.getUid();

        try {
            JSONObject json = new JSONObject();
            json.put("userId", userId);
            json.put("prePrompt", prePrompt);
            json.put("model", modelPro);

            URL url = new URL(ServerConfig.ANALYSIS_URL);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream is = conn.getInputStream();
                String result = new BufferedReader(new InputStreamReader(is))
                        .lines().collect(Collectors.joining("\n"));
                callback.onSuccess(result);
            } else {
                callback.onError("Error en la función: " + responseCode);
            }

        } catch (Exception e) {
            callback.onError("Error de conexión: " + e.getMessage());
        }
    }
}
