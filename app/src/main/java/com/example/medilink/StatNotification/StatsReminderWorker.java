package com.example.medilink.StatNotification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class StatsReminderWorker extends Worker {

    private static final long WATER_THRESHOLD = 2000;
    private static final long SLEEP_THRESHOLD = 6;

    private final FirebaseFirestore db;
    private String userId;

    public StatsReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        if (userId == null) {
            return Result.success();
        }

        // Start of today
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Timestamp todayStart = new Timestamp(cal.getTime());

        checkStat("weight", todayStart);
        checkStat("water", todayStart);
        checkStat("sleep", todayStart);

        return Result.success();
    }

    private void checkStat(String type, Timestamp todayStart) {
        db.collection("users").document(userId)
                .collection("stats")
                .document(type)
                .collection("data")
                .whereGreaterThan("timestamp", todayStart)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    boolean entered = !querySnapshot.isEmpty();

                    if (!entered) {
                        NotificationHelper.sendNotification(
                                getApplicationContext(),
                                "Stats Reminder",
                                "You haven't entered your " + type + " today!",
                                type.hashCode()
                        );
                    } else if (type.equals("water") || type.equals("sleep")) {
                        for (var doc : querySnapshot.getDocuments()) {
                            Long valueObj = null;
                            if (type.equals("water")) valueObj = doc.getLong("value");
                            if (type.equals("sleep")) valueObj = doc.getLong("hours");

                            if (valueObj == null) continue;
                            long value = valueObj;

                            boolean isLow = (type.equals("water") && value < WATER_THRESHOLD)
                                    || (type.equals("sleep") && value < SLEEP_THRESHOLD);

                            if (isLow) {
                                NotificationHelper.sendNotification(
                                        getApplicationContext(),
                                        "Health Alert",
                                        "Your " + type + " is low! Try to drink more water or sleep better.",
                                        type.hashCode() + 100
                                );
                            }
                        }
                    }

                });
    }
}
