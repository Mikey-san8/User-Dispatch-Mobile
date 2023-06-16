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
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
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
public class FragmentHome extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, View.OnTouchListener
{
    public String API_KEY = "AIzaSyDKmNOWX9-j1NyReDFb6-5R9P2wdIKJvyg";

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
                        blinkBtn3,
                        sendButton;

    private CardView callButton;

    public BottomSheetBehavior bottomSheetCall;
    public View bottomSheetView;

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
                    textResponders,
                    helloName;

    public ListView placeList;

    private ArrayList<String> pArray;

    private ArrayAdapter<String> pAdapter;

    DateFormat dateFormat;

    ValueEventListener valueEventListener;

    TextToSpeech textToSpeech;

    public Boolean isDialogShown = false;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Boolean settingSpeech,
            settingBgService,
            settingAlarm,
            settingAnonymous;

    String userAnonymous;

    zCalculations calculate;

    public FragmentHome()
    {

    }

    public void gotoDrawer()
    {
        ((U_ActivityMain) getActivity()).bottomNavigationView.setSelectedItemId(R.id.btmMenu);
        ((U_ActivityMain) getActivity()).navigationView.setCheckedItem(R.id.nav_home);
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
        main = inflater.inflate(R.layout.fragment_home, container, false);

        textToSpeech = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        showFirstDialog();

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
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        String list = "list";

        ((Task<?>) placeResponse).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                FindCurrentPlaceResponse response = (FindCurrentPlaceResponse) task.getResult();
                List<PlaceLikelihood> placeLikelihoods = response.getPlaceLikelihoods();

                if (placeLikelihoods != null && !placeLikelihoods.isEmpty())
                {
                    Place place = placeLikelihoods.get(0).getPlace();
                    String name = place.getName();
                    String address = place.getAddress();
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
        calculate = new zCalculations(main.getContext());

        endIcon();

        callButton      = main.findViewById(R.id.cardView);

        userN           = main.findViewById(R.id.nameHere);
        userP           = main.findViewById(R.id.phoneHere);
        userA           = main.findViewById(R.id.addressHere);
        userL           = main.findViewById(R.id.currentLocHere);

        whenTyping();

        userC           = main.findViewById(R.id.commentBox);

        accountN        = main.findViewById(R.id.accounName);
        accountE        = main.findViewById(R.id.accountEmail);

        blinkBtn        = main.findViewById(R.id.blink_btn);
        blinkBtn2       = main.findViewById(R.id.shadow);
        blinkBtn3       = main.findViewById(R.id.fire_btn2);

        sendButton      = main.findViewById(R.id.sendButton);

        blinking();

        blinkBtn3       .setOnClickListener(this);
        sendButton      .setOnClickListener(this);

        textAvailable   = main.findViewById(R.id.textAvailable);
        textResponders  = main.findViewById(R.id.textResponders);

        helloName       = main.findViewById(R.id.helloName);

        main            .findViewById(R.id.checkOnMap)          .setOnClickListener(this);
        main            .findViewById(R.id.seeLocation)         .setOnClickListener(this);
        main            .findViewById(R.id.cardView)            .setOnClickListener(this);
        main            .findViewById(R.id.respondCheck)        .setOnClickListener(this);
        main            .findViewById(R.id.checkDirectories)    .setOnClickListener(this);
        main            .findViewById(R.id.chooseFromMap)       .setOnClickListener(this);
        main            .findViewById(R.id.checkProfile)        .setOnClickListener(this);
        main            .findViewById(R.id.arrowDown)           .setOnClickListener(this);

        linearMainLayout    = main.findViewById(R.id.linearMainLayout);
        linearResponder     = main.findViewById(R.id.linearResponder);
        linearForms         = main.findViewById(R.id.linearForms);

        bottomSheetView     = main.findViewById(R.id.fireCallSheet);
        bottomSheetCall     = BottomSheetBehavior.from(bottomSheetView);

        placeList = main    .findViewById(R.id.listPlace);

        placeList           .setOnScrollListener(new AbsListView.OnScrollListener()
        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {
                if(scrollState == SCROLL_STATE_TOUCH_SCROLL)
                {
                    bottomSheetCall.setDraggable(false);
                }
                else
                {
                    bottomSheetCall.setDraggable(true);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(firstVisibleItem == 0)
                {
                    bottomSheetCall.setDraggable(true);
                }
            }
        });

        placeList       .setOnItemClickListener(this);

        pArray          = new ArrayList<>();
        pAdapter        = new ArrayAdapter<>(getContext(), R.layout.list_place, R.id.placeList, pArray);
        placeList       .setAdapter(pAdapter);

        bottomSheetCall .setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback()
        {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                switch (newState)
                {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    {
                        callButton.setVisibility(View.INVISIBLE);
                        linearResponder.setVisibility(View.INVISIBLE);
                        linearMainLayout.setVisibility(View.INVISIBLE);
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

                        callButton.setVisibility(View.VISIBLE);
                        linearResponder.setVisibility(View.VISIBLE);
                        linearMainLayout.setVisibility(View.VISIBLE);

                        hideKeyboard(bottomSheet);
                    }
                    break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {
                callButton.setVisibility(View.INVISIBLE);
                linearResponder.setVisibility(View.INVISIBLE);
                linearMainLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void endIcon()
    {
        nameLay         = main.findViewById(R.id.nameLayout);
        phoneLay        = main.findViewById(R.id.phoneLayout);
        addressLay      = main.findViewById(R.id.addressLayout);
        locationLay     = main.findViewById(R.id.currentLocLayout);

        nameLay         .setEndIconOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                userN.setText("");
            }
        });

        phoneLay        .setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userP.setText("");
            }
        });

