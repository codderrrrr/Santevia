package com.example.medilink.ModelClass;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

public class DoctorSchedule {

    public void setSchedule(List<Slots> scheduleList) {
        this.schedule = scheduleList;
    }

    public String getDocId() {
        return docId;
    }

    public static class Slots{
        public String day;
        public String start;
        public String end;
        public boolean isAvailable;
        public String bookedBy;
        public Slots() {}
        public Slots(String day, String start, String end, boolean isAvailable, String bookedBy) {
            this.day = day;
            this.start = start;
            this.isAvailable = isAvailable;
            this.end = end;
            this.bookedBy = bookedBy;
        }
        public String getDay() { return day; }
        public String getStart() { return start; }
        public String getEnd() { return end; }
        public boolean isAvailable() { return isAvailable; }
        public String getBookedBy() {return bookedBy;}
    }
    private String name;
    private String specialization;
    private String experience;
    private String docId;
    private String PhoneNo;
    private String education;
    private List<Slots> schedule;
    private String Hospital;
    int price;
    public DoctorSchedule() {}

    public DoctorSchedule(String name, String specialization, String education, int price, String phoneNo, String experience, int imageResId, String Hospital) {
        this.name = name;
        this.specialization = specialization;
        this.experience = experience;
        this.PhoneNo = phoneNo;
        this.education = education;
        this.Hospital = Hospital;
        this.price = price;
        generateDefaultSchedule();
    }

    public void generateDefaultSchedule() {
        schedule = new ArrayList<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for(String day: days) {
            addSlots(schedule, day, 9, 13);
            addSlots(schedule, day, 14, 17);
        }
    }

    public void addSlots(List<Slots> schedule, String day, int startHour, int EndHour) {
        for(int i = startHour; i < EndHour; i++) {
            schedule.add(createSlot(day, format(i, 0), format(i, 30)));
            schedule.add(createSlot(day, format(i, 30), format(i + 1, 0)));
        }
    }

    private Slots createSlot(String day, String start, String end) {
        return new Slots(day, start, end, true, "");
    }

    @SuppressLint("DefaultLocale")
    private String format(int hour, int minute) {
        return String.format("%02d:%02d", hour, minute);
    }

    public String getSpecialization() { return specialization; }
    public String getExperience() { return experience; }
    public String getPhone() { return PhoneNo; }
    public List<Slots> getSchedule() {return schedule;}
    public String getEducation() { return education; }
    public String getName() { return name; }
    public String getHospital() {return Hospital;}
    public int getPrice() { return price;}
    public void setDocId(String docId) { this.docId = docId;}
}
