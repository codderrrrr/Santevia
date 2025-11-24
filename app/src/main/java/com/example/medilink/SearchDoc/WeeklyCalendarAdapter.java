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
import com.example.medilink.ModelClass.Booking;
import com.example.medilink.R;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_slot_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeeklyCalendarAdapter.ViewHolder holder, int position) {
        Day day = days.get(position);
        holder.tvDayName.setText(day.getName());
        holder.tvDayDate.setText(String.valueOf(day.getNo()));

        LocalDate localDate = day.getDate();
        Date dayDate = Date.from(
                localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        );

        List<DoctorSchedule.PotentialSlot> potentialSlots = DoctorSchedule.generatePotentialSlotsForDay(dayDate);
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
        View sheetView = LayoutInflater.from(context).inflate(R.layout.slot_bottom_sheet, null);
        bottomSheet.setContentView(sheetView);

        TextView tvSelectedDay = sheetView.findViewById(R.id.tvSelectedDay);
        RecyclerView rvSlots = sheetView.findViewById(R.id.rvSlots);

        LocalDate localDate = day.getDate();
        Date dayDate = Date.from(
                localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
        );
        final List<DoctorSchedule.PotentialSlot> potentialSlots = DoctorSchedule.generatePotentialSlotsForDay(dayDate);

        if (potentialSlots.isEmpty()) {
            Toast.makeText(context, doctor.getName() + " is not available on " + day.getName(), Toast.LENGTH_SHORT).show();
            bottomSheet.dismiss();
            return;
        }

        tvSelectedDay.setText(day.getName() + ", " + day.getNo());

        Calendar calStart = Calendar.getInstance();

        calStart.setTime(dayDate);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);

        Calendar calEnd = (Calendar) calStart.clone();
        calEnd.add(Calendar.DAY_OF_YEAR, 1);

        db.collection("doctors").document(doctor.getDocId())
                .collection("bookings")
                .whereGreaterThanOrEqualTo("appointmentTime", new Timestamp(calStart.getTime()))
                .whereLessThan("appointmentTime", new Timestamp(calEnd.getTime()))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Booking booking = doc.toObject(Booking.class);
                        if (booking != null && booking.getAppointmentTime() != null) {
                            Date bookedTime = booking.getAppointmentTime().toDate();

                            for (DoctorSchedule.PotentialSlot slot : potentialSlots) {
                                if (slot.startTime.equals(bookedTime)) {
                                    slot.isBooked = true;
                                    break;
                                }
                            }
                        }
                    }

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

            holder.tvSlot.setText(slot.getDisplayTime());

            if (slot.isBooked) {
                holder.tvSlot.setBackgroundResource(R.drawable.slot_unavailable_bg);
                holder.tvSlot.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            } else {
                holder.tvSlot.setBackgroundResource(R.drawable.slot_available_bg);
                holder.tvSlot.setTextColor(context.getResources().getColor(android.R.color.black));
            }

            holder.tvSlot.setOnClickListener(v -> {
                if (slot.isBooked || userId == null) return;

                bookSlotTransaction(slot);
            });
        }

        private void bookSlotTransaction(DoctorSchedule.PotentialSlot slot) {

            final Timestamp slotTimestamp = new Timestamp(slot.startTime);
            final DocumentReference newBookingRef = db.collection("doctors")
                    .document(doctor.getDocId())
                    .collection("bookings")
                    .document();

            db.runTransaction(transaction -> {
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
                    Booking newBooking = new Booking(
                            slotTimestamp,
                            userId,
                            doctor.getDocId(),
                            30,
                            Timestamp.now()
                    );
                    transaction.set(newBookingRef, newBooking);
                    return "SUCCESS";
                } else {
                    throw new FirebaseFirestoreException("Slot already booked.",
                            FirebaseFirestoreException.Code.ABORTED);
                }
            }).addOnSuccessListener(result -> {
                if (result.equals("SUCCESS")) {
                    Toast.makeText(context, "Slot booked: " + slot.getDisplayTime(), Toast.LENGTH_LONG).show();
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
            });
        }

        @Override
        public int getItemCount() {
            return slots.size();
        }
    }
}