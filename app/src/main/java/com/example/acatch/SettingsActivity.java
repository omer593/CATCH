package com.example.acatch;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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

        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        toggleLocationBtn = findViewById(R.id.toggleLocationBtn);
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        // 🔐 שינוי סיסמה
        changePasswordBtn.setOnClickListener(v -> {
            String email = auth.getCurrentUser().getEmail();

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show());
        });

        // 📍 הסתרת מיקום
        toggleLocationBtn.setOnClickListener(v -> {

            locationHidden = !locationHidden;

            String userId = auth.getCurrentUser().getUid();

            db.collection("users").document(userId)
                    .update("hidden", locationHidden);

            toggleLocationBtn.setText(locationHidden ? "Show Location" : "Hide Location");
        });

        // ❌ מחיקת משתמש
        deleteAccountBtn.setOnClickListener(v -> {

            String userId = auth.getCurrentUser().getUid();

            db.collection("users").document(userId).delete();

            auth.getCurrentUser().delete();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // 🚪 Logout
        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}