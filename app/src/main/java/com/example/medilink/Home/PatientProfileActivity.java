package com.example.medilink.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medilink.Auth.LoginSignUp;
import com.example.medilink.FireBaseDataLoader;
import com.example.medilink.ModelClass.Patient;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class PatientProfileActivity extends AppCompatActivity {
    TextView tvName, tvPhoneNo, tvGender,tvAddress, tvAge;
    Button btnPatientLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_patient_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

        btnPatientLogOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(PatientProfileActivity.this, LoginSignUp.class);
            startActivity(intent);
            finish();
        });

        String patientId = FirebaseAuth.getInstance().getUid();
        loadPatientProfile(patientId);
    }

    @SuppressLint("SetTextI18n")
    private void loadPatientProfile(String patientId) {
        FirebaseFirestore.getInstance()
                .collection("Patients")
                .document(patientId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phoneNo");
                        String gender = documentSnapshot.getString("gender");
                        String age = documentSnapshot.getString("age");
                        String address = documentSnapshot.getString("address");

                        tvName.setText("Name: " + name);
                        tvPhoneNo.setText("Phone No: " + phone);
                        tvGender.setText("Gender: " + gender);
                        tvAge.setText("Age: " + age);
                        tvAddress.setText("Address: " + address);
                    } else {
                        Toast.makeText(this, "Patient data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }



    private void init() {
        tvName = findViewById(R.id.tvName);
        tvPhoneNo = findViewById(R.id.tvPhoneNo);
        tvGender = findViewById(R.id.tvGender);
        tvAge = findViewById(R.id.tvAge);
        tvAddress = findViewById(R.id.tvAddress);
        btnPatientLogOut = findViewById(R.id.btnPatientLogOut);
    }
}