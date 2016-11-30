package com.geasser.marcheauxrabais;

import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    // Localisation en plein dans le Tchad
    private double mLatitude = 20;
    private double mLongitude = 20;
    private Location mCurrentLocation;
    private Location mLastLocation;
    private boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    private String  mLastUpdateTime;

    private LatLng positionCamera;


    private final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    private final String LOCATION_KEY = "location-key";
    private final String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private final String SAVE_POSITION_CAMERA = "save_camera_camera";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ControleurBdd.getInstance(this).syncEntreprise();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        createLocationRequest();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        updateValuesFromBundle(savedInstanceState);


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
        // LatLng(Latitude,Longitude)
        //LatLng chicoutimi = new LatLng(48.4222, -71.0619);
        // Ajoute un marqueur rouge sur la carte à la latitude et la longitude de Chicoutimi
       // mMap.addMarker(new MarkerOptions().position(chicoutimi).title("Marker in Chicoutimi").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        getMaker();


        // Obligatoire d'avoir l'autorisation de l'utilisateur pour la localisation.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {

        // Si l'utilisateur accepte de partager sa localisation alors ...
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            //Récupération des coordonnées GPS de la localisation GPS
            if (mLastLocation != null) {
                mLatitude = mLastLocation.getLatitude();
                mLongitude = mLastLocation.getLongitude();
                LatLng test = new LatLng(mLatitude, mLongitude);
                    // Centre la carte sur l'utilisateur avec un zoom de 16.
                if (positionCamera !=null){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(positionCamera));
                }
                else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(test, 16));
                }

            }

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }

        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }



    // Paramètres pour mettre à jour la position de l'utilisateur
    protected void createLocationRequest() {

        // Create an instance of GoogleAPIClient, nécessaire pour les services de géolocalisation.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            // Tout ce qui concerne les paramètres de fréquence, fréquence max et précision spatiale de la localisation.
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(
                                        MapsActivity.this,
                                        1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });


        }
    }




    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        // Attention ...
        mLastUpdateTime=java.text.DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }
    // This can be handy to reduce power consumption, provided the app doesn't need to collect information even when it's running in the background.
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    // Use a boolean, mRequestingLocationUpdates, to track whether location updates are currently turned on.
    // In the activity's onResume() method, check whether location updates are currently active, and activate them if not.
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    // A change to the device's configuration, such as a change in screen orientation or language, can cause the current activity to be destroyed.
    // Your app must therefore store any information it needs to recreate the activity. One way to do this is via an instance state stored in a Bundle object.
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        positionCamera = mMap.getCameraPosition().target;
        savedInstanceState.putParcelable(SAVE_POSITION_CAMERA, positionCamera);
        super.onSaveInstanceState(savedInstanceState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and
            // make sure that the Start Updates and Stop Updates buttons are
            // correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
               // setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the
            // UI to show the correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that
                // mCurrentLocationis not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(
                        LAST_UPDATED_TIME_STRING_KEY);
            }

            if (savedInstanceState.keySet().contains(SAVE_POSITION_CAMERA)) {
                positionCamera = savedInstanceState.getParcelable(SAVE_POSITION_CAMERA);
           }
            updateUI();
        }
    }

    public void getMaker (){
        HashMap<String,LatLng> list = new HashMap<String, LatLng>();
        ArrayList<HashMap<String,String>> tab = ControleurBdd.getInstance(this).selection("SELECT Nom, Latitude, Longitude FROM entreprises", ControleurBdd.BASE.INTERNE);
        // On affiche simplement le texte retourné.
        int i =0;
            while (i<tab.size()){
            //  info.setText(tab.get(i).get("MotDePasse").toString() + " " + password);
            LatLng coordo = new LatLng(Double.parseDouble(tab.get(i).get("Latitude")),Double.parseDouble(tab.get(i).get("Longitude")));
            String nomEntreprise = tab.get(i).get("Nom");
            list.put(nomEntreprise,coordo);
            i++;
        }
        for (String key : list.keySet()){
            mMap.addMarker(new MarkerOptions().position(list.get(key)).title(key).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));
        }

    }


    private void updateUI() {
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    }
