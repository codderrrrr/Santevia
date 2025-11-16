package com.example.medilink.Home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.medilink.BookedAppointment.HomeFragment;
import com.example.medilink.Chats.ChatFragment;
import com.example.medilink.R;
import com.example.medilink.SearchDoc.SearchDocFragment;
import com.example.medilink.Stats.StatisticsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
                replace(new ChatFragment());
            }
            return true;
        });
    }

    private void replace(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flHome, fragment)
                .commit();
    }
}