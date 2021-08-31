package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.eam.adapter.ChatListAdapter;
import com.example.eam.databinding.ActivityChatBinding;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Chatlist;
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

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private ActivityChatBinding binding;
    //FirebaseAuth mAuth;
    FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private List<Chatlist> list;
    private ArrayList<String> allUserID;
    private Handler handler = new Handler();
    private ChatListAdapter adapter;
    private SessionManager sessionManager;
    private String companyID;
    //String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        //setUpWithViewPager(binding.viewPager);
        //binding.tabLayout.setupWithViewPager(binding.viewPager);
        setSupportActionBar(binding.toolbar);

        //FirebaseApp.initializeApp(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(this);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);

        list = new ArrayList<>();
        allUserID = new ArrayList<>();
        
        //mAuth = FirebaseAuth.getInstance();
        //fStore = FirebaseFirestore.getInstance();

        //userID = mAuth.getCurrentUser().getUid();
        //FirebaseUser user = mAuth.getCurrentUser();

        //RecyclerView recyclerView = findViewById(R.id.recyclerView);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatListAdapter(list, this);
        binding.recyclerView.setAdapter(adapter);

        if(firebaseUser != null){
            getChatList();
        }

        binding.fabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChatActivity.this, ContactActivity.class));
            }
        });
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

    /*private void getUserData(String userID) {
        firestore.collection("users").document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                Chatlist chat = new Chatlist(
                        documentSnapshot.getString("ID"),
                        documentSnapshot.getString("Name"),
                        "this is description..",
                        "",
                        documentSnapshot.getString("ProfilePic")
                );
                list.add(chat);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onDataChange: list " + list.size());
            }
        });
    }*/

    /*private void setUpWithViewPager(ViewPager viewPager){
        ChatActivity.SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        //adapter.addFragment(new CameraFragment(),"");
        adapter.addFragment(new ChatsFragment(),"Chats");
        //adapter.addFragment(new StatusFragment(),"Status");
        adapter.addFragment(new CallsFragment(),"Calls");
        viewPager.setAdapter(adapter);
    }*/

    /*private static class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.menu_search :  Toast.makeText(ChatActivity.this, "Action Search", Toast.LENGTH_LONG).show(); break;
            case R.id.action_new_group :  Toast.makeText(ChatActivity.this, "Action New Group", Toast.LENGTH_LONG).show(); break;
            case R.id.action_new_broadcast :  Toast.makeText(ChatActivity.this, "Action New Broadcast", Toast.LENGTH_LONG).show(); break;
            case R.id.action_wa_web :  Toast.makeText(ChatActivity.this, "Action Web", Toast.LENGTH_LONG).show(); break;
            case R.id.action_starred_message :  Toast.makeText(ChatActivity.this, "Action Starred Message", Toast.LENGTH_LONG).show(); break;
            case R.id.action_settings :
                startActivity(new Intent(ChatActivity.this, ProfileActivity.class));
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
                                startActivity(new Intent(ChatActivity.this, ContactActivity.class));
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
    }
}