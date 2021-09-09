package com.example.eam.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.eam.R;
import com.example.eam.model.Chats;
import com.example.eam.service.AudioService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder>{
    private List<Chats> list;
    private Context context;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private FirebaseUser firebaseUser;
    private ImageButton tmpBtnPlay;
    private AudioService audioService;

    public ChatsAdapter(List<Chats> list, Context context) {
        this.list = list;
        this.context = context;
        this.audioService = new AudioService(context);
    }

    public void setList(List<Chats> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new ViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textMessage, imageText, tvDocumentName;
        private LinearLayout layoutText, layoutImage, layoutVoice, layoutDocument;
        private ImageView imageMessage, imageFileType;
        private ImageButton btnPlay, btnDownload;
        private ViewHolder tmpHolder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textMessage = itemView.findViewById(R.id.tv_text_message);
            layoutImage = itemView.findViewById(R.id.layout_image);
            layoutText = itemView.findViewById(R.id.layout_text);
            layoutDocument = itemView.findViewById(R.id.layout_document);
            imageMessage = itemView.findViewById(R.id.image_chat);
            imageText = itemView.findViewById(R.id.image_text);
            layoutVoice = itemView.findViewById(R.id.layout_voice);
            btnPlay = itemView.findViewById(R.id.btn_play_chat);
            btnDownload = itemView.findViewById(R.id.btn_download);
            tvDocumentName = itemView.findViewById(R.id.tvDocumentName);
            imageFileType = itemView.findViewById(R.id.document_chat);

        }
        void bind(Chats chats){
            //Check chat type
            switch (chats.getType()){
                case "TEXT":
                    layoutDocument.setVisibility(View.GONE);
                    layoutText.setVisibility(View.VISIBLE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);

                    textMessage.setText(chats.getTextMessage());
                    break;

                case "IMAGE":
                    layoutDocument.setVisibility(View.GONE);
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.VISIBLE);
                    layoutVoice.setVisibility(View.GONE);

                    Glide.with(context).load(chats.getUrl()).into(imageMessage);

                    if (!chats.getTextMessage().equals("") && chats.getTextMessage() != null) {
                        imageText.setVisibility(View.VISIBLE);
                        imageText.setText(chats.getTextMessage());
                    } else {
                        imageText.setVisibility(View.GONE);
                    }

                    break;

                case "DOCUMENT":
                    layoutDocument.setVisibility(View.VISIBLE);
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.GONE);

                    tvDocumentName.setText(chats.getTextMessage());

                    btnDownload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(chats.getUrl()));
                            context.startActivity(intent);
                        }
                    });

                    String extension = chats.getTextMessage().split("\\.")[1];
                    if(extension.equals("pdf")){
                        imageFileType.setImageResource(R.drawable.pdf);
                    }
                    else if(extension.equals("doc") || extension.equals("docx")){
                        imageFileType.setImageResource(R.drawable.doc);
                    }
                    else if(extension.equals("ppt") || extension.equals("pptx")){
                        imageFileType.setImageResource(R.drawable.ppt);
                    }
                    else if(extension.equals("xls") || extension.equals("xlsx")){
                        imageFileType.setImageResource(R.drawable.xls);
                    }
                    else if(extension.equals("txt")){
                        imageFileType.setImageResource(R.drawable.txt);
                    }
                    else if(extension.equals("zip")){
                        imageFileType.setImageResource(R.drawable.zip);
                    }

                    break;

                case "VOICE":
                    layoutDocument.setVisibility(View.GONE);
                    layoutText.setVisibility(View.GONE);
                    layoutImage.setVisibility(View.GONE);
                    layoutVoice.setVisibility(View.VISIBLE);

                    layoutVoice.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(tmpBtnPlay != null){
                                tmpBtnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                            }

                            btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_pause_circle_filled_24));
                            audioService.playAudioFromUrl(chats.getUrl(), new AudioService.OnPlayCallBack() {
                                @Override
                                public void onFinished() {
                                    btnPlay.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_baseline_play_circle_filled_24));
                                }
                            });
                            tmpBtnPlay = btnPlay;
                        }
                    });
                    break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(list.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }
}
