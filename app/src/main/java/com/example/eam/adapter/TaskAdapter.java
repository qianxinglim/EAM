package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eam.R;
import com.example.eam.TaskDetailActivity;
import com.example.eam.TaskFormActivity;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Project;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder>{
    private List<Project> list;
    private Context context;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;

    public TaskAdapter(List<Project> list, Context context) {
        this.list = list;
        this.context = context;

        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(context);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Project task = list.get(position);

        holder.tvTitle.setText(task.getTitle());

        if(task.getStatus().equals("Pending")){
            holder.tvStatusX.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.GONE);
            holder.tvForward.setVisibility(View.VISIBLE);
            holder.cardview.setCardBackgroundColor(Color.WHITE);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
            try {
                Date dueDate = sdf.parse(task.getDueDate());
                Date currDate = new Date();
                Date dueTime = sdf2.parse(task.getDueTime());
                String currTime1 = sdf2.format(currDate);
                Date currTime = sdf2.parse(currTime1);

                Log.e("TAG", "currTime: " + currTime1 + " " + currTime + ", dueTime: " + dueTime);

                if(currDate.after(dueDate) && currTime.after(dueTime)) {
                    holder.tvTitle.setTextColor(Color.parseColor("#DB4437"));
                    holder.tvStatusX.setVisibility(View.VISIBLE);
                    holder.tvStatus.setVisibility(View.GONE);
                    holder.tvForward.setVisibility(View.GONE);
                    holder.cardview.setCardBackgroundColor(Color.parseColor("#33DB4437"));
                }
                else if(currDate.after(dueDate)){
                    holder.tvTitle.setTextColor(Color.parseColor("#DB4437"));
                    holder.tvStatusX.setVisibility(View.VISIBLE);
                    holder.tvStatus.setVisibility(View.GONE);
                    holder.tvForward.setVisibility(View.GONE);
                    holder.cardview.setCardBackgroundColor(Color.parseColor("#33DB4437"));
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            holder.tvStatusX.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvForward.setVisibility(View.GONE);
            holder.cardview.setCardBackgroundColor(Color.parseColor("#1A5ebf95"));
        }

        firestore.collection("Companies").document(companyID).collection("Users").document(task.getCreator()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                holder.tvCreator.setText("Created by " + documentSnapshot.getString("name"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        holder.tvDate.setText("Start: " + task.getStartDate() + " - Due: " + task.getDueDate());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, TaskDetailActivity.class)
                        .putExtra("projectId", task.getTaskId()));

                Log.e("TaskAdapter: " , "taskId: " + task.getTaskId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvTitle, tvDate, tvCreator;
        private ImageView tvStatus, tvForward, tvStatusX;
        private CardView cardview;
        private LinearLayout linearlayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvCreator = itemView.findViewById(R.id.tvCreator);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvStatusX = itemView.findViewById(R.id.tvStatusX);
            tvForward = itemView.findViewById(R.id.tvForward);
            cardview = itemView.findViewById(R.id.cardview);
        }
    }
}
