package com.example.medilink.Stats;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medilink.AppCache;
import com.example.medilink.ModelClass.BloodPressure;
import com.example.medilink.ModelClass.Pulse;
import com.example.medilink.ModelClass.HeartRate;
import com.example.medilink.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private LineChart lineChart;
    private MaterialCardView cardBP, cardPulse, cardHeartRate, cardStatInfo;
    private TextView tvStatName, tvMax, tvMin, tvMedian, tvMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        lineChart = view.findViewById(R.id.lineChartStats);
        cardBP = view.findViewById(R.id.cardBloodPressure);
        cardPulse = view.findViewById(R.id.cardPulse);
        cardHeartRate = view.findViewById(R.id.cardHeartRate);
        cardStatInfo = view.findViewById(R.id.CardStatInfo);

        // Initialize TextViews inside the stat info card
        tvStatName = view.findViewById(R.id.tvStatName);
        tvMax = view.findViewById(R.id.tvMax);
        tvMin = view.findViewById(R.id.tvMin);
        tvMedian = view.findViewById(R.id.tvMedian);
        tvMode = view.findViewById(R.id.tvMode);

        setupChart();

        // Default chart: Blood Pressure
        loadData("BloodPressure");

        // Switch datasets when cards clicked
        cardBP.setOnClickListener(v -> loadData("BloodPressure"));
        cardPulse.setOnClickListener(v -> loadData("Pulse"));
        cardHeartRate.setOnClickListener(v -> loadData("HeartRate"));

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

        // Get current user ID from FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = currentUser.getUid();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1); // yearly data
        long oneYearAgoMillis = cal.getTimeInMillis();

        switch (type) {
            case "BloodPressure":
                List<BloodPressure> bpData = AppCache.getInstance().getLoadedData() != null ?
                        AppCache.getInstance().getLoadedData().bloodPressures : new ArrayList<>();
                for (BloodPressure bp : bpData) {
                    if (bp.getUserId().equals(currentUserId) &&
                            bp.getTimestamp().toDate().getTime() >= oneYearAgoMillis) {
                        float value = bp.getValue();
                        entries.add(new Entry(entries.size(), value));
                        values.add(value);
                    }
                }
                break;

            case "Pulse":
                List<Pulse> pulseData = AppCache.getInstance().getLoadedData() != null ?
                        AppCache.getInstance().getLoadedData().pulses : new ArrayList<>();
                for (Pulse p : pulseData) {
                    if (p.getUserId().equals(currentUserId) &&
                            p.getTimestamp().toDate().getTime() >= oneYearAgoMillis) {
                        float value = p.getValue();
                        entries.add(new Entry(entries.size(), value));
                        values.add(value);
                    }
                }
                break;

            case "HeartRate":
                List<HeartRate> hrData = AppCache.getInstance().getLoadedData() != null ?
                        AppCache.getInstance().getLoadedData().heartRates : new ArrayList<>();
                for (HeartRate hr : hrData) {
                    if (hr.getUserId().equals(currentUserId) &&
                            hr.getTimestamp().toDate().getTime() >= oneYearAgoMillis) {
                        float value = hr.getValue();
                        entries.add(new Entry(entries.size(), value));
                        values.add(value);
                    }
                }
                break;
        }

        if (entries.isEmpty()) {
            lineChart.clear();
            cardStatInfo.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No data available for the last year", Toast.LENGTH_SHORT).show();
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, type);
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

        // Show stat summary
        showStatSummary(type, values);
    }

    @SuppressLint("SetTextI18n")
    private void showStatSummary(String statName, List<Float> values) {
        if (values.isEmpty()) {
            cardStatInfo.setVisibility(View.GONE);
            return;
        }

        Collections.sort(values);

        // Max, Min
        float max = Collections.max(values);
        float min = Collections.min(values);

        // Median
        float median = values.size() % 2 == 1 ?
                values.get(values.size() / 2) :
                (values.get(values.size() / 2 - 1) + values.get(values.size() / 2)) / 2f;

        // Mode
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

        // Display in TextViews
        cardStatInfo.setVisibility(View.VISIBLE);
        tvStatName.setText(statName);
        tvMax.setText(max + "");
        tvMin.setText(min + "");
        tvMedian.setText("" + median);
        tvMode.setText("" + mode);
    }
}
