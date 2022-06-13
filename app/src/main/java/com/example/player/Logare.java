package com.example.player;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class Logare extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccount, mRecoverPassw;
    Button mLoginBtn;
    SignInButton mGoogleLoginBtn;

    String first_name, last_name, image, userType;

    // Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //progress
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logare);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAccount = findViewById(R.id.nothave_accountTv);
        mLoginBtn = findViewById(R.id.loginBtn);
        mRecoverPassw = findViewById(R.id.recover_PasswordTv);
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                } else if (passw.length() == 0) {
                    mPasswordEt.setError("Enter your password");
                    mPasswordEt.setFocusable(true);
                }
                else{
                    //valid email
                    loginUser(email, passw);
                }

            }
        });

        //nu ai cont? buton
        notHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(Logare.this, Inregistrare.class));
                finish();
            }
        });


        //recuperare parola buton
        mRecoverPassw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //buton login cu google
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });


        //progres
        pd = new ProgressDialog(this);

    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        //seteaza lungimea minima a EditView pentru a incapea 16 caractere 'M'
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

    private void beginRecovery(String email) {
        pd.setMessage("Sending email...");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>(){
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(Logare.this, "Email sent", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(Logare.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(Logare.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String passw) {
        pd.setMessage("Logging In...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismiss progress
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");

                            Query query = reference.orderByChild("email").equalTo(user.getEmail());
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                                        first_name = ds.child("firstName").getValue(String.class);
                                        last_name = ""+ds.child("lastName").getValue();
                                        image = ""+ds.child("image").getValue();
                                        userType = ""+ds.child("userType").getValue();
                                    }

                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if(Objects.equals(first_name, "") || Objects.equals(last_name, "") || Objects.equals(image, "")){
                                        //show user email in Toast
                                        Intent i =  new Intent(Logare.this, CompleteProfile.class);
                                        i.putExtra("Welcome message", "Welcome " + user.getEmail());
                                        startActivity(i);
                                        finish();
                                    } else {
                                        //show user email in Toast
                                        if(Objects.equals(userType, "admin")){
                                            Intent i =  new Intent(Logare.this, AdminActivity.class);
                                            i.putExtra("Welcome", "Welcome " + user.getEmail());
                                            startActivity(i);
                                            finish();
                                        }else if(Objects.equals(userType, "basic")){
                                            Intent i =  new Intent(Logare.this, MainPage.class);
                                            i.putExtra("Welcome", "Welcome " + user.getEmail());
                                            startActivity(i);
                                            finish();
                                        }else if(Objects.equals(userType, "artist")){
                                            Intent i =  new Intent(Logare.this, ArtistActivity.class);
                                            i.putExtra("Welcome", "Welcome " + user.getEmail());
                                            startActivity(i);
                                            finish();
                                        }else if(Objects.equals(userType, "banned")){
                                            Toast.makeText(Logare.this, "Your account is banned", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        } else {
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Logare.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(Logare.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        pd.setMessage("Logging In...");
        pd.show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference reference = database.getReference("Users");

                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    final FirebaseUser user = mAuth.getCurrentUser();
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference reference = database.getReference("Users");

                                    if(!snapshot.hasChild(user.getUid())){
                                        String email = user.getEmail();
                                        String uid = user.getUid();
                                        HashMap<Object, String> hashMap = new HashMap<>();
                                        hashMap.put("email", email);
                                        hashMap.put("uid", uid);
                                        hashMap.put("firsName", "");
                                        hashMap.put("lastName", "");
                                        hashMap.put("image", "");
                                        hashMap.put("userType", "basic");

                                        reference.child(uid).setValue(hashMap);

                                        Toast.makeText(Logare.this,""+user.getEmail(), Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Logare.this, CompleteProfile.class));
                                        finish();
                                    } else{
                                        Query query = reference.orderByChild("email").equalTo(user.getEmail());
                                        query.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                                    first_name = ""+ds.child("firstName").getValue();
                                                    last_name = ""+ds.child("lastName").getValue();
                                                    image = ""+ds.child("image").getValue();
                                                    userType = ""+ds.child("userType").getValue();

                                                }
                                                if(Objects.equals(first_name, "") || Objects.equals(last_name, "") || Objects.equals(image, "")){
                                                    //show user email in Toast
                                                    Intent i =  new Intent(Logare.this, CompleteProfile.class);
                                                    i.putExtra("Welcome message", "Welcome " + user.getEmail());
                                                    startActivity(i);
                                                    finish();

                                                } else {
                                                    //show user email in Toast
                                                    if(Objects.equals(userType, "admin")){
                                                        Intent i =  new Intent(Logare.this, AdminActivity.class);
                                                        i.putExtra("Welcome", "Welcome " + user.getEmail());
                                                        startActivity(i);
                                                        finish();
                                                    }else if(Objects.equals(userType, "basic")){
                                                        Intent i =  new Intent(Logare.this, MainPage.class);
                                                        i.putExtra("Welcome", "Welcome " + user.getEmail());
                                                        startActivity(i);
                                                        finish();
                                                    }else if(Objects.equals(userType, "artist")){
                                                        Intent i =  new Intent(Logare.this, ArtistActivity.class);
                                                        i.putExtra("Welcome", "Welcome " + user.getEmail());
                                                        startActivity(i);
                                                        finish();
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        } else {
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Logare.this, "Login Failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(Logare.this, "Failed...", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
