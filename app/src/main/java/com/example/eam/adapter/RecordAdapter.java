package com.example.eam.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.eam.R;
import com.example.eam.model.Attendance;

import java.time.LocalDate;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*public class RecordAdapter extends ArrayAdapter<Attendance> {
    public RecordAdapter(@NonNull Context context, List<Attendance> events)
    {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Event event = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_cell, parent, false);

        TextView eventCellTV = convertView.findViewById(R.id.eventCellTV);

        String eventTitle = event.getName() +" "+ CalendarUtils.formattedTime(event.getTime());
        eventCellTV.setText(eventTitle);
        return convertView;
    }
}*/

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

        holder.tvClockInTime.setText(attendance.getClockInTime());

        if(attendance.getClockOutTime() == null){

        }
        else{
            holder.tvClockOutTime.setText(attendance.getClockOutTime());
        }

        Log.d("TAG", "clockInTime: " + attendance.getClockInTime() + ", cloclOutTime: " + attendance.getClockOutTime());

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
        private TextView tvClockInTime, tvClockOutTime, tvDuration, tvClockInLocation, tvClockOutLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvClockInTime = itemView.findViewById(R.id.tvClockInTime);
            tvClockOutTime = itemView.findViewById(R.id.tvClockOutTime);
            tvClockInLocation = itemView.findViewById(R.id.tvClockInLocation);
            tvClockOutLocation = itemView.findViewById(R.id.tvClockOutLocation);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }
    }
}
