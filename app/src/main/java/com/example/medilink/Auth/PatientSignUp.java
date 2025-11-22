package com.example.medilink.Auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.medilink.ModelClass.Patient;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PatientSignUp extends Fragment {
    Button btnSignUp;
    EditText etName, etAge, etPhone, etAddress, etUsername, etPassword, etVerifyPassword;
    RadioGroup radioGroupGender;
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_patient_sign_up, container, false);
        init(v);

        btnSignUp.setOnClickListener(view -> {
            SignUpPatient();
        });

        return v;
    }
    private void init(View v) {
        btnSignUp = v.findViewById(R.id.btnSignUp);
        etUsername = v.findViewById(R.id.etUsername);
        etPassword = v.findViewById(R.id.etPassword);
        etVerifyPassword = v.findViewById(R.id.etVerifyPassword);
        etName = v.findViewById(R.id.etName);
        etAddress = v.findViewById(R.id.etAddress);
        etAge = v.findViewById(R.id.etAge);
        etPhone = v.findViewById(R.id.etPhoneNo);
        radioGroupGender = v.findViewById(R.id.radioGroupGender);
    }

    private void SignUpPatient() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String verifyPassword = etVerifyPassword.getText().toString().trim();

        if(username.isEmpty() || password.isEmpty() || verifyPassword.isEmpty()) {
            Toast.makeText(getContext(), "Fill All Fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(verifyPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        savePatientData();
                    }
                    else {
                        Toast.makeText(getContext(),
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void savePatientData() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String Address = etAddress.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String gender = "";
        int selectedId = radioGroupGender.getCheckedRadioButtonId();

        if (selectedId != -1) {
            RadioButton selectedRadioButton = requireActivity().findViewById(selectedId);
            gender = selectedRadioButton.getText().toString().trim();
        }

        Patient patient = new Patient(
                name,
                Address,
                phone,
                age,
                gender
        );

        String uid = mAuth.getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("Patients")
                .document(uid)
                .set(patient)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Patient registered successfully", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = new HashMap<>();
                    map.put("userType", "patient");

                    FirebaseFirestore.getInstance().collection("users")
                            .document(uid)
                            .set(map)
                            .addOnSuccessListener(unused -> {
                                requireActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.FragmentContainer, new LoginFragment())
                                        .commit();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to save userType: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}