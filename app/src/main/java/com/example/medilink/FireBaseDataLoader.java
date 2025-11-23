package com.example.medilink;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.ModelClass.Patient;
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
        public List<user> users = new ArrayList<>();
        public List<Patient> patients = new ArrayList<>();
        public List<DoctorSchedule> doctors = new ArrayList<>();
    }

    public void loadData(final LoadCallBack callBack) {
        final int TOTAL = 3;
        final AtomicInteger completed = new AtomicInteger(0);
        final LoadedData data = new LoadedData();

        Runnable progress = () -> {
            int c = completed.get();
            callBack.onProgress(c, TOTAL);
            if(c == TOTAL) {
                callBack.onSuccess(data);
            }
        };

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

        db.collection("doctors")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(DocumentSnapshot d: queryDocumentSnapshots.getDocuments()) {
                        DoctorSchedule doc = d.toObject(DoctorSchedule.class);
                        if(doc!=null) {
                            doc.setDocId(d.getId());
                            data.doctors.add(doc);
                        }
                    }
                    completed.incrementAndGet();
                    progress.run();
                })
                .addOnFailureListener(callBack::onFailure);
    }

}