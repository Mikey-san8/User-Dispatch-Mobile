package com.example.dispatchmain;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class ActivitySplash extends AppCompatActivity
{
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();
    ConstraintLayout splashDemo;
    ImageView logo_demo;
    TextView title, tap;
    TextToSpeech textToSpeech;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashDemo = findViewById(R.id.splash_tap_screen);
        logo_demo = findViewById(R.id.Logo_demo);
        title = findViewById(R.id.App_name);
        tap = findViewById(R.id.Tap_screen);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if(status != TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        splashDemo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySplash.this);
                builder.setMessage("Choose Dispatch's Purpose?\n\nChoices:\n\t\t\t*Open Main Homepage*\n\t\t\t*Run Dispatch In The Background Only*" +
                        "\n\nWhile application running in the background, It will send notification of a Fireman nearby");
                builder.setPositiveButton("Run Background Service", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                builder.setNegativeButton("Open Dispatch Home", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(ActivitySplash.this, "Redirecting Dispatch", Toast.LENGTH_SHORT).show();
                        gotoHome();

                        if(currentUser != null)
                        {
                            String welcome = "Welcome to dispatch.";
                            String dialogSpeech = "This application sends location of fire, emergency reports";
                            textToSpeech.speak(welcome + dialogSpeech, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void gotoHome()
    {
        Intent intent = new Intent(ActivitySplash.this, ActivityMain.class);
        Bundle b = ActivityOptions.makeSceneTransitionAnimation(ActivitySplash.this).toBundle();
        startActivity(intent, b);
        finish();
    }

    private void gotoBackground()
    {

    }
}