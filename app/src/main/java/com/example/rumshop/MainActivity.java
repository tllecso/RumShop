package com.example.rumshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);

        mAuth = FirebaseAuth.getInstance();

        // Animáció betöltése
        final Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        btnLogin.setOnClickListener(view -> {
            // Animáció alkalmazása a gombra kattintáskor
            view.startAnimation(scaleAnimation);

            // Bejelentkezési folyamat
            loginUser();
        });

        tvRegisterLink.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Töltsd ki az összes mezőt!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Sikeres bejelentkezés: " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        // Sikeres bejelentkezés után átirányítás a HomeActivity-re
                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Az aktív Activity lezárása
                    } else {
                        Toast.makeText(MainActivity.this, "Hiba: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
