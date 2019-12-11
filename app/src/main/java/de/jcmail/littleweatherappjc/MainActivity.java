package de.jcmail.littleweatherappjc;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //variables for the layout
    private TextView dateView;
    private TextView locationTextView;
    private TextView tempTextView;
    private TextView weatherDescritptionTextView;


    private String myTemperature;
    private String myCity;
    private String myWeatherDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateView = findViewById(R.id.dateView);
        locationTextView = findViewById(R.id.locationTextView);
        tempTextView = findViewById(R.id.tempTextView);
        weatherDescritptionTextView = findViewById(R.id.weatherDescriptionTextView);




    }
}
