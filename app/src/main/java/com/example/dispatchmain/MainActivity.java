package com.example.dispatchmain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    MainFragment mainFragment = new MainFragment();
    RecordFragment recordFragment = new RecordFragment();
    MapFragment mapFragment = new MapFragment();

    BottomNavigationView bottomNavigationView;

    public View headerView;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    public TextView drawerN,
                    drawerE;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_main);

        if (currentUser == null)
        {
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
            return;
        }

        bottomNavigation();

        mainEvents();

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

    public void bottomNavigation()
    {
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.btmHome);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())

        {
            case R.id.btmHome:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mainFragment)
                        .commit();
                return true;

            case R.id.btmMap:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mapFragment)
                        .commit();
                return true;

            case R.id.btmRecord:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, recordFragment)
                        .commit();
                return true;

//            case R.id.btmMenu:
//                drawerLayout.openDrawer(navigationView, true);
//                userDetails();
//                return true;

            case R.id.nav_logout:
                logoutUser();
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
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, Login.class);
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
        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
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

}