package com.example.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class FoundPlaylist extends AppCompatActivity {

    Playlist playlist;

    ImageView playlistPhoto;
    TextView title, creator, playlistEmpty, numberSongs;
    Button followBtn;
    RecyclerView followedRv;
    ProgressBar progressBar;
    static JcPlayerView jcplayer;
    String userName;

    List<Song> mUpload;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    SearchPlaylistAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_playlist);

        numberSongs = findViewById(R.id.numberSongs);
        playlistPhoto = findViewById(R.id.playlistPhoto);
        title = findViewById(R.id.playlistName);
        creator = findViewById(R.id.playlistCreator);
        followBtn = findViewById(R.id.followBtn);
        followedRv = findViewById(R.id.followedRv);
        progressBar = findViewById(R.id.progressBarFollowed);
        jcplayer = findViewById(R.id.jcplayer);
        playlistEmpty = findViewById(R.id.playlistEmpty);

        playlistEmpty.setVisibility(View.GONE);

        followedRv.setHasFixedSize(true);
        followedRv.setLayoutManager(new LinearLayoutManager(this));

        mUpload = new ArrayList<>();
        adapter = new SearchPlaylistAdapter(this,mUpload);
        followedRv.setAdapter(adapter);

        Intent i = getIntent();
        playlist = i.getParcelableExtra("playlist");

        //set songs
        retrieveSongs();
        //set image
        String image = playlist.getImage();
        if(!Objects.equals(image, "")) {
            Picasso.get().load(image).into(playlistPhoto);
        }
        //set title
        title.setText(playlist.getTitle());
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
                        creator.setText("Created by: "+userName);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //set number of songs
        DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Playlists").child(playlist.getUploadId()).child("melodii");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                numberSongs.setText("Songs: "+count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Playlists").child(playlist.getUploadId()).child("followedBy");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = user.getUid();
                        if (snapshot.hasChild(uid)) {
                            Toast.makeText(FoundPlaylist.this, "You already following the playlist", Toast.LENGTH_SHORT).show();
                        } else {
                            ref.child(uid).setValue(uid);
                            Toast.makeText(FoundPlaylist.this, "Success", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

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
                    playlistEmpty.setVisibility(View.GONE);
                    jcplayer.initPlaylist(jcAudios,null);
                } else {
                    playlistEmpty.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    public void playSong(int adapterPosition) {
        jcplayer.playAudio(jcAudios.get(adapterPosition));
        jcplayer.setVisibility(View.VISIBLE);
        jcplayer.createNotification();
    }
}
