package com.example.user.uber;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;


    String user;
    String id;

    Boolean requestActive = false;

    Button uberButton;
    Boolean buttonChanged = false;

    Handler handler = new Handler();

    public void checkForUpdates(final LatLng latLng) {


        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.CHECK_FOR_DRIVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        if (!response.contains("No driver found!")) {

                            JSONArray jsonArray = null;

                            try {

                                jsonArray = new JSONArray(response);
                                if (jsonArray != null) {

                                    //  Toast.makeText(getApplicationContext(), "Driver is on the way", Toast.LENGTH_SHORT).show();
                                    uberButton.setVisibility(View.INVISIBLE);

                                    JSONArray arr = jsonArray.getJSONArray(0);
                                    String distance = arr.getString(0);
                                    String latitude = arr.getString(1);
                                    String longnitude = arr.getString(2);
                                    String username = arr.getString(3);

                                    Double distanceKm = Double.parseDouble(distance);
                                    Double roundDistance = (double) Math.round(distanceKm * 10) / 10;

                                    ArrayList<Marker> markers = new ArrayList<Marker>();
                                    if (roundDistance < 0.01) {
                                        Toast.makeText(getApplicationContext(), "Your driver has arrived ", Toast.LENGTH_SHORT).show();
                                        //DELETE THE DRIVER ID AND HIS LAT AND LONG FROM DB FOR A NEW CALL


                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                uberButton.setVisibility(View.VISIBLE);
                                                sharedPreferences.edit().remove("location").apply();
                                                uberButton.setText("Call an Uber");
                                                mMap.clear();
                                                requestActive = false;
                                                buttonChanged = false;

                                            }
                                        }, 5000);


                                    } else {


                                        Toast.makeText(getApplicationContext(), "Your driver is : " + roundDistance.toString() + " km away.", Toast.LENGTH_SHORT).show();
                                        markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location")));
                                        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longnitude))).title("Request Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));


                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                        for (Marker marker : markers) {
                                            builder.include(marker.getPosition());

                                        }

                                        //markers.clear();


                                        LatLngBounds bounds = builder.build();

                                        int padding = 60;
                                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                                        mMap.animateCamera(cu);
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "json array is null no response", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), "exception", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }


                        } else {
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(), "Timeout Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(getApplicationContext(), "No Connection Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Authentication Failure Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(), "Networl Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(getApplicationContext(), "Server Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getApplicationContext(), "JSON Parse Error!!!", Toast.LENGTH_SHORT).show();
                }

            }
        }) {

            // whats sending to database
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userType", sharedPreferences.getString("userType", ""));
                params.put("userId", sharedPreferences.getString("userId", ""));
                params.put("location", sharedPreferences.getString("location", ""));


                return params;
            }

            // and this sending to db for more security accessing
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-Agent", "MyTestApp");
                return headers;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                checkForUpdates(latLng);

            }
        }, 5000);


    }


    public void logout(View view) {

        sharedPreferences.edit().remove("location").apply();
        sharedPreferences.edit().remove("userType").apply();
        sharedPreferences.edit().remove("userId").apply();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("answer", "logout");
        startActivity(intent);
        finish();// not going back


    }


    public void callUber(View view) {

        if (requestActive) {

            sharedPreferences.edit().remove("location").apply();
            uberButton.setText("Call an Uber");
            requestActive = false;
            buttonChanged = false;


        } else {

            if (Build.VERSION.SDK_INT > 23) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnownLocation != null) {

                        // search in db
                        requestActive = true;
                        uberButton.setText("Cancel Uber");

                    } else {
                        Toast.makeText(this, "Could not find location. Please try again later.", Toast.LENGTH_SHORT).show();
                    }

                }   // sdk lower than 23
            } else {


                uberButton.setText("Cancel Uber");
                requestActive = true;
                buttonChanged = true;


            }

        }

    }

    // for sdk >23
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    updateMap(lastKnownLocation);
                }

            }
        }

    }

    public void saveLocation() {

        //.makeText(getActivity(), "Registered!!!", Toast.LENGTH_SHORT).show();
        //from Constats class
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.SAVE_LOCATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.contains("Location saved")) {


                            Toast.makeText(getApplicationContext(), "Location updated in db", Toast.LENGTH_SHORT).show();

                            /// to change

                            String b = sharedPreferences.getString("location", "");

                            // Toast.makeText(this, b, Toast.LENGTH_SHORT).show();
                            if (!b.equals("")) {

                                int gjat = b.length() - 1;
                                String temp1 = b.substring(10, gjat);


                                String[] first = temp1.split(",");

                                Double lat = Double.parseDouble(first[0]);
                                Double lng = Double.parseDouble(first[1]);


                                //LatLng loc = new LatLng(lat, lng);

                                LatLng latLng = new LatLng(lat, lng);


                                checkForUpdates(latLng);
                            }


                        } else {
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(), "Timeout Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(getApplicationContext(), "No Connection Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(getApplicationContext(), "Authentication Failure Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(), "Networl Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(getApplicationContext(), "Server Error!!!", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getApplicationContext(), "JSON Parse Error!!!", Toast.LENGTH_SHORT).show();
                }

            }
        }) {

            // whats sending to database
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("userType", sharedPreferences.getString("userType", ""));
                params.put("userId", sharedPreferences.getString("userId", ""));
                params.put("location", sharedPreferences.getString("location", ""));


                return params;
            }

            // and this sending to db for more security accessing
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("User-Agent", "MyTestApp");
                return headers;
            }
        };

        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);


    }


    public void updateMap(Location location) {

        //Toast.makeText(getApplicationContext(), location.toString(), Toast.LENGTH_SHORT).show();
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        String a = sharedPreferences.getString("location", "");
        if (a != "") {
            sharedPreferences.edit().remove("location").apply();
            sharedPreferences.edit().putString("location", userLocation.toString()).apply();
        } else {
            sharedPreferences.edit().putString("location", userLocation.toString()).apply();
        }


        saveLocation();


        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        sharedPreferences = this.getSharedPreferences("com.example.user.uber", Context.MODE_PRIVATE);

        Intent intent = getIntent();

        user = intent.getStringExtra("userType");
        id = intent.getStringExtra("userId");

        sharedPreferences.edit().putString("userType", user).apply();
        sharedPreferences.edit().putString("userId", id).apply();


        uberButton = (Button) findViewById(R.id.callUberButton);


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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);



        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
              //  Toast.makeText(getApplicationContext(), "location changed", Toast.LENGTH_SHORT).show();

               if(buttonChanged == true ){
                   updateMap(location);
                   buttonChanged = false;

                 }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        // requestin the gps service

        if(Build.VERSION.SDK_INT < 23){

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED  ) {
                  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);



                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                buildAlertMessageNoGps();



            }else
            {
                Toast.makeText(this, "Enable gps", Toast.LENGTH_SHORT).show();

            }



        }else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            }else{
                        // if permission get last known location
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                updateMap(lastKnownLocation);

            }
        }


        String b = sharedPreferences.getString("location", "");

       // Toast.makeText(this, b, Toast.LENGTH_SHORT).show();
        if (! b.equals("") ){

            int gjat = b.length() - 1;
            String temp1 = b.substring(10, gjat);



            String[] first =  temp1.split(",");

            Double lat = Double.parseDouble(first[0]);
            Double lng = Double.parseDouble(first[1]);


            //LatLng loc = new LatLng(lat, lng);

            LatLng latLng = new LatLng(lat, lng);

            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));

            requestActive = true;
            uberButton.setText("Cancel Uber");

            /// to change

            checkForUpdates(latLng);

        }


    }



    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(" Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("ّyes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }




}
