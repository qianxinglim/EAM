package com.example.eam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.example.eam.databinding.ActivityEditProfileBinding;
import com.example.eam.databinding.ActivityEmployeeProfileBinding;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

public class EmployeeProfileActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeProfileActivity";
    private ActivityEmployeeProfileBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;
    private String userId;
    private BottomSheetDialog bottomSheetDialog;
    private int IMAGE_GALLERY_REQUEST = 111;
    private Uri imageUri;

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

        binding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetPickPhoto();
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

    private void showBottomSheetPickPhoto() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick,null);

        ((View) view.findViewById(R.id.ln_gallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
                bottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.ln_camera)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCameraPermission();
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Objects.requireNonNull(bottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                bottomSheetDialog=null;
            }
        });
        bottomSheetDialog.show();

    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    221);

        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    222);
        }
        else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMDD_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";

        try {
            File file = File.createTempFile("IMG_" + timeStamp, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,  imageUri);
            intent.putExtra("listPhotoName", imageFileName);
            startActivityForResult(intent, 440);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //Intent chooserIntent = Intent.createChooser(intent,"select image");

        startActivityForResult(Intent.createChooser(intent,"select image"),IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK && data !=null && data.getData() !=null){
            imageUri = data.getData();

            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Glide.with(EmployeeProfileActivity.this).load(bitmap).into(binding.imageProfile);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //From Camera
        if(requestCode == 440 && resultCode == RESULT_OK){
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Glide.with(EmployeeProfileActivity.this).load(bitmap).into(binding.imageProfile);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void updateUserInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(EmployeeProfileActivity.this);
        progressDialog.setMessage("Sending leave request...");
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

        if(imageUri == null){
            uploadInfo(updateInfo);
            progressDialog.dismiss();
            finish();
        }
        else{
            if(imageUri != null){
                new FirebaseService(EmployeeProfileActivity.this).uploadImageToFirebaseStorage(imageUri, new FirebaseService.OnCallBack() {
                    @Override
                    public void onUploadSuccess(String imageUrl) {
                        updateInfo.put("profilePic", imageUrl);
                        uploadInfo(updateInfo);
                        progressDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onUploadFailed(Exception e) {
                        Log.e("TAG", "onUploadFailed: " + e);
                    }
                });
            }
        }
    }

    private void uploadInfo(Map<String, Object> updateInfo) {
        firestore.collection("Companies").document(companyID).collection("Users").document(userId).update(updateInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(EmployeeProfileActivity.this, "user info updated successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "fail to update user info");
            }
        });
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
                if(!s.toString().isEmpty() && !s.toString().matches("[a-zA-Z]+")){
                    //When value is not equal to empty and contain numeric value
                    binding.tvName.setError("Allow only character");
                }
                else{
                    binding.tvName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_edit, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_promote :

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

                break;
        }
        return super.onOptionsItemSelected(item);
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