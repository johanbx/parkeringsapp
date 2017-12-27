package com.example.wirle.parkeringsapp;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DatabaseReference;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class ParkFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 15;

    private static final String POSITIONITEMKEY = "POSITIONITEMKEY";
    private static final String CURRENTMARKERKEY = "CURRENTMARKERKEY";
    private static final String DEBUGTAG = "ParkFragment";

    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private OnDatabaseRefListener mRefListener;

    private MarkerObject currentMarker;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    private class MarkerObject implements Serializable{
        private Double latitude;
        private Double longitude;
        private String address;
    }

    public ParkFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_park, container, false);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // attach click listener on togglebutton
        ToggleButton toggleButton = v.findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(this);

        return v;
    }

    public void savePositionToDb() throws IOException {
        DatabaseReference newPos = mRefListener.OnDatabaseRef().child("positions").push();
        PositionContent.PositionItem positionItem = new PositionContent.PositionItem();

        positionItem.id = newPos.getKey();
        positionItem.latitude = mLastKnownLocation.getLatitude();
        positionItem.longitude = mLastKnownLocation.getLongitude();
        positionItem.time = mLastKnownLocation.getTime();
        positionItem.accuracy = mLastKnownLocation.getAccuracy();
        positionItem.address = getAddressFromLocation(positionItem.latitude,
                positionItem.longitude);

        newPos.setValue(positionItem);

        currentMarker = new MarkerObject();
        currentMarker.latitude = positionItem.latitude;
        currentMarker.longitude = positionItem.longitude;
        currentMarker.address = positionItem.address;

        addMarker(currentMarker);
    }

    private void addMarker(MarkerObject markerObject) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(
                new LatLng(markerObject.latitude, markerObject.longitude))
                .title(markerObject.address));
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
        /*
        Log.d("GoogleMap", "Enter onMapReady");
        LatLng sydney = new LatLng(-33.852, 151.211);
        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // check bundle
        checkBundle();
    }

    private void checkBundle() {
        // if sent with bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(POSITIONITEMKEY)) {
                // hide parking button
                ToggleButton toggleButton = getView()
                        .findViewById(R.id.toggleButton);
                toggleButton.setVisibility(View.INVISIBLE);

                // show marker on that item
                PositionContent.PositionItem positionItem =
                        (PositionContent.PositionItem) bundle
                                .getSerializable(POSITIONITEMKEY);

                if (positionItem != null) {
                    MarkerObject markerObject = new MarkerObject();
                    markerObject.longitude = positionItem.longitude;
                    markerObject.latitude = positionItem.latitude;
                    markerObject.address = positionItem.address;

                    currentMarker = markerObject;

                    addMarker(currentMarker);

                    // move camera to marker
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(positionItem.latitude,
                                    positionItem.longitude), DEFAULT_ZOOM));
                }
            }
        }
        else {
            // Get the current location of the device and set the position of the map.
            getDeviceLocation();
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
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

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
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

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null)
                            {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }

                        } else {
                            //Log.d(TAG, "Current location is null. Using defaults.");
                            //Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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

        if (context instanceof ParkFragment.OnDatabaseRefListener) {
            mRefListener = (OnDatabaseRefListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDatabaseRefListener");
        }
    }

    @Override
    public void onClick(View view) {
        //do what you want to do when button is clicked
        switch (view.getId()) {
            case R.id.toggleButton:
                ToggleButton toggleButton = view.findViewById(R.id.toggleButton);
                if (toggleButton.isChecked()) {
                    try {
                        savePositionToDb();
                    } catch (IOException e) {
                        Toast.makeText(getActivity(),"Failed to save location",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
                else {
                    // remove marking on map
                    mMap.clear();
                }
                break;
        }
    }

    // interfaces
    public interface OnDatabaseRefListener {
        DatabaseReference OnDatabaseRef();
    }
}