package com.dam.kairos.data.model;

import java.util.Date;

public class EntryDiario {
    private String id;
    private String idUser;
    private String text;
    private String imageUrl;
    private Date date;
    private String formattedDate;

    public EntryDiario() {}

    public EntryDiario(String id, String idUser, String text, String imageUrl, Date date, String formattedDate) {
        this.id = id;
        this.idUser = idUser;
        this.text = text;
        this.imageUrl = imageUrl;
        this.date = date;
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

    public Date getDate() {
        return date;
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

    public void setDate(Date date) {
        this.date = date;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }
}
