package com.example.player;

import com.google.firebase.database.Exclude;

public class Song {
    public String title, duration, link, uid, stageName, mKey, uploadId, genre;
    long time;

    public Song(String title, String duration, String link, String uid, String stageName, String uploadId, String genre, long time){
        this.title=title;
        this.duration=duration;
        this.link=link;
        this.uid=uid;
        this.stageName=stageName;
        this.uploadId=uploadId;
        this.genre=genre;
        this.time=time;
    }

    public Song(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUid(){
        return uid;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getStageName(){
        return stageName;
    }

    public void setStageName(String stageName){
        this.stageName = stageName;
    }

    public String getUploadId(){
        return uploadId;
    }

    public void setuploadId(String uploadId){
        this.uploadId = uploadId;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre=genre;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time=time;
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
