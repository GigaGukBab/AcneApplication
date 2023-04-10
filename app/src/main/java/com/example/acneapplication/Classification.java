package com.example.acneapplication;

import androidx.annotation.Keep;

@Keep
public class Classification {
    private String id;
    private String result;
    private String timestamp;

    private String documentId;

    // 기본 생성자 추가
    public Classification() {

    }

    public Classification(String id, String result, String timestamp) {
        this.id = id;
        this.result = result;
        this.timestamp = timestamp;
        this.documentId = documentId;
    }

    // 필드에 대한 getter와 setter 메서드를 추가합니다.
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
