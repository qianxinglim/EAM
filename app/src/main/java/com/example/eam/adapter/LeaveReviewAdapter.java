package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.IndvChatActivity;
import com.example.eam.ProfileActivity;
import com.example.eam.R;
import com.example.eam.common.CalendarUtils;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
        View view = LayoutInflater.from(context).inflate(R.layout.review_leave_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Leave leave = list.get(position);

        SessionManager sessionManager = new SessionManager(context);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        String companyID = userDetail.get(sessionManager.COMPANYID);

        FirebaseFirestore.getInstance().collection("Companies").document(companyID).collection("Users").document(leave.getRequester()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String userName = Objects.requireNonNull(documentSnapshot.get("name")).toString();
                String userProfilePic = documentSnapshot.get("profilePic").toString();

                holder.tvRequesterName.setText(userName);
                holder.tvStatus.setText(leave.getStatus());
                holder.tvType.setText(leave.getType());

                if(leave.isFullDay()){
                    holder.tvLeaveDate.setText(leave.getDateFrom());
                }
                else{
                    holder.tvLeaveDate.setText(leave.getDate());
                }

                if(userProfilePic!=null && !userProfilePic.equals("")) {
                    Glide.with(context).load(userProfilePic).into(holder.tvProfilePic);
                }
                else{
                    Glide.with(context).load(R.drawable.icon_male_ph).into(holder.tvProfilePic);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bottomSheetLeaveDetail(userName, userProfilePic, leave, companyID);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error getting info", Toast.LENGTH_SHORT).show();
            }
        });

        /*if(leave.isFullDay()){
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
        holder.tvType.setText(leave.getType());*/
    }

    private void bottomSheetLeaveDetail(String userName, String userProfilePic, Leave leave, String companyID) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_leave, null);

        TextView tvName = (TextView) bottomSheetView.findViewById(R.id.tvName);
        TextView tvStatus = (TextView) bottomSheetView.findViewById(R.id.tvStatus);
        tvName.setText(userName);
        tvStatus.setText(leave.getStatus());

        if(leave.getStatus().equals("Pending")) {
            bottomSheetView.findViewById(R.id.btnApprove).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance().getReference().child(companyID).child("Leaves").child(leave.getLeaveId()).child("status").setValue("Approved").addOnSuccessListener(new OnSuccessListener<Void>() {
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

                    bottomSheetDialog.dismiss();
                }
            });

            bottomSheetView.findViewById(R.id.btnDecline).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance().getReference().child(companyID).child("Leaves").child(leave.getLeaveId()).child("status").setValue("Declined").addOnSuccessListener(new OnSuccessListener<Void>() {
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

                    bottomSheetDialog.dismiss();
                }
            });
        }
        else{
            bottomSheetView.findViewById(R.id.lnBtn).setVisibility(View.GONE);
        }

        bottomSheetView.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvType, tvStatus, tvLeaveDate, tvRequesterName;
        private CircularImageView tvProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tvType);
            tvLeaveDate = itemView.findViewById(R.id.tvLeaveDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRequesterName = itemView.findViewById(R.id.tvRequesterName);
            tvProfilePic = itemView.findViewById(R.id.tvProfilePic);
        }
    }
}
