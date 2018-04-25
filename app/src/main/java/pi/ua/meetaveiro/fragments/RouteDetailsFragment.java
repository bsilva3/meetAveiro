package pi.ua.meetaveiro.fragments;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pi.ua.meetaveiro.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RouteDetailsFragment extends Fragment implements OnMapReadyCallback {


    private static final int DEFAULT_ZOOM = 17;
    private GoogleMap mMap;
    Polyline line;


    public RouteDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //Recieve the argument
        //reconstructRoute( * Argument here *);

        return inflater.inflate(R.layout.fragment_route_details, container, false);
    }


    /**
     * Reconstructs the Route with all the markers
     * @param datax
     */
    private void reconstructRoute(String datax){
        //get all info
        try {
            System.out.println("IM WORKING ");
            System.out.println(datax);
            JSONObject json = new JSONObject(datax.toString());
            JSONArray arr = json.getJSONArray("Markers");
            //Iterate trough the array of markers
            for (int i = 0; i < arr.length(); i++) {
                JSONObject gfg = arr.getJSONObject(i);
                String title = gfg.get("Titl").toString();
                String snippet = gfg.get("Snippet").toString();

                String lat = gfg.get("Latitude").toString();
                String longi = gfg.get("Longitude").toString();
                LatLng l = new LatLng(Double.parseDouble(lat),Double.parseDouble(longi));


                String icon = gfg.get("Icon").toString();
                String newIcon = icon.replaceAll("//","/");
                Bitmap image = StringToBitMap(newIcon);

                Marker m = mMap.addMarker(new MarkerOptions().position(l)
                        .icon(BitmapDescriptorFactory.fromBitmap(image))
                        .title(title)
                        .snippet(snippet));
            }
            //Reconstruct the polilyne
            arr = json.getJSONArray("Poly");
            PolylineOptions options = new PolylineOptions().width(10).color(Color.BLUE).geodesic(true);
            LatLng l = new LatLng(0,0);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject gfg = arr.getJSONObject(i);
                String lat = gfg.get("Latitude").toString();
                String longi = gfg.get("Longitude").toString();
                l = new LatLng(Double.parseDouble(lat),Double.parseDouble(longi));
                options.add(l);

            }

            line = mMap.addPolyline(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(l.latitude,
                            l.longitude), DEFAULT_ZOOM));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap StringToBitMap(String encodedString){
        byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String route = getArguments().getString("routeInformation");
        reconstructRoute(route);

    }
}
