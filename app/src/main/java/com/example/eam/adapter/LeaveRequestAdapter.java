package com.example.eam.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eam.IndvChatActivity;
import com.example.eam.LeaveDetailActivity;
import com.example.eam.R;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Leave;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder>{
    private List<Leave> list;
    private Context context;

    public LeaveRequestAdapter(List<Leave> list, Context context) {
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

        Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1,
                r.getDisplayMetrics()
        );

        if(position == 0){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 0);
            holder.linearlayout.setLayoutParams(layoutParams);
        }
        else{
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, px);
            holder.linearlayout.setLayoutParams(layoutParams);
        }

        if(leave.isFullDay()){
            holder.tvLeaveDate.setText(leave.getDateFrom());
            holder.tvLeaveDateTo.setText(leave.getDateTo());
        }
        else{
            holder.tvLeaveDate.setText(leave.getDate());
        }

        if(leave.getStatus().equals("Approved")){
            holder.tvStatus.setText(leave.getStatus());
            DrawableCompat.setTint(holder.tvStatus.getBackground(), ContextCompat.getColor(context, R.color.quantum_googgreen));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        else if(leave.getStatus().equals("Declined")){
            holder.tvStatus.setText(leave.getStatus());
            DrawableCompat.setTint(holder.tvStatus.getBackground(), ContextCompat.getColor(context, R.color.quantum_googred));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
        }
        else{
            holder.tvStatus.setText(leave.getStatus());
            DrawableCompat.setTint(holder.tvStatus.getBackground(), ContextCompat.getColor(context, R.color.colorDivider));
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.grey));
        }

        if(leave.getRequestDate() != null && leave.getRequestTime() != null){
            holder.tvRequestDateTime.setText("Requested on " + leave.getRequestDate() + " at " + leave.getRequestTime());
        }

        holder.tvStatus.setText(leave.getStatus());
        holder.tvType.setText(leave.getType());

        //holder.tvRequestDateTime.setText(leave.);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, LeaveDetailActivity.class)
                        .putExtra("leaveObj", leave)
                        .putExtra("Activity", "LeaveRecord"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvType, tvStatus, tvLeaveDate, tvRequestDateTime, tvLeaveDateTo;
        private LinearLayout linearlayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tvType);
            tvLeaveDate = itemView.findViewById(R.id.tvLeaveDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvRequestDateTime = itemView.findViewById(R.id.tvRequestDateTime);
            linearlayout = itemView.findViewById(R.id.linearLayout);
            tvLeaveDateTo = itemView.findViewById(R.id.tvLeaveDateTo);
        }
    }
}
