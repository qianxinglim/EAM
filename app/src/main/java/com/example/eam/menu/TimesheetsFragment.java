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
import com.example.eam.adapter.ChatListAdapter;
import com.example.eam.adapter.EmployeeListAdapter;
import com.example.eam.adapter.TimesheetsAdapter;
import com.example.eam.databinding.FragmentChatsBinding;
import com.example.eam.databinding.FragmentTimesheetsBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Attendance;
import com.example.eam.model.Chatlist;
import com.example.eam.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TimesheetsFragment extends Fragment {
    private static final String TAG = "TimesheetsFragment";

    public TimesheetsFragment() {
        // Required empty public constructor
    }

    private FragmentTimesheetsBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;
    private List<User> list;
    private TimesheetsAdapter adapter;
    private Handler handler = new Handler();
    private List<String> employeelist;
    private List<Attendance> attendancelist;
    //private Handler handler = new Handler();
    //private ChatListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_timesheets, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        list = new ArrayList<>();
        attendancelist = new ArrayList<>();
        employeelist = new ArrayList<>();

        getEmployeeList();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TimesheetsAdapter(list, getContext());
        binding.recyclerView.setAdapter(adapter);

        return binding.getRoot();
    }

    private void getEmployeeList() {
        firestore.collection("Companies").document(companyID).collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                list.clear();
                employeelist.clear();

                for(QueryDocumentSnapshot doc : value){
                    if (doc.get("name") != null){
                        User user = new User(doc.getString("id"), doc.getString("name"), doc.getString("phoneNo"), doc.getString("profilePic"), doc.getString("email"),"", doc.getString("title"), doc.getString("department"),doc.getString("clockInTime"),doc.getString("clockOutTime"),doc.getLong("minutesOfWork").intValue());

                        list.add(user);
                        employeelist.add(doc.getString("id"));

                        Log.d(TAG, "list: " + doc.getString("name"));

                        if(adapter != null){
                            adapter.notifyItemInserted(0);
                            adapter.notifyDataSetChanged();

                            Log.d(TAG, "onSuccess: adapter" + adapter.getItemCount());
                        }
                    }
                }

                //getDuration();
            }
        });
    }

    private void getDuration() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for(String userID : employeelist) {
                    reference.child(companyID).child("Attendance").orderByChild("userId").equalTo(userID).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DataSnapshot dataSnapshot) {
                            /*for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Attendance attendance = snapshot.getValue(Attendance.class);

                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

                                Date clockInTime, clockOutTime;
                                int workMinutes = 0;

                                try {
                                    clockInTime = sdf.parse(attendance.getClockInTime());
                                    clockOutTime = sdf.parse(attendance.getClockOutTime());
                                    long diff = clockOutTime.getTime() - clockInTime.getTime();
                                    workMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(diff);

                                    if(clockInTime.after(clockOutTime)){

                                    }
                                    else{

                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if (attendance != null && attendance.getClockInDate().equals(date.format(formatter))){
                                    attendancelist.add(attendance);
                                }
                            }*/
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        });
    }


}