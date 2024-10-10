package com.example.catchcontroller;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitAPI {
    @Headers({"Content-Type:application/json"})
    @POST("conversation")
    Call<ResponseBody> createPost(@Body MessageBody messages);
}