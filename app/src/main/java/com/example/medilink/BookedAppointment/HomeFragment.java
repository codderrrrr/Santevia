package com.example.medilink.BookedAppointment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.medilink.R;

public class HomeFragment extends Fragment {
    RecyclerView rvGridAppointments;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        rvGridAppointments = v.findViewById(R.id.rvGridAppointments);
        rvGridAppointments.setLayoutManager(new GridLayoutManager((getContext(), 2)));

        fetchDoctors();
        return v;
    }
}