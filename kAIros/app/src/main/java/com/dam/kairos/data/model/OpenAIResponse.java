package com.dam.kairos.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OpenAIResponse {
    @SerializedName("choices")
    public List<Choice> choices;

    public static class Choice {
        @SerializedName("message")
        public Message message;

        public Message getMessage() {
            return message;
        }
    }
}
