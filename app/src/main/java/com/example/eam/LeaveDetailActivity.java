package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.adapter.Attachment2Adapter;
import com.example.eam.databinding.ActivityLeaveDetailBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Leave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LeaveDetailActivity extends AppCompatActivity {
    private ActivityLeaveDetailBinding binding;
    private static final String TAG = "LeaveDetailActivity";
    private Leave leave;
    private Attachment2Adapter attachmentAdapter;
    private SessionManager sessionManager;
    private String companyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_leave_detail);

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        Intent i = getIntent();
        leave = (Leave) getIntent().getSerializableExtra("leaveObj");
        String activity = getIntent().getExtras().getString("Activity");

        if(leave != null){
            if(activity.equals("LeaveRecord")){
                displayLeaveDetailsUser();
            }
            else if(activity.equals("LeaveReview")){
                String profilePic = getIntent().getExtras().getString("profilePic");
                String userName = getIntent().getExtras().getString("userName");

                displayLeaveDetailsAdmin(userName, profilePic);
            }
        }
        else{
            finish();
            Toast.makeText(this, "Error viewing leave detail.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayLeaveDetailsUser() {
        //tvRequestDateTime.setText("Requested absence on " + leave.getRequestDate());

        binding.lnBtn.setVisibility(View.GONE);
        binding.lnBtnCancel.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.lnBtnCancel);
        params.addRule(RelativeLayout.BELOW, R.id.toolbar);
        binding.nestedScrollView.setLayoutParams(params);

        binding.lnUserDetail.setVisibility(View.GONE);
        binding.toolbar.setVisibility(View.VISIBLE);

        if(leave.getStatus().equals("Approved")){
            binding.tvStatus.setText("Approved");
            binding.ivStatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quantum_googgreen));
        }
        else if(leave.getStatus().equals("Declined")){
            binding.tvStatus.setText("Declined");
            binding.ivStatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quantum_googred));
        }
        else{
            binding.tvStatus.setText("Pending");
            binding.ivStatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quantum_yellow));
        }


        //Middle part
        //tvStatus.setText(leave.getStatus());
        binding.tvLeaveType.setText(leave.getType());
        binding.etNote.setText(leave.getNote());
        binding.etNote.setFocusable(false);
        //tvNote.setEnabled(false);
        binding.etNote.setCursorVisible(false);
        binding.etNote.setKeyListener(null);
        binding.etNote.setBackgroundColor(Color.TRANSPARENT);
        binding.tvTotal.setText(leave.getDuration());

        binding.switchAllDay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                binding.switchAllDay.setClickable(false);
                return false;
            }
        });

        if(leave.getAttachments() != null){
            binding.lnAttachments.setVisibility(View.VISIBLE);
            binding.attachmentRecyclerView.setLayoutManager(new GridLayoutManager(this, leave.getAttachments().size(), GridLayoutManager.VERTICAL, false));
            attachmentAdapter = new Attachment2Adapter(leave.getAttachments(), this);
            binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
        }
        else{
            binding.lnAttachments.setVisibility(View.GONE);
        }

        if(leave.getNote().equals("") || leave.getNote() == null){
            binding.lnNote.setVisibility(View.GONE);
        }
        else{
            binding.lnNote.setVisibility(View.VISIBLE);
        }

        if(leave.isFullDay()){
            binding.lnStartDate.setVisibility(View.VISIBLE);
            binding.lnEndDate.setVisibility(View.VISIBLE);
            binding.tvDateFrom.setText(leave.getDateFrom());
            binding.tvDateTo.setText(leave.getDateTo());
            binding.lnDate.setVisibility(View.GONE);
            binding.lnStartTime.setVisibility(View.GONE);
            binding.lnEndTime.setVisibility(View.GONE);
            binding.switchAllDay.setChecked(true);
        }
        else{
            binding.lnDate.setVisibility(View.VISIBLE);
            binding.lnStartTime.setVisibility(View.VISIBLE);
            binding.lnEndTime.setVisibility(View.VISIBLE);
            binding.tvDate.setText(leave.getDate());
            binding.tvTimeFrom.setText(leave.getTimeFrom());
            binding.tvTimeTo.setText(leave.getTimeTo());
            binding.lnStartDate.setVisibility(View.GONE);
            binding.lnEndDate.setVisibility(View.GONE);
            binding.switchAllDay.setChecked(false);
        }

        if (leave.getStatus().equals("Pending")) {
            binding.lnBtnCancel.setVisibility(View.VISIBLE);

            binding.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance().getReference().child(companyID).child("Leaves").child(leave.getLeaveId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                            Toast.makeText(LeaveDetailActivity.this, "Successfully Cancelled Request.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LeaveDetailActivity.this, "Fail to cancel request.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } else {
            binding.lnBtnCancel.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params2.addRule(RelativeLayout.BELOW, R.id.toolbar);
            binding.nestedScrollView.setLayoutParams(params2);
        }
    }

    private void displayLeaveDetailsAdmin(String userName, String userProfilePic) {
        //Set User Info (Top part)

        binding.lnBtn.setVisibility(View.VISIBLE);
        binding.lnBtnCancel.setVisibility(View.GONE);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.lnBtn);
        params.addRule(RelativeLayout.BELOW, R.id.lnUserDetail);
        binding.nestedScrollView.setLayoutParams(params);

        binding.lnUserDetail.setVisibility(View.VISIBLE);
        binding.toolbar.setVisibility(View.GONE);

        binding.tvName.setText(userName);

        if(userProfilePic!=null && !userProfilePic.equals("")) {
            Glide.with(this).load(userProfilePic).into(binding.tvProfilePic);
        }
        else{
            Glide.with(this).load(R.drawable.icon_male_ph).into(binding.tvProfilePic);
        }

        binding.tvRequestDateTime.setText("Requested absence on " + leave.getRequestDate());

        if(leave.getStatus().equals("Approved")){
            binding.tvStatus.setText("Approved");
            binding.ivStatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quantum_googgreen));
        }
        else if(leave.getStatus().equals("Declined")){
            binding.tvStatus.setText("Declined");
            binding.ivStatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quantum_googred));
        }
        else{
            binding.tvStatus.setText("Pending");
            binding.ivStatus.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quantum_yellow));
        }


        //Middle part
        //tvStatus.setText(leave.getStatus());
        binding.tvLeaveType.setText(leave.getType());
        binding.etNote.setText(leave.getNote());
        binding.etNote.setFocusable(false);
        //tvNote.setEnabled(false);
        binding.etNote.setCursorVisible(false);
        binding.etNote.setKeyListener(null);
        binding.etNote.setBackgroundColor(Color.TRANSPARENT);
        binding.tvTotal.setText(leave.getDuration());

        binding.switchAllDay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                binding.switchAllDay.setClickable(false);
                return false;
            }
        });

        if(leave.getAttachments() != null){
            binding.lnAttachments.setVisibility(View.VISIBLE);
            binding.attachmentRecyclerView.setLayoutManager(new GridLayoutManager(this, leave.getAttachments().size(), GridLayoutManager.VERTICAL, false));
            attachmentAdapter = new Attachment2Adapter(leave.getAttachments(), this);
            binding.attachmentRecyclerView.setAdapter(attachmentAdapter);
        }
        else{
            binding.lnAttachments.setVisibility(View.GONE);
        }

        if(leave.getNote().equals("") || leave.getNote() == null){
            binding.lnNote.setVisibility(View.GONE);
        }
        else{
            binding.lnNote.setVisibility(View.VISIBLE);
        }

        if(leave.isFullDay()){
            binding.lnStartDate.setVisibility(View.VISIBLE);
            binding.lnEndDate.setVisibility(View.VISIBLE);
            binding.tvDateFrom.setText(leave.getDateFrom());
            binding.tvDateTo.setText(leave.getDateTo());
            binding.lnDate.setVisibility(View.GONE);
            binding.lnStartTime.setVisibility(View.GONE);
            binding.lnEndTime.setVisibility(View.GONE);
            binding.switchAllDay.setChecked(true);
        }
        else{
            binding.lnDate.setVisibility(View.VISIBLE);
            binding.lnStartTime.setVisibility(View.VISIBLE);
            binding.lnEndTime.setVisibility(View.VISIBLE);
            binding.tvDate.setText(leave.getDate());
            binding.tvTimeFrom.setText(leave.getTimeFrom());
            binding.tvTimeTo.setText(leave.getTimeTo());
            binding.lnStartDate.setVisibility(View.GONE);
            binding.lnEndDate.setVisibility(View.GONE);
            binding.switchAllDay.setChecked(false);
        }

        //if(){
            if (leave.getStatus().equals("Pending")) {
                binding.btnApprove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseDatabase.getInstance().getReference().child(companyID).child("Leaves").child(leave.getLeaveId()).child("status").setValue("Approved").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {
                                Toast.makeText(LeaveDetailActivity.this, "Successfully approved", Toast.LENGTH_SHORT).show();
                                binding.lnBtn.setVisibility(View.GONE);
                                binding.tvStatus.setText("Approved");
                                binding.ivStatus.setBackgroundTintList(ContextCompat.getColorStateList(LeaveDetailActivity.this, R.color.quantum_googgreen));

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LeaveDetailActivity.this, "Fail to approve", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                binding.btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseDatabase.getInstance().getReference().child(companyID).child("Leaves").child(leave.getLeaveId()).child("status").setValue("Declined").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(@NonNull Void aVoid) {
                                Toast.makeText(LeaveDetailActivity.this, "Successfully declined", Toast.LENGTH_SHORT).show();
                                binding.lnBtn.setVisibility(View.GONE);
                                binding.tvStatus.setText("Declined");
                                binding.ivStatus.setBackgroundTintList(ContextCompat.getColorStateList(LeaveDetailActivity.this, R.color.quantum_googred));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LeaveDetailActivity.this, "Successfully decline", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else {
                binding.lnBtn.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params2.addRule(RelativeLayout.BELOW, R.id.lnUserDetail);
                binding.nestedScrollView.setLayoutParams(params2);
            }
        //}

        /*bottomSheetView.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });*/
    }
}