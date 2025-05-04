package com.dam.kairos.data.network;

import com.dam.kairos.data.model.ApiKeyResponse;
import com.dam.kairos.data.model.OpenAIRequest;
import com.dam.kairos.data.model.OpenAIResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OpenAIService {
    @POST("v1/chat/completions")
    Call<OpenAIResponse> getChatGPTResponse(@Body OpenAIRequest request);

    @GET("/apikey")
    Call<ApiKeyResponse> getApiKey(@Header("Authorization") String bearerToken);
}

