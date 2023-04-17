package com.example.dispatchmain;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("ALL")
public class MainFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener
{
    public String API_KEY = "AIzaSyDKHY_L76RS3qMCyDSctItyGycxGAwBK8U";

    public View main;

    private BottomNavigationView bottomNavigationView;

    LocationManager locationManager;
    Location mLocation;

    private static final int REQUEST_LOCATION_PERMISSIONS = 1;
    private FusedLocationProviderClient fusedLocationClient;

    private PlacesClient placesClient;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    public ImageView    blinkBtn,
                        blinkBtn2,
                        blinkBtn3;

    private BottomSheetBehavior bottoSheetCall;
    private View bottomSheetView;

    LinearLayout    fireCallSheet,
                    linearMainLayout,
                    linearResponder,
                    linearForms;

    public EditText userN,
                    userP,
                    userA,
                    userL,
                    userC;

    public TextInputLayout  nameLay,
                            phoneLay,
                            addressLay,
                            locationLay;

    public TextView accountN,
                    accountE,
                    textCurrent,
                    textAvailable,
                    helloName;

    public ListView placeList;

    private ArrayList<String> pArray;

    private ArrayAdapter<String> pAdapter;

    DateFormat dateFormat;
    String latitude;
    String longitude;

    Boolean stop = false;

    private int counter;
    private int count;

    public MainFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (!Places.isInitialized())
        {
            Places.initialize(requireContext(), API_KEY);
        }
        placesClient = Places.createClient(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        main = inflater.inflate(R.layout.main, container, false);


        setEvents();
        userDetails();
        getResponders();

        return main;
    }