        addressLay      .setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                userA.setText("");
            }
        });

        locationLay     .setEndIconOnClickListener(new View.OnClickListener() {
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
                break;
        }
    }

    @Override
    public void onClick(View v)
    {
        Fragment fragment;

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        String navigation;

        switch (v.getId())
        {
            case R.id.fire_btn2:

                if(!(textCurrent == null))
                {
                    String holderName = "list";
                    bottomSheetCall.setState(BottomSheetBehavior.STATE_EXPANDED);

                    placeName(main.getContext(), mLocation.getLatitude(), mLocation.getLongitude(), holderName);

                    if(settingSpeech == true)
                    {
                        String emergencyButton = "You may change the details or leave it as be. Don't forget to send your report.";

                        textToSpeech.speak(emergencyButton, TextToSpeech.QUEUE_ADD, null, null);
                    }
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
                                cancel();
                            }
                        }

                        public void onFinish()
                        {

                        }
                    }.start();
                }

                break;
            case R.id.sendButton:

                if(settingSpeech == true)
                {
                    String checkDetails = "Please check your details then send. After sending, wait for a nearby firefighter to respond to your incident location.";

                    textToSpeech.speak(checkDetails, TextToSpeech.QUEUE_ADD, null, null);
                }

                send();
                break;
            case R.id.respondCheck:
                fragment = new FragmentMap();
                navigation = "map";

                if(textCurrent != null)
                {
                    Toast.makeText(requireContext(), "Location Detected", Toast.LENGTH_SHORT).show();
                    ((U_ActivityMain) getActivity()).navigateToFragment(fragment, navigation);
                }
                else
                {
                    Toast.makeText(requireContext(), "Detecting Location", Toast.LENGTH_LONG).show();
                }

            case R.id.checkOnMap:
                fragment = new FragmentMap();
                navigation = "map";

                if(textCurrent != null)
                {
                    Toast.makeText(requireContext(), "Location Detected", Toast.LENGTH_SHORT).show();
                    ((U_ActivityMain) getActivity()).navigateToFragment(fragment, navigation);
                }
                else
                {
                    Toast.makeText(requireContext(), "Detecting Location", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.seeLocation:
                fragment = new FragmentMap();
                navigation = "map";

                if(textCurrent != null)
                {
                    Toast.makeText(requireContext(), "Location Detected", Toast.LENGTH_SHORT).show();
                    ((U_ActivityMain) getActivity()).navigateToFragment(fragment, navigation);
                }
                else
                {
                    Toast.makeText(requireContext(), "Detecting Location", Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.cardView:
                AlertDialog.Builder builder = new AlertDialog.Builder(main.getContext());
                builder.setMessage("Emergency call? 911");
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
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                        dialIntent.setData(Uri.parse("tel:" + "911"));
                        startActivity(dialIntent);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

            case R.id.checkDirectories:

                FragmentDirectories directories = new FragmentDirectories();

                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);

                transaction
                        .replace(R.id.container, directories, "fragment_directory")
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.chooseFromMap:

                FragmentChooseMap chooseMap = new FragmentChooseMap();

                if(chooseMap != null)
                {
                    transaction
                            .replace(R.id.chooseMap, chooseMap, "choose_fragment")
                            .addToBackStack(null)
                            .commit();
                }

                bottomSheetCall.setState(BottomSheetBehavior.STATE_EXPANDED);
                bottomSheetCall.setDraggable(false);

                break;
            case R.id.checkProfile:
                FragmentCheckProfile fragmentCheckProfile = new FragmentCheckProfile();

                transaction
                        .replace(R.id.container, fragmentCheckProfile, "check_fragment")
                        .addToBackStack(null)
                        .commit();

                break;
            case R.id.arrowDown:
                bottomSheetCall.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
        }
    }

    public void whenTyping()
    {

        userL.addTextChangedListener(new TextWatcher()
        {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {

        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

            RectangularBounds bounds = RectangularBounds.newInstance
                    (
                            new LatLng(5, 114),
                            new LatLng(21, 127));

            FindAutocompletePredictionsRequest builder = FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(bounds)
                    .setCountry("PH")
                    .setSessionToken(token)
                    .setQuery(userL.getText().toString())
                    .build();

            placesClient.findAutocompletePredictions(builder).addOnSuccessListener(response ->
            {
                for (AutocompletePrediction prediction : response.getAutocompletePredictions())
                {
                    pArray.add(String.valueOf(prediction.getFullText(null)));
                    pAdapter.notifyDataSetChanged();
                }

            }).addOnFailureListener((exception) ->
            {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                }
            });
        }

        @Override
        public void afterTextChanged(Editable editable)
        {
            pArray.clear();
            pAdapter.notifyDataSetChanged();
        }
        
        });
    }

    private void send()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("House Bill 10706\nAN ACT PENALIZING PRANK CALLERS TO EMERGENCY HOTLINES\n\n" +
                "Emergency hotlines established for the purpose of " +
                "responding to emergency situations shall, at all times, be free from receiving " +
                "unnecessary calls. It shall be prohibited for any individual to make prank calls to any " +
                "hotline at any time.\n\n"+ "Please check your details before sending.");
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
                String userId = auth.getCurrentUser().getUid();

                firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
                databaseReference = firebaseDatabase.getReference();

                if(settingSpeech == true)
                {
                    String sendingReport = "You are now sending the report.";
                    String checkIntruction = "Please be safe and stay calm. You may check further instructions by tapping the directories.";
                    textToSpeech.speak(sendingReport + checkIntruction, TextToSpeech.QUEUE_ADD, null, null);
                }

                Fragment information;

                Bundle bundle = new Bundle();
                String navigation;

                String nUser = userN.getText().toString();
                String pUser = userP.getText().toString();
                String aUser = userA.getText().toString();
                String lUser = userL.getText().toString();
                String cUser = userC.getText().toString();

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        Boolean checkAnonymous = snapshot.child("Users").child(userId).child("Settings").child("send anonymous").getValue(Boolean.class);

                        String updatedUserName = null;

                        if (userId != null && userId.length() > 20)
                        {
                            updatedUserName = "user." + userId.substring(0, userId.length() - 20);
                        }

                        String getName      = null;


                        if(checkAnonymous == true)
                        {
                            getName     = updatedUserName;
                        }
                        else
                        {
                            getName     = nUser;
                        }

                        sendReport(getName, pUser, aUser, lUser, cUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                bottomSheetCall.setState(BottomSheetBehavior.STATE_COLLAPSED);

                if(!bundle.isEmpty())
                {
                    bundle.clear();
                }

                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

                bundle.putString("Name", nUser);
                bundle.putString("Phone", pUser);
                bundle.putString("Address", aUser);
                bundle.putString("Location", lUser);
                bundle.putString("Comment", cUser);
                bundle.putString("TimeDate", dateFormat.format(new Date()));

                information = new FragmentInformation();
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
                        new CountDownTimer(1200, 1000){

                            @Override
                            public void onTick(long millisUntilFinished)
                            {

                            }

                            @Override
                            public void onFinish()
                            {
                                ((U_ActivityMain) getActivity()).navigateToFragment(information, navigation);
                                if(settingSpeech == true)
                                {
                                    textToSpeech.speak("This is your information. You may now view the map, for monitoring",
                                            TextToSpeech.QUEUE_ADD, null, null);
                                }

                            }
                        }.start();
                    }
                }.start();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void sendReport(String name, String phone, String address, String location, String comment)
    {
        String userId = auth.getCurrentUser().getUid();

        long currentTime    = System.currentTimeMillis();

        dateFormat          = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String timeDate     = dateFormat.format(new Date());

        Map<String, Object> report = new HashMap<>();
        report.put("FullName", name);
        report.put("MobileNumber", phone);
        report.put("Home Address", address);
        report.put("Location", location);
        report.put("Comment", comment);
        report.put("Date & Time", timeDate);
        report.put("TimeStamp", currentTime);

        databaseReference.child("Users").child(userId).child("Report").updateChildren(report);
        databaseReference.child("Users").child(userId).child("Report").child("Reports").push().updateChildren(report);
        databaseReference.child("Data").push().updateChildren(report);
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

        blinkBtn    .startAnimation(zoomInAnimation);
        blinkBtn2   .startAnimation(anim2);
        sendButton  .startAnimation(anim);

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
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(main.getContext(), U_ActivityLogin.class);
                startActivity(intent);
                getActivity().getFragmentManager().popBackStack();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showFirstDialog()
    {
        if(isDialogShown == false)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(main.getContext());
            builder.setMessage("This application sends location of incident reports by a user for firefighters to respond.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

            isDialogShown = true;
        }
    }

    public void userDetails()
    {
        String userId = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        valueEventListener = new ValueEventListener()
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

                Boolean vTextToSpeech = snapshot.child("Users").child(userId).child("Settings").child("text to speech").getValue(Boolean.class);
                Boolean vBackgroundService = snapshot.child("Users").child(userId).child("Settings").child("background service").getValue(Boolean.class);
                Boolean vAlarm = snapshot.child("Users").child(userId).child("Settings").child("alarm").getValue(Boolean.class);
                Boolean vSendAnonymous = snapshot.child("Users").child(userId).child("Settings").child("send anonymous").getValue(Boolean.class);

                settingSpeech       = vTextToSpeech;
                settingBgService    = vBackgroundService;
                settingAlarm        = vAlarm;
                settingAnonymous    = vSendAnonymous;

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
        String userId = auth.getCurrentUser().getUid();

        List<String> countr = new ArrayList<>();

        List<String> responderCounter = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!countr.isEmpty())
                {
                    countr.clear();
                }

                if(!responderCounter.isEmpty())
                {
                    responderCounter.clear();
                }

                fusedLocationClient.getLastLocation().addOnCompleteListener(task ->
                {
                    if(task.isSuccessful())
                    {
                        Location location = task.getResult();

                        for (DataSnapshot Snapshot: snapshot.child("Firefighter").getChildren())
                        {
                            if(Snapshot.hasChild("Settings"))
                            {
                                Boolean onlineStatus = Snapshot.child("Settings").child("online status").getValue(Boolean.class);

                                if(onlineStatus == true)
                                {
                                    Double lat = Snapshot.child("lat").getValue(Double.class);
                                    Double lng = Snapshot.child("lng").getValue(Double.class);

                                    LatLng fighterLocation = new LatLng(lat, lng);
                                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                    if(calculate.calculateDistance(fighterLocation, userLocation) < 7001)
                                    {
                                        countr.add(String.valueOf(onlineStatus));

                                        String responder = String.valueOf(countr.size());
                                        textAvailable.setText("Available responder/s: " + responder);
                                    }
                                }
                                else
                                {
                                    String responder = String.valueOf(countr.size());
                                    textAvailable.setText("Available responder/s: " + responder);
                                }
                            }
                        }

                        for(DataSnapshot Snapshot: snapshot.child("Users").getChildren())
                        {
                            String checkID = Snapshot.getKey();

                            if(userId == checkID)
                            {
                                long currentTime = System.currentTimeMillis();

                                long thirtyMinutesInMillis = 30 * 60 * 1000;

                                if(Snapshot.child("Report").hasChild("TimeStamp"))
                                {
                                    long timeStamp = Snapshot.child("Report").child("TimeStamp").getValue(Long.class);

                                    if (currentTime - timeStamp >= thirtyMinutesInMillis)
                                    {
                                        firebaseDatabase.getReference().child("Users").child(userId).child("Report").child("Location").removeValue();
                                    }
                                }
                            }

                            for(DataSnapshot Snapshot2: Snapshot.child("Responders").getChildren())
                            {
                                if(userId == checkID)
                                {
                                    String responderID = Snapshot2.getKey();
                                    Boolean status = Snapshot2.child("Status").getValue(Boolean.class);

                                    long currentTime = System.currentTimeMillis();

                                    if(Snapshot2.hasChild("TimeStamp"))
                                    {
                                        long timeStamp = Snapshot2.child("TimeStamp").getValue(Long.class);
                                        long checkTime = 30 * 60 * 1000;

                                        if(status == true && currentTime - timeStamp <= checkTime)
                                        {
                                            responderCounter.add(String.valueOf(status));

                                            String responderCount = String.valueOf(responderCounter.size());
                                            textResponders.setText("Responder/s: " + responderCount);
                                        }
                                        else
                                        {
                                            String responderCount = String.valueOf(responderCounter.size());
                                            textResponders.setText("Responder/s: " + responderCount);
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

    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showAddress()
    {
        whenTyping();

        Bundle address = getArguments();

        String getAddress = address.getString("getaddress");

        userL.setText(getAddress);
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

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        switch (v.getId())
        {

        }
        return false;
    }
}
