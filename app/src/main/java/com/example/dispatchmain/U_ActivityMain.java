package com.example.dispatchmain;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Locale;

@SuppressWarnings("ALL")
public class U_ActivityMain extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    FragmentHome mainFragment = new FragmentHome();
    FragmentRecord recordFragment = new FragmentRecord();
    FragmentMap mapFragment = new FragmentMap();
    FragmentChat chatFragment = new FragmentChat();
    FragmentEditProfile editFragment = new FragmentEditProfile();
    FragmentSettings settingsFragment = new FragmentSettings();

    BottomNavigationView bottomNavigationView;

    public View headerView;

    private DrawerLayout drawerLayout;
    public NavigationView navigationView;

    public TextView drawerN,
                    drawerE;

    private boolean doubleBackToExitPressedOnce = false;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    private FusedLocationProviderClient fusedLocationClient;

    zCalculations calculate = new zCalculations(this);

    TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bottomNavigation();
        mainEvents();
//        getNearby();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    public void mainEvents()
    {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        headerView = navigationView.getHeaderView(0);

        drawerN = headerView.findViewById(R.id.drawerName);
        drawerE = headerView.findViewById(R.id.drawerEmail);

        navigationView.setCheckedItem(R.id.nav_home);
        navigationView.setNavigationItemSelectedListener(this);
    }

    Boolean getAlarm;

    public void getNearby()
    {
        String userId = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot users:snapshot.child("Users").getChildren())
                {
                    String getKey = users.getKey();

                    if(userId.equals(getKey))
                    {
                        getAlarm = users.child("Settings").child("alarm").getValue(Boolean.class);
                    }
                }

                fusedLocationClient.getLastLocation().addOnCompleteListener(task ->
                {
                    if(task.isSuccessful())
                    {
                        Location location = task.getResult();

                        for (DataSnapshot Snapshot : snapshot.child("Firefighter").getChildren())
                        {
                            if (Snapshot.hasChild("Settings"))
                            {
                                Boolean onlineStatus = Snapshot.child("Settings").child("online status").getValue(Boolean.class);

                                if (onlineStatus == true)
                                {
                                    Double lat      = Snapshot.child("lat").getValue(Double.class);
                                    Double lng      = Snapshot.child("lng").getValue(Double.class);
                                    String userName   = Snapshot.child("userId").getValue(String.class);
                                    String updatedUserName = null;

                                    if (userName != null && userName.length() > 20)
                                    {
                                        updatedUserName = "fighter." + userName.substring(0, userName.length() - 20);
                                    }

                                    LatLng fighterLocation = new LatLng(lat, lng);
                                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                    double getDistance = calculate.calculateDistance(fighterLocation, userLocation) / 1000;

                                    DecimalFormat decimalFormat = new DecimalFormat("#.##");

                                    String formattedDistance = decimalFormat.format(getDistance);

                                    String getName  = null;

                                    if (calculate.calculateDistance(fighterLocation, userLocation) < 301
                                            && calculate.calculateDistance(fighterLocation, userLocation) >= 289)
                                    {
                                        Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

                                        if(getAlarm == true)
                                        {
//                                            AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
//                                            builder.setMessage(updatedUserName+" is nearby\nDistance: "+formattedDistance+"km");
//                                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
//                                            {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which)
//                                                {
//
//                                                }
//                                            });
//                                            AlertDialog dialog = builder.create();
//                                            dialog.show();

                                            String speech = "A firefighter is nearby! Distance is " + formattedDistance;
                                            textToSpeech.speak(speech, TextToSpeech.QUEUE_ADD, null);
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        };

        databaseReference.addValueEventListener(valueEventListener);
    }

    public void bottomNavigation()
    {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, editFragment, "edit_fragment")
                .addToBackStack(null)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mainFragment, "home_fragment")
                .addToBackStack(null)
                .commit();

        bottomNavigationView.setSelectedItemId(R.id.btmHome);
    }

    public void clickMap()
    {
        bottomNavigationView.setSelectedItemId(R.id.btmMap);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

        switch (item.getItemId())
        {
            case R.id.btmHome:
                transaction.replace(R.id.container, mainFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                return true;

            case R.id.btmMap:
                transaction.replace(R.id.container, mapFragment, "map_fragment");
                transaction.addToBackStack(null);
                transaction.commit();
                return true;

            case R.id.btmChat:
                transaction.replace(R.id.container, chatFragment, "chat_fragment");
                transaction.addToBackStack(null);
                transaction.commit();
                return true;

            case R.id.btmRecord:
                transaction.replace(R.id.container, recordFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                return true;

            case R.id.btmMenu:
                drawerLayout.openDrawer(navigationView, true);
                userDetails();
                return true;

            case R.id.nav_logout:
                logoutUser();
                return true;
            case R.id.nav_fighter:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage
                        ("This is a BETA Application therefore:" +
                        "\nEmail: dispatchofficial@gmail.com." +
                        "\nProvide verification:" +
                        "\n\t- An identification (ID) of a fireman will do." +
                        "\n\t- A selfie beside with the provided ID." +
                        "\nAdmin will send the APK.");

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                       Toast.makeText(U_ActivityMain.this, "Thank you for your service!", Toast.LENGTH_LONG).show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            case R.id.nav_settings:

                if(drawerLayout.isDrawerOpen(navigationView))
                {
                    drawerLayout.closeDrawer(navigationView, false);
                }

                transaction
                        .replace(R.id.container, settingsFragment, "settings_fragment")
                        .addToBackStack(null)
                        .commit();

                return true;
        }

        return false;
    }

    private void logoutUser()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(U_ActivityMain.this, U_ActivityLogin.class);
                startActivity(intent);
                try {
                    finalize();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void userDetails()
    {
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        String userId = auth.getCurrentUser().getUid();

        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                String firstName =  snapshot.child("Users").child(userId).child("firstName").getValue(String.class);
                String lastName = snapshot.child("Users").child(userId).child("lastName").getValue(String.class);
                String userName = snapshot.child("Users").child(userId).child("userName").getValue(String.class);
                String email =  snapshot.child("Users").child(userId).child("email").getValue(String.class);
                String phone = snapshot.child("Users").child(userId).child("phone").getValue(String.class);
                String address = snapshot.child("Users").child(userId).child("address").getValue(String.class);

                drawerN.setText(firstName + " " + lastName);
                drawerE.setText(email);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.keepSynced(true);
    }

    public void navigateToFragment(Fragment fragment, String navigation)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();

        switch (navigation)
        {
            case "map":
                bottomNavigationView.setSelectedItemId(R.id.btmMap);
                break;
            case "main":
                bottomNavigationView.setSelectedItemId(R.id.btmHome);
                break;
        }
    }

    private void exit()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to exit?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                System.exit(0);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed()
    {
        if(drawerLayout.isDrawerOpen(navigationView))
        {
            drawerLayout.closeDrawer(navigationView, false);
        }
        else
        {
            if (doubleBackToExitPressedOnce)
            {
                System.exit(0);
                return;
            }

            this.doubleBackToExitPressedOnce = true;

            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private final LocationCallback locationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult)
        {
            Location location = locationResult.getLastLocation();
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}