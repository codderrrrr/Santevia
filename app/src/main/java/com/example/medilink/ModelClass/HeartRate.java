package com.example.medilink.ModelClass;
import com.google.firebase.Timestamp;

public class HeartRate {
    Timestamp timestamp;
    String userId;
    long value;

    HeartRate() {}
    HeartRate(Timestamp timestamp, String userId, long value) {
        this.timestamp = timestamp;
        this.value = value;
        this.userId = userId;
    }

    public String getUserId() { return userId; }
    public long getValue() { return value; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setValue(long value) {
        this.value = value;
    }
}

