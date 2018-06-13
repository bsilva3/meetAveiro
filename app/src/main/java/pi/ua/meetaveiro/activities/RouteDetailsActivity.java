package pi.ua.meetaveiro.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.RouteHistoryDetailAdapter;
import pi.ua.meetaveiro.data.Route;
import pi.ua.meetaveiro.others.MapScrollWorkAround;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.Utils;

import static pi.ua.meetaveiro.others.Constants.INSTANCE_BY_ID;
import static pi.ua.meetaveiro.others.Constants.ROUTE_BY_ID;
import static pi.ua.meetaveiro.others.Constants.URL_SEND_ROUTE;

public class RouteDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String TAG = RouteDetailsActivity.class.getSimpleName();
    ViewPager viewPager;
    RouteHistoryDetailAdapter adapter;
    ArrayList<Bitmap> images = new ArrayList<>();
    private String routeTitle = "";
    private String value = "";
    private TextView routeDescription, routeDate;
    //Map
    private GoogleMap mMap;

    //Boolean to check if it is from the storage
    private boolean isFromServer = false;

    //RouteInstanceID
    private String routeInstanceID;
    //RouteID
    private String routeID;

    private Route route;

    //Boolean to check if it is an instance or a Route
    private Boolean isRoute;

    private CardView dateCardView;
    private CardView photosCardView;

    //Variables to be used only
    Map<LatLng,Bitmap> mapBit = new HashMap<>();
    Map<Bitmap,String> mapBitDate = new HashMap<>();
    String dateT;
    LatLng larr;


    /**
     * To send here:
     * routeTitle: The title of the route (MANDATORY)
     * fileName: The name of the file if it is local
     *
     * @param savedInstanceState Instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the title
        Bundle b = getIntent().getExtras();
        getSupportActionBar().setTitle(b.getString("routeTitle"));


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_route_details);
        mapFragment.getMapAsync(this);

        //Initialize the TextViews
        routeDescription = findViewById(R.id.descriptRoute);
        routeDate = findViewById(R.id.DateRoute);
        photosCardView = findViewById(R.id.photos_cardview);
        dateCardView = findViewById(R.id.date_cardview);

        //Verify if it comes from the server or it is local
        if (Objects.equals(b.getString("RouteInstanceID"), "noNumber") || b.getString("RouteInstanceID") == null) {
            isFromServer = false;
        } else {
            isFromServer = true;
            if(Objects.equals(b.getString("Type"), "Route"))
                this.routeID = b.getString("RouteInstanceID");
            else
                this.routeInstanceID = b.getString("RouteInstanceID");
        }


        NestedScrollView mScrollView = findViewById(R.id.route_details_scroll);
        //fix for google maps scroll inside a nested scroll (a tap is required sometimes...)
        ((MapScrollWorkAround) getSupportFragmentManager().findFragmentById(R.id.map_route_details)).setListener(new MapScrollWorkAround.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });
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

            value = "route" + b.getString("routeTitle") + ".json";

            //Get all info
            if (!isFromServer) {
                reconstructRoute(Utils.getRouteFromFile(value, this.getApplicationContext()));
            } else {
                if (b.getString("Type").equals("Route")) {
                    //Do as it is a Route
                    photosCardView.setVisibility(View.GONE);
                    dateCardView.setVisibility(View.GONE);


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

        this.isRoute = true;

        Route r;
        Map<Marker, Bitmap> tempMap = new HashMap<>();

        try {

            JSONObject json = new JSONObject(datax.toString());
            JSONArray arr = json.getJSONArray("Markers");

            r = new Route(json.get("Title").toString());
            routeTitle = json.get("Title").toString();
            //Get Route Description
            r.setRouteDescription(json.get("Description").toString());
            String routeDesc = json.get("Description").toString();
            TextView txt = findViewById(R.id.PercursoTextoData);
            txt.setText(routeDesc);


            //Get the dates and set them in the view date
            Date startDate = new Date(json.get("StartDate").toString());
            Date endDate = new Date(json.get("EndDate").toString());
            routeDate.setText("Começo: " + startDate.toString() + "\nFim: " + endDate.toString());

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

            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markLar, 17));


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
            int i = getMiddleofArray(arr);
            double lat = arr.getJSONObject(i).getDouble("Latitude");
            double longt = arr.getJSONObject(i).getDouble("Longitude");
            Log.d("latLong", lat+", "+longt);
            //center the map in the route
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longt), 18.0f));
            Polyline p = mMap.addPolyline(options);
            route = new Route();
            route.setId(Integer.parseInt(routeID));
            route.setRoutePath(p);
            //Set the route Description if there is any
            if (routeDesc != "" || routeDesc != null)
                routeDescription.setText(routeDesc);
            return;
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }


    //returns the middle element of a list
    private int getMiddleofArray(JSONArray arrayOfNames) {
        if (arrayOfNames.length() %2 == 0)
            return arrayOfNames.length() /2;
        else
            return (arrayOfNames.length() /2)-1;
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

        if (id == R.id.start_route) {
            Intent intent = new Intent(this, RouteActivity.class);
            //only the route points and id is necessary
            intent.putExtra("route", (Parcelable) route);
            if (route != null)
                startActivity(intent);
        } else if (id == R.id.edit_route) {

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

        this.isRoute = true;
            Log.d("routeInfo", inFo+"");
        try {
            JSONObject json = new JSONObject(inFo);
            routeTitle = json.getString("title");
            Bundle b = getIntent().getExtras();

            String description = b.getString("routeDescription");



            routeDescription.setText(description);
            getSupportActionBar().setTitle(routeTitle);

            JSONArray jarrTrajectory = json.getJSONArray("trajectory");
            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);
            LatLng l = null;
            for (int i = 0;i<jarrTrajectory.length();i++){
                JSONObject obj = jarrTrajectory.getJSONObject(i);
                String lat = obj.get("latitude").toString();
                String longi = obj.get("longitude").toString();
                l = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
                options.add(l);
            }

            try {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(l, 18);
                mMap.animateCamera(cameraUpdate);

            }catch (Exception e){}


            mMap.addPolyline(options);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //Rearranges all the information from the Instance as Images
    private void rearrangeJSONDataInstance(String inFo){
        this.isRoute = false;

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
                String dateTime = obj.getString("date");

                new AsyncGettingBitmapFromUrl().execute(img,dateTime,latitude,longitude);

            }

            JSONArray jarrTrajectory = json.getJSONArray("trajectory");
            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);
             LatLng l = null;
             for (int i = 0;i<jarrTrajectory.length();i++){
                JSONObject obj = jarrTrajectory.getJSONObject(i);
                String lat = obj.get("latitude").toString();
                String longi = obj.get("longitude").toString();
                l = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));
                Log.d("line", l+"");
                options.add(l);
            }
            try {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(l, 18);
                mMap.animateCamera(cameraUpdate);

            }catch (Exception e){}

             mMap.addPolyline(options);

            Bundle b = getIntent().getExtras();
            routeDate.setText("Começo: " + b.getString("StartDate") + "\nFim: " + b.getString("EndDate"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Reassures that all markers are in the correct positions
    public void populateImagesMarker(){

        for(Iterator<Map.Entry<LatLng, Bitmap>> it = mapBit.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<LatLng, Bitmap> entry = it.next();
            if(entry.getKey().equals(entry.getKey())) {

                for(Iterator<Map.Entry<Bitmap, String>> iter = mapBitDate.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry<Bitmap, String> entry2 = iter.next();
                    if(entry2.getKey().equals(entry.getValue())) {
                        images.add(entry.getValue());

                        Bitmap img = Utils.createSmallMarker(entry.getValue());
                        Marker m = mMap.addMarker(new MarkerOptions().position(entry.getKey())
                                .icon(BitmapDescriptorFactory.fromBitmap(img)).title(entry2.getValue())
                                .snippet(entry2.getValue()));
                    }
                }

                it.remove();
            }
        }
        adapter.notifyDataSetChanged();
    }


    private class AsyncGettingBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap bitmap = null;
            dateT = params[1];
            larr = new LatLng(Double.parseDouble(params[2]),Double.parseDouble(params[3]));
            bitmap = Utils.downloadImage(params[0]);

            if(bitmap == null){
                finish();
                startActivity(getIntent());}


            mapBit.put(larr,bitmap);
            mapBitDate.put(bitmap,dateT);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            populateImagesMarker();
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

    //edit name privacy and description of route. 1 - public, 0 - private
    private void changeRouteInfo(String title, String description, int privacy) {
        JSONObject j = new JSONObject();
        try {
            j.put("title", title);
            j.put("description", description);
            j.put("privacy", privacy);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.PUT,
                URL_SEND_ROUTE, j, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("res", response.toString());

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }



}
