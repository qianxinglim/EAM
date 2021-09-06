package com.example.eam.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eam.R;
import com.example.eam.common.CalendarUtils;
import com.example.eam.model.Attendance;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        holder.tvClockInTime.setText(attendance.getOriClockInTime());
        if(attendance.isClockIninRange()){
            holder.tvClockInLocation.setText("Within company area");
        }
        else{
            holder.tvClockInLocation.setText("Not in company area");
        }

        Log.d("RecordAdapter", "clockInInRange" + holder.tvClockInTime.getText().toString());

        if(attendance.getClockOutTime() == null){
            holder.tvClockOutTime.setText("-");
            holder.tvClockOutStatus.setText("No Records yet");
            holder.tvClockOutLocation.setVisibility(View.GONE);
        }
        /*else if(!attendance.getClockOutDate().equals(date.format(formatter))){
            holder.
        }*/
        else{
            holder.tvClockOutTime.setText(attendance.getOriClockOutTime());
            holder.tvClockOutLocation.setVisibility(View.VISIBLE);

            if(attendance.isClockOutInRange()){
                holder.tvClockOutLocation.setText("Within company area");
            }
            else{
                holder.tvClockOutLocation.setText("Not in company area");
            }

            long diff = attendance.getClockOutTimestamp() - attendance.getClockInTimestamp();
            int workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);

            int hr = workMinutes / 60;
            int min = workMinutes % 60;

            holder.tvDuration.setText("duration: " + hr + " hours " + min + " minutes ");
        }

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
        private TextView tvClockInTime, tvClockOutTime, tvDuration, tvClockInLocation, tvClockOutLocation, tvClockOutStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvClockInTime = itemView.findViewById(R.id.tvClockInTime);
            tvClockOutTime = itemView.findViewById(R.id.tvClockOutTime);
            tvClockInLocation = itemView.findViewById(R.id.tvClockInLocation);
            tvClockOutLocation = itemView.findViewById(R.id.tvClockOutLocation);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvClockOutStatus = itemView.findViewById(R.id.tvClockOutStatus);
        }
    }
}
