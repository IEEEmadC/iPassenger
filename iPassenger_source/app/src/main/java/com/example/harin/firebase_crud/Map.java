package com.example.harin.firebase_crud;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.*;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Map extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LatLng myLocation;

    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private boolean setCamera=true;
    private boolean sendCoordinates=false;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private Intent intent;
    private String mobile;

    private Switch switch_visibility;
    private Button btn_logout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Set database
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("/users/" + mobile + "/location/");

        switch_visibility=(Switch)findViewById(R.id.switch_visibility);
        btn_logout=(Button)findViewById(R.id.btn_logout);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        intent=getIntent();
        mobile=intent.getStringExtra("mobile");

        //Set switch
        switch_visibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sendCoordinates=true;
                    switch_visibility.setText("Online");
                    Log.d("msg:","sendCoordinates=true");
                }else{
                    sendCoordinates=false;
                    switch_visibility.setText("Go Online");
                    Log.d("msg:","sendCoordinates=false");
                }
                reference.child("visibility").setValue(sendCoordinates);
            }
        });

        //Set logout
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCoordinates=false;
                reference.child("visibility").setValue(sendCoordinates);

                Intent intent=new Intent(getApplicationContext(),Login.class);
                startActivity(intent);
                finish();
            }
        });

        //check permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            Log.d("msg:","permission requested");
        }else {
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<android.location.Location>() {
                @Override
                public void onSuccess(android.location.Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        mMap.clear();
                        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(myLocation).title("You'r here"));
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Location is unavailable", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
            });
        }

        //set callback
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                //Set database values
                for (android.location.Location location : locationResult.getLocations()) {
                    // Set current location on the map
                    mMap.clear();
                    myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(myLocation).title("You'r here").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));

                    //Set camera focus once on the map
                    if(setCamera){
                        mMap.clear();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                        mMap.addMarker(new MarkerOptions().position(myLocation).title("You'r here").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus))).showInfoWindow();
                        setCamera=false;
                    }

                    //Send coordinates to fire-base database
                    reference.child("visibility").setValue(sendCoordinates);
                    if(sendCoordinates) {
                        reference.child("latitude").setValue(location.getLatitude());
                        reference.child("longitude").setValue(location.getLongitude());
                    }
                }
            };
        };
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCamera=true;
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        Log.d("msg:","inside startLocationUpdates()");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            Log.d("msg:","permission requested");
        }else {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
            Log.d("msg:","requestLocationUpdates");
        }
    }

    public void getLastLocation() {
        Log.d("msg:","inside getLocation()");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            Log.d("msg:","permission requested");
        }else {
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(myLocation).title("You'r here"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                        Log.d("msg:", "location available :" + location);
                    } else {
                        Log.d("msg:", "location unavailable");
                    }
                }
            });
            Log.d("msg:", "permission granted");
        }
    }
}
