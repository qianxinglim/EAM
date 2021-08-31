package com.example.eam.service;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

public class AudioService {
    private Context context;
    //private MediaPlayer mediaPlayer;
    private MediaPlayer tmpMediaPlayer;
    //private OnPlayCallBack onPlayCallBack;

    public AudioService(Context context) {
        this.context = context;
        //this.mediaPlayer = new MediaPlayer();
    }

    public void playAudioFromUrl(String url, final OnPlayCallBack onPlayCallBack){
        if(tmpMediaPlayer != null){
            tmpMediaPlayer.stop();
        }

        MediaPlayer mediaPlayer = new MediaPlayer();

        //this.onPlayCallBack = onPlayCallBack;

        /*if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }*/
        try {
            //mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();

            tmpMediaPlayer = mediaPlayer;
        }catch (IOException e){
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //mp.release();
                onPlayCallBack.onFinished();

            }
        });
    }

    public interface OnPlayCallBack{
        void onFinished();

    }
}
