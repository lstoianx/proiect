package com.example.player;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class PlaylistActivity extends AppCompatActivity {

    Vibrator vibrator;

    ProgressDialog pd;

    Song song;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    String storagePath = "Playlist_Photo/";
    Uri image_uri;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];

    Playlist playlist;

    TextView playlistTitle, playlistSongs, code, songs, followers;
    ImageView playlistImage;

    RecyclerView playlistRv;
    static JcPlayerView jcPlayerView;
    List<Song> mUpload;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    PlaylistSongsAdapter adapter;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        retrieveSongs();

        mUpload = new ArrayList<>();
        pd = new ProgressDialog(this);
        pd.setMessage("Loading, please wait...");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Playlists");
        storageReference = getInstance().getReference();

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        songs = findViewById(R.id.songsNumber);
        followers = findViewById(R.id.followersNumber);
        code = findViewById(R.id.codePlaylist);
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
        adapter = new PlaylistSongsAdapter(this,mUpload);
        playlistRv.setAdapter(adapter);

        Intent i = getIntent();
        playlist = i.getParcelableExtra("playlist");

        //set title
        playlistTitle.setText(playlist.getTitle());
        //set unique code
        code.setText(playlist.getCode());
        code.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("code", playlist.getCode());
                code.setSelected(true);
                vibrator.vibrate(50);
                Toast.makeText(PlaylistActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                clipboard.setPrimaryClip(clip);
                return false;
            }
        });
        //set image
        String image = playlist.getImage();
        if(!Objects.equals(image, "")) {
            Picasso.get().load(image).into(playlistImage);
        }
        else{
        }
        //set number of followers
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Playlists").child(playlist.getUploadId()).child("followedBy");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                followers.setText("Followers: "+count);
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
                songs.setText("Songs: "+count);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        playlistImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });

        //swipe pe delete din playlist
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                Song song = mUpload.get(viewHolder.getAdapterPosition());
                final String test = song.getUploadId();

                DatabaseReference drUser = FirebaseDatabase.getInstance().getReference("Playlists").child(playlist.getUploadId()).child("melodii").child(test);
                drUser.removeValue();

                // arrayListSongs.remove(adapterPosition);
                // notifyItemRemoved(adapterPosition);

                Toast.makeText(PlaylistActivity.this, "The song has been deleted", Toast.LENGTH_SHORT).show();



                //adapter.notifyItemRemoved(viewHolder.getAdapterPosition());

            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(playlistRv);

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


    //poza playlist
    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void showImagePicDialog() {

        String options[] = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }

                else if (which == 1){
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else{
                        pickFromGallery();
                    }
                }
            }
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length >0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this,"Please enable camera and storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length >0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this,"Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();

                uploadPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){

                uploadPhoto(image_uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadPhoto(Uri uri) {

        pd.show();

        String filePathAndName = storagePath+ ""+ "image" +"_"+playlist.getUploadId();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful()){
                    HashMap<String, Object> results = new HashMap<>();
                    results.put("image",downloadUri.toString());

                    databaseReference.child(playlist.getUploadId()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(PlaylistActivity.this,"Image updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(PlaylistActivity.this,"Error updating image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    pd.dismiss();
                    Toast.makeText(PlaylistActivity.this, "Some error occured" , Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PlaylistActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickFromCamera() {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");

        image_uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

    }

    public void playSong(int adapterPosition) {
        jcPlayerView.playAudio(jcAudios.get(adapterPosition));
        jcPlayerView.setVisibility(View.VISIBLE);
        jcPlayerView.createNotification();
    }

    public void shareSong(int adapterPosition) {
        vibrator.vibrate(50);
        Song song = mUpload.get(adapterPosition);
        String link = song.getLink();
        String stageName = song.getStageName();
        String title = song.getTitle();

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Write your subject here");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "Listen to the song " +title+" by the artist "+stageName+"           "+link);
        startActivity(Intent.createChooser(sharingIntent, "Share text via"));
    }
}
