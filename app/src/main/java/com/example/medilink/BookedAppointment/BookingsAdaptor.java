package com.example.medilink.BookedAppointment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.permissionx.guolindev.PermissionX;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

// Import the new AppointmentSummary class
import com.example.medilink.BookedAppointment.HomeFragment.AppointmentSummary;

public class BookingsAdaptor extends RecyclerView.Adapter<BookingsAdaptor.ViewHolder> {

    private final Context context;
    // Change list type to the new summary class
    private List<AppointmentSummary> list;

    public BookingsAdaptor(Context context, List<AppointmentSummary> list) {
        this.context = context;
        this.list = list;
    }

    public void updateList(List<AppointmentSummary> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpecialization, tvHospital, tvBookedSlot;
        ImageView ivCall, ivChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvBookedSlot = itemView.findViewById(R.id.tvBookedSlot);
            ivCall = itemView.findViewById(R.id.ivCall);
            ivChat = itemView.findViewById(R.id.ivChat);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.appointment_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the summary object
        AppointmentSummary summary = list.get(position);
        DoctorSchedule doctor = summary.doctor;
        Timestamp appointmentTime = summary.booking.getAppointmentTime();

        holder.tvName.setText(doctor.getName() != null ? doctor.getName() : "Unknown");
        holder.tvSpecialization.setText(doctor.getSpecialization() != null ? doctor.getSpecialization() : "Unknown");
        holder.tvHospital.setText(doctor.gethospital() != null ? doctor.gethospital() : "Unknown");

        // Format the Timestamp for display
        if (appointmentTime != null) {
            Date date = appointmentTime.toDate();
            // Example format: Monday, 25 Nov 09:30 AM
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM hh:mm a", Locale.getDefault());

            holder.tvBookedSlot.setText(dateFormat.format(date));
        } else {
            holder.tvBookedSlot.setText("Time not available");
        }

        holder.ivCall.setOnClickListener(v -> {
            PermissionX.init((FragmentActivity) context)
                    .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            if (doctor.getDocId() != null && !doctor.getDocId().isEmpty()) {
                                startVideoCall(doctor.getDocId());
                            } else {
                                Toast.makeText(context, "Doctor ID not available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(context, "Permissions denied: " + deniedList, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        holder.ivChat.setOnClickListener(v -> {
            if (doctor.getDocId() != null && doctor.getName() != null) {
                Intent intent = new Intent(context, ChatActivity.class); // Assume ChatActivity path
                intent.putExtra("otherUserID", doctor.getDocId());
                intent.putExtra("otherUserName", doctor.getName());
                context.startActivity(intent);
            }
        });
    }

    private void startVideoCall(String docID) {
        String patientUID = FirebaseAuth.getInstance().getUid();
        if (patientUID == null) return;

        String roomID = docID + "_" + patientUID;

        // Notify the doctor about the incoming call
        FirebaseFirestore.getInstance().collection("Calls")
                .document(docID)
                .set(new HashMap<String, Object>() {{
                    put("from", patientUID);
                    put("timestamp", System.currentTimeMillis());
                }});

        // Start the call for the patient
        Intent intent = new Intent(context, VideoCall.class); // Assume VideoCall path
        intent.putExtra("roomID", roomID);
        intent.putExtra("otherUserID", docID);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }
}