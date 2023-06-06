package com.example.dispatchmain;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        choose = inflater.inflate(R.layout.fragment_choosemap, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapChoose);
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
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null)
        {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            draggedMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
            previousMarkerPosition = latLng;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        }
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
}




