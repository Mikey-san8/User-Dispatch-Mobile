package com.example.dispatchmain;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class zCalculations
{
    private final Context context;

    public zCalculations(Context context) {
        this.context = context;
    }

    public LatLng getCalculations(String placeName, String ID)
    {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

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

    public double calculateDistance(LatLng loc1, LatLng loc2)
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
}
