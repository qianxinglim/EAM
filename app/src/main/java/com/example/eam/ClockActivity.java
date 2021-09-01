package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eam.databinding.ActivityClockBinding;
import com.example.eam.managers.ChatService;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Chats;
import com.example.eam.service.FirebaseService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.meta.When;

public class ClockActivity extends AppCompatActivity {
    public static final String TAG = "ClockActivity";
    private ActivityClockBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID, attendanceID;
    FusedLocationProviderClient fusedLocationProviderClient;
    //String userID, userName;
    private DatabaseReference reference;
    private boolean clockOut = false;
    private boolean clockOutToday = false, clockOutYtd = false;
    private boolean todayHasRecord = false;
    private boolean clockInYtd = false;
    private String lastClockinDate;
    private String currentDate, currentTime, currTime, currDate, newTime, newDate;
    private long tsLong, newtsLong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_clock);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        if(firebaseUser == null){
            startActivity(new Intent(getApplicationContext(),Login.class));
            finish();
        }

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        checkLastNode();

        binding.btnClock.setOnClickListener(view -> {
            final ProgressDialog progressDialog = new ProgressDialog(ClockActivity.this);
            progressDialog.setMessage("Adding Record...");
            progressDialog.show();

            checkLastNode();
            //getCurrentDateTime();
            getPermission();
            //recordAttendance();

            progressDialog.dismiss();
        });

        binding.btnClockin.setOnClickListener(view -> {
            //getPermission();

            clockin();
        });

        binding.btnClockout.setOnClickListener(view -> {
            //getPermission();

            clockout();
        });
    }

    private void recordAttendance(double latitude, double longitude, String address) {
        getCurrentDateTime();

        if(clockOut){
            //getCurrentDateTime();

            if(currDate.equals(lastClockinDate) && lastClockinDate != null){
                Toast.makeText(this, "You already clocked in today", Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d(TAG, "date: " + currDate + ", time: " + currTime);
                Log.d(TAG, "newdate: " + newDate + ", newtime: " + newTime);

                Map<String, Object> attendance = new HashMap<>();
                attendance.put("clockInTimestamp", tsLong);
                attendance.put("clockInDate", currDate);
                attendance.put("clockInTime", currTime);
                attendance.put("duration", 9);
                attendance.put("oriClockOutTimestamp", newtsLong);
                attendance.put("userId", firebaseUser.getUid());
                attendance.put("clockInLat", latitude);
                attendance.put("clockInLong", longitude);
                //attendance.put("clockInAddress", address);

                reference.child(companyID).child("Attendance").push().setValue(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void aVoid) {
                        clockOut = false;
                        binding.btnClock.setText("Clock out");
                        Log.d("addAttendance", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addAttendance", "onFailure: " + e.getMessage());
                    }
                });
            }
        }
        else{
            //getCurrentDateTime();

            Map<String,Object> attendance = new HashMap<>();
            attendance.put("clockOutTimestamp", tsLong);
            attendance.put("clockOutDate", currDate);
            attendance.put("clockOutTime", currTime);
            attendance.put("clockOutLat", latitude);
            attendance.put("clockOutLong", longitude);
            //attendance.put("clockOutAddress", address);

            reference.child(companyID).child("Attendance").child(attendanceID).updateChildren(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void aVoid) {
                    Log.d("addAttendance", "onSuccess: ");
                    clockOut = true;
                    binding.btnClock.setText("Clock in");
                    Toast.makeText(ClockActivity.this, "Successfully clockout.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("addAttendance", "onFailure: " + e.getMessage());
                    Toast.makeText(ClockActivity.this, "Fail to clockOut. Try again later.", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void checkLastNode(){
        //getCurrentDateTime();

        reference.child(companyID).child("Attendance").orderByChild("userId").equalTo(firebaseUser.getUid()).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot children: snapshot.getChildren()) {
                        attendanceID = children.getKey();
                        lastClockinDate = children.child("clockInDate").getValue().toString();

                        if(children.hasChild("clockOutTime") && children.hasChild("clockOutDate") && children.hasChild("clockOutTimestamp")){
                            clockOut = true;
                            binding.btnClock.setText("Clock in");
                            //binding.btnClockin.setVisibility(View.VISIBLE);
                            //binding.btnClockout.setVisibility(View.GONE);
                        }
                        else{
                            clockOut = false;
                            binding.btnClock.setText("Clock out");
                            //binding.btnClockout.setVisibility(View.VISIBLE);
                            //binding.btnClockin.setVisibility(View.GONE);
                        }

                        //Log.d("key", attendanceID);

                        Log.d(TAG, "lastClockinDate" + lastClockinDate);
                        Log.d("val", children.child("clockInTime").getValue().toString());
                    }
                }
                else{
                    clockOut = true;
                    binding.btnClock.setText("Clock in");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void dk(){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot children: snapshot.getChildren()) {
                        Log.d(TAG,  children.getKey());
                    }
                }
                else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void clockout() {

        //checkLastNode();

        //getCurrentDateTime();

        //if(!clockOut){
            final ProgressDialog progressDialog = new ProgressDialog(ClockActivity.this);
            progressDialog.setMessage("Adding Record...");
            progressDialog.show();

            reference.child(companyID).child("Attendance").orderByChild("userId").equalTo(firebaseUser.getUid()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot children: snapshot.getChildren()) {
                        attendanceID = children.getKey();

                        Log.d("val2", children.child("clockInTime").getValue().toString());
                    }

                    getCurrentDateTime();

                    Map<String,Object> attendance = new HashMap<>();
                    attendance.put("clockOutTimestamp", tsLong);
                    attendance.put("clockOutDate", currDate);
                    attendance.put("clockOutTime", currTime);

                    /*Map<String,Object> attendance = new HashMap<String,Object>();
                    attendance.put("clockOutDate", currentDate);
                    attendance.put("clockOutTime", currentTime);*/

                    reference.child(companyID).child("Attendance").child(attendanceID).updateChildren(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            Log.d("addAttendance", "onSuccess: ");
                            progressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("addAttendance", "onFailure: " + e.getMessage());
                            progressDialog.dismiss();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        //}

        /*final ProgressDialog progressDialog = new ProgressDialog(ClockActivity.this);
        progressDialog.setMessage("Adding Record...");
        progressDialog.show();

        getCurrentDateTime();

//        Attendance attendance = new Attendance();
//        attendance.setClockOutDate(localDate);
//        attendance.setClockOutTime(localTime);

        reference.child(companyID).child("Attendance").child(currentDate).orderByChild("user").equalTo(firebaseUser.getUid()).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot children: snapshot.getChildren()) {
                    attendanceID = children.getKey();

                    *//*if(snapshot.hasChild("clockOutTime") && snapshot.hasChild("clockOutDate")){
                        clockOut = true;
                    }*//*

                    //Log.d("key", attendanceID);
                    Log.d("val", children.child("clockInTime").getValue().toString());
                }

                Map<String,Object> attendance = new HashMap<String,Object>();
                attendance.put("clockOutDate", currentDate);
                attendance.put("clockOutTime", currentTime);

                reference.child(companyID).child("Attendance").child(currentDate).child(attendanceID).updateChildren(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void aVoid) {
                        Log.d("addAttendance", "onSuccess: ");
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("addAttendance", "onFailure: " + e.getMessage());
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/







        //Add to Chat
        /*reference.child(companyID).child("Attendance").child(localDate).child(attendanceID).updateChildren(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Log.d("addAttendance", "onSuccess: ");
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("addAttendance", "onFailure: " + e.getMessage());
                progressDialog.dismiss();
            }
        });*/
    }

    private void clockin() {

        //checkLastNode();
        //getCurrentDateTime();

        //if(clockOut){
            final ProgressDialog progressDialog = new ProgressDialog(ClockActivity.this);
            progressDialog.setMessage("Adding Record...");
            progressDialog.show();

            getCurrentDateTime();

            Log.d(TAG, "date: " + currDate + ", time: " + currTime);
            Log.d(TAG, "newdate: " + newDate + ", newtime: " + newTime);


            Map<String, Object> attendance = new HashMap<>();
            attendance.put("clockInTimestamp", tsLong);
            attendance.put("clockInDate", currDate);
            attendance.put("clockInTime", currTime);
            attendance.put("duration", 9);
            attendance.put("oriClockOutTimestamp", newtsLong);
            attendance.put("userId", firebaseUser.getUid());

            /*Attendance attendance = new Attendance();
            attendance.setUser(firebaseUser.getUid());
            attendance.setClockInDate(ts);*/

            //attendance.setClockInDate(currentDate);
            //attendance.setClockInTime(currentTime);

            //String attendanceId = reference.child("Attendance").child(attendance.getClockInDate()).push().getKey();



            reference.child(companyID).child("Attendance").push().setValue(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void aVoid) {
                    Log.d("addAttendance", "onSuccess: ");
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("addAttendance", "onFailure: " + e.getMessage());
                    progressDialog.dismiss();
                }
            });



            //}

        /*final ProgressDialog progressDialog = new ProgressDialog(ClockActivity.this);
        progressDialog.setMessage("Adding Record...");
        progressDialog.show();

        getCurrentDateTime();

//        Attendance attendance = new Attendance(
//                firebaseUser.getUid(),
//                //chatService.getCurrentDate(),
//                localDate,
//                localTime,
//                "",
//                "",
//                ""
//        );

        //checkLastNode();

        Attendance attendance = new Attendance();
        attendance.setUser(firebaseUser.getUid());
        attendance.setClockInDate(currentDate);
        attendance.setClockInTime(currentTime);

        //String attendanceId = reference.child("Attendance").child(attendance.getClockInDate()).push().getKey();

        reference.child(companyID).child("Attendance").child(currentDate).push().setValue(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Log.d("addAttendance", "onSuccess: ");
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("addAttendance", "onFailure: " + e.getMessage());
                progressDialog.dismiss();
            }
        });*/





        /*Attendance attendance = new Attendance();
        attendance.setUser(firebaseUser.getUid());
        attendance.setClockInDate(localDate);
        attendance.setClockInTime(localTime);

        String attendanceId = reference.child("Attendance").child(attendance.getClockInDate()).push().getKey();*/

        /*reference.child(companyID).child("Attendance").child(attendance.getClockInDate()).child(attendanceId).setValue(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Log.d("addAttendance", "onSuccess: " + attendanceId);
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("addAttendance", "onFailure: " + e.getMessage());
                progressDialog.dismiss();
            }
        });*/

        //Add to Chat
        /*reference.child(companyID).child("Attendance").child(attendance.getClockInDate()).child(attendanceID).setValue(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Log.d("addAttendance", "onSuccess: ");
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("addAttendance", "onFailure: " + e.getMessage());
                progressDialog.dismiss();
            }
        });*/

        /*reference.child(companyID).child("Attendance").child(attendance.getClockInDate()).push().setValue(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Log.d("addAttendance", "onSuccess: ");
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("addAttendance", "onFailure: " + e.getMessage());
                progressDialog.dismiss();
            }
        });*/
    }

    public void getCurrentDateTime(){
        /*Date date = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        currentDate = formatter.format(date);

        Calendar currentDateTime = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
        currentTime = df.format(currentDateTime.getTime());*/

        Calendar c = Calendar.getInstance();
        tsLong = c.getTimeInMillis();
        Date dateTime = new Date(tsLong);

        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        currTime = df.format(dateTime);
        currDate = formatter.format(dateTime);

        newtsLong = tsLong + TimeUnit.HOURS.toMillis(9);
        Date newdateTime = new Date(newtsLong);

        newTime = df.format(newdateTime);
        newDate = formatter.format(newdateTime);
    }

    private void getPermission(){
        if (ActivityCompat.checkSelfPermission(ClockActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //When permission granted
            getLocation();
        }
        else {
            //When permission denied
            ActivityCompat.requestPermissions(ClockActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1]) == PackageManager.PERMISSION_GRANTED){
            getLocation();
        }
        else{
            Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
        }
    }*/

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();

            if (location != null) {

                try {
                    Geocoder geocoder = new Geocoder(ClockActivity.this, Locale.getDefault());

                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    double latitude = addresses.get(0).getLatitude();
                    double longitude = addresses.get(0).getLongitude();
                    String address = addresses.get(0).getAddressLine(0);

                    Log.d(TAG, "Lat: " + latitude + ", Long: " + longitude + ", Address: " + address);

                    recordAttendance(latitude, longitude, address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*private String[] getCurrentDateTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        Date currentLocalTime = cal.getTime();
        DateFormat time = new SimpleDateFormat("HH:mm:ss");
        DateFormat date = new SimpleDateFormat("dd-MM-yyyy");
        time.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        date.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

        String localTime = time.format(currentLocalTime);
        String localDate = date.format(currentLocalTime);

        String[] dateTime = new String[2];
        dateTime[0] = localDate;
        dateTime[1] = localTime;

        return dateTime;
    }*/
}