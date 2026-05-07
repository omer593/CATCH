package com.example.acatch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.*;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;
    FirebaseFirestore db;
    FirebaseAuth auth;

    RecyclerView recyclerView;
    UserAdapter adapter;
    List<User> userList = new ArrayList<>();

    Button btnSettings, btnNearby, btnLogout, btnProfile;

    // 🔥 פילטרים
    EditText minAge, maxAge;
    Spinner genderFilter;

    double myLat = 0, myLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔗 כפתורים
        btnSettings = findViewById(R.id.btnSettings);
        btnNearby = findViewById(R.id.btnNearby);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);

        // 🔗 פילטרים
        minAge = findViewById(R.id.minAge);
        maxAge = findViewById(R.id.maxAge);
        genderFilter = findViewById(R.id.genderFilter);

        String[] genders = {"All", "Male", "Female"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                genders
        );
        genderFilter.setAdapter(adapterSpinner);

        // 🔥 טריגרים לפילטר
        minAge.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) applyFilters();
        });

        maxAge.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) applyFilters();
        });

        genderFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 🔥 כפתורים
        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, UserProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        // Firebase
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Recycler
        recyclerView = findViewById(R.id.usersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList);
        recyclerView.setAdapter(adapter);

        getLocation();
    }

    // 🔥 הפעלת פילטרים מחדש
    private void applyFilters() {
        userList.clear();
        getNearbyUsers(myLat, myLng);
    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {

                        myLat = location.getLatitude();
                        myLng = location.getLongitude();

                        saveLocation(myLat, myLng);
                        getNearbyUsers(myLat, myLng);

                    } else {
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveLocation(double lat, double lng) {

        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("lat", lat);
        user.put("lng", lng);
        user.put("email", auth.getCurrentUser().getEmail());

        db.collection("users").document(userId)
                .set(user, com.google.firebase.firestore.SetOptions.merge());
    }

    private void getNearbyUsers(double myLat, double myLng) {

        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {

            userList.clear();

            String minAgeText = minAge.getText().toString();
            String maxAgeText = maxAge.getText().toString();

            int min = minAgeText.isEmpty() ? 0 : Integer.parseInt(minAgeText);
            int max = maxAgeText.isEmpty() ? 100 : Integer.parseInt(maxAgeText);

            String selectedGender = genderFilter.getSelectedItem().toString();

            for (var doc : queryDocumentSnapshots.getDocuments()) {
                // 🔥 הסתרת משתמשים
                Boolean hidden = doc.getBoolean("hidden");
                if (hidden != null && hidden) continue;

                Double lat = doc.getDouble("lat");
                Double lng = doc.getDouble("lng");

                Long age = doc.getLong("age");
                String gender = doc.getString("gender");

                String email = doc.getString("email");
                String name = doc.getString("name");

                if (lat == null || lng == null || email == null) continue;

                // לא להציג את עצמי
                if (auth.getCurrentUser() != null &&
                        doc.getId().equals(auth.getCurrentUser().getUid())) {
                    continue;
                }

                // 🔥 סינון גיל
                if (age != null && (age < min || age > max)) continue;

                // 🔥 סינון מגדר
                if (!selectedGender.equals("All")) {
                    if (gender == null || !gender.equals(selectedGender)) {
                        continue;
                    }
                }

                double distance = distanceBetween(myLat, myLng, lat, lng);

                if (distance < 100) {
                    userList.add(new User(
                            doc.getId(),
                            email,
                            name,
                            doc.getString("instagram"),
                            doc.getString("facebook"),
                            doc.getString("linkedin"),
                            doc.getString("twitter"),
                            lat,
                            lng,
                            distance
                    ));
                }
            }

            adapter.notifyDataSetChanged();
        });
    }

    private double distanceBetween(double lat1, double lon1, double lat2, double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.cos(Math.toRadians(theta));

        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515;
        return dist * 1.609344;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getLocation();
        }
    }
}