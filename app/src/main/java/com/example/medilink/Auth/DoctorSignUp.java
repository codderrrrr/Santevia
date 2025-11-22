package com.example.medilink.Auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DoctorSignUp extends Fragment {

    Button btnSignUp, btnAddDegree;
    EditText etName, etUsername, etPassword, etVerifyPassword, etPhoneNo,
            etSpecialization, etFees, etHospital, etExperience;
    LinearLayout degreeContainer;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doctor_sign_up, container, false);
        init(v);
        btnSignUp.setOnClickListener(view -> signUpDoc());
        btnAddDegree.setOnClickListener(view -> addDegrees());
        return v;
    }

    private void init(View v) {
        etName = v.findViewById(R.id.etName);
        etUsername = v.findViewById(R.id.etUsername);
        etPassword = v.findViewById(R.id.etPassword);
        etVerifyPassword = v.findViewById(R.id.etVerifyPassword);
        etPhoneNo = v.findViewById(R.id.etPhoneNo);
        etSpecialization = v.findViewById(R.id.etSpecialization);
        etFees = v.findViewById(R.id.etFees);
        etHospital = v.findViewById(R.id.etHospital);
        etExperience = v.findViewById(R.id.etExperience);
        degreeContainer = v.findViewById(R.id.degreeContainer);
        btnAddDegree = v.findViewById(R.id.btnAddDegree);
        btnSignUp = v.findViewById(R.id.btnSignUp);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void addDegrees() {
        EditText newDegree = new EditText(getContext());
        newDegree.setHint("Enter Degree");
        newDegree.setSingleLine(true);
        newDegree.setImeOptions(EditorInfo.IME_ACTION_DONE);
        degreeContainer.addView(newDegree);
    }

    private void signUpDoc() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String verifyPassword = etVerifyPassword.getText().toString().trim();

        if(username.isEmpty() || password.isEmpty() || verifyPassword.isEmpty()) {
            Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!password.equals(verifyPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) saveDoctorData();
                    else Toast.makeText(getContext(), "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveDoctorData() {
        String name = etName.getText().toString().trim();
        String phone = etPhoneNo.getText().toString().trim();
        String specialization = etSpecialization.getText().toString().trim();
        String fees = etFees.getText().toString().trim();
        String hospital = etHospital.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();

        ArrayList<String> degrees = new ArrayList<>();
        for(int i=0; i<degreeContainer.getChildCount(); i++){
            EditText etDegree = (EditText) degreeContainer.getChildAt(i);
            String degree = etDegree.getText().toString().trim();
            if(!degree.isEmpty()) degrees.add(degree);
        }

        DoctorSchedule doctor = new DoctorSchedule(
                name, specialization, String.join(", ", degrees),
                Integer.parseInt(fees), phone, experience, 0, hospital
        );

        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> doctorMap = new HashMap<>();
        doctorMap.put("name", doctor.getName());
        doctorMap.put("specialization", doctor.getSpecialization());
        doctorMap.put("education", doctor.getEducation());
        doctorMap.put("price", doctor.getPrice());
        doctorMap.put("PhoneNo", doctor.getPhone());
        doctorMap.put("experience", doctor.getExperience());
        doctorMap.put("hospital", doctor.gethospital());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("doctors").document(uid)
                .set(doctorMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Doctor registered successfully", Toast.LENGTH_SHORT).show();

                    Map<String, Object> map = new HashMap<>();
                    map.put("userType", "doctor");

                    db.collection("users").document(uid)
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