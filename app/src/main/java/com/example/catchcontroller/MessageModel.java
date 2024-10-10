package com.example.catchcontroller;

import com.google.gson.annotations.SerializedName;

public class MessageModel {
    @SerializedName("role")
    String role;
    @SerializedName("content")
    String content;
}
