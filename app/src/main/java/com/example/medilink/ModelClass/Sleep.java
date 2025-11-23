package com.example.medilink.ModelClass;
import com.google.firebase.Timestamp;

public class Sleep {
    Timestamp timestamp;
    String userId;
    long hours;

    Sleep() {}
    Sleep(Timestamp timestamp, String userId, long hours) {
        this.timestamp = timestamp;
        this.hours = hours;
        this.userId = userId;
    }

    public String getUserId() { return userId; }
    public long getHours() { return hours; }
    public Timestamp getTimestamp() { return timestamp; }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setHours(long hours) {
        this.hours = hours;
    }
}
