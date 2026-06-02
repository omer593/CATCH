package com.example.acatch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.cloudinary.android.callback.ErrorInfo;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import android.os.Build;

public class UserProfileActivity extends AppCompatActivity {

    EditText nameText, ageText, genderText;
    TextView instagramText, facebookText, linkedinText, twitterText;
    EditText instagramEdit, facebookEdit, linkedinEdit, twitterEdit;

    ImageView profileImage;
    Button uploadImageBtn;

    Uri imageUri;

    Button editBtn, saveBtn;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // 🔐 הרשאות
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 1);
        } else {
            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // 🔗 חיבורים
        nameText = findViewById(R.id.nameText);
        ageText = findViewById(R.id.ageText);
        genderText = findViewById(R.id.genderText);

        instagramText = findViewById(R.id.instagramText);
        facebookText = findViewById(R.id.facebookText);
        linkedinText = findViewById(R.id.linkedinText);
        twitterText = findViewById(R.id.twitterText);

        instagramEdit = findViewById(R.id.instagramEdit);
        facebookEdit = findViewById(R.id.facebookEdit);
        linkedinEdit = findViewById(R.id.linkedinEdit);
        twitterEdit = findViewById(R.id.twitterEdit);

        editBtn = findViewById(R.id.editBtn);
        saveBtn = findViewById(R.id.saveBtn);

        profileImage = findViewById(R.id.profileImage);
        uploadImageBtn = findViewById(R.id.uploadImageBtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userId = getIntent().getStringExtra("userId");
        if (userId == null && auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
        }

        // 🔥 Bottom Navigation (החדש)
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }

            if (item.getItemId() == R.id.nav_profile) {
                return true;
            }

            if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }

            return false;
        });

        // 📸 העלאת תמונה
        uploadImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });

        // 🔥 Cloudinary init (מתוקן — בלי קריסות)
        try {
            MediaManager.get();
        } catch (Exception e) {
            Map config = new HashMap();
            config.put("cloud_name", "dzo0vdcwk");
            config.put("api_key", "738559658125758");
            config.put("api_secret", "HzfIy84xxSISHleEV9kC4oJVW_4");

            MediaManager.init(this, config);
        }

// 🔥 טוען נתונים
        loadUserData();

        // ✏️ Edit
        editBtn.setOnClickListener(v -> {
            nameText.setEnabled(true);

            instagramEdit.setVisibility(View.VISIBLE);
            facebookEdit.setVisibility(View.VISIBLE);
            linkedinEdit.setVisibility(View.VISIBLE);
            twitterEdit.setVisibility(View.VISIBLE);

            instagramEdit.setText(instagramText.getText().toString());
            facebookEdit.setText(facebookText.getText().toString());
            linkedinEdit.setText(linkedinText.getText().toString());
            twitterEdit.setText(twitterText.getText().toString());

            saveBtn.setVisibility(View.VISIBLE);
        });

        // 💾 Save
        saveBtn.setOnClickListener(v -> saveChanges());

        // 🔗 לינקים
        instagramText.setOnClickListener(v -> openLink(instagramText.getText().toString()));
        facebookText.setOnClickListener(v -> openLink(facebookText.getText().toString()));
        linkedinText.setOnClickListener(v -> openLink(linkedinText.getText().toString()));
        twitterText.setOnClickListener(v -> openLink(twitterText.getText().toString()));
    }

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

                        String imageUrl = doc.getString("imageUrl");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(profileImage);
                        }
                    }
                });
    }

    private void saveChanges() {

        Map<String, Object> updates = new HashMap<>();

        updates.put("name", nameText.getText().toString());
        updates.put("instagram", instagramEdit.getText().toString());
        updates.put("facebook", facebookEdit.getText().toString());
        updates.put("linkedin", linkedinEdit.getText().toString());
        updates.put("twitter", twitterEdit.getText().toString());

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {

                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();

                    instagramText.setText(instagramEdit.getText().toString());
                    facebookText.setText(facebookEdit.getText().toString());
                    linkedinText.setText(linkedinEdit.getText().toString());
                    twitterText.setText(twitterEdit.getText().toString());

                    instagramEdit.setVisibility(View.GONE);
                    facebookEdit.setVisibility(View.GONE);
                    linkedinEdit.setVisibility(View.GONE);
                    twitterEdit.setVisibility(View.GONE);

                    saveBtn.setVisibility(View.GONE);
                });
    }

    private void openLink(String url) {

        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "No link available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();

            // מראה את התמונה מיד
            profileImage.setImageURI(imageUri);

            // 🔥 העלאה ל-Cloudinary
            MediaManager.get().upload(imageUri)
                    .callback(new UploadCallback() {

                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {

                            String imageUrl = resultData.get("secure_url").toString();

                            // 🔥 שמירה ב-Firestore
                            db.collection("users")
                                    .document(userId)
                                    .update("imageUrl", imageUrl)
                                    .addOnSuccessListener(aVoid -> {

                                        // 🔥 טוען מחדש מיד
                                        Picasso.get().load(imageUrl).into(profileImage);

                                        Toast.makeText(UserProfileActivity.this,
                                                "Saved to Firestore!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(UserProfileActivity.this,
                                                "Firestore failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                            Toast.makeText(UserProfileActivity.this,
                                    "Image uploaded!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(UserProfileActivity.this,
                                    "Upload failed", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    })
                    .dispatch();
        }
    }
}