package com.example.player;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MyPlaylistsActivity extends AppCompatActivity {

    Button adauga, followed, search;

    RecyclerView listaPlaylist;
    List<Playlist> mUpload;
    PlaylistAdapter adapter;
    Vibrator vibrator;
    TextView noPlaylists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_playlists);

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        noPlaylists = findViewById(R.id.noPlaylists);
        adauga = findViewById(R.id.makePlaylist);
        followed = findViewById(R.id.followedPlaylists);
        search = findViewById(R.id.searchPlaylist);
        listaPlaylist = findViewById(R.id.myPlaylistRv);
        listaPlaylist.setHasFixedSize(true);
        listaPlaylist.setLayoutManager(new GridLayoutManager(this,2));

        mUpload = new ArrayList<>();
        adapter = new PlaylistAdapter(this,mUpload);
        listaPlaylist.setAdapter(adapter);

        retrievePlaylists();

        //creeaza playlist
        adauga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPlaylistsActivity.this);
                builder.setTitle("Create Playlist");
                final EditText input = new EditText(MyPlaylistsActivity.this);
                input.setHint("Playlist Name");
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
                builder.setView(input);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case DialogInterface.BUTTON_POSITIVE:
                                // User clicked the Yes button

                                String title = input.getText().toString();
                                if(Objects.equals(title, "")){
                                    Toast.makeText(MyPlaylistsActivity.this, "Please enter a name for the playlist", Toast.LENGTH_SHORT).show();
                                } else {

                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String uid = user.getUid();

                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference reference = database.getReference("Playlists");

                                    String uploadId = reference.push().getKey();

                                    HashMap<Object, String> hashMap = new HashMap<>();
                                    hashMap.put("uploadId", uploadId);
                                    hashMap.put("uid", uid);
                                    hashMap.put("title", title);
                                    hashMap.put("image", "");
                                    hashMap.put("code", generateCode(25));

                                    reference.child(uploadId).setValue(hashMap);
                                }

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
        });

        //cauta playlist
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPlaylistsActivity.this);
                builder.setTitle("Search Playlist");
                final EditText input = new EditText(MyPlaylistsActivity.this);
                input.setHint("Playlist Code");
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
                builder.setView(input);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case DialogInterface.BUTTON_POSITIVE:
                                // User clicked the Yes button

                                FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
                                final String userUid = user.getUid();

                                final String text = input.getText().toString();
                                if(Objects.equals(text, "")){
                                    Toast.makeText(MyPlaylistsActivity.this, "Please enter a code", Toast.LENGTH_SHORT).show();
                                } else if (text.length() != 25){
                                    Toast.makeText(MyPlaylistsActivity.this, "Please enter a correct code", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Query query = FirebaseDatabase.getInstance().getReference("Playlists");
                                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            boolean playlistYours=false;
                                            boolean playlistFound = false;
                                            for(DataSnapshot ds : snapshot.getChildren()){
                                                Playlist playlist = ds.getValue(Playlist.class);
                                                if(playlist.getCode().equals(text)){
                                                    if(playlist.getUid().equals(userUid)){
                                                        Toast.makeText(MyPlaylistsActivity.this, "The playlist is yours", Toast.LENGTH_SHORT).show();
                                                        playlistYours = true;
                                                        break;
                                                    } else{
                                                        Intent i = new Intent(MyPlaylistsActivity.this, FoundPlaylist.class);
                                                        i.putExtra("playlist", playlist);
                                                        startActivity(i);
                                                        playlistFound = true;
                                                        break;
                                                    }
                                                } else {
                                                }
                                            }

                                            if(!playlistFound && !playlistYours){
                                                Toast.makeText(MyPlaylistsActivity.this, "The playlist does not exist", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }

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
        });

        //mergi la activitatea de playlisturi pe care le urmaresti
        followed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyPlaylistsActivity.this, FollowingPlaylistsActivity.class));
            }
        });

    }

    //unique code for each playlist
    private String generateCode(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@%^&*".toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for(int i=0; i<length; i++){
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    private void retrievePlaylists() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Query query  = FirebaseDatabase.getInstance().getReference("Playlists").orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mUpload.clear();

                for(DataSnapshot ds : snapshot.getChildren()){

                    Playlist playlistObj = ds.getValue(Playlist.class);

                    playlistObj.setmKey(ds.getKey());
                    mUpload.add(playlistObj);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(MyPlaylistsActivity.this);
        builder.setTitle("Delete Playlist");
        builder.setMessage("Are you sure you want to delete the playlist "+ title +"?");

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // User clicked the Yes button

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Playlists").child(uploadId);
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
        Intent i = new Intent(MyPlaylistsActivity.this, PlaylistActivity.class);
        i.putExtra("playlist", playlist);
        startActivity(i);

    }
}
