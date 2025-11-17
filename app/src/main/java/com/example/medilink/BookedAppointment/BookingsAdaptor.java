package com.example.medilink.BookedAppointment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;

import java.util.List;

public class BookingsAdaptor extends RecyclerView.Adapter<BookingsAdaptor.ViewHolder> {

    private final Context context;
    private List<DoctorSchedule> list;

    public BookingsAdaptor(Context context, List<DoctorSchedule> list) {
        this.context = context;
        this.list = list;
    }

    public void updateList(List<DoctorSchedule> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpecialization, tvHospital, tvBookedSlot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvBookedSlot = itemView.findViewById(R.id.tvBookedSlot);
        }
    }

    @NonNull
    @Override
    public BookingsAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.appointment_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BookingsAdaptor.ViewHolder holder, int position) {
        DoctorSchedule doctor = list.get(position);
        holder.tvName.setText(doctor.getName() != null ? doctor.getName() : "Unknown");
        holder.tvSpecialization.setText(doctor.getSpecialization() != null ? doctor.getSpecialization() : "Unknown");
        holder.tvHospital.setText(doctor.gethospital() != null ? doctor.gethospital() : "Unknown");

        if (doctor.getSchedule() != null && !doctor.getSchedule().isEmpty()) {
            DoctorSchedule.Slots slot = doctor.getSchedule().get(0);
            if (slot != null) {
                holder.tvBookedSlot.setText(
                        slot.getDay() + "  •  " + slot.getStart() + " - " + slot.getEnd()
                );
            } else {
                holder.tvBookedSlot.setText("No Active Booking");
            }
        } else {
            holder.tvBookedSlot.setText("No Active Booking");
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }
}
