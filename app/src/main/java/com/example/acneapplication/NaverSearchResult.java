package com.example.acneapplication;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NaverSearchResult {
    @SerializedName("items")
    private List<NaverSearchItem> items;

    public List<NaverSearchItem> getItems() {
        return items;
    }

    public void setItems(List<NaverSearchItem> items) {
        this.items = items;
    }

    public static class NaverSearchItem {
        @SerializedName("title")
        private String title;

        @SerializedName("mapx")
        private double mapX;

        @SerializedName("mapy")
        private double mapY;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public double getMapX() {
            return mapX;
        }

        public void setMapX(double mapX) {
            this.mapX = mapX;
        }

        public double getMapY() {
            return mapY;
        }

        public void setMapY(double mapY) {
            this.mapY = mapY;
        }
    }
}

