package com.example.medilink.SearchDoc;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.Day;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchDocFragment extends Fragment {

    RecyclerView rvGridAppointments, rvSchedule;
    MaterialCardView mcvAppointment;
    TextView tvPrice;

    // MUST BE LIST<DoctorSchedule>
    private List<DoctorSchedule> doctorSchedules = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_doc, container, false);

        rvGridAppointments = v.findViewById(R.id.rvGridAppointments);
        rvGridAppointments.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvSchedule = v.findViewById(R.id.rvSchedule);
        mcvAppointment = v.findViewById(R.id.mcvAppointment);
        tvPrice = v.findViewById(R.id.tvPrice);

        fetchDoctors();
        return v;
    }


    private void fetchDoctors() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("doctors").addSnapshotListener((querySnapshot, error) -> {
            if (error != null || querySnapshot == null) {
                Toast.makeText(getContext(), "Error loading doctors", Toast.LENGTH_SHORT).show();
                return;
            }

            doctorSchedules.clear();

            for (DocumentSnapshot doc : querySnapshot) {
                DoctorSchedule doctor = parseDoctor(doc);
                doctor.setDocId(doc.getId());
                doctorSchedules.add(doctor); // FIXED
            }

            updateSlotsBasedOnTime(doctorSchedules); // OPTIONAL BUT USEFUL

            setupUI(doctorSchedules);
        });
    }


    private void updateSlotsBasedOnTime(List<DoctorSchedule> schedules) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime current = now.toLocalTime();

        for (DoctorSchedule doctor : schedules) {
            for (DoctorSchedule.Slots slot : doctor.getSchedule()) {
                LocalTime endTime = LocalTime.parse(slot.end);

                // If booked slot time passed → reset
                if (!slot.isAvailable && endTime.isBefore(current)) {
                    slot.isAvailable = true;
                    slot.bookedBy = null;
                }
            }
        }
    }


    private DoctorSchedule parseDoctor(DocumentSnapshot doc) {
        String name = doc.getString("name");
        String specialization = doc.getString("specialization");
        String education = doc.getString("education");
        String phoneNo = doc.getString("PhoneNo");
        String experience = doc.getString("experience");
        String hospital = doc.getString("hospital");

        int price = doc.getLong("price") != null ? doc.getLong("price").intValue() : 0;

        DoctorSchedule doctor = new DoctorSchedule(
                name, specialization, education, price, phoneNo, experience, 0, hospital
        );

        List<Map<String, Object>> scheduleRaw = (List<Map<String, Object>>) doc.get("schedule");
        List<DoctorSchedule.Slots> scheduleList = new ArrayList<>();

        if (scheduleRaw != null) {
            for (Map<String, Object> s : scheduleRaw) {
                boolean isAvailable = s.get("isAvailable") == null || (boolean) s.get("isAvailable");
                String bookedBy = s.get("bookedBy") != null ? (String) s.get("bookedBy") : null;

                scheduleList.add(new DoctorSchedule.Slots(
                        (String) s.get("day"),
                        (String) s.get("start"),
                        (String) s.get("end"),
                        isAvailable,
                        bookedBy
                ));
            }
        }

        doctor.setSchedule(scheduleList);
        return doctor;
    }


    private void setupUI(List<DoctorSchedule> doctorSchedules) {

        AppointmentAdaptor adaptor = new AppointmentAdaptor(getContext(), doctorSchedules, doctor -> {

            mcvAppointment.setVisibility(View.VISIBLE);
            tvPrice.setText("PKR " + doctor.getPrice());

            List<Day> days = getNext7Days();
            ScheduleAdaptor scheduleAdaptor = new ScheduleAdaptor(days, doctor, (day1, doctor1) -> {
                showDoctorSlotsDialog(doctor1, day1);
            });

            rvSchedule.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvSchedule.setAdapter(scheduleAdaptor);

        });

        rvGridAppointments.setAdapter(adaptor);
    }


    private void showDoctorSlotsDialog(DoctorSchedule doctor, Day day) {
        String selectedDay = day.getName();
        List<String> availableSlots = new ArrayList<>();

        for (DoctorSchedule.Slots slot : doctor.getSchedule()) {
            if (slot.day.equalsIgnoreCase(selectedDay) && slot.isAvailable) {
                availableSlots.add(slot.start + " - " + slot.end);
            }
        }

        if (availableSlots.isEmpty()) {
            Toast.makeText(getContext(), "No available slots on " + selectedDay, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Available Slots - " + selectedDay)
                .setItems(availableSlots.toArray(new String[0]), (dialog, which) -> {

                    String[] times = availableSlots.get(which).split(" - ");
                    String start = times[0];
                    String end = times[1];

                    for (DoctorSchedule.Slots slot : doctor.getSchedule()) {
                        if (slot.day.equalsIgnoreCase(selectedDay) &&
                                slot.start.equals(start) &&
                                slot.end.equals(end)) {
                            bookSlotForUser(doctor, slot);
                            break;
                        }
                    }
                })
                .setPositiveButton("Close", null)
                .show();
    }


    public List<Day> getNext7Days() {
        List<Day> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            list.add(new Day(date, date.format(formatter), date.getDayOfMonth()));
        }

        return list;
    }


    private void bookSlotForUser(DoctorSchedule doctor, DoctorSchedule.Slots slot) {
        slot.isAvailable = false;
        slot.bookedBy = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("doctors").document(doctor.getDocId())
                .update("schedule", doctor.getSchedule())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Slot booked: " + slot.start + " - " + slot.end, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    slot.isAvailable = true;   // rollback
                    slot.bookedBy = null;
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

