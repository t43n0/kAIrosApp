package com.dam.kairos.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.dam.kairos.R;

public class Inicio extends AppCompatActivity {

    Button btnIniciarSesion;
    Button btnRegistrarCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistrarCuenta = findViewById(R.id.btnRegistrarCuenta);

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Inicio.this, InicioSesion.class);
                startActivity(intent);
            }
        });

        btnRegistrarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Inicio.this, Registro.class);
                startActivity(intent);
            }
        });
    }
}