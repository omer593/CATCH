package com.example.acatch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    EditText nameText, ageText, genderText, instagramText, facebookText, linkedinText, twitterText;
    Button editBtn, saveBtn;

    Button btnSettings, btnNearby, btnLogout, btnProfile;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String userId; // 🔥 חשוב

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // 🔗 חיבורים
        nameText = findViewById(R.id.nameText);
        ageText = findViewById(R.id.ageText);
        genderText = findViewById(R.id.genderText);

        instagramText = findViewById(R.id.instagramText);
        facebookText = findViewById(R.id.facebookText);
        linkedinText = findViewById(R.id.linkedinText);
        twitterText = findViewById(R.id.twitterText);

        editBtn = findViewById(R.id.editBtn);
        saveBtn = findViewById(R.id.saveBtn);

        btnSettings = findViewById(R.id.btnSettings);
        btnNearby = findViewById(R.id.btnNearby);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔥 קבלת userId מהמסך הקודם
        userId = getIntent().getStringExtra("userId");

        // אם לא הגיע → זה הפרופיל שלי
        if (userId == null) {
            userId = auth.getCurrentUser().getUid();
        }

        // 🔥 בדיקה אם זה הפרופיל שלי
        String myId = auth.getCurrentUser().getUid();
        boolean isMyProfile = userId.equals(myId);

        // ❌ אם זה לא שלי → אין עריכה
        if (!isMyProfile) {
            editBtn.setVisibility(View.GONE);
            saveBtn.setVisibility(View.GONE);
        }

        // 🔄 טעינת נתונים
        loadUserData();

        // ✏️ Edit
        editBtn.setOnClickListener(v -> {
            nameText.setEnabled(true);
            instagramText.setEnabled(true);
            facebookText.setEnabled(true);
            linkedinText.setEnabled(true);
            twitterText.setEnabled(true);
            saveBtn.setVisibility(View.VISIBLE);
        });

        // 💾 Save
        saveBtn.setOnClickListener(v -> saveChanges());

        // 🔥 Bottom Bar

        btnNearby.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show());
    }

    // 🔄 טעינת נתונים מה־Firebase
    private void loadUserData() {

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        nameText.setText(doc.getString("name"));
                        instagramText.setText(doc.getString("instagram"));
                        facebookText.setText(doc.getString("facebook"));
                        linkedinText.setText(doc.getString("linkedin"));
                        twitterText.setText(doc.getString("twitter"));

                        Long age = doc.getLong("age");
                        String gender = doc.getString("gender");

                        if (age != null)
                            ageText.setText(String.valueOf(age));

                        if (gender != null)
                            genderText.setText(gender);
                    } else {
                        Toast.makeText(this, "No profile data found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // 💾 שמירת שינויים
    private void saveChanges() {

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", nameText.getText().toString());
        updates.put("instagram", instagramText.getText().toString());
        updates.put("facebook", facebookText.getText().toString());
        updates.put("linkedin", linkedinText.getText().toString());
        updates.put("twitter", twitterText.getText().toString());

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();

                    nameText.setEnabled(false);
                    instagramText.setEnabled(false);
                    facebookText.setEnabled(false);
                    linkedinText.setEnabled(false);
                    twitterText.setEnabled(false);

                    saveBtn.setVisibility(View.GONE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}