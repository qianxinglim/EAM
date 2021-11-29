package com.example.eam.menu;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eam.ContactActivity;
import com.example.eam.R;
import com.example.eam.ReviewLeaveActivity;
import com.example.eam.ViewAttendanceActivity;
import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.adapter.TimesheetsAdapter;
import com.example.eam.adapter.TodayTimesheetsAdapter;
import com.example.eam.adapter.ViewAttendanceAdapter;
import com.example.eam.common.Common;
import com.example.eam.databinding.FragmentTimesheetsBinding;
import com.example.eam.databinding.FragmentTodayBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Chatlist;
import com.example.eam.model.User;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TodayFragment extends Fragment {

    private static final String TAG = "TodayFragment";

    public TodayFragment() {
        // Required empty public constructor
    }

    private FragmentTodayBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private List<User> userList;
    private List<Attendance> list;
    private List<User> clockedInUserList;
    private List<User> noClockedInUserList;
    private TodayTimesheetsAdapter todayTimesheetsAdapter;
    private int filter = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_today, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        list = new ArrayList<>();
        userList = new ArrayList<>();

        LocalDate currDate = new LocalDate();
        displayChart(currDate);

//        getAttendanceList(new OnCallBack() {
//            @Override
//            public void onSuccess() {
//                getUserList(new OnCallBack() {
//                    @Override
//                    public void onSuccess() {
//                        compareLists();
//                    }
//
//                    @Override
//                    public void onFailed(Exception e) {
//
//                    }
//                });
//            }
//
//            @Override
//            public void onFailed(Exception e) {
//
//            }
//        });

        getUserList(new OnCallBack() {
            @Override
            public void onSuccess() {
                getAttendanceList();
            }

            @Override
            public void onFailed(Exception e) {

            }
        });

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetShow();
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        binding.btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(binding.btnDate);
            }
        });

        return binding.getRoot();
    }

    private void filter(String text) {
        //binding.recyclerView.setVisibility(View.GONE);
        //binding.progressBar.setVisibility(View.VISIBLE);

        ArrayList<User> searchList = new ArrayList<>();

        switch(filter){
            case 0:
                for(User user : userList){
                    if(user.getName().toLowerCase().contains(text.toLowerCase())){
                        searchList.add(user);
                    }
                }

                break;

            case 1:
                for(User user : clockedInUserList){
                    if(user.getName().toLowerCase().contains(text.toLowerCase())){
                        searchList.add(user);
                    }
                }

                break;

            case 2:
                for(User user : noClockedInUserList){
                    if(user.getName().toLowerCase().contains(text.toLowerCase())){
                        searchList.add(user);
                    }
                }

                break;
        }

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        todayTimesheetsAdapter = new TodayTimesheetsAdapter(searchList, list, getContext());
        binding.recyclerView.setAdapter(todayTimesheetsAdapter);
        binding.recyclerView.setVisibility(View.VISIBLE);
        //binding.progressBar.setVisibility(View.GONE);
    }

    private void bottomSheetShow() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_filter_today_attendance, null);

        bottomSheetView.findViewById(R.id.btnAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //binding.btnFilter.setText("All");
                bottomSheetDialog.dismiss();

                filter = 0;

                binding.tvCount.setText(String.valueOf(clockedInUserList.size()));
                binding.tvCountDes.setText("/" + userList.size() + " Users clocked in");

                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                todayTimesheetsAdapter = new TodayTimesheetsAdapter(userList, list, getContext());
                binding.recyclerView.setAdapter(todayTimesheetsAdapter);
            }
        });

        bottomSheetView.findViewById(R.id.btnClockedIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //binding.btnFilter.setText("Clocked in");
                bottomSheetDialog.dismiss();

                filter = 1;

                binding.tvCount.setText(String.valueOf(clockedInUserList.size()));
                binding.tvCountDes.setText("/" + userList.size() + " Users clocked in");

                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                todayTimesheetsAdapter = new TodayTimesheetsAdapter(clockedInUserList, list, getContext());
                binding.recyclerView.setAdapter(todayTimesheetsAdapter);
            }
        });

        bottomSheetView.findViewById(R.id.btnNotClockedIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //binding.btnFilter.setText("Not Clocked in");
                bottomSheetDialog.dismiss();

                filter = 2;

                binding.tvCount.setText(String.valueOf(noClockedInUserList.size()));
                binding.tvCountDes.setText("/" + userList.size() + " Users did not clocked in");

                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                todayTimesheetsAdapter = new TodayTimesheetsAdapter(noClockedInUserList, list, getContext());
                binding.recyclerView.setAdapter(todayTimesheetsAdapter);
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void compareLists(final OnCallBack onCallBack){
        clockedInUserList = new ArrayList<>();
        noClockedInUserList = new ArrayList<>();

        if(userList.size() > 0 && list.size() > 0) {
            for (User user : userList) {
                for (Attendance attendance : list) {
                    Log.e(TAG, "attendance user id: " + attendance.getUserId() + ", user id: " + user.getID());

                    if (user.getID().equals(attendance.getUserId())) {
                        clockedInUserList.add(user);
                    }
//                    else {
//                        noClockedInUserList.add(user);
//                    }
                }
            }

            for(User user : userList){
                if(!clockedInUserList.contains(user)){
                    noClockedInUserList.add(user);
                }
            }
        }
        else{
            //Collections.copy(noClockedInUserList, userList);
            noClockedInUserList = new ArrayList<>(userList);
        }

        Log.e(TAG, "yoyoyo, " + "clockedInUserList: " + clockedInUserList + ", noClockedInUserList: " + noClockedInUserList + ", userList: " + userList + ", list: " + list);
        onCallBack.onSuccess();
    }

    private void getUserList(final OnCallBack onCallBack){
        firestore.collection("Companies").document(companyID).collection("Users").orderBy("department", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                userList.clear();

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getId() != null) {
                        User user = new User(doc.getString("id"), doc.getString("name"), doc.getString("phoneNo"), doc.getString("profilePic"), doc.getString("email"),"", doc.getString("title"), doc.getString("department"),doc.getString("clockInTime"),doc.getString("clockOutTime"),doc.getLong("minutesOfWork").intValue());

                        userList.add(user);
                    }
                }

                onCallBack.onSuccess();

                //getAttendanceList();
                //onCallBack.onSuccess();

//                binding.tvCount.setText(String.valueOf(clockedInUserList.size()));
//                binding.tvCountDes.setText("/" + userList.size() + " Users clocked in");
//
//                binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//                todayTimesheetsAdapter = new TodayTimesheetsAdapter(userList, list, getContext());
//                binding.recyclerView.setAdapter(todayTimesheetsAdapter);
//
//                if(todayTimesheetsAdapter != null){
//                    todayTimesheetsAdapter.notifyItemInserted(0);
//                    todayTimesheetsAdapter.notifyDataSetChanged();
//                }
            }
        });
    }

    private void getAttendanceList() {
        SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
        Date dateTime = new Date();
        String currDate = formatter2.format(dateTime);
        String queryDate;

        if(binding.btnDate.getText().toString().equals("Today")){
            queryDate = currDate;
        }
        else{
            queryDate = binding.btnDate.getText().toString();
        }

        Log.d(TAG, "querydate: " + queryDate);

        reference.child(companyID).child("Attendance").orderByChild("clockInDate").equalTo(queryDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Attendance attendance = snapshot.getValue(Attendance.class);

                    list.add(attendance);
                }

//                if(list != null){
//                    onCallBack.onSuccess();
//                }

                compareLists(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        binding.tvCount.setText(String.valueOf(clockedInUserList.size()));
                        binding.tvCountDes.setText("/" + userList.size() + " Users clocked in");

                        Log.e(TAG, "clockedInUserList: " + clockedInUserList + ", noClockedInUserList: " + noClockedInUserList + ", userList: " + userList);

                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        todayTimesheetsAdapter = new TodayTimesheetsAdapter(userList, list, getContext());
                        binding.recyclerView.setAdapter(todayTimesheetsAdapter);

                        if(todayTimesheetsAdapter != null) {
                            todayTimesheetsAdapter.notifyItemInserted(0);
                            todayTimesheetsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDate(TextView tvDate) {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                String day1 = String.valueOf(day), month1 = String.valueOf(month);

                if(day < 10){
                    day1 = "0" + day;
                }
                if(month < 10){
                    month1 = "0" + month;
                }

                String date = day1 + "-" + month1 + "-" + year;

                SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
                Date dateTime = new Date();
                String currDate = formatter2.format(dateTime);

                if(date.equals(currDate)){
                    tvDate.setText("Today");
                }
                else{
                    tvDate.setText(date);
                }

                //binding.progressBar.setVisibility(View.VISIBLE);
                //binding.recyclerView.setVisibility(View.GONE);

                getAttendanceList();

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    String selectedDate = formatter.format(sdf.parse(date));
                    LocalDate selectedDate2 = new LocalDate(selectedDate);

                    displayChart(selectedDate2);
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void displayChart(LocalDate currDate) {

        //axis lines
        binding.chart.getAxisRight().setEnabled(false);
        binding.chart.getAxisLeft().setEnabled(false);
        binding.chart.getAxisLeft().removeAllLimitLines();
        binding.chart.getAxisRight().removeAllLimitLines();
        binding.chart.getAxisLeft().setDrawLabels(false);
        binding.chart.getAxisRight().setDrawLabels(false);
        binding.chart.getAxisLeft().setDrawAxisLine(false);
        binding.chart.getXAxis().setDrawAxisLine(false);
        binding.chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        //Remove background grid lines
        binding.chart.getAxisLeft().setDrawGridLines(false);
        binding.chart.getXAxis().setDrawGridLines(false);
        binding.chart.getAxisRight().setDrawGridLines(false);
        //Hide legend
        binding.chart.getLegend().setEnabled(false);
        //Set background color
        binding.chart.setBackgroundColor(Color.WHITE);
        binding.chart.setDrawGridBackground(false);
        //remove description label
        binding.chart.getDescription().setEnabled(false);
        //Set text color & size
        binding.chart.getXAxis().setTextColor(Color.parseColor("#808080"));
        binding.chart.getLegend().setTextColor(Color.parseColor("#5ebf95"));
        binding.chart.getDescription().setTextColor(Color.parseColor("#5ebf95"));
        binding.chart.getXAxis().setTextSize(12);
        //Set margins
        binding.chart.getXAxis().setSpaceMin(0.1f);
        binding.chart.getXAxis().setSpaceMax(0.1f);
        binding.chart.getXAxis().setYOffset(1);

        LocalDate fiveDaysAgo = currDate.minusDays(4);
        LocalDate dateto = currDate.plusDays(1);

        int days = Days.daysBetween(fiveDaysAgo, dateto).getDays();
        List<String> datelist = new ArrayList<>(days);  // Set initial capacity to `days`.
        List<String> datelist2 = new ArrayList<>(days);  // Set initial capacity to `days`.
        for (int i=0; i < days; i++) {
            LocalDate d = fiveDaysAgo.withFieldAdded(DurationFieldType.days(), i);
            String date = Common.getJodaTimeFormattedDate(d);
            String date2 = Common.getJodaTimeFormattedDate2(d);
            datelist.add(date);
            datelist2.add(date2);
        }
        Collections.sort(datelist);

        Log.d(TAG, "dates: " + datelist);
        Log.d(TAG, "datelist2: " + datelist2);

        ArrayList<Entry> yValues = new ArrayList();

        for(int i=0; i < datelist.size(); i++){
            String dates = datelist.get(i);
            int finali = i;

            reference.child(companyID).child("Attendance").orderByChild("clockInDate").equalTo(dates).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //if(dataSnapshot.exists()){
                    yValues.add(new Entry(finali, dataSnapshot.getChildrenCount()));

                    if(yValues.size() == datelist.size()){
                        displayChart2(yValues, datelist2);
                    }

                    Log.d(TAG, dates + "= " + dataSnapshot.getChildrenCount());
                    Log.d(TAG, "inlist: " + yValues);
                    Log.d(TAG, "finali: " + finali);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void displayChart2(ArrayList<Entry> yValues, List<String> datelist2){

        yValues.sort(Comparator.comparing(Entry::getX));

        Log.d(TAG, "inlist2: " + yValues);

        LineDataSet set1 = new LineDataSet(yValues, "Data Set 1");
        set1.setFillAlpha(110);
        //line customization
        set1.setCircleColor(Color.parseColor("#5ebf95"));
        set1.setCircleHoleColor(Color.parseColor("#5ebf95"));
        set1.setLineWidth(3f);
        set1.setCircleRadius(5f);
        set1.setColor(Color.parseColor("#5ebf95"));
        set1.setValueTextSize(10f);
        set1.setValueTextColor(Color.BLACK);
        //set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);

        //set value text from float to int
        set1.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int)value);
            }
        });

        //set graph fill color
        set1.setDrawFilled(true);
        if (Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_white);
            set1.setFillDrawable(drawable);
        }
        else {
            set1.setFillColor(Color.parseColor("#5ebf95"));
        }

        //Set data
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        LineData data = new LineData(set1);
        binding.chart.setData(data);

        XAxis xAxis = binding.chart.getXAxis();
        xAxis.setValueFormatter(new MyAxisValueFormatter(datelist2.toArray(new String[0])));
        xAxis.setGranularity(1f);

        binding.chart.invalidate();
        binding.chart.refreshDrawableState();
    }

    public class MyAxisValueFormatter extends ValueFormatter {
        private String[] mValues;

        public MyAxisValueFormatter(String[] values){
            this.mValues = values;
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis){
            return mValues[(int) value];
        }
    }

    public interface OnCallBack{
        void onSuccess();
        void onFailed(Exception e);
    }
}