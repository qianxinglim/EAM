package com.example.eam.interfaces;

import com.example.eam.model.Chats;

import java.util.List;

public interface OnReadChatCallBack {
    void onReadSuccess(List<Chats> list);
    void onReadFailed();
}
