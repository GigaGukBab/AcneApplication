package com.example.acneapplication;

import androidx.annotation.Keep;

@Keep
public class Classification {
    private String id;
    private String imageName;
    private String resultStr;
    private String timeStamp;

    public Classification(String id, String imageName, String resultStr, String timeStamp) {
        this.id = id;
        this.imageName = imageName;
        this.resultStr = resultStr;
        this.timeStamp = timeStamp;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getResultStr() {
        return resultStr;
    }

    public void setResultStr(String resultStr) {
        this.resultStr = resultStr;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
