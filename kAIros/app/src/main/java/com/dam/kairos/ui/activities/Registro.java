package com.dam.kairos.ui.activities;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.dam.kairos.utils.ValidationUtils;
import com.dam.kairos.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class Registro extends AppCompatActivity {

    EditText etCorreo, etContrasena, etNombreUsuario, etConfirmarContrasena;
    Button btnRegistro, btnVolver, btnFechaNacimiento;
    TextView tvFechaSeleccionada;
    RadioGroup rgSexo;
    SwitchCompat switchRecordatorios;
    FirebaseAuth mAuth;

    String fechaNacimientoSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        etConfirmarContrasena = findViewById(R.id.etConfirmarContrasena);
        etNombreUsuario = findViewById(R.id.etNombreUsuario);
        btnRegistro = findViewById(R.id.btnRegistro);
        btnVolver = findViewById(R.id.btnVolver);
        btnFechaNacimiento = findViewById(R.id.btnFechaNacimiento);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);
        rgSexo = findViewById(R.id.rgSexo);
        switchRecordatorios = findViewById(R.id.switchRecordatorios);

        // Listener para seleccionar fecha
        btnFechaNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        // Listener para volver al inicio
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registro.this, Inicio.class);
                startActivity(intent);
            }
        });

        // Listener para el registro
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etCorreo.getText().toString();
                String password = etContrasena.getText().toString();
                String passwordConfirm = etConfirmarContrasena.getText().toString();
                String username = etNombreUsuario.getText().toString();
                int selectedSexoId = rgSexo.getCheckedRadioButtonId();
                RadioButton selectedRadio = findViewById(selectedSexoId);
                String sexo = selectedRadio != null ? selectedRadio.getText().toString() : "No especificado";
                boolean recibirRecordatorios = switchRecordatorios.isChecked();

                if (!ValidationUtils.isValidEmail(email)) {
                    etCorreo.setError("Correo no válido");
                }

                if (!ValidationUtils.isValidPassword(password)) {
                    etContrasena.setError("Contraseña demasiado corta");
                }

                if (!ValidationUtils.passwordsMatch(password, passwordConfirm)) {
                    etContrasena.setError("Las contraseñas no coinciden");
                    etConfirmarContrasena.setError("Las contraseñas no coinciden");
                }

                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || fechaNacimientoSeleccionada.isEmpty()) {
                    Toast.makeText(Registro.this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Registro.this, new com.google.android.gms.tasks.OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                            @Override
                            public void onComplete(com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Registro.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(Registro.this, MainActivity.class);
                                    i.putExtra("USERID", email);
                                    startActivity(i);
                                    finish();
                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Registro.this, "Error de registro", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fechaNacimientoSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                        tvFechaSeleccionada.setText(fechaNacimientoSeleccionada);
                    }
                },
                anio, mes, dia
        );

        datePickerDialog.show();
    }
}
