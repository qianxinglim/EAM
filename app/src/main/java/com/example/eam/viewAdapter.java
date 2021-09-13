package com.example.eam;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.eam.model.Attendance;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class viewAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private List<Attendance> list;
    private List<String> datelist;
    private Activity mActivity;
    private TextView tvClockInTime, tvClockInStatus;

    public viewAdapter(Activity activity, List<Attendance> list, List<String> datelist) {
        mActivity = activity;
        this.list = list;
        this.datelist = datelist;
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
            view = mLayoutInflater.inflate(R.layout.attendance_item, null);
        }

        tvClockInTime = view.findViewById(R.id.tvClockInTime);
        tvClockInStatus = view.findViewById(R.id.tvClockInStatus);

        String theDate = datelist.get(i);

        tvClockInTime.setText(theDate);
        tvClockInStatus.setText("Absebce");

        for(Attendance attendance: list){
            Log.e("viewAdapter", "attendancelist: " + attendance.getClockInDate() + ", theDate: " + theDate + ", position: " + i);

            if(theDate.equals(attendance.getClockInDate())){
                tvClockInStatus.setText("Present");
                Log.d("TAG", "theDate: " + theDate + "clockInDate: " + attendance.getClockInDate());
            }
        }

        return view;
    }
}
