package com.example.dispatchmain;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ALL")
public class U_ActivitySplash extends AppCompatActivity
{
    zLocationService mLocationService;
    Intent mServiceIntent;

    private static final int MY_FINE_LOCATION_REQUEST = 99;
    private static final int MY_BACKGROUND_LOCATION_REQUEST = 100;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();
    ConstraintLayout splashDemo;
    ImageView logo_demo, logoImage;
    TextView title, title2, tap, dispatchDescription;
    TextToSpeech textToSpeech;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    String userId;

    Boolean settingsSpeech      = false;
    Boolean backgroundService   = false;

    Boolean retrieved           = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (currentUser == null)
        {
            Intent intent = new Intent(this, U_ActivityLogin.class);
            startActivity(intent);
            finish();
            return;
        }

        events();
        setandgetSettings();
        animateText();
        dispatchDescription();

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
    }

    private void events()
    {
        userId          = auth.getCurrentUser().getUid();

        splashDemo      = findViewById(R.id.splash_tap_screen);
        logo_demo       = findViewById(R.id.Logo_demo);
        logoImage       = findViewById(R.id.logoImage);
        title           = findViewById(R.id.App_name);
        title2          = findViewById(R.id.App_name2);
        tap             = findViewById(R.id.Tap_screen);
    }

    private void setandgetSettings()
    {
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        Map<String, Object> setSettings = new HashMap<>();

        databaseReference.child("Users").child(userId).child("Settings").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                boolean setTextToSpeech;

                if (!dataSnapshot.exists())
                {
                    setSettings.put("text to speech", true);
                    setSettings.put("background service", true);
                    setSettings.put("alarm", false);
                    setSettings.put("send anonymous", false);
                    setSettings.put("online status", false);

                    databaseReference.child("Users").child(userId).child("Settings").updateChildren(setSettings);

                    setTextToSpeech = true;
                }
                else
                {
                    setTextToSpeech = dataSnapshot.child("text to speech").getValue(Boolean.class);

                }

                isTextToSpeech(setTextToSpeech);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void isTextToSpeech(boolean setTexToSpeech)
    {
        Map<String, Object> bgService = new HashMap<>();

        if(setTexToSpeech == false)
        {
            settingsSpeech = false;
        }
        else
        {
            settingsSpeech = true;
        }

        tap.clearAnimation();

        tap.setText("Tap screen to continue");

        splashDemo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(U_ActivitySplash.this);
                builder.setMessage("Choose Dispatch's Purpose?\n\nChoices:\n\t\t\t*Open Main Homepage*\n\t\t\t*Run Dispatch In The Background Only*" +
                        "\n\nWhile application running in the background, It will send notification of a Fireman nearby");
                builder.setPositiveButton("Run Background Service", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        startServiceFunc();
                        bgService.put("background service", true);

                        databaseReference.child("Users").child(userId).child("Settings").updateChildren(bgService);
                        moveTaskToBack(true);
                    }
                });
                builder.setNegativeButton("Open Dispatch Home", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(U_ActivitySplash.this, "Redirecting Dispatch", Toast.LENGTH_SHORT).show();
                        stopServiceFunc();

                        bgService.put("background service", false);
                        databaseReference.child("Users").child(userId).child("Settings").updateChildren(bgService);

                        if(currentUser != null && settingsSpeech == true)
                        {
                            String welcome = "Welcome to dispatch.";
                            String dialogSpeech = "This application sends location of fire, emergency reports";
                            textToSpeech.speak(welcome + dialogSpeech, TextToSpeech.QUEUE_FLUSH, null, null);
                        }

                        gotoHome();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void animateText()
    {
        Animation anim = new AlphaAnimation(0.5f, 1.0f);
        anim.setDuration(1500);
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        Animation zoomInAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        Animation zoomOutAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_out);

        tap.startAnimation(zoomInAnimation);
        title.startAnimation(anim);
        title2.startAnimation(anim);
        logo_demo.startAnimation(anim);
    }

    private void dispatchDescription()
    {
        dispatchDescription = findViewById(R.id.dispatchDescription);

        String text = "Dispatch: Real-time tracking and monitoring system\n" +
                "for civilian fire emergency reports and\nfirefighter fire emergency response";

        SpannableString spannableString = new SpannableString(text);
        ForegroundColorSpan dispatchColorSpan = new ForegroundColorSpan(getColor(R.color.LineDirection));
        ForegroundColorSpan civilianColorSpan = new ForegroundColorSpan(getColor(R.color.textColor));
        ForegroundColorSpan firefighterColorSpan = new ForegroundColorSpan(getColor(R.color.textColor));

        int dispatchStartIndex = text.indexOf("Dispatch:");
        int dispatchEndIndex = dispatchStartIndex + "Dispatch:".length();
        int civilianStartIndex = text.indexOf("civilian fire emergency reports");
        int civilianEndIndex = civilianStartIndex + "civilian fire emergency reports".length();
        int firefighterStartIndex = text.indexOf("firefighter fire emergency response");
        int firefighterEndIndex = firefighterStartIndex + "firefighter fire emergency response".length();

        spannableString.setSpan(dispatchColorSpan, dispatchStartIndex, dispatchEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(civilianColorSpan, civilianStartIndex, civilianEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(firefighterColorSpan, firefighterStartIndex, firefighterEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        dispatchDescription.setText(spannableString);
    }

    private void gotoHome()
    {
        Intent intent = new Intent(U_ActivitySplash.this, U_ActivityMain.class);
        Bundle b = ActivityOptions.makeSceneTransitionAnimation(U_ActivitySplash.this).toBundle();
        startActivity(intent, b);
        finish();
    }

    public void startServiceFunc()
    {
        mLocationService = new zLocationService();

        mServiceIntent = new Intent(this, mLocationService.getClass());

        if (!zUtil.isMyServiceRunning(mLocationService.getClass(), this))
        {
            startService(mServiceIntent);
        }
    }

    public void stopServiceFunc()
    {
        mLocationService = new zLocationService();

        mServiceIntent = new Intent(this, mLocationService.getClass());

        if (zUtil.isMyServiceRunning(mLocationService.getClass(), this))
        {
            stopService(mServiceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Toast.makeText(this, Integer.toString(requestCode), Toast.LENGTH_LONG).show();

        if ( requestCode == MY_FINE_LOCATION_REQUEST){

            if (grantResults.length !=0 /*grantResults.isNotEmpty()*/ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
                {
                    requestBackgroundLocationPermission();
                }

            }
            else
            {
                Toast.makeText(this, "ACCESS_FINE_LOCATION permission denied", Toast.LENGTH_LONG).show();
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                {
                    startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:com.trickyworld.locationupdates")
                    ));

                }
            }

        }else if (requestCode == MY_BACKGROUND_LOCATION_REQUEST){

            if (grantResults.length!=0 /*grantResults.isNotEmpty()*/ && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "Background location Permission Granted", Toast.LENGTH_LONG).show();
                }

            }
            else
            {
                Toast.makeText(this, "Background location permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestBackgroundLocationPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    MY_BACKGROUND_LOCATION_REQUEST);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        stopServiceFunc();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        stopServiceFunc();
    }
}