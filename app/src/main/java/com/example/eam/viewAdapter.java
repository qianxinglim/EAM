package com.example.eam;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eam.common.Common;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;

import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class viewAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<Attendance> list;
    private List<Leave> leavelist;
    private List<String> datelist;
    private Activity mActivity;
    private TextView tvClockInTime, tvClockInStatus, tvDailyTotal, tvDateNo, tvLeaveType;
    private LinearLayout tvView;

    public viewAdapter(Activity activity, List<Attendance> list, List<Leave> leavelist, List<String> datelist) {
        mActivity = activity;
        this.list = list;
        this.datelist = datelist;
        this.leavelist = leavelist;
    }

    @Override
    public int getCount() {
        return datelist.size();
    }

    @Override
    public Object getItem(int i) {
        return datelist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(mLayoutInflater == null){
            mLayoutInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }
        if(view == null){
            view = mLayoutInflater.inflate(R.layout.view_attendance_item, null);
        }

        /*tvClockInTime = view.findViewById(R.id.tvClockInTime);
        tvClockInStatus = view.findViewById(R.id.tvClockInStatus);*/

        tvDateNo = view.findViewById(R.id.tvDateNo);
        tvDailyTotal = view.findViewById(R.id.tvDailyTotal);
        tvView = view.findViewById(R.id.tvView);
        tvLeaveType = view.findViewById(R.id.tvLeaveType);

        tvLeaveType.setVisibility(View.GONE);
        tvView.setVisibility(View.GONE);

        String theDate = datelist.get(i);

        tvDateNo.setText(theDate);
        tvDailyTotal.setText("Absebce");

        Log.e("viewAdapter", ", position: " + i + ", theDate: " + theDate);
        Log.d("viewAdapter: ","list: " + list);

        for(int j=0; j < list.size(); j++){
            Log.e("viewAdapter", "attendancelist: " + list.get(j).getClockInDate() + ", position: " + j);

            if(theDate.equals(list.get(j).getClockInDate())){
                tvDailyTotal.setText("Present");
                Log.d("TAG", "theDate: " + theDate + "clockInDate: " + list.get(j).getClockInDate());
            }
        }

        for(Leave leave : leavelist) {
            tvLeaveType.setVisibility(View.GONE);
            tvView.setVisibility(View.GONE);

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

                    for (int j = 0; j < days; j++) {
                        LocalDate d = dateFrom2.withFieldAdded(DurationFieldType.days(), j);
                        String date = Common.getJodaTimeFormattedDate(d);

                        if(date.equals(theDate)){
                            tvView.setVisibility(View.VISIBLE);
                            tvLeaveType.setVisibility(View.VISIBLE);
                            tvLeaveType.setText(leave.getType());
                            //holder.tvClockOutStatus.setText(leave.getType());
                        }
                    }
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return view;
    }
}
