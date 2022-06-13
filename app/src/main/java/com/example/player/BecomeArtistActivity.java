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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class BecomeArtistActivity extends AppCompatActivity {

    EditText stageName, title;
    Button upload, send;
    TextView name, progresTv;
    ProgressBar pb;

    Uri audioUri;
    StorageReference reference;
    DatabaseReference referenceSong;
    StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_become_artist);

        stageName = findViewById(R.id.stageNameEt);
        title = findViewById(R.id.songTitleEt);
        upload = findViewById(R.id.uploadSongBtn);
        name = findViewById(R.id.nameSongTv);
        pb = findViewById(R.id.loading);
        progresTv = findViewById(R.id.progressTv);

        name.setSelected(true);

        send = findViewById(R.id.uploadBtn);

        stageName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        title.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        reference = getInstance().getReference();
        referenceSong = FirebaseDatabase.getInstance().getReference().child("Applications");

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAudioFile(101);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAudioToFirebase();
            }
        });
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
            name.setText(fileName);
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
        if (stageName.getText().toString().equals("")){
            Toast.makeText(BecomeArtistActivity.this, "Please enter your stage name", Toast.LENGTH_SHORT).show();
        }
        else if (name.getText().toString().equals("Song not selected yet")) {
            Toast.makeText(BecomeArtistActivity.this, "Please select a song", Toast.LENGTH_SHORT).show();
        }
        else if (title.getText().toString().equals("")) {
            Toast.makeText(BecomeArtistActivity.this, "Please enter the title of the song",Toast.LENGTH_SHORT).show();
        }
        else {

            //verific daca exista copilul cu id-ul userului curent in tabela applications
            //userul nu poate sa foloseaca functionalitatea de mai multe ori
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            final String uid = user.getUid();

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("Applications");
            Query query = rootRef.orderByChild("uid").equalTo(uid);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(BecomeArtistActivity.this, "You have already requested to be an artist",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(mUploadTask != null && mUploadTask.isInProgress()) {
                            Toast.makeText(BecomeArtistActivity.this, "The upload is in progress. Please wait...", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(BecomeArtistActivity.this, "The song is uploading", Toast.LENGTH_SHORT).show();
                            uploadFile(audioUri, pb, title);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }

    private void uploadFile(final Uri uri, final ProgressBar progressBar, final EditText titlee) {
        if (uri != null) {
            String durationTxt;

            progressBar.setVisibility(View.VISIBLE);

            final StorageReference storageReference = reference.child("Applications").child(System.currentTimeMillis() + "." + getFileExtension(uri));

            int durationInMillis = findSongDuration(uri);

            if(durationInMillis == 0){
                durationTxt = "NA";
            }

            durationTxt = getDurationFromMilli(durationInMillis);

            final String finalDurationTxt = durationTxt;
            mUploadTask = storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                            hashMap.put("title", titlee.getText().toString());
                            hashMap.put("link", uri.toString());
                            hashMap.put("uid", uid);
                            hashMap.put("stageName", stageName.getText().toString());

                            final HashMap<String, Object> stage_Name = new HashMap<>();
                            stage_Name.put("stageName", stageName.getText().toString());

                            DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            referenceSong.child(uploadId).setValue(hashMap);

                            users.updateChildren(stage_Name);
                        }
                    });
                        Toast.makeText(BecomeArtistActivity.this, "The song has been uploaded.", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int)progress);
                    progresTv.setText((int)progress+"%");
                }
            });

        }

        else{
            Toast.makeText(BecomeArtistActivity.this, "No file selected to upload", Toast.LENGTH_SHORT).show();
        }
    }

    private String getDurationFromMilli(int durationInMillis) {
        Date date = new Date(durationInMillis);
        SimpleDateFormat simple = new SimpleDateFormat("mm:ss", Locale.getDefault());
        String myTime = simple.format(date);
        return myTime;
    }

    private int findSongDuration(Uri audioUri) {
        int timeInMillisec = 0;

        try{
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(BecomeArtistActivity.this, audioUri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Integer.parseInt(time);

            retriever.release();
            return timeInMillisec;
        } catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    private String getFileExtension(Uri audioUri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }
}
