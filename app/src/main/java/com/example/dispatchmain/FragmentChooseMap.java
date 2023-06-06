package com.example.dispatchmain;

import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("ALL")
public class FragmentChooseMap extends Fragment implements OnMapReadyCallback {

    private FusedLocationProviderClient locationClient;
    private GoogleMap mGoogleMap;
    private Geocoder geocoder;
    private Marker draggedMarker;
    private boolean isMarkerDragging = false;
    private LatLng previousMarkerPosition;

    private static final float DEFAULT_ZOOM = 20f;
    private static final long ANIMATION_DURATION = 500; // Animation duration in milliseconds

    private Handler handler;
    private Runnable markerAnimationRunnable;

    View choose;

    SupportMapFragment mapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        choose = inflater.inflate(R.layout.fragment_choosemap, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapChoose);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(requireContext(), Locale.getDefault());

        return choose;
    }

    LatLng draggedMarkerPosition;

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;

        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener()
        {
            @Override
            public void onCameraMoveStarted(int i)
            {
                isMarkerDragging = true;
            }
        });

        mGoogleMap.setOnCameraMoveListener(() ->
        {
            if (isMarkerDragging)
            {
                draggedMarker.setPosition(mGoogleMap.getCameraPosition().target);
                mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        draggedMarkerPosition = mGoogleMap.getCameraPosition().target;
                        getAddressFromLocation(draggedMarkerPosition.latitude, draggedMarkerPosition.longitude);
                    }
                });
            }
        });

        enableMyLocation();
    }

    private void enableMyLocation()
    {
        locationClient.getLastLocation().addOnCompleteListener(task -> {

            if (task.isSuccessful())
            {
                Location location = task.getResult();

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                draggedMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
                previousMarkerPosition = latLng;
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
        });
    }

    private void getAddressFromLocation(double latitude, double longitude)
    {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                FragmentHome homeFragment = (FragmentHome) getParentFragmentManager().findFragmentByTag("home_fragment");

                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);

                if (addressText != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(choose.getContext());
                    builder.setMessage("Pinned Address:\n\n" + addressText);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Bundle bundle = new Bundle();
                            bundle.putString("getaddress", addressText);
                            homeFragment.setArguments(bundle);

                            homeFragment.bottomSheetCall.setDraggable(true);

                            getFragmentManager().popBackStack();

                            homeFragment.showAddress();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final LocationCallback locationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult)
        {
            if(mGoogleMap != null)
            {
                Location location = locationResult.getLastLocation();

            }
        }
    };

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
}




