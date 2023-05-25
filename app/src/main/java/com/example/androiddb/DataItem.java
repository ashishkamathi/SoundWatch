package com.example.androiddb;

public class DataItem {

    private String dateTime;
    private String placeName;
    private String decibel;

    public DataItem(String dateTime, String placeName, String decibel) {
        this.dateTime = dateTime;
        this.placeName = placeName;
        this.decibel = decibel;
    }

    // Getters and setters
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getDecibel() {
        return decibel;
    }

    public void setDecibel(String decibel) {
        this.decibel = decibel;
    }
}
