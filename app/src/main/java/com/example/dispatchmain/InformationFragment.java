package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InformationFragment extends Fragment
{
    View info;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    ArrayList<String> recordArray;

    ArrayAdapter<String> recordAdapter;

    TextView    infoName,
                infoPhone,
                infoAddress,
                infoLocation,
                timeDate;

    public InformationFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        info = inflater.inflate(R.layout.report_info, container, false);

        events();

        return info;
    }

    public void events()
    {
        infoName = info.findViewById(R.id.infoName);
        infoPhone = info.findViewById(R.id.infoPhone);
        infoAddress = info.findViewById(R.id.infoAddress);
        infoLocation = info.findViewById(R.id.infoLocation);
        timeDate = info.findViewById(R.id.infoDate);

        Bundle bundle = getArguments();
        String name = bundle.getString("Name");
        String phone = bundle.getString("Phone");
        String address = bundle.getString("Address");
        String location = bundle.getString("Location");
        String time = bundle.getString("TimeDate");

        infoName.setText(name);
        infoPhone.setText(phone);
        infoAddress.setText(address);
        infoLocation.setText(location);
        timeDate.setText(time);

        info.findViewById(R.id.logo_demo).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Fragment map;
                map = new MapFragment();
                String navigation = "map";
                ((MainActivity) requireActivity()).navigateToFragment(map, navigation);
            }
        });

        info.findViewById(R.id.recordClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Fragment main;
                main = new MainFragment();
                String navigation = "main";
                ((MainActivity) requireActivity()).navigateToFragment(main, navigation);
            }
        });
    }
}
