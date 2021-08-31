package com.example.eam.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eam.R;
import com.example.eam.common.CalendarUtils;

import java.time.LocalDate;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder>{
    private final ArrayList<LocalDate> days;
    private Context context;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<LocalDate> days, Context context, OnItemListener onItemListener) {
        this.days = days;
        this.onItemListener = onItemListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        if(days.size() > 15) //month view
            layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        else // week view
            layoutParams.height = (int) parent.getHeight();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final LocalDate date = days.get(position);

        if(date == null)
            holder.dayOfMonth.setText("");
        else {
            holder.dayOfMonth.setText(String.valueOf(date.getDayOfMonth()));

            if(date.equals(CalendarUtils.selectedDate))
                holder.parentView.setBackgroundColor(Color.LTGRAY);
        }

        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!date.equals("")) {
                    //if(date.equals(CalendarUtils.selectedDate))
                        //holder.parentView.setBackgroundColor(Color.LTGRAY);

                    //Toast.makeText(context, "Selected Date: " +  days.get(position), Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    @Override
    public int getItemCount(){
        return days.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, LocalDate date);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //private final ArrayList<LocalDate> days;
        private TextView dayOfMonth;
        public final View parentView;
        //private final OnItemListener onItemListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            parentView = itemView.findViewById(R.id.parentView);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
            //this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
            //this.days = days;
        }

        @Override
        public void onClick(View view)
        {
            onItemListener.onItemClick(getAdapterPosition(), days.get(getAdapterPosition()));
        }
    }
}
