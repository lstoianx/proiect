package com.example.player;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowingPlaylistsActivity extends AppCompatActivity {

    RecyclerView listaFollowerPlaylist;
    List<Playlist> mUpload;
    FollowingPlaylistsAdapter adapter;
    Vibrator vibrator;
    TextView noPlaylists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following_playlists);

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        noPlaylists=findViewById(R.id.noPlaylists);
        listaFollowerPlaylist = findViewById(R.id.followedPlaylistRv);

        listaFollowerPlaylist.setHasFixedSize(true);
        listaFollowerPlaylist.setLayoutManager(new GridLayoutManager(this,2));

        mUpload = new ArrayList<>();
        adapter = new FollowingPlaylistsAdapter(this,mUpload);
        listaFollowerPlaylist.setAdapter(adapter);

        retrievePlaylists();
    }


    private void retrievePlaylists() {

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Query query  = FirebaseDatabase.getInstance().getReference("Playlists");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mUpload.clear();

                for(DataSnapshot ds:snapshot.getChildren()){
                   if(ds.child("followedBy").hasChild(uid)){
                       Playlist playlistObj = ds.getValue(Playlist.class);
                       playlistObj.setmKey(ds.getKey());
                       mUpload.add(playlistObj);
                   }
               }
                if (mUpload != null && mUpload.size() > 0) {
                    noPlaylists.setVisibility(View.GONE);
                } else {
                    noPlaylists.setVisibility(View.VISIBLE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void deletePlaylist(List<Playlist> arrayListPlaylists, int adapterPosition) {

        vibrator.vibrate(100);

        Playlist playlist = arrayListPlaylists.get(adapterPosition);
        String title = playlist.getTitle();
        final String uploadId = playlist.getUploadId();

        AlertDialog.Builder builder = new AlertDialog.Builder(FollowingPlaylistsActivity.this);
        builder.setTitle("Delete Playlist");
        builder.setMessage("Are you sure you want to unfollow the playlist "+ title +"?");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // User clicked the Yes button

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = user.getUid();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Playlists").child(uploadId).child("followedBy").child(uid);
                        ref.removeValue();
                        adapter.notifyDataSetChanged();

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // User clicked the No button
                        break;
                }
            }
        };

        builder.setPositiveButton("Yes", dialogClickListener);
        builder.setNegativeButton("No",dialogClickListener);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void openPlaylistActivity(List<Playlist> arrayListPlaylists, int adapterPosition) {

        Playlist playlist = arrayListPlaylists.get(adapterPosition);
        Intent i = new Intent(FollowingPlaylistsActivity.this, FollowingSongsActivity.class);
        i.putExtra("playlist", playlist);
        startActivity(i);
    }
}
