package com.example.dispatchmain;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ALL")
public class FragmentMap extends Fragment implements OnMapReadyCallback, View.OnClickListener, AdapterView.OnItemClickListener, GoogleMap.OnPolylineClickListener
{
    FirebaseAuth auth = FirebaseAuth.getInstance();

    String API_KEY = "AIzaSyAP-IkIEmtq7bvYGusX6kaICdpcytjFgOU";

    private SupportMapFragment mapFragment;

    private final int GPS_REQUEST_CODE = 9001;
    boolean isPermissionGranted;
    private FusedLocationProviderClient locationClient;
    private GoogleMap mGoogleMap;
    private PlacesClient placesClient;
    private GeoApiContext mGeoApiContext;

    int PROXIMITY_RADIUS = 5000;

    protected LatLng start = null;
    protected LatLng end = null;

    public Marker pMarker;
    private Marker eMarker;

    private ArrayList<Marker> currentMarker = new ArrayList<>();
    private ArrayList<Marker> placeMarker = new ArrayList<>();
    private ArrayList<Marker> fighterMarker = new ArrayList<>();
    private List<String> responder = new ArrayList<>();

    private ArrayList<String>   placeArray,
                                responderArray;

    private ArrayAdapter<String>    placeAdapter,
                                    responderAdapter;

    public View map;

    ImageView   findStations,
                mapMenu;

    Circle userLocationAccuracyCircle;

    private ListView    searhPlace,
                        responderList;

    private BottomSheetBehavior bottomSheetSearch,
                                bottomResponders;

    private View    bottomSheetView,
                    responderView;

    private ArrayList<Polyline> polylineOnmap = new ArrayList<>();
    private ArrayList<zPolylineData> mPolyLinesData = new ArrayList<>();
    private zPolylineData polylineData;
    private Polyline polyline = null;

    EditText search;

    TextInputLayout searchLayout;

    TextView    textMapResponders,
                textFindStations,
                textTapIcon;

    CardView    cardNearby,
                cardRespond,
                cardCurrent,
                cardSeeList,
                cardStations;

    Drawable currentDrawable;

    Bitmap currentBitmap;

    zCalculations calculate;

    public FragmentMap()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        locationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (!Places.isInitialized())
        {
            Places.initialize(requireContext(), API_KEY);
        }
        placesClient = Places.createClient(requireContext());

