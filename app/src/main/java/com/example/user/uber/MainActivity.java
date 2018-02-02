package com.example.user.uber;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String userType = "";
    String userId = "";

    public void redirectActivity(){



        Toast.makeText(getApplicationContext(), sharedPreferences.getString("userType", "") + sharedPreferences.getString("userId", "") , Toast.LENGTH_SHORT).show();

        userType = sharedPreferences.getString("userType", "");
        userId = sharedPreferences.getString("userId", "");


        if (userType != "" && userId != "") {

            if (userType.equals("rider")) {
                Intent intent = new Intent(getApplicationContext(), RiderActivity.class);
                intent.putExtra("userType", userType);
                intent.putExtra("userId", userId);
                startActivity(intent);

            }
            if (userType.equals("driver")) {
                Intent intent = new Intent(getApplicationContext(), ViewRequestsActivity.class);
                intent.putExtra("userType", userType);
                intent.putExtra("userId", userId);
                startActivity(intent);

            }


        }
    }



        


    public void getStarted(View view){

        Switch swich = (Switch) findViewById(R.id.userTypeSwich);

       // Log.i("Switch value", String.valueOf(swich.isChecked()));
        userType = sharedPreferences.getString("userType", "");

        if(userType.equals("")){

            userType = "rider";

            if(swich.isChecked()){
                userType = "driver";

            }

            sharedPreferences.edit().putString("userType", userType).apply();

        }else {
            if (userType.equals("rider")) {

                if (swich.isChecked()) {
                    userType = "driver";

                    sharedPreferences.edit().remove("location").apply();
                    sharedPreferences.edit().remove("userType").apply();
                    sharedPreferences.edit().remove("userId").apply();

                    sharedPreferences.edit().putString("userType", userType).apply();

                }

            }

            if(userType.equals("driver")){
                if (!swich.isChecked()) {
                    userType = "rider";

                    sharedPreferences.edit().remove("location").apply();
                    sharedPreferences.edit().remove("userType").apply();
                    sharedPreferences.edit().remove("userId").apply();

                    sharedPreferences.edit().putString("userType", userType).apply();
                }


            }
        }


        userId = sharedPreferences.getString("userId", "");

            if (!userId.equals(""))
            { Toast.makeText(getApplicationContext(), "userId is  not empty string", Toast.LENGTH_SHORT).show();
                     redirectActivity();

            }
            else
            {

                Toast.makeText(getApplicationContext(), "userId is empty String and will be a registration", Toast.LENGTH_SHORT).show();
                register();
                //redirectActivity();

            }




    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();// hide action to get more space for app

        sharedPreferences = this.getSharedPreferences("com.example.user.uber", Context.MODE_PRIVATE);
        Toast.makeText(getApplicationContext(), sharedPreferences.getString("userType", "") + sharedPreferences.getString("userId", "") , Toast.LENGTH_SHORT).show();
        redirectActivity();



        Intent intent = getIntent();
        if(intent != null) {
            String answer = intent.getStringExtra("answer");
            if (answer == "logout") {
                sharedPreferences.edit().remove("location").apply();
               sharedPreferences.edit().remove("userType").apply();
                sharedPreferences.edit().remove("userId").apply();
            }
        }
    }


    public void register(){

        //.makeText(getActivity(), "Registered!!!", Toast.LENGTH_SHORT).show();
        //from Constats class
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(!response.contains("Error")){

                            Toast.makeText(getApplicationContext(), "is registering", Toast.LENGTH_SHORT).show();
                            sharedPreferences.edit().putString("userId", response).apply();
                            redirectActivity();
                            Toast.makeText(getApplicationContext(), "finished registering userId updated", Toast.LENGTH_SHORT).show();


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
                    Toast.makeText(getApplicationContext(), "Network Error!!!", Toast.LENGTH_SHORT).show();
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
                params.put("userType", userType);

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
