package com.example.eam.menu;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.example.eam.AddUserActivity;
import com.example.eam.EmployeeListActivity;
import com.example.eam.R;
import com.example.eam.ReviewLeaveActivity;
import com.example.eam.adapter.LeaveReviewAdapter;
import com.example.eam.common.Common;
import com.example.eam.databinding.FragmentAdminBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Leave;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

public class AdminFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener{

    private static final String TAG = "ProfileFragment";

    public AdminFragment() {
        // Required empty public constructor
    }

    private FragmentAdminBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    //ArrayList<Entry> yValues;

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i(TAG, "onChartGestureStart: X" + me.getX() + "Y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i(TAG, "onChartGestureEnd: X" + lastPerformedGesture);
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i(TAG, "onChartLongPressed: ");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i(TAG, "onChartDoubleTapped: ");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i(TAG, "onChartSingleTapped: ");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i(TAG, "onChartFling: velox" + velocityX + ", veloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i(TAG, "onChartScale: ScaleX" + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i(TAG, "onChartTranslate: dX" + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i(TAG, "onValueSelected: " + e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i(TAG, "onNothingSelected: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin, container, false);

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        binding.btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddUserActivity.class));
            }
        });

        binding.btnUserList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EmployeeListActivity.class));
            }
        });

        binding.btnLeaveRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ReviewLeaveActivity.class));
            }
        });

        //displayChart();

        return binding.getRoot();
    }

    private void displayChart() {
        binding.chart.setOnChartGestureListener(this);
        binding.chart.setOnChartValueSelectedListener(this);

        binding.chart.setDragEnabled(true);
        binding.chart.setScaleEnabled(false);

        /*LimitLine upper_limit = new LimitLine(65f, "Danger");
        upper_limit.setLineWidth(4f);
        upper_limit.enableDashedLine(10f, 10f, 0f);
        upper_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        upper_limit.setTextSize(15f);

        LimitLine lower_limit = new LimitLine(35f, "Too Low");
        lower_limit.setLineWidth(4f);
        lower_limit.enableDashedLine(10f, 10f, 0f);
        lower_limit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        lower_limit.setTextSize(15f);*/

        YAxis leftAxis = binding.chart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        //leftAxis.addLimitLine(upper_limit);
        //leftAxis.addLimitLine(lower_limit);
        //leftAxis.setAxisMaximum(100f);
        //leftAxis.setAxisMinimum(25f);
        //leftAxis.enableGridDashedLine(10f, 10f, 0);
        //leftAxis.setDrawLimitLinesBehindData(true);


        //view
        binding.chart.getAxisRight().setEnabled(false);
        binding.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        //Remove background grid lines
        binding.chart.getAxisLeft().setDrawGridLines(false);
        binding.chart.getXAxis().setDrawGridLines(false);
        binding.chart.getAxisRight().setDrawGridLines(false);
        //Hide y-axis label
        //binding.chart.getAxisLeft().setDrawLabels(false);
        //Hide legend
        binding.chart.getLegend().setEnabled(false);
        //Set background color
        binding.chart.setBackgroundColor(Color.GRAY);
        binding.chart.setDrawGridBackground(false);
        //remove description label
        binding.chart.getDescription().setEnabled(false);

        ArrayList<Entry> yValues = new ArrayList();
        //yValues = new ArrayList();

        LocalDate currDate = new LocalDate();
        LocalDate fiveDaysAgo = currDate.minusDays(4);

        int days = Days.daysBetween(fiveDaysAgo, currDate).getDays();
        List<String> datelist = new ArrayList<>(days);  // Set initial capacity to `days`.
        for (int i=0; i < days; i++) {
            LocalDate d = fiveDaysAgo.withFieldAdded(DurationFieldType.days(), i);
            String date = Common.getJodaTimeFormattedDate(d);
            datelist.add(date);
        }
        String date = Common.getJodaTimeFormattedDate(currDate);
        datelist.add(date);

        Log.d(TAG, "dates: " + datelist);

        for(int i=0; i < datelist.size(); i++){
            String dates = datelist.get(i);
            int finali = i;

        //for(String dates : datelist){
            reference.child(companyID).child("Attendance").orderByChild("clockInDate").equalTo(dates).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        yValues.add(new Entry(finali, (float) dataSnapshot.getChildrenCount()));
                        Log.d(TAG, dates + "= " + dataSnapshot.getChildrenCount());
                        Log.d(TAG, "inlist: " + yValues);
                    }
                    else{
                        yValues.add(new Entry(finali, (float) 0));
                        Log.d(TAG, dates + "= Does not exists");
                        Log.d(TAG, "inlist: " + yValues);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        Log.d(TAG, "list: " + yValues);

        yValues.add(new Entry(0, 50f));
        yValues.add(new Entry(1, 70f));
        yValues.add(new Entry(2, 30f));
        yValues.add(new Entry(3, 50f));
        yValues.add(new Entry(4, 60f));

        LineDataSet set1 = new LineDataSet(yValues, "Data Set 1");
        set1.setFillAlpha(110);

        //line customization
        set1.setCircleColor(Color.BLUE);
        set1.setCircleHoleColor(Color.BLACK);
        set1.setLineWidth(2f);
        set1.setCircleRadius(5f);
        set1.setColor(Color.BLUE);
        set1.setValueTextSize(10f);
        set1.setValueTextColor(Color.GRAY);
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(set1);
        binding.chart.setData(data);

        String[] values = new String[]{"Jan", "Feb", "March", "Apr", "May"};

        XAxis xAxis = binding.chart.getXAxis();
        //xAxis.setValueFormatter(new MyAxisValueFormatter(datelist.toArray(new String[0])));
        xAxis.setValueFormatter(new MyAxisValueFormatter(values));
        xAxis.setGranularity(1f);

    }

    public class MyAxisValueFormatter extends ValueFormatter{
        private String[] mValues;

        public MyAxisValueFormatter(String[] values){
            this.mValues = values;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis){
            //return mValues[(int) value];
            return mValues[(int) value];
        }
    }

    public interface OnCallBack{
        void onSuccess();
        void onFailed(Exception e);
    }
}