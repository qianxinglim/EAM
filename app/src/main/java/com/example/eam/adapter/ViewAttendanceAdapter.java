package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.IndvChatActivity;
import com.example.eam.LeaveDetailActivity;
import com.example.eam.R;
import com.example.eam.WeeklyViewActivity;
import com.example.eam.common.Common;
import com.example.eam.menu.CalendarFragment;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;
import com.example.eam.model.User;

import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ViewAttendanceAdapter extends RecyclerView.Adapter<ViewAttendanceAdapter.ViewHolder>{
    private List<Attendance> list;
    private List<String> datelist;
    private List<Leave> leavelist;
    private Context context;
    private String userId;
    private String userName;
    private String userProfilePic;

    public ViewAttendanceAdapter(String userId, String userName, String userProfilePic, List<Attendance> list, List<String> datelist, List<Leave> leaveList, Context context) {
        this.list = list;
        this.datelist = datelist;
        this.context = context;
        this.leavelist = leaveList;
        this.userId = userId;
        this.userName = userName;
        this.userProfilePic = userProfilePic;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_attendance_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String theDate = datelist.get(position);

        //Set day of week & dateNum
        try {
            SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
            Date dateTime = new Date();
            String currDate = formatter2.format(dateTime);

            DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            Date datee = format.parse(theDate);
            String day = new SimpleDateFormat("EE").format(datee);
            String dateNum = new SimpleDateFormat("dd").format(datee);

            holder.tvDateDay.setText(day);
            holder.tvDateNo.setText(dateNum);
            holder.tvDateNo.setTypeface(null, Typeface.NORMAL);
            holder.tvDateDay.setTextColor(ContextCompat.getColor(context, R.color.grey));
            holder.tvDateNo.setTextColor(ContextCompat.getColor(context, R.color.grey));

            if(currDate.equals(theDate)){
                holder.tvDateNo.setTypeface(null, Typeface.BOLD);
                holder.tvDateDay.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.tvDateNo.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        /*Resources r = context.getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10,
                r.getDisplayMetrics()
        );

        if(position == datelist.size() - 1){
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, px, 0, px);
            holder.linearlayout.setLayoutParams(layoutParams);
        }
        else{
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, px, 0, 0);
            holder.linearlayout.setLayoutParams(layoutParams);
        }*/

        //holder.tvDateNo.setText(theDate);
        holder.tvDailyTotal.setText("Absent");
        holder.tvLeaveType.setVisibility(View.GONE);
        holder.tvView.setVisibility(View.GONE);
        holder.tvDailyTotal.setTextColor(ContextCompat.getColor(context, R.color.grey));

        for(Attendance attendance : list){
            Log.d("TAG", "attendancelist: " + attendance.getClockInDate());

            if(attendance.getClockInDate().equals(theDate)){
                holder.tvDailyTotal.setText("Present");
                holder.tvDailyTotal.setTextColor(ContextCompat.getColor(context, R.color.black));
                Log.d("TAG", "theDate: " + theDate + "clockInDate: " + attendance.getClockInDate());

                holder.tvDailyTotal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        context.startActivity(new Intent(context, WeeklyViewActivity.class)
                                .putExtra("date", theDate)
                                .putExtra("userId", userId));
                    }
                });
            }
            /*else{
                holder.tvClockInStatus.setText("Absence");
            }*/
        }

        for(Leave leave : leavelist) {
            Log.e("TAGLOL", "leavelist: " + leavelist + ", leaveFrom: " + leave.getDateFrom() + ", leaveTo: " + leave.getDateTo());

            if (leave.isFullDay()) {
                Log.d("TAG1", "leaveFrom: " + leave.getDateFrom() + "leaveTo: " + leave.getDateTo());

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
                            Log.d("TAG", "leave: " + leave.getDateFrom());
                            holder.tvView.setVisibility(View.VISIBLE);
                            holder.tvLeaveType.setVisibility(View.VISIBLE);
                            holder.tvLeaveType.setText(leave.getType());

                            holder.tvLeaveType.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    context.startActivity(new Intent(context, LeaveDetailActivity.class)
                                            .putExtra("leaveObj", leave)
                                            .putExtra("Activity", "LeaveReview")
                                            .putExtra("userName", userName)
                                            .putExtra("profilePic", userProfilePic));
                                }
                            });
                        }
                    }
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else{
                Log.d("TAG1", "leaveDate: " + leave.getDate());

                if(leave.getDate().equals(theDate)){
                    Log.d("TAG", "leave: " + leave.getDate());
                    holder.tvView.setVisibility(View.VISIBLE);
                    holder.tvLeaveType.setVisibility(View.VISIBLE);
                    holder.tvLeaveType.setText(leave.getType());

                    holder.tvLeaveType.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            context.startActivity(new Intent(context, LeaveDetailActivity.class)
                                    .putExtra("leaveObj", leave)
                                    .putExtra("Activity", "LeaveReview")
                                    .putExtra("userName", userName)
                                    .putExtra("profilePic", userProfilePic));
                        }
                    });
                }
            }
        }

        //Log.d("TAG", "attendancelist: " + list);
    }

    @Override
    public int getItemCount() {
        return datelist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvClockInTime, tvClockInStatus, tvClockOutStatus;
        private TextView tvDateNo, tvDateDay, tvLeaveType, tvDailyTotal;
        private LinearLayout tvView, linearlayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*tvClockInTime = itemView.findViewById(R.id.tvClockInTime);
            tvClockInStatus = itemView.findViewById(R.id.tvClockInStatus);
            tvClockOutStatus = itemView.findViewById(R.id.tvClockOutStatus);*/

            tvDateNo = itemView.findViewById(R.id.tvDateNo);
            tvDateDay = itemView.findViewById(R.id.tvDateDay);
            tvLeaveType = itemView.findViewById(R.id.tvLeaveType);
            tvDailyTotal = itemView.findViewById(R.id.tvDailyTotal);
            tvView = itemView.findViewById(R.id.tvView);
            linearlayout = itemView.findViewById(R.id.linearLayout);
        }
    }
}
