package com.example.player;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class AddSongActivity extends AppCompatActivity {

    TextView stageName, audioName, progressTv;
    EditText songName;
    Button selectAudio, uploadAudio, addAnotherAudio;
    ProgressBar progressBar;
    Spinner genMuzical;

    String stage_name;

    Uri audioUri;
    StorageReference reference;
    DatabaseReference referenceSong;
    StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        stageName = findViewById(R.id.stageNameDisplayed);
        audioName = findViewById(R.id.nameAudio);
        songName = findViewById(R.id.songName);
        selectAudio = findViewById(R.id.selectAudio);
        uploadAudio = findViewById(R.id.uploadSong);
        addAnotherAudio = findViewById(R.id.uploadAnotherSong);
        progressBar = findViewById(R.id.progressUploadedSong);
        progressTv = findViewById(R.id.progressTv);
        genMuzical = findViewById(R.id.genMuzical);

        audioName.setSelected(true);

        songName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        reference = getInstance().getReference();
        referenceSong = FirebaseDatabase.getInstance().getReference().child("Songs");

        selectAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(progressBar.getProgress() == 0) {
                    openAudioFile(101);
                } else if(progressBar.getProgress() == 100){
                    Toast.makeText(AddSongActivity.this, "Please select the button ADD ANOTHER SONG", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddSongActivity.this, "The upload is in progreess", Toast.LENGTH_SHORT).show();
                }

            }
        });

        uploadAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAudioToFirebase();
            }
        });

        addAnotherAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(progressBar.getProgress()==0){
                    Toast.makeText(AddSongActivity.this, "First upload a song", Toast.LENGTH_SHORT).show();
                    if (songName.getText().toString() == ""){
                        songName.setFocusable(true);
                    } else if (audioName.getText().toString().equals("Song not selected yet")) {
                        selectAudio.callOnClick();
                    }
                } else if(progressBar.getProgress()==100) {
                    songName.setFocusable(true);
                    songName.setText("");
                    audioName.setText("Song not selected yet");
                    progressBar.setProgress(0);
                    progressTv.setText("0%");
                    selectAudio.callOnClick();
                } else {
                    Toast.makeText(AddSongActivity.this,"Please wait for the previous song to load" , Toast.LENGTH_SHORT).show();
                }
            }
        });



        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final String uid = user.getUid();

        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    stage_name = ""+ds.child("stageName").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter.createFromResource(this, R.array.gen_muzical, android.R.layout.simple_spinner_item);
        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genMuzical.setAdapter(staticAdapter);
    }

    public void openAudioFile(int requestCode) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data.getData() != null) {
            audioUri = data.getData();
            String fileName = getFileName(audioUri);
            audioName.setText(fileName);
        }
    }

    private String getFileName(Uri uri) {

        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                }
            } finally {
                cursor.close();
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    public void uploadAudioToFirebase() {
        if (songName.getText().toString().equals("")){
            Toast.makeText(AddSongActivity.this, "Please enter the song's name", Toast.LENGTH_SHORT).show();
        }
        else if (audioName.getText().toString().equals("Song not selected yet")) {
            Toast.makeText(AddSongActivity.this, "Please select a song", Toast.LENGTH_SHORT).show();
        }
        else if (genMuzical.getSelectedItem().toString().equals("Genre")){
            Toast.makeText(this, "Please select the genre", Toast.LENGTH_SHORT).show();
        }
        else if(progressBar.getProgress() == 100) {
            Toast.makeText(AddSongActivity.this, "Please select the button ADD ANOTHER SONG", Toast.LENGTH_SHORT).show();
        }
        else {
            if(mUploadTask != null && mUploadTask.isInProgress()) {
                Toast.makeText(AddSongActivity.this, "The upload is in progress. Please wait...", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(AddSongActivity.this, "The song is uploading", Toast.LENGTH_SHORT).show();
                uploadFile(audioUri, progressBar, songName);
            }
        }
    }

    private void uploadFile(Uri audioUri, final ProgressBar progressBar, final EditText songName) {
        if (audioUri != null) {
            String durationTxt;

            progressBar.setVisibility(View.VISIBLE);

            final StorageReference storageReference = reference.child("Songs").child(System.currentTimeMillis() + "." + getFileExtension(audioUri));

            int durationInMillis = findSongDuration(audioUri);

            if(durationInMillis == 0){
                durationTxt = "NA";
            }

            durationTxt = getDurationFromMilli(durationInMillis);

            final String finalDurationTxt = durationTxt;
            mUploadTask = storageReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    final String uid = user.getUid();
                    final String uploadId = referenceSong.push().getKey();

                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("duration", finalDurationTxt);
                            hashMap.put("title", songName.getText().toString());
                            hashMap.put("link", uri.toString());
                            hashMap.put("uid", uid);
                            hashMap.put("stageName", stage_name);
                            hashMap.put("genre", genMuzical.getSelectedItem().toString());
                            hashMap.put("uploadId", uploadId);
                            hashMap.put("time", System.currentTimeMillis());

                            System.out.println("=========================="+stage_name);

                            referenceSong.child(uploadId).setValue(hashMap);

                        }
                    });
                    Toast.makeText(AddSongActivity.this, "The song has been uploaded.", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int)progress);
                    progressTv.setText((int)progress+"%");
                }
            });
        }

        else{
            Toast.makeText(AddSongActivity.this, "No file selected to upload", Toast.LENGTH_SHORT).show();
        }
    }

    private int findSongDuration(Uri audioUri) {
        int timeInMillisec = 0;

        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(AddSongActivity.this, audioUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Integer.parseInt(time);

            retriever.release();
            return timeInMillisec;
        } catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private String getDurationFromMilli(int durationInMillis) {
        Date date = new Date(durationInMillis);
        SimpleDateFormat simple = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String myTime = simple.format(date);
        return myTime;
    }

    private String getFileExtension(Uri audioUri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }
}
