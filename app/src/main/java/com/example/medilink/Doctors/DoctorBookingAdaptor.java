package com.example.medilink.Doctors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medilink.ModelClass.DoctorSchedule;
import com.example.medilink.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DoctorBookingAdaptor extends RecyclerView.Adapter<DoctorBookingAdaptor.ViewHolder> {

    private final Context context;
    private final List<DoctorSchedule.Slots> slotList;

    public DoctorBookingAdaptor(Context context, List<DoctorSchedule.Slots> slotList) {
        this.context = context;
        this.slotList = slotList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.doctorappointmentlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorSchedule.Slots slot = slotList.get(position);

        holder.tvBookedSlot.setText(slot.getDay() + " • " + slot.getStart() + " - " + slot.getEnd());

        FirebaseFirestore.getInstance().collection("Patients")
                .document(slot.getBookedBy())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        String gender = snapshot.getString("gender");
                        String age = snapshot.getString("age");
                        String phone = snapshot.getString("phoneNo");

                        holder.tvName.setText(name != null ? name : "Unknown");
                        holder.tvGender.setText(gender != null ? gender : "N/A");
                        holder.tvAge.setText(age);
                        holder.tvPhoneNo.setText(phone != null ? phone : "N/A");
                    }
                })
                .addOnFailureListener(e -> holder.tvName.setText("Error loading"));
    }


    @Override
    public int getItemCount() {
        return slotList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBookedSlot, tvGender, tvAge, tvPhoneNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvBookedSlot = itemView.findViewById(R.id.tvBookedSlot);
            tvPhoneNo = itemView.findViewById(R.id.tvPhoneNo); // Make sure your layout has tvPrice
        }
    }
}
