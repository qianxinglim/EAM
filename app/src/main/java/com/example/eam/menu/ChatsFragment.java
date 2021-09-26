package com.example.eam.menu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.eam.ChatActivity;
import com.example.eam.ContactActivity;
import com.example.eam.ProfileActivity;
import com.example.eam.R;
import com.example.eam.adapter.ChatListAdapter;
import com.example.eam.adapter.ContactsAdapter;
import com.example.eam.databinding.ActivityChatBinding;
import com.example.eam.databinding.FragmentChatsBinding;
import com.example.eam.managers.SessionManager;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ChatsFragment extends Fragment {

    private static final String TAG = "ChatsFragment";

    public ChatsFragment() {
        // Required empty public constructor
    }

    private FragmentChatsBinding binding;
    FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private List<Chatlist> list;
    private ArrayList<String> allUserID;
    private Handler handler = new Handler();
    private ChatListAdapter adapter;
    private SessionManager sessionManager;
    private String companyID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chats, container, false);

        //setSupportActionBar(binding.toolbar);
        //((AppCompatActivity)getActivity()).setSupportActionBar(binding.toolbar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(getContext());
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        list = new ArrayList<>();
        allUserID = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListAdapter(list, getContext());
        binding.recyclerView.setAdapter(adapter);

        if(firebaseUser != null){
            getChatList();
        }

        binding.fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ContactActivity.class).putExtra("pos", 2));
            }
        });

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

        return binding.getRoot();
    }

    private void filter(String text) {
        binding.recyclerView.setVisibility(View.GONE);
        //binding.progressBar.setVisibility(View.VISIBLE);

        ArrayList<Chatlist> searchList = new ArrayList<>();

        for(Chatlist chat : list){
            if(chat.getUserName().toLowerCase().contains(text.toLowerCase())){
                searchList.add(chat);
            }
        }

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatListAdapter(searchList, getContext());
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.setVisibility(View.VISIBLE);
        //binding.progressBar.setVisibility(View.GONE);
    }

    private void getChatList() {
        binding.progressCircular.setVisibility(View.VISIBLE);

        reference.child(companyID).child("ChatList").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                allUserID.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String userID = Objects.requireNonNull(snapshot.child("chatID").getValue()).toString();
                    Log.d(TAG, "onDataChange: userid" + userID);

                    binding.progressCircular.setVisibility(View.GONE);
                    allUserID.add(userID);
                    //getUserData(userID);
                }
                getUserInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserInfo() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                for(String userID : allUserID) {
                    firestore.collection("Companies").document(companyID).collection("Users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                            Log.d(TAG, "onSuccess: ddd" + documentSnapshot.getString("Name"));
                            try {
                                Chatlist chat = new Chatlist(
                                        documentSnapshot.getString("id"),
                                        documentSnapshot.getString("name"),
                                        "this is description..",
                                        "",
                                        documentSnapshot.getString("profilePic")
                                );
                                list.add(chat);
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
                    });
                }
            }
        });
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.menu_search :  Toast.makeText(getContext(), "Action Search", Toast.LENGTH_LONG).show(); break;
            case R.id.action_new_group :  Toast.makeText(getContext(), "Action New Group", Toast.LENGTH_LONG).show(); break;
            case R.id.action_new_broadcast :  Toast.makeText(getContext(), "Action New Broadcast", Toast.LENGTH_LONG).show(); break;
            case R.id.action_wa_web :  Toast.makeText(getContext(), "Action Web", Toast.LENGTH_LONG).show(); break;
            case R.id.action_starred_message :  Toast.makeText(getContext(), "Action Starred Message", Toast.LENGTH_LONG).show(); break;
            case R.id.action_settings :
                startActivity(new Intent(getContext(), ProfileActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeFabIcon(final int index){
        binding.fabAction.hide();
        //binding.btnAddStatus.setVisibility(View.GONE);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (index){
                    case 0 :
                        binding.fabAction.hide();
                        binding.fabAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(getContext(), ContactActivity.class));
                            }
                        });
                        break;
                    case 1 :
                        binding.fabAction.show();
                        //binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_chat_black_24dp));
                        break;
                    case 2 :

                        binding.fabAction.show();
                        //binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_camera_alt_black_24dp));
                        //binding.btnAddStatus.setVisibility(View.VISIBLE);
                        break;

                    case 3 :
                        binding.fabAction.show();
                        //binding.fabAction.setImageDrawable(getDrawable(R.drawable.ic_call_black_24dp));
                        break;
                }

            }
        },400);

        //performOnClick(index);
    }*/
}