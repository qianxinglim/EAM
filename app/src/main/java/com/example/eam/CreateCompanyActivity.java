package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.eam.databinding.ActivityCreateCompanyBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.UUID;

public class CreateCompanyActivity extends AppCompatActivity {
    private static final String TAG = "CreateCompanyActivity";
    private ActivityCreateCompanyBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private int latitude = 0, longitude = 1;
    private boolean userExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_company);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        binding.btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCompany();
            }
        });
    }

    private void createCompany() {
        String companyName = binding.etCompanyName.getText().toString();
        String industryType = binding.etIndustryType.getText().toString();
        int staffSize = Integer.parseInt(binding.etStaffSize.getText().toString());
        //String companyID = UUID.randomUUID().toString();

        //DocumentReference documentReference = firestore.collection("Companies").document();

        WriteBatch batch = firestore.batch();

        DocumentReference ref = firestore.collection("Companies").document();
        String companyID = ref.getId();

        Map<String,Object> company = new HashMap<>();
        Map<String,Object> location = new HashMap<>();
        company.put("companyID", companyID);
        company.put("companyName", companyName);
        company.put("staffSize", staffSize);
        company.put("industryType", industryType);
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        company.put("companyLocation", location);
        //company.put("company_address", );
        company.put("creatorID", firebaseUser.getUid());
        batch.set(ref, company);

        DocumentReference ref2 = firestore.collection("Companies").document(companyID).collection("Users").document(firebaseUser.getUid());
        Map<String,Object> addUserData = new HashMap<>();
        addUserData.put("Name", "dan");
        addUserData.put("Email", "dan@gmail.com");
        addUserData.put("PhoneNo", firebaseUser.getPhoneNumber());
        addUserData.put("Title", "leader");
        addUserData.put("Department", "meme");
        batch.set(ref2, addUserData);


        DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
        Map<String, Object> addUser = new HashMap<>();
        addUser.put("ID", firebaseUser.getUid());
        addUser.put("PhoneNo", firebaseUser.getPhoneNumber());
        addUser.put("CompanyID", FieldValue.arrayUnion(companyID));
        batch.set(userRef, addUser);


        /*DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();

                    if (!document.exists()) {
                        //userExists = false;


                        Map<String, Object> addUser = new HashMap<>();
                        addUser.put("ID", firebaseUser.getUid());
                        addUser.put("PhoneNo", firebaseUser.getPhoneNumber());
                        addUser.put("CompanyID", FieldValue.arrayUnion(companyID));
                        batch.set(userRef, addUser);

                    }
                    else{

                        //userExists = true;
                        //firestore.collection("users").document(firebaseUser.getUid()).update("CompanyID", FieldValue.arrayUnion(companyID));
                        //batch.update(userRef, "CompanyID", FieldValue.arrayUnion(companyID));
                    }
                }
            }
        });*/


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