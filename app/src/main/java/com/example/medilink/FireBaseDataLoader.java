package com.example.medilink;

import com.example.medilink.ModelClass.BloodPressure;
import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.ModelClass.HeartRate;
import com.example.medilink.ModelClass.Patient;
import com.example.medilink.ModelClass.Pulse;
import com.example.medilink.ModelClass.user;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FireBaseDataLoader {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public interface LoadCallBack {
        void onProgress(int completed, int total);
        void onSuccess(LoadedData data);
        void onFailure(Exception e);
    }

    public static class LoadedData{
        public List<HeartRate> heartRates = new ArrayList<>();
        public List<Pulse> pulses = new ArrayList<>();
        public List<user> users = new ArrayList<>();
        public List<Patient> patients = new ArrayList<>();
        // Note: The DoctorSchedule objects loaded here will NOT have the schedule list.
        public List<DoctorSchedule> doctors = new ArrayList<>();
        public List<BloodPressure> bloodPressures = new ArrayList<>();
    }

    public void loadData(final LoadCallBack callBack) {
        final int TOTAL = 6;
        final AtomicInteger completed = new AtomicInteger(0);
        final LoadedData data = new LoadedData();

        Runnable progress = () -> {
            int c = completed.get();
            callBack.onProgress(c, TOTAL);
            if(c == TOTAL) {
                callBack.onSuccess(data);
            }
        };

        // --- HeartRate ---
        db.collection("HeartRate")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                        HeartRate hr = d.toObject(HeartRate.class);
                        if(hr!=null) {
                            data.heartRates.add(hr);
                        }
                    }
                    completed.incrementAndGet();
                    progress.run();
                })
                .addOnFailureListener(callBack::onFailure);

        // --- users ---
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                        user u = d.toObject(user.class);
                        if(u!=null) {
                            u.setUserId(d.getId());
                            data.users.add(u);
                        }
                    }
                    completed.incrementAndGet();
                    progress.run();
                })
                .addOnFailureListener(callBack::onFailure);

        // --- BloodPressure ---
        db.collection("BloodPressure")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                        BloodPressure bp = d.toObject(BloodPressure.class);
                        if(bp!=null) {
                            data.bloodPressures.add(bp);
                        }
                    }
                    completed.incrementAndGet();
                    progress.run();
                })
                .addOnFailureListener(callBack::onFailure);

        // --- Pulse ---
        db.collection("Pulse")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                        Pulse p = d.toObject(Pulse.class);
                        if(p!=null) {
                            data.pulses.add(p);
                        }
                    }
                    completed.incrementAndGet();
                    progress.run();
                })
                .addOnFailureListener(callBack::onFailure);

        // --- Patients ---
        db.collection("Patients")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                        Patient p = d.toObject(Patient.class);
                        if(p!=null) {
                            data.patients.add(p);
                        }
                    }
                    completed.incrementAndGet();
                    progress.run();
                })
                .addOnFailureListener(callBack::onFailure);

        // --- doctors ---
        db.collection("doctors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                        DoctorSchedule doc = d.toObject(DoctorSchedule.class);
                        if(doc!=null) {
                            doc.setDocId(d.getId());
                            // REMOVED: cleanExpiredSlotsAndUpdateFirebase(doc);
                            data.doctors.add(doc);
                        }
                    }
                    completed.incrementAndGet();
                    progress.run();
                })
                .addOnFailureListener(callBack::onFailure);
    }

}