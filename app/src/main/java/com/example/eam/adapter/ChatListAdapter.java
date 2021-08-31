package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.example.eam.IndvChatActivity;
import com.example.eam.R;
import com.example.eam.model.Chatlist;
import com.mikhaellopez.circularimageview.CircularImageView;
//import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {
    private List<Chatlist> list;
    private Context context;

    public ChatListAdapter(List<Chatlist> list, Context context) {
        this.list = list;
        this.context = context;
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

        holder.tvName.setText(chatlist.getUserName());
        holder.tvDesc.setText(chatlist.getDescription());
        holder.tvDate.setText(chatlist.getDate());

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
        public Holder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvName = itemView.findViewById(R.id.tv_name);
            profile = itemView.findViewById(R.id.image_profile);
        }
    }
}
