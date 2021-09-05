package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.eam.adapter.CompanyListAdapter;
import com.example.eam.databinding.ActivitySelectCompanyBinding;
import com.example.eam.model.Company;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SelectCompanyActivity extends AppCompatActivity {
    private static final String TAG = "SelectCompanyActivity";
    private ActivitySelectCompanyBinding binding;
    private List<Company> list = new ArrayList<>();
    private CompanyListAdapter adapter;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_company);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "userID" + firebaseUser.getUid());

        /*adapter = new CompanyListAdapter(list,SelectCompanyActivity.this);
        binding.recyclerView.setAdapter(adapter);*/

        getCompanyList();
    }

    private void getCompanyList() {
        list.clear();

        firestore.collection("users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> group = (List<String>) document.get("CompanyID");
                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

                        for(String companyid : group){
                            /*firestore.collection("Companies").document(companyid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                                    Companies company = documentSnapshot.toObject(Companies.class);
                                    Log.d(TAG, "compID:" + company.getCompanyID());
                                    Log.d(TAG, "compName:" + company.getCompanyName());
                                    //list.add(company);
                                }
                            });*/

                            tasks.add(firestore.collection("Companies").document(companyid).get());
                        }

                        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                            @Override
                            public void onSuccess(@NonNull List<Object> objlist) {
                                try {
                                    for (Object object : objlist) {
                                        Company company = ((DocumentSnapshot) object).toObject(Company.class);
                                        list.add(company);

                                        Log.d(TAG, "companyID" + company.getCompanyID());
                                        Log.d(TAG, "companyName" + company.getCompanyName());
                                    }
                                }catch (Exception e){
                                    Log.d(TAG, "onSuccess: "+e.getMessage());
                                }

                                adapter = new CompanyListAdapter(list,SelectCompanyActivity.this);
                                binding.recyclerView.setAdapter(adapter);

                                if (adapter!=null){
                                    adapter.notifyItemInserted(0);
                                    adapter.notifyDataSetChanged();

                                    Log.d(TAG, "onSuccess: adapter "+adapter.getItemCount());
                                }
                            }
                        });

                        /*for(String companyid : group){
                            Companies company = new Companies();
                            company.setCompanyID(companyid);

                            list.add(company);
                        }*/

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                    } else {
                        Log.d(TAG, "No such document. Can't find ur company. Create new company or contact ur company's admin");
                        startActivity(new Intent(SelectCompanyActivity.this, CreateCompanyActivity.class));
                        finish();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


        /*firestore.collection("users").document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //note.setDocumentID(documentSnapshot.getId());
                //Users2 user = new Users2();

                Users2 user2 = documentSnapshot.toObject(Users2.class);

                for(String companyid : user2.getUserCompanies()){
                    Companies company = new Companies();
                    company.setCompanyID(companyid);

                    list.add(company);
                }

                //List<String> group = (List<String>) documentSnapshot.get("dungeon_group");

                //String companyID = documentSnapshot.getString("companyID");

                adapter = new CompanyListAdapter(list,SelectCompanyActivity.this);
                binding.recyclerView.setAdapter(adapter);
            }

        });*/
    }
}