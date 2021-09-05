package com.example.eam.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.eam.MainActivity;
import com.example.eam.R;
import com.example.eam.managers.SessionManager;
import com.example.eam.model.Company;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CompanyListAdapter extends RecyclerView.Adapter<CompanyListAdapter.Holder>{
    private List<Company> list;
    private Context context;
    private SessionManager sessionManager;

    public CompanyListAdapter(List<Company> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setList(List<Company> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CompanyListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_company_list,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Company companylist = list.get(position);

        holder.tvCompanyName.setText(companylist.getCompanyName());
        holder.tvCompanyID.setText(companylist.getCompanyID());

        sessionManager = new SessionManager(context);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionManager.createSession(companylist.getCompanyID());

                context.startActivity(new Intent(context, MainActivity.class));
                ((Activity)context).finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        private TextView tvCompanyName, tvCompanyID;

        public Holder(@NonNull View itemView) {
            super(itemView);

            tvCompanyID = itemView.findViewById(R.id.tv_companyID);
            tvCompanyName = itemView.findViewById(R.id.tv_companyName);
        }
    }
}
