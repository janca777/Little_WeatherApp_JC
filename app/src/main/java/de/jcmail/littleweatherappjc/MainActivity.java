package de.jcmail.littleweatherappjc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    //**********
    // Field variables
    // ********/
    final String LOG = "WeatherAppLogTag";

    //openweather api access data
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    //my APP ID for openweather api access
    final String APP_ID = "0b3bc0be989204273fac056cabcafeaa";
    //select language for "description"-String: DEUTSCH
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
    private TextView countryTextView;
    private EditText editCityField;
    private Button clearEditText;
    private Button getCurrentLocationBtn;

    private String myCity;
    private String myCountry;
    private String myWeatherDescription;

    //**********
    // Android lifecicle callback methods
    // ********/

    //setting up the app in oncreate() callback
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //wiring the layout elements
        dateView = findViewById(R.id.dateView);
        locationTextView = findViewById(R.id.locationTextView);
        countryTextView = findViewById(R.id.countryTextView);
        tempTextView = findViewById(R.id.tempTextView);
        weatherDescritptionTextView = findViewById(R.id.weatherDescriptionTextView);
        editCityField = findViewById(R.id.editCityName);
        clearEditText = findViewById(R.id.clearEditText);
        getCurrentLocationBtn = findViewById(R.id.getCurrentLocationBtn);

        //getting the current date and updating the textfield
        dateView.setText(getCurrentDate());

        //wiring the textfield to the app
        editCityField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String newCity = editCityField.getText().toString();

                if (newCity != null) {
                    getWeatherForNewCity(newCity);
                } else {
                    getWeatherForLocation();
                }

                InputMethodManager myInputMedthodMangr = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                myInputMedthodMangr.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                return false;
            }//onEditorAction override
        });//setOnEditorActionListener

        //wiring the clear button behind the editText
        View.OnClickListener clearTextClicklistener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCityField.setText("");
            }//onClick override
        };//clearTextClicklistener

        //wiring the "get current location" button
        View.OnClickListener getLocationClicklistener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeatherForLocation();
                //clearing the edit Textfield after the location has changes
                editCityField.setText("");
            }//onClick override
        };//getLocationClicklistener

        //setting the listeners for the functionality of the buttons
        clearEditText.setOnClickListener(clearTextClicklistener);
        getCurrentLocationBtn.setOnClickListener(getLocationClicklistener);
    }//onCreate callback

    //getting device location in onResume() callback
    @Override
    protected void onResume() {
        super.onResume();
        getWeatherForLocation();
    }//onResume callback

    //resource maintenance - removing updated from the LocationManager
    @Override
    protected void onPause() {
        super.onPause();

        if (myLocationManager != null) myLocationManager.removeUpdates(myLocationListener);
    }//onPause callback

    //****
    // Custom methods
    // **/

    //getting the device's location, and displaying the local weather
    private void getWeatherForLocation() {

        myLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //getting the location and logging the result
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());

                RequestParams myParams = new RequestParams();
                myParams.put("lang", LANGUAGE);
                myParams.put("lat", latitude);
                myParams.put("lon", longitude);
                myParams.put("appid", APP_ID);

                myNetworkingMethod(myParams);
            }//onLocationChanged override

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(LOG, "onProviderDisabled() callback received");
            }//onProviderDisabled override
        };//LocationListener

        //setting up the location manager
        myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //permission check added automatically
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

            return;
        }
        myLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME,
                MIN_DISTANCE, myLocationListener);

    }//getWeatherForLocation

    //getting the weather-info based on custom textfield-data
    private void getWeatherForNewCity(String city) {

        RequestParams myParams = new RequestParams();
        myParams.put("q", city);
        myParams.put("lang", LANGUAGE);
        myParams.put("appid", APP_ID);

        myNetworkingMethod(myParams);
    }//getWeatherForNewCity

    //get the user"s permission to acces the location data
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG, "onRequestPermissionResult(): Permission granted!");
                getWeatherForLocation();

            } else {
                Log.d(LOG, "Permission DENIED!");
            }
        }//outer if-statement
    }//onRequestPermissionsResult override

    //establishing network connection to access the json weatherdata
    private void myNetworkingMethod(RequestParams myParams) {

        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.get(WEATHER_URL, myParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int status, Header[] headers, JSONObject response) {

                processJson(response);
            }//onSuccess override

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                Log.e(LOG, e.toString());
                Log.d(LOG, "Statuscode: " + statusCode);

                //displaying an error toast when no network connection is available
                Toast.makeText(MainActivity.this, "The HTTP-Request failed", Toast.LENGTH_SHORT).show();
            }//onFailure override
        });//myClient.get
    }//myNetworkingMethod

    //decoding the received json
    private void processJson(JSONObject jsonObject) {
        try {
            //getting the city's name
            myCity = jsonObject.getString("name");
            locationTextView.setText(myCity);

            // accessing temperature value and converting Kelvin to Celsius
            double myTempResult = jsonObject.getJSONObject("main").getDouble("temp") - 273.15;
            //converting result to integer
            int myRoundedTemp = (int) Math.rint(myTempResult);
            tempTextView.setText(String.valueOf(myRoundedTemp));

            //getting the weather's description
            JSONArray listArray = jsonObject.getJSONArray("weather");
            JSONObject firstObject = listArray.getJSONObject(0);
            myWeatherDescription = firstObject.getString("description");
            weatherDescritptionTextView.setText(myWeatherDescription);

            //extracting the country of the location
            myCountry = jsonObject.getJSONObject("sys").getString("country");
            countryTextView.setText(", " + myCountry);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }//processJson

    //getting the current date from the system and formatting it
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd. MMMM", Locale.GERMAN);
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }//getCurrentDate
}// MainActivity
