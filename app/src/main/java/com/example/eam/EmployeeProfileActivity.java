package com.example.eam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityEditProfileBinding;
import com.example.eam.databinding.ActivityEmployeeProfileBinding;
import com.example.eam.display.ViewImageActivity;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.User;
import com.example.eam.service.FirebaseService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class EmployeeProfileActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeProfileActivity";
    private ActivityEmployeeProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID, creatorID;
    private String userId;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_employee_profile);

        setSupportActionBar(binding.toolbar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);
        creatorID = userDetail.get(sessionManager.CREATORID);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userID");

        binding.etName.setText(intent.getStringExtra("userName"));
        binding.etPhoneNo.setText(intent.getStringExtra("userPhoneNo"));
        binding.etEmail.setText(intent.getStringExtra("userEmail"));
        binding.etTitle.setText(intent.getStringExtra("userTitle"));
        binding.etDepartment.setText(intent.getStringExtra("userDepartment"));
        binding.etClockInTime.setText(intent.getStringExtra("userClockInTime"));
        binding.etClockOutTime.setText(intent.getStringExtra("userClockOutTime"));

        if(intent.getStringExtra("userProfilePic").equals("") || intent.getStringExtra("userProfilePic") == null){
            Glide.with(EmployeeProfileActivity.this).load(R.drawable.icon_male_ph).into(binding.imageProfile);
        }
        else{
            Glide.with(EmployeeProfileActivity.this).load(intent.getStringExtra("userProfilePic")).into(binding.imageProfile);
        }

        binding.etClockInTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(binding.etClockInTime);
            }
        });

        binding.etClockOutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTime(binding.etClockOutTime);
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserInfo();
            }
        });

        binding.imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.imageProfile.invalidate();
                Drawable dr = binding.imageProfile.getDrawable();
                Common.IMAGE_BITMAP = ((BitmapDrawable)dr.getCurrent()).getBitmap();
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(EmployeeProfileActivity.this, binding.imageProfile,"image");
                Intent intent = new Intent(EmployeeProfileActivity.this, ViewImageActivity.class);
                startActivity(intent, activityOptionsCompat.toBundle());
            }
        });

        /*String userName = intent.getStringExtra("userName");
        String userProfilePic = intent.getStringExtra("userProfilePic");
        String userDepartment = intent.getStringExtra("userDepartment");
        String userEmail = intent.getStringExtra("userEmail");
        String userPhoneNo = intent.getStringExtra("userPhoneNo");
        String userTitle = intent.getStringExtra("userTitle");*/

        validation();
    }


    private void updateUserInfo() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date timeFrom = sdf.parse(binding.etClockInTime.getText().toString());
            Date timeTo = sdf.parse(binding.etClockOutTime.getText().toString());

            if (timeFrom.after(timeTo)) {
                Toast.makeText(this, "Work end time cannot be earlier than work start time.", Toast.LENGTH_SHORT).show();
            }
            else{
                submitUpdate();
            }
        }
        catch (ParseException e){
            e.printStackTrace();
            Toast.makeText(this, "Fail to update user profile. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitUpdate() {
        if(binding.tvName.getError() != null || binding.tvEmail.getError() != null || binding.tvPhoneNo.getError() != null || binding.tvDepartment.getError() != null || binding.tvTitle.getError() != null){
            Toast.makeText(this, "Please enter the correct information.", Toast.LENGTH_SHORT).show();
        }
        else{
            final ProgressDialog progressDialog = new ProgressDialog(EmployeeProfileActivity.this);
            progressDialog.setMessage("Updating user profile...");
            progressDialog.show();

            String phoneNo = binding.etPhoneNo.getText().toString();
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

            //User user = new User("",name,phoneNo,"",email,"",title,department, clockInTime, clockOutTime, workMinutes);

            Map<String, Object> updateInfo = new HashMap<>();
            updateInfo.put("phoneNo", phoneNo);
            updateInfo.put("name", name);
            updateInfo.put("email", email);
            updateInfo.put("department", department);
            updateInfo.put("title", title);
            updateInfo.put("clockInTime", clockInTime);
            updateInfo.put("clockOutTime", clockOutTime);
            updateInfo.put("minutesOfWork", workMinutes);

            firestore.collection("Companies").document(companyID).collection("Users").document(userId).update(updateInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void aVoid) {
                    Toast.makeText(EmployeeProfileActivity.this, "User info updated successfully.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EmployeeProfileActivity.this, "Fail to update user profile. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Fail to update user info.");
                }
            });
        }
    }

    private void getTime(TextView tvTime) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(EmployeeProfileActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                String time = hourOfDay + ":" + minute;

                SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm");

                try{
                    Date date = f24Hours.parse(time);

                    tvTime.setText(f24Hours.format(date));
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }, 12,0, false);

        timePickerDialog.show();
    }

    private void validation(){
        binding.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().isEmpty() && !s.toString().matches("[a-zA-Z ]*")){
                    binding.tvName.setError("Allows only character");
                }
                else if(s.toString().isEmpty()){
                    binding.tvName.setError("Field cannot be empty");
                }
                else{
                    binding.tvName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Pattern ptr = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

                if(s.toString().isEmpty()){
                    binding.tvEmail.setError("Field cannot be empty");
                }
                else if(!ptr.matcher(s.toString()).matches()){
                    binding.tvEmail.setError("Please enter a valid email address.");
                }
                else{
                    binding.tvEmail.setError(null);
                }
            }
        });

        binding.etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    binding.tvTitle.setError("Field cannot be empty");
                }
                else{
                    binding.tvTitle.setError(null);
                }
            }
        });

        binding.etDepartment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    binding.tvDepartment.setError("Field cannot be empty");
                }
                else{
                    binding.tvDepartment.setError(null);
                }
            }
        });

        binding.etPhoneNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Pattern ptr = Pattern.compile("^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$");

                if(s.toString().isEmpty()){
                    binding.tvPhoneNo.setError("Field cannot be empty");
                }
                else if(!ptr.matcher(s.toString()).matches()){
                    binding.tvPhoneNo.setError("Please enter a valid phone number.");
                }
                else{
                    binding.tvPhoneNo.setError(null);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_promote);

        firestore.collection("Companies").document(companyID).collection("Admin").document(userId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    item.setTitle("Demote admin");
                    isAdmin = true;
                }
                else{
                    item.setTitle("Promote to admin");
                    isAdmin = false;
                }
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_promote :

                if(isAdmin){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeProfileActivity.this);
                    builder.setMessage("Do you want to demote this admin?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            demoteUser();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else{
                    final AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeProfileActivity.this);
                    builder.setMessage("Do you want to promote this user to admin?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            promoteUser();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void demoteUser() {

        if(!userId.equals(creatorID)){
            firestore.collection("Companies").document(companyID).collection("Admin").document(userId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void aVoid) {
                    Toast.makeText(EmployeeProfileActivity.this, "successfully demoted user", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EmployeeProfileActivity.this, "Fail to promote user", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(this, "Creator of the company cannot be demoted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void promoteUser() {
        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("id", userId);

        firestore.collection("Companies").document(companyID).collection("Admin").document(userId).set(adminInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(EmployeeProfileActivity.this, "successfully promoted user", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EmployeeProfileActivity.this, "Fail to promote user", Toast.LENGTH_SHORT).show();
            }
        });
    }
}