        if(mGeoApiContext == null)
        {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(API_KEY)
                    .build();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        map = inflater.inflate(R.layout.fragment_map, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        if(mapFragment != null)
        {
            events();
            getResponders();
            getNearby();
        }

        return map;
    }

    public void events()
    {
        calculate               = new zCalculations(map.getContext());

        textMapResponders       = map.findViewById(R.id.textMapResponders);
        textFindStations        = map.findViewById(R.id.textFindStations);

        textTapIcon             = map.findViewById(R.id.textTapIcon);
        textTapIcon             .setVisibility(View.INVISIBLE);

        findStations            = map.findViewById(R.id.findStations);

        cardNearby              = map.findViewById(R.id.cardNearby);
        cardRespond             = map.findViewById(R.id.cardRespond);
        cardCurrent             = map.findViewById(R.id.cardCurrent);
        cardSeeList             = map.findViewById(R.id.cardSeeList);
        cardStations            = map.findViewById(R.id.cardStations);

        cardSeeList             .setVisibility(View.INVISIBLE);
        cardStations            .setVisibility(View.INVISIBLE);

        search                  = map.findViewById(R.id.searchPlace);

        mapMenu                 = map.findViewById(R.id.mapMenu);

        currentDrawable         = mapMenu.getDrawable();
        currentBitmap           = drawableToBitmap(currentDrawable);

        mapMenu                 .setOnClickListener(this);

        map                     .findViewById(R.id.currentLocation) .setOnClickListener(this);
        map                     .findViewById(R.id.seeResponders)   .setOnClickListener(this);

        searhPlace              = map.findViewById(R.id.listSearch);
        responderList           = map.findViewById(R.id.listResponders);

        searhPlace              .setOnItemClickListener(this);
        responderList           .setOnItemClickListener(this);

        searchLayout            = map.findViewById(R.id.layoutSearch);

        bottomSheetView         = map.findViewById(R.id.searchSheet);
        bottomSheetSearch       = BottomSheetBehavior.from(bottomSheetView);

        responderView           = map.findViewById(R.id.sheetResponders);
        bottomResponders        = BottomSheetBehavior.from(responderView);

        placeArray              = new ArrayList<>();
        responderArray          = new ArrayList<>();

        placeAdapter            = new ArrayAdapter<>(getContext(), R.layout.list_search, R.id.searchList, placeArray);
        responderAdapter        = new ArrayAdapter<>(getContext(), R.layout.list_responder, R.id.adapterResponders, responderArray);

        searhPlace              .setAdapter(placeAdapter);
        responderList           .setAdapter(responderAdapter);

        bottomSheetSearch       .setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                switch (newState)
                {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    {
                        cardNearby      .setVisibility(View.INVISIBLE);
                        cardRespond     .setVisibility(View.INVISIBLE);
                        cardCurrent     .setVisibility(View.INVISIBLE);

                        textTapIcon     .setVisibility(View.VISIBLE);
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
                        cardNearby      .setVisibility(View.VISIBLE);
                        cardRespond     .setVisibility(View.VISIBLE);
                        cardCurrent     .setVisibility(View.VISIBLE);

                        textTapIcon     .setVisibility(View.INVISIBLE);
                    }
                    break;
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        bottomResponders        .setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
                switch (newState)
                {
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    {
                        cardNearby      .setVisibility(View.INVISIBLE);
                        cardRespond     .setVisibility(View.INVISIBLE);
                        cardCurrent     .setVisibility(View.INVISIBLE);
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
                        cardNearby      .setVisibility(View.VISIBLE);
                        cardRespond     .setVisibility(View.VISIBLE);
                        cardCurrent     .setVisibility(View.VISIBLE);

                        if(mPolyLinesData.size() > 0)
                        {
                            for(zPolylineData polylineData: mPolyLinesData)
                            {
                                polylineData.getPolyline().remove();
                            }
                            mPolyLinesData.clear();
                            mPolyLinesData = new ArrayList<>();
                        }
                    }
                    break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {
                FrameLayout frameLayout = map.findViewById(R.id.FrameMap);
                frameLayout.setTranslationY(-slideOffset * bottomSheet.getHeight()/2.5F);
            }
        });

        search.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(placeArray.isEmpty())
                {
                    bottomSheetSearch.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                else
                {
                    bottomSheetSearch.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

                RectangularBounds bounds = RectangularBounds.newInstance
                        (
                                new LatLng(5, 114),
                                new LatLng(21, 127));

                FindAutocompletePredictionsRequest builder = FindAutocompletePredictionsRequest.builder()
                        .setLocationBias(bounds)
                        .setCountry("PH")
                        .setSessionToken(token)
                        .setQuery(search.getText().toString())
                        .build();

                placesClient.findAutocompletePredictions(builder).addOnSuccessListener(response ->
                {
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions())
                    {
                        placeArray.add(String.valueOf(prediction.getFullText(null)));
                        placeAdapter.notifyDataSetChanged();
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
                placeArray.clear();
                placeAdapter.notifyDataSetChanged();
            }
        });

        searchLayout.setEndIconOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(placeMarker != null)
                {
                    for(Marker marker:placeMarker)
                    {
                        marker.remove();
                    }
                }

                cardNearby.setVisibility(View.VISIBLE);
                cardRespond.setVisibility(View.VISIBLE);
                cardCurrent.setVisibility(View.VISIBLE);

                String locationDest = search.getText().toString();

                hideKeyboard(view);

                List<Address> addressList = null;

                if (!locationDest.isEmpty())
                {
                    Geocoder geocoder = new Geocoder(map.getContext());

                    for(int i = 0; i < placeArray.size(); i++)
                    {
                        String invoke = placeArray.get(i).toString();

                        try
                        {
                            addressList = geocoder.getFromLocationName(invoke, 1);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                        if(addressList != null)
                        {
                            Address address = addressList.get(0);

                            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                            pMarker = mGoogleMap.addMarker(markerOptions);
                            placeMarker.add(pMarker);

                            if(i == 0)
                            {
                                CameraUpdate gotoSearch = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                                mGoogleMap.animateCamera(gotoSearch);
                            }
                        }
                        else
                        {
                            Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    search.setText("");
                }
                else
                {
                    Toast.makeText(requireContext(), "Type your destination", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private final LocationCallback locationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult)
        {
            if(mGoogleMap != null)
            {
                Location location = locationResult.getLastLocation();


                saveLocation(location.getLatitude(), location.getLongitude());
            }
        }
    };

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        mGoogleMap      = googleMap;
        mGoogleMap      .setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap      .getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap      .getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap      .getUiSettings().setCompassEnabled(false);
        mGoogleMap      .setBuildingsEnabled(false);
        mGoogleMap      .setMinZoomPreference(11);
        mGoogleMap      .setMyLocationEnabled(true);
        mGoogleMap      .setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle));
        locationClient  .getLastLocation()
                        .addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Location location           = task.getResult();
                LatLng LatLng               = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate gotoLocation   = CameraUpdateFactory.newLatLngZoom(LatLng, 20);
                mGoogleMap                  .moveCamera(gotoLocation);
            }
        });
    }

