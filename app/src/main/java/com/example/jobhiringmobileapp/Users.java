package com.example.jobhiringmobileapp;

public class Users {

    public String date, fullname, profileimage, username, uid, role;

    public Users(){

    }

    public Users(String date, String fullname, String profileimage, String username, String uid, String role) {
        this.date = date;
        this.fullname = fullname;
        this.profileimage = profileimage;
        this.username = username;
        this.uid = uid;
        this.role = role;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
