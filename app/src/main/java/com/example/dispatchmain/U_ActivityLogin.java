package com.example.dispatchmain;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

@SuppressWarnings("ALL")
public class U_ActivityLogin extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ConstraintLayout login_screen_tap;
    boolean isPermissionGranted;
    private final int GPS_REQUEST_CODE = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        checkMyPermission();

        if(isPermissionGranted == false)
        {
            checkMyPermission();
        }
        isGpsenabled();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        ImageView close = findViewById(R.id.CloseApp);
        close.setOnClickListener(view -> exit());
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null)
        {
            finish();
            return;
        }

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        Button Login = findViewById(R.id.Login);
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                authenticateUser();
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        TextView SwitchToRegister = findViewById(R.id.toRegister);
        SwitchToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToRegister();
            }
        });
        login_screen_tap = findViewById(R.id.login_tap_screen);

        login_screen_tap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                View view = getCurrentFocus();
                if (view != null)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (imm != null)
                    {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }

                return false;
            }
        });

        findViewById(R.id.fighterRegister).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switchFiremanRegister();
            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if(isPermissionGranted)
        {

        }
        else
        {
            checkMyPermission();
        }
    }

    @SuppressLint("SetTextI18n")
    private void authenticateUser()
    {

        EditText etLoginEmail = findViewById(R.id.logEmailAddress);
        EditText etLoginPassword = findViewById(R.id.logPassword);

        String email = etLoginEmail.getText().toString();
        String password = etLoginPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show();

            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful())
                    {
                        showMainActivity();
                    }
                    else
                    {
                        Toast.makeText(U_ActivityLogin.this, "Password & Email Unmatched",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showMainActivity()
    {
        Intent main = new Intent(this, U_ActivityMain.class);
        startActivity(main);
        finish();
    }

    private void switchToRegister()
    {
        Intent intent = new Intent(this, U_ActivityRegister.class);
        startActivity(intent);
        finish();
    }

    private void switchFiremanRegister()
    {
        Intent intent = new Intent(this, UF_ActivityRegister.class);
        startActivity(intent);
        finish();
    }

    private void exit(){

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Closing App").setMessage("Are you sure?")
                .setPositiveButton("Yes", ((dialogInterface, i) ->
                {
                    System.exit(0);
                }))
                .setCancelable(true)
                .show();
    }
    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void checkMyPermission()
    {

        Dexter.withContext(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse)
            {
                Toast.makeText(U_ActivityLogin.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse)
            {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), "");
                intent.setData(uri);
                startActivity(intent);
            }
            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public boolean isGpsenabled()
    {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (providerEnable)
        {
            return true;
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("GPS Permission").setMessage("GPS require, Please enable")
                    .setPositiveButton("Yes", ((dialogInterface, i) ->
                    {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);

                    })).setCancelable(false).show();
        }

        return false;
    }

    @Override
    public void onBackPressed()
    {

        System.exit(0);
        super.onBackPressed();
    }
}