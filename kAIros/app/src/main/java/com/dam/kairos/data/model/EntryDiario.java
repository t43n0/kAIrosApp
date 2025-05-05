package com.dam.kairos.data.model;

import com.google.firebase.Timestamp;

import java.util.Date;

public class EntryDiario {
    private String id;
    private String idUser;
    private String text;
    private String imageUrl;
    private Timestamp timestamp;
    private String formattedDate;

    public EntryDiario() {}

    public EntryDiario(String id, String idUser, String text, String imageUrl, Timestamp timestamp, String formattedDate) {
        this.id = id;
        this.idUser = idUser;
        this.text = text;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.formattedDate = formattedDate;
    }

    public String getId() {
        return id;
    }

    public String getIdUser() {
        return idUser;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }
}
