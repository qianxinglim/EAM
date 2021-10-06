package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eam.IndvChatActivity;
import com.example.eam.R;
import com.example.eam.TaskFormActivity;
import com.example.eam.UserProfileActivity;
import com.example.eam.WeeklyViewActivity;
import com.example.eam.model.Attendance;
import com.example.eam.model.User;
import com.google.android.gms.tasks.Task;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AssignUsersAdapter extends RecyclerView.Adapter<AssignUsersAdapter.ViewHolder>{
    private List<User> list;
    private Context context;

    public AssignUsersAdapter(List<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_assign_item, parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);

        if (position > 0 && list.get(position - 1).getDepartment().equals(user.getDepartment())) {
            holder.tvHeader.setVisibility(View.GONE);
        } else {
            holder.tvHeader.setVisibility(View.VISIBLE);
            holder.tvHeader.setText(user.getDepartment());
        }

        holder.tvName.setText(user.getName());

        if(user.getProfilePic().equals("")){
            holder.profile.setImageResource(R.drawable.icon_male_ph);
        }
        else{
            Glide.with(context).load(user.getProfilePic()).into(holder.profile);
        }

        holder.checkBox.setChecked(false);

        for(User selectedUser : TaskFormActivity.selectedUserList){
            if(user.getID().equals(selectedUser.getID())){
                holder.checkBox.setChecked(true);
            }
        }


        /*holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.checkBox.isChecked()){
                    TaskFormActivity.selectedUserList.add(user);
                }
                else{
                    for(Iterator<User> iter = TaskFormActivity.selectedUserList.listIterator(); iter.hasNext();) {
                        User user1 = iter.next();

                        if(user1 == user){
                            iter.remove();
                        }
                    }
                }
            }
        });*/


        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.checkBox.isChecked()){
                    TaskFormActivity.selectedUserList.add(user);
                    TaskFormActivity.selectedUserIdList.add(user.getID());
                }
                else{
                    //int i = TaskFormActivity.selectedUserList.indexOf();
                    TaskFormActivity.selectedUserList.remove(user);
                    TaskFormActivity.selectedUserIdList.remove(user.getID());
                    Log.d("AssignUserAdapter", "remove: " + user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvName, tvHeader;
        private CircularImageView profile;
        private CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            profile = itemView.findViewById(R.id.image_profile);
            tvHeader = itemView.findViewById(R.id.tvHeader);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
