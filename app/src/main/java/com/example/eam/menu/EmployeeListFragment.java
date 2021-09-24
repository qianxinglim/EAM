package com.example.eam.menu;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eam.R;
import com.example.eam.adapter.EmployeeListAdapter;
import com.example.eam.databinding.FragmentEmployeeListBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EmployeeListFragment extends Fragment {

    private static final String TAG = "EmployeeListFragment";

    public EmployeeListFragment() {
        // Required empty public constructor
    }

    private FragmentEmployeeListBinding binding;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firestore;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private EmployeeListAdapter adapter;
    private List<User> list;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_employee_list, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        list = new ArrayList<>();

        getEmployeeList();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmployeeListAdapter(list, getContext());
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    private void getEmployeeList(){
        firestore.collection("Companies").document(companyID).collection("Users").orderBy("department", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                list.clear();

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("name") != null) {
                        User user = new User(doc.getString("id"), doc.getString("name"), doc.getString("phoneNo"), doc.getString("profilePic"), doc.getString("email"),"", doc.getString("title"), doc.getString("department"),doc.getString("clockInTime"),doc.getString("clockOutTime"),doc.getLong("minutesOfWork").intValue());

                        list.add(user);

                        Log.d(TAG, "list: " + doc.getString("name"));

                        if(adapter != null){
                            adapter.notifyItemInserted(0);
                            adapter.notifyDataSetChanged();

                            Log.d(TAG, "onSuccess: adapter" + adapter.getItemCount());
                        }
                    }
                }
            }
        });
    }
}