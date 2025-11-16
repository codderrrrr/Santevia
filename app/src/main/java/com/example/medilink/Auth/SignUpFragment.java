package com.example.medilink.Auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.medilink.R;

public class SignUpFragment extends Fragment {
    Button btnDoctorSignIn, btnPatientSignIn;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);

        btnDoctorSignIn = v.findViewById(R.id.btnDoctorSignIn);
        btnPatientSignIn = v.findViewById(R.id.btnPatientSignIn);

        btnDoctorSignIn.setOnClickListener(view -> {
            replaceFragment(new DoctorSignUp());
        });
        btnPatientSignIn.setOnClickListener(view -> {
            replaceFragment(new PatientSignUp());
        });

        return v;
    }

    void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity()
                .getSupportFragmentManager()
                .beginTransaction();

        transaction.replace(R.id.FragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}