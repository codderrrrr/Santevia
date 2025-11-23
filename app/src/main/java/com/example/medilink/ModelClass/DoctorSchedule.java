package com.example.medilink.ModelClass;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DoctorSchedule {

    private String name;
    private String specialization;
    private String experience;
    private String docId;
    private String PhoneNo;
    private String education;
    private String hospital;
    private int price;

    private static final String[] DAYS_OF_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private static final int MORNING_START = 9;
    private static final int MORNING_END = 13;
    private static final int AFTERNOON_START = 14;
    private static final int AFTERNOON_END = 17;
    private static final int SLOT_DURATION_MINUTES = 30;

    public DoctorSchedule() {}
    public DoctorSchedule(String name, String specialization, String education, int price, String phoneNo, String experience, int imageResId, String hospital) {
        this.name = name;
        this.specialization = specialization;
        this.experience = experience;
        this.PhoneNo = phoneNo;
        this.education = education;
        this.hospital = hospital;
        this.price = price;
    }

    public static class PotentialSlot {
        public Date startTime;
        public Date endTime;
        public String dayName;
        public boolean isBooked = false;

        public PotentialSlot(Date startTime, Date endTime, String dayName) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.dayName = dayName;
        }

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
    public static List<PotentialSlot> generatePotentialSlotsForDay(Date dayDate) {
        List<PotentialSlot> slots = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(dayDate);
        String dayName = getDayNameFromCalendar(cal.get(Calendar.DAY_OF_WEEK));

        if (!java.util.Arrays.asList(DAYS_OF_WEEK).contains(dayName)) {
            return slots;
        }

        addTimeSlots(slots, dayDate, dayName, MORNING_START, MORNING_END);
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

            if (endCal.after(endLimit)) break;

            slots.add(new PotentialSlot(startTime, endCal.getTime(), dayName));

            startCal.add(Calendar.MINUTE, SLOT_DURATION_MINUTES);
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


    public String getSpecialization() { return specialization; }
    public String getExperience() { return experience; }
    public String getPhoneNo() { return PhoneNo; }
    public String getEducation() { return education; }
    public String getName() { return name; }
    public String gethospital() {return hospital;}
    public int getPrice() { return price;}
    public String getDocId() { return docId; }
    public void setPhoneNo(String PhoneNo) { this.PhoneNo = PhoneNo;}
    public void setDocId(String docId) { this.docId = docId;}
}