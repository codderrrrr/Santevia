package com.example.medilink.Doctors;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.BookedAppointment.VideoCall;
import com.example.medilink.ModelClass.Booking; // Using the new Booking model
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.List;

public class DoctorBookingFragment extends Fragment {

    private RecyclerView recyclerView;
    private DoctorBookingAdaptor adaptor;
    // Changed list type to the new Booking model
    private final List<Booking> bookingList = new ArrayList<>();
    private final String doctorId = FirebaseAuth.getInstance().getUid();
    private ListenerRegistration callListenerRegistration;
    private ListenerRegistration bookingListenerRegistration; // New listener for real-time bookings

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_doctor_booking, container, false);
        recyclerView = view.findViewById(R.id.rvGridAppointments);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // Adaptor initialized with the new Booking list
        adaptor = new DoctorBookingAdaptor(getContext(), bookingList);
        recyclerView.setAdapter(adaptor);

        loadDoctorBookings(); // Changed method name and logic
        listenForIncomingCalls();

        return view;
    }

    private void loadDoctorBookings() {
        if (doctorId == null) return;

        // Use a real-time listener for the doctor's bookings, ordered by appointment time
        bookingListenerRegistration = FirebaseFirestore.getInstance()
                .collection("doctors")
                .document(doctorId)
                .collection("bookings")
                .orderBy("appointmentTime", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w("DoctorBookingFragment", "Listen failed for bookings.", error);
                        Toast.makeText(getContext(), "Failed to load bookings.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        bookingList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots.getDocuments()) {
                            Booking booking = doc.toObject(Booking.class);
                            if (booking != null) {
                                // Optionally set the Booking document ID if needed elsewhere
                                // booking.setBookingId(doc.getId());
                                bookingList.add(booking);
                            }
                        }
                        adaptor.notifyDataSetChanged();
                    } else if (snapshots != null && snapshots.isEmpty()) {
                        bookingList.clear();
                        adaptor.notifyDataSetChanged();
                        Toast.makeText(getContext(), "You have no upcoming appointments.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void listenForIncomingCalls() {
        if (doctorId == null) return;

        callListenerRegistration = FirebaseFirestore.getInstance()
                .collection("Calls")
                .document(doctorId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) return;

                    if (snapshot.exists() && snapshot.contains("from")) {
                        String patientUID = snapshot.getString("from");
                        if (patientUID != null) {
                            String roomID = doctorId + "_" + patientUID;

                            snapshot.getReference().delete().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    PermissionX.init(requireActivity())
                                            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                            .request((allGranted, grantedList, deniedList) -> {
                                                if (allGranted) {
                                                    // Ensure VideoCall is imported correctly
                                                    Intent intent = new Intent(getContext(), VideoCall.class);
                                                    intent.putExtra("roomID", roomID);
                                                    intent.putExtra("otherUserID", patientUID);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(getContext(),
                                                            "Permissions denied. Cannot accept call.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
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
        if (bookingListenerRegistration != null) bookingListenerRegistration.remove(); // Remove booking listener
    }
}