package com.example.eam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.DataUrlLoader;
import com.example.eam.common.Common;
import com.example.eam.databinding.ActivityEditCompanyBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Company;
import com.example.eam.model.User;
import com.example.eam.service.FirebaseService;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EditCompanyActivity extends AppCompatActivity {
    private static final String TAG = "EditCompanyActivity";
    private ActivityEditCompanyBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;
    private double latitude, longitude;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_company);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        Places.initialize(getApplicationContext(), "AIzaSyDQymcPSWVISf1RAmlmRHwf0MaIrc_5sXU");

        getInfo();

        binding.etLocation.setFocusable(false);
        binding.etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(EditCompanyActivity.this);
                startActivityForResult(intent, 100);
            }
        });

        binding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCompanyInfo();
            }
        });
    }

    private void getInfo() {
        firestore.collection("Companies").document(companyID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                Company company = documentSnapshot.toObject(Company.class);

                binding.etCompanyName.setText(company.getCompanyName());
                binding.etPunchRange.setText(String.valueOf(company.getPunchRange()));

                switch (company.getStaffSize()){
                    case "1-10":
                        binding.rgStaffSize.check(R.id.rbStaff1);
                        break;
                    case "11-20":
                        binding.rgStaffSize.check(R.id.rbStaff2);
                        break;
                    case "21-50":
                        binding.rgStaffSize.check(R.id.rbStaff3);
                        break;
                    case "51-150":
                        binding.rgStaffSize.check(R.id.rbStaff4);
                        break;
                    case "151-300":
                        binding.rgStaffSize.check(R.id.rbStaff5);
                        break;
                    case "301-500":
                        binding.rgStaffSize.check(R.id.rbStaff6);
                        break;
                    case "501-1000":
                        binding.rgStaffSize.check(R.id.rbStaff7);
                        break;
                    case "1000+":
                        binding.rgStaffSize.check(R.id.rbStaff8);
                        break;

                }

                switch (company.getIndustryType()){
                    case "Cleaning":
                        binding.rgIndustry.check(R.id.rbInd1);
                        break;
                    case "Security":
                        binding.rgIndustry.check(R.id.rbInd2);
                        break;
                    case "HealthCare":
                        binding.rgIndustry.check(R.id.rbInd3);
                        break;
                    case "Manufacturing":
                        binding.rgIndustry.check(R.id.rbInd4);
                        break;
                    case "Construction":
                        binding.rgIndustry.check(R.id.rbInd5);
                        break;
                    case "Retail":
                        binding.rgIndustry.check(R.id.rbInd6);
                        break;
                    case "F & B":
                        binding.rgIndustry.check(R.id.rbInd7);
                        break;
                    case "Hospitality":
                        binding.rgIndustry.check(R.id.rbInd8);
                        break;
                    case "Transportation":
                        binding.rgIndustry.check(R.id.rbInd9);
                        break;
                    case "Events":
                        binding.rgIndustry.check(R.id.rbInd10);
                        break;
                    case "Staffing":
                        binding.rgIndustry.check(R.id.rbInd11);
                        break;
                    case "Real estate":
                        binding.rgIndustry.check(R.id.rbInd12);
                        break;
                    case "Entertainment":
                        binding.rgIndustry.check(R.id.rbInd13);
                        break;
                    case "Field services":
                        binding.rgIndustry.check(R.id.rbInd14);
                        break;
                    case "Beauty & Spa":
                        binding.rgIndustry.check(R.id.rbInd15);
                        break;
                    case "Gardening":
                        binding.rgIndustry.check(R.id.rbInd16);
                        break;
                    case "Fitness":
                        binding.rgIndustry.check(R.id.rbInd17);
                        break;
                    case "Logistics":
                        binding.rgIndustry.check(R.id.rbInd18);
                        break;
                    case "Education":
                        binding.rgIndustry.check(R.id.rbInd19);
                        break;
                    case "Religious":
                        binding.rgIndustry.check(R.id.rbInd20);
                        break;
                    case "Governmental":
                        binding.rgIndustry.check(R.id.rbInd21);
                        break;
                    case "Telecom":
                        binding.rgIndustry.check(R.id.rbInd22);
                        break;
                    case "NPO":
                        binding.rgIndustry.check(R.id.rbInd23);
                        break;
                    case "Other":
                        binding.rgIndustry.check(R.id.rbInd24);
                        break;
                }


                Map<String, Object> latlong = company.getCompanyLocation();
                for (Map.Entry<String, Object> dataEntry : latlong.entrySet()) {
                    if (dataEntry.getKey().equals("latitude")) {
                        latitude = (double) dataEntry.getValue();
                    }
                    else if(dataEntry.getKey().equals("longitude")){
                        longitude = (double) dataEntry.getValue();
                    }
                }

                Geocoder geocoder = new Geocoder(EditCompanyActivity.this, Locale.getDefault());

                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                    String address = addresses.get(0).getAddressLine(0);
                    binding.etLocation.setText(address);

                    Log.d(TAG, "Lat: " + latitude + ", Long: " + longitude + ", Address: " + address);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //binding.etLocation.setText();


//                binding.etLocation.setText(userDepartment);
//
//                binding.tvTitle.setText(userTitle);
//                binding.tvClockInTime.setText(userClockInTime);
//                binding.tvClockOutTime.setText(userClockOutTime);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){
            //Initialize place
            Place place = Autocomplete.getPlaceFromIntent(data);
            //Set address on EditText
            address = place.getAddress();
            binding.etLocation.setText(address);
            //Set locality name

            //Set latitude & longitude
            LatLng latlng = place.getLatLng();
            latitude = latlng.latitude;
            longitude = latlng.longitude;

            Log.d(TAG, "address: " + place.getAddress() + ", latitude: " + latitude + ", longitude: " + longitude);
        }
        else if(resultCode == AutocompleteActivity.RESULT_ERROR){
            //Initialize status when Error
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCompanyInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(EditCompanyActivity.this);
        progressDialog.setMessage("Updating Company Info...");
        progressDialog.show();

        int staffSizeSelectedId = binding.rgStaffSize.getCheckedCheckableImageButtonId();
        int industrySelectedId = binding.rgIndustry.getCheckedCheckableImageButtonId();

        // find the radiobutton by returned id
        RadioButton rbStaffSize = (RadioButton) findViewById(staffSizeSelectedId);
        RadioButton rbIndustryType = (RadioButton) findViewById(industrySelectedId);

        Map<String,Object> company = new HashMap<>();
        company.put("companyName", binding.etCompanyName.getText().toString());
        company.put("staffSize",rbStaffSize.getText().toString());
        company.put("industryType", rbIndustryType.getText().toString());
        company.put("punchRange", binding.etPunchRange.getText().toString());

        Map<String,Object> location = new HashMap<>();
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        company.put("companyLocation", location);

        if(binding.etCompanyName.getText().toString().equals("")){
            Toast.makeText(this, "Please fill in company title.", Toast.LENGTH_SHORT).show();
        }
        else if(latitude == 0.0 || longitude == 0.0 || binding.etLocation.getText().toString().equals("")){
            Toast.makeText(this, "Please fill in company location.", Toast.LENGTH_SHORT).show();
        }
        else if(binding.etPunchRange.getText().toString().equals("")){
            Toast.makeText(this, "Please fill in employee allowed punch range.", Toast.LENGTH_SHORT).show();
        }
        else if(rbStaffSize.getText().toString() == null || rbStaffSize.getText().toString().equals("")){
            Toast.makeText(this, "Please select an approximate staff size.", Toast.LENGTH_SHORT).show();
        }
        else if(rbIndustryType.getText().toString() == null || rbIndustryType.getText().toString().equals("")){
            Toast.makeText(this, "Please select company industry type.", Toast.LENGTH_SHORT).show();
        }
        else{
            firestore.collection("Companies").document(companyID).update(company).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(@NonNull Void aVoid) {
                    progressDialog.dismiss();
                    finish();
                    Toast.makeText(EditCompanyActivity.this, "company info updated successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "fail to update company info");
                }
            });
        }
    }
}