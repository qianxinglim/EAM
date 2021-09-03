package com.example.eam.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService {
    protected Context context;
    public static double latitude, longitude;
    public static String address;
    private FusedLocationProviderClient fusedLocationProviderClient;

    public LocationService(Context context){
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            android.location.Location location = task.getResult();

            if (location != null) {

                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    latitude = addresses.get(0).getLatitude();
                    longitude = addresses.get(0).getLongitude();
                    address = addresses.get(0).getAddressLine(0);

                    //Log.d(TAG, "Lat: " + latitude + ", Long: " + longitude + ", Address: " + address);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }



}
