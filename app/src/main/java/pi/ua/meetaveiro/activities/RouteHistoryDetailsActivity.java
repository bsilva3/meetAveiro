package pi.ua.meetaveiro.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.RouteHistoryDetailAdapter;
import pi.ua.meetaveiro.models.Route;

public class RouteHistoryDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {



    ViewPager viewPager;
    RouteHistoryDetailAdapter adapter;
    ArrayList<Bitmap> images = new ArrayList<>();
    private Toolbar mTopToolbar;
    private String routeTitle = "",routeDesc = "",value ="";
    TextView descTitle;


    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_history_details);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }



    private int test(){
        try {
            viewPager = (ViewPager) findViewById(R.id.view_images);
            descTitle = (TextView) findViewById(R.id.routeDescriptionOpened);

            Bundle b = getIntent().getExtras();
            if (b != null)
                value = b.getString("name");

            //Get all info
            reconstructRoute(getRouteFromFile(value));
            adapter = new RouteHistoryDetailAdapter(this, images);
            viewPager.setAdapter(adapter);

            mTopToolbar = (Toolbar) findViewById(R.id.toolbar_map_simple);
            TextView appTitl = (TextView) findViewById(R.id.toolbar_map_title);
            appTitl.setText(routeTitle);
            descTitle.setText(routeDesc);

            setSupportActionBar(mTopToolbar);
            return 0;
        }catch (Exception e){
            return 1;
        }

    }


    /**
     * Opens the file with the route and reconctructs it
     * File name format: route+++.json
     * +++ = route name
     *
     * @param filename
     * @return String (json format) with all the information
     * <p>
     * Must be sent to RouteDetais as an argument
     */
    private String getRouteFromFile(String filename) {

        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);
            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }
            isr.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return datax.toString();


    }


    /**
     * Reconstructs the Route with all the markers
     *
     * @param datax
     */
    private Route reconstructRoute(String datax) {
        System.out.println(datax);

        Route r;
        Map<Marker, Bitmap> tempMap = new HashMap<>();
        try {

            JSONObject json = new JSONObject(datax.toString());
            JSONArray arr = json.getJSONArray("Markers");

            r = new Route(json.get("Title").toString());

            routeTitle = json.get("Title").toString();

            r.setRouteDescription(json.get("Description").toString());
            routeDesc = json.get("Description").toString();
            LatLng markLar = null;

            //Iterate trough the array of markers
            for (int i = 0; i < arr.length(); i++) {
                JSONObject gfg = arr.getJSONObject(i);
                String title = gfg.get("Titl").toString();
                String snippet = gfg.get("Snippet").toString();

                String lat = gfg.get("Latitude").toString();
                String longi = gfg.get("Longitude").toString();
                markLar = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));


                String icon = gfg.get("Icon").toString();
                String newIcon = icon.replaceAll("//", "/");
                Bitmap image = StringToBitMap(newIcon);
                images.add(image);
                Marker m = mMap.addMarker(new MarkerOptions().position(markLar)
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        .title(title)
                        .snippet(snippet));

                //Put in the map of markers
                tempMap.put(m,image);


            }
            /*r.setRouteMarkers(tempMap);*/
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markLar, 17));


            //Reconstruct the polilyne
            arr = json.getJSONArray("Poly");
            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);
            LatLng l = new LatLng(0, 0);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject gfg = arr.getJSONObject(i);
                String lat = gfg.get("Latitude").toString();
                String longi = gfg.get("Longitude").toString();
                l = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
                options.add(l);
            }
            mMap.addPolyline(options);
            return r;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap StringToBitMap(String encodedString) {
        byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.go_back) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_menu_simplified, menu);
        return true;
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
        test();
    }



    }
