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
import com.example.medilink.ModelClass.Booking;
import com.example.medilink.ModelClass.Patient;
import com.example.medilink.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoctorBookingAdaptor extends RecyclerView.Adapter<DoctorBookingAdaptor.ViewHolder> {

    private final Context context;
    private final List<Booking> bookingList;
    private final Map<String, Patient> patientMap = new HashMap<>();

    public DoctorBookingAdaptor(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;

        preloadPatients();
    }

    private void preloadPatients() {
        FirebaseFirestore.getInstance().collection("Patients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Patient p = doc.toObject(Patient.class);
                        if (p != null) {
                            patientMap.put(doc.getId(), p); // document ID is patient ID
                        }
                    }
                    notifyDataSetChanged(); // Refresh RecyclerView
                })
                .addOnFailureListener(e -> e.printStackTrace());
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
        String patientId = booking.getBookedByUserId();
        Timestamp appointmentTimestamp = booking.getAppointmentTime();

        if (appointmentTimestamp != null) {
            Date date = appointmentTimestamp.toDate();
            SimpleDateFormat slotFormat = new SimpleDateFormat("EEEE, dd MMM | hh:mm a", Locale.getDefault());
            holder.tvBookedSlot.setText(slotFormat.format(date));

            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());

            holder.tvDay.setText(dayFormat.format(date));
            holder.tvDate.setText(dateFormat.format(date));
            holder.tvMonth.setText(monthFormat.format(date));
        } else {
            holder.tvBookedSlot.setText("Time Unknown");
            holder.tvDay.setText("--");
            holder.tvDate.setText("--");
            holder.tvMonth.setText("---");
        }

        Patient patient = patientMap.get(patientId);
        if (patient != null) {
            holder.tvName.setText(patient.getName());
            holder.tvGender.setText(patient.getGender());
            holder.tvAge.setText(patient.getAge());
            holder.tvPhoneNo.setText(patient.getPhoneNo());
        } else {
            holder.tvName.setText("Unknown Patient");
            holder.tvGender.setText("");
            holder.tvAge.setText("");
            holder.tvPhoneNo.setText("");
        }

        holder.ivChat.setOnClickListener(v -> {
            String senderID = FirebaseAuth.getInstance().getUid();
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("senderID", senderID);
            intent.putExtra("receiverID", patientId);
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookedSlot, tvName, tvGender, tvAge, tvPhoneNo, tvDay, tvDate, tvMonth;
        ImageView ivChat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookedSlot = itemView.findViewById(R.id.tvBookedSlot);
            ivChat = itemView.findViewById(R.id.ivChat);
            tvName = itemView.findViewById(R.id.tvName);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvPhoneNo = itemView.findViewById(R.id.tvPhoneNo);
            tvMonth = itemView.findViewById(R.id.tvMonth);
        }
    }
}
