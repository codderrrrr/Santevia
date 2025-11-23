package com.example.medilink.BookedAppointment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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
import com.example.medilink.BookedAppointment.HomeFragment.AppointmentSummary;

public class BookingsAdaptor extends RecyclerView.Adapter<BookingsAdaptor.ViewHolder> {

    private final Context context;
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
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialization = itemView.findViewById(R.id.tvSpecialization);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvBookedSlot = itemView.findViewById(R.id.tvBookedSlot);
            ivCall = itemView.findViewById(R.id.ivCall);
            ivChat = itemView.findViewById(R.id.ivChat);
            btnCancel = itemView.findViewById(R.id.btnCancel);
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
        AppointmentSummary summary = list.get(position);
        DoctorSchedule doctor = summary.doctor;
        Timestamp appointmentTime = summary.booking.getAppointmentTime();

        holder.tvName.setText(doctor.getName());
        holder.tvSpecialization.setText(doctor.getSpecialization());
        holder.tvHospital.setText(doctor.gethospital());

        Date date = appointmentTime.toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM hh:mm a", Locale.getDefault());
        holder.tvBookedSlot.setText(dateFormat.format(date));

        holder.btnCancel.setOnClickListener(v -> {
            String userId = summary.booking.getBookedByUserId();
            String doctorId = summary.booking.getDoctorId();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("doctors").document(doctorId)
                    .collection("bookings")
                    .whereEqualTo("bookedByUserId", userId)
                    .whereEqualTo("appointmentTime", appointmentTime)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                            db.collection("doctors").document(doctorId)
                                    .collection("bookings")
                                    .document(doc.getId())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(context, "Booking removed", Toast.LENGTH_SHORT).show();
                                        list.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, list.size());
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(context, "Booking not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

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
            String senderID = FirebaseAuth.getInstance().getUid();
            String receiverID = doctor.getDocId();

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("senderID", senderID);
            intent.putExtra("receiverID", receiverID);
            context.startActivity(intent);
        });
    }

    private void startVideoCall(String docID) {

        String patientUID = FirebaseAuth.getInstance().getUid();
        if (patientUID == null) return;

        String roomID = docID + "_" + patientUID;

        FirebaseFirestore.getInstance()
                .collection("Calls")
                .document(docID)
                .collection("incomingCall")
                .document("call")
                .set(new HashMap<String, Object>() {{
                    put("from", patientUID);
                    put("roomID", roomID);
                    put("timestamp", System.currentTimeMillis());
                }});

        Intent intent = new Intent(context, VideoCall.class);
        intent.putExtra("roomID", roomID);
        intent.putExtra("otherUserID", docID);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }
}