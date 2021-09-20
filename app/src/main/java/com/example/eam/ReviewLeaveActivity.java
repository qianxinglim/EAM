package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.eam.adapter.CalendarAdapter;
import com.example.eam.adapter.LeaveReviewAdapter;
import com.example.eam.adapter.RecordAdapter;
import com.example.eam.common.CalendarUtils;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityReviewLeaveBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ReviewLeaveActivity extends AppCompatActivity {
    private static final String TAG = "ReviewLeaveActivity";
    private ActivityReviewLeaveBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private List<String> datelist;
    //private CalendarAdapter adapter;
    private List<Leave> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_review_leave);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);

        setDate(new OnCallBack() {
            @Override
            public void onSuccess() {
                getLeaveApplications();
            }

            @Override
            public void onFailed(Exception e) {

            }
        });

        binding.tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(binding.tvStartDate);
            }
        });

        binding.tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDate(binding.tvEndDate);
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
                finish();
            }
        });

        //getLeaveApplications();
    }

    private void bottomSheetShow() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ReviewLeaveActivity.this, R.style.BottomSheetDialogTheme);

        View bottomSheetView = LayoutInflater.from(ReviewLeaveActivity.this).inflate(R.layout.bottom_sheet_filter_leave_requests, null);

        bottomSheetView.findViewById(R.id.btnAll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnFilter.setText("All requests");
                bottomSheetDialog.dismiss();
                getLeaveApplications();
            }
        });

        bottomSheetView.findViewById(R.id.btnPending).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnFilter.setText("Pending");
                bottomSheetDialog.dismiss();
                getLeaveApplications();
            }
        });

        bottomSheetView.findViewById(R.id.btnApproved).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnFilter.setText("Approved");
                bottomSheetDialog.dismiss();
                getLeaveApplications();
            }
        });

        bottomSheetView.findViewById(R.id.btnDeclined).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.btnFilter.setText("Declined");
                bottomSheetDialog.dismiss();
                getLeaveApplications();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void getLeaveApplications(){
        list.clear();

        for(String dates : datelist) {
            reference.child(companyID).child("Leaves").orderByChild("requestDate").equalTo(dates).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Leave leave = snapshot.getValue(Leave.class);
                        leave.setLeaveId(snapshot.getKey());

                        if(binding.btnFilter.getText().toString().equals("Approved")){
                            if(leave.getStatus().equals("Approved")){
                                list.add(leave);
                            }
                        }
                        else if(binding.btnFilter.getText().toString().equals("Declined")){
                            if(leave.getStatus().equals("Declined")){
                                list.add(leave);
                            }
                        }
                        else if(binding.btnFilter.getText().toString().equals("Pending")){
                            if(leave.getStatus().equals("Pending")){
                                list.add(leave);
                            }
                        }
                        else{
                            list.add(leave);
                        }

                        //list.add(leave);
                        Log.d(TAG, "attachments: " + leave.getAttachments());
                    }

                    Log.d(TAG, "listSize: " + list.size());

                    if (list.size() > 0) {
                        //binding.tvNoRecord.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);

                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ReviewLeaveActivity.this));
                        LeaveReviewAdapter adapter = new LeaveReviewAdapter(list, ReviewLeaveActivity.this);
                        binding.recyclerView.setAdapter(adapter);

                        binding.progressBar.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        //binding.tvNoRecord.setVisibility(View.VISIBLE);
                        //binding.recyclerView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void setDate(final OnCallBack onCallBack) {
        org.joda.time.LocalDate currDate = new org.joda.time.LocalDate();
        org.joda.time.LocalDate daysAgo = currDate.minusDays(15);

        String currdate = Common.getJodaTimeFormattedDate(currDate);
        String daysago = Common.getJodaTimeFormattedDate(daysAgo);

        binding.tvStartDate.setText(daysago);
        binding.tvEndDate.setText(currdate);

        int days = Days.daysBetween(daysAgo, currDate).getDays();
        datelist = new ArrayList<>(days);
        for (int i=0; i < days; i++) {
            org.joda.time.LocalDate d = daysAgo.withFieldAdded(DurationFieldType.days(), i);
            String date = Common.getJodaTimeFormattedDate(d);
            datelist.add(date);
        }
        datelist.add(currdate);

        if(datelist != null){
            onCallBack.onSuccess();
        }
    }

    private void getDate(TextView tvDate) {
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ReviewLeaveActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                tvDate.setText(date);

                binding.progressBar.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);

                resetDate(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        getLeaveApplications();
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void resetDate(final OnCallBack onCallBack) {
        String start = binding.tvStartDate.getText().toString();
        String end = binding.tvEndDate.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String dateFrom = formatter.format(sdf.parse(start));
            String dateTo = formatter.format(sdf.parse(end));
            org.joda.time.LocalDate dateFrom2 = new org.joda.time.LocalDate(dateFrom);
            org.joda.time.LocalDate dateTo2 = new org.joda.time.LocalDate(dateTo);
            org.joda.time.LocalDate dateto = dateTo2.plusDays(1);
            int days = Days.daysBetween(dateFrom2, dateto).getDays();

            datelist.clear();

            datelist = new ArrayList<>(days);
            for (int i=0; i < days; i++) {
                LocalDate d = dateFrom2.withFieldAdded(DurationFieldType.days(), i);
                String date = Common.getJodaTimeFormattedDate(d);
                datelist.add(date);
            }

            if(datelist != null){
                onCallBack.onSuccess();
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public interface OnCallBack{
        void onSuccess();
        void onFailed(Exception e);
    }
}