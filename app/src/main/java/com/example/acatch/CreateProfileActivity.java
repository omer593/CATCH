package com.example.acatch;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class CreateProfileActivity extends AppCompatActivity {

    EditText name, age, instagram, facebook, linkedin, twitter;
    Spinner gender;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        gender = findViewById(R.id.gender);

        instagram = findViewById(R.id.instagram);
        facebook = findViewById(R.id.facebook);
        linkedin = findViewById(R.id.linkedin);
        twitter = findViewById(R.id.twitter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔥 מגדר - Spinner
        String[] genders = {"Male", "Female"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                genders
        );

        gender.setAdapter(adapter);
        age.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String ageText = age.getText().toString();

                if (!ageText.isEmpty()) {
                    try {
                        int a = Integer.parseInt(ageText);
                        if (a < 18 || a > 99) {
                            age.setError("Invalid input");
                        }
                    } catch (Exception e) {
                        age.setError("Invalid input");
                    }
                }
            }
        });
    }

    public void saveProfile(View view) {

        if (auth.getCurrentUser() == null) return;

        String nameText = name.getText().toString().trim();
        String ageText = age.getText().toString().trim();
        String genderText = gender.getSelectedItem().toString();

        String insta = instagram.getText().toString().trim();
        String fb = facebook.getText().toString().trim();
        String ln = linkedin.getText().toString().trim();
        String tw = twitter.getText().toString().trim();

        // ❗ בדיקות שדות חובה
        if (nameText.isEmpty() || ageText.isEmpty()) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            return;
        }

        int ageNum;
        try {
            ageNum = Integer.parseInt(ageText);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid age", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 הגבלת גיל
        if (ageNum < 18 || ageNum > 99) {
            age.setError("Age must be between 18-99");
            return;
        }

        // 🔥 בדיקת קישורים
        if (!isValidUrl(insta) || !isValidUrl(fb) || !isValidUrl(ln) || !isValidUrl(tw)) {
            Toast.makeText(this, "Invalid link", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> data = new HashMap<>();
        data.put("name", nameText);
        data.put("age", ageNum);
        data.put("gender", genderText);
        data.put("instagram", insta);
        data.put("facebook", fb);
        data.put("linkedin", ln);
        data.put("twitter", tw);

        db.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_LONG).show();

                    // 🔥 מעבר למסך הראשי
                    startActivity(new Intent(CreateProfileActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // 🔥 בדיקת URL
    private boolean isValidUrl(String url) {

        if (url.isEmpty()) return true;

        return url.startsWith("https://www.instagram.com") ||
                url.startsWith("https://www.facebook.com") ||
                url.startsWith("https://www.linkedin.com") ||
                url.startsWith("https://twitter.com") ||
                url.startsWith("https://www.twitter.com");
    }
}