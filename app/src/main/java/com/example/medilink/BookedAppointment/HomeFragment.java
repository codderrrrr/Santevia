package com.example.medilink.BookedAppointment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragment extends Fragment {

    RecyclerView rvGridAppointments;
    private final List<DoctorSchedule> doctorSchedules = new ArrayList<>();
    private BookingsAdaptor adaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);

        rvGridAppointments = v.findViewById(R.id.rvGridAppointments);
        rvGridAppointments.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adaptor = new BookingsAdaptor(getContext(), doctorSchedules);
        rvGridAppointments.setAdapter(adaptor);

        fetchDoctors();
        return v;
    }

    private void fetchDoctors() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getUid();

        // This list will hold one DoctorSchedule per booked slot
        List<DoctorSchedule> bookedAppointments = new ArrayList<>();

        db.collection("doctors").addSnapshotListener((querySnapshot, error) -> {
            if (error != null || querySnapshot == null) {
                Toast.makeText(getContext(), "Error loading doctors", Toast.LENGTH_SHORT).show();
                return;
            }

            bookedAppointments.clear();

            for (DocumentSnapshot doc : querySnapshot) {
                DoctorSchedule doctor = parseDoctor(doc);
                doctor.setDocId(doc.getId());

                // For every slot booked by the current user, add a separate DoctorSchedule
                for (DoctorSchedule.Slots slot : doctor.getSchedule()) {
                    if (!slot.isAvailable() &&
                            slot.getBookedBy() != null &&
                            slot.getBookedBy().equals(currentUserId)) {

                        // Create a new DoctorSchedule object for this specific booked slot
                        DoctorSchedule appointment = new DoctorSchedule(
                                doctor.getName(),
                                doctor.getSpecialization(),
                                doctor.getEducation(),
                                doctor.getPrice(),
                                doctor.getPhone(),
                                doctor.getExperience(),
                                0,
                                doctor.getHospital()
                        );

                        // Assign only this booked slot to the appointment
                        List<DoctorSchedule.Slots> singleSlotList = new ArrayList<>();
                        singleSlotList.add(slot);
                        appointment.setSchedule(singleSlotList);
                        appointment.setDocId(doctor.getDocId());

                        bookedAppointments.add(appointment);
                    }
                }
            }

            if (!bookedAppointments.isEmpty()) {
                rvGridAppointments.setAdapter(new BookingsAdaptor(getContext(), bookedAppointments));
            } else {
                Toast.makeText(getContext(), "No booked appointments found", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private DoctorSchedule parseDoctor(DocumentSnapshot doc) {

        String name = doc.getString("name");
        String specialization = doc.getString("specialization");
        String education = doc.getString("education");
        String phoneNo = doc.getString("PhoneNo");
        String experience = doc.getString("experience");
        String hospital = doc.getString("hospital");

        int price = doc.getLong("price") != null ?
                Objects.requireNonNull(doc.getLong("price")).intValue() : 0;

        DoctorSchedule doctor = new DoctorSchedule(
                name, specialization, education, price, phoneNo, experience, 0, hospital
        );

        List<Map<String, Object>> scheduleRaw =
                (List<Map<String, Object>>) doc.get("schedule");

        List<DoctorSchedule.Slots> scheduleList = new ArrayList<>();

        if (scheduleRaw != null) {
            for (Map<String, Object> s : scheduleRaw) {

                boolean isAvailable = s.get("isAvailable") == null ||
                        (boolean) s.get("isAvailable");

                String bookedBy = s.get("bookedBy") != null ?
                        (String) s.get("bookedBy") : null;

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
}
