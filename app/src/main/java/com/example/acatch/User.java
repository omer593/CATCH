package com.example.acatch;

public class User {

    String id;
    String email;

    String name;
    String instagram;
    String facebook;
    String linkedin;
    String twitter;

    double lat, lng, distance;

    public User(String id, String email,
                String name,
                String instagram,
                String facebook,
                String linkedin,
                String twitter,
                double lat, double lng, double distance) {

        this.id = id;
        this.email = email;

        this.name = name;
        this.instagram = instagram;
        this.facebook = facebook;
        this.linkedin = linkedin;
        this.twitter = twitter;

        this.lat = lat;
        this.lng = lng;
        this.distance = distance;
    }
}