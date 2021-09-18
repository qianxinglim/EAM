package com.example.eam.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.eam.R;
import com.example.eam.common.CalendarUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder>{
    private final List<Uri> imageList;
    private Context context;
    private final OnClickListener onClickListener;

    public AttachmentAdapter(List<Uri> imageList, Context context, OnClickListener onClickListener) {
        this.imageList = imageList;
        this.onClickListener = onClickListener;
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
        final Uri image = imageList.get(position);

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //imageList.remove(position);
                onClickListener.onBtnDeleteClick(view, position);
            }
        });

        if(image != null) {
            String displayName = getUriPath(image);
            String extension = displayName.split("\\.")[1];

            if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")){
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), image);
                    holder.imageview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                if(extension.equals("pdf")){
                    holder.imageview.setImageResource(R.drawable.pdf);
                }
                else if(extension.equals("doc") || extension.equals("docx")){
                    holder.imageview.setImageResource(R.drawable.doc);
                }
                else if(extension.equals("ppt") || extension.equals("pptx")){
                    holder.imageview.setImageResource(R.drawable.ppt);
                }
                else if(extension.equals("xls") || extension.equals("xlsx")){
                    holder.imageview.setImageResource(R.drawable.xls);
                }
                else if(extension.equals("txt")){
                    holder.imageview.setImageResource(R.drawable.txt);
                }
                else if(extension.equals("zip")){
                    holder.imageview.setImageResource(R.drawable.zip);
                }
            }
        }

        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!date.equals("")) {
                    //if(date.equals(CalendarUtils.selectedDate))
                        //holder.parentView.setBackgroundColor(Color.LTGRAY);

                    //Toast.makeText(context, "Selected Date: " +  days.get(position), Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    private String getUriPath(Uri pdfUri){
        String uriString = pdfUri.toString();
        File theFile = new File(uriString);
        String filePath = theFile.getAbsolutePath();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(pdfUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = theFile.getName();
        }

        return displayName;
    }

    @Override
    public int getItemCount(){
        return imageList.size();
    }

    public interface OnClickListener{
        void onBtnDeleteClick(View view, int position);
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
