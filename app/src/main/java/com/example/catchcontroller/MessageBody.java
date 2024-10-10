package com.example.catchcontroller;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MessageBody {
    @SerializedName("messages")
    List<MessageModel> messages = new ArrayList<>();
}
