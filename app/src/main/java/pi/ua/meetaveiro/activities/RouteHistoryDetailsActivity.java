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
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

public class RouteHistoryDetailsActivity extends AppCompatActivity {



    ViewPager viewPager;
    RouteHistoryDetailAdapter adapter;
    ArrayList<Bitmap> images = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_history_details);
        viewPager = (ViewPager)findViewById(R.id.view_images);

        //Get all info
        reconstructRoute( getRouteFromFile("routereal.json"));
        adapter = new RouteHistoryDetailAdapter(this,images);
        viewPager.setAdapter(adapter);
        System.out.println("I WORKEDDDDDDDDDDDD BITVH");
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


        Route r;
        Map<Marker, Bitmap> tempMap = new HashMap<>();
        try {

            JSONObject json = new JSONObject(datax.toString());
            JSONArray arr = json.getJSONArray("Markers");

            r = new Route(json.get("Title").toString());
            r.setRouteDescription(json.get("Description").toString());

            //Iterate trough the array of markers
            for (int i = 0; i < arr.length(); i++) {
                JSONObject gfg = arr.getJSONObject(i);
                String title = gfg.get("Titl").toString();
                String snippet = gfg.get("Snippet").toString();

                String lat = gfg.get("Latitude").toString();
                String longi = gfg.get("Longitude").toString();
                LatLng l = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));


                String icon = gfg.get("Icon").toString();
                String newIcon = icon.replaceAll("//", "/");
                Bitmap image = StringToBitMap(newIcon);
                images.add(image);
               /* Marker m = mMap.addMarker(new MarkerOptions().position(l)
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        .title(title)
                        .snippet(snippet));

                //Put in the map of markers
                tempMap.put(m,image);
*/
            }
            /*r.setRouteMarkers(tempMap);*/

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



}
