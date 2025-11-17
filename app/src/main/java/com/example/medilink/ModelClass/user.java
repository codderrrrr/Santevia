package com.example.medilink.ModelClass;

public class users {
    String userId;
    String userType;

    users() {}
    users(String userId, String userType) {
        this.userId = userId;
        this.userType = userType;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    void setUserType(String userType) {
        this.userType = userType;
    }

    String getUserId() {
        return userId;
    }

    String getUserType() {
        return userType;
    }
}
