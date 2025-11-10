package com.dam.kairos.ui.fragments;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.dam.kairos.R;
import com.dam.kairos.data.model.EntryDiario;
import com.dam.kairos.data.model.Message;
import com.dam.kairos.data.model.OpenAIRequest;
import com.dam.kairos.ui.adapters.EntryDiarioAdapter;
import com.dam.kairos.utils.ImageStylePrompt;
import com.dam.kairos.utils.PromptStyles;
import com.dam.kairos.utils.ServerConfig;
import com.dam.kairos.utils.WeeklyAnalyzer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class DiarioFragment extends Fragment {

    public static final String TAG_BACKEND = "Backend";
    private SwitchCompat swtPublic, swtImage;
    private Spinner spnStyle;
    private final List<EntryDiario> entryList = new ArrayList<>();
    private ImageView pbAnalisis;
    private EditText editTextEntry;
    private Button buttonSave, buttonAnalyze, buttonClearAll, buttonClearEntriesFromLastWeek, buttonClearLastEntry;
    private RecyclerView recyclerViewEntries;
    private EntryDiarioAdapter entryAdapter;
    private String currentUserId; // Almacena el ID del usuario actual
    private FirebaseAuth auth;
    private final String modelStandar = "gpt-3.5-turbo";
    private final String modelPro = "gpt-4o";
    private static final String longPromp = "Eres un asistente que genera resúmenes semanales y recomendaciones basadas en entradas diarias de un diario. Ofrece un objetivo semanal acorde a lo anotado y ejemplos prácticos para lograrlo. Al final del análisis, para cada día selecciona una de estas 6 emociones precedidas de la fecha actual en formato dd-MM-yyyy (en inglés) según la emoción predominante e imprime cada línea iniciada con \"::\" (ej: \"::25-03-2025 ; emotion_happy;\"): emotion_happy, emotion_sad, emotion_angry, emotion_afraid, emotion_surprised, emotion_upset.";
    private static final String shortPromp = "Genera un resumen semanal y una recomendación basada en las entradas del diario. Propón un objetivo semanal con ejemplos prácticos. Al final, para cada entrada, indica la emoción predominante precedida por la fecha en formato dd-MM-yyyy (en inglés), usando este formato: ::25-03-2025 ; emotion_happy;. Emociones: emotion_happy, emotion_sad, emotion_angry, emotion_afraid, emotion_surprised, emotion_upset.";
    private static String visualPrompt = "Ilustración basada en este análisis emocional semanal: ";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_diario, container, false);

        editTextEntry = view.findViewById(R.id.editTextEntry);
        buttonSave = view.findViewById(R.id.buttonSave);
        buttonAnalyze = view.findViewById(R.id.buttonAnalyze);
        buttonClearAll = view.findViewById(R.id.buttonDeleteAll);
        buttonClearEntriesFromLastWeek = view.findViewById(R.id.buttonDeleteEntriesFromLastWeek);
        buttonClearLastEntry = view.findViewById(R.id.buttonDeleteLastEntry);
        recyclerViewEntries = view.findViewById(R.id.recyclerViewEntries);
        swtPublic = view.findViewById(R.id.switchPublic);
        swtImage = view.findViewById(R.id.switchImage);
        pbAnalisis = view.findViewById(R.id.pbAnalisis);
        spnStyle = view.findViewById(R.id.spnStyle);

        auth = FirebaseAuth.getInstance();

        currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        recyclerViewEntries.setLayoutManager(new LinearLayoutManager(getContext()));
        entryAdapter = new EntryDiarioAdapter(getContext(), entryList);
        recyclerViewEntries.setAdapter(entryAdapter);

        ArrayAdapter<ImageStylePrompt> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                PromptStyles.STYLES
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnStyle.setAdapter(adapter);

        swtImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    spnStyle.setVisibility(VISIBLE);
                } else {
                    spnStyle.setVisibility(GONE);
                }
            }
        });

        // Cargar entradas del usuario actual
        loadEntries();

        buttonClearLastEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que quieres borrar el último registro?")
                        .setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Si el usuario confirma, borrar el último registro
                                deleteLastEntry();
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Si el usuario cancela, cierra el cuadro de diálogo
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        buttonClearEntriesFromLastWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que quieres borrar toda la última semana?")
                        .setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Si el usuario confirma, borrar el último registro
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        deleteEntriesFromLastWeek();
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                loadEntries(); // Recargar entradas después de borrar
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Si el usuario cancela, cierra el cuadro de diálogo
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        buttonClearAll.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Mostrar el cuadro de diálogo de confirmación
                new AlertDialog.Builder(getContext())
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que quieres borrar todos los registros?")
                        .setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Si el usuario confirma, borrar todos los registros del usuario actual
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        deleteAllEntries();
                                        requireActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                loadEntries(); // Recargar entradas después de borrar
                                            }
                                        });
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Si el usuario cancela, cierra el cuadro de diálogo
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

        // Configurar el listener para el botón Save
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEntry();
            }
        });

        // Configurar el listener para el botón Analyze
        buttonAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WeeklyAnalyzer.analyzeUserWeek(longPromp , modelPro, new WeeklyAnalyzer.AnalysisCallback() {
                    @Override
                    public void onSuccess(String analysisText) {
                        saveAnalysisToFirestore(analysisText);
                        hidePb();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        hidePb();
                    }
                });
            }
        });
        return view;
    }

    private void deleteEntriesFromLastWeek() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Crear una referencia a Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Obtener la fecha de hace 7 días para calcular el rango de la última semana
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);  // Establecer al lunes de la semana actual
                calendar.add(Calendar.WEEK_OF_YEAR, -1);  // Retroceder una semana
                Date startOfLastWeek = calendar.getTime();

                // Establecer el final de la semana (domingo)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);  // Establecer al domingo de la semana pasada
                Date endOfLastWeek = calendar.getTime();

                // Realizar la consulta para obtener las entradas de la semana pasada
                db.collection("entries")
                        .whereEqualTo("userId", currentUserId)  // Filtrar por el ID del usuario
                        .whereGreaterThanOrEqualTo("timestamp", startOfLastWeek)  // Fecha mayor o igual al inicio de la última semana
                        .whereLessThanOrEqualTo("timestamp", endOfLastWeek)  // Fecha menor o igual al final de la última semana
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    // Si hay entradas para borrar, eliminar cada una de ellas
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String entryId = document.getId();  // Obtener el ID de la entrada

                                        // Eliminar la entrada
                                        db.collection("entries").document(entryId).delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        requireActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                deleteImageFromBackend((String) document.get("imageUrl"));
                                                                loadEntries(); // Recargar entradas
                                                                Toast.makeText(getContext(), "Entradas de la última semana eliminadas", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        requireActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getContext(), "Error al eliminar las entradas", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                } else {
                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), "No hay entradas de la última semana para eliminar", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    private void deleteAllEntries() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("entries")
                        .whereEqualTo("userId", currentUserId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String entryId = document.getId();  // Obtener el ID de la entrada

                                        // Eliminar la entrada
                                        db.collection("entries").document(entryId).delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        requireActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                deleteImageFromBackend((String) document.get("imageUrl"));
                                                                loadEntries(); // Recargar entradas
                                                                Toast.makeText(getContext(), "Todas las entradas eliminadas", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        requireActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getContext(), "Error al eliminar las entradas", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                    }
                                }
                            }
                        });
            }
        });
    }

    private void deleteLastEntry() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Crear una referencia a Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Consulta para obtener la última entrada del usuario actual
                db.collection("entries")
                        .whereEqualTo("userId", currentUserId)  // Filtrar por el ID del usuario
                        .orderBy("timestamp", Query.Direction.DESCENDING) // Ordenar por fecha descendente (última entrada)
                        .limit(1) // Obtener solo la última entrada
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    // Obtener la última entrada
                                    DocumentSnapshot lastEntryDocument = task.getResult().getDocuments().get(0);
                                    String lastEntryId = lastEntryDocument.getId();  // ID de la última entrada

                                    // Eliminar la última entrada
                                    db.collection("entries").document(lastEntryId).delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    requireActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            loadEntries();
                                                            deleteImageFromBackend((String) lastEntryDocument.get("imageUrl"));
                                                            Toast.makeText(getContext(), "Última entrada eliminada", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    requireActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getContext(), "Error al eliminar la entrada", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            });
                                } else {
                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), "No hay entradas para eliminar", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }

    private void deleteImageFromBackend(String imageUrl) {
        try {
            Uri uri = Uri.parse(imageUrl);
            String path = uri.getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1); // quitar el primer "/"
            }

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("path", path);

            String backendUrl = ServerConfig.IMAGE_URL;

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    backendUrl,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("DeleteImage", "Imagen eliminada del backend correctamente");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("DeleteImage", "Error al eliminar imagen en el backend", error);
                        }
                    }
            );

            RequestQueue queue = Volley.newRequestQueue(requireContext());
            queue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void loadEntries() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("entries")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<EntryDiario> entries = new ArrayList<>();
                        EntryDiario entry = null;

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String id = doc.getId(); // id del documento
                            String userId = doc.getString("userId");
                            String content = doc.getString("content");
                            String imageUrl = doc.getString("imageUrl") != null ? doc.getString("imageUrl") : "";
                            Timestamp timestamp = doc.getTimestamp("timestamp");
                            assert timestamp != null;
                            String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(timestamp.toDate());
                            entry = new EntryDiario(id, userId, content, imageUrl, timestamp, formattedDate);
                            entries.add(entry);
                            Log.e(TAG, "Entrada " + id + ": " + content + ";" + imageUrl + ";" + formattedDate);
                        }

                        entryAdapter.setEntries(entries);
                        entryAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al cargar entradas", e);
                        Toast.makeText(getContext(), "Error al cargar entradas", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void saveEntry() {
        final String content = editTextEntry.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, escribe algo antes de guardar.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fecha en formato dd-MM-yyyy
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        boolean isPublic = swtPublic.isChecked();
        boolean needsImage = swtImage.isChecked();

        // Construimos el ID del documento
        final String docId = currentUserId + "_" + currentDate;

        // Preparamos los datos a guardar
        Map<String, Object> entryData = new HashMap<>();

        Timestamp now = new Timestamp(new Date());

        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now.toDate());

        entryData.put("userId", currentUserId);
        entryData.put("content", content);
        entryData.put("isPublic", isPublic);
        entryData.put("needsImage", needsImage);
        entryData.put("formattedDate", formattedDate);
        entryData.put("timestamp", new Timestamp(now.toDate()));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("entries")
                .document(docId)
                .set(entryData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Entrada guardada", Toast.LENGTH_SHORT).show();
                        showPb();
                        // Si hay que generar imagen, lanzamos la petición al backend
                        if (needsImage) {
                            ImageStylePrompt style = (ImageStylePrompt) spnStyle.getSelectedItem();
                            String prompt = style.getFullPrompt(content);
                            generateImageInBackend(docId, prompt);
                            startListeningForImageUpdate(docId);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error al guardar entrada", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error al guardar entrada", e);
                        hidePb();
                    }
                });

        editTextEntry.setText("");
    }

    private void showPb() {
        pbAnalisis.setVisibility(VISIBLE);
        Glide.with(requireContext())
                .asGif()
                .load(R.drawable.loading_gif)
                .into(pbAnalisis);
        swtImage.setVisibility(GONE);
        swtPublic.setVisibility(GONE);
        spnStyle.setVisibility(GONE);
        editTextEntry.setVisibility(GONE);
        buttonClearEntriesFromLastWeek.setVisibility(GONE);
        buttonSave.setVisibility(GONE);
        buttonAnalyze.setVisibility(GONE);
        buttonClearAll.setVisibility(GONE);
        buttonClearLastEntry.setVisibility(GONE);
        recyclerViewEntries.setVisibility(GONE);
    }

    /**
     * Lanza en un hilo secundario la petición POST al backend para generar
     * la imagen a partir de texto. Cuando recibe la URL, actualiza Firestore.
     */
    private void generateImageInBackend(final String docId, final String prompt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Configurar cliente OkHttp
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .build();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        Log.e("BACKEND", "Usuario no autenticado");
                        return;
                    }

                    String userId = user.getUid();

                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("docId", docId);
                    jsonBody.put("prompt", prompt);
                    jsonBody.put("userId", userId);

                    RequestBody body = RequestBody.create(
                            jsonBody.toString(),
                            MediaType.parse("application/json; charset=utf-8")
                    );

                    String url = ServerConfig.IMAGE_URL;

                    okhttp3.Request request = new okhttp3.Request.Builder()
                            .url(url)
                            .post(body)
                            .build();

                    okhttp3.Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        JSONObject jsonResp = new JSONObject(responseBody);

                        if (jsonResp.has("imageUrl")) {
                            final String imageUrl = jsonResp.getString("imageUrl");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                FirebaseFirestore.getInstance()
                                        .collection("entries")
                                        .document(docId)
                                        .update("imageUrl", imageUrl)
                                        .addOnSuccessListener(aVoid -> Log.d("BACKEND", "URL actualizada en Firestore"))
                                        .addOnFailureListener(e -> Log.e("BACKEND", "Error guardando URL", e));
                            } else {
                                Log.e("BACKEND", "URL recibida vacía o nula");
                            }
                        } else {
                            Log.e("BACKEND", "No se encontró 'imageUrl' en la respuesta");
                        }
                    } else {
                        Log.e("BACKEND", "Error en backend: " + response.code());
                        Log.e("BACKEND", "Respuesta: " + response.body().string());
                    }

                } catch (Exception e) {
                    Log.e("BACKEND", "Excepción en generateImageInBackend", e);
                }
            }
        }).start();
    }


    /**
     * Crea un cliente OkHttp que ignora la validación SSL (solo para entornos locales con certificados autofirmados)
     */
    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) { }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) { }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            builder.connectTimeout(5, TimeUnit.SECONDS);
            builder.readTimeout(10, TimeUnit.SECONDS);
            builder.writeTimeout(10, TimeUnit.SECONDS);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void startListeningForImageUpdate(String docId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("entries").document(docId);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Escucha fallida.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String imageUrl = snapshot.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Log.d(TAG, "Imagen detectada. Mostrando...");

                        // Ocultamos animación y restauramos UI
                        pbAnalisis.setVisibility(GONE);
                        swtImage.setVisibility(VISIBLE);
                        swtPublic.setVisibility(VISIBLE);
                        spnStyle.setVisibility(VISIBLE);
                        editTextEntry.setVisibility(VISIBLE);
                        buttonClearEntriesFromLastWeek.setVisibility(VISIBLE);
                        buttonSave.setVisibility(VISIBLE);
                        buttonAnalyze.setVisibility(VISIBLE);
                        buttonClearAll.setVisibility(VISIBLE);
                        buttonClearLastEntry.setVisibility(VISIBLE);
                        recyclerViewEntries.setVisibility(VISIBLE);

                        // Recargamos las entradas para que se vea la imagen
                        loadEntries();
                    }
                }
            }
        });
    }


    /**
     * Helper para convertir InputStream a String
     */
    private String streamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }


    @NonNull
    private static HttpURLConnection getHttpURLConnection(EntryDiario entrada) throws IOException, JSONException {
        // Nueva URL de Firebase Function desplegada
        URL url = new URL(ServerConfig.IMAGE_URL);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        JSONObject json = new JSONObject();
        json.put("texto", entrada.getText());
        json.put("esPublica", true);
        json.put("generarImagen", true);
        json.put("usuarioId", entrada.getIdUser()); // opcional

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return conn;
    }

    private void hidePb() {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbAnalisis.setVisibility(GONE);
                swtImage.setVisibility(VISIBLE);
                swtPublic.setVisibility(VISIBLE);
                spnStyle.setVisibility(VISIBLE);
                editTextEntry.setVisibility(VISIBLE);
                buttonClearEntriesFromLastWeek.setVisibility(VISIBLE);
                buttonSave.setVisibility(VISIBLE);
                buttonAnalyze.setVisibility(VISIBLE);
                buttonClearAll.setVisibility(VISIBLE);
                buttonClearLastEntry.setVisibility(VISIBLE);
                recyclerViewEntries.setVisibility(VISIBLE);
            }
        });
    }


    private void saveAnalysisToFirestore(String analysis) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Crear un objeto con los datos
        Map<String, Object> analysisData = new HashMap<>();
        analysisData.put("userId", currentUserId);
        analysisData.put("analysis", analysis);

        // Guardar en Firestore en la colección "analyses"
        db.collection("analyses")
                .document(currentUserId) // Un documento por usuario
                .set(analysisData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Análisis guardado en Firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al guardar análisis", e);
                    }
                });
    }


    @NonNull
    private static OpenAIRequest getOpenAIRequest(List<EntryDiario> lastWeekEntries, String model) {

        List<Message> messages = new ArrayList<>();

        // Mensaje de tipo "system" que establece el comportamiento del asistente
        messages.add(new Message("system", longPromp));

        // Concatenar todas las entradas de la semana en un solo mensaje de tipo "user"
        StringBuilder userContent = new StringBuilder("Aquí están mis entradas de la última semana:\n");

        for (EntryDiario entry : lastWeekEntries) {
            userContent.append(entry.getTimestamp()).append(": ").append(entry.getText()).append("\n");
        }

        messages.add(new Message("user", userContent.toString().trim()));

        return new OpenAIRequest(model, messages, 4096);
    }
}