package com.example.eam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.eam.adapter.CalendarAdapter;
import com.example.eam.common.CalendarUtils;
import com.example.eam.databinding.ActivityAttendanceRecordBinding;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.eam.common.CalendarUtils.daysInMonthArray;
import static com.example.eam.common.CalendarUtils.monthYearFromDate;

public class AttendanceRecordActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private static final String TAG = "AttendanceRecordActivity";
    private ActivityAttendanceRecordBinding binding;
    private CalendarAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attendance_record);

        CalendarUtils.selectedDate = LocalDate.now();
        setMonthView();
        initBtnClick();
    }

    private void initBtnClick() {
        binding.btnChgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AttendanceRecordActivity.this, WeeklyViewActivity.class));
            }
        });
        binding.btnPreviousMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
                setMonthView();
            }
        });

        binding.btnNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
                setMonthView();
            }
        });
    }

    private void setMonthView() {
        binding.monthYearTV.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(CalendarUtils.selectedDate);

        adapter = new CalendarAdapter(daysInMonth, AttendanceRecordActivity.this,this);
        //RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        binding.calendarRecyclerView.setLayoutManager(new GridLayoutManager(AttendanceRecordActivity.this, 7));
        binding.calendarRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if(date != null) {
            CalendarUtils.selectedDate = date;
            if(!date.equals("")) {
                String message = "Selected Date " + date;
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            setMonthView();
        }
    }


    /*public String[] getCurrentDateTime(){
        Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String today = formatter.format(date);

        Calendar currentDateTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        String currentTime = df.format(currentDateTime.getTime());

        String[] dateTime = new String[2];
        dateTime[0] = today;
        dateTime[1] = currentTime;

        return dateTime;
    }*/
}