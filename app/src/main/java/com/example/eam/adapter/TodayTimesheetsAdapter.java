package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eam.R;
import com.example.eam.ViewAttendanceActivity;
import com.example.eam.model.Attendance;
import com.example.eam.model.User;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TodayTimesheetsAdapter extends RecyclerView.Adapter<TodayTimesheetsAdapter.Holder> {
    private List<User> userList;
    private List<Attendance> list;
    private String filter;
    private Context context;

    public TodayTimesheetsAdapter(String filter, List<User> userList, List<Attendance> list, Context context) {
        this.list = list;
        this.userList = userList;
        this.filter = filter;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_today_timesheet_item,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getName());
        holder.tvAttendance.setVisibility(View.VISIBLE);
        holder.lnAttendance.setVisibility(View.GONE);

        if (position > 0 && userList.get(position - 1).getDepartment().equals(user.getDepartment())) {
            holder.tvHeader.setVisibility(View.GONE);
        }
        else {
            holder.tvHeader.setVisibility(View.VISIBLE);
            holder.tvHeader.setText(user.getDepartment());
        }

        if(user.getProfilePic().equals("")){
            holder.profile.setImageResource(R.drawable.icon_male_ph);
        }
        else{
            Glide.with(context).load(user.getProfilePic()).into(holder.profile);
        }

        for(Attendance attendance : list){
            if(attendance.getUserId().equals(user.getID())){
                holder.lnAttendance.setVisibility(View.VISIBLE);
                holder.tvAttendance.setVisibility(View.GONE);
                holder.tvAttendanceFrom.setText(attendance.getClockInTime());

                if(attendance.getClockOutDate() == null){
                    holder.tvAttendanceTo.setText("--");
                }
                else {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        //Date oriClockOutTime = sdf.parse(user.getClockOutTime());
                        Date mustClockOutTime = sdf.parse(attendance.getMustClockOutTime());
                        Date realClockOutTime = sdf.parse(attendance.getClockOutTime());

                        if (realClockOutTime.after(mustClockOutTime)) {
                            holder.tvAttendanceTo.setText(attendance.getMustClockOutTime());
                        }
                        else{
                            holder.tvAttendanceTo.setText(attendance.getClockOutTime());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
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
        });*/
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvHeader, tvAttendanceFrom, tvAttendanceTo, tvAttendance;
        private LinearLayout lnAttendance;
        private CircularImageView profile;
        public Holder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            profile = itemView.findViewById(R.id.image_profile);
            tvHeader = itemView.findViewById(R.id.tvHeader);
            tvAttendanceFrom = itemView.findViewById(R.id.tvAttendanceFrom);
            tvAttendanceTo = itemView.findViewById(R.id.tvAttendanceTo);
            tvAttendance = itemView.findViewById(R.id.tvAttendance);
            lnAttendance = itemView.findViewById(R.id.lnAttendance);
        }
    }
}
