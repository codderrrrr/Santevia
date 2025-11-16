package com.example.medilink.SearchDoc;

public class AppointmentModel {
    int image;
    String name;
    String Specialization;
    public AppointmentModel(Integer image, String name, String Date, String Specialization) {
        this.image = image;
        this.name= name;
        this.Specialization = Specialization;
    }

    public int getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return Specialization;
    }
}
