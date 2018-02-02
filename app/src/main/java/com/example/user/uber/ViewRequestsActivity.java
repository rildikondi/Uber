package com.example.user.uber;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewRequestsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String user;
    String id;


    ListView requestListView;
    ArrayList<String> requests ;
    ArrayAdapter arrayAdapter;

    ArrayList<Double> requestLatitudes ;
    ArrayList<Double> requestLongnitudes ;

    ArrayList<String> usernames;



    LocationManager locationManager;
    LocationListener locationListener;

    Boolean updateMyLocation = true;

    public void showRiders(){

        String c = sharedPreferences.getString("location", "");
                if(!c.equals("")) {

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.SHOW_RIDERS_URL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (!response.contains("No riders found!")) {
                                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                        // json array getting
                                        requests.clear();
                                        requestLatitudes.clear();
                                        requestLongnitudes.clear();
                                        usernames.clear();

                                        JSONArray jsonArray = null;

                                        try {
                                            jsonArray = new JSONArray(response);
                                            if (jsonArray != null) {
                                                Toast.makeText(getApplicationContext(), jsonArray.length() + "", Toast.LENGTH_SHORT).show();
                                                for (int i = 0; i < jsonArray.length(); i++) {

                                                    JSONArray arr = jsonArray.getJSONArray(i);
                                                    String distance = arr.getString(0);
                                                    String latitude = arr.getString(1);
                                                    String longnitude = arr.getString(2);
                                                    String username = arr.getString(3);

                                                    Double distanceKm = Double.parseDouble(distance);
                                                    Double roundDistance = (double) Math.round(distanceKm * 10)/10;

                                                    //Toast.makeText(getApplicationContext(), distance, Toast.LENGTH_SHORT).show();
                                                    requests.add(roundDistance.toString() + "km");
                                                    requestLatitudes.add(Double.parseDouble(latitude));
                                                    requestLongnitudes.add(Double.parseDouble(longnitude));
                                                    usernames.add(username);

                                                }

                                                arrayAdapter.notifyDataSetChanged();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "json array is null no response", Toast.LENGTH_SHORT).show();
                                            }


                                        } catch (JSONException e) {
                                            Toast.makeText(getApplicationContext(), "exception", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                       // Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

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
                                Toast.makeText(getApplicationContext(), "Network Error!!!", Toast.LENGTH_SHORT).show();
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

    }





    public void updateListView(Location location) {

        //Toast.makeText(getApplicationContext(), location.toString(), Toast.LENGTH_SHORT).show();
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());


        String a = sharedPreferences.getString("location", "");
        if(!a.equals("") ) {
            sharedPreferences.edit().remove("location").apply();
            sharedPreferences.edit().putString("location", userLocation.toString()).apply();
        }
        else{
            sharedPreferences.edit().putString("location", userLocation.toString()).apply();
        }

        saveLocation();









    }

    // for sdk >23
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    updateListView(lastKnownLocation);
                }

            }
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        String a = sharedPreferences.getString("location", "");
        if(!a.equals("")) {
            showRiders();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        setTitle("Nearby requests");

        sharedPreferences = this.getSharedPreferences("com.example.user.uber", Context.MODE_PRIVATE);

        Toast.makeText(this, sharedPreferences.getString("location", ""), Toast.LENGTH_SHORT).show();

        requests = new ArrayList<String>();
        requestLatitudes = new ArrayList<Double>();
        requestLongnitudes = new ArrayList<Double>();

        usernames = new ArrayList<>();

        Intent intent = getIntent();



        user = intent.getStringExtra("userType");
        id = intent.getStringExtra("userId");
        if(user.equals("") && id.equals("") ) {
            sharedPreferences.edit().putString("userType", user).apply();
            sharedPreferences.edit().putString("userId", id).apply();
        }



        requestListView = (ListView) findViewById(R.id.requestListView);

        arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, requests);



        requestListView.setAdapter(arrayAdapter);
        requestListView.setBackgroundColor(Color.BLUE);

        String savedloc = sharedPreferences.getString("location", "");
        if(!savedloc.equals("")){
            showRiders();
            updateMyLocation = false;
        }



        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(getApplicationContext(), sharedPreferences.getString("location", ""), Toast.LENGTH_SHORT).show();

                Toast.makeText(getApplicationContext(), requestLatitudes.size() + "  " +  requestLongnitudes.size()
                        + usernames.size() , Toast.LENGTH_SHORT).show();
               // if this dont work try with shared preferences to get the last known location

                if(Build.VERSION.SDK_INT < 23) {
                    if (requestLatitudes.size() > i && requestLongnitudes.size() > i && usernames.size() > 1) {

                        Intent intent2 = new Intent(getApplicationContext(), DriverLocationActivity.class);

                        intent2.putExtra("requestLatitude", requestLatitudes.get(i));
                        intent2.putExtra("requestLongnitude", requestLongnitudes.get(i));
                        intent2.putExtra("riderId", usernames.get(i));


                        intent2.putExtra("userType", sharedPreferences.getString("userType", ""));
                        intent2.putExtra("userId", sharedPreferences.getString("userId", ""));

                        // if  lastknown dont work for sdk <23
                        intent2.putExtra("location", sharedPreferences.getString("location", ""));
                        startActivity(intent2);

                    }


                }
                else if( Build.VERSION.SDK_INT > 22 && ContextCompat.checkSelfPermission(ViewRequestsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    //Toast.makeText(getApplicationContext(), lastKnownLocation.toString(), Toast.LENGTH_SHORT).show();
                  //  Toast.makeText(getApplicationContext(), "item : "+ i + "  requestLatitudes :" + requestLatitudes.get(i)
                  //          + " requestLongnitudes : " + requestLongnitudes.get(i)+ " request : " + requests.get(i), Toast.LENGTH_SHORT).show();


                    if(requestLatitudes.size() > i && requestLongnitudes.size() > i && usernames.size() > 1 && lastKnownLocation != null){

                        Intent intent = new Intent(getApplicationContext(), DriverLocationActivity.class);

                        intent.putExtra("requestLatitude", requestLatitudes.get(i));
                        intent.putExtra("requestLongnitude", requestLongnitudes.get(i));

                        intent.putExtra("driverLatitude", lastKnownLocation.getLatitude());
                        intent.putExtra("driverLongnitude", lastKnownLocation.getLongitude());

                        intent.putExtra("riderId", usernames.get(i));

                        intent.putExtra("userType", sharedPreferences.getString("userType", ""));
                        intent.putExtra("userId", sharedPreferences.getString("userId", ""));

                        // if  lastknown dont work for sdk <23
                        // intent.putExtra("location", sharedPreferences.getString("location", ""));
                        startActivity(intent);




                    }


                }





            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {


               if(updateMyLocation) {
                   Toast.makeText(getApplicationContext(), "location changed" , Toast.LENGTH_SHORT).show();
                   updateListView(location);
                   updateMyLocation = false;
               }


            }

             @Override
             public void onStatusChanged (String s,int i, Bundle bundle){

             }

             @Override
             public void onProviderEnabled (String s){

             }

            @Override
             public void onProviderDisabled (String s){

             }
        };


        if(Build.VERSION.SDK_INT < 23){

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED  ) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


                //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                //  buildAlertMessageNoGps();



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

                updateListView(lastKnownLocation);

            }
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

    public void saveLocation(){

        //.makeText(getActivity(), "Registered!!!", Toast.LENGTH_SHORT).show();
        //from Constats class
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.SAVE_LOCATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.contains("Location saved")){


                            Toast.makeText(getApplicationContext(), "Location saved in db", Toast.LENGTH_SHORT).show();
                                showRiders();


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





}
