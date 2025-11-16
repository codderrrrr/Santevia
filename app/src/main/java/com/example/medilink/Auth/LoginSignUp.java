package com.example.medilink.Auth;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medilink.R;

public class LoginSignUp extends AppCompatActivity {
    Button btnLoginChoice, btnSignUpChoice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.FragmentContainer, new LoginFragment())
                .commit();

        btnLoginChoice.setOnClickListener(v -> {
            btnLoginChoice.setBackgroundColor(getResources().getColor(R.color.whiteshadecard));
            btnSignUpChoice.setBackgroundColor(getResources().getColor(R.color.outerRing));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.FragmentContainer, new LoginFragment())
                    .commit();
        });

        btnSignUpChoice.setOnClickListener(v -> {
            btnLoginChoice.setBackgroundColor(getResources().getColor(R.color.outerRing));
            btnSignUpChoice.setBackgroundColor(getResources().getColor(R.color.whiteshadecard));

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.FragmentContainer, new SignUpFragment())
                    .commit();
        });
    }

    private void init() {
        btnLoginChoice = findViewById(R.id.btnLoginChoice);
        btnSignUpChoice = findViewById(R.id.btnSignUpChoice);
    }
}