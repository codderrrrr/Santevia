package com.example.medilink.OnBoarding;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.medilink.Auth.LoginSignUp;
import com.example.medilink.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    ViewPager2 viewPagerOnboard;
    ImageButton btnGetStarted;
    TextView tvGetStarted;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isOnboardingDone = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getBoolean("IS_ONBOARDING_DONE", false);

        if (isOnboardingDone) {
            Intent intent = new Intent(MainActivity.this, LoginSignUp.class);
            startActivity(intent);
            finish();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewPagerOnboard = findViewById(R.id.viewPagerOnboard);
        OnBoradingViewPagerAdaptor adaptor = new OnBoradingViewPagerAdaptor(this);
        viewPagerOnboard.setAdapter(adaptor);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        tvGetStarted = findViewById(R.id.tvGetStarted);

        btnGetStarted.setOnClickListener(v -> {
            if(btnGetStarted.isSelected()){
                btnGetStarted.setSelected(!btnGetStarted.isSelected());
                btnGetStarted.getBackground().setTint(getColor(android.R.color.white));
                btnGetStarted.setColorFilter(getColor(android.R.color.black));
            } else {
                btnGetStarted.setSelected(!btnGetStarted.isSelected());
                btnGetStarted.getBackground().setTint(getColor(android.R.color.black));
                btnGetStarted.setColorFilter(getColor(android.R.color.white));
            }
        });

        if (savedInstanceState != null) {
            boolean tickSelected = savedInstanceState.getBoolean("tick_selected", false);
            btnGetStarted.setSelected(tickSelected);
            if (tickSelected) {
                btnGetStarted.getBackground().setTint(getColor(android.R.color.white));
                btnGetStarted.setColorFilter(getColor(android.R.color.black));
            } else {
                btnGetStarted.getBackground().setTint(getColor(android.R.color.black));
                btnGetStarted.setColorFilter(getColor(android.R.color.white));
            }
        }


        tvGetStarted.setOnClickListener(v -> {
            if(btnGetStarted.isSelected()) {
                getSharedPreferences("AppPrefs", MODE_PRIVATE)
                        .edit()
                        .putBoolean("IS_ONBOARDING_DONE", true)
                        .apply();

                Intent intent = new Intent(MainActivity.this, LoginSignUp.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this,
                        "Accept Terms and Conditions to continue",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("tick_selected", btnGetStarted.isSelected());
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int next = (viewPagerOnboard.getCurrentItem() + 1) % Objects.requireNonNull(viewPagerOnboard.getAdapter()).getItemCount();
            viewPagerOnboard.setCurrentItem(next, true);
            sliderHandler.postDelayed(this, 3000);
        }
    };
}