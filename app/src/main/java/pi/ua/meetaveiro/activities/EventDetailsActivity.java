package pi.ua.meetaveiro.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.models.Event;

public class EventDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);

        event = (Event) getIntent().getSerializableExtra("Event");

        collapsingToolbarLayout.setTitleEnabled(true);
        collapsingToolbarLayout.setTitle("Event");

        TextView textTitle = findViewById(R.id.event_title);
        textTitle.setText(event.getTitle());

        TextView textPrice = findViewById(R.id.event_price);
        textPrice.setText(event.getPrice() + "€");

        TextView textLocation = findViewById(R.id.event_location);
        textLocation.setText(event.getLocation());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            // Creates an Intent that will load a map of San Francisco
            Uri gmmIntentUri = Uri.parse("geo:"+ event.getLatitude()
                    + "," + event.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        FloatingActionButton fabSearch = findViewById(R.id.fab_search);
        fabSearch.setOnClickListener(view -> {
            String escapedQuery = "";
            try {
                escapedQuery = URLEncoder.encode(event.getTitle(), "UTF-8");
            } catch (UnsupportedEncodingException e) {}

            Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

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
        mMap.getUiSettings().setScrollGesturesEnabled(false);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(event.getLatitude(), event.getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title(event.getTitle()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 7.0f ) );
    }
}
