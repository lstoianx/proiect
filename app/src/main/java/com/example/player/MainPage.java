package com.example.player;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class MainPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;

    ProgressDialog pd;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    String storagePath = "Users_Profile_Imgs/";

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String cameraPermissions[];
    String storagePermissions[];

    String profilePhoto = "image";

    Uri image_uri;

    ImageView profile_photo;
    TextView profile_name;

    Song song;

    TextView songsAdded;

    ProgressBar progressBar;
    EditText search;

    JcPlayerView jcPlayerView;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();

    RecyclerView recyclerView;
    List<Song> mUpload;
    UserSongsAdapter adapter;

    List<String> playlistTitle;
    String playlistId;

    Spinner selectGenre;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        selectGenre = findViewById(R.id.searchGenre);
        jcPlayerView = findViewById(R.id.jcplayer);
        progressBar = findViewById(R.id.loadSongs);
        search = findViewById(R.id.searchSong);
        recyclerView = findViewById(R.id.songsRv);
        songsAdded = findViewById(R.id.songsTxt);
        songsAdded.setVisibility(View.GONE);

        pd = new ProgressDialog(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        playlistTitle = new ArrayList<>();

        mUpload = new ArrayList<>();

        adapter = new UserSongsAdapter(this,mUpload);
        recyclerView.setAdapter(adapter);

        //get current user's playlists
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Query query = database.getReference("Playlists").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                playlistTitle.clear();

                for (DataSnapshot ds : snapshot.getChildren()){
                    String title = ""+ds.child("title").getValue().toString();

                    playlistTitle.add(title);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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

        retrieveSongs();

        Toast toast = new Toast(getBaseContext());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            toast.makeText(getBaseContext(), extras.getString("Welcome"), Toast.LENGTH_SHORT).show();
        }
        else {
        }


        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        profile_photo = headerLayout.findViewById(R.id.profile_photo);
        profile_name = headerLayout.findViewById(R.id.profile_name);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Query query2 = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String image = ""+ds.child("image").getValue();
                    String name = ""+ds.child("lastName").getValue() +" "+ ds.child("firstName").getValue();

                    profile_name.setText(name);

                    try{
                        Picasso.get().load(image).into(profile_photo);
                    }
                    catch(Exception e){
                        Picasso.get().load(R.drawable.ic_face_grey).into(profile_photo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profile_photo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });


        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter.createFromResource(this, R.array.filter_genre, android.R.layout.simple_spinner_item);
        staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectGenre.setAdapter(staticAdapter);

        selectGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                String text = selectGenre.getSelectedItem().toString();
              //  jcAudios.clear();

                if (Objects.equals(text, "All genres")) {

                    jcAudios.clear();
                    ArrayList<Song> filteredList;
                    filteredList = (ArrayList<Song>) mUpload;
                    for (Song songs : filteredList) {

                        String title = songs.getTitle();
                        String stageName = songs.getStageName();
                        String song = stageName+" - "+title;

                        jcAudios.add(JcAudio.createFromURL(song, songs.getLink()));
                    }
                    adapter.filterList(filteredList);
                    adapter.notifyDataSetChanged();
                    
                } else {
                    

                    ArrayList<Song> filteredList = new ArrayList<>();

                    jcAudios.clear();

                    for (Song songs : mUpload) {
                        if (songs.getGenre().contains(text)) {
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void selectPlaylist(List<Song> arrayListSongs, int position) {

        final Song song = arrayListSongs.get(position);
        final String idMelodie = song.getUploadId();

        AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
        builder.setTitle("Add to playlist");

        final Spinner playlist = new Spinner(MainPage.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        playlist.setLayoutParams(lp);
        builder.setView(playlist);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(MainPage.this, android.R.layout.simple_spinner_item, playlistTitle);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playlist.setAdapter(spinnerArrayAdapter);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:

                        String playlistTitle = playlist.getSelectedItem().toString();

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        Query query = database.getReference("Playlists").orderByChild("title").equalTo(playlistTitle);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    Playlist playlistObj = ds.getValue(Playlist.class);
                                    playlistId = playlistObj.getUploadId();
                                }

                                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Playlists").child(playlistId).child("melodii");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild(idMelodie)) {
                                            Toast.makeText(MainPage.this, "The song is already in your playlist", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainPage.this, "Song added succesfully", Toast.LENGTH_SHORT).show();
                                            ref.child(idMelodie).setValue(song);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

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
            /*    Collections.sort(mUpload, new Comparator<Song>() {
                    @Override
                    public int compare(Song o1, Song o2) {
                        if(o1.getTime() > o2.getTime()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });*/

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                if(jcAudios != null && jcAudios.size()>0){
                    songsAdded.setVisibility(View.GONE);
                    jcPlayerView.initPlaylist(jcAudios,null);
                } else {
                    songsAdded.setVisibility(View.VISIBLE);
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
        jcPlayerView.createNotification();
        jcPlayerView.setVisibility(View.VISIBLE);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_playlists:
                startActivity(new Intent(MainPage.this, MyPlaylistsActivity.class));
                break;

            case R.id.nav_become_artist:
                startActivity(new Intent(MainPage.this, BecomeArtistActivity.class));
                break;

            case R.id.nav_profile:
                startActivity(new Intent(MainPage.this, ProfilActivity.class));
                break;

            case R.id.nav_logout:
                firebaseAuth.getInstance().signOut();
                if(jcPlayerView.isPlaying()){
                    jcPlayerView.kill();
                }
                if(PlaylistActivity.jcPlayerView!=null && PlaylistActivity.jcPlayerView.isPlaying()){
                    PlaylistActivity.jcPlayerView.kill();
                }

                if(FoundPlaylist.jcplayer!=null && FoundPlaylist.jcplayer.isPlaying()){
                    FoundPlaylist.jcplayer.kill();
                }

                if(FollowingSongsActivity.jcPlayerView!=null && FollowingSongsActivity.jcPlayerView.isPlaying()){
                    FollowingSongsActivity.jcPlayerView.kill();
                }
                checkUserStatus();
                break;

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
        } else {
            Intent i = new Intent(MainPage.this, MainActivity.class);
            // set the new task and clear flags
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            
            Toast.makeText(this, "If you want to log out, press the logout button on the menu", Toast.LENGTH_SHORT).show();
        }
    }

//CAMERA SI UPDATE LA POZA DE LA PROFIL
    //IDENTIC CA IN COMPLETE PROFILE


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

        pd.setMessage("Loading");
        pd.show();

        String filePathAndName = storagePath+ ""+ profilePhoto +"_"+user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful()){
                    HashMap<String, Object> results = new HashMap<>();
                    results.put(profilePhoto,downloadUri.toString());

                    databaseReference.child(user.getUid()).updateChildren(results)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(MainPage.this,"Image updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(MainPage.this,"Error updating image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    pd.dismiss();
                    Toast.makeText(MainPage.this, "Some error occured" , Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(MainPage.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
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
}
