package com.example.acneapplication.ResultSearchKeyword;

import java.util.List;

public class ResultSearchKeyword {
    private PlaceMeta meta;
    private List<Place> documents;

    // getters and setters

    public PlaceMeta getMeta() {
        return meta;
    }

    public void setMeta(PlaceMeta meta) {
        this.meta = meta;
    }

    public List<Place> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Place> documents) {
        this.documents = documents;
    }
}