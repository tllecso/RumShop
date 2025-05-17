package com.example.rumshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private Animation bounceAnim;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Toolbar beállítása
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Firebase példányok
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Animáció betöltése
        bounceAnim = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        // Gomb események kezelése
        for (int i = 1; i <= 6; i++) {
            int buttonId = getResources().getIdentifier("btnAddToCart" + i, "id", getPackageName());
            Button addButton = findViewById(buttonId);
            int finalI = i;

            if (addButton != null) {
                addButton.setOnClickListener(v -> {
                    v.startAnimation(bounceAnim);

                    String itemName;
                    int itemPrice = 0;

                    switch (finalI) {
                        case 1: itemName = "Zacapa XO"; itemPrice = 28990; break;
                        case 2: itemName = "Bacardi Caribbean Spiced"; itemPrice = 16990; break;
                        case 3: itemName = "Havanna Especial"; itemPrice = 17990; break;
                        case 4: itemName = "Diplomatico Reserva"; itemPrice = 28990; break;
                        case 5: itemName = "Bumbu"; itemPrice = 20990; break;
                        case 6: itemName = "Don Papa"; itemPrice = 29900; break;
                        default: itemName = "Ismeretlen rum";
                    }

                    if (user != null) {
                        String userId = user.getUid();
                        Map<String, Object> cartItem = new HashMap<>();
                        cartItem.put("name", itemName);
                        cartItem.put("price", itemPrice);
                        cartItem.put("addedAt", FieldValue.serverTimestamp());

                        db.collection("carts")
                                .document(userId)
                                .collection("items")
                                .add(cartItem)
                                .addOnSuccessListener(docRef -> {
                                    Toast.makeText(HomeActivity.this, itemName + " hozzáadva a kosárhoz!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(HomeActivity.this, "Hiba történt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(HomeActivity.this, "Bejelentkezés szükséges a kosár használatához!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Üdv újra itt!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_cart) {
            startActivity(new Intent(HomeActivity.this, CartActivity.class));
            return true;
        } else if (id == R.id.menu_profile) {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}