    private final LocationCallback locationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult)
        {
            Location location = locationResult.getLastLocation();
            String holder = "location";
            placeName(main.getContext(), location.getLatitude(), location.getLongitude(), holder);

            mLocation = location;
            saveLocation(location.getLatitude(), location.getLongitude());
        }
    };

    public void placeName(Context context, double latitude, double longitude, String holder)
    {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Create a request object for the place details
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();

        // Use the Places API client to get the place details
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);

        // Add a listener to handle the response from the Places API

        String list = "list";

        ((Task<?>) placeResponse).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                // Get the list of places returned by the Places API
                FindCurrentPlaceResponse response = (FindCurrentPlaceResponse) task.getResult();
                List<PlaceLikelihood> placeLikelihoods = response.getPlaceLikelihoods();

                if (placeLikelihoods != null && !placeLikelihoods.isEmpty())
                {
                    // Get the most likely place from the list
                    Place place = placeLikelihoods.get(0).getPlace();

                    // Use the place details to get the name and address
                    String name = place.getName();
                    String address = place.getAddress();

                    // Use the latitude and longitude from the place details to verify accuracy
                    LatLng placeLatLng = place.getLatLng();
                    LatLng location = new LatLng(latitude, longitude);

                    if(!(placeLatLng == null) || !placeLatLng.equals(location))
                    {
                        textCurrent = main.findViewById(R.id.textCurrent);
                        if(holder == "location")
                        {
                            textCurrent.setText(name + ", " + address);
                        }
                        if(holder == "list")
                        {
                            userL.setText(name + ", " + address);
                            getPlaceName(main.getContext(), placeLatLng, list);
                        }
                    }
                    else
                    {
                        if(holder == "location")
                        {
                            textCurrent.setText(name + ", " + address);
                        }
                        if(holder == "list")
                        {
                            userL.setText(name + ", " + address);
                            getPlaceName(main.getContext(), placeLatLng, list);
                        }
                    }
                }
            }
            else
            {
                // Handle errors from the Places API
                Exception exception = task.getException();
                if (exception != null)
                {
                }
            }
        });
    }

    private void getPlaceName(Context context, LatLng location, String holderName)
    {
        if(holderName == "list")
        {
            if(!pArray.isEmpty())
            {
                pArray.clear();
                pAdapter.notifyDataSetChanged();
            }
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        int maxResults = 10;

        try
        {
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, maxResults);

            if(holderName == "list")
            {
                for (Address address : addressList)
                {
                    pArray.add(address.getAddressLine(0));
                    pAdapter.notifyDataSetChanged();
                }
            }

            if(holderName == "location")
            {
                if (!addressList.isEmpty())
                {
                    Address address = addressList.get(0);
                    String placeName = address.getAddressLine(0);
                    textCurrent.setText(placeName);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

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

    @Override
    public void onPause()
    {
        super.onPause();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void setEvents()
    {
        endIcon();

        userN = main.findViewById(R.id.nameHere);
        userP = main.findViewById(R.id.phoneHere);
        userA = main.findViewById(R.id.addressHere);
        userL = main.findViewById(R.id.currentLocHere);
        userC = main.findViewById(R.id.commentBox);

        accountN = main.findViewById(R.id.accounName);
        accountE = main.findViewById(R.id.accountEmail);

        blinkBtn = main.findViewById(R.id.blink_btn);
        blinkBtn2 = main.findViewById(R.id.shadow);
        blinkBtn3 = main.findViewById(R.id.fire_btn2);
        blinking();

        blinkBtn3.setOnClickListener(this);

        textAvailable = main.findViewById(R.id.textAvailable);

        helloName = main.findViewById(R.id.helloName);

        main.findViewById(R.id.sendButton).setOnClickListener(this);
        main.findViewById(R.id.checkOnMap).setOnClickListener(this);
        main.findViewById(R.id.seeLocation).setOnClickListener(this);

        linearMainLayout = main.findViewById(R.id.linearMainLayout);
        linearResponder = main.findViewById(R.id.linearResponder);
        linearForms = main.findViewById(R.id.linearForms);

        bottomSheetView = main.findViewById(R.id.fireCallSheet);
        bottoSheetCall = BottomSheetBehavior.from(bottomSheetView);

        placeList = main.findViewById(R.id.listPlace);

        pArray = new ArrayList<>();

        pAdapter = new ArrayAdapter<>(getContext(), R.layout.place_list, R.id.placeList, pArray);

        placeList.setAdapter(pAdapter);

        placeList.setOnItemClickListener(this);

        bottoSheetCall.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
        {
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
                    {

                    }
                    break;
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
                        userL.clearFocus();

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
        locationLay = main.findViewById(R.id.currentLocLayout);

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

        locationLay.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userL.setText("");
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        switch (parent.getId())
        {
            case R.id.listPlace:

                String placeClick = (String) parent.getAdapter().getItem(position);

                userL.setText(placeClick);

                onClickItem(parent, view, position, id);

                break;
        }
    }

    public void onClickItem(AdapterView<?> adapterView, View view, int i, long l)
    {
        String invoke = adapterView.getAdapter().getItem(i).toString();

        Geocoder geocoder = new Geocoder(main.getContext());

        List<Address> addressList = null;

        try
        {
            addressList = geocoder.getFromLocationName(invoke, 1);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Address address = addressList.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        String holder = "list";
        getPlaceName(main.getContext(), latLng, holder);
    }

    @Override
    public void onClick(View v)
    {
        Fragment fragment;

        String navigation;

        switch (v.getId())
        {
            case R.id.fire_btn2:

                if(!(textCurrent == null))
                {
                    String holderName = "list";
                    bottoSheetCall.setState(BottomSheetBehavior.STATE_EXPANDED);

                    placeName(main.getContext(), mLocation.getLatitude(), mLocation.getLongitude(), holderName);
                }
                else
                {
                    Toast.makeText(main.getContext(), "Detecting Location", Toast.LENGTH_SHORT).show();

                    new CountDownTimer(6000, 1000)
                    {

                        public void onTick(long millisUntilFinished)
                        {
                            if(textCurrent != null)
                            {
                                main.findViewById(R.id.fire_btn2).callOnClick();
                            }
                        }

                        public void onFinish()
                        {

                        }
                    }.start();
                }

                break;
            case R.id.sendButton:
                send();
                break;
            case R.id.checkOnMap:
                fragment = new MapFragment();
                navigation = "map";

                if(textCurrent != null)
                {
                    Toast.makeText(requireContext(), "Location Detected", Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).navigateToFragment(fragment, navigation);
                }
                else
                {
                    Toast.makeText(requireContext(), "Detecting Location", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.seeLocation:
                fragment = new MapFragment();
                navigation = "map";

                if(textCurrent != null)
                {
                    Toast.makeText(requireContext(), "Location Detected", Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).navigateToFragment(fragment, navigation);
                }
                else
                {
                    Toast.makeText(requireContext(), "Detecting Location", Toast.LENGTH_LONG).show();
                }

                break;
        }
    }

    private void send()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Please check your details");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Fragment information;

                Bundle bundle = new Bundle();
                String navigation;

                String nUser = userN.getText().toString();
                String pUser = userP.getText().toString();
                String aUser = userA.getText().toString();
                String lUser = userL.getText().toString();
                String cUser = userC.getText().toString();

                sendReport(nUser, pUser, aUser, lUser, cUser);

                bottoSheetCall.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(!bundle.isEmpty())
                {
                    bundle.clear();
                }
                bundle.putString("Name", nUser);
                bundle.putString("Phone", pUser);
                bundle.putString("Address", aUser);
                bundle.putString("Location", lUser);
                bundle.putString("Comment", cUser);
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
                            public void onTick(long millisUntilFinished)
                            {

                            }

                            @Override
                            public void onFinish()
                            {
                                ((MainActivity) getActivity()).navigateToFragment(information, navigation);
                            }
                        }.start();
                    }
                }.start();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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

        Animation zoomInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in);
        Animation zoomOutAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_out);

        blinkBtn.startAnimation(zoomInAnimation);
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

    public void userDetails()
    {
        String userId = auth.getCurrentUser().getUid();

        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        String responder = String.valueOf(counter);

        ValueEventListener valueEventListener = new ValueEventListener()
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

                helloName.setText("Hello! " + firstName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
        databaseReference.keepSynced(true);
    }

    public void getResponders()
    {
        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(counter != 0)
                {
                    counter = 0;
                }

                fusedLocationClient.getLastLocation().addOnCompleteListener(task ->
                {
                    if(task.isSuccessful())
                    {
                        Location location = task.getResult();

                        for (DataSnapshot Snapshot: snapshot.child("Firefighter").getChildren())
                        {
                            Double lat = Snapshot.child("lat").getValue(Double.class);
                            Double lng = Snapshot.child("lng").getValue(Double.class);

                            String show = String.valueOf(lat +", "+ lng);

                            LatLng fighterLocation = new LatLng(lat, lng);
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            if(calculateDistance(fighterLocation, userLocation) < 5001 && calculateDistance(fighterLocation, userLocation) > 1)
                            {
                                counter++;
                            }
                            String responder = String.valueOf(counter);
                            textAvailable.setText("Available responder/s: " + responder);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        databaseReference.addValueEventListener(valueEventListener);

    }

    public void sendReport(String name, String phone, String address, String location, String comment)
    {
        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference usersRef = firebaseFirestore.collection("Users");
        dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String timeDate = dateFormat.format(new Date());

        Map<String, Object> report = new HashMap<>();
        report.put("FullName", name);
        report.put("MobileNumber", phone);
        report.put("Home Address", address);
        report.put("Location", location);
        report.put("Comment", comment);
        report.put("Date & Time", timeDate);

        DocumentReference userRef = firebaseFirestore.collection("Users").document(userId);
        CollectionReference reportRef = userRef.collection("Reports");

        reportRef.add(report);
        firebaseFirestore.collection("Users").document(userId).set(report);

    }

    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public LatLng getCalculations(String placeName, String ID)
    {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        List<Address> addresses = null;
        try
        {
            addresses = geocoder.getFromLocationName(placeName, 1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0)
        {
            Address address = addresses.get(0);
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();

            LatLng distance = new LatLng(latitude, longitude);

            return distance;
        }

        return null;
    }

    private double calculateDistance(LatLng loc1, LatLng loc2)
    {
        Location startPoint=new Location("locationA");
        startPoint.setLatitude(loc1.latitude);
        startPoint.setLongitude(loc1.longitude);

        Location endPoint=new Location("locationA");
        endPoint.setLatitude(loc2.latitude);
        endPoint.setLongitude(loc2.longitude);

        double distance=startPoint.distanceTo(endPoint);

        return distance;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, (LocationListener) this);
            }
            else
            {

            }
        }
    }
}
