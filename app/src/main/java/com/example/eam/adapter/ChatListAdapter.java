package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.example.eam.IndvChatActivity;
import com.example.eam.R;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Chatlist;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;
//import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {
    private List<Chatlist> list;
    private Context context;
    private DatabaseReference reference;
    private SessionManager sessionManager;
    private String companyID;

    public ChatListAdapter(List<Chatlist> list, Context context) {
        this.list = list;
        this.context = context;

        reference = FirebaseDatabase.getInstance().getReference();

        sessionManager = new SessionManager(context);
        HashMap<String, String> userDetail = sessionManager.getUserDetail();
        companyID = userDetail.get(sessionManager.COMPANYID);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_chat_list,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Chatlist chatlist = list.get(position);

        reference.child(companyID).child("Chats").orderByChild("receiver").equalTo(chatlist.getUserID()).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot children: snapshot.getChildren()) {
                        String type = children.child("type").getValue().toString();
                        String dateTime = children.child("dateTime").getValue().toString();
                        String[] date = dateTime.split(",");
                        String msg = "";

                        switch (type) {
                            case "TEXT":
                                msg = children.child("textMessage").getValue().toString();
                                holder.ivIcon.setVisibility(View.GONE);
                                break;

                            case "IMAGE":
                                msg = "Photo";
                                holder.ivIcon.setVisibility(View.VISIBLE);
                                holder.ivIcon.setBackgroundResource(R.drawable.ic_baseline_photo_24);
                                break;

                            case "DOCUMENT":
                                msg = children.child("textMessage").getValue().toString();
                                holder.ivIcon.setVisibility(View.GONE);
                                break;

                            case "VOICE":
                                msg = "Voice Message";
                                holder.ivIcon.setVisibility(View.VISIBLE);
                                holder.ivIcon.setBackgroundResource(R.drawable.ic_baseline_keyboard_voice_24);
                                break;
                        }

                        holder.tvDate.setText(date[0]);
                        holder.tvDesc.setText(msg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.tvName.setText(chatlist.getUserName());
        //holder.tvDesc.setText(chatlist.getDescription());
        //holder.tvDate.setText(chatlist.getDate());

        if(chatlist.getUrlProfile().equals("")){
            holder.profile.setImageResource(R.drawable.icon_male_ph);
        }
        else{
            Glide.with(context).load(chatlist.getUrlProfile()).into(holder.profile);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, IndvChatActivity.class)
                        .putExtra("userID", chatlist.getUserID())
                        .putExtra("userName", chatlist.getUserName())
                        .putExtra("userProfilePic", chatlist.getUrlProfile()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDesc, tvDate;
        private CircularImageView profile;
        private ImageView ivIcon;

        public Holder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvName = itemView.findViewById(R.id.tv_name);
            profile = itemView.findViewById(R.id.image_profile);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
