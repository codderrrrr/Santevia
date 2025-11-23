package com.example.medilink.Home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.medilink.BookedAppointment.HomeFragment;
import com.example.medilink.ChatBot.ChatBotFragment;
import com.example.medilink.R;
import com.example.medilink.SearchDoc.SearchDocFragment;
import com.example.medilink.StatNotification.NotificationHelper;
import com.example.medilink.StatNotification.StatsReminderWorker;
import com.example.medilink.Stats.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HomeScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.flHome, new HomeFragment())
                .commit();

        NotificationHelper.createNotificationChannel(this);
        scheduleDailyStatsReminder();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                replace(new HomeFragment());
            }
            else if(item.getItemId() == R.id.nav_stats) {
                replace(new StatisticsFragment());
            }
            else if(item.getItemId() == R.id.nav_search) {
                replace(new SearchDocFragment());
            }
            else if(item.getItemId() == R.id.nav_chat){
                replace(new ChatBotFragment());
            }
            return true;
        });
    }

    private void replace(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flHome, fragment)
                .commit();
    }

    private void scheduleDailyStatsReminder() {
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) return;

        PeriodicWorkRequest statsWork =
                new PeriodicWorkRequest.Builder(StatsReminderWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(calculateDelayTo8PM(), TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueue(statsWork);
    }

    private long calculateDelayTo8PM() {
        Calendar now = Calendar.getInstance();
        Calendar next8PM = (Calendar) now.clone();
        next8PM.set(Calendar.HOUR_OF_DAY, 20);
        next8PM.set(Calendar.MINUTE, 0);
        next8PM.set(Calendar.SECOND, 0);

        if (now.after(next8PM)) {
            next8PM.add(Calendar.DAY_OF_YEAR, 1);
        }

        return next8PM.getTimeInMillis() - now.getTimeInMillis();
    }

}