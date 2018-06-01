package com.example.minh.googlemap;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.minh.googlemap.Model.MyPlaces;
import com.example.minh.googlemap.Model.Results;
import com.example.minh.googlemap.Remote.IGoogleAPIService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        ,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener
        ,LocationListener{

    private static final int MY_PERMISSION_CODE = 1000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleAPIClient;
    private double latitude,longtitude;
    private Location mLastLocation;
    private Marker mMarker;
    private LocationRequest mLocationRequest;
    IGoogleAPIService mService;
    MyPlaces currentPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Khoi tao service
        mService = Common.getGoogleAPIService();
        //Request Runtime Permisson
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_hospital:
                        nearByPlace("hospital");
                        break;
                    case R.id.action_market:
                        nearByPlace("market");
                        break;
                    case R.id.action_restaurant:
                        nearByPlace("restaurant");
                        break;
                    case R.id.action_school:
                        nearByPlace("school");
                        break;
                    default:break;
                }
                return true;
            }
        });
    }

    private void nearByPlace(final String placeType){
        mMap.clear();
        String url = getUrl(latitude,longtitude,placeType);
        mService.getNearByPlaces(url)
                .enqueue(new Callback<MyPlaces>() {
                    @Override
                    public void onResponse(Call<MyPlaces> call, Response<MyPlaces> response) {
                        currentPlace = response.body();
                        if(response.isSuccessful()){
                            for (int i = 0;i<response.body().getResults().length;i++){
                                MarkerOptions markerOptions = new MarkerOptions();
                                Results googlePlace = response.body().getResults()[i];
                                double lat = Double.parseDouble(googlePlace.getGeometry().getLocation().getLat());
                                double lng = Double.parseDouble(googlePlace.getGeometry().getLocation().getLng());
                                String placeName = googlePlace.getName();
                                String vicinity = googlePlace.getVicinity();
                                LatLng latLng = new LatLng(lat,lng);
                                markerOptions.position(latLng);
                                markerOptions.title(placeName);
                                if(placeType.equals("hospital"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.maker_hospital));
                                else if(placeType.equals("school"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.maker_school));
                                else if(placeType.equals("market"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.maker_shop));
                                else if(placeType.equals("restaurant"))
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.maker_restaurant));
                                else
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                markerOptions.snippet(String.valueOf(i));
                                mMap.addMarker(markerOptions);
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<MyPlaces> call, Throwable t) {

                    }
                });
    }

    private String getUrl(double latitude, double longtitude, String placeType) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/search/json?");
        googlePlacesUrl.append("radius=10000");
        googlePlacesUrl.append("&location="+latitude+","+longtitude);
        googlePlacesUrl.append("&key="+"AIzaSyBpqauS6AspRa6DsL7-gEsg0WfKyvdAkRY");
        googlePlacesUrl.append("&type="+placeType);
        Log.d("getUrl",googlePlacesUrl.toString());
        return googlePlacesUrl.toString();
    }

    private boolean checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
           if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
               ActivityCompat.requestPermissions(this,new String[]{
                       Manifest.permission.ACCESS_FINE_LOCATION
               },MY_PERMISSION_CODE);
           }else {
               ActivityCompat.requestPermissions(this,new String[]{
                       Manifest.permission.ACCESS_FINE_LOCATION
               },MY_PERMISSION_CODE);
           }
           return false;
        }else return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        latitude = -33.847927;
        longtitude = 150.651777;
        LatLng sydney = new LatLng(latitude, longtitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Your location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14));

        //Khoi tao google play service
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClien();
                mMap.setMyLocationEnabled(true);
            }
        }else
            {
                buildGoogleApiClien();
                mMap.setMyLocationEnabled(true);
            }
            //Make event click on Marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //when user select marker, just get result of place and assign to static variable
                Common.currentResult = currentPlace.getResults()[Integer.parseInt(marker.getSnippet())];
                //start new activity
                startActivity(new Intent(MapsActivity.this,ViewPlace.class));
                return true;
            }
        });
    }


    public synchronized void buildGoogleApiClien(){
        mGoogleAPIClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleAPIClient,mLocationRequest,this);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleAPIClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if(mMarker !=null)
            mMarker.remove();
        latitude = location.getLatitude();
        longtitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude,longtitude);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("Your position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMarker = mMap.addMarker(markerOptions);

        //Move camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        if(mGoogleAPIClient !=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPIClient,this);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_CODE:
            {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        if(mGoogleAPIClient == null)
                            buildGoogleApiClien();
                        mMap.setMyLocationEnabled(true);
                    }
                }else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}

