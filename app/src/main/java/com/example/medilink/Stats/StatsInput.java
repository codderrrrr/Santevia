package com.example.medilink.Stats;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.medilink.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StatsInput extends AppCompatActivity {
    RadioGroup radioGroupTime, radioGroupStats;
    RadioButton radioManual, radioDefualt, radioHeartRate, radioPulse, radioBloosPressure;
    EditText etValue;
    Button btnEnter;
    TimePicker timePickerManual;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats_input);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

        radioGroupTime.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDefualt) {
                timePickerManual.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioManual) {
                timePickerManual.setVisibility(View.VISIBLE);
            }
        });

        btnEnter.setOnClickListener(v -> saveData());
    }

    private void saveData() {
        String valueStr = etValue.getText().toString().trim();
        if(valueStr.isEmpty()) {
            Toast.makeText(StatsInput.this, "Enter Value field", Toast.LENGTH_SHORT).show();
            return;
        }
        int value = Integer.parseInt(valueStr);
        String collection;
        if(radioHeartRate.isChecked()) {collection = "HeartRate";}
        else if(radioBloosPressure.isChecked()) {collection = "BloodPressure";}
        else if(radioPulse.isChecked()) {collection = "Pulse";}
        else {
            collection = "";
            Toast.makeText(StatsInput.this, "Select Stats for field", Toast.LENGTH_SHORT).show();}

        Calendar calendar = Calendar.getInstance();
        if (radioManual.isChecked()) {
            calendar.set(Calendar.HOUR_OF_DAY, timePickerManual.getHour());
            calendar.set(Calendar.MINUTE, timePickerManual.getMinute());
        }
        Timestamp timestamp = new Timestamp(calendar.getTime());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("value", value);
        data.put("timestamp", timestamp);

        FirebaseFirestore.getInstance()
                .collection(collection)
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Data saved to " + collection, Toast.LENGTH_SHORT).show();
                    etValue.setText("");
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void init() {
        radioGroupTime = findViewById(R.id.radioGroupTime);
        radioGroupStats = findViewById(R.id.radioGroupStats);
        radioManual = findViewById(R.id.radioManual);
        radioDefualt = findViewById(R.id.radioDefualt);
        radioHeartRate = findViewById(R.id.radioHeartRate);
        radioPulse = findViewById(R.id.radioPulse);
        radioBloosPressure = findViewById(R.id.radioBloosPressure);
        etValue = findViewById(R.id.etValue);
        btnEnter = findViewById(R.id.btnEnter);
        timePickerManual = findViewById(R.id.timePickerManual);
    }
}