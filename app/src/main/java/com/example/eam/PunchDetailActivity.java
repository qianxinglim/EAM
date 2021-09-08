package com.example.eam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.eam.databinding.ActivityPunchDetailBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PunchDetailActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "PunchDetailActivity";
    private ActivityPunchDetailBinding binding;
    private double latitude, longitude;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_punch_detail);

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");

        if(type.equals("clockIn")){
            binding.tvDate.setText(intent.getStringExtra("clockInDate"));
            binding.tvTime.setText(intent.getStringExtra("clockInTime"));

            latitude = intent.getDoubleExtra("clockInLat", 0.0);
            longitude = intent.getDoubleExtra("clockInLong", 0.0);

            displayLocation();
        }
        else{
            if(intent.getStringExtra("clockOutDate") == null){
                binding.tvDate.setText("No record");
            }else{
                binding.tvDate.setText(intent.getStringExtra("clockOutDate"));
                binding.tvTime.setText(intent.getStringExtra("clockOutTime"));

                latitude = intent.getDoubleExtra("clockOutLat", 0.0);
                longitude = intent.getDoubleExtra("clockOutLong", 0.0);

                displayLocation();
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void displayLocation(){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String address = addresses.get(0).getAddressLine(0);
            binding.tvAddress.setText(address);

            Log.d(TAG, "Lat: " + latitude + ", Long: " + longitude + ", Address: " + address);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        LatLng latlng = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(latlng));
        //map.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 16.0f));
    }
}