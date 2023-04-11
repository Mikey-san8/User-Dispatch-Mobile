package com.example.dispatchmain;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SplashScreen extends AppCompatActivity
{

    ConstraintLayout splashDemo;
    ImageView logo_demo;
    TextView title, tap;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splashDemo = findViewById(R.id.splash_tap_screen);
        logo_demo = findViewById(R.id.Logo_demo);
        title = findViewById(R.id.App_name);
        tap = findViewById(R.id.Tap_screen);

        splashDemo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                Bundle b = ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this).toBundle();
                startActivity(intent, b);
                finish();
            }
        });
    }
}