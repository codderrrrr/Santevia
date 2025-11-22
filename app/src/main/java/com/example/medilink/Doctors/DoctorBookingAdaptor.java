package com.example.medilink.Doctors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.BookedAppointment.ChatActivity;
import com.example.medilink.ModelClass.Booking; // Use the new Booking model
import com.example.medilink.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoctorBookingAdaptor extends RecyclerView.Adapter<DoctorBookingAdaptor.ViewHolder> {

    private final Context context;
    // Changed list type to the new Booking model
    private final List<Booking> bookingList;

    public DoctorBookingAdaptor(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doctorappointmentlist, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        String patientUID = booking.getBookedByUserId();
        Timestamp appointmentTimestamp = booking.getAppointmentTime();

        if (appointmentTimestamp != null) {
            Date date = appointmentTimestamp.toDate();
            // Format the Timestamp to display the appointment time and date
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM | hh:mm a", Locale.getDefault());

            holder.tvBookedSlot.setText(dateFormat.format(date));
        } else {
            holder.tvBookedSlot.setText("Time Unknown");
        }

        holder.ivChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("otherUserID", patientUID);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookedSlot;
        ImageView ivChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookedSlot = itemView.findViewById(R.id.tvBookedSlot);
            ivChat = itemView.findViewById(R.id.ivChat);
        }
    }
}