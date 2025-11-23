package com.example.medilink.Stats;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medilink.ModelClass.Sleep;
import com.example.medilink.ModelClass.WaterIntake;
import com.example.medilink.ModelClass.Weight;
import com.example.medilink.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private LineChart lineChart;
    private MaterialCardView cardWeight, cardWater, cardSleep, cardStatInfo;
    private TextView tvStatName, tvMax, tvMin, tvMedian, tvMode;
    private ImageButton btnAdd;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Views
        lineChart = view.findViewById(R.id.lineChartStats);
        cardWeight = view.findViewById(R.id.cardWeight);
        cardWater = view.findViewById(R.id.cardWater);
        cardSleep = view.findViewById(R.id.cardSleep);
        cardStatInfo = view.findViewById(R.id.CardStatInfo);
        btnAdd = view.findViewById(R.id.btnAdd);

        tvStatName = view.findViewById(R.id.tvStatName);
        tvMax = view.findViewById(R.id.tvMax);
        tvMin = view.findViewById(R.id.tvMin);
        tvMedian = view.findViewById(R.id.tvMedian);
        tvMode = view.findViewById(R.id.tvMode);

        setupChart();

        loadData("weight");

        // Card Click Listeners
        cardWeight.setOnClickListener(v -> loadData("weight"));
        cardWater.setOnClickListener(v -> loadData("water"));
        cardSleep.setOnClickListener(v -> loadData("sleep"));

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), StatsInput.class);
            startActivity(intent);
        });

        return view;
    }


    private void setupChart() {
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setBackgroundColor(getResources().getColor(R.color.DarkBlue));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);

        lineChart.getAxisRight().setEnabled(false);
    }

    private void loadData(String type) {
        List<Entry> entries = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        String userId = currentUser.getUid();

        CollectionReference ref = db.collection("users")
                .document(userId)
                .collection("stats")
                .document(type)
                .collection("data");

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        Timestamp oneYearAgo = new Timestamp(cal.getTime());

        ref.whereGreaterThan("timestamp", oneYearAgo)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        createStatsCollectionIfMissing(type, ref);
                        lineChart.clear();
                        cardStatInfo.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No data available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Map of week -> list of values
                    Map<Integer, List<Float>> weekMap = new HashMap<>();
                    Calendar tempCal = Calendar.getInstance();

                    for (var doc : querySnapshot.getDocuments()) {
                        Timestamp ts = doc.getTimestamp("timestamp");
                        if (ts == null) continue;
                        tempCal.setTime(ts.toDate());
                        int week = tempCal.get(Calendar.WEEK_OF_YEAR);

                        float value = 0;
                        switch (type) {
                            case "weight":
                                Weight w = doc.toObject(Weight.class);
                                if (w != null) value = (float) w.getWeight_kg();
                                break;
                            case "water":
                                WaterIntake wt = doc.toObject(WaterIntake.class);
                                if (wt != null) value = (float) wt.getValue() / 1000f; // liters
                                break;
                            case "sleep":
                                Sleep sl = doc.toObject(Sleep.class);
                                if (sl != null) value = (float) sl.getHours();
                                break;
                        }

                        if (!weekMap.containsKey(week)) weekMap.put(week, new ArrayList<>());
                        weekMap.get(week).add(value);
                    }

                    // Aggregate weekly averages
                    List<Integer> sortedWeeks = new ArrayList<>(weekMap.keySet());
                    Collections.sort(sortedWeeks);

                    for (int week : sortedWeeks) {
                        List<Float> weekValues = weekMap.get(week);
                        float sum = 0;
                        for (float v : weekValues) sum += v;
                        float avg = sum / weekValues.size();
                        entries.add(new Entry(week, avg));
                        values.add(avg);
                    }

                    if (entries.isEmpty()) {
                        lineChart.clear();
                        cardStatInfo.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No data available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showChart(entries);
                    showStatSummary(type, values);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading stats", Toast.LENGTH_SHORT).show());
    }


    private void createStatsCollectionIfMissing(String type, CollectionReference ref) {
        Map<String, Object> dummy = new HashMap<>();
        dummy.put("timestamp", Timestamp.now());
        dummy.put("value", 0);
        ref.document("init").set(dummy);
    }

    private void showChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setColor(Color.GREEN);
        dataSet.setDrawCircles(false);
        dataSet.setLineWidth(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.GREEN);
        dataSet.setFillAlpha(80);

        lineChart.setData(new LineData(dataSet));
        lineChart.invalidate();
    }

    @SuppressLint("SetTextI18n")
    private void showStatSummary(String statName, List<Float> values) {
        if (values.isEmpty()) {
            cardStatInfo.setVisibility(View.GONE);
            return;
        }

        Collections.sort(values);

        float max = Collections.max(values);
        float min = Collections.min(values);

        float median = values.size() % 2 == 1 ?
                values.get(values.size() / 2) :
                (values.get(values.size() / 2 - 1) + values.get(values.size() / 2)) / 2f;

        Map<Float, Integer> freq = new HashMap<>();
        float mode = values.get(0);
        int maxCount = 0;
        for (Float v : values) {
            int count = freq.getOrDefault(v, 0) + 1;
            freq.put(v, count);
            if (count > maxCount) {
                maxCount = count;
                mode = v;
            }
        }

        cardStatInfo.setVisibility(View.VISIBLE);
        tvStatName.setText(statName.substring(0,1).toUpperCase() + statName.substring(1));
        tvMax.setText(String.format("%.1f", max));
        tvMin.setText(String.format("%.1f", min));
        tvMedian.setText(String.format("%.1f", median));
        tvMode.setText(String.format("%.1f", mode));
    }
}
