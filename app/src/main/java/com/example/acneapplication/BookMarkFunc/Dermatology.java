package com.example.acneapplication.BookMarkFunc;

public class Dermatology {
    private String name;

    public Dermatology() {
        // Default constructor required for calls to DataSnapshot.getValue(Dermatology.class)
    }

    public Dermatology(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
