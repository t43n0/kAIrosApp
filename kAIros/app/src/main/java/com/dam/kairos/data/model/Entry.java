package com.dam.kairos.data.model;

public class Entry {
    private String id;
    private String userId;
    private String date;
    private String text;
    private String imageUrl;
    private boolean imagen;
    private boolean publica;
    private boolean liked;
    private boolean saved;
    private long likeCount;

    public Entry(String id, String userId, String text, String imageUrl, boolean imagen, boolean publica, boolean liked, boolean saved, long likeCount) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.imageUrl = imageUrl;
        this.liked = liked;
        this.saved = saved;
        this.likeCount = likeCount;
    }

    // Getters y setters

    public boolean isImagen() {
        return imagen;
    }

    public boolean isPublica() {
        return publica;
    }

    public void setImagen(boolean imagen) {
        this.imagen = imagen;
    }

    public void setPublica(boolean publica) {
        this.publica = publica;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isLiked() {
        return liked;
    }

    public boolean isSaved() {
        return saved;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setText(String text) {
        this.text = text;
    }

     public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
