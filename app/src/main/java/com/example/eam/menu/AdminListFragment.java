package com.example.eam.menu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eam.R;
import com.example.eam.adapter.EmployeeListAdapter;
import com.example.eam.databinding.FragmentAdminListBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Leave;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AdminListFragment extends Fragment {

    private static final String TAG = "AdminListFragment";

    public AdminListFragment() {
        // Required empty public constructor
    }

    private FragmentAdminListBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private EmployeeListAdapter adapter;
    private List<User> list;
    private ArrayList<String> allAdminID;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_admin_list, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        list = new ArrayList<>();
        allAdminID = new ArrayList<>();

        getEmployeeList();

        return binding.getRoot();
    }

    private void getEmployeeList(){
        firestore.collection("Companies").document(companyID).collection("Admin").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                allAdminID.clear();

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.getId() != null) {
                        allAdminID.add(doc.getId());
                        Log.d(TAG, "docId: " + doc.getId());
                    }
                }

                getUserInfo();
            }
        });
    }

    private void getUserInfo() {
        list.clear();

        handler.post(new Runnable() {
            @Override
            public void run() {
                for(String userID : allAdminID) {
                    firestore.collection("Companies").document(companyID).collection("Users").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(value.exists()){

                                try {
                                    User user = new User(value.getString("id"), value.getString("name"), value.getString("phoneNo"), value.getString("profilePic"), value.getString("email"),"", value.getString("title"), value.getString("department"),value.getString("clockInTime"),value.getString("clockOutTime"),value.getLong("minutesOfWork").intValue());
                                    list.add(user);
                                }catch(Exception e){
                                    Log.d(TAG, "onSuccess: s" + e.getMessage());
                                }

                                if(adapter != null){
                                    adapter.notifyItemInserted(0);
                                    adapter.notifyDataSetChanged();

                                    Log.d(TAG, "onSuccess: adapter" + adapter.getItemCount());
                                }
                            }
                            else{

                            }
                        }
                    });

                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    list.sort(Comparator.comparing(User::getDepartment));
                    adapter = new EmployeeListAdapter(list, getContext());
                    binding.recyclerView.setAdapter(adapter);

                    /*firestore.collection("Companies").document(companyID).collection("Users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DocumentSnapshot doc) {
                            try {
                                User user = new User(doc.getString("id"), doc.getString("name"), doc.getString("phoneNo"), doc.getString("profilePic"), doc.getString("email"),"", doc.getString("title"), doc.getString("department"),doc.getString("clockInTime"),doc.getString("clockOutTime"),doc.getLong("minutesOfWork").intValue());
                                list.add(user);
                            }catch(Exception e){
                                Log.d(TAG, "onSuccess: s" + e.getMessage());
                            }
                            if(adapter != null){
                                adapter.notifyItemInserted(0);
                                adapter.notifyDataSetChanged();

                                Log.d(TAG, "onSuccess: adapter" + adapter.getItemCount());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Error " + e.getMessage());
                        }
                    });*/
                }
            }
        });
    }
}