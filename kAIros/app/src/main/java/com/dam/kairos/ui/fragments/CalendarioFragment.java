package com.dam.kairos.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.dam.kairos.R;
import com.github.prolificinteractive.materialcalendarview.CalendarDay;
import com.github.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.github.prolificinteractive.materialcalendarview.DayViewFacade;
import com.github.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;

public class CalendarioFragment extends Fragment {

    private static final String TAG = "CalendarioFragment";

    private MaterialCalendarView calendarView;

    private String currentUserId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendario, container, false);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Log.e(TAG, "User is not authenticated.");
            // Handle user not being logged in. For example, navigate to login screen.
            return view;
        }

        AndroidThreeTen.init(requireContext());

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        calendarView = view.findViewById(R.id.calendarView);
        loadEmotionsFromFirestore();

        return view;
    }

    private void loadEmotionsFromFirestore() {

        if (currentUserId == null) {
            return; // Don't proceed if user is not logged in
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("analyses").document(currentUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String analysis = document.getString("analysis");

                        if (analysis == null || analysis.trim().isEmpty()) {
                            Log.e(TAG, "⚠️ No se encontró análisis en Firestore.");
                            return;
                        }

                        Map<String, String> emotionsByDate = extractEmotionsFromAnalysis(analysis);
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateCalendar(emotionsByDate);
                            }
                        });
                    } else {
                        Log.e(TAG, "⚠️ Documento no encontrado en Firestore.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener análisis", e));
    }

    private void updateCalendar(final Map<String, String> emotionsByDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (Map.Entry<String, String> entry : emotionsByDate.entrySet()) {
            String fechaString = entry.getKey();  // Clave (fecha)
            String emotionName = entry.getValue();  // Valor (nombre del emoticono)

            Log.d(TAG, "📅 Procesando: " + fechaString + " -> " + emotionName);

            try {
                // 🔁 Usamos ThreeTenBP en lugar de java.time
                LocalDate localDate = LocalDate.parse(fechaString, formatter);
                CalendarDay calendarDay = CalendarDay.from(
                        localDate.getYear(),
                        localDate.getMonthValue(),
                        localDate.getDayOfMonth()
                );

                // Obtener el Drawable de la emoción
                Drawable emotionDrawable = getEmotionDrawable(emotionName);
                if (emotionDrawable != null) {
                    calendarView.addDecorator(new EmotionDecorator(calendarDay, emotionDrawable));
                } else {
                    Log.e(TAG, "⚠️ No se encontró Drawable para: " + emotionName);
                }
            } catch (Exception e) {
                Log.e(TAG, "⚠️ Error al parsear la fecha: " + fechaString, e);
            }
        }
    }


    private Drawable getEmotionDrawable(String emotionName) {
        if (getContext() == null) {
            Log.e(TAG, "Fragment not attached to a context.");
            return null;
        }
        int resourceId = getResources().getIdentifier(
                emotionName, "drawable", getContext().getPackageName()
        );


        if (resourceId == 0) {
            Log.w(TAG, "Resource not found for drawable: " + emotionName);
            // Returning a default/fallback drawable might be a good idea
            return null;
        }
        return ContextCompat.getDrawable(getContext(), resourceId);

    }

    private Map<String, String> extractEmotionsFromAnalysis(String analysis) {
        Map<String, String> emotionsByDate = new HashMap<>();
        String[] lines = analysis.split("\n");

        for (String line : lines) {
            if (line.startsWith("::")) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String date = parts[0].replace("::", "").trim();
                    String emotion = parts[1].trim();
                    emotionsByDate.put(date, emotion);
                }
            }
        }
        return emotionsByDate;
    }

    private static class EmotionDecorator implements DayViewDecorator {
        private final CalendarDay date;
        private final Drawable drawable;

        EmotionDecorator(CalendarDay date, Drawable drawable) {
            this.date = date;
            this.drawable = drawable;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            Log.d(TAG, "🔍 Comparando: " + day + " con " + date);
            return day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            Log.d(TAG, "✅ Aplicando decoración en: " + date);
            view.setBackgroundDrawable(drawable);
        }
    }
}
