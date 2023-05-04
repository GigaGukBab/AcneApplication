package com.example.acneapplication;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlaceSearchResponse {
    @SerializedName("places")
    private List<Place> places;

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }
}

