package com.example.player;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.List;

class Playlist implements Parcelable {
    public String title, uploadId, uid, image, mKey, code;
    public List<Song> lista;

    public Playlist(String title, String uploadId, String uid, String image, List<Song> lista, String code){
        this.title=title;
        this.uploadId=uploadId;
        this.uid=uid;
        this.image=image;
        this.lista=lista;
        this.code=code;
    }

    public Playlist(){}

    protected Playlist(Parcel in) {
        title = in.readString();
        uploadId = in.readString();
        uid = in.readString();
        image = in.readString();
        mKey = in.readString();
        code = in.readString();
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Song> getLista() {
        return lista;
    }

    public void setLista(List<Song> lista) {
        this.lista = lista;
    }

    public String getCode(){
        return code;
    }

    public void setCode(String code){
        this.code=code;
    }

    @Exclude
    public String getmKey() {
        return mKey;
    }
    @Exclude
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(uploadId);
        dest.writeString(uid);
        dest.writeString(image);
        dest.writeString(mKey);
        dest.writeString(code);
    }
}
