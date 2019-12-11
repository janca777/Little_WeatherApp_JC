package de.jcmail.littleweatherappjc;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    final String LOG = "WeatherAppLogTag";

    //openweather api access data
//    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    //my APP ID for openweather api access
    final String APP_ID = "0b3bc0be989204273fac056cabcafeaa";
    //select language for "description" String, DEUTSCH
    final String LANGUAGE = "de";

    //the request code we need the user's permission for
    final int REQUEST_CODE = 123;
    //time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    //setting the location provicer for the fine location
    final String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
    private LocationManager myLocationManager;
    private LocationListener myLocationListener;

    //variables for the layout
    private TextView dateView;
    private TextView locationTextView;
    private TextView tempTextView;
    private TextView weatherDescritptionTextView;


    private String myTemperature;
    private String myCity;
    private String myWeatherDescription;

    //setting up the app in oncreate() callback
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //wiring the layout elements
        dateView = findViewById(R.id.dateView);
        locationTextView = findViewById(R.id.locationTextView);
        tempTextView = findViewById(R.id.tempTextView);
        weatherDescritptionTextView = findViewById(R.id.weatherDescriptionTextView);


    }

    //getting device location in onResume() callback
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG, "onResume() callback called");
        Log.d(LOG, "Getting the weather from the location");

        getWeatherForLocation();
    }

    private void getWeatherForLocation() {

        myLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.d(LOG, "onProviderChanged() callback received");

                //getting the location and logging the result
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d(LOG, "Longitude is: " + longitude);
                Log.d(LOG, "Latitude is: " + latitude);

                RequestParams myParams = new RequestParams();
                myParams.put("lang", LANGUAGE);
                myParams.put("lat", latitude);
                myParams.put("lon", longitude);
                myParams.put("appid", APP_ID);

                Log.d(LOG, "myParams contains: " + myParams.toString());

                myNetworkingMethod(myParams);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

                Log.d(LOG, "onProviderDisabled() callback received");
            }
        };

        //setting up the location manager
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //permission check added automatically
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

            return;
        }
        myLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, myLocationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG, "onRequestPermissionResult(): Permission granted!");
                getWeatherForLocation();

            } else {
                Log.d(LOG, "Permission DENIED!");
            }


        }

    }

    //establishing network connection to access the json weatherdata
    private void myNetworkingMethod(RequestParams myParams) {

        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.get(WEATHER_URL, myParams, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int status, Header[] headers, JSONObject response) {

                //logging the json response to the screan
                Log.d(LOG, "Success! JSON: " + response.toString());

                //decding the information from the json
                processJson(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                Log.e(LOG, e.toString());
                Log.d(LOG, "Statuscode: " + statusCode);

                Toast.makeText(MainActivity.this, "The HTTP-Request failed", Toast.LENGTH_SHORT).show();
            }


        });
    }

    //decoding the received json
    private void processJson(JSONObject jsonObject) {
        try {

            //getting the city's name
            myCity = jsonObject.getString("name");
            Log.d(LOG, "City: " + myCity);
            locationTextView.setText(myCity);

            // accessing temperature value and converting Kelvin to Celsius
            double myTempResult = jsonObject.getJSONObject("main").getDouble("temp") - 273.15;
            //converting result to integer
            int myRoundedTemp = (int) Math.rint(myTempResult);
            Log.d(LOG, "Temperature in Celsius: " + myRoundedTemp);

            tempTextView.setText(String.valueOf(myRoundedTemp));

            //getting the weather's description
            JSONArray listArray = jsonObject.getJSONArray("weather");
            JSONObject firstObject = listArray.getJSONObject(0);
            myWeatherDescription = firstObject.getString("description");

            weatherDescritptionTextView.setText(myWeatherDescription);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}// MainActivity
