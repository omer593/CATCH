package com.example.acatch;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.media.MediaPlayer;

public class SettingsActivity extends AppCompatActivity {

    Button changePasswordBtn, toggleLocationBtn, deleteAccountBtn, logoutBtn;

    FirebaseAuth auth;
    FirebaseFirestore db;

    boolean locationHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (auth.getCurrentUser() != null) {

            String userId = auth.getCurrentUser().getUid();

            db.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {

                        Boolean hidden = doc.getBoolean("hidden");

                        if (hidden != null) {
                            locationHidden = hidden;
                        }

                        toggleLocationBtn.setText(
                                locationHidden ?
                                        "Show Location" :
                                        "Hide Location"
                        );
                    });
        }

        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        toggleLocationBtn = findViewById(R.id.toggleLocationBtn);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        // 🔥 Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            playClickSound();

            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }

            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
                finish();
                return true;
            }

            if (item.getItemId() == R.id.nav_settings) {
                return true;
            }

            return false;
        });

        // 🔐 שינוי סיסמה
        changePasswordBtn.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) return;

            String email = auth.getCurrentUser().getEmail();

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // 📍 הסתרת מיקום
        toggleLocationBtn.setOnClickListener(v -> {

            if (auth.getCurrentUser() == null) return;

            locationHidden = !locationHidden;

            String userId = auth.getCurrentUser().getUid();

            db.collection("users").document(userId)
                    .update("hidden", locationHidden)
                    .addOnSuccessListener(aVoid -> {

                        Toast.makeText(
                                this,
                                locationHidden ?
                                        "Location Hidden" :
                                        "Location Visible",
                                Toast.LENGTH_SHORT
                        ).show();

                        toggleLocationBtn.setText(
                                locationHidden ?
                                        "Show Location" :
                                        "Hide Location"
                        );
                    });
        });

        // ❌ מחיקת משתמש
        deleteAccountBtn.setOnClickListener(v -> {

            if (auth.getCurrentUser() == null) return;

            String userId = auth.getCurrentUser().getUid();

            db.collection("users").document(userId).delete();

            auth.getCurrentUser().delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // 🚪 Logout
        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
    private void playClickSound() {

        MediaPlayer mp =
                MediaPlayer.create(this, R.raw.bubble);

        if (mp != null) {
            mp.setOnCompletionListener(mediaPlayer -> mediaPlayer.release());
            mp.start();
        }
    }
}