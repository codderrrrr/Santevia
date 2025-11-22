package com.example.medilink.SearchDoc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.Day;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.ModelClass.Booking; // Import the new Booking model
import com.example.medilink.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class WeeklyCalendarAdapter extends RecyclerView.Adapter<WeeklyCalendarAdapter.ViewHolder> {

    private final List<Day> days;
    private final DoctorSchedule doctor;
    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public WeeklyCalendarAdapter(List<Day> days, DoctorSchedule doctor, Context context) {
        this.days = days;
        this.doctor = doctor;
        this.context = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName, tvDayDate;
        View slotIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayDate = itemView.findViewById(R.id.tvDayDate);
            slotIndicator = itemView.findViewById(R.id.slotIndicator);
        }
    }

    @NonNull
    @Override
    public WeeklyCalendarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_slot_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeklyCalendarAdapter.ViewHolder holder, int position) {
        Day day = days.get(position);
        holder.tvDayName.setText(day.getName());
        holder.tvDayDate.setText(String.valueOf(day.getNo()));

        // Check if the doctor works on this day (simplified check)
        java.time.LocalDate localDate = day.getDate();
        java.util.Date dayDate = java.util.Date.from(
                localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        );

        List<DoctorSchedule.PotentialSlot> potentialSlots = DoctorSchedule.generatePotentialSlotsForDay(dayDate);

        // Show indicator based on potential slots existence (simplified)
        holder.slotIndicator.setBackgroundResource(
                !potentialSlots.isEmpty() ? R.drawable.slot_available_dot : R.drawable.slot_unavailable_dot);

        holder.itemView.setOnClickListener(v -> showSlotsBottomSheet(day));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    @SuppressLint("SetTextI18n")
    private void showSlotsBottomSheet(Day day) {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(context);
        @SuppressLint("InflateParams") View sheetView = LayoutInflater.from(context)
                .inflate(R.layout.slot_bottom_sheet, null);
        bottomSheet.setContentView(sheetView);

        TextView tvSelectedDay = sheetView.findViewById(R.id.tvSelectedDay);
        RecyclerView rvSlots = sheetView.findViewById(R.id.rvSlots);

        // 1. Generate all potential hardcoded slots for the selected day
        java.time.LocalDate localDate = day.getDate();
        java.util.Date dayDate = java.util.Date.from(
                localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        );
        final List<DoctorSchedule.PotentialSlot> potentialSlots = DoctorSchedule.generatePotentialSlotsForDay(dayDate);

        if (potentialSlots.isEmpty()) {
            Toast.makeText(context, doctor.getName() + " is not available on " + day.getName(), Toast.LENGTH_SHORT).show();
            bottomSheet.dismiss();
            return;
        }

        tvSelectedDay.setText(day.getName() + ", " + day.getNo());

        // 2. Determine the time window for querying bookings
        Calendar calStart = Calendar.getInstance();

        calStart.setTime(dayDate);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);

        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.DAY_OF_YEAR, 1);

        // 3. Query the 'bookings' subcollection
        db.collection("doctors").document(doctor.getDocId())
                .collection("bookings")
                .whereGreaterThanOrEqualTo("appointmentTime", new Timestamp(calStart.getTime()))
                .whereLessThan("appointmentTime", new Timestamp(calEnd.getTime()))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 4. Mark slots as booked
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Booking booking = doc.toObject(Booking.class);
                        if (booking != null && booking.getAppointmentTime() != null) {
                            // We use the exact Timestamp (startTime) to check for a match
                            Date bookedTime = booking.getAppointmentTime().toDate();

                            for (DoctorSchedule.PotentialSlot slot : potentialSlots) {
                                if (slot.startTime.equals(bookedTime)) {
                                    slot.isBooked = true;
                                    break;
                                }
                            }
                        }
                    }

                    // 5. Setup and show slot adapter with updated availability
                    SlotAdapter slotAdapter = new SlotAdapter(potentialSlots, context, bottomSheet, doctor);
                    rvSlots.setLayoutManager(new LinearLayoutManager(context));
                    rvSlots.setAdapter(slotAdapter);
                    bottomSheet.show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to load availability: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("WeeklyCalendarAdapter", "Error loading bookings", e);
                    bottomSheet.dismiss();
                });
    }

    // --- SlotAdapter (Updated for new booking logic) ---
    private static class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {
        private final List<DoctorSchedule.PotentialSlot> slots;
        private final Context context;
        private final BottomSheetDialog dialog;
        private final DoctorSchedule doctor;
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();
        private final String userId = FirebaseAuth.getInstance().getUid();

        public SlotAdapter(List<DoctorSchedule.PotentialSlot> slots, Context context, BottomSheetDialog dialog, DoctorSchedule doctor) {
            this.slots = slots;
            this.context = context;
            this.dialog = dialog;
            this.doctor = doctor;
        }

        public static class SlotViewHolder extends RecyclerView.ViewHolder {
            TextView tvSlot;
            public SlotViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSlot = itemView.findViewById(R.id.tvSlot);
            }
        }

        @NonNull
        @Override
        public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.slot_item, parent, false);
            return new SlotViewHolder(view);
        }

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
        @Override
        public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
            DoctorSchedule.PotentialSlot slot = slots.get(position);

            // Display using the helper method from DoctorSchedule.PotentialSlot
            holder.tvSlot.setText(slot.getDisplayTime());

            // Color coding based on isBooked flag
            if (slot.isBooked) {
                holder.tvSlot.setBackgroundResource(R.drawable.slot_unavailable_bg);
                holder.tvSlot.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            } else {
                holder.tvSlot.setBackgroundResource(R.drawable.slot_available_bg);
                holder.tvSlot.setTextColor(context.getResources().getColor(android.R.color.black));
            }

            holder.tvSlot.setOnClickListener(v -> {
                if (slot.isBooked || userId == null) return;

                // --- NEW BOOKING LOGIC: TRANSACTION ---
                bookSlotTransaction(slot);
            });
        }

        private void bookSlotTransaction(DoctorSchedule.PotentialSlot slot) {

            // 1. Define the target appointment time (Timestamp is key)
            final Timestamp slotTimestamp = new Timestamp(slot.startTime);
            final DocumentReference newBookingRef = db.collection("doctors")
                    .document(doctor.getDocId())
                    .collection("bookings")
                    .document(); // Auto-ID

            // 2. Run the transaction
            db.runTransaction(transaction -> {
                // Check if a booking already exists for this exact time
                QuerySnapshot conflictSnapshot = null;
                try {
                    conflictSnapshot = (QuerySnapshot) Tasks.await(db.collection("doctors")
                            .document(doctor.getDocId())
                            .collection("bookings")
                            .whereEqualTo("appointmentTime", slotTimestamp)
                            .limit(1)
                            .get());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (conflictSnapshot.isEmpty()) {
                    // Slot is available, create the new booking document
                    Booking newBooking = new Booking(
                            slotTimestamp,
                            userId,
                            doctor.getDocId(),
                            30, // Hardcoded duration for now
                            Timestamp.now()
                    );

                    transaction.set(newBookingRef, newBooking);
                    return "SUCCESS";
                } else {
                    // Conflict found (another user booked it just now)
                    throw new FirebaseFirestoreException("Slot already booked.",
                            FirebaseFirestoreException.Code.ABORTED);
                }
            }).addOnSuccessListener(result -> {
                if (result.equals("SUCCESS")) {
                    Toast.makeText(context, "Slot booked: " + slot.getDisplayTime(), Toast.LENGTH_LONG).show();

                    // Update UI locally immediately
                    slot.isBooked = true;
                    notifyDataSetChanged();
                    dialog.dismiss();
                }
            }).addOnFailureListener(e -> {
                if (e instanceof FirebaseFirestoreException && ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.ABORTED) {
                    Toast.makeText(context, "Slot was just booked by someone else. Please refresh.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "Booking failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                Log.e("SlotAdapter", "Booking Transaction Failed", e);
            });
        }


        @Override
        public int getItemCount() {
            return slots.size();
        }
    }
}