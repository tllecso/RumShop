package com.example.rumshop;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class CartActivity extends AppCompatActivity {

    private LinearLayout cartItemsContainer;
    private TextView tvTotalPrice;
    private Button btnCheckout;

    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        // Toolbar beállítása
        Toolbar toolbar = findViewById(R.id.toolbarCart);
        setSupportActionBar(toolbar);

        // Értesítési csatorna létrehozása (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "rumshop_channel",
                    "Rendelés értesítések",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Értesítési engedély kérése (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        // UI elemek
        cartItemsContainer = findViewById(R.id.cartItemsContainer);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);

        // Firebase
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            // Komplex Firestore lekérdezés: orderBy + limit
            db.collection("carts")
                    .document(userId)
                    .collection("items")
                    .orderBy("addedAt", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int total = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String name = doc.getString("name");
                            Long price = doc.getLong("price");

                            if (name != null && price != null) {
                                TextView tv = new TextView(this);
                                tv.setText(name + " - " + price + " Ft");
                                tv.setTextSize(18f);
                                tv.setPadding(8, 16, 8, 16);
                                cartItemsContainer.addView(tv);
                                total += price;
                            }
                        }

                        tvTotalPrice.setText("Végösszeg: " + total + " Ft");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Hiba a kosár lekérdezésekor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        // Megrendelés gomb
        btnCheckout.setOnClickListener(v -> {
            if (user != null) {
                String userId = user.getUid();

                db.collection("carts")
                        .document(userId)
                        .collection("items")
                        .whereEqualTo("name", "Zacapa XO")
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            db.collection("carts")
                                    .document(userId)
                                    .collection("items")
                                    .get()
                                    .addOnSuccessListener(allItems -> {
                                        for (DocumentSnapshot doc : allItems) {
                                            doc.getReference().delete();
                                        }

                                        // ÉRTESÍTÉS
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "rumshop_channel")
                                                .setSmallIcon(R.drawable.ic_cart)
                                                .setContentTitle("Rendelés sikeres")
                                                .setContentText("Köszönjük a vásárlást!")
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                                        notificationManager.notify(1001, builder.build());

                                        Toast.makeText(this, "Rendelés elküldve, kosár törölve!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_home) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return true;
        } else if (id == R.id.menu_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Értesítési engedély válaszának kezelése
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Értesítések engedélyezve.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Értesítések letiltva.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
