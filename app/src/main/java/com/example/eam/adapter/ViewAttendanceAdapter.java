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
import com.example.eam.common.Common;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;
import com.example.eam.model.User;

import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewAttendanceAdapter extends RecyclerView.Adapter<ViewAttendanceAdapter.ViewHolder>{
    private List<Attendance> list;
    private List<String> datelist;
    private List<Leave> leavelist;
    private Context context;

    public ViewAttendanceAdapter(List<Attendance> list, List<String> datelist, List<Leave> leaveList, Context context) {
        this.list = list;
        this.datelist = datelist;
        this.context = context;
        this.leavelist = leaveList;
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

        holder.tvClockInTime.setText(theDate);
        holder.tvClockInStatus.setText("Absence");
        holder.tvClockOutStatus.setText("Type");

        //Log.d("TAG", "attendancelist: " + list);

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

        for(Leave leave : leavelist) {
            if (leave.isFullDay()) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    String dateFrom = formatter.format(sdf.parse(leave.getDateFrom()));
                    String dateTo = formatter.format(sdf.parse(leave.getDateTo()));
                    LocalDate dateFrom2 = new LocalDate(dateFrom);
                    LocalDate dateTo2 = new LocalDate(dateTo);
                    LocalDate dateto = dateTo2.plusDays(1);
                    int days = Days.daysBetween(dateFrom2, dateto).getDays();

                    for (int i = 0; i < days; i++) {
                        LocalDate d = dateFrom2.withFieldAdded(DurationFieldType.days(), i);
                        String date = Common.getJodaTimeFormattedDate(d);

                        if(date.equals(theDate)){
                            holder.tvClockOutStatus.setText(leave.getType());
                        }
                    }
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
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
        private TextView tvClockInTime, tvClockInStatus, tvClockOutStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvClockInTime = itemView.findViewById(R.id.tvClockInTime);
            tvClockInStatus = itemView.findViewById(R.id.tvClockInStatus);
            tvClockOutStatus = itemView.findViewById(R.id.tvClockOutStatus);
        }
    }
}
