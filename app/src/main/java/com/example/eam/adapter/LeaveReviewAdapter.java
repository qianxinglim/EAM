package com.example.eam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eam.R;
import com.example.eam.common.CalendarUtils;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class LeaveReviewAdapter extends RecyclerView.Adapter<LeaveReviewAdapter.ViewHolder>{
    private List<Leave> list;
    private Context context;

    public LeaveReviewAdapter(List<Leave> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leave_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Leave leave = list.get(position);

        SessionManager sessionManager = new SessionManager(context);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        String companyID = userDetail.get(sessionManager.COMPANYID);

        if(leave.getStatus().equals("pending")){
            holder.btnApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance().getReference().child(companyID).child("Leaves").child(leave.getLeaveId()).child("status").setValue("approved").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            Toast.makeText(context, "Successfully approved", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Fail to approve", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            holder.btnDecline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance().getReference().child(companyID).child("Leaves").child(leave.getLeaveId()).child("status").setValue("declined").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            Toast.makeText(context, "Successfully declined", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Successfully decline", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
        else{
            holder.btnDecline.setVisibility(View.GONE);
            holder.btnApprove.setVisibility(View.GONE);
        }

        if(leave.isFullDay()){
            holder.tvDateFrom.setText(leave.getDateFrom());
            holder.tvDateTo.setText(leave.getDateTo());

            holder.tvDate.setVisibility(View.GONE);
            holder.tvTimeFrom.setVisibility(View.GONE);
            holder.tvTimeTo.setVisibility(View.GONE);
        }
        else{
            holder.tvDateFrom.setVisibility(View.GONE);
            holder.tvDateTo.setVisibility(View.GONE);

            holder.tvDate.setText(leave.getDate());
            holder.tvTimeFrom.setText(leave.getTimeFrom());
            holder.tvTimeTo.setText(leave.getTimeTo());
        }

        holder.tvDuration.setText(leave.getDuration());
        holder.tvDay.setText("Full Day");
        holder.tvRequester.setText(leave.getRequester());
        holder.tvStatus.setText(leave.getStatus());
        holder.tvType.setText(leave.getType());

        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, IndvChatActivity.class)
                        .putExtra("userID", user.getID())
                        .putExtra("userName", user.getName())
                        .putExtra("userProfilePic", user.getProfilePic()));
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvRequester, tvType, tvDay, tvDuration, tvDateFrom, tvDateTo, tvTimeFrom, tvTimeTo, tvStatus, tvDate;
        private Button btnDecline, btnApprove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRequester = itemView.findViewById(R.id.tvRequester);
            tvType = itemView.findViewById(R.id.tvType);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDateFrom = itemView.findViewById(R.id.tvDateFrom);
            tvDateTo = itemView.findViewById(R.id.tvDateTo);
            tvTimeFrom = itemView.findViewById(R.id.tvTimeFrom);
            tvTimeTo = itemView.findViewById(R.id.tvTimeTo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDecline = itemView.findViewById(R.id.btnDecline);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
