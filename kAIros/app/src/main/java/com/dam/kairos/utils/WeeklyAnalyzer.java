package com.dam.kairos.utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class WeeklyAnalyzer {

    public interface AnalysisCallback {
        void onSuccess(String analysisText);
        void onError(String errorMessage);
    }
    
    public static void analyzeUserWeek(final String prePrompt,
                                       final String modelPro,
                                       final AnalysisCallback callback) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado.");
            return;
        }

        FirebaseFunctions functions = FirebaseFunctions.getInstance("us-central1");

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("userId", user.getUid());
        data.put("prePrompt", prePrompt);
        data.put("model", modelPro);

        functions
                .getHttpsCallable("analyzeUserWeek") // <-- nombre de tu Function onCall
                .call(data)
                .addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                    @Override
                    public void onSuccess(HttpsCallableResult httpsCallableResult) {
                        Object raw = httpsCallableResult.getData();
                        String analysis = null;

                        if (raw instanceof Map) {
                            Object a = ((Map<?, ?>) raw).get("analysis");
                            if (a instanceof String) analysis = (String) a;
                        } else if (raw instanceof String) {
                            analysis = (String) raw;
                        }

                        if (analysis != null && !analysis.isEmpty()) {
                            callback.onSuccess(analysis);
                        } else {
                            callback.onError("Respuesta sin análisis disponible.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError("Error en la función: " + e.getMessage());
                    }
                });
    }
}
