package com.example.medilink.SearchDoc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.Day;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;

import java.util.List;

public class ScheduleAdaptor extends RecyclerView.Adapter<ScheduleAdaptor.ViewHolder> {
    private final List<Day> days;
    private final DoctorSchedule doctor; // Added doctor
    private final onScheduleClickListener listener;

    public interface onScheduleClickListener {
        void onScheduleClick(Day day, DoctorSchedule doctor);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }

    public ScheduleAdaptor(List<Day> days, DoctorSchedule doctor, onScheduleClickListener listener) {
        this.days = days;
        this.doctor = doctor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdaptor.ViewHolder holder, int position) {
        Day day = days.get(position);
        holder.tvDayNumber.setText(String.valueOf(day.getNo()));
        holder.itemView.setOnClickListener(v -> listener.onScheduleClick(day, doctor));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
}
