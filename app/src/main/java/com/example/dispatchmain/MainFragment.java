package com.example.dispatchmain;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ALL")
public class MainFragment extends Fragment implements View.OnClickListener, LocationListener
{
    public View main;

    private BottomNavigationView bottomNavigationView;

    private LocationManager locationManager;

    private static final int REQUEST_LOCATION_PERMISSIONS = 1;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    public ImageView    blinkBtn,
                        blinkBtn2,
                        blinkBtn3;

    BottomSheetBehavior fireCall;

    LinearLayout    fireCallSheet,
                    linearMainLayout,
                    linearResponder,
                    linearForms;

    public EditText userN,
                    userP,
                    userA;

    public TextInputLayout  nameLay,
                            phoneLay,
                            addressLay;

    public TextView accountN,
                    accountE,
                    textCurrent;

    DateFormat dateFormat;


    public MainFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        main = inflater.inflate(R.layout.main, container, false);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        setEvents();

        return main;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());
        textCurrent = main.findViewById(R.id.textCurrent);
        textCurrent.setText(latitude + ", " + longitude);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        locationManager.removeUpdates(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, (LocationListener) this);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSIONS);
        }
    }

    public void setEvents()
    {
        endIcon();
        userN = main.findViewById(R.id.nameHere);
        userP = main.findViewById(R.id.phoneHere);
        userA = main.findViewById(R.id.addressHere);

        accountN = main.findViewById(R.id.accounName);
        accountE = main.findViewById(R.id.accountEmail);

        blinkBtn = main.findViewById(R.id.blink_btn);
        blinkBtn2 = main.findViewById(R.id.shadow);
        blinkBtn3 = main.findViewById(R.id.fire_btn2);
        blinking();

        blinkBtn3.setOnClickListener(this);

        main.findViewById(R.id.commentClick).setOnClickListener(this);
        main.findViewById(R.id.sendButton).setOnClickListener(this);
        main.findViewById(R.id.checkOnMap).setOnClickListener(this);
        main.findViewById(R.id.seeLocation).setOnClickListener(this);

        fireCallSheet = main.findViewById(R.id.fireCallSheet);

        linearMainLayout = main.findViewById(R.id.linearMainLayout);
        linearResponder = main.findViewById(R.id.linearResponder);
        linearForms = main.findViewById(R.id.linearForms);

        fireCall = BottomSheetBehavior.from(fireCallSheet);

        fireCall.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
        {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        fireCall.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    {
                        linearMainLayout.setVisibility(View.INVISIBLE);
                        linearResponder.setVisibility(View.INVISIBLE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:

                    case BottomSheetBehavior.STATE_SETTLING:

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    {

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    {
                        userN.clearFocus();
                        userP.clearFocus();
                        userA.clearFocus();

                        linearMainLayout.setVisibility(View.VISIBLE);
                        linearResponder.setVisibility(View.VISIBLE);

                        hideKeyboard(bottomSheet);
                    }
                    break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {
                linearMainLayout.setVisibility(View.INVISIBLE);
                linearResponder.setVisibility(View.INVISIBLE);
            }
        });

    }

    public void endIcon()
    {
        nameLay = main.findViewById(R.id.nameLayout);
        phoneLay = main.findViewById(R.id.phoneLayout);
        addressLay = main.findViewById(R.id.addressLayout);

        nameLay.setEndIconOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userN.setText("");
            }
        });

        phoneLay.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userP.setText("");
            }
        });

        addressLay.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userA.setText("");
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        Fragment fragment;
        Fragment information;

        Bundle bundle = new Bundle();
        String navigation;

        switch (v.getId())
        {
            case R.id.fire_btn2:
                fireCall.setState(BottomSheetBehavior.STATE_EXPANDED);
                userDetails();
                break;
            case R.id.sendButton:

                String nUser = userN.getText().toString();
                String pUser = userP.getText().toString();
                String lUser = userA.getText().toString();

                sendReport(nUser, pUser, lUser);

                fireCall.setState(BottomSheetBehavior.STATE_COLLAPSED);

                bundle.putString("Name", nUser);
                bundle.putString("Phone", pUser);
                bundle.putString("Address", lUser);
                bundle.putString("Location", lUser);
                bundle.putString("TimeDate", dateFormat.format(new Date()));

                information = new InformationFragment();
                information.setArguments(bundle);
                navigation = "information";
                Toast.makeText(main.getContext(), "SENDING REPORT", Toast.LENGTH_LONG).show();

                new CountDownTimer(6000, 1000)
                {

                    public void onTick(long millisUntilFinished)
                    {

                    }

                    public void onFinish()
                    {
                        Toast.makeText(main.getContext(), "REPORT SENT", Toast.LENGTH_SHORT).show();
                        new CountDownTimer(1500, 1000){

                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish()
                            {
                                ((MainActivity) getActivity()).navigateToFragment(information, navigation);
                            }
                        }.start();
                    }
                }.start();


                break;
            case R.id.checkOnMap:
                fragment = new MapFragment();
                navigation = "map";

                ((MainActivity) getActivity()).navigateToFragment(fragment, navigation);
                break;
            case R.id.seeLocation:
                fragment = new MapFragment();
                navigation = "map";

                ((MainActivity) getActivity()).navigateToFragment(fragment, navigation);
                break;
        }
    }

    private void navigateToFragment(Fragment fragment)
    {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    public void blinking()
    {
        Animation anim = new AlphaAnimation(0.5f, 1.0f);
        anim.setDuration(1200); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        Animation anim2 = new AlphaAnimation(0.5f, 1.0f);
        anim2.setDuration(500); //You can manage the blinking time with this parameter
        anim2.setStartOffset(20);
        anim2.setRepeatMode(Animation.REVERSE);
        anim2.setRepeatCount(Animation.INFINITE);

        blinkBtn.startAnimation(anim);
        blinkBtn2.startAnimation(anim2);
//        blinkBtn3.startAnimation(anim);
    }


    private void logoutUser()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(main.getContext());
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
            public void onClick(DialogInterface dialog, int which)
            {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(main.getContext(), Login.class);
                startActivity(intent);
                getActivity().getFragmentManager().popBackStack();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    private void exit()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(main.getContext());
        builder.setMessage("Are you sure you want to exit?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

                userN.setText(firstName + " " + lastName);
                userP.setText(phone);
                userA.setText(address);

                accountN.setText(firstName + " " + lastName);
                accountE.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        databaseReference.keepSynced(true);
    }

    public void sendReport(String name, String phone, String location)
    {

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = firebaseFirestore.collection("Users");
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String timeDate = dateFormat.format(new Date());

        Map<String, Object> report = new HashMap<>();
        report.put("FullName", name);
        report.put("MobileNumber", phone);
        report.put("Location", location);
        report.put("Date & Time", timeDate);

        DocumentReference userRef = firebaseFirestore.collection("Users").document(userId);
        CollectionReference reportRef = userRef.collection("Reports");

        reportRef.add(report);

    }

    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, (LocationListener) this);
            } else
            {
                // Permission denied
                // Handle accordingly (e.g. show a message to the user)
            }
        }
    }
}
