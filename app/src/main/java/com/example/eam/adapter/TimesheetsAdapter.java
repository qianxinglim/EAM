package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eam.EmployeeProfileActivity;
import com.example.eam.R;
import com.example.eam.ViewAttendanceActivity;
import com.example.eam.model.User;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TimesheetsAdapter extends RecyclerView.Adapter<TimesheetsAdapter.Holder> {
    private List<User> list;
    private Context context;

    public TimesheetsAdapter(List<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_contact_item,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        User user = list.get(position);

        if (position > 0 && list.get(position - 1).getDepartment().equals(user.getDepartment())) {
            holder.tvHeader.setVisibility(View.GONE);
        } else {
            holder.tvHeader.setVisibility(View.VISIBLE);
            holder.tvHeader.setText(user.getDepartment());
        }

        holder.tvName.setText(user.getName());

        if(user.getProfilePic().equals("")){
            holder.profile.setImageResource(R.drawable.icon_male_ph);
        }
        else{
            Glide.with(context).load(user.getProfilePic()).into(holder.profile);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ViewAttendanceActivity.class)
                        .putExtra("userID", user.getID())
                        .putExtra("userName", user.getName())
                        .putExtra("userProfilePic", user.getProfilePic())
                        .putExtra("userDepartment", user.getDepartment())
                        .putExtra("userEmail", user.getEmail())
                        .putExtra("userPhoneNo", user.getPhoneNo())
                        .putExtra("userTitle", user.getTitle())
                        .putExtra("userClockInTime", user.getClockInTime())
                        .putExtra("userClockOutTime", user.getClockOutTime())
                        .putExtra("userMinutesOfWork", user.getMinutesOfWork())
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvHeader;
        private CircularImageView profile;
        public Holder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            profile = itemView.findViewById(R.id.image_profile);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }
    }
}
