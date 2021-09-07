package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.load.model.DataUrlLoader;
import com.example.eam.databinding.ActivityEditCompanyBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.User;
import com.example.eam.service.FirebaseService;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditCompanyActivity extends AppCompatActivity {
    private static final String TAG = "EditCompanyActivity";
    private ActivityEditCompanyBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_company);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);
    }

    private void updateUserInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(EditCompanyActivity.this);
        progressDialog.setMessage("Sending leave request...");
        progressDialog.show();

        /*String phoneNo = binding.etPhoneNo.getText().toString();
        String name = binding.etName.getText().toString();
        String department = binding.etDepartment.getText().toString();
        String email = binding.etEmail.getText().toString();
        String title = binding.etTitle.getText().toString();
        String clockInTime = binding.etClockInTime.getText().toString();
        String clockOutTime = binding.etClockOutTime.getText().toString();
        int workMinutes = 0;

        SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

        try {
            Date clockIn = f24Hours.parse(clockInTime);
            Date clockOut = f24Hours.parse(clockOutTime);
            long diff = clockOut.getTime() - clockIn.getTime();
            workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        User user = new User("",name,phoneNo,"",email,"",title,department, clockInTime, clockOutTime, workMinutes);

        Map<String, Object> updateInfo = new HashMap<>();
        updateInfo.put("phoneNo", phoneNo);
        updateInfo.put("name", name);
        updateInfo.put("email", email);
        updateInfo.put("department", department);
        updateInfo.put("title", title);
        updateInfo.put("clockInTime", clockInTime);
        updateInfo.put("clockOutTime", clockOutTime);
        updateInfo.put("minutesOfWork", workMinutes);

        firestore.collection("Companies").document(companyID).update(updateInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(EditCompanyActivity.this, "company info updated successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "fail to update company info");
            }
        });*/

        progressDialog.dismiss();
        finish();
    }
}