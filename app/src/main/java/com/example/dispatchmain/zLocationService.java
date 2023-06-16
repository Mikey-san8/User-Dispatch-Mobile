package com.example.dispatchmain;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ALL")
public class zLocationService extends Service
{
    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    FragmentHome fragmentHome_fireman = new FragmentHome();

    private Handler mHandler;
    private Runnable mRunnable;

    private Location setLocation;

    public static final String ACTION_STOP_ALARM = "com.example.app.STOP_ALARM";

    U_ActivityMain mainActivity;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Boolean getAlarm;

    zCalculations calculate = new zCalculations(zLocationService.this);

    TextToSpeech textToSpeech;

    private void startLocationUpdates()
    {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setMaxWaitTime(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startLocationUpdates();

        mainActivity = new U_ActivityMain();

        mHandler = new Handler();

        mRunnable = new Runnable()
        {
            @Override
            public void run()
            {
//                saveLocation(setLocation.getLatitude(), setLocation.getLongitude());

                mHandler.postDelayed(this, 20000);
            }
        };

        mHandler.postDelayed(mRunnable, 20000);

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

        getNotice();
        getNearby();
    }

    LocationCallback locationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult)
        {
            Location location =  locationResult.getLastLocation();

            setLocation = location;
        }
    };

    public void saveLocation(double latitude, double longitude)
    {
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        HashMap User = new HashMap();
        String userId = auth.getCurrentUser().getUid();
        User.put("lat", latitude);
        User.put("lng", longitude);
        mDatabase.child("Users").child(userId).updateChildren(User);
        mDatabase.keepSynced(true);
    }

    String distance;

    private void createNotificationChanel(String title, String text)
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("responder", "responder notification", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "responder")
                .setSmallIcon(R.drawable.fireicon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(defaultSoundUri)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            builder.setCategory(NotificationCompat.CATEGORY_CALL)
                    .setFullScreenIntent(null, true);
        }

        int notificationID = (int) System.currentTimeMillis();

        notificationManager.notify(notificationID, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if (intent != null && intent.getAction() != null)
        {
            if (intent.getAction().equals(ACTION_STOP_ALARM))
            {
                Intent stopIntent = new Intent(zLocationService.ACTION_STOP_ALARM);
                sendBroadcast(stopIntent);
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void getNearby()
    {
        String userId = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        Map<String, Object> nearby = new HashMap<>();

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);

        databaseReference.child("Users").child(userId).child("Responders").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot change : snapshot.getChildren())
                {
                    String key = change.getKey();

                    nearby.put("Nearby", false);
                    databaseReference.child("Users").child(userId).child("Responders").child(key).updateChildren(nearby);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        databaseReference.child("Firefighter").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                fusedLocationClient.getLastLocation().addOnCompleteListener(task ->
                {
                    if(task.isSuccessful())
                    {
                        Location location = task.getResult();

                        for (DataSnapshot Snapshot : snapshot.getChildren())
                        {
                            String fighterKey = Snapshot.getKey();

                            if (Snapshot.hasChild("Settings"))
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

                                distance = formattedDistance;

                                if (calculate.calculateDistance(fighterLocation, userLocation) < 601
                                        && calculate.calculateDistance(fighterLocation, userLocation) >= 0)
                                {
                                    nearby.put("Nearby", true);
                                    databaseReference.child("Users").child(userId).child("Responders").child(fighterKey).updateChildren(nearby);
                                }
                                else
                                {
                                    nearby.put("Nearby", false);
                                    databaseReference.child("Users").child(userId).child("Responders").child(fighterKey).updateChildren(nearby);
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getNotice()
    {
        String userId = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        databaseReference.child("Users").child(userId).child("Responders").addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {
               Boolean getNearby = snapshot.child("Nearby").getValue(Boolean.class);

               if(snapshot.hasChild("Status"))
               {
                   Boolean status   = snapshot.child("Status").getValue(Boolean.class);

                   if(getNearby == true && status == true)
                   {
                       createNotificationChanel("Responder","A firefighter is nearby!");
                   }
               }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
