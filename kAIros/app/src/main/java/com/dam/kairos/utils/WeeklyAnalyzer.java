package com.dam.kairos.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.dam.kairos.data.model.Message;
import com.dam.kairos.data.model.OpenAIRequest;
import com.dam.kairos.data.model.OpenAIResponse;
import com.dam.kairos.data.network.OpenAIService;
import com.dam.kairos.data.network.RetrofitClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class WeeklyAnalyzer {

    public interface AnalysisCallback {
        void onSuccess(String analysisText);
        void onError(String errorMessage);
    }

    public static void analyzeUserWeek(String prePrompt , String modelPro, AnalysisCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onError("Usuario no autenticado.");
            return;
        }

        String userId = user.getUid();

        Calendar startCal = Calendar.getInstance();
        startCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Date startOfWeek = startCal.getTime();

        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.DAY_OF_WEEK, 6);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date endOfWeek = endCal.getTime();

        db.collection("entries")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", startOfWeek)
                .whereLessThanOrEqualTo("date", endOfWeek)
                .get()
                .addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            List<Message> messages = new ArrayList<>();

// Mensaje de sistema inicial opcional (recomendado)
                            messages.add(new Message("assistant", prePrompt));

// Añadir las entradas del usuario como mensajes
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                String content = doc.getString("content"); // Asegúrate de que el campo sea "text" o cambia el nombre si es diferente
                                if (content != null && !content.isEmpty()) {
                                    messages.add(new Message("user", content));
                                }
                            }
                            Log.d("WeeklyAnalyzer", "Número de entradas encontradas: " + messages.size());

                            obtenerApiKeyDesdeBackend(new ApiKeyCallback() {
                                @Override
                                public void onSuccess(String apiKey) {
                                    Log.d("WeeklyAnalyzer", "Clave API obtenida: " + apiKey);
                                    OpenAIRequest request = new OpenAIRequest(modelPro, messages, 4096);
                                    Gson gson = new Gson();
                                    Log.d("WeeklyAnalyzer", "Solicitud enviada a OpenAI: " + gson.toJson(request));
                                    OpenAIService openAI = RetrofitClient.getClient("https://api.openai.com/", apiKey)
                                            .create(OpenAIService.class);

                                    openAI.getChatGPTResponse(request).enqueue(new Callback<OpenAIResponse>() {
                                        @Override
                                        public void onResponse(@NonNull Call<OpenAIResponse> call, @NonNull Response<OpenAIResponse> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                Log.d("WeeklyAnalyzer", "Respuesta OpenAI recibida con éxito");
                                                String analysis = response.body().choices.get(0).message.getContent();
                                                callback.onSuccess(analysis);
                                            } else {
                                                Log.e("WeeklyAnalyzer", "Error en respuesta OpenAI: " + response.code());
                                                try {
                                                    callback.onError("Error al procesar la respuesta de OpenAI.");
                                                } catch (Exception e) {
                                                    callback.onError("Error inesperado en OpenAI.");
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(@NonNull Call<OpenAIResponse> call, @NonNull Throwable t) {
                                            Log.e("WeeklyAnalyzer", "Fallo conexión OpenAI: " + t.getMessage());
                                            callback.onError("Fallo de conexión con el servicio.");
                                        }
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    callback.onError("Error obteniendo la clave API.");
                                }
                            });

                        } else {
                            Log.e("WeeklyAnalyzer", "No hay entradas para analizar.");
                            callback.onError("No se encontraron entradas.");
                        }
                    }
                });
    }

    private interface ApiKeyCallback {
        void onSuccess(String apiKey);
        void onError(String error);
    }

    private static void obtenerApiKeyDesdeBackend(ApiKeyCallback callback) {
        // Configura Retrofit para usar HTTPS
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://10.0.2.2:3001/")  // Cambia la URL de tu backend a HTTPS
                .addConverterFactory(GsonConverterFactory.create())  // Usamos Gson para convertir la respuesta JSON
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Realizamos la solicitud HTTPS
        Call<ApiKeyResponse> call = apiService.getApiKey();

        call.enqueue(new Callback<ApiKeyResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiKeyResponse> call, @NonNull Response<ApiKeyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String apiKey = response.body().getApiKey();
                    callback.onSuccess(apiKey);
                } else {
                    Log.e("WeeklyAnalyzer", "Error al obtener la API Key: " + response.code());
                    callback.onError("Error al obtener la clave API.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiKeyResponse> call, @NonNull Throwable t) {
                Log.e("WeeklyAnalyzer", "Fallo en la solicitud de la API: " + t.getMessage());
                callback.onError("Fallo en la solicitud de la API.");
            }
        });
    }

    // Interface de Retrofit para obtener la API Key
    public interface ApiService {
        @GET("get-api-key")
        Call<ApiKeyResponse> getApiKey();
    }

    // Respuesta esperada del backend (API Key)
    public static class ApiKeyResponse {
        private final String apiKey;

        public ApiKeyResponse(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiKey() {
            return apiKey;
        }

    }
}
