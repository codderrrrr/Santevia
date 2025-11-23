package com.example.medilink;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.medilink.OnBoarding.MainActivity;
import com.example.medilink.StatNotification.StatsReminderWorker;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView tvProgress;
    private FireBaseDataLoader dataLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        dataLoader = new FireBaseDataLoader();
        startLoading();
    }

    private void startLoading() {
        dataLoader.loadData(new FireBaseDataLoader.LoadCallBack() {
            @Override
            public void onProgress(int completed, int total) {
                runOnUiThread(() -> {
                    int percent = (completed * 100) / total;
                    progressBar.setProgress(percent);
                    String text = "Loading " + percent + "%";
                    tvProgress.setText(text);
                });
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(FireBaseDataLoader.LoadedData data) {
                AppCache.getInstance().setLoadedData(data);
                runOnUiThread(() -> {
                    progressBar.setProgress(100);
                    tvProgress.setText("Done");
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(SplashActivity.this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finishAffinity();
            }
        });
    }

}
