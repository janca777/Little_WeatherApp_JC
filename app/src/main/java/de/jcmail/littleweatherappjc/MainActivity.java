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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    //time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    final String LOG = "WeatherAppLogTag";

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
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);

            return;
        }
        myLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, myLocationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 123) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG, "onRequestPermissionResult(): Permission granted!");
                getWeatherForLocation();

            } else {
                Log.d(LOG, "Permission DENIED!");
            }


        }

    }

}
