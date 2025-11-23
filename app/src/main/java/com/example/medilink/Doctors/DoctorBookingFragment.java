package com.example.medilink.Doctors;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.medilink.ModelClass.Booking;
import com.example.medilink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.List;

public class DoctorBookingFragment extends Fragment {

    private RecyclerView recyclerView;
    private DoctorBookingAdaptor adaptor;
    private final List<Booking> bookingList = new ArrayList<>();
    private String doctorId;

    private ListenerRegistration callListenerRegistration;
    private ListenerRegistration bookingListenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_doctor_booking, container, false);

        doctorId = FirebaseAuth.getInstance().getUid();

        recyclerView = view.findViewById(R.id.rvGridAppointments);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adaptor = new DoctorBookingAdaptor(getContext(), bookingList);
        recyclerView.setAdapter(adaptor);

        loadDoctorBookings();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startCallListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (callListenerRegistration != null) callListenerRegistration.remove();
        if (bookingListenerRegistration != null) bookingListenerRegistration.remove();
    }

    private void loadDoctorBookings() {
        if (doctorId == null) return;

        bookingListenerRegistration = FirebaseFirestore.getInstance()
                .collection("doctors")
                .document(doctorId)
                .collection("bookings")
                .orderBy("appointmentTime", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {

                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load bookings.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    bookingList.clear();

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            Booking booking = doc.toObject(Booking.class);
                            if (booking != null) bookingList.add(booking);
                        }
                    }

                    adaptor.notifyDataSetChanged();
                });
    }

    private void startCallListener() {

        if (doctorId == null) return;

        if (callListenerRegistration != null) callListenerRegistration.remove();

        callListenerRegistration = FirebaseFirestore.getInstance()
                .collection("Calls")
                .document(doctorId)
                .collection("incomingCall")
                .document("call")
                .addSnapshotListener((snapshot, error) -> {

                    if (error != null || snapshot == null) return;

                    if (!snapshot.exists()) return;

                    String patientUID = snapshot.getString("from");
                    String roomID = snapshot.getString("roomID");

                    snapshot.getReference().delete();

                    PermissionX.init(requireActivity())
                            .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                            .request((allGranted, grantedList, deniedList) -> {

                                if (allGranted) {
                                    Intent intent = new Intent(getContext(), VideoCall.class);
                                    intent.putExtra("roomID", roomID);
                                    intent.putExtra("otherUserID", patientUID);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getContext(),
                                            "Permission denied. Cannot accept call.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }
}
