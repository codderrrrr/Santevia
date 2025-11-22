package com.example.medilink.BookedAppointment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.AppCache;
import com.example.medilink.ModelClass.Booking;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends Fragment {

    private RecyclerView rvGridAppointments;
    // Adaptor now accepts a list of AppointmentSummary objects
    private BookingsAdaptor adaptor;
    private ListenerRegistration callListenerRegistration;
    private FirebaseFirestore db;
    private String currentUserId;

    // Helper class to combine Doctor details and Booking details for the adapter
    public static class AppointmentSummary {
        public DoctorSchedule doctor;
        public Booking booking;

        public AppointmentSummary(DoctorSchedule doctor, Booking booking) {
            this.doctor = doctor;
            this.booking = booking;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        rvGridAppointments = v.findViewById(R.id.rvGridAppointments);
        rvGridAppointments.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // Adaptor initialized with the new summary class
        adaptor = new BookingsAdaptor(getContext(), new ArrayList<>());
        rvGridAppointments.setAdapter(adaptor);

        loadBookedAppointments();
        listenForIncomingCalls();

        return v;
    }

    private void loadBookedAppointments() {
        if (currentUserId == null || getContext() == null) return;

        List<DoctorSchedule> cachedDoctors = AppCache.getInstance().getLoadedData() != null ?
                AppCache.getInstance().getLoadedData().doctors : new ArrayList<>();

        if (cachedDoctors.isEmpty()) {
            Toast.makeText(getContext(), "Doctor data not loaded. Check connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Task<QuerySnapshot>> bookingTasks = new ArrayList<>();

        // 1. Query the 'bookings' subcollection for EACH doctor
        for (DoctorSchedule doctor : cachedDoctors) {
            if (doctor.getDocId() != null) {
                // Find all bookings for this doctor where 'bookedByUserId' is the current user
                Task<QuerySnapshot> task = db.collection("doctors").document(doctor.getDocId())
                        .collection("bookings")
                        .whereEqualTo("bookedByUserId", currentUserId)
                        .get();
                bookingTasks.add(task);
            }
        }

        // 2. Wait for ALL queries to complete
        Tasks.whenAllSuccess(bookingTasks)
                .addOnSuccessListener(results -> {
                    List<AppointmentSummary> appointments = new ArrayList<>();
                    int doctorIndex = 0;

                    for (Object result : results) {
                        QuerySnapshot snapshot = (QuerySnapshot) result;
                        DoctorSchedule doctor = cachedDoctors.get(doctorIndex);

                        // 3. Combine Doctor details with Booking details
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            Booking booking = document.toObject(Booking.class);
                            if (booking != null) {
                                appointments.add(new AppointmentSummary(doctor, booking));
                            }
                        }
                        doctorIndex++;
                    }

                    if (!appointments.isEmpty()) {
                        adaptor.updateList(appointments);
                    } else {
                        Toast.makeText(getContext(), "No booked appointments found", Toast.LENGTH_SHORT).show();
                        adaptor.updateList(new ArrayList<>()); // Clear old list
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("HomeFragment", "Booking load error", e);
                });
    }

    private void listenForIncomingCalls() {
        // ... (Call listener logic remains unchanged as it uses 'Calls' collection)
        String patientUID = FirebaseAuth.getInstance().getUid();
        if (patientUID == null) return;

        callListenerRegistration = FirebaseFirestore.getInstance()
                .collection("Calls")
                .document(patientUID)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;

                    if (snapshot.exists() && snapshot.contains("from")) {
                        String doctorUID = snapshot.getString("from");
                        if (doctorUID != null) {
                            String roomID = doctorUID + "_" + patientUID;

                            snapshot.getReference().delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Incoming call from Doctor", Toast.LENGTH_SHORT).show();
                                    // Make sure VideoCall class path is correct
                                    Intent intent = new Intent(getContext(), VideoCall.class);
                                    intent.putExtra("roomID", roomID);
                                    intent.putExtra("otherUserID", doctorUID);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (callListenerRegistration != null) callListenerRegistration.remove();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookedAppointments();
        listenForIncomingCalls();
    }
}