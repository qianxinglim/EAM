package com.example.eam.menu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eam.R;
import com.example.eam.databinding.FragmentPunchBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.service.FirebaseService;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class PunchFragment extends Fragment {

    private static final String TAG = "PunchFragment";

    public PunchFragment() {
        // Required empty public constructor
    }

    private FragmentPunchBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID, attendanceID;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    private static double companyLat, companyLong;
    private static double latitude, longitude;
    LocationCallback locationCallback;
    private DatabaseReference reference;
    private boolean clockOut = false;
    private String lastClockinDate;
    private String currTime, currDate, newTime, newDate, oriClockInTime, oriClockOutTime;
    private long tsLong, newtsLong;
    private String address;
    private int duration;
    private double range;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_punch, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checkLastNode();

        getCompanyLocation();

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null){
                    return;
                }
                for(Location location: locationResult.getLocations()){
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    calcDistance();
                    Log.d(TAG, "myLat: " + latitude + ", myLong: " + longitude + ", compLat: " + companyLat + ", compLong: " + companyLong);

                    //Log.d(TAG, "onLocationResult: " + location.toString() + ", latitude: " + latitude + ", longitude: " + longitude);
                }
            }
        };

        initBtnClick();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        getPermission();
    }

    @Override
    public void onStop() {
        super.onStop();

        stopLocationUpdates();
    }

    private void getPermission(){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSettingsAndStartLocationUpdates();
        }
        else {
            mPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            //ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result) {
                    checkSettingsAndStartLocationUpdates();
                    Log.e(TAG, "onActivityResult: PERMISSION GRANTED");
                } else {
                    Toast.makeText(getContext(), "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                }
            }
    });

    private void checkSettingsAndStartLocationUpdates(){
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(@NonNull LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try {
                        apiException.startResolutionForResult(getActivity(), 1001);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void initBtnClick() {
        binding.btnClock.setOnClickListener(view -> {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                final ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Adding Record...");
                progressDialog.show();

                checkLastNode();
                recordAttendance();

                progressDialog.dismiss();
            }
            else {
                mPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    private void getCompanyLocation(){
        firestore.collection("Companies").document(companyID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d(TAG, "Listen failed: " + error);
                    return;
                }

                if (value != null && value.exists()) {
                    range = (double) value.get("punchRange");

                    Map<String, Object> location = value.getData();

                    for(Map.Entry<String, Object> entry : location.entrySet()){
                        if(entry.getKey().equals("companyLocation")){
                            Map<String, Object> latlong = (Map<String, Object>) entry.getValue();
                            for (Map.Entry<String, Object> dataEntry : latlong.entrySet()) {
                                if (dataEntry.getKey().equals("latitude")) {
                                    companyLat = (double) dataEntry.getValue();
                                    Log.d(TAG, "companyLat: " + companyLat);
                                }
                                else if(dataEntry.getKey().equals("longitude")){
                                    companyLong = (double) dataEntry.getValue();
                                    Log.d(TAG, "companyLong: " + companyLong);
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "company does not have location");
                }
            }
        });

        /*firestore.collection("Companies").document(companyID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()){
                        Map<String, Object> location = document.getData();

                        for(Map.Entry<String, Object> entry : location.entrySet()){
                            if(entry.getKey().equals("companyLocation")){
                                Map<String, Object> latlong = (Map<String, Object>) entry.getValue();
                                for (Map.Entry<String, Object> dataEntry : latlong.entrySet()) {
                                    if (dataEntry.getKey().equals("latitude")) {
                                        companyLat = (double) dataEntry.getValue();
                                        Log.d(TAG, "companyLat: " + companyLat);
                                    }
                                    else if(dataEntry.getKey().equals("longitude")){
                                        companyLong = (double) dataEntry.getValue();
                                        Log.d(TAG, "companyLong: " + companyLong);
                                    }
                                }
                            }
                        }
                    }
                    else{
                        Log.d(TAG, "company does not have location");
                    }
                }
                else{
                    Log.d(TAG, "get Failed with: " + task.getException());
                }
            }
        });*/
    }

    private void recordAttendance() {
        getDuration(new OnCallBack() {
            @Override
            public void onSuccess() {
                getCurrentDateTime();

                boolean inRange = calcDistance();

                /*SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                DateTimeFormatter dtf = DateTimeFormat.forPattern("HH:mm");

                //Date oriClockIn = null, currTimeNow = null;
                DateTime oriClockIn = null;

                //oriClockIn = df.parse(oriClockInTime);
                //currTimeNow = df.parse(currTime);

                oriClockIn = dtf.parseDateTime(oriClockInTime);
                //currTimeNow = dtf.parseDateTime(currTime);

                DateTime onehrb4 = oriClockIn.minusHours(1);

                //String ori = dtf.print(onehrb4);

                Date b4 = null, currTimeNow = null;

                try {
                    b4 = df.parse(dtf.print(onehrb4));
                    currTimeNow = df.parse(currTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "onehrb4: " + onehrb4);

                if (currTimeNow.after(b4)) {
                    Toast.makeText(getContext(), "can clock in", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "can clock in" + b4 + ", " + currTimeNow + ", " + dtf.print(onehrb4));
                }
                else{
                    Toast.makeText(getContext(), "Cannot clock in", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "cannot clock in" + b4 + ", " + currTimeNow + ", " + dtf.print(onehrb4));
                }*/

                if(clockOut){
                    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                    DateTimeFormatter dtf = DateTimeFormat.forPattern("HH:mm");

                    DateTime oriClockIn = null;

                    oriClockIn = dtf.parseDateTime(oriClockInTime);

                    DateTime onehrb4 = oriClockIn.minusHours(1);

                    Date b4 = null, currTimeNow = null;
                    try {
                        b4 = df.parse(dtf.print(onehrb4));
                        currTimeNow = df.parse(currTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //one hour before the oriClockInTime set by admin
                    if (currTimeNow.after(b4)) {
                        Toast.makeText(getContext(), "can clock in", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "can clock in" + b4 + ", " + currTimeNow + ", " + dtf.print(onehrb4));

                        if(currDate.equals(lastClockinDate) && lastClockinDate != null){
                            Toast.makeText(getContext(), "You already clocked in today", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.d(TAG, "date: " + currDate + ", time: " + currTime);
                            Log.d(TAG, "newdate: " + newDate + ", newtime: " + newTime);

                            Map<String, Object> attendance = new HashMap<>();
                            attendance.put("clockInTimestamp", tsLong);
                            attendance.put("clockInDate", currDate);
                            attendance.put("clockInTime", currTime);
                            attendance.put("duration", duration);
                            //attendance.put("oriClockOutTimestamp", newtsLong);
                            attendance.put("userId", firebaseUser.getUid());
                            attendance.put("clockInLat", latitude);
                            attendance.put("clockInLong", longitude);
                            attendance.put("clockIninRange", inRange);
                            attendance.put("oriClockInTime", oriClockInTime);
                            attendance.put("oriClockOutTime", oriClockOutTime);
                            attendance.put("mustClockOutTime", newTime);
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
                        Toast.makeText(getContext(), "Cannot clock in", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "cannot clock in" + b4 + ", " + currTimeNow + ", " + dtf.print(onehrb4));
                    }
                }
                else{

                    Map<String,Object> attendance = new HashMap<>();
                    attendance.put("clockOutTimestamp", tsLong);
                    attendance.put("clockOutDate", currDate);
                    attendance.put("clockOutTime", currTime);
                    attendance.put("clockOutLat", latitude);
                    attendance.put("clockOutLong", longitude);
                    attendance.put("clockOutInRange", inRange);
                    //attendance.put("clockOutAddress", address);

                    reference.child(companyID).child("Attendance").child(attendanceID).updateChildren(attendance).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            Log.d("addAttendance", "onSuccess: ");
                            clockOut = true;
                            binding.btnClock.setText("Clock in");
                            Toast.makeText(getContext(), "Successfully clockout.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("addAttendance", "onFailure: " + e.getMessage());
                            Toast.makeText(getContext(), "Fail to clockOut. Try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            @Override
            public void onFailed(Exception e) {

            }
        });
    }

    private void checkLastNode(){
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
                        }
                        else{
                            clockOut = false;
                            binding.btnClock.setText("Clock out");
                        }

                        //Log.d("key", attendanceID);

                        Log.d(TAG, "lastClockinDate" + lastClockinDate);
                        Log.d("val", children.child("clockInTime").getValue().toString());
                    }
                }
                else{
                    lastClockinDate = null;
                    clockOut = true;
                    binding.btnClock.setText("Clock in");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean calcDistance() {
        float results[] = new float[10];
        Location.distanceBetween(latitude, longitude, companyLat, companyLong, results);
        double km = results[0]/1000;
        Log.d(TAG, "Distance: " + km);

        if(km > range){
            binding.tvLocation.setText("Not Within Company");
            return false;
        }
        else{
            binding.tvLocation.setText("Within Company");
            return true;
        }
    }

    /*private void dk(){
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
    }*/

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

        //newtsLong = tsLong + TimeUnit.HOURS.toMillis(9);

        newtsLong = tsLong + TimeUnit.MINUTES.toMillis(duration);
        Date newdateTime = new Date(newtsLong);

        newTime = df.format(newdateTime);
        newDate = formatter.format(newdateTime);

        Log.d(TAG, "newTime: " + newTime);
    }

    private void getDuration(final OnCallBack onCallBack){
        firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                duration = documentSnapshot.getLong("minutesOfWork").intValue();
                oriClockInTime = documentSnapshot.getString("clockInTime");
                oriClockOutTime = documentSnapshot.getString("clockOutTime");
                Log.d(TAG, "duration: " + duration);
                onCallBack.onSuccess();
            }
        });
    }

    /*private void getPermission(final OnCallBack onCallBack){
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //When permission granted
            //getLocation();

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();

                if (location != null) {

                    try {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();
                        address = addresses.get(0).getAddressLine(0);

                        Log.d(TAG, "Lat: " + latitude + ", Long: " + longitude + ", Address: " + address);

                        onCallBack.onSuccess();

                    } catch (IOException e) {
                        e.printStackTrace();
                        onCallBack.onFailed(e);
                    }
                }
                else{
                    Toast.makeText(getContext(), "Fail to get location. Please try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            //When permission denied
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }*/

    /*@SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            Location location = task.getResult();

            if (location != null) {

                try {
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    latitude = addresses.get(0).getLatitude();
                    longitude = addresses.get(0).getLongitude();
                    address = addresses.get(0).getAddressLine(0);

                    Log.d(TAG, "Lat: " + latitude + ", Long: " + longitude + ", Address: " + address);

                    //recordAttendance(latitude, longitude, address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(getContext(), "Fail to get location. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

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

    public interface OnCallBack{
        void onSuccess();
        void onFailed(Exception e);
    }
}