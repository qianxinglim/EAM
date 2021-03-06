package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eam.IndvChatActivity;
import com.example.eam.PunchDetailActivity;
import com.example.eam.R;
import com.example.eam.common.CalendarUtils;
import com.example.eam.model.Attendance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>{
    private List<Attendance> list;
    private Context context;

    public RecordAdapter(List<Attendance> list, Context context) {
        this.list = list;
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
        Attendance attendance = list.get(position);

        LocalDate date = CalendarUtils.selectedDate;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");

        Date dateTime = new Date();
        String currDate = formatter2.format(dateTime);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        Date clockInTime = null, oriClockInTime = null, mustClockOutTime = null;

        try {
            oriClockInTime = df.parse(attendance.getOriClockInTime());
            clockInTime = df.parse(attendance.getClockInTime());
            mustClockOutTime = df.parse(attendance.getMustClockOutTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvClockInTime.setText(attendance.getOriClockInTime());

        //No punch out
        if (attendance.getClockOutDate() == null) {
            //If today
            if (date.format(formatter).equals(currDate)) {
                if (clockInTime.after(oriClockInTime)) {
                    if (attendance.isClockIninRange()) {
                        holder.tvClockInLocation.setText("Within company area, punch in late (" + attendance.getClockInTime() + ")");
                        holder.tvClockInLocation.setTextColor(Color.RED);
                        holder.tvClockInStatus.setTextColor(Color.RED);
                    } else {
                        holder.tvClockInLocation.setText("Not in company area, punch in late (" + attendance.getClockInTime() + ")");
                        holder.tvClockInLocation.setTextColor(Color.RED);
                        holder.tvClockInStatus.setTextColor(Color.RED);
                    }
                }
                else{
                    if (attendance.isClockIninRange()) {
                        holder.tvClockInLocation.setText("Within company area (" + attendance.getClockInTime() + ")");
                    } else {
                        holder.tvClockInLocation.setText("Not in company area (" + attendance.getClockInTime() + ")");
                    }
                }

                //No records
                holder.tvClockOutTime.setText("-");
                holder.tvClockOutStatus.setText("No Records yet");
                holder.tvClockOutLocation.setVisibility(View.GONE);

            } //If not today
            else if (!date.format(formatter).equals(currDate)) {
                //punch in late
                if (clockInTime.after(oriClockInTime)) {
                    holder.tvClockOutTime.setText(attendance.getOriClockOutTime());

                    if (attendance.isClockIninRange()) {
                        holder.tvClockInLocation.setText("Within company area, punch in late (" + attendance.getClockInTime() + ")");
                        holder.tvClockInLocation.setTextColor(Color.RED);
                        holder.tvClockInStatus.setTextColor(Color.RED);
                    } else {
                        holder.tvClockInLocation.setText("Not in company area, punch in late (" + attendance.getClockInTime() + ")");
                        holder.tvClockInLocation.setTextColor(Color.RED);
                        holder.tvClockInStatus.setTextColor(Color.RED);
                    }
                }
                else{
                    holder.tvClockOutTime.setText(attendance.getMustClockOutTime());

                    if (attendance.isClockIninRange()) {
                        holder.tvClockInLocation.setText("Within company area (" + attendance.getClockInTime() + ")");
                    } else {
                        holder.tvClockInLocation.setText("Not in company area (" + attendance.getClockInTime() + ")");
                    }
                }

                //Punch missed
                holder.tvClockOutLocation.setText("Punch missed");
                holder.tvClockOutLocation.setTextColor(Color.RED);
                holder.tvClockOutStatus.setTextColor(Color.RED);
                //holder.tvClockOutLocation.setVisibility(View.GONE);
            }

            holder.tvDuration.setText("Working duration: 0 hours 0 minutes");
            holder.btnlnClockOut.setEnabled(false);

        }
        else { //If punch out
            //If clock in late
            if (clockInTime.after(oriClockInTime)) {
                if (attendance.isClockIninRange()) {
                    holder.tvClockInLocation.setText("Within company area, punch in late (" + attendance.getClockInTime() + ")");
                    holder.tvClockInLocation.setTextColor(Color.RED);
                    holder.tvClockInStatus.setTextColor(Color.RED);
                } else {
                    holder.tvClockInLocation.setText("Not in company area, punch in late (" + attendance.getClockInTime() + ")");
                    holder.tvClockInLocation.setTextColor(Color.RED);
                    holder.tvClockInStatus.setTextColor(Color.RED);
                }

                if(mustClockOutTime.before(oriClockInTime)){
                    holder.tvClockOutTime.setText(attendance.getOriClockOutTime());
                }
                else{
                    holder.tvClockOutTime.setText(attendance.getMustClockOutTime());
                }

                //holder.tvClockOutTime.setText(attendance.getMustClockOutTime());
                holder.tvClockOutLocation.setVisibility(View.VISIBLE);

                //If punch out that day
                if(attendance.getClockOutDate().equals(attendance.getClockInDate())){
                    if (attendance.isClockOutInRange()) {
                        holder.tvClockOutLocation.setText("Within company area (" + attendance.getClockOutTime() + ")");
                    } else {
                        holder.tvClockOutLocation.setText("Not in company area (" + attendance.getClockOutTime() + ")");
                    }

                    long diff = attendance.getClockOutTimestamp() - attendance.getClockInTimestamp();
                    int workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);

                    int hr = workMinutes / 60;
                    int min = workMinutes % 60;

                    holder.tvDuration.setText("duration: " + hr + " hours " + min + " minutes ");
                }
                else{ //If not
                    if (attendance.isClockOutInRange()) {
                        holder.tvClockOutLocation.setText("Punch missed");
                        holder.tvClockOutLocation.setTextColor(Color.RED);
                        holder.tvClockOutStatus.setTextColor(Color.RED);
                    } else {
                        holder.tvClockOutLocation.setText("Punch missed");
                        holder.tvClockOutLocation.setTextColor(Color.RED);
                        holder.tvClockOutStatus.setTextColor(Color.RED);
                    }

                    holder.tvDuration.setText("Working duration: 0 hours 0 minutes");
                }

                /*long diff = attendance.getClockOutTimestamp() - attendance.getClockInTimestamp();
                int workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);

                int hr = workMinutes / 60;
                int min = workMinutes % 60;

                holder.tvDuration.setText("duration: " + hr + " hours " + min + " minutes ");*/
            }
            else {
                if (attendance.isClockIninRange()) {
                    holder.tvClockInLocation.setText("Within company area (" + attendance.getClockInTime() + ")");
                } else {
                    holder.tvClockInLocation.setText("Not in company area (" + attendance.getClockInTime() + ")");
                }

                holder.tvClockOutTime.setText(attendance.getOriClockOutTime());
                holder.tvClockOutLocation.setVisibility(View.VISIBLE);

                if(attendance.getClockOutDate().equals(attendance.getClockInDate())){
                    if (attendance.isClockOutInRange()) {
                        holder.tvClockOutLocation.setText("Within company area (" + attendance.getClockOutTime() + ")");
                    } else {
                        holder.tvClockOutLocation.setText("Not in company area (" + attendance.getClockOutTime() + ")");
                    }

                    long diff = attendance.getClockOutTimestamp() - attendance.getClockInTimestamp();
                    int workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);

                    int hr = workMinutes / 60;
                    int min = workMinutes % 60;

                    holder.tvDuration.setText("Working duration: " + hr + " hour(s) " + min + " minutes ");
                }
                else{
                    if (attendance.isClockOutInRange()) {
                        holder.tvClockOutLocation.setText("Punch missed");
                        holder.tvClockOutLocation.setTextColor(Color.RED);
                        holder.tvClockOutStatus.setTextColor(Color.RED);
                    } else {
                        holder.tvClockOutLocation.setText("Punch missed");
                        holder.tvClockOutLocation.setTextColor(Color.RED);
                        holder.tvClockOutStatus.setTextColor(Color.RED);
                    }

                    holder.tvDuration.setText("Working duration: 0 hours 0 minutes");
                }
            }
        }

        holder.btnlnClockIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, PunchDetailActivity.class)
                        .putExtra("type", "clockIn")
                        .putExtra("clockInDate", attendance.getClockInDate())
                        .putExtra("clockInTime", attendance.getClockInTime())
                        .putExtra("clockInTimestamp", attendance.getClockInTimestamp())
                        .putExtra("clockInLat", attendance.getClockInLat())
                        .putExtra("clockInLong", attendance.getClockInLong()));
            }
        });

        holder.btnlnClockOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, PunchDetailActivity.class)
                        .putExtra("type", "clockOut")
                        .putExtra("clockOutDate", attendance.getClockOutDate())
                        .putExtra("clockOutTime", attendance.getClockOutTime())
                        .putExtra("clockOutTimestamp", attendance.getClockOutTimestamp())
                        .putExtra("clockOutLat", attendance.getClockOutLat())
                        .putExtra("clockOutLong", attendance.getClockOutLong()));
            }
        });

        /*String currDateTime = df.format(new Date());

        Date currTime = null, oriClockOutTime = null;

        try {
            currTime = df.parse(currDateTime);
            oriClockOutTime = df.parse(attendance.getOriClockOutTime());

            Log.d("TAG", "currTime: " + currTime + ", oriClockInTime: " + oriClockOutTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //&& currTime.after(oriClockOutTime)

        if(attendance.getClockOutTime() == null){
            holder.tvClockOutTime.setText("-");
            holder.tvClockOutStatus.setText("No Records yet");
            holder.tvClockOutLocation.setVisibility(View.GONE);
        }
        else{
            holder.tvClockOutTime.setText(attendance.getOriClockOutTime());
            holder.tvClockOutLocation.setVisibility(View.VISIBLE);

            if(attendance.isClockOutInRange()){
                holder.tvClockOutLocation.setText("Within company area (" + attendance.getClockOutTime() + ")");
            }
            else{
                holder.tvClockOutLocation.setText("Not in company area (" + attendance.getClockOutTime() + ")");
            }

            long diff = attendance.getClockOutTimestamp() - attendance.getClockInTimestamp();
            int workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);

            int hr = workMinutes / 60;
            int min = workMinutes % 60;

            holder.tvDuration.setText("duration: " + hr + " hours " + min + " minutes ");
        }*/

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
        private TextView tvClockInTime, tvClockOutTime, tvDuration, tvClockInLocation, tvClockOutLocation, tvClockOutStatus, tvClockInStatus;
        private LinearLayout btnlnClockIn, btnlnClockOut;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvClockInTime = itemView.findViewById(R.id.tvClockInTime);
            tvClockOutTime = itemView.findViewById(R.id.tvClockOutTime);
            tvClockInLocation = itemView.findViewById(R.id.tvClockInLocation);
            tvClockOutLocation = itemView.findViewById(R.id.tvClockOutLocation);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvClockOutStatus = itemView.findViewById(R.id.tvClockOutStatus);
            btnlnClockIn = itemView.findViewById(R.id.btnlnClockIn);
            btnlnClockOut = itemView.findViewById(R.id.btnlnClockOut);
            tvClockInStatus = itemView.findViewById(R.id.tvClockInStatus);
        }
    }
}
