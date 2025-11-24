package com.example.medilink.Stats;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medilink.ModelClass.Sleep;
import com.example.medilink.ModelClass.WaterIntake;
import com.example.medilink.ModelClass.Weight;
import com.example.medilink.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StatsInput extends AppCompatActivity {

    RadioGroup radioGroupStats;
    RadioButton radioWeight, radioWater, radioSleep;
    EditText etValue;
    Button btnEnter;

    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_input);

        init();

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnEnter.setOnClickListener(v -> saveData());
    }

    private void init() {
        radioGroupStats = findViewById(R.id.radioGroupStats);
        radioWeight = findViewById(R.id.radioWeight);
        radioWater = findViewById(R.id.radioWater);
        radioSleep = findViewById(R.id.radioSleep);
        etValue = findViewById(R.id.etValue);
        btnEnter = findViewById(R.id.btnEnter);
    }

    private void saveData() {
        String valueStr = etValue.getText().toString().trim();
        if (valueStr.isEmpty()) {
            Toast.makeText(this, "Enter a value", Toast.LENGTH_SHORT).show();
            return;
        }

        long value;
        try {
            value = Long.parseLong(valueStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        String type;
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", Timestamp.now());
        data.put("userId", userId);

        if (radioWeight.isChecked()) {
            type = "weight";
            data.put("weight_kg", value);
            data.put("weight_lbs", (long) (value * 2.20462));
        } else if (radioWater.isChecked()) {
            type = "water";
            data.put("value", value);
        } else if (radioSleep.isChecked()) {
            type = "sleep";
            data.put("hours", value);
        } else {
            Toast.makeText(this, "Select a metric", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference ref = db.collection("users")
                .document(userId)
                .collection("stats")
                .document(type)
                .collection("data");

        ref.add(data).addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Saved " + type + " data", Toast.LENGTH_SHORT).show();
                    etValue.setText("");
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
