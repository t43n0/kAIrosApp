package com.dam.kairos.ui.fragments;

import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dam.kairos.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class AnalisisFragment extends Fragment {

    private TextView tvAnalisis;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analisis, container, false);
        tvAnalisis = view.findViewById(R.id.tvAnalisis);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            Log.d("TAG", "Usuario no autenticado");
        } else {
            Log.d("TAG", "Usuario autenticado: " + firebaseAuth.getCurrentUser().getUid());
        }

        loadAnalysisFromFirestore();
        return view;
    }

    private void loadAnalysisFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("analyses").document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            String analysis = document.getString("analysis");
                            Log.d("TAG", "Análisis recibido: " + analysis); // Verificar qué datos estamos recibiendo

                            if (analysis != null && !analysis.isEmpty()) {
                                analysis = procesarTexto(analysis);
                                SpannableStringBuilder formattedText = (SpannableStringBuilder) Html.fromHtml(analysis, Html.FROM_HTML_MODE_LEGACY);
                                showTextWithFormatting(formattedText); // ya no devuelve nada
                                tvAnalisis.setText(formattedText);
                            } else {
                                Log.d("TAG", "El análisis está vacío.");
                            }
                        } else {
                            Log.d("TAG", "No hay análisis guardado.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "Error al obtener análisis", e);
                    }
                });
    }

    public String procesarTexto(String textoOriginal) {
        String sinLineasOcultas = textoOriginal.replaceAll("(?m)^::.*\\n?", "");
        return sinLineasOcultas.replaceAll("\\*\\*(.*?)\\*\\*", "<strong><big>$1</big></strong>");
    }

    public void showTextWithFormatting(SpannableStringBuilder spannableText) {
        String text = spannableText.toString();
        String[] lines = text.split("\n");

        int index = 0;
        for (String line : lines) {
            int start = text.indexOf(line, index);
            int end = start + line.length();
            index = end;

            // Aquí podrías cambiar el criterio para detectar títulos si usas otro estilo
            if (line.matches(".*<big>.*</big>.*")) {
                spannableText.setSpan(
                        new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        start,
                        end,
                        0
                );
            }
        }
    }

}
