package com.example.medilink.ModelClass;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DoctorSchedule {

    // --- Data Model Fields ---
    private String name;
    private String specialization;
    private String experience;
    private String docId;
    private String PhoneNo;
    private String education;
    private String hospital;
    private int price;

    // --- Hardcoded Schedule Constants (Used for Slot Generation in UI) ---
    private static final String[] DAYS_OF_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final int MORNING_START = 9; // 9 AM
    private static final int MORNING_END = 13; // 1 PM
    private static final int AFTERNOON_START = 14; // 2 PM
    private static final int AFTERNOON_END = 17; // 5 PM
    private static final int SLOT_DURATION_MINUTES = 30;

    // --- Constructor ---
    public DoctorSchedule() {}

    public DoctorSchedule(String name, String specialization, String education, int price, String phoneNo, String experience, int imageResId, String hospital) {
        this.name = name;
        this.specialization = specialization;
        this.experience = experience;
        this.PhoneNo = phoneNo;
        this.education = education;
        this.hospital = hospital;
        this.price = price;
        // Removed generateDefaultSchedule() call
    }

    // --- Helper Class for Generating Slots (Moved here for clean access) ---
    public static class PotentialSlot {
        public Date startTime;
        public Date endTime;
        public String dayName;
        public boolean isBooked = false; // Determined by querying bookings collection

        public PotentialSlot(Date startTime, Date endTime, String dayName) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.dayName = dayName;
        }

        // Helper method to format time for display
        @SuppressLint("DefaultLocale")
        public String getDisplayTime() {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startTime);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endTime);

            return String.format("%02d:%02d - %02d:%02d",
                    startCal.get(Calendar.HOUR_OF_DAY),
                    startCal.get(Calendar.MINUTE),
                    endCal.get(Calendar.HOUR_OF_DAY),
                    endCal.get(Calendar.MINUTE));
        }
    }

    // --- NEW: Slot Generation Logic for UI ---
    // This is now the core schedule logic, no longer saved to Firestore.
    public static List<PotentialSlot> generatePotentialSlotsForDay(Date dayDate) {
        List<PotentialSlot> slots = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(dayDate);
        String dayName = getDayNameFromCalendar(cal.get(Calendar.DAY_OF_WEEK));

        if (!java.util.Arrays.asList(DAYS_OF_WEEK).contains(dayName)) {
            return slots; // Doctor does not work on this day
        }

        // Add Morning Slots (9:00 to 1:00 PM)
        addTimeSlots(slots, dayDate, dayName, MORNING_START, MORNING_END);
        // Add Afternoon Slots (2:00 PM to 5:00 PM)
        addTimeSlots(slots, dayDate, dayName, AFTERNOON_START, AFTERNOON_END);

        return slots;
    }

    private static void addTimeSlots(List<PotentialSlot> slots, Date dayDate, String dayName, int startHour, int endHour) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(dayDate);
        startCal.set(Calendar.HOUR_OF_DAY, startHour);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endLimit = (Calendar) startCal.clone();
        endLimit.set(Calendar.HOUR_OF_DAY, endHour);

        while (startCal.before(endLimit)) {
            Date startTime = startCal.getTime();
            Calendar endCal = (Calendar) startCal.clone();
            endCal.add(Calendar.MINUTE, SLOT_DURATION_MINUTES);

            if (endCal.after(endLimit)) break; // Prevent generating a slot that goes over the end time

            slots.add(new PotentialSlot(startTime, endCal.getTime(), dayName));

            startCal.add(Calendar.MINUTE, SLOT_DURATION_MINUTES); // Move to the next slot
        }
    }

    private static String getDayNameFromCalendar(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
            case Calendar.SUNDAY: return "Sunday";
            default: return "";
        }
    }


    // --- Getters and Setters (Updated to reflect changes) ---
    public String getSpecialization() { return specialization; }
    public String getExperience() { return experience; }
    public String getPhone() { return PhoneNo; }
    // public List<Slots> getSchedule() {return schedule;} <-- REMOVED
    public String getEducation() { return education; }
    public String getName() { return name; }
    public String gethospital() {return hospital;}
    public int getPrice() { return price;}
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId;}
}