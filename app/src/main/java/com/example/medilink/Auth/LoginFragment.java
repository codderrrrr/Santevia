package com.example.medilink.Auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.medilink.Home.DoctorHomeScreen;
import com.example.medilink.Home.HomeScreen;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class LoginFragment extends Fragment {

    private Button btnLogin;
    private EditText etUsername, etPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        String cachedUserType = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("USER_TYPE", null);

        if (currentUser != null && cachedUserType != null) {
            navigateToHome(cachedUserType);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login, container, false);

        etUsername = v.findViewById(R.id.etUsername);
        etPassword = v.findViewById(R.id.etPassword);
        btnLogin = v.findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(view -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(getContext(), "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                lookupUserInFirestore(user.getUid());
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                    "Login Failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        return v;
    }

    private void lookupUserInFirestore(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.contains("userType")) {
                            String userType = document.getString("userType");
                            if (userType != null) {
                                saveUserTypeAndNavigate(userType);
                            } else {
                                Toast.makeText(requireContext(), "User type not set in Firestore.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        } else {
                            Toast.makeText(requireContext(), "User profile missing in Firestore.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error fetching user data: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                    }
                });
    }

    @SuppressLint("CommitPrefEdits")
    private void saveUserTypeAndNavigate(String userType) {
        requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .putString("USER_TYPE", userType)
                .apply();

        navigateToHome(userType);
    }

    private void navigateToHome(String userType) {
        if ("doctor".equals(userType)) {
            startActivity(new Intent(getActivity(), DoctorHomeScreen.class));
        } else if ("patient".equals(userType)) {
            startActivity(new Intent(getActivity(), HomeScreen.class));
        } else {
            Toast.makeText(requireContext(), "Unknown user type!", Toast.LENGTH_SHORT).show();
        }
        requireActivity().finish();
    }
}
