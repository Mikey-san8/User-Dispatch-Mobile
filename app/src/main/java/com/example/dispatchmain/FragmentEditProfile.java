package com.example.dispatchmain;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragmentEditProfile extends Fragment
{
    View edit;

    public EditText firstname, lastname, phone, email, address;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    public BottomSheetBehavior bottomSheetSearch;

    public View bottomSheetView;

    public ArrayList<String> placeArray;

    public ArrayAdapter<String> placeAdapter;

    public ListView searchPlace;

    FragmentCheckProfile fragmentCheckProfile = new FragmentCheckProfile();

    public FragmentEditProfile()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

//        if (!Places.isInitialized())
//        {
//            Places.initialize(requireContext(), API_KEY);
//        }
//        placesClient = Places.createClient(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        edit = inflater.inflate(R.layout.fragment_editprofile, container, false);

        events();
        getProfile();

        return edit;
    }

    public void events()
    {
        firstname = edit.findViewById(R.id.editFirstName);
        lastname = edit.findViewById(R.id.editLastName);
        phone = edit.findViewById(R.id.editMobileNumber);
        email = edit.findViewById(R.id.editEmail);
        address = edit.findViewById(R.id.editAddress);

        searchPlace = edit.findViewById(R.id.listSearch);

        bottomSheetView = edit.findViewById(R.id.searchSheet);

        bottomSheetSearch = BottomSheetBehavior.from(bottomSheetView);

        placeArray = new ArrayList<>();

        placeAdapter = new ArrayAdapter<>(getContext(), R.layout.list_search, R.id.searchList, placeArray);

        searchPlace.setAdapter(placeAdapter);

//        address.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after)
//            {
//                bottomSheetSearch.setState(BottomSheetBehavior.STATE_EXPANDED);
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
//
//                RectangularBounds bounds = RectangularBounds.newInstance
//                        (
//                                new LatLng(5, 114),
//                                new LatLng(21, 127));
//
//                FindAutocompletePredictionsRequest builder = FindAutocompletePredictionsRequest.builder()
//                        .setLocationBias(bounds)
//                        .setCountry("PH")
//                        .setSessionToken(token)
//                        .setQuery(address.getText().toString())
//                        .build();
//
//                placesClient.findAutocompletePredictions(builder).addOnSuccessListener(response ->
//                {
//                    for (AutocompletePrediction prediction : response.getAutocompletePredictions())
//                    {
//                        placeArray.add(String.valueOf(prediction.getFullText(null)));
//                        placeAdapter.notifyDataSetChanged();
//                    }
//
//                }).addOnFailureListener((exception) ->
//                {
//                    if (exception instanceof ApiException) {
//                        ApiException apiException = (ApiException) exception;
//                    }
//                });
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s)
//            {
//                placeArray.clear();
//                placeAdapter.notifyDataSetChanged();
//            }
//        });

        searchPlace.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                address.setText(parent.getAdapter().getItem(position).toString());
            }
        });

        edit.findViewById(R.id.saveProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                saveProfile();
            }
        });

        edit.findViewById(R.id.checkToProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentCheckProfile)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    public void getProfile()
    {
        String userId = auth.getCurrentUser().getUid();

        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot profile: snapshot.child("Users").getChildren())
                {
                    String profileID = profile.getKey();

                    if(userId == profileID)
                    {
                        String sfirstname = profile.child("firstName").getValue(String.class);
                        String slastname = profile.child("lastName").getValue(String.class);
                        String sphone = profile.child("phone").getValue(String.class);
                        String semail = profile.child("email").getValue(String.class);
                        String saddress = profile.child("address").getValue(String.class);

                        firstname.setText(sfirstname);
                        lastname.setText(slastname);
                        phone.setText(sphone);
                        email.setText(semail);
                        address.setText(saddress);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void saveProfile()
    {
        Map<String, Object> save = new HashMap<>();

        String userId = auth.getCurrentUser().getUid();

        String sfirstname = firstname.getText().toString();
        String slastname = lastname.getText().toString();
        String sphone = phone.getText().toString();
        String semail = email.getText().toString();
        String saddress = address.getText().toString();

        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        if(!sfirstname.isEmpty() && !slastname.isEmpty() && !sphone.isEmpty() && !semail.isEmpty() && !saddress.isEmpty())
        {
            save.put("firstName", sfirstname);
            save.put("lastName", slastname);
            save.put("phone", sphone);
            save.put("email", semail);
            save.put("address", saddress);

            databaseReference.child("Users").child(userId).updateChildren(save);

            Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, fragmentCheckProfile, "check_fragment")
                    .addToBackStack(null)
                    .commit();
        }
        else
        {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
