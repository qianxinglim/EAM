package com.example.eam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eam.R;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Leave;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
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

        if(leave.isFullDay()){
            holder.tvLeaveDate.setText(leave.getDateFrom());
        }
        else{
            holder.tvLeaveDate.setText(leave.getDate());
        }

        holder.tvStatus.setText(leave.getStatus());
        holder.tvType.setText(leave.getType());

        //holder.tvRequestDateTime.setText(leave.);

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
        private TextView tvType, tvStatus, tvLeaveDate, tvRequestDateTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tvType);
            tvLeaveDate = itemView.findViewById(R.id.tvLeaveDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
