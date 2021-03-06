package com.example.eam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.eam.databinding.ActivityCreateCompanyBinding;
import com.example.eam.model.Company;
import com.example.eam.model.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateCompanyActivity extends AppCompatActivity {
    private static final String TAG = "CreateCompanyActivity";
    private ActivityCreateCompanyBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private double latitude, longitude;
    private String address;
    private boolean userExists = false;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_company);

        Intent i = getIntent();
        pos = i.getIntExtra("pos", 1);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        Places.initialize(getApplicationContext(), "AIzaSyDQymcPSWVISf1RAmlmRHwf0MaIrc_5sXU");

        validation();

        binding.etLocation.setFocusable(false);
        binding.etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place .Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(CreateCompanyActivity.this);
                startActivityForResult(intent, 100);
            }
        });

        binding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int staffSizeSelectedId = binding.rgStaffSize.getCheckedCheckableImageButtonId();
                int industrySelectedId = binding.rgIndustry.getCheckedCheckableImageButtonId();

                // find the radiobutton by returned id
                RadioButton rbStaffSize = (RadioButton) findViewById(staffSizeSelectedId);
                RadioButton rbIndustryType = (RadioButton) findViewById(industrySelectedId);

                if(binding.etCompanyName.getText().toString().equals("")){
                    binding.tvCompanyName.setError("This field is required");
                    //Toast.makeText(this, "Please fill in company title.", Toast.LENGTH_SHORT).show();
                }
                else if(latitude == 0.0 || longitude == 0.0 || binding.etLocation.getText().toString().equals("") || binding.etLocation.getText().toString() == null){
                    binding.tvCompanyAddress.setError("This field is required");
                    //Toast.makeText(this, "Please fill in company location.", Toast.LENGTH_SHORT).show();
                }
                else if(rbStaffSize == null || rbStaffSize.getText().toString().equals("")){
                    Toast.makeText(CreateCompanyActivity.this, "Please select an approximate staff size.", Toast.LENGTH_SHORT).show();
                }
                else if(rbIndustryType == null || rbIndustryType.getText().toString().equals("")){
                    Toast.makeText(CreateCompanyActivity.this, "Please select company industry type.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Company company = new Company();
                    company.setCompanyName(binding.etCompanyName.getText().toString());
                    company.setStaffSize(rbStaffSize.getText().toString());
                    company.setIndustryType(rbIndustryType.getText().toString());
                    company.setPunchRange(0.5);
                    company.setCreatorID(firebaseUser.getUid());

                    Map<String,Object> location = new HashMap<>();
                    location.put("latitude", latitude);
                    location.put("longitude", longitude);
                    company.setCompanyLocation(location);

                    startActivity(new Intent(CreateCompanyActivity.this, RegisterUserActivity.class)
                            .putExtra("companyObj", company)
                            .putExtra("pos", pos));
                }
                //createCompany();
            }
        });
    }

    private void validation() {
        binding.etCompanyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().isEmpty()){
                    binding.tvCompanyName.setError("Field cannot be empty");
                }
                else{
                    binding.tvCompanyName.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    binding.tvCompanyAddress.setError("Field cannot be empty");
                }
                else{
                    binding.tvCompanyAddress.setError(null);
                }
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

    private void createCompany() {
        String companyName = binding.etCompanyName.getText().toString();
        //String industryType = binding.etIndustryType.getText().toString();
        //int staffSize = Integer.parseInt(binding.etStaffSize.getText().toString());
        //double range = Double.parseDouble(binding.etRange.getText().toString());
        //String companyID = UUID.randomUUID().toString();

        //DocumentReference documentReference = firestore.collection("Companies").document();

        //WriteBatch batch = firestore.batch();

        DocumentReference ref = firestore.collection("Companies").document();
        String companyID = ref.getId();

        Map<String,Object> company = new HashMap<>();
        Map<String,Object> location = new HashMap<>();
        company.put("companyID", companyID);
        company.put("companyName", companyName);
        //company.put("staffSize", staffSize);
        //company.put("industryType", industryType);
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        company.put("companyLocation", location);
        company.put("company_address", address);
        company.put("creatorID", firebaseUser.getUid());
        //company.put("punchRange", range);
        //batch.set(ref, company);

        DocumentReference ref2 = firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid());
        /*Map<String,Object> addUserData = new HashMap<>();
        addUserData.put("id", firebaseUser.getUid());
        addUserData.put("name", "dan");
        addUserData.put("email", "dan@gmail.com");
        addUserData.put("phoneNo", firebaseUser.getPhoneNumber());
        addUserData.put("title", "leader");
        addUserData.put("department", "meme");*/
        //batch.set(ref2, addUserData);
        User user = new User(firebaseUser.getUid(),"dan",firebaseUser.getPhoneNumber(),"","dan@gmail.com","","leader","bake", "08:00","18:30",630);

        DocumentReference ref3 = firestore.collection("Companies").document(companyID).collection("Admin").document(firebaseUser.getUid());
        Map<String,Object> addAdmin = new HashMap<>();
        addAdmin.put("id", firebaseUser.getUid());
        addAdmin.put("name", "dan");
        addAdmin.put("role", "creator");


        DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
        Map<String, Object> addUser = new HashMap<>();
        addUser.put("ID", firebaseUser.getUid());
        addUser.put("PhoneNo", firebaseUser.getPhoneNumber());
        addUser.put("CompanyID", FieldValue.arrayUnion(companyID));
        //batch.set(userRef, addUser);


        //DocumentReference Ref = firestore.collection("users").document(firebaseUser.getUid());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (!document.exists()) {
                        WriteBatch batch = firestore.batch();

                        Map<String, Object> addUser = new HashMap<>();
                        addUser.put("ID", firebaseUser.getUid());
                        addUser.put("PhoneNo", firebaseUser.getPhoneNumber());
                        addUser.put("CompanyID", FieldValue.arrayUnion(companyID));
                        batch.set(userRef, addUser);
                        batch.set(ref, company);
                        batch.set(ref3, addAdmin);
                        batch.set(ref2, user);

                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CreateCompanyActivity.this, "Successfully committed", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreateCompanyActivity.this, "Fail to commit", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else{
                        WriteBatch batch = firestore.batch();

                        batch.set(ref, company);
                        batch.set(ref2, user);
                        batch.set(ref3, addAdmin);
                        batch.update(userRef, "CompanyID", FieldValue.arrayUnion(companyID));
                        //firestore.collection("users").document(firebaseUser.getUid()).update("CompanyID", FieldValue.arrayUnion(companyID));

                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(CreateCompanyActivity.this, "Successfully committed", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(CreateCompanyActivity.this, "Fail to commit", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });


        /*batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(CreateCompanyActivity.this, "Successfully committed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateCompanyActivity.this, "Fail to commit", Toast.LENGTH_SHORT).show();
            }
        });*/

        //firestore.collection("users").document(firebaseUser.getUid()).update("CompanyID", FieldValue.arrayUnion(companyID));

        /*ref.set(company).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(@NonNull Void aVoid) {
                Toast.makeText(CreateCompanyActivity.this, "Company created successfully", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(getApplicationContext(), ));
                //finish();
            }
        });*/

        /*documentReference.set(company).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "onSuccess: user profile is created for " + companyName);
        });*/
    }
}