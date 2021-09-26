package com.example.eam.menu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eam.R;
import com.example.eam.WeeklyViewActivity;
import com.example.eam.adapter.CalendarAdapter;
import com.example.eam.adapter.RecordAdapter;
import com.example.eam.common.CalendarUtils;
import com.example.eam.databinding.FragmentCalendarBinding;
import com.example.eam.databinding.FragmentPunchBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import static com.example.eam.common.CalendarUtils.daysInWeekArray;
import static com.example.eam.common.CalendarUtils.monthYearFromDate;

public class CalendarFragment extends Fragment {

    private static final String TAG = "PunchFragment";

    public CalendarFragment() {
        // Required empty public constructor
    }

    private FragmentCalendarBinding binding;
    private CalendarAdapter adapter;
    //private RecordAdapter recordAdapter;
    private List<Attendance> list = new ArrayList<>();
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;
    private DatabaseReference reference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        CalendarUtils.selectedDate = LocalDate.now();
        setWeekView();
        initBtnClick();

        return binding.getRoot();
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

        adapter = new CalendarAdapter(days, getContext(), new CalendarAdapter.OnItemListener() {
            @Override
            public void onItemClick(int position, LocalDate date) {
                CalendarUtils.selectedDate = date;
                if(!date.equals("")) {
                    String message = "Selected Date " + date;
                    //Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
                setWeekView();
            }
        });

        binding.calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.calendarRecyclerView.setAdapter(adapter);
        setEventAdpater();
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

                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    RecordAdapter recordAdapter = new RecordAdapter(list, getContext());
                    binding.recyclerView.setAdapter(recordAdapter);
                }
                else{
                    binding.tvNoRecord.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}