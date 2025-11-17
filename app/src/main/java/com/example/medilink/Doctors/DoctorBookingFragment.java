package com.example.medilink.Doctors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DoctorBookingFragment extends Fragment {

    private RecyclerView recyclerView;
    private DoctorBookingAdaptor adaptor;
    private final List<DoctorSchedule.Slots> slotList = new ArrayList<>();
    private final String doctorId = FirebaseAuth.getInstance().getUid();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_doctor_booking, container, false);
        recyclerView = view.findViewById(R.id.rvGridAppointments);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adaptor = new DoctorBookingAdaptor(getContext(), slotList);
        recyclerView.setAdapter(adaptor);

        loadDoctorSlots();

        return view;
    }

    private void loadDoctorSlots() {
        FirebaseFirestore.getInstance()
                .collection("doctors")
                .document(doctorId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        DoctorSchedule schedule = doc.toObject(DoctorSchedule.class);
                        if (schedule != null && schedule.getSchedule() != null) {
                            slotList.clear();

                            // FILTER only booked slots
                            for (DoctorSchedule.Slots slot : schedule.getSchedule()) {
                                if (slot.getBookedBy() != null && !slot.getBookedBy().trim().isEmpty()) {
                                    slotList.add(slot);
                                }
                            }

                            if (slotList.isEmpty()) {
                                Toast.makeText(getContext(), "No booked appointments", Toast.LENGTH_SHORT).show();
                            }

                            adaptor.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "No schedule available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Doctor data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}
