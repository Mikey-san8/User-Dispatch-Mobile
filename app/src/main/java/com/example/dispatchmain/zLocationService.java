package com.example.dispatchmain;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;

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
                saveLocation(setLocation.getLatitude(), setLocation.getLongitude());

                Toast.makeText(zLocationService.this, "Dispatch: Location Updated", Toast.LENGTH_SHORT).show();

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChanel()
    {
        int ID = (int) System.currentTimeMillis();

        String notificationChannelId = "Location";
        String channelName = "Background Service";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(notificationChannelId, channelName , NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channelName);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.deleteNotificationChannel(channelName);
            notificationManager.deleteNotificationChannel("Location channel id");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, U_ActivitySplash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, notificationChannelId)
                .setSmallIcon(R.drawable.fireicon)
                .setContentTitle("Dispatch is running")
                .setContentText("A firefighter is nearby! Distance: " + distance + "km")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_media_play, "Open App", pendingIntent);

        notificationBuilder
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setSound(defaultSoundUri);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(1, notificationBuilder.build());
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
                                        && calculate.calculateDistance(fighterLocation, userLocation) >= 0)
                                {
                                    Toast.makeText(getApplicationContext(), "Notice", Toast.LENGTH_SHORT).show();

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


//                                            Intent intent = new Intent(xLocationService.this, U_ActivityMain.class);
//                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                            intent.putExtra("NOTIFICATION_TAPPED", false);
//                                            PendingIntent pendingIntent = PendingIntent.getActivity(
//                                                    xLocationService.this, 1, intent,
//                                                    PendingIntent.FLAG_IMMUTABLE);
//
//                                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(xLocationService.this, notificationChannelId);
//
//                                            notificationBuilder
//                                                    .setOngoing(true)
//                                                    .setContentTitle("You have a message")
//                                                    .setContentText("A firefighter is nearby!\nDistance is "+formattedDistance+"kilometers")
//                                                    .setPriority(NotificationCompat.PRIORITY_MIN)
//                                                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
//                                                    .setContentIntent(pendingIntent)
//                                                    .addAction(android.R.drawable.ic_media_pause, "Open application", pendingIntent);
//
//                                            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                                            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
//                                            notificationBuilder.setSound(defaultSoundUri);
//
//                                            Notification notification = notificationBuilder.build();
//
//                                            startForeground(2, notification);
                                        distance = formattedDistance;

                                        new Notification();

                                        createNotificationChanel();

                                        String speech = "A firefighter is nearby! Distance is " + formattedDistance +"kilometers";
                                        textToSpeech.speak(speech, TextToSpeech.QUEUE_ADD, null);
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
}
