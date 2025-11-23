package com.example.medilink.SearchDoc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.AppCache;
import com.example.medilink.ModelClass.Day;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.android.material.card.MaterialCardView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SearchDocFragment extends Fragment {

    RecyclerView rvGridAppointments, rvWeeklyCalendar;
    MaterialCardView mcvAppointment;
    TextView tvPrice, tvMonth;
    ImageButton btnPrevWeek, btnNextWeek;

    private LocalDate currentWeekStart;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_doc, container, false);

        rvGridAppointments = v.findViewById(R.id.rvGridAppointments);
        rvGridAppointments.setLayoutManager(new GridLayoutManager(getContext(), 1));

        rvWeeklyCalendar = v.findViewById(R.id.rvWeeklyCalendar);
        mcvAppointment = v.findViewById(R.id.mcvAppointment);
        tvPrice = v.findViewById(R.id.tvPrice);
        tvMonth = v.findViewById(R.id.tvMonth);
        btnPrevWeek = v.findViewById(R.id.btnPrevWeek);
        btnNextWeek = v.findViewById(R.id.btnNextWeek);

        fetchDoctors();
        return v;
    }

    private void fetchDoctors() {
        if(AppCache.getInstance().getLoadedData() == null || AppCache.getInstance().getLoadedData().doctors == null) {
            Toast.makeText(getContext(), "No cached doctor data found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<DoctorSchedule> doctorSchedules = AppCache.getInstance().getLoadedData().doctors;
        setUpUi(doctorSchedules);
    }

    private void setUpUi(List<DoctorSchedule> doctorSchedules) {
        AppointmentAdaptor adaptor = new AppointmentAdaptor(
                getContext(),
                doctorSchedules,
                doctor -> {
                    mcvAppointment.setVisibility(View.VISIBLE);
                    String setPrice = "PKR " + doctor.getPrice();
                    tvPrice.setText(setPrice);
                    initWeeklyCalendar(doctor);
                }
        );
        rvGridAppointments.setAdapter(adaptor);
    }

    private void initWeeklyCalendar(DoctorSchedule doctor) {
        currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        updateWeek(doctor);

        btnPrevWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            updateWeek(doctor);
        });

        btnNextWeek.setOnClickListener(v -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            updateWeek(doctor);
        });
    }

    private void updateWeek(DoctorSchedule doctor) {
        List<Day> days = new ArrayList<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            days.add(new Day(date, date.format(dayFormatter), date.getDayOfMonth()));
        }

        String set = currentWeekStart.getMonth().toString() + " " + currentWeekStart.getDayOfMonth()
                + " - " + currentWeekStart.plusDays(6).getMonth().toString() + " " + currentWeekStart.plusDays(6).getDayOfMonth();
        tvMonth.setText(set);

        WeeklyCalendarAdapter adapter = new WeeklyCalendarAdapter(days, doctor, requireContext());
        rvWeeklyCalendar.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvWeeklyCalendar.setAdapter(adapter);
    }
}