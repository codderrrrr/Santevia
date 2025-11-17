package com.example.medilink.ModelClass;

public class user {
    String userId;
    String userType;

    user() {}
    user(String userId, String userType) {
        this.userId = userId;
        this.userType = userType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }
}
