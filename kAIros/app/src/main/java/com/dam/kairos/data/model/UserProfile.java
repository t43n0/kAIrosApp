package com.dam.kairos.data.model;

public class UserProfile {

    private String userId;
    private String username;
    private String email;
    private String sex;
    private String birthdate;
    private boolean remindersEnabled;

    public UserProfile() {} // Fi

    public UserProfile(String userId, String username, String email, String sex, String birthdate, boolean remindersEnabled) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.sex = sex;
        this.birthdate = birthdate;
        this.remindersEnabled = remindersEnabled;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }

    public boolean isRemindersEnabled() { return remindersEnabled; }
    public void setRemindersEnabled(boolean remindersEnabled) { this.remindersEnabled = remindersEnabled; }
}
