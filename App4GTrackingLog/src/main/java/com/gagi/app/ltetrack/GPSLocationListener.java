package com.gagi.app.ltetrack;


import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by igaglioti on 28/02/14.
 */
public class GPSLocationListener implements LocationListener {

    private static double latitude;
    private static double longitude;


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
