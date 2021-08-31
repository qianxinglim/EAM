package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eam.IndvChatActivity;
import com.example.eam.ProfileActivity;
import com.example.eam.R;
import com.example.eam.model.Users;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder>{
    private List<Users> list;
    private Context context;

    public ContactsAdapter(List<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_contact_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Users user = list.get(position);

        holder.username.setText(user.getName());
        //holder.desc.setText(user.getUserID());
        if(!user.getProfilePic().equals("-") && user.getProfilePic()!=null && !user.getProfilePic().equals("")) {
            Glide.with(context).load(user.getProfilePic()).into(holder.imageProfile);
        }
        else{
            Glide.with(context).load(R.drawable.icon_male_ph).into(holder.imageProfile);
        }

        //Glide.with(context).load(user.getProfilePic()).into(holder.imageProfile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, IndvChatActivity.class)
                        .putExtra("userID", user.getID())
                        .putExtra("userName", user.getName())
                        .putExtra("userProfilePic", user.getProfilePic()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageProfile;
        private TextView username, desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.tv_username);
            desc = itemView.findViewById(R.id.tv_desc);
        }
    }
}
