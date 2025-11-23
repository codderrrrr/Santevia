package com.example.medilink.ModelClass;

import com.google.firebase.Timestamp;

public class Weight {
    Timestamp timestamp;
    String userId;
    long weight_kg;
    long weight_lbs;

    Weight() {}
    Weight(Timestamp timestamp, String userId, long weight_kg, long weight_lbs) {
        this.timestamp = timestamp;
        this.weight_kg = weight_kg;
        this.userId = userId;
        this.weight_lbs = weight_lbs;
    }

    public String getUserId() { return userId; }
    public long getWeight_kg() { return weight_kg; }
    public long getWeight_lsb() { return weight_lbs; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setWeight_kg(long weight_kg) {
        this.weight_kg = weight_kg;
    }
    public void setWeight_lbs(long weight_lbs) {
        this.weight_lbs = weight_lbs;
    }

}
