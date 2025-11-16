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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
public class StatisticsFragment extends Fragment {

    private MaterialCardView cardBloodPressure, cardPulse, cardHeartRate;
    private LineChart lineChartStats;
    private MaterialCardView cardStatInfo;
    private FirebaseFirestore db;
    private String currentUserId;
    private String selectedStat = "Pulse";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statistics, container, false);

        cardBloodPressure = v.findViewById(R.id.cardBloodPressure);
        cardPulse = v.findViewById(R.id.cardPulse);
        cardHeartRate = v.findViewById(R.id.cardHeartRate);
        lineChartStats = v.findViewById(R.id.lineChartStats);
        cardStatInfo = v.findViewById(R.id.CardStatInfo);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupChart();

        cardBloodPressure.setOnClickListener(view -> {
            selectedStat = "BloodPressure";
            cardStatInfo.setVisibility(View.VISIBLE);
            loadLineChart();
        });

        cardPulse.setOnClickListener(view -> {
            selectedStat = "Pulse";
            cardStatInfo.setVisibility(View.VISIBLE);
            loadLineChart();
        });

        cardHeartRate.setOnClickListener(view -> {
            selectedStat = "HeartRate";
            cardStatInfo.setVisibility(View.VISIBLE);
            loadLineChart();
        });

        loadLineChart();
        return v;
    }

    private void setupChart() {
        lineChartStats.setDragEnabled(true);
        lineChartStats.setScaleEnabled(true);
        lineChartStats.setPinchZoom(true);
        lineChartStats.getDescription().setEnabled(false);

        XAxis xAxis = lineChartStats.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = lineChartStats.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        lineChartStats.getAxisRight().setEnabled(false);
    }

    private void loadLineChart() {
        db.collection(selectedStat)
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Entry> entries = new ArrayList<>();
                    List<String> labels = new ArrayList<>();

                    int index = 0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        float value = ((Number) doc.get("value")).floatValue();
                        entries.add(new Entry(index, value));
                        Timestamp ts = doc.getTimestamp("timestamp");
                        labels.add(ts != null ? ts.toDate().toString() : "");
                        index++;
                    }

                    LineDataSet dataSet = new LineDataSet(entries, selectedStat);
                    dataSet.setColor(Color.parseColor("#FF6200EE"));
                    dataSet.setCircleColor(Color.WHITE);
                    dataSet.setLineWidth(2f);
                    dataSet.setCircleRadius(3f);
                    dataSet.setDrawValues(false);
                    dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

                    LineData lineData = new LineData(dataSet);
                    lineChartStats.setData(lineData);

                    lineChartStats.getXAxis().setValueFormatter(new ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int i = (int) value;
                            if (i >= 0 && i < labels.size()) return labels.get(i);
                            else return "";
                        }
                    });

                    lineChartStats.invalidate();
                });
    }
}
