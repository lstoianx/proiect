package com.example.player;

import com.google.firebase.database.Exclude;

public class User {
    public String email, firstName, lastName, stageName, userType, uid, mKey;

    public User(String email, String firstName, String lastName, String stageName, String userType, String uid){
        this.email=email;
        this.firstName=firstName;
        this.lastName=lastName;
        this.stageName=stageName;
        this.userType=userType;
        this.uid = uid;
    }

    public User(){}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    @Exclude
    public String getmKey() {
        return mKey;
    }
    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }
}
