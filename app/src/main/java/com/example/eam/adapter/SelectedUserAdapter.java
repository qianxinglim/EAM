package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eam.IndvChatActivity;
import com.example.eam.R;
import com.example.eam.UserProfileActivity;
import com.example.eam.model.User;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SelectedUserAdapter extends RecyclerView.Adapter<SelectedUserAdapter.ViewHolder>{
    private List<User> list;
    private Context context;
    private final OnClickListener onClickListener;

    public SelectedUserAdapter(List<User> list, Context context, OnClickListener onClickListener) {
        this.list = list;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.selected_user_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickListener.onBtnDeleteClick(view, position);
            }
        });

        holder.tvName.setText(user.getName());

        if(user.getProfilePic().equals("")){
            holder.profile.setImageResource(R.drawable.icon_male_ph);
        }
        else{
            Glide.with(context).load(user.getProfilePic()).into(holder.profile);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnClickListener{
        void onBtnDeleteClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvName;
        private CircularImageView profile;
        private ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            profile = itemView.findViewById(R.id.image_profile);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
