package com.example.player;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FollowingSongsActivity extends AppCompatActivity {

    ProgressDialog pd;

    Song song;

    Playlist playlist;

    TextView playlistTitle, playlistSongs, createdBy;
    ImageView playlistImage;

    RecyclerView playlistRv;
    static JcPlayerView jcPlayerView;
    List<Song> mUpload;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    FollowedSongsAdapter adapter;
    ProgressBar progressBar;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_songs);

        retrieveSongs();

        mUpload = new ArrayList<>();
        pd = new ProgressDialog(this);
        pd.setMessage("Loading, please wait...");

        createdBy = findViewById(R.id.createdBy);
        playlistImage = findViewById(R.id.playlistImage);
        playlistTitle = findViewById(R.id.playlistTitle);
        playlistSongs = findViewById(R.id.playlistSongs);
        playlistRv = findViewById(R.id.playlistRv);
        jcPlayerView = findViewById(R.id.jcplayer);
        progressBar = findViewById(R.id.progressBarShowSongs);

        playlistSongs.setVisibility(View.GONE);

        playlistRv.setHasFixedSize(true);
        playlistRv.setLayoutManager(new LinearLayoutManager(this));

        mUpload = new ArrayList<>();
        adapter = new FollowedSongsAdapter(this,mUpload);
        playlistRv.setAdapter(adapter);

        Intent i = getIntent();
        playlist = i.getParcelableExtra("playlist");

        playlistTitle.setText(playlist.getTitle());

        //set username
        Query query= FirebaseDatabase.getInstance().getReference("Users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    String uid = playlist.getUid();
                    if(user.getUid().equals(uid)){
                        userName = user.getFirstName()+" "+user.getLastName();
                        createdBy.setText("Created by: "+userName);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        String image = playlist.getImage();
        if(!Objects.equals(image, "")) {
            Picasso.get().load(image).into(playlistImage);
        }
        else{

        }

    }

    private void retrieveSongs() {

        Intent i = getIntent();
        Playlist playlist = i.getParcelableExtra("playlist");

        String uploadId = playlist.getUploadId();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Playlists").child(uploadId).child("melodii");;

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mUpload.clear();
                jcAudios.clear();

                for(DataSnapshot ds : snapshot.getChildren()){

                    Song songObj = ds.getValue(Song.class);

                    songObj.setmKey(ds.getKey());
                    mUpload.add(songObj);

                    String title = songObj.getTitle();
                    String stageName = songObj.getStageName();
                    String song = stageName+" - "+title;

                    jcAudios.add(JcAudio.createFromURL(song,songObj.getLink()));
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if(jcAudios != null && jcAudios.size()>0){
                    playlistSongs.setVisibility(View.GONE);
                    jcPlayerView.initPlaylist(jcAudios,null);
                } else {
                    playlistSongs.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }


    public void playSong(int adapterPosition) {
        jcPlayerView.playAudio(jcAudios.get(adapterPosition));
        jcPlayerView.setVisibility(View.VISIBLE);
        jcPlayerView.createNotification();
    }
}

