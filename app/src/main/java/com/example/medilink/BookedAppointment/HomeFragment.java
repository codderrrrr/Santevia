    package com.example.medilink.BookedAppointment;

    import android.os.Bundle;
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
    import com.google.firebase.firestore.QuerySnapshot;

    import java.util.ArrayList;
    import java.util.Date;
    import java.util.List;

    public class HomeFragment extends Fragment {

        private RecyclerView rvGridAppointments;
        private BookingsAdaptor adaptor;
        private FirebaseFirestore db;
        private String patientID;

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
            patientID = FirebaseAuth.getInstance().getUid();

            rvGridAppointments = v.findViewById(R.id.rvGridAppointments);
            rvGridAppointments.setLayoutManager(new GridLayoutManager(getContext(), 1));

            adaptor = new BookingsAdaptor(getContext(), new ArrayList<>());
            rvGridAppointments.setAdapter(adaptor);

            loadBookedAppointments();

            return v;
        }

        @Override
        public void onResume() {
            super.onResume();
            loadBookedAppointments();
        }

        private void loadBookedAppointments() {
            if (patientID == null || getContext() == null) return;

            List<DoctorSchedule> cachedDoctors;
            if(AppCache.getInstance().getLoadedData() != null) {
                cachedDoctors = AppCache.getInstance().getLoadedData().doctors;
            } else {
                cachedDoctors = new ArrayList<>();
            }

            if (cachedDoctors.isEmpty()) {
                Toast.makeText(getContext(), "Doctor data not loaded. Check connection.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Task<QuerySnapshot>> bookingTasks = new ArrayList<>();
            Date now = new Date();

            for (DoctorSchedule doctor : cachedDoctors) {
                if (doctor.getDocId() != null) {
                    Task<QuerySnapshot> task = db.collection("doctors").document(doctor.getDocId())
                            .collection("bookings")
                            .whereEqualTo("bookedByUserId", patientID)
                            .get();
                    bookingTasks.add(task);
                }
            }

            Tasks.whenAllSuccess(bookingTasks)
                    .addOnSuccessListener(results -> {
                        List<AppointmentSummary> appointments = new ArrayList<>();
                        int doctorIndex = 0;

                        for (Object result : results) {
                            QuerySnapshot snapshot = (QuerySnapshot) result;
                            DoctorSchedule doctor = cachedDoctors.get(doctorIndex);

                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                Booking booking = document.toObject(Booking.class);
                                if (booking != null) {
                                    if (booking.getAppointmentTime() != null &&
                                            booking.getAppointmentTime().toDate().after(now)) {
                                        appointments.add(new AppointmentSummary(doctor, booking));
                                    }
                                }
                            }
                            doctorIndex++;
                        }

                        if (!appointments.isEmpty()) {
                            adaptor.updateList(appointments);
                        } else {
                            Toast.makeText(getContext(), "No booked appointments found", Toast.LENGTH_SHORT).show();
                            adaptor.updateList(new ArrayList<>());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to load bookings: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }

    }