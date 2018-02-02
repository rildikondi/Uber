package com.example.user.uber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
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

public class DriverLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent intent;


    SharedPreferences sharedPreferences;
    ArrayList<Marker> markers;

    LatLng driverLocation;
    LatLng requestLocation;



    public void acceptRequest(View view){
        // CHANGE IN DB DRIVERID for the client


        saveDriver();


        String b = intent.getStringExtra("location") ;


        int gjat = b.length() - 1;
        String temp1 = b.substring(10, gjat);



        String[] first =  temp1.split(",");

        Double lat = Double.parseDouble(first[0]);
        Double lng = Double.parseDouble(first[1]);


        //LatLng loc = new LatLng(lat, lng);







        Intent directionsIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=" + lat + "," + lng +
                "&daddr=" + intent.getDoubleExtra("requestLatitude", 0) + "," + intent.getDoubleExtra("requestLongnitude", 0)));
        startActivity(directionsIntent);

    }

    public void saveDriver(){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.SAVE_DRIVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(!response.contains("You got the rider!")){

                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

                        }else
                        {
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof TimeoutError){
                    Toast.makeText(getApplicationContext(), "Timeout Error!!!", Toast.LENGTH_SHORT).show();
                }else if(error instanceof NoConnectionError){
                    Toast.makeText(getApplicationContext(), "No Connection Error!!!", Toast.LENGTH_SHORT).show();
                }else if(error instanceof AuthFailureError){
                    Toast.makeText(getApplicationContext(), "Authentication Failure Error!!!", Toast.LENGTH_SHORT).show();
                }else if(error instanceof NetworkError){
                    Toast.makeText(getApplicationContext(), "Networl Error!!!", Toast.LENGTH_SHORT).show();
                }else if(error instanceof ServerError){
                    Toast.makeText(getApplicationContext(), "Server Error!!!", Toast.LENGTH_SHORT).show();
                }else if(error instanceof ParseError){
                    Toast.makeText(getApplicationContext(), "JSON Parse Error!!!", Toast.LENGTH_SHORT).show();
                }

            }
        }){

            // whats sending to database
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("riderId", intent.getStringExtra("riderId"));
                params.put("userId", intent.getStringExtra("userId"));
                params.put("location", intent.getStringExtra("location"));

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_location);

       // sharedPreferences = this.getSharedPreferences("com.example.user.uber", Context.MODE_PRIVATE);

        intent = getIntent();



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




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


        if (Build.VERSION.SDK_INT < 23) {


            String b = intent.getStringExtra("location");



            int gjat = b.length() - 1;
            String temp1 = b.substring(10, gjat);



            String[] first =  temp1.split(",");

            Double lat = Double.parseDouble(first[0]);
            Double lng = Double.parseDouble(first[1]);


            //LatLng loc = new LatLng(lat, lng);




            driverLocation = new LatLng(lat , lng);




            requestLocation = new LatLng(intent.getDoubleExtra("requestLatitude", 0), intent.getDoubleExtra("requestLongnitude", 0));

            markers = new ArrayList<Marker>();





            //(RelativeLayout)
            RelativeLayout mapLayout = findViewById(R.id.mapRelativeLayout);
            mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    markers.add(mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your Location")));
                    markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Request Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));


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
            });

        }else
        {

            driverLocation = new LatLng(intent.getDoubleExtra("driverLatitude", 0), intent.getDoubleExtra("driverLongnitude", 0));
            requestLocation = new LatLng(intent.getDoubleExtra("requestLatitude", 0), intent.getDoubleExtra("requestLongnitude", 0));

            markers = new ArrayList<Marker>();


            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            //(RelativeLayout)
            RelativeLayout mapLayout = findViewById(R.id.mapRelativeLayout);
            mapLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    markers.add(mMap.addMarker(new MarkerOptions().position(driverLocation).title("Your Location")));
                    markers.add(mMap.addMarker(new MarkerOptions().position(requestLocation).title("Request Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))));


                    LatLngBounds.Builder builder = new LatLngBounds.Builder();

                    for (Marker marker : markers) {
                        builder.include(marker.getPosition());

                    }

                    markers.clear();


                    LatLngBounds bounds = builder.build();

                    int padding = 60;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                    mMap.animateCamera(cu);

                }
            });


        }


    }
}
