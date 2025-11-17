package com.example.medilink.Doctors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.medilink.AppCache;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class DoctorProfileFragment extends Fragment {
    TextView tvName, tvPhoneNo, tvEducation, tvExperience, tvHospital, tvFees;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_doctor_profile, container, false);
        init(v);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "No logged-in user found", Toast.LENGTH_SHORT).show();
            return v;
        }

        String doctorId = currentUser.getUid();

        List<DoctorSchedule> doctors = AppCache.getInstance().getLoadedData() != null ?
                AppCache.getInstance().getLoadedData().doctors : null;

        if (doctors == null || doctors.isEmpty()) {
            Toast.makeText(getContext(), "Doctors data not loaded yet", Toast.LENGTH_SHORT).show();
            return v;
        }

        DoctorSchedule loggedInDoctor = null;

        for (DoctorSchedule d : doctors) {
            if (d.getDocId().equals(doctorId)) {
                loggedInDoctor = d;
                break;
            }
        }

        if (loggedInDoctor != null) {
            tvName.setText(loggedInDoctor.getName());
            tvPhoneNo.setText(loggedInDoctor.getPhone());
            tvEducation.setText(loggedInDoctor.getEducation());
            tvExperience.setText(loggedInDoctor.getExperience());
            tvHospital.setText(loggedInDoctor.gethospital());
            tvFees.setText(String.valueOf(loggedInDoctor.getPrice()));
        } else {
            Toast.makeText(getContext(), "Logged-in doctor profile not found", Toast.LENGTH_SHORT).show();
        }

        return v;
    }

    private void init(View v) {
        tvName = v.findViewById(R.id.tvName);
        tvPhoneNo = v.findViewById(R.id.tvPhoneNo);
        tvFees = v.findViewById(R.id.tvFees);
        tvEducation = v.findViewById(R.id.tvEducation);
        tvExperience = v.findViewById(R.id.tvExperience);
        tvHospital = v.findViewById(R.id.tvHospital);
    }
}
