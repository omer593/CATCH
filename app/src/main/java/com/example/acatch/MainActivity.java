package com.example.acatch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;
    FirebaseFirestore db;
    FirebaseAuth auth;

    RecyclerView recyclerView;
    UserAdapter adapter;
    List<User> userList = new ArrayList<>();

    Button btnSettings, btnNearby, btnLogout, btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 חיבור כפתורים
        btnSettings = findViewById(R.id.btnSettings);
        btnNearby = findViewById(R.id.btnNearby);
        btnLogout = findViewById(R.id.btnLogout);
        btnProfile = findViewById(R.id.btnProfile);

        // 🔥 פעולות כפתורים

        btnNearby.setOnClickListener(v -> {
            // כבר במסך הזה
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        btnSettings.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Settings coming soon", Toast.LENGTH_SHORT).show();
        });

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

                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        saveLocation(lat, lng);
                        getNearbyUsers(lat, lng);

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

            for (var doc : queryDocumentSnapshots.getDocuments()) {

                Double lat = doc.getDouble("lat");
                Double lng = doc.getDouble("lng");
                String email = doc.getString("email");

                String name = doc.getString("name");
                String instagram = doc.getString("instagram");
                String facebook = doc.getString("facebook");
                String linkedin = doc.getString("linkedin");
                String twitter = doc.getString("twitter");

                if (lat == null || lng == null || email == null) continue;

                if (auth.getCurrentUser() != null &&
                        doc.getId().equals(auth.getCurrentUser().getUid())) {
                    continue;
                }

                double distance = distanceBetween(myLat, myLng, lat, lng);

                if (distance < 100) {
                    userList.add(new User(
                            doc.getId(),
                            email,
                            name,
                            instagram,
                            facebook,
                            linkedin,
                            twitter,
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