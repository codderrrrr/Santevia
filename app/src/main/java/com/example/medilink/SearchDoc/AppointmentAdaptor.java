package com.example.medilink.SearchDoc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;

import java.util.List;

public class AppointmentAdaptor extends RecyclerView.Adapter<AppointmentAdaptor.ViewHolder> {
    public interface OnDoctorClickListener {
        void onDoctorClick(DoctorSchedule doctor);
    }

    private final Context context;
    private final List<DoctorSchedule> doctorSchedules;
    private final OnDoctorClickListener listener;

    public AppointmentAdaptor(Context context, List<DoctorSchedule> doctorSchedules, OnDoctorClickListener listener) {
        this.context = context;
        this.doctorSchedules = doctorSchedules;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDoctor, btnPhoneNo;
        TextView tvName, tvSpecialization, tvEducation, tvHospital, tvPhoneNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDoctor = itemView.findViewById(R.id.ivDoctor);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            tvEducation = itemView.findViewById(R.id.tvEducation);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvPhoneNo = itemView.findViewById(R.id.tvPhoneNo);
            btnPhoneNo = itemView.findViewById(R.id.btnPhoneNo);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.appointment_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorSchedule doctor = doctorSchedules.get(position);

        holder.ivDoctor.setImageResource(R.drawable.profile);
        holder.tvName.setText(doctor.getName());
        holder.tvSpecialization.setText(doctor.getSpecialization());
        holder.tvEducation.setText(doctor.getEducation().toUpperCase());
        holder.tvHospital.setText(doctor.gethospital());
        holder.tvPhoneNo.setText("(+92) " + doctor.getPhoneNo());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDoctorClick(doctor);
            }
        });

        holder.btnPhoneNo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + doctor.getPhoneNo()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return doctorSchedules.size();
    }
}
