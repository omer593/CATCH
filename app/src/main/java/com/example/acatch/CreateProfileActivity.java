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

        // 🔗 חיבורים
        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        gender = findViewById(R.id.gender);

        instagram = findViewById(R.id.instagram);
        facebook = findViewById(R.id.facebook);
        linkedin = findViewById(R.id.linkedin);
        twitter = findViewById(R.id.twitter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 🔥 Spinner מגדר
        String[] genders = {"Male", "Female"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                genders
        );

        gender.setAdapter(adapter);

        // 🔥 בדיקת גיל
        age.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) validateAge();
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

        // ❗ בדיקות
        if (nameText.isEmpty()) {
            name.setError("Name required");
            return;
        }

        if (ageText.isEmpty()) {
            age.setError("Age required");
            return;
        }

        int ageNum;
        try {
            ageNum = Integer.parseInt(ageText);
        } catch (Exception e) {
            age.setError("Invalid age");
            return;
        }

        if (ageNum < 18 || ageNum > 99) {
            age.setError("Age must be between 18-99");
            return;
        }

        if (!isValidUrl(insta) || !isValidUrl(fb) || !isValidUrl(ln) || !isValidUrl(tw)) {
            Toast.makeText(this, "Invalid social link", Toast.LENGTH_SHORT).show();
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

        // 🔥 חשוב — שדה תמונה (ריק בהתחלה)
        data.put("imageUrl", "");

        db.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(CreateProfileActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void validateAge() {
        String ageText = age.getText().toString();

        if (!ageText.isEmpty()) {
            try {
                int a = Integer.parseInt(ageText);
                if (a < 18 || a > 99) {
                    age.setError("Age must be between 18-99");
                }
            } catch (Exception e) {
                age.setError("Invalid age");
            }
        }
    }

    private boolean isValidUrl(String url) {

        if (url.isEmpty()) return true;

        return url.startsWith("https://www.instagram.com") ||
                url.startsWith("https://www.facebook.com") ||
                url.startsWith("https://www.linkedin.com") ||
                url.startsWith("https://twitter.com") ||
                url.startsWith("https://www.twitter.com");
    }
}