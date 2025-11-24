package com.example.medilink.ModelClass;

import com.google.firebase.Timestamp;

public class Booking {
    private Timestamp appointmentTime;
    private String bookedByUserId;
    private String doctorId;
    private long durationMinutes;
    private Timestamp bookedAt;
    public Booking() {}

    public Booking(Timestamp appointmentTime, String bookedByUserId, String doctorId, long durationMinutes, Timestamp bookedAt) {
        this.appointmentTime = appointmentTime;
        this.bookedByUserId = bookedByUserId;
        this.doctorId = doctorId;
        this.durationMinutes = durationMinutes;
        this.bookedAt = bookedAt;
    }

    public Timestamp getAppointmentTime() {
        return appointmentTime;
    }

    public String getBookedByUserId() {
        return bookedByUserId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public Timestamp getBookedAt() {
        return bookedAt;
    }
    public void setAppointmentTime(Timestamp appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public void setBookedByUserId(String bookedByUserId) {
        this.bookedByUserId = bookedByUserId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setBookedAt(Timestamp bookedAt) {
        this.bookedAt = bookedAt;
    }
}