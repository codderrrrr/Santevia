package com.example.medilink.Stats;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.medilink.R;

public class StatisticsFragment extends Fragment {
    Spinner spinnerStats;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_statistics, container, false);
        spinnerStats = v.findViewById(R.id.spinnerStats);
        String[] options = {"All", "Day", "Week", "Month", "Year"};
        ArrayAdapter<String> adaptor = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                options
        );
        adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStats.setAdapter(adaptor);

        spinnerStats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }
}