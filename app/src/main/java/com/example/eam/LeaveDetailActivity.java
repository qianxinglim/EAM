package com.example.eam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.example.eam.databinding.ActivityLeaveDetailBinding;
import com.example.eam.model.Leave;
import com.google.firebase.database.DatabaseReference;

public class LeaveDetailActivity extends AppCompatActivity {
    private ActivityLeaveDetailBinding binding;
    private static final String TAG = "LeaveDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_detail);

        Intent i = getIntent();
        Leave leave = getIntent().getExtras().getParcelable("leaveObj");

        if(leave != null){
            Log.e(TAG, "intent: " + i);
        }
        else{
            finish();
            Toast.makeText(this, "Error viewing leave detail.", Toast.LENGTH_SHORT).show();
        }
    }
}