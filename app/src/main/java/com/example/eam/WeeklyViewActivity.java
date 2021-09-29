package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.eam.adapter.CalendarAdapter;
import com.example.eam.adapter.RecordAdapter;
import com.example.eam.common.CalendarUtils;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityWeeklyViewBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Leave;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.example.eam.common.CalendarUtils.daysInWeekArray;
import static com.example.eam.common.CalendarUtils.monthYearFromDate;

public class WeeklyViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private static final String TAG = "WeeklyViewActivity";
    private ActivityWeeklyViewBinding binding;
    private CalendarAdapter adapter;
    //private RecordAdapter recordAdapter;
    private List<Attendance> list = new ArrayList<>();
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;
    private DatabaseReference reference;
    private Attendance attendance;
    //private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weekly_view);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        String date = getIntent().getExtras().getString("date");

        if(date != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate date2 = LocalDate.parse(date, formatter);

            CalendarUtils.selectedDate = date2;
        }
        else{
            CalendarUtils.selectedDate = LocalDate.now();
        }

        //CalendarUtils.selectedDate = LocalDate.now();
        setWeekView();
        initBtnClick();
    }

    private void initBtnClick() {
        binding.btnPreviousWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
                setWeekView();
            }
        });
        binding.btnNextWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
                setWeekView();
            }
        });
    }

    private void setWeekView() {
        binding.monthYearTV.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);

        adapter = new CalendarAdapter(days, WeeklyViewActivity.this, this);
        //RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        binding.calendarRecyclerView.setLayoutManager(new GridLayoutManager(WeeklyViewActivity.this, 7));
        binding.calendarRecyclerView.setAdapter(adapter);
        setEventAdpater();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        CalendarUtils.selectedDate = date;
        if(!date.equals("")) {
            String message = "Selected Date " + date;
            //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
        setWeekView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //setEventAdpater();
    }

    private void setEventAdpater() {
        //ArrayList<Event> dailyEvents = Event.eventsForDate(CalendarUtils.selectedDate);
        //RecordAdapter recordAdapter = new RecordAdapter(getApplicationContext(), dailyEvents);
        //binding.recyclerView.setAdapter(recordAdapter);
        LocalDate date = CalendarUtils.selectedDate;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        reference.child(companyID).child("Attendance").orderByChild("userId").equalTo(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Attendance attendance = snapshot.getValue(Attendance.class);

                    if (attendance != null && attendance.getClockInDate().equals(date.format(formatter))){
                        list.add(attendance);
                    }


                    /*try {
                        if (attendance != null && attendance.getClockInDate().equals(date)){
                            list.add(attendance);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }*/

                    //attendance.setClockInTime();
                    //String userID = Objects.requireNonNull(snapshot.child("chatID").getValue()).toString();
                    //Log.d(TAG, "onDataChange: userid" + userID);

                    //list.add(userID);
                }

                Log.d(TAG, "listSize: " + list.size());

                if(list.size() > 0){
                    binding.tvNoRecord.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(WeeklyViewActivity.this));
                    RecordAdapter recordAdapter = new RecordAdapter(list, WeeklyViewActivity.this);
                    binding.recyclerView.setAdapter(recordAdapter);
                }
                else{
                    binding.tvNoRecord.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                }

                //binding.recyclerView.setLayoutManager(new LinearLayoutManager(WeeklyViewActivity.this));
                //RecordAdapter recordAdapter = new RecordAdapter(list, WeeklyViewActivity.this);
                //binding.recyclerView.setAdapter(recordAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}