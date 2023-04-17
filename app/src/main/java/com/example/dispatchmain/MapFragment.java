package com.example.dispatchmain;

import static android.content.Context.LOCATION_SERVICE;
import static com.android.volley.VolleyLog.setTag;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

@SuppressWarnings("ALL")
public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, AdapterView.OnItemClickListener
{
    private static final String TAG = "MapFragment";

    FirebaseAuth auth = FirebaseAuth.getInstance();
    String API_KEY = "AIzaSyDKHY_L76RS3qMCyDSctItyGycxGAwBK8U";

    private SupportMapFragment mapFragment;

    private final int GPS_REQUEST_CODE = 9001;
    boolean isPermissionGranted;
    private FusedLocationProviderClient locationClient;
    private GoogleMap mGoogleMap;
    private PlacesClient placesClient;

    int PROXIMITY_RADIUS = 50000;

    protected LatLng start = null;
    protected LatLng end = null;

    public Marker pMarker;
    private Marker eMarker;
    private ArrayList<Marker> currentIcon = new ArrayList<>();
    private ArrayList<Marker> placeMarker = new ArrayList<>();

    private ArrayList<String> placeArray;

    private ArrayAdapter<String> placeAdapter;

    public View map;

    ImageView currentClick;

    Circle userLocationAccuracyCircle;

    private ListView searhPlace;
    private BottomSheetBehavior bottomSheetSearch;
    private View bottomSheetView;

    EditText search;
    TextInputLayout searchLayout;

    public MapFragment()
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        map = inflater.inflate(R.layout.activity_maps, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        if (getTag() == null) {
            setTag(TAG);
        }

        events();

        return map;
    }

    public void events()
    {
        search = map.findViewById(R.id.searchPlace);

        map.findViewById(R.id.currentLocation).setOnClickListener(this);

        searhPlace = map.findViewById(R.id.listSearch);

        searhPlace.setOnItemClickListener(this);

        searchLayout = map.findViewById(R.id.layoutSearch);

        bottomSheetView = map.findViewById(R.id.searchSheet);
        bottomSheetSearch = BottomSheetBehavior.from(bottomSheetView);

        placeArray = new ArrayList<>();

        placeAdapter = new ArrayAdapter<>(getContext(), R.layout.search_list, R.id.searchList, placeArray);

        searhPlace.setAdapter(placeAdapter);

        bottomSheetSearch.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                bottomSheetSearch.setState(BottomSheetBehavior.STATE_EXPANDED);
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
                    for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
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
            public void afterTextChanged(Editable editable) {

                placeAdapter.clear();
            }
        });

        searchLayout.setEndIconOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String locationDest = search.getText().toString();

                hideKeyboard(view);

                List<Address> addressList = null;

                if (!locationDest.isEmpty())
                {
                    Geocoder geocoder = new Geocoder(map.getContext());
                    bottomSheetSearch.setState(BottomSheetBehavior.STATE_COLLAPSED);

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
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.getUiSettings().setCompassEnabled(false);
        mGoogleMap.setBuildingsEnabled(false);
        mGoogleMap.setMinZoomPreference(11);
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle));
        locationClient.getLastLocation().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Location location = task.getResult();
                LatLng LatLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate gotoLocation = CameraUpdateFactory.newLatLngZoom(LatLng, 20);
                mGoogleMap.animateCamera(gotoLocation);
            }
        });
    }

    private void CurrentLocation()
    {
        locationClient.getLastLocation().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Location location = task.getResult();
                gotoLocation(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private void gotoLocation(double latitude, double longitude)
    {
        locationClient.getLastLocation().addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Location location = task.getResult();
                LatLng LatLng = new LatLng(latitude, longitude);
                CameraUpdate gotoLocation = CameraUpdateFactory.newLatLngZoom(LatLng, 20);
                mGoogleMap.animateCamera(gotoLocation);
            }
        });
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
                isPermissionGranted = false;
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), "");
                intent.setData(uri);
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
    public void onDestroy() {
        super.onDestroy();
        if (mapFragment != null) {
            mapFragment.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapFragment != null) {
            mapFragment.onLowMemory();
        }
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.currentLocation:
                CurrentLocation();
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

    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
