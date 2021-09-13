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

import com.example.eam.adapter.RecordAdapter;
import com.example.eam.adapter.ViewAttendanceAdapter;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityViewAttendanceBinding;
import com.example.eam.managers.SessionManager;
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
import java.util.HashMap;
import java.util.List;

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
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_attendance);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userID");

        firestore = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        setDate();

        /*binding.recyclerView.setLayoutManager(new LinearLayoutManager(ViewAttendanceActivity.this));
        ViewAttendanceAdapter viewAttendanceAdapter = new ViewAttendanceAdapter(list, datelist, leavelist,ViewAttendanceActivity.this);
        binding.recyclerView.setAdapter(viewAttendanceAdapter);

        if(viewAttendanceAdapter != null){
            viewAttendanceAdapter.notifyItemInserted(0);
            viewAttendanceAdapter.notifyDataSetChanged();

            Log.d(TAG, "onSuccess: adapter" + viewAttendanceAdapter.getItemCount());
        }*/

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
        list.clear();

        for(String dates : datelist){
            reference.child(companyID).child("Attendance").orderByChild("clockInDate").equalTo(dates).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //list.clear();

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Attendance attendance = snapshot.getValue(Attendance.class);

                        if (attendance != null && attendance.getUserId().equals(userId) && attendance.getClockInDate().equals(dates)){
                            list.add(attendance);

                            Log.d(TAG, "datelist: " + datelist);
                            Log.d(TAG, "listSize: " + list);

                            Log.d(TAG, "user: " + attendance.getClockInDate());
                            Log.d(TAG, "userId: " + attendance.getUserId());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        reference.child(companyID).child("Leaves").orderByChild("requester").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                leavelist.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Leave leave = snapshot.getValue(Leave.class);

                    leavelist.add(leave);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(ViewAttendanceActivity.this));
        ViewAttendanceAdapter viewAttendanceAdapter = new ViewAttendanceAdapter(list, datelist, leavelist,ViewAttendanceActivity.this);
        binding.recyclerView.setAdapter(viewAttendanceAdapter);

        if(viewAttendanceAdapter != null){
            viewAttendanceAdapter.notifyItemInserted(0);
            viewAttendanceAdapter.notifyDataSetChanged();

            Log.d(TAG, "onSuccess: adapter" + viewAttendanceAdapter.getItemCount());
        }
    }

    private void setDate() {
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

        setAdapter();
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

                String date = day1 + "-" + month1 + "-" + year;
                tvDate.setText(date);

                resetDate();
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    private void resetDate() {
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

            datelist = new ArrayList<>(days);
            for (int i=0; i < days; i++) {
                LocalDate d = dateFrom2.withFieldAdded(DurationFieldType.days(), i);
                String date = Common.getJodaTimeFormattedDate(d);
                datelist.add(date);
            }

            setAdapter();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        //LocalDate startDate = LocalDate.parse(start);
        //LocalDate endDate = LocalDate.parse(end);

        /*int days = Days.daysBetween(startDate, endDate).getDays();
        datelist = new ArrayList<>(days);
        for (int i=0; i < days; i++) {
            LocalDate d = startDate.withFieldAdded(DurationFieldType.days(), i);
            String date = Common.getJodaTimeFormattedDate(d);
            datelist.add(date);
        }
        datelist.add(end);

        setAdapter();*/
    }
}