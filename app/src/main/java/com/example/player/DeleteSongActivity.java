package com.example.player;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeleteSongActivity extends AppCompatActivity {

    Song song;

    ProgressBar progressBar;
    EditText search;

    static JcPlayerView jcPlayerView;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();

    RecyclerView recyclerView;
    List<Song> mUpload;
    AllSongsAdapter adapter;

    TextView allSongs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_song);

        jcPlayerView = findViewById(R.id.jcplayer);
        progressBar = findViewById(R.id.progressBarShowSongs);
        search = findViewById(R.id.searchEt);
        allSongs = findViewById(R.id.allSongsTxt);

        allSongs.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUpload = new ArrayList<>();
        adapter = new AllSongsAdapter(this,mUpload);
        recyclerView.setAdapter(adapter);

        //search/filter
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        //swipe action
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                song = adapter.getModel();
                adapter.deleteSong(viewHolder.getAdapterPosition());

            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        retrieveSongs();

    }

    private void retrieveSongs() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Songs");

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
                    allSongs.setVisibility(View.GONE);
                    jcPlayerView.initPlaylist(jcAudios,null);
                } else {
                    allSongs.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void filter(String text){
        ArrayList<Song> filteredList = new ArrayList<>();

        jcAudios.clear();

            for (Song songs : mUpload) {
                if (songs.getTitle().toLowerCase().contains(text.toLowerCase()) || songs.getStageName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(songs);

                    String title = songs.getTitle();
                    String stageName = songs.getStageName();
                    String song = stageName+" - "+title;

                    jcAudios.add(JcAudio.createFromURL(song, songs.getLink()));
                }
            }
        adapter.filterList(filteredList);
        adapter.notifyDataSetChanged();

    }

    public void playSong(int adapterPosition) {
        jcPlayerView.playAudio(jcAudios.get(adapterPosition));
        jcPlayerView.setVisibility(View.VISIBLE);
        jcPlayerView.createNotification();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
