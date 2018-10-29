package com.example.harin.firebase_crud;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Locate extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    boolean setCamera=true;
    boolean threadState=true;

    private LocationCallback mLocationCallback;
    private LatLng passengerLocation=new LatLng(6.898899,79.860494 );;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;

    private Button btn_search_route;
    private EditText et_route;
    private String route="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locate);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Database instance
        database=FirebaseDatabase.getInstance();
        reference=database.getReference("/users");

        btn_search_route=(Button) findViewById(R.id.btn_search_route);
        et_route=(EditText) findViewById(R.id.et_route);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //map update every 5 second
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(threadState) {
                    locateBuses();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //setting up route search button
        btn_search_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("msg:","inside btn_search_route.setOnClickListener:"+route);
                route=et_route.getText().toString();
            }
        });

        //startPassengerLocationUpdates callback
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast toast=Toast.makeText(getApplicationContext(),"Turn on Location Service",Toast.LENGTH_LONG);
                    return;
                }else {
                    for (android.location.Location location : locationResult.getLocations()) {
                        // Update UI with location data
                        passengerLocation = new LatLng(location.getLatitude(), location.getLongitude());
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
        startPassengerLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadState=false;
    }

    private void startPassengerLocationUpdates() {
        Log.d("msg:","inside startLocationUpdates()");

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
            Log.d("msg:","permission requested");
        }else {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
            Log.d("msg:","requestLocationUpdates");
        }
    }

    private void locateBuses(){
        Log.d("Count " ,"inside locateBuses()");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("Count " ,""+dataSnapshot.getChildrenCount());
                mMap.clear();
                //set passengers position
                mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You'r here").icon(BitmapDescriptorFactory.fromResource(R.drawable.passenger)));
                //set camera focused on passenger once
                if(setCamera){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation, 15));
                    setCamera=false;
                }
                //set bus positions
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Log.e("msg", postSnapshot.getValue().toString());
                    //obtain values from the database
                    String route_no=postSnapshot.child("vehicle/route_no").getValue().toString();
                    String registration_no=postSnapshot.child("vehicle/registration_no").getValue().toString();
                    double lat=Double.parseDouble(postSnapshot.child("location/latitude").getValue().toString());
                    double lng=Double.parseDouble(postSnapshot.child("location/longitude").getValue().toString());
                    boolean visibility=Boolean.parseBoolean(postSnapshot.child("location/visibility").getValue().toString());
                    //set location on users map
                    //visibility on and route matches. If no route is set check all.
                    if(visibility && (route.equals("") || route.equals(route_no))){
                        LatLng myLocation = new LatLng(lat, lng);
                        mMap.addMarker(new MarkerOptions().position(myLocation).title(route_no+" | "+registration_no).icon(BitmapDescriptorFactory.fromResource(R.drawable.bus)));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Count " ,"DatabaseError");
            }
        });
    }
}
