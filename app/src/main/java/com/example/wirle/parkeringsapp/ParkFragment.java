package com.example.wirle.parkeringsapp;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class ParkFragment extends Fragment implements
        OnMapReadyCallback, View.OnClickListener {

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 18;

    private static final String POSITIONITEMKEY = "POSITIONITEMKEY";
    private static final String MARKERLATITUDEKEY = "MARKERLATITUDEKEY";
    private static final String MARKERLONGITUDEKEY = "MARKERLONGITUDEKEY";
    private static final String MARKERADDRESSKEY = "MARKERADDRESSKEY";
    private static final String CURRENTZOOMKEY = "CURRENTZOOMKEY";
    private static final String MAPVIEWLATITUDEKEY = "MAPVIEWLATITUDEKEY";
    private static final String MAPVIEWLONGITUDEKEY = "MAPVIEWLONGITUDEKEY";

    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private MarkerObject currentMarker;
    private float currentZoom = DEFAULT_ZOOM;
    private double currentLatitude;
    private double currentLongitude;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;

    private class MarkerObject implements Serializable {
        private Double latitude;
        private Double longitude;
        private String address;

        MarkerObject() {

        }

        MarkerObject(Double latitude_, Double longitude_, String address_) {
            latitude = latitude_;
            longitude = longitude_;
            address = address_;
        }

        @Override
        public String toString() {
            return latitude + ", " + longitude;
        }
    }

    public ParkFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // load marker
        if (savedInstanceState != null) {
            currentMarker = new MarkerObject(
                    savedInstanceState.getDouble(MARKERLATITUDEKEY),
                    savedInstanceState.getDouble(MARKERLONGITUDEKEY),
                    savedInstanceState.getString(MARKERADDRESSKEY)
            );

            // load last zoom level
            currentZoom = savedInstanceState.getFloat(CURRENTZOOMKEY, DEFAULT_ZOOM);

            // load last view position on map
            currentLatitude = savedInstanceState.getDouble(MAPVIEWLATITUDEKEY);
            currentLongitude = savedInstanceState.getDouble(MAPVIEWLONGITUDEKEY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save currentmarker
        if (currentMarker != null) {
            outState.putDouble(MARKERLATITUDEKEY, currentMarker.latitude);
            outState.putDouble(MARKERLONGITUDEKEY, currentMarker.longitude);
            outState.putString(MARKERADDRESSKEY, currentMarker.address);
        }

        if (mMap != null) {
            // save zoom level
            currentZoom = mMap.getCameraPosition().zoom;
            outState.putFloat(CURRENTZOOMKEY, currentZoom);

            // save view positon on map
            outState.putDouble(MAPVIEWLATITUDEKEY, mMap.getCameraPosition().target.latitude);
            outState.putDouble(MAPVIEWLONGITUDEKEY, mMap.getCameraPosition().target.longitude);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // layout
        View v = inflater.inflate(R.layout.fragment_park, container, false);

        // location service
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // fill out mapfragment in layout
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // attach click listener on togglebutton
        ToggleButton toggleButton = v.findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(this);

        // hide button if positionitem is clicked
        if (getPositionItem() != null) {
            toggleButton.setVisibility(View.INVISIBLE);
        }

        return v;
    }

    public PositionContent.PositionItem getPositionItem() {
        Bundle bundle = getArguments();
        if(bundle != null && bundle.containsKey(POSITIONITEMKEY)) {
            PositionContent.PositionItem positionItem = (PositionContent.PositionItem)
                    bundle.getSerializable(POSITIONITEMKEY);
            return positionItem;
        }
        return null;
    }

    public void savePositionToDb() throws IOException {
        // get the latest device location
        getDeviceLocation(false);

        if (mLastKnownLocation == null) {
            Toast.makeText(getActivity(),
                    "Could not find location", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || database == null) {
            return;
        }

        DatabaseReference dbRef = database.getReference(user.getUid());
        DatabaseReference newPos = dbRef.child("positions").push();
        PositionContent.PositionItem positionItem = new PositionContent.PositionItem();

        positionItem.id = newPos.getKey();
        positionItem.latitude = mLastKnownLocation.getLatitude();
        positionItem.longitude = mLastKnownLocation.getLongitude();
        positionItem.time = mLastKnownLocation.getTime();
        positionItem.accuracy = mLastKnownLocation.getAccuracy();
        positionItem.address = getAddressFromLocation(positionItem.latitude,
                positionItem.longitude);

        newPos.setValue(positionItem);

        currentMarker = new MarkerObject(positionItem.latitude, positionItem.longitude,
                positionItem.address);

        placeCurrentMarker();
    }

    private void removeCurrentMarker() {
        if (currentMarker != null) {
            mMap.clear();
            currentMarker = null;
        }
    }

    private void placeCurrentMarker() {
        if (currentMarker != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(currentMarker.latitude, currentMarker.longitude))
                    .title(currentMarker.address));
        }
    }

    private String getAddressFromLocation(Double latitude, Double longitude) throws IOException {
        // fetch address from long and lat
        Geocoder geocoder  = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> result = geocoder.getFromLocation(
                latitude, longitude, 1);

        return result.get(0).getAddressLine(0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // get the device location
        getDeviceLocation(false);

        // if positionitem is set, make it current marker and move camera to it
        PositionContent.PositionItem positionItem = getPositionItem();
        if (positionItem != null) {
            currentMarker = new MarkerObject(positionItem.latitude,
                    positionItem.longitude, positionItem.address);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(currentMarker.latitude,
                            currentMarker.longitude), currentZoom));
        }
        // move camera to the last viewposition on map
        else if (currentLatitude != 0.0f && currentLongitude != 0.0f) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(currentLatitude,
                            currentLongitude), currentZoom));
        }
        // move camera to our position
        else {
            getDeviceLocation(true);
        }

        // If marker is set, place it
        placeCurrentMarker();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation(final boolean moveCameraToLocation) {
        // try to get latest known location if locationpermission is granted
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();

                            if (moveCameraToLocation) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), currentZoom));
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View view) {
        // do what you want to do when button is clicked
        switch (view.getId()) {
            case R.id.toggleButton:
                ToggleButton toggleButton = view.findViewById(R.id.toggleButton);
                if (toggleButton.isChecked()) {
                    try {
                        savePositionToDb();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(),"Failed to save location",
                                Toast.LENGTH_SHORT).show();
                        StringWriter errors = new StringWriter();
                        e.printStackTrace(new PrintWriter(errors));
                        Log.e("ParkFragment", errors.toString());
                    }
                }
                else {
                    // remove marking on map
                    removeCurrentMarker();
                }
                break;
        }
    }
}