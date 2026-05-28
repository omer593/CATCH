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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;
    FirebaseFirestore db;
    FirebaseAuth auth;

    RecyclerView recyclerView;
    UserAdapter adapter;
    List<User> userList = new ArrayList<>();

    //Button btnSettings, btnNearby, btnLogout, btnProfile;

    EditText minAge, maxAge;
    Spinner genderFilter;

    double myLat = 0, myLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //btnSettings = findViewById(R.id.btnSettings);
       // btnNearby = findViewById(R.id.btnNearby);
       // btnLogout = findViewById(R.id.btnLogout);
        //btnProfile = findViewById(R.id.btnProfile);

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

        // 🔥 Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) {
                return true;
            }

            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfileActivity.class));
                finish();
                return true;
            }

            if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }

            return false;
        });

       // btnProfile.setOnClickListener(v ->
              //  startActivity(new Intent(this, UserProfileActivity.class)));

      //  btnLogout.setOnClickListener(v -> {
          //  FirebaseAuth.getInstance().signOut();
          //  startActivity(new Intent(this, LoginActivity.class));
        //finish();
       // });

       // btnSettings.setOnClickListener(v ->
           //     startActivity(new Intent(this, SettingsActivity.class)));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.usersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter(userList);
        recyclerView.setAdapter(adapter);

        getLocation();
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

            String minAgeText = minAge.getText().toString().trim();
            String maxAgeText = maxAge.getText().toString().trim();

            int min = 0;
            int max = 100;

            try {
                if (!minAgeText.isEmpty()) {
                    min = Integer.parseInt(minAgeText);
                }
                if (!maxAgeText.isEmpty()) {
                    max = Integer.parseInt(maxAgeText);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid age input", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedGender = genderFilter.getSelectedItem().toString();

            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                Boolean hidden = doc.getBoolean("hidden");
                if (hidden != null && hidden) continue;

                Double lat = doc.getDouble("lat");
                Double lng = doc.getDouble("lng");

                if (lat == null || lng == null) continue;

                String email = doc.getString("email");
                if (email == null) email = "unknown@email.com";

                String name = doc.getString("name");
                if (name == null) name = "Unknown";

                Long age = doc.getLong("age");
                String gender = doc.getString("gender");

                if (auth.getCurrentUser() != null &&
                        doc.getId().equals(auth.getCurrentUser().getUid())) {
                    continue;
                }

                if (age != null && (age < min || age > max)) continue;

                if (!selectedGender.equals("All")) {
                    if (gender == null || !gender.equals(selectedGender)) continue;
                }

                double distance = distanceBetween(myLat, myLng, lat, lng);

                if (distance < 100) {

                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl == null) imageUrl = "";

                    userList.add(new User(
                            doc.getId(),
                            email,
                            name,
                            doc.getString("instagram"),
                            doc.getString("facebook"),
                            doc.getString("linkedin"),
                            doc.getString("twitter"),
                            imageUrl,
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