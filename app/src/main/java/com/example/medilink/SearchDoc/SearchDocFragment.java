package com.example.medilink.SearchDoc;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.medilink.AppCache;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.Day;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchDocFragment extends Fragment {

    RecyclerView rvGridAppointments, rvSchedule;
    MaterialCardView mcvAppointment;
    TextView tvPrice;
    private final List<DoctorSchedule> doctorSchedules = new ArrayList<>();

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
        if(AppCache.getInstance().getLoadedData() == null || AppCache.getInstance().getLoadedData().doctors == null) {
            Toast.makeText(getContext(), "No cached doctor data found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DoctorSchedule> doctors = AppCache.getInstance().getLoadedData().doctors;

        for(DoctorSchedule doctor: doctors) {
            updateSlotBasedOnTime(doctor);
        }

        setUpUi(doctors);
    }

    private void updateSlotBasedOnTime(DoctorSchedule doctors) {
        List<DoctorSchedule.Slots> schedule = doctors.getSchedule();

        if(schedule == null) { return;}

        LocalTime currentTime = LocalTime.now();
        for(DoctorSchedule.Slots slot : schedule) {
            if(!slot.isAvailable && slot.bookedBy != null) {
                LocalTime endTime = LocalTime.parse(slot.end);
                if(endTime.isBefore(currentTime)) {
                    slot.bookedBy = null;
                    slot.isAvailable = true;
                }
            }
        }

    }

    private void setUpUi(List<DoctorSchedule> doctorSchedules) {
        AppointmentAdaptor adaptor = new AppointmentAdaptor(
                getContext(),
                doctorSchedules,
                doctor -> {
                    mcvAppointment.setVisibility(View.VISIBLE);
                    tvPrice.setText("PKR " + doctor.getPrice());

                    List<Day> days = getNext7Days();

                    ScheduleAdaptor scheduleAdaptor = new ScheduleAdaptor(
                            days,
                            doctor,
                            (day1, doctor1) -> showDoctorSlotsDialog(doctor1, day1)
                    );

                    rvSchedule.setLayoutManager(
                            new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
                    );
                    rvSchedule.setAdapter(scheduleAdaptor);
                }
        );
        rvGridAppointments.setAdapter(adaptor);
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

    private void showDoctorSlotsDialog(DoctorSchedule doctor, Day day) {
        String selectedDay = day.getName();
        List<String> availableSlot = new ArrayList<>();

        for(DoctorSchedule.Slots slot: doctor.getSchedule()) {
            if(slot.day.equalsIgnoreCase(selectedDay) && slot.isAvailable) {
                availableSlot.add(slot.start + "-" + slot.end);
            }
        }

        if(availableSlot.isEmpty()) {
            Toast.makeText(getContext(), "No available slots on " + selectedDay, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Available Slots for " + selectedDay)
                .setItems(availableSlot.toArray(new String[0]), (dialog, which) -> {
                   String[] times = availableSlot.get(which).split(" - ");
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

    private void bookSlotForUser(DoctorSchedule doctor, DoctorSchedule.Slots slot) {
        if(!isAdded()) {
            return;
        }

        final Context context = requireContext();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getUid();

        db.collection("doctors").document(doctor.getDocId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if(!snapshot.exists()) {
                        Toast.makeText(context, "Doctor data not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    List<Map<String, Object>> scheduleRaw =
                            (List<Map<String, Object>>) snapshot.get("schedule");

                    if(scheduleRaw == null) {
                        Toast.makeText(context, "No schedule available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean slotFound = false;

                    for(Map<String, Object> slotMap: scheduleRaw) {
                        String day = (String) slotMap.get("day");
                        String start = (String) slotMap.get("start");
                        String end = (String) slotMap.get("end");

                        if(day.equals(slot.day) && start.equals(slot.start) && end.equals(slot.end)) {
                            slotMap.put("isAvailable", false);
                            slotMap.put("bookedBy", userId);
                            slotFound = true;
                            break;
                        }
                    }

                    if(!slotFound) {
                        Toast.makeText(context, "Selected slot not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.collection("doctors").document(doctor.getDocId())
                            .update("schedule", scheduleRaw)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Slot booked: " + slot.start + " - " + slot.end, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to fetch schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}
