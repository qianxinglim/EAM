package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.IndvChatActivity;
import com.example.eam.R;
import com.example.eam.model.Attendance;
import com.example.eam.model.User;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewAttendanceAdapter extends RecyclerView.Adapter<ViewAttendanceAdapter.ViewHolder>{
    private List<Attendance> list;
    private List<String> datelist;
    private Context context;

    public ViewAttendanceAdapter(List<Attendance> list, List<String> datelist, Context context) {
        this.list = list;
        this.datelist = datelist;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.attendance_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String theDate = datelist.get(position);
        //Attendance attendance = list.get(position);

        holder.tvClockInTime.setText(theDate);

        //Log.d("TAG", "attendancelist: " + list);

        /*if(theDate.equals(attendance.getClockInDate())){
            holder.tvClockInStatus.setText("Present");
        }
        else{
            holder.tvClockInStatus.setText("Absence");
        }*/

        for(Attendance attendance : list){
            Log.d("TAG", "attendancelist: " + attendance.getClockInDate());

            if(attendance.getClockInDate().equals(theDate)){
                holder.tvClockInStatus.setText("Present");
                Log.d("TAG", "theDate: " + theDate + "clockInDate: " + attendance.getClockInDate());
            }
            /*else{
                holder.tvClockInStatus.setText("Absence");
            }*/
        }

        //Log.d("TAG", "attendancelist: " + list);

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
        return datelist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvClockInTime, tvClockInStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvClockInTime = itemView.findViewById(R.id.tvClockInTime);
            tvClockInStatus = itemView.findViewById(R.id.tvClockInStatus);
        }
    }
}
