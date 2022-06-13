package com.example.player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ProfilActivity extends AppCompatActivity {

    String last_name;
    String first_name;
    String nume, prenume;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    ProgressDialog dialog;


    EditText mNumeEt, mPrenumeEt, mParolaEt, mParolaEt2;
    Button mDeleteBtn, mChangeNameBtn, mChangePassBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        mNumeEt = findViewById(R.id.last_nameEt);
        mPrenumeEt = findViewById(R.id.first_nameEt);
        mParolaEt = findViewById(R.id.change_passwordEt);
        mParolaEt2 = findViewById(R.id.change_passwordEt2);
        mDeleteBtn = findViewById(R.id.deactivateAccount);
        mChangeNameBtn = findViewById(R.id.change_nameBtn);
        mChangePassBtn = findViewById(R.id.change_passwordBtn);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

        dialog = new ProgressDialog(getBaseContext());

        final String Uid = user.getUid();


        //select nume si prenume din baza de date + afisare in editTexturi
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    last_name = ""+ds.child("lastName").getValue();
                    first_name = ""+ ds.child("firstName").getValue();

                    mNumeEt.setText(last_name);
                    mPrenumeEt.setText(first_name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //schimbare nume
        mChangeNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nume = mNumeEt.getText().toString();
                String prenume = mPrenumeEt.getText().toString();

                if(Objects.equals(nume, "") || Objects.equals(prenume, "")){
                    Toast.makeText(getBaseContext(), "Please enter your name correctly", Toast.LENGTH_SHORT).show();
                }
                else if(Objects.equals(nume, last_name) && Objects.equals(prenume, first_name)){
                    Toast.makeText(getBaseContext(), "You did not change the name", Toast.LENGTH_SHORT).show();
                }
                else{
                    changeName();
                }
            }
        });

        mChangePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String parola1 = mParolaEt.getText().toString();
                String parola2 = mParolaEt2.getText().toString();

                if(Objects.equals(parola1, "") || Objects.equals(parola2, "")){
                    Toast.makeText(getBaseContext(), "Please enter your password correctly", Toast.LENGTH_SHORT).show();
                }
                else if(!Objects.equals(parola1, "") || !Objects.equals(parola2, "")){
                    if(!Objects.equals(parola1, parola2)){
                        Toast.makeText(getBaseContext(), "Your passwords does not match", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (parola1.length() < 6){
                            Toast.makeText(getBaseContext(), "Password length must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            changePassword();
                        }
                    }
                }
            }
        });


        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user != null) {
                    deleteUser(Uid);
                    dialog.setMessage("Account is deleting");
                    dialog.show();
                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getBaseContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getBaseContext(), MainActivity.class));
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getBaseContext(), "Some error occurred! Account could not be deleted", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }

    public void changePassword() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            dialog.setMessage("Changing password, please wait...");
            dialog.show();
            user.updatePassword(mParolaEt.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>(){
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        dialog.dismiss();
                        Toast.makeText(getBaseContext(), "The password changed successfully", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        startActivity(new Intent(getBaseContext(), Logare.class));
                    }
                    else {
                        dialog.dismiss();
                        Toast.makeText(getBaseContext(), "The password did not changed", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }

    public void changeName(){

        nume = mNumeEt.getText().toString();
        prenume = mPrenumeEt.getText().toString();

        DatabaseReference mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        HashMap<String,Object> name = new HashMap<>();
        name.put("firstName", prenume);
        name.put("lastName", nume);
        mDatabase.updateChildren(name);
        Toast.makeText(getBaseContext(), "Name updated", Toast.LENGTH_SHORT).show();
    }

    public void deleteUser(String uid){

        DatabaseReference drUser = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        drUser.removeValue();

    }
}
