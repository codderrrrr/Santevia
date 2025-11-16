package com.example.medilink.Stats;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.medilink.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private MaterialCardView cardBloodPressure, cardPulse, cardHeartRate;
    private Button btnAll, btnDay, btnWeeks, btnMonths, btnYear;
    ImageButton btnAdd;
    private BarChart barChartStats;
    private FirebaseFirestore db;
    private String currentUserId;
    private String selectedFilter = "All";
    private String selectedStat = "HeartRate";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statistics, container, false);

        init(v);

        // Filter buttons
        btnAll.setOnClickListener(view -> updateFilter("All"));
        btnDay.setOnClickListener(view -> updateFilter("Day"));
        btnWeeks.setOnClickListener(view -> updateFilter("Week"));
        btnMonths.setOnClickListener(view -> updateFilter("Month"));
        btnYear.setOnClickListener(view -> updateFilter("Year"));
        btnAdd.setOnClickListener(view -> addStats());

        // Stat selection cards
        cardBloodPressure.setOnClickListener(view -> { selectedStat = "BloodPressure"; loadStats(); });
        cardHeartRate.setOnClickListener(view -> { selectedStat = "HeartRate"; loadStats(); });
        cardPulse.setOnClickListener(view -> { selectedStat = "Pulse"; loadStats(); });

        // Load default stats
        loadStats();

        return v;
    }

    private void init(View v) {
        cardBloodPressure = v.findViewById(R.id.cardBloodPressure);
        cardPulse = v.findViewById(R.id.cardPulse);
        cardHeartRate = v.findViewById(R.id.cardHeartRate);

        btnAll = v.findViewById(R.id.btnAll);
        btnDay = v.findViewById(R.id.btnDay);
        btnWeeks = v.findViewById(R.id.btnWeeks);
        btnMonths = v.findViewById(R.id.btnMonths);
        btnYear = v.findViewById(R.id.btnYear);
        btnAdd = v.findViewById(R.id.btnAdd);

        barChartStats = v.findViewById(R.id.barChartStats);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void updateFilter(String filter) {
        selectedFilter = filter;
        loadStats();
    }

    private long getStartedTime(String filter) {
        Calendar cal = Calendar.getInstance();

        switch (filter) {
            case "Day":
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case "Week":
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case "Month":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case "Year":
                cal.set(Calendar.DAY_OF_YEAR, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case "All":
            default:
                return 0; // For "All", start from epoch
        }
        return cal.getTimeInMillis();
    }

    private void loadStats() {
        barChartStats.clear();

        Query query = db.collection(selectedStat)
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING);

        if(!selectedFilter.equals("All")){
            long startTime = getStartedTime(selectedFilter);
            query = query.whereGreaterThanOrEqualTo("timestamp", new Timestamp(new Date(startTime)));
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<BarEntry> entries = new ArrayList<>();
            int i = 0;
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Object valObj = doc.get("value");
                float value = 0f;
                if(valObj instanceof Number){
                    value = ((Number) valObj).floatValue();
                }
                entries.add(new BarEntry(i++, value));
            }

            if(entries.isEmpty()){
                barChartStats.invalidate();
                return;
            }

            BarDataSet dataSet = new BarDataSet(entries, selectedStat);
            dataSet.setColor(Color.parseColor("#FF6200EE"));
            BarData barData = new BarData(dataSet);
            barData.setBarWidth(0.9f);

            barChartStats.setData(barData);
            barChartStats.setFitBars(true);
            barChartStats.invalidate();
        });
    }

    private void addStats() {

    }
}
