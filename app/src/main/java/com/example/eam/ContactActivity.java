package com.example.eam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListView;

import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.databinding.ActivityContactBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactActivity extends AppCompatActivity {
    private static final String TAG = "ContactActivity";
    private ActivityContactBinding binding;
    private List<User> list = new ArrayList<>();
    private ContactsAdapter adapter;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String companyID;

    //-----------------here----------------------------
    public static final int REQUEST_READ_CONTACTS = 79;
    private ListView contactlist;
    private ArrayList mobileArray;
    //-----------------to here ------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact);

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        binding.recyclerView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        //getContactFromPhone();
        getContactList();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });


        /*if(mobileArray != null){
            getContactList();
            //for(int i=0; i<mobileArray.size();i++){
            //    Log.d(TAG, "onCreate: contacts" + mobileArray.get(i).toString());
            //}
        }*/
    }

    private void filter(String text) {
        binding.recyclerView.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        ArrayList<User> searchList = new ArrayList<>();

        for(User user : list){
            if(user.getName().toLowerCase().contains(text.toLowerCase())){
                searchList.add(user);
            }
        }

        adapter = new ContactsAdapter(searchList, ContactActivity.this);
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
    }

    private void getContactFromPhone() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            mobileArray = getAllPhoneContacts();
        }
        else{
            requestPermission();
        }

        /*list = findViewById(R.id.list);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mobileArray);
        list.setAdapter(adapter);*/
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mobileArray = getAllPhoneContacts();
                } else {
                    finish();
                }
                return;
            }
        }
    }

    private ArrayList getAllPhoneContacts() {
        ArrayList<String> phoneList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cur.getString(cur.getColumnIndex(
//                        ContactsContract.Contacts.DISPLAY_NAME));
//                nameList.add(name);

                if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneList.add(phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
        return phoneList;
    }



    private void getContactList() {
        firestore.collection("Companies").document(companyID).collection("Users").orderBy("department", Query.Direction.ASCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot snapshots : queryDocumentSnapshots){
                    User user = new User(snapshots.getString("id"), snapshots.getString("name"), snapshots.getString("phoneNo"), snapshots.getString("profilePic"), snapshots.getString("email"),"", snapshots.getString("title"), snapshots.getString("department"),snapshots.getString("clockInTime"),snapshots.getString("clockOutTime"),snapshots.getLong("minutesOfWork").intValue());

                    if(user.getID() != null && !user.getID().equals(firebaseUser.getUid())){
                        list.add(user);
                    }
                }

                //-------------------------------here-----------------------------------
                /*for(Users user : list){
                    if(mobileArray.contains(user.getUserPhone())){
                        Log.d(TAG, "getContactList: true" + user.getUserPhone());
                    }
                    else{
                        Log.d(TAG, "getContactList: false" + user.getUserPhone());
                    }
                }*/
                //------------------------------to here---------------------------------

                adapter = new ContactsAdapter(list, ContactActivity.this);
                binding.recyclerView.setAdapter(adapter);

                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
}