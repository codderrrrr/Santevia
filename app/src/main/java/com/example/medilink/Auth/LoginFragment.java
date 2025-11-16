package com.example.medilink.Auth;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class LoginFragment extends Fragment {
    private Button btnLogin;
    private EditText etUsername, etPassword;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            checkUserType(currentUser.getUid());
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

        btnLogin.setOnClickListener(view -> {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(email.isEmpty()) {
                Toast.makeText(getContext(), "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if(password.isEmpty()) {
                Toast.makeText(getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser != null && !currentUser.getEmail().equals(email)) {
                // Sign out previous user if trying to login with a different account
                mAuth.signOut();
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null) checkUserType(user.getUid());
                        } else {
                            Toast.makeText(getContext(),
                                    "Error: " + Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        return v;
    }

    private void checkUserType(String uid) {
        FirebaseFirestore.getInstance().collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if(snapshot.exists()) {
                        String userType = snapshot.getString("userType");
                        if("doctor".equals(userType)) {
                            startActivity(new Intent(getActivity(), DoctorHomeScreen.class));
                        } else if("patient".equals(userType)) {
                            startActivity(new Intent(getActivity(), HomeScreen.class));
                        } else {
                            Toast.makeText(getContext(), "User type not found!", Toast.LENGTH_SHORT).show();
                        }
                        requireActivity().finish();
                    } else {
                        Toast.makeText(getContext(), "User record missing!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
