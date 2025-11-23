package com.example.medilink.Doctors;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.medilink.AppCache;
import com.example.medilink.Auth.LoginSignUp;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class DoctorProfileFragment extends Fragment {
    TextView tvName, tvPhoneNo, tvEducation, tvExperience, tvHospital, tvFees;
    Button btnLogOut;

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

        btnLogOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginSignUp.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

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

        assert loggedInDoctor != null;
        tvName.setText("Name: " + loggedInDoctor.getName());
        tvPhoneNo.setText("Phone No: " + loggedInDoctor.getPhoneNo());
        tvEducation.setText("Education: " + loggedInDoctor.getEducation());
        tvExperience.setText("Experience: " + loggedInDoctor.getExperience());
        tvHospital.setText("Hospital: " + loggedInDoctor.gethospital());
        tvFees.setText("Fee: " + String.valueOf(loggedInDoctor.getPrice()));

        return v;
    }

    private void init(View v) {
        tvName = v.findViewById(R.id.tvName);
        tvPhoneNo = v.findViewById(R.id.tvPhoneNo);
        tvFees = v.findViewById(R.id.tvFees);
        tvEducation = v.findViewById(R.id.tvEducation);
        tvExperience = v.findViewById(R.id.tvExperience);
        tvHospital = v.findViewById(R.id.tvHospital);
        btnLogOut = v.findViewById(R.id.btnLogOut);
    }
}
