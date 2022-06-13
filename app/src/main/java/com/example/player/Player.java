package com.example.player;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Player extends AppCompatActivity {

    List<Song> mUpload;
    ValueEventListener valueEventListener;
    MediaPlayer mediaPlayer;

    DatabaseReference databaseReference;

    ImageView playBtn, denyBtn, acceptBtn;
    TextView title, currentTime, totalTime;
    SeekBar seekBar;
    Thread t;
    boolean isPlay = false;
    int length = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playBtn = findViewById(R.id.imageView2);
        denyBtn = findViewById(R.id.imageView3);
        acceptBtn = findViewById(R.id.imageView4);
        title = findViewById(R.id.songTitle);
        seekBar = findViewById(R.id.progressBar);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        final Bundle extras = getIntent().getExtras();
        final String uid = (String)extras.get("uid");
        final String duration = (String)extras.get("duration");

        totalTime.setText(duration.substring(1));
        title.setText((String)extras.get("title"));

        denyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("Applications");

                Query query = reference.orderByChild("uid").equalTo(uid);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            ds.getRef().removeValue();
                            Toast.makeText(Player.this, "The request has been denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                onBackPressed();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("Applications");

                Query query = reference.orderByChild("uid").equalTo(uid);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds : snapshot.getChildren()){
                            ds.getRef().removeValue();
                            Toast.makeText(Player.this, "The request has been accepted", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference reference2 = database.getReference("Users");
                Query query2 = reference2.orderByChild("uid").equalTo(uid);
                query2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        final HashMap<String, Object> accountType = new HashMap<>();
                        accountType.put("userType", "artist");

                        DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                        users.updateChildren(accountType);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                onBackPressed();

            }
        });

        mUpload = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Applications");
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUpload.clear();
                for(DataSnapshot des:snapshot.getChildren()){
                    Song song = des.getValue(Song.class);
                    song.setmKey(des.getKey());
                    mUpload.add(song);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Player.this, ""+error.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });


        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlay){
                    playBtn.setImageResource(R.drawable.play_btn);
                    length = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();
                }else{
                    playBtn.setImageResource(R.drawable.pause_btn);
                    if (mediaPlayer == null) {
                        try {
                            playSong(mUpload, (int)extras.get("position"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mediaPlayer.seekTo(length);
                        mediaPlayer.start();
                    }
                }
                isPlay = !isPlay;

            }
        });
    }

    public void playSong(List<Song> arrayListSongs, int adapterPosition) throws IOException {
        Song song = arrayListSongs.get(adapterPosition);

        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(song.getLink());

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekBar.setMax(mediaPlayer.getDuration());

                String totTime = createTimerLabel(mediaPlayer.getDuration());
                totalTime.setText(totTime);

                mp.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //ce se intampla cand se termina melodia
                playBtn.setImageResource(R.drawable.play_btn);
                seekBar.setProgress(0);
                mediaPlayer.seekTo(0);
            }
        });

        //click pe progressBar, melodia se deruleaza
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //progressBar se incarca in functie de mediaPlayer
        t = new Thread(new Runnable() {

            @Override
            public void run() {
                while(mediaPlayer != null) {
                    try{
                        if(mediaPlayer.isPlaying()){
                            Message message = new Message();
                            message.what = mediaPlayer.getCurrentPosition();
                            handler.sendMessage(message);
                            Thread.sleep(1000);

                        }
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

        mediaPlayer.prepareAsync();
    }

    //creare handler pentru a seta progresul
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            currentTime.setText(createTimerLabel(msg.what));
            seekBar.setProgress(msg.what);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }

    }

    //timer pe melodie
   public String createTimerLabel(int duration){
       String timerLabel = "";
       int min = duration / 1000 / 60;
       int sec = duration / 1000 % 60;

       timerLabel += min + ":";

       if(sec<10) timerLabel+="0";
       timerLabel+=sec;

       return  timerLabel;

   }
}
