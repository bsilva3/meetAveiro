package pi.ua.meetaveiro.activities;


import android.graphics.Bitmap;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.RouteHistoryDetailAdapter;
import pi.ua.meetaveiro.models.Route;
import pi.ua.meetaveiro.others.Utils;

import static pi.ua.meetaveiro.others.Constants.INSTANCE_BY_ID;
import static pi.ua.meetaveiro.others.Constants.ROUTE_BY_ID;

public class RouteDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String TAG = RouteDetailsActivity.class.getSimpleName();
    ViewPager viewPager;
    RouteHistoryDetailAdapter adapter;
    ArrayList<Bitmap> images = new ArrayList<>();
    private Toolbar mTopToolbar;
    private String routeTitle = "", routeDesc = "", value = "";
    private TextView routeDescription, routeDate;
    //Map
    private GoogleMap mMap;

    //Boolean to check if it is from the storage
    private boolean isFromServer = false;

    //RouteInstanceID
    private String routeInstanceID;

    private String routeID;


    /**
     * To send here:
     * <p>
     * routeTitle: The title of the route (MANDATORY)
     * fileName: The name of the file if it is local
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);

        //Get the title
        Bundle b = getIntent().getExtras();
        getSupportActionBar().setTitle(b.getString("routeTitle"));


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialize the TextViews
        routeDescription = findViewById(R.id.descriptRoute);
        routeDate = findViewById(R.id.DateRoute);


        //Verify if it comes from the server or it is local
        if (Objects.equals(b.getString("RouteInstanceID").toString(), "noNumber")) {
            isFromServer = false;
        } else {
            isFromServer = true;
            if(b.getString("Type").equals("Route"))
                this.routeID = b.getString("RouteInstanceID").toString();
            else
                this.routeInstanceID = b.getString("RouteInstanceID").toString();
        }

    }


    /**
     * Constructs the page
     * Setting all information
     * <p>
     * Gets a ROUTE_BY_ID with all the information
     */
    private void constructPage() {
        try {
            viewPager = (ViewPager) findViewById(R.id.view_images);

            //Get the fileName in case of local
            Bundle b = getIntent().getExtras();
            if (b.getString("fileName") != null)
                value = b.getString("fileName");

            //Get all info
            if (isFromServer == false) {
                reconstructRoute(Utils.getRouteFromFile(value, this.getApplicationContext()));
            } else {
                //TODO: METHOD GET
                if (b.getString("Type").equals("Route")) {
                    //Do as it is a Route
                    new fetchDataAsRoute().execute();
                } else {
                    //Do as it is a Instance that has all the information
                    new fetchDataAsRouteInstance().execute();
                }

            }


            adapter = new RouteHistoryDetailAdapter(this, images);
            viewPager.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
        }

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
        constructPage();
    }


    /**
     * Reconstructs the Route with all the markers from the storage
     * @param datax JSON data
     */
    private void reconstructRoute(String datax) {

        Route r;
        Map<Marker, Bitmap> tempMap = new HashMap<>();
        try {

            JSONObject json = new JSONObject(datax.toString());
            JSONArray arr = json.getJSONArray("Markers");

            r = new Route(json.get("Title").toString());
            routeTitle = json.get("Title").toString();
            //Get Route Description
            r.setRouteDescription(json.get("Description").toString());
            routeDesc = json.get("Description").toString();
            //Get the dates and set them in the view date
            Date startDate = new Date(json.get("StartDate").toString());
            Date endDate = new Date(json.get("EndDate").toString());
            routeDate.setText("Start: " + startDate.toString() + "\nFinished: " + endDate.toString());

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
                Bitmap image = Utils.StringToBitMap(newIcon);
                images.add(image);
                Marker m = mMap.addMarker(new MarkerOptions().position(markLar)
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        .title(title)
                        .snippet(snippet));

                //Put in the map of markers
                tempMap.put(m, image);


            }

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

            //Set the route Description if there is any
            if (routeDesc != "" || routeDesc != null)
                routeDescription.setText(routeDesc);

            return;
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }


    /**
     * Go back
     *
     * @param item menu Item
     * @return True or False
     */
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

    /**
     * Create a simplefied menu
     *
     * @param menu Menu
     * @return True or False
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_menu_simplified, menu);
        return true;
    }


    private void rearrangeJSONDataRoute(String inFo){
        /*
        {
  "description": "Conhece a UA",
  "title": "Conhece a UA",
  "trajectory": [
    {
      "latitude": 40.6310031,
      "longitude": -8.65964259999998
    },
    {
      "latitude": 40.633175,
      "longitude": -8.659496
    },
    {
      "latitude": 40.630349,
      "longitude": -8.658214
    }
  ]
}
         */
    }


    private void rearrangeJSONDataInstance(String inFo){
        try {
            JSONObject json = new JSONObject(inFo);

            routeTitle = json.getString("title");
            String description = json.getString("description");



            routeDescription.setText(description);
            getSupportActionBar().setTitle(routeTitle);



            JSONArray jarrMarkers = json.getJSONArray("markers");
            //Marker array
            for(int i = 0; i<jarrMarkers.length();i++){
                JSONObject obj = jarrMarkers.getJSONObject(i);
                String img = obj.getString("img");
                String latitude = obj.getString("latitude");
                String longitude = obj.getString("longitude");
                LatLng l = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                String dateTime = obj.getString("date");

                images.add(Utils.StringToBitMap(img));
                Marker m = mMap.addMarker(new MarkerOptions().position(l)
                        .icon(BitmapDescriptorFactory.fromBitmap(Utils.StringToBitMap(img))).title(dateTime)
                        .snippet(dateTime));

            }

            JSONArray jarrTrajectory = json.getJSONArray("trajectory");
            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);

            for (int i = 0;i<jarrTrajectory.length();i++){
                JSONObject obj = jarrTrajectory.getJSONObject(i);
                String lat = obj.get("latitude").toString();
                String longi = obj.get("longitude").toString();
                LatLng l = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
                options.add(l);
            }

            mMap.addPolyline(options);

            Bundle b = getIntent().getExtras();
            routeDate.setText("Start: " + b.getString("StartDate") + "\nFinished: " + b.getString("EndDate"));

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    // Fetch the data as it was a Route  only trajectory and description/name
    private class fetchDataAsRouteInstance extends AsyncTask<Void, Void, Void> {
        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(INSTANCE_BY_ID + routeInstanceID);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    System.out.println(line);
                    data = data + line;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            rearrangeJSONDataInstance(data);
        }
    }


    // Fetch the data as it was a Route  only trajectory and description/name
    public class fetchDataAsRoute extends AsyncTask<Void, Void, Void> {
        String data = "";

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(ROUTE_BY_ID + routeID);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    System.out.println(line + "   " + routeID);
                    data = data + line;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            rearrangeJSONDataRoute(data);
        }
    }


}