    private void CurrentLocation()
    {
        locationClient.getLastLocation().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Location location       = task.getResult();
                gotoLocation(location   .getLatitude(),
                            location    .getLongitude());
            }
        });
    }

    private void gotoLocation(double latitude, double longitude)
    {
        locationClient.getLastLocation().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Location location           = task.getResult();
                LatLng LatLng               = new LatLng(latitude, longitude);
                CameraUpdate gotoLocation   = CameraUpdateFactory.newLatLngZoom(LatLng, 20);
                mGoogleMap                  .animateCamera(gotoLocation);
            }
        });
    }

    public void saveLocation(double latitude, double longitude)
    {
        DatabaseReference mDatabase;

        mDatabase           = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        HashMap User        = new HashMap();
        String userId       = auth.getCurrentUser().getUid();
        User                .put("lat", latitude);
        User                .put("lng", longitude);
        mDatabase           .child("Users")
                            .child(userId)
                            .updateChildren(User);
        mDatabase           .keepSynced(true);
    }

    public void checkMyPermission()
    {
        Dexter.withContext(map.getContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse)
            {
                Toast.makeText(map.getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermissionGranted = true;
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse)
            {
                isPermissionGranted     = false;
                Intent intent           = new Intent();
                intent                  .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri           .fromParts("package", getActivity().getPackageName(), "");
                intent                  .setData(uri);
                startActivity(intent);
            }
            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }

    public boolean isGpsenabled()
    {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        boolean providerEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (providerEnable)
        {
            return true;
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(map.getContext())
                    .setTitle("GPS Permission").setMessage("GPS require, Please enable")
                    .setPositiveButton("Yes", ((dialogInterface, i) ->
                    {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);

                    })).setCancelable(false).show();
        }

        return false;
    }

    private void changeText(String text, String find)
    {
        String nearby = "Fire station/s nearby: " + text;
        String respond = "Responder/s: " + text;

        SpannableString spannableString = new SpannableString(nearby);
        ForegroundColorSpan nearbySpan = new ForegroundColorSpan(getActivity().getColor(R.color.LineDirection));

        SpannableString spannableRespond = new SpannableString(respond);
        ForegroundColorSpan respondSpan = new ForegroundColorSpan(getActivity().getColor(R.color.LineDirection));

        int nearbyStartIndex = nearby.indexOf(text);
        int nearbyEndIndex = nearbyStartIndex + text.length();
        int respondStartIndex = respond.indexOf(text);
        int respondEndIndex = respondStartIndex + text.length();

        spannableString.setSpan(nearbySpan, nearbyStartIndex, nearbyEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableRespond.setSpan(respondSpan, respondStartIndex, respondEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (find.equals("nearby")) {
            textFindStations.setText(spannableString);
        }
        if (find.equals("respond")) {
            textMapResponders.setText(spannableRespond);
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        if (mapFragment != null)
        {
            mapFragment.onResume();
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10000)
                    .setFastestInterval(5000);
            locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mapFragment != null)
        {
            mapFragment.onPause();
            locationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mapFragment != null)
        {
            mapFragment.onDestroy();
        }
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();

        if (mapFragment != null)
        {
            mapFragment.onLowMemory();

            for(Marker marker:respondMarkers)
            {
                marker.setVisible(false);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.currentLocation:

                if(currentMarker != null)
                {
                    removePreviousMarker();
                }

                if(placeMarker != null)
                {
                    for(Marker marker:placeMarker)marker.remove();
                }

                CurrentLocation();

                if(search.isFocused())
                {
                    search.clearFocus();
                }

                break;
            case R.id.seeResponders:

                if(!responderArray.isEmpty())
                {
                    bottomResponders.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else
                {
                    Toast.makeText(requireContext(), "No responders", Toast.LENGTH_SHORT).show();
                }

                if(search.isFocused())
                {
                    search.clearFocus();
                }
                break;
            case R.id.mapMenu:

                if(cardStations.getVisibility() == View.INVISIBLE)
                {
                    cardStations    .setVisibility(View.VISIBLE);
                    cardSeeList     .setVisibility(View.VISIBLE);

                    Drawable newDrawable =
                            getResources().getDrawable(R.drawable.arrow_back);

                    mapMenu.setImageDrawable(newDrawable);
                }

                else
                {
                    cardStations    .setVisibility(View.INVISIBLE);
                    cardSeeList     .setVisibility(View.INVISIBLE);

                    Bitmap newBitmap = currentBitmap;

                    mapMenu.setImageBitmap(newBitmap);
                }

                if(search.isFocused())
                {
                    search.clearFocus();
                }
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId())
        {
            case R.id.listSearch:
                search.setText(parent.getAdapter().getItem(position).toString());
                break;
            case R.id.listResponders:
                LatLng markerLoc = new LatLng(respondMarkers.get(position).getPosition().latitude, respondMarkers.get(position).getPosition().longitude);

                calculateDirections(markerLoc, incidentLocation);

                break;
        }
    }

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


                    }
                    else
                    {

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

    Location fighterLocation;
    Location fighterLoc;

    LatLng incidentLocation;
    LatLng fighterMark;

    Map<String, Marker> markerMap = new HashMap<>();
    List<Marker> respondMarkers = new ArrayList<>();

    LatLngBounds.Builder builder = new LatLngBounds.Builder();

    public void getResponders()
    {
        locationClient.getLastLocation().addOnCompleteListener(task -> {

            if(task.isSuccessful())
            {
                Location location = task.getResult();
                LatLng myMark = new LatLng(location.getLatitude(), location.getLongitude());

                builder.include(myMark);
            }

        });
        String userId = auth.getCurrentUser().getUid();

        FirebaseDatabase firebaseDatabase;
        DatabaseReference databaseReference;
        firebaseDatabase = FirebaseDatabase.getInstance("https://dispatchmain-22ce5-default-rtdb.asia-southeast1.firebasedatabase.app/");
        databaseReference = firebaseDatabase.getReference();

        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(!responder.isEmpty())
                {
                    responder.clear();
                }

                if(!responderArray.isEmpty())
                {
                    responderArray.clear();
                    responderAdapter.notifyDataSetChanged();
                }
                    for(DataSnapshot users: snapshot.child("Users").getChildren())
                    {
                        String userIDs = users.getKey();

                        if(userIDs == userId)
                        {
                            if(users.child("Report").hasChild("Location"))
                            {
                                String fireLocation = users.child("Report").child("Location").getValue(String.class);
                                LatLng fireMark = getCalculations(fireLocation);

                                mGoogleMap.addMarker(new MarkerOptions()
                                        .position(fireMark)
                                        .title(fireLocation)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.fireicon)));
                            }

                            for(DataSnapshot responders: users.child("Responders").getChildren())
                            {
                                Boolean status = responders.child("Status").getValue(Boolean.class);

                                String responderID = responders.getKey();

                                String show = String.valueOf(status);

                                long currentTime = System.currentTimeMillis();

                                if(responders.hasChild("TimeStamp"))
                                {
                                    long timeStamp = responders.child("TimeStamp").getValue(Long.class);

                                    long checkTime = 30 * 60 * 1000;

                                    if(status == true && currentTime - timeStamp <= checkTime)
                                    {
                                        Marker marker;

                                        if(users.child("Report").hasChild("Location"))
                                        {
                                            String iLocation = users.child("Report").child("Location").getValue(String.class);

                                            incidentLocation = getCalculations(iLocation);

                                            fighterLocation = new Location("service Provider");
                                            fighterLocation.setLatitude(incidentLocation.latitude);
                                            fighterLocation.setLongitude(incidentLocation.longitude);

                                            Double lat = snapshot.child("Firefighter").child(responderID).child("lat").getValue(Double.class);
                                            Double lng = snapshot.child("Firefighter").child(responderID).child("lng").getValue(Double.class);
                                            Float bearing = snapshot.child("Firefighter").child(responderID).child("bearing").getValue(Float.class);

                                            fighterMark = new LatLng(lat, lng);

                                            fighterLoc = new Location("service Provider");
                                            fighterLoc.setLatitude(lat);
                                            fighterLoc.setLongitude(lng);

                                            if(bearing != null)
                                            {
                                                fighterLoc.setBearing(bearing);
                                            }

//                                    if(calculateDistance(fighterMark, userMark) > 585 && calculateDistance(fighterMark, userMark) < 600)
//                                    {
//                                        String nameFighter = snapshot.child("Firefighter").child(responderID).child("firstName").getValue(String.class);
//                                        Toast.makeText(requireContext(), "Firefighter: " + nameFighter + " is nearby...", Toast.LENGTH_SHORT).show();
//                                    }

                                            if (markerMap.get(responderID) != null && markerMap.get(responderID).isVisible())
                                            {
                                                markerMap.get(responderID).setPosition(fighterMark);
                                                markerMap.get(responderID).setRotation(fighterLoc.getBearing());
                                            }
                                            else
                                            {
                                                marker = mGoogleMap.addMarker(new MarkerOptions()
                                                        .position(fighterMark)
                                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.firetruck))
                                                        .rotation(fighterLoc.getBearing())
                                                        .anchor((float) 0.5, (float) 0.5));
                                                markerMap.put(responderID, marker);
                                                respondMarkers.add(marker);
                                            }

                                            String name = "Name: " +
                                                    snapshot.child("Firefighter").child(responderID).child("lastName").getValue(String.class);
                                            String station = "Station Address: " +
                                                    snapshot.child("Firefighter").child(responderID).child("station").getValue(String.class);
                                            String telephone = "Telephone Number: " +
                                                    snapshot.child("Firefighter").child(responderID).child("telephone").getValue(String.class);
                                            String phone = "Mobile Number: " +
                                                    snapshot.child("Firefighter").child(responderID).child("phone").getValue(String.class);

                                            responderArray.add(name +
                                                    "\n" + station +
                                                    "\n" + telephone +
                                                    "\n" + phone);
                                            responderAdapter.notifyDataSetChanged();

                                            responder.add(String.valueOf(status));

                                            String responderCount = String.valueOf(responder.size());
                                            changeText(responderCount, "respond");
                                        }
                                    }
                                }
                                else
                                {
                                    if(markerMap.get(responderID) != null)
                                    {
                                        markerMap.get(responderID).setVisible(false);
                                        markerMap.get(responderID).remove();
                                    }

                                    String responderCount = String.valueOf(responder.size());
                                    changeText(responderCount, "respond");
                                }
                            }
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
        databaseReference.keepSynced(true);
    }

    public void blinkMarker()
    {

    }

    public LatLng getCalculations(String placeName)
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

    public class NearbyStation extends AsyncTask<Object, String, String>
    {
        private String googlePlacesData;
        String url;

        @Override
        protected String doInBackground(Object... objects)
        {
            GoogleMap mMap = (GoogleMap) objects[0];
            url = (String)objects[1];

            zDownloadUrl zDownloadURL = new zDownloadUrl();
            try
            {
                googlePlacesData = zDownloadURL.readUrl(url);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String s)
        {
            List<HashMap<String, String>> nearbyPlaceList;
            zDataParser parser = new zDataParser();
            nearbyPlaceList = parser.parse(s);
            showNearbyPlaces(nearbyPlaceList);
        }
    }

    private String getUrlPlace(double latitude, double longitude, String nearbyPlace)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("&location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key=" + API_KEY);

        return googlePlaceUrl.toString();
    }

    public void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {
        String stationCount = String.valueOf(nearbyPlaceList.size());

        changeText(stationCount, "nearby");

        findStations.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LatLng first = null;
                LatLng last = null;

                if(currentMarker != null)
                {
                    removePreviousMarker();
                }

                for(int i = 0; i < nearbyPlaceList.size(); i++)
                {
                    HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                    String placeName = googlePlace.get("place_name");
                    String vicinity = googlePlace.get("vicinity");
                    String placeID = googlePlace.get("reference");
                    double lat = Double.parseDouble(Objects.requireNonNull(googlePlace.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(googlePlace.get("lng")));

                    last = new LatLng(lat, lng);

                    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(last)
                            .title(placeName)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.fire_station)));
                    currentMarker.add(marker);

                    if(i == 0)
                    {
                        first = new LatLng(lat, lng);
                    }
                }

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(first);
                builder.include(last);
                LatLngBounds bounds = builder.build();

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,200));

                if(search.isFocused())
                {
                    search.clearFocus();
                }
            }
        });
    }

    public void getNearby()
    {
        locationClient.getLastLocation().addOnCompleteListener(task ->
        {
            if (task.isSuccessful()) {
                Location location = task.getResult();
                Object[] dataTransfer = new Object[2];

                NearbyStation getData = new NearbyStation();

                String station = "fire_station";
                String url = getUrlPlace(location.getLatitude(), location.getLongitude(), station);
                dataTransfer[0] = mGoogleMap;
                dataTransfer[1] = url;

                getData.execute(dataTransfer);
            }
        });
    }

    public void calculateDirections(LatLng origin, LatLng end)
    {
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.alternatives(true)
                .mode(TravelMode.DRIVING)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(end.latitude, end.longitude))
                .setCallback(new PendingResult.Callback<DirectionsResult>()
                {
                    @Override
                    public void onResult(DirectionsResult result)
                    {
                        addPolylines(result);
                    }

                    @Override
                    public void onFailure(Throwable e) {

                    }
                });
    }

    public void addPolylines(final DirectionsResult result)
    {

        new Handler(Looper.getMainLooper()).post(new Runnable()
        {
            @Override
            public void run()
            {
                if(mPolyLinesData.size() > 0)
                {
                    for(zPolylineData polylineData: mPolyLinesData)
                    {
                        polylineData.getPolyline().remove();
                    }
                    mPolyLinesData.clear();
                    mPolyLinesData = new ArrayList<>();
                }

                double duration = 999999999;
                double tempDuration = 0;

                for(DirectionsRoute route: result.routes)
                {
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    for(com.google.maps.model.LatLng latLng: decodedPath)
                    {
                        newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                    }

                    tempDuration = route.legs[0].duration.inSeconds;

                    if (tempDuration < duration)
                    {
                        duration = tempDuration;

                        polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                        polyline.setWidth(25);
                        polyline.setClickable(true);

                        polylineOnmap.add(polyline);
                        mPolyLinesData.add(new zPolylineData(polyline, route.legs[0], route.legs[0].steps[0]));
                        polylineData = new zPolylineData(polyline, route.legs[0], route.legs[0].steps[0]);

                        onPolylineClick(polyline);
                    }
                }

                List<LatLng> points = polyline.getPoints();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (LatLng point : points)
                {
                    builder.include(point);
                }

                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 250);
                mGoogleMap.animateCamera(cameraUpdate);
            }
        });
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline)
    {
        for(zPolylineData polylineData: mPolyLinesData)
        {
            if(polyline.getId().equals(polylineData.getPolyline().getId()))
            {
                polylineData.getPolyline().setColor(getResources().getColor(R.color.LineDirection));
                polylineData.getPolyline().setZIndex(1);;

                String distance = String.valueOf(polylineData.getLeg().distance);
                String duration = String.valueOf(polylineData.getLeg().duration);
            }
        }
    }

    private Bitmap drawableToBitmap(Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void removePreviousMarker()
    {
        for(Marker marker: currentMarker)
            marker.remove();
    }

}
