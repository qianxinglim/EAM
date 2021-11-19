package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.adapter.RecordAdapter;
import com.example.eam.adapter.ViewAttendanceAdapter;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityViewAttendanceBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.menu.PunchFragment;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;
import com.example.eam.model.User;
import com.github.mikephil.charting.data.Entry;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ViewAttendanceActivity extends AppCompatActivity {
    private static final String TAG = "ViewAttendanceActivity";
    private ActivityViewAttendanceBinding binding;
    private List<String> datelist;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private List<Attendance> list = new ArrayList<>();
    private List<Leave> leavelist = new ArrayList<>();
    private List<String> leaveDateList = new ArrayList<>();
    private String userId, userProfilePic, userName;
    private int presentCount, punchMissed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_attendance);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userID");
        userProfilePic = intent.getStringExtra("userProfilePic");
        userName = intent.getStringExtra("userName");

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        if(userProfilePic == null || userProfilePic.equals("")){
            binding.tvProfilePic.setImageResource(R.drawable.icon_male_ph);
        }
        else{
            Glide.with(this).load(userProfilePic).into(binding.tvProfilePic);
        }

        binding.tvTimesheet.setText("Timesheet for " + userName);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);

        setDate(new OnCallBack() {
            @Override
            public void onSuccess() {
                setAdapter();
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
    }

    private void setAdapter() {
        getAttendanceList(new OnCallBack() {
            @Override
            public void onSuccess() {
                getLeaveList(new OnCallBack() {
                    @Override
                    public void onSuccess() {

                        //Set leave count and absence count
                        int absenceCount = datelist.size() - presentCount;

                        for(String theDate : datelist) {
                            for (Leave leave : leavelist) {
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

                                            if (date.equals(theDate)) {
                                                leaveDateList.add(theDate);
                                            }
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    if (leave.getDate().equals(theDate)) {
                                        leaveDateList.add(theDate);
                                    }
                                }
                            }
                        }

                        //remove repeated dates
                        Set<String> set = new HashSet<>(leaveDateList);
                        leaveDateList.clear();
                        leaveDateList.addAll(set);

                        if(leaveDateList.size() > 1){
                            binding.tvLeave.setText(leaveDateList.size() + " days");
                        }
                        else{
                            binding.tvLeave.setText(leaveDateList.size() + " day");
                        }

                        if(absenceCount > 1){
                            binding.tvAbsence.setText(absenceCount + " days");
                        }
                        else{
                            binding.tvAbsence.setText(absenceCount + " day");
                        }

                        if(punchMissed > 1){
                            binding.tvPunchMissed.setText(punchMissed + " days");
                        }
                        else{
                            binding.tvPunchMissed.setText(punchMissed + "day");
                        }

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewAttendanceActivity.this);
                        linearLayoutManager.setReverseLayout(true);
                        linearLayoutManager.setStackFromEnd(true);
                        binding.recyclerView.setLayoutManager(linearLayoutManager);
                        ViewAttendanceAdapter viewAttendanceAdapter = new ViewAttendanceAdapter(userName, userProfilePic, list, datelist, leavelist,ViewAttendanceActivity.this);
                        binding.recyclerView.setAdapter(viewAttendanceAdapter);

                        if(viewAttendanceAdapter != null){
                            viewAttendanceAdapter.notifyItemInserted(0);
                            viewAttendanceAdapter.notifyDataSetChanged();
                        }

                        binding.progressBar.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }

    private void getAttendanceList(final OnCallBack onCallBack) {
        list.clear();
        presentCount = 0;
        punchMissed = 0;

        for(String dates : datelist){
            reference.child(companyID).child("Attendance").orderByChild("clockInDate").equalTo(dates).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //list.clear();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Attendance attendance = snapshot.getValue(Attendance.class);

                        if (attendance != null && attendance.getUserId().equals(userId) && attendance.getClockInDate().equals(dates)){
                            list.add(attendance);

                            presentCount = presentCount + 1;

                            if(attendance.getClockOutDate() != null) {
                                if (!attendance.getClockInDate().equals(attendance.getClockOutDate())) {
                                    punchMissed = punchMissed + 1;
                                }
                            }
                            else{
                                SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
                                Date dateTime = new Date();
                                String currDate = formatter2.format(dateTime);

                                if(!attendance.getClockInDate().equals(currDate)){
                                    punchMissed = punchMissed + 1;
                                }
                            }
                        }
                    }

                    if(list != null){
                        onCallBack.onSuccess();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getLeaveList(final OnCallBack onCallBack) {
        reference.child(companyID).child("Leaves").orderByChild("requester").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                leavelist.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Leave leave = snapshot.getValue(Leave.class);

                    if (leave != null && leave.getStatus().equals("Approved")) {
                        //leaveCount = leaveCount + 1;

                        leavelist.add(leave);
                        Log.e(TAG, "leavelist: " + leave.getStatus());
                    }
                }

                if(leavelist != null){
                    onCallBack.onSuccess();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setDate(final OnCallBack onCallBack) {
        LocalDate currDate = new LocalDate();
        LocalDate daysAgo = currDate.minusDays(15);

        String currdate = Common.getJodaTimeFormattedDate(currDate);
        String daysago = Common.getJodaTimeFormattedDate(daysAgo);

        binding.tvStartDate.setText(daysago);
        binding.tvEndDate.setText(currdate);

        int days = Days.daysBetween(daysAgo, currDate).getDays();
        datelist = new ArrayList<>(days);
        for (int i=0; i < days; i++) {
            LocalDate d = daysAgo.withFieldAdded(DurationFieldType.days(), i);
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

        DatePickerDialog datePickerDialog = new DatePickerDialog(ViewAttendanceActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                String chosenDate = tvDate.getText().toString();

                String date = day1 + "-" + month1 + "-" + year;
                tvDate.setText(date);

                validateDates(chosenDate, tvDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void validateDates(String chosenDate, TextView tvDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        try{
            Date dateFrom = sdf.parse(binding.tvStartDate.getText().toString());
            Date dateTo = sdf.parse(binding.tvEndDate.getText().toString());

            int days = Days.daysBetween(new LocalDate(dateFrom), new LocalDate(dateTo)).getDays();

            if(days <= 0){
                tvDate.setText(chosenDate);
                Toast.makeText(this, "End date cannot be earlier than start date.", Toast.LENGTH_SHORT).show();
//                binding.tvStartDate.setText(chosenDate);
//                binding.tvEndDate.setText(chosenDate);
            }
            else{
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.recyclerView.setVisibility(View.GONE);

                resetDate(new OnCallBack() {
                    @Override
                    public void onSuccess() {
                        setAdapter();
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                });
            }
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    private void resetDate(final OnCallBack onCallBack) {
        String start = binding.tvStartDate.getText().toString();
        String end = binding.tvEndDate.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String dateFrom = formatter.format(sdf.parse(start));
            String dateTo = formatter.format(sdf.parse(end));
            LocalDate dateFrom2 = new LocalDate(dateFrom);
            LocalDate dateTo2 = new LocalDate(dateTo);
            LocalDate dateto = dateTo2.plusDays(1);
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