package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.eam.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Attachment2Adapter extends RecyclerView.Adapter<Attachment2Adapter.ViewHolder>{
    private final List<String> imageList;
    private Context context;

    public Attachment2Adapter(List<String> imageList, Context context) {
        this.imageList = imageList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.attachment_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String doc = imageList.get(position);

        holder.btnDelete.setVisibility(View.GONE);

        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(doc);
        Log.d("TAG", "reference: " + reference.getName());

        String extension = reference.getName().split("\\.")[1];

        if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")){
            Glide.with(context).load(doc).into(holder.imageview);
        }
        else if(extension.equals("pdf")){
            holder.imageview.setImageResource(R.drawable.pdf);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24);
        }
        else if(extension.equals("doc") || extension.equals("docx")){
            holder.imageview.setImageResource(R.drawable.doc);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24);
        }
        else if(extension.equals("ppt") || extension.equals("pptx")){
            holder.imageview.setImageResource(R.drawable.ppt);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24);
        }
        else if(extension.equals("xls") || extension.equals("xlsx")){
            holder.imageview.setImageResource(R.drawable.xls);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24);
        }
        else if(extension.equals("txt")){
            holder.imageview.setImageResource(R.drawable.txt);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24);
        }
        else if(extension.equals("zip")){
            holder.imageview.setImageResource(R.drawable.zip);
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24);
        }

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(doc));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount(){
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageview;
        private ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageview = itemView.findViewById(R.id.imageView);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
