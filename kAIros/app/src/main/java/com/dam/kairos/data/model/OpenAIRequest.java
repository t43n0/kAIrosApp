package com.dam.kairos.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpenAIRequest {
    @SerializedName("model")
    public String model;

    @SerializedName("messages")
    public List<Message> messages;

    @SerializedName("max_tokens")
    public int maxTokens;

    public OpenAIRequest(String model, List<Message> messages, int maxTokens) {
        this.model = model;
        this.messages = messages;
        this.maxTokens = maxTokens;
    }

    public String getModel() {
        return model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
}

