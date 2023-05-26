package com.example.acneapplication.ResultSearchKeyword;

import java.util.List;

public class RegionInfo {
    private List<String> region;
    private String keyword;
    private String selected_region;

    // getters and setters

    public List<String> getRegion() {
        return region;
    }

    public void setRegion(List<String> region) {
        this.region = region;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSelected_region() {
        return selected_region;
    }

    public void setSelected_region(String selected_region) {
        this.selected_region = selected_region;
    }
}
