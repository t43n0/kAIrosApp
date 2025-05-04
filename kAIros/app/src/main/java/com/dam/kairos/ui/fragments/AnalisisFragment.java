package com.dam.kairos.ui.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
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

public class AnalisisFragment extends Fragment {

    private TextView tvAnalisis, tvTitulo;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analisis, container, false);
        tvTitulo = view.findViewById(R.id.titulo_analisis);
        tvAnalisis = view.findViewById(R.id.tvAnalisis);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            Log.d(TAG, "Usuario no autenticado");
        } else {
            Log.d(TAG, "Usuario autenticado: " + firebaseAuth.getCurrentUser().getUid());
        }

        loadAnalysisFromFirestore();
        return view;
    }

    private void loadAnalysisFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("analyses").document(firebaseAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot document) {
                        if (document.exists()) {
                            String analysis = document.getString("analysis");
                            Log.d(TAG, "Análisis recibido: " + analysis); // Verificar qué datos estamos recibiendo

                            if (analysis != null && !analysis.isEmpty()) {
                                tvAnalisis.setText(analysis);
                            } else {
                                Log.d(TAG, "El análisis está vacío.");
                            }
                        } else {
                            Log.d(TAG, "No hay análisis guardado.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al obtener análisis", e);
                    }
                });
    }

}