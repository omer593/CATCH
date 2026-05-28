package com.example.acatch;

public class User {

    public String id;
    public String email;

    public String name;
    public String instagram;
    public String facebook;
    public String linkedin;
    public String twitter;

    public String imageUrl; // 🔥 חדש

    public double lat, lng, distance;

    // 🔥 חובה ל-Firebase
    public User() {}

    public User(String id, String email,
                String name,
                String instagram,
                String facebook,
                String linkedin,
                String twitter,
                String imageUrl, // 🔥 נוסף
                double lat, double lng, double distance) {

        this.id = id;
        this.email = email;

        this.name = name;
        this.instagram = instagram;
        this.facebook = facebook;
        this.linkedin = linkedin;
        this.twitter = twitter;

        this.imageUrl = imageUrl; // 🔥

        this.lat = lat;
        this.lng = lng;
        this.distance = distance;
    }
}