package com.example.player;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {


    //views
    Button mRegisterBtn, mLoginBtn;
    TextView mText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRegisterBtn = findViewById((R.id.register_btn));
        mLoginBtn = findViewById((R.id.login_btn));
       /* mText = findViewById(R.id.textMesaj);*/

        //handle register button click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start Register Activity
                startActivity(new Intent(MainActivity.this, Inregistrare.class));
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start Login Activity
                startActivity(new Intent(MainActivity.this, Logare.class));

            }
        });

        //pulsing animation
       /* ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(mText, PropertyValuesHolder.ofFloat("scaleX", 1.2f), PropertyValuesHolder.ofFloat("scaleY", 1.2f));
        scaleDown.setDuration(300);
        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.start();*/

    }

    @Override
    public void onBackPressed() {
        System.exit(1);

    }
}
