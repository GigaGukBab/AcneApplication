package com.example.acneapplication.ResultSearchKeyword;

public class PlaceMeta {
    private int total_count;
    private int pageable_count;
    private boolean is_end;
    private RegionInfo same_name;

    // getters and setters

    public int getTotal_count() {
        return total_count;
    }

    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }

    public int getPageable_count() {
        return pageable_count;
    }

    public void setPageable_count(int pageable_count) {
        this.pageable_count = pageable_count;
    }

    public boolean isIs_end() {
        return is_end;
    }

    public void setIs_end(boolean is_end) {
        this.is_end = is_end;
    }

    public RegionInfo getSame_name() {
        return same_name;
    }

    public void setSame_name(RegionInfo same_name) {
        this.same_name = same_name;
    }
}