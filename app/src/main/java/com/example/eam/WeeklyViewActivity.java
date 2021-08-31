package com.example.eam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.eam.adapter.CalendarAdapter;
import com.example.eam.adapter.EventAdapter;
import com.example.eam.common.CalendarUtils;
import com.example.eam.databinding.ActivityAttendanceRecordBinding;
import com.example.eam.databinding.ActivityWeeklyViewBinding;
import com.example.eam.model.Event;

import java.time.LocalDate;
import java.util.ArrayList;

import static com.example.eam.common.CalendarUtils.daysInMonthArray;
import static com.example.eam.common.CalendarUtils.daysInWeekArray;
import static com.example.eam.common.CalendarUtils.monthYearFromDate;

public class WeeklyViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{
    private static final String TAG = "WeeklyViewActivity";
    private ActivityWeeklyViewBinding binding;
    private CalendarAdapter adapter;
    //private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_weekly_view);

        CalendarUtils.selectedDate = LocalDate.now();
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
        binding.btnNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(this, EventEditActivity.class));
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
        //setEventAdpater();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        CalendarUtils.selectedDate = date;
        if(!date.equals("")) {
            String message = "Selected Date " + date;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
        ArrayList<Event> dailyEvents = Event.eventsForDate(CalendarUtils.selectedDate);
        EventAdapter eventAdapter = new EventAdapter(getApplicationContext(), dailyEvents);
        binding.eventListView.setAdapter(eventAdapter);
    }
}