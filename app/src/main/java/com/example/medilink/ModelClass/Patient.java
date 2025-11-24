package com.example.medilink.ModelClass;

public class Patient {
    private String name;
    private String address;
    private String phoneNo;
    private String age;
    private String gender;

    public Patient() {}

    public Patient(String name, String address, String phoneNo, String age, String gender) {
        this.name = name;
        this.address = address;
        this.phoneNo = phoneNo;
        this.age = age;
        this.gender = gender;
    }

    // Getters
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhoneNo() { return phoneNo; }
    public String getAge() { return age; }
    public String getGender() { return gender; }

    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNo(String phoneNo) { this.phoneNo = phoneNo; }
    public void setAge(String age) { this.age = age; }
    public void setGender(String gender) { this.gender = gender; }
}
