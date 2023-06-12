package com.example.dispatchmain;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class FragmentSettings extends Fragment implements Switch.OnCheckedChangeListener{

    View settings;

    Switch  textToSpeech,
            backgroundService,
            alarm,
            sendAnonymous;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    String userId = auth.getCurrentUser().getUid();

    public FragmentSettings()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        settings = inflater.inflate(R.layout.fragment_settings, container, false);


        events();

        getSettings();

        return settings;
    }

    private void events()
    {
        textToSpeech        = settings.findViewById(R.id.settingsToSpeech);
        backgroundService   = settings.findViewById(R.id.settingsToBgService);
        alarm               = settings.findViewById(R.id.settingsToAlarm);
        sendAnonymous       = settings.findViewById(R.id.settingsToAnonymous);

        textToSpeech.setOnCheckedChangeListener(this);
        backgroundService.setOnCheckedChangeListener(this);
        alarm.setOnCheckedChangeListener(this);
        sendAnonymous.setOnCheckedChangeListener(this);

        settings            .findViewById(R.id.settingsToHome)
                            .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

                FragmentHome fragmentHome = (FragmentHome) getParentFragmentManager().findFragmentByTag("home_fragment");

                if(fragmentHome != null)
                {
                    fragmentHome.gotoDrawer();
                    getParentFragmentManager().popBackStack();

                    transaction
                            .replace(R.id.container, fragmentHome)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        settings            .findViewById(R.id.settingsLogout)
                            .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void getSettings()
    {
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot settings:snapshot.child("Users").getChildren())
                {
                    String getID = settings.getKey();

                    if(userId == getID)
                    {
                        if(settings.hasChild("Settings"))
                        {
                            Boolean vTextToSpeech = settings.child("Settings").child("text to speech").getValue(Boolean.class);
                            Boolean vBackgroundService = settings.child("Settings").child("background service").getValue(Boolean.class);
                            Boolean vAlarm = settings.child("Settings").child("alarm").getValue(Boolean.class);
                            Boolean vSendAnonymous = settings.child("Settings").child("send anonymous").getValue(Boolean.class);

                            if(vTextToSpeech == true)
                            {
                                textToSpeech.setChecked(true);
                            }
                            else
                            {
                                textToSpeech.setChecked(false);
                            }

                            if(vBackgroundService == true)
                            {
                                backgroundService.setChecked(true);
                            }
                            else
                            {
                                backgroundService.setChecked(false);
                            }

                            if(vAlarm == true)
                            {
                                alarm.setChecked(true);
                            }
                            else
                            {
                                alarm.setChecked(false);
                            }

                            if(vSendAnonymous == true)
                            {
                                sendAnonymous.setChecked(true);
                            }
                            else
                            {
                                sendAnonymous.setChecked(false);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void logoutUser()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(settings.getContext());
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
                Intent intent = new Intent(settings.getContext(), U_ActivityLogin.class);
                startActivity(intent);
                getActivity().getFragmentManager().popBackStack();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
         Map<String, Object> setSetting = new HashMap<>();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        switch (buttonView.getId())
        {
            case R.id.settingsToSpeech:
                if(isChecked)
                {
                    setSetting.put("text to speech", true);
                }
                else
                {
                    setSetting.put("text to speech", false);
                }

                databaseReference.child("Users").child(userId).child("Settings").updateChildren(setSetting);

                break;
            case R.id.settingsToBgService:
                if(isChecked)
                {
                    setSetting.put("background service", true);
                }
                else
                {
                    setSetting.put("background service", false);
                }

                databaseReference.child("Users").child(userId).child("Settings").updateChildren(setSetting);
                break;
            case R.id.settingsToAlarm:
                if(isChecked)
                {
                    setSetting.put("alarm", true);
                }
                else
                {
                    setSetting.put("alarm", false);
                }

                databaseReference.child("Users").child(userId).child("Settings").updateChildren(setSetting);
                break;
            case R.id.settingsToAnonymous:
                if(isChecked)
                {
                    setSetting.put("send anonymous", true);
                }
                else
                {
                    setSetting.put("send anonymous", false);
                }

                databaseReference.child("Users").child(userId).child("Settings").updateChildren(setSetting);
                break;
        }
    }
}
