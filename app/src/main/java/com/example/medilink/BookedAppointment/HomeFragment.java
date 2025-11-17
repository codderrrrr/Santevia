package com.example.medilink.BookedAppointment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.medilink.AppCache;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView rvGridAppointments;
    private BookingsAdaptor adaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        rvGridAppointments = v.findViewById(R.id.rvGridAppointments);
        rvGridAppointments.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adaptor = new BookingsAdaptor(getContext(), new ArrayList<>());
        rvGridAppointments.setAdapter(adaptor);

        loadBookedAppointments();

        return v;
    }

    private void loadBookedAppointments() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        List<DoctorSchedule> bookedAppointments = new ArrayList<>();

        if (AppCache.getInstance().getLoadedData() != null &&
                AppCache.getInstance().getLoadedData().doctors != null) {

            for (DoctorSchedule doctor : AppCache.getInstance().getLoadedData().doctors) {
                if (doctor.getSchedule() == null) continue;

                for (DoctorSchedule.Slots slot : doctor.getSchedule()) {
                    if (slot != null && !slot.isAvailable && userId.equals(slot.getBookedBy())) {
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

                        List<DoctorSchedule.Slots> singleSlotList = new ArrayList<>();
                        singleSlotList.add(slot);
                        appointment.setSchedule(singleSlotList);

                        bookedAppointments.add(appointment);
                    }
                }
            }
        }

        if (!bookedAppointments.isEmpty()) {
            adaptor.updateList(bookedAppointments);
        } else {
            Toast.makeText(getContext(), "No booked appointments found", Toast.LENGTH_SHORT).show();
        }
    }
}
