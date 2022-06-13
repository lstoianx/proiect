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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
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

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ArtistActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {

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

    TextView ownSongs, name;
    String stageName;

    ProgressBar progressBar;
    EditText search;

    JcPlayerView jcPlayerView;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();

    RecyclerView recyclerView;
    List<Song> mUpload;
    OwnSongsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        pd = new ProgressDialog(this);

        jcPlayerView = findViewById(R.id.jcplayer);
        progressBar = findViewById(R.id.progressBarOwnSongs);
        search = findViewById(R.id.searchOwnSong);
        recyclerView = findViewById(R.id.ownSongsRv);
        ownSongs = findViewById(R.id.ownSongsTxt);
        name = findViewById(R.id.ownNameTv);
        ownSongs.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUpload = new ArrayList<>();
        adapter = new OwnSongsAdapter(this,mUpload);
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

        Toast toast = new Toast(getBaseContext());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            toast.makeText(getBaseContext(), extras.getString("Welcome"), Toast.LENGTH_SHORT).show();
        }
        else {
        }

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        String uid = user.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

        Query query2 = databaseReference.orderByChild("uid").equalTo(uid);
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    stageName = ds.child("stageName").getValue().toString();
                    name.setText(stageName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_artist);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerLayout = navigationView.getHeaderView(0);
        profile_photo = headerLayout.findViewById(R.id.profile_photo);
        profile_name = headerLayout.findViewById(R.id.profile_name);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
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
    }

    private void retrieveSongs() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Query query = FirebaseDatabase.getInstance().getReference("Songs").orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
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
                    ownSongs.setVisibility(View.GONE);
                    jcPlayerView.initPlaylist(jcAudios,null);
                } else {
                    ownSongs.setVisibility(View.VISIBLE);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.nav_add_songs:
                startActivity(new Intent(ArtistActivity.this, AddSongActivity.class));
                break;

            case R.id.nav_profile:
                startActivity(new Intent(ArtistActivity.this, ProfilActivity.class));
                break;

            case R.id.nav_logout:
                firebaseAuth.getInstance().signOut();
                if(jcPlayerView.isPlaying()){
                    jcPlayerView.kill();
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
            Intent i = new Intent(ArtistActivity.this, MainActivity.class);
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
            jcPlayerView.kill();
            super.onBackPressed();
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
                                    Toast.makeText(ArtistActivity.this,"Image updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(ArtistActivity.this,"Error updating image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    pd.dismiss();
                    Toast.makeText(ArtistActivity.this, "Some error occured" , Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(ArtistActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
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
