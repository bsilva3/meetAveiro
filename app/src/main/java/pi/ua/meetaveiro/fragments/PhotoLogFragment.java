package pi.ua.meetaveiro.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.interfaces.DataReceiver;

import static android.content.Context.MODE_PRIVATE;

/**
 * Photo logging and route making {@link Fragment} subclass.
 */
public class PhotoLogFragment extends Fragment implements OnMapReadyCallback, DataReceiver{
    private static final int CAMERA_REQUEST = 1888;
    private static final String TAG = "ERRO";
    private static final int DEFAULT_ZOOM = 10;
    private static final String KEY_CAMERA_POSITION = "lastCameraPosition";
    private static final String KEY_LOCATION = "lastLocation";

    private final LatLng mDefaultLocation = new LatLng(40.6442700, -8.6455400);
    // Last known map camera position
    private CameraPosition mCameraPosition;
    // Last known device location
    private Location mLastKnownLocation;
    // Map Object
    private GoogleMap mMap;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;

    //map to store marker that are not yet updated with information
    private Map<Marker, Boolean> markers;
    //map that stores the image in each map
    private Map<Marker, Bitmap> imageMarkers;

    //Alterar consoante o IP da máquina (que pode ser consultado com ip addr show)
    private final static String API_URL = "http://192.168.1.3:8080/search";
    private final static String FILENAME = "pi.ua.meetaveirocom.PREFERENCE_FILE_KEY";

    //To store in local
    private SharedPreferences prefs = null;
    // To delete the shared preferences use : SharedPreferences.Editor.clear();
    //following a commit();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new HashMap<>();
        imageMarkers = new HashMap<>();

        if (savedInstanceState != null) {
            this.mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            this.mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());




    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_log, container, false);
        FloatingActionButton buttonAddPhoto = view.findViewById(R.id.search);
        buttonAddPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.map, mapFragment);
        transaction.commit();
        return view;
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
        updateLocationUI();
        requestDeviceLocationUpdates();
        //Initialize Preferences*
        prefs = getActivity().getSharedPreferences("LatLng",MODE_PRIVATE);


        /*While we have markers stored iterate trough.
        * For each marker add to the map with all information related to it*/
        boolean stop = false;
        int tmp = 0;
        while (!stop){
            if (prefs.contains("Lat" + tmp)) {
                String lat = prefs.getString("Lat" + tmp, "");
                String lng = prefs.getString("Lng" + tmp, "");
                String btm = prefs.getString("bmp" + tmp, "");
                String snip = prefs.getString("Snip" + tmp, "");
                String titl = prefs.getString("Titl" + tmp,"");
                LatLng l = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                Bitmap img = StringToBitMap(btm);
                try {
                    mMap.addMarker(new MarkerOptions().position(l)
                            .icon(BitmapDescriptorFactory.fromBitmap(img))
                            .title(titl)
                            .snippet(snip));
                }catch (Exception e){
                    mMap.addMarker(new MarkerOptions().position(l)
                            .title(titl)
                            .snippet(snip));
                }
                tmp++;
            }else{
                stop = true;
            }
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult == null) {
                return;
            }

            Log.i("MapsActivity", "Location: " + locationResult.getLastLocation().getLatitude() + " " + locationResult.getLastLocation().getLongitude());

            mLastKnownLocation = locationResult.getLastLocation();

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

        }

        @Override
        public void onLocationAvailability (LocationAvailability locationAvailability){
            super.onLocationAvailability(locationAvailability);
            if(!locationAvailability.isLocationAvailable())
                Toast.makeText(getContext(), "Localização não disponivel",  Toast.LENGTH_SHORT);
        }
    };

    private void requestDeviceLocationUpdates() {
        try{
            mFusedLocationProviderClient.requestLocationUpdates(LocationRequest.create(), mLocationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void removeDeviceLocationUpdates() {
        try{
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            String base64Photo = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
            //create json with server request, and add the photo base 64 encoded
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("image", base64Photo);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Add a marker in photo location
            LatLng marker;
            if (mLastKnownLocation!=null)
                marker = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
            else
                marker = mDefaultLocation;
            //add the marker to the map of markers, but indicate that this marker
            //does not have an updated info yet
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(marker)
                    .title(getContext().getString(R.string.unknown_string))
                    .snippet("Unknown")
                    .icon(BitmapDescriptorFactory.fromBitmap(photo)));
            markers.put(m, false);
            imageMarkers.put(m, photo);
            //send a base 64 encoded photo to server
            new uploadFileToServerTask().execute(jsonRequest.toString());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onResponseReceived(Object result) {
        JSONObject json = null;
        try {
            json = new JSONObject(result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (java.lang.NullPointerException e){
            Toast.makeText(getContext(), "Connection error", Toast.LENGTH_LONG).show();
            return;
        }
        Marker markerToUpdate = null;
        //search the not yet updated marker
        for(Map.Entry entry : markers.entrySet()){
            if(entry.getValue().equals(false)){
                markerToUpdate = (Marker) entry.getKey();
                break;
            }
        }
        Marker oldMarker = markerToUpdate;
        try {
            markerToUpdate.setTitle(json.get("name").toString());
            markerToUpdate.setSnippet(json.get("description").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        markers.put(markerToUpdate, true);


        //update the marker in the map that stores every marker and the image in the marker
        updateMarkerOnMap(oldMarker, markerToUpdate);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                Log.d("markerClick", "clicked");
                if (m != null) {
                    Log.d("markerClickI", "clicked with info");
                    createAndShowInfoDialog(m);
                    return true;
                }
                return false;
            }
        });




        //saveMarkersOnStorage(FILENAME, imageMarkers);
        createAndShowInfoDialog(markerToUpdate);
    }

    @Override
    public void onResume() {
        super.onResume();
        removeDeviceLocationUpdates();

        //loadMarkersFromStorage(FILENAME);
    }

    //used to save the updated marker with the info obtained from the server
    private void updateMarkerOnMap(Marker old, Marker newMarker){
        Bitmap bit = imageMarkers.get(old);
        imageMarkers.remove(old);
        imageMarkers.put(newMarker, bit);
    }

    private void createAndShowInfoDialog(Marker m){
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.point_info_dialog);
        dialog.setTitle(m.getTitle());
        dialog.setCancelable(true);
        ImageView image = (ImageView) dialog.findViewById(R.id.point_image);
        TextView description = (TextView) dialog.findViewById(R.id.point_description);
        Button btn = (Button) dialog.findViewById(R.id.close_info_dialog);
        image.setImageBitmap(imageMarkers.get(m));
        description.setText(m.getSnippet());
        //******************************************************
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dialog.dismiss();
            }

        });
        //******************************************************
        dialog.show();
    }


    /*
    * When the app is in background and whren a different fragment is selected
    * this will be triggered.
    * Each marker has:
    * - Latitude    Lat
    * - Longitude   Lng
    * - Icon        bmp
    * - Title       Titl
    * - Snippet     Snip
    */
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        //Iterate trough all saved markers.
        int i = 0;
        Iterator<Map.Entry<Marker, Bitmap>> it = imageMarkers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Marker, Bitmap> pair = it.next();
            Bitmap img = pair.getValue();
            Marker m = pair.getKey();

            //Add to the preferences the information of the markers

            prefs.edit().putString("Lat"+i,String.valueOf(m.getPosition().latitude)).commit();
            prefs.edit().putString("Lng"+i,String.valueOf(m.getPosition().longitude)).commit();
            prefs.edit().putString("Titl"+i,String.valueOf(m.getTitle())).commit();
            prefs.edit().putString("Snip"+i,String.valueOf(m.getSnippet())).commit();
            prefs.edit().putString("bmp"+i,BitMapToString(img)).commit();
            i++;

        }



        //saveMarkersOnStorage(FILENAME, imageMarkers);
    }

    /*Encodes the Image to a string*/
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }


    private class uploadFileToServerTask extends AsyncTask<String, Void, String> {
        ProgressDialog progDailog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(getContext());
            progDailog.setMessage(getContext().getString(R.string.scanning_image_string));
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Log.d("response", "begin");
            try {
                //Create a URL object holding our url
                URL url = new URL(API_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                //set headers and method
                Writer writer = new BufferedWriter(new OutputStreamWriter
                        (urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);
                // json data
                writer.close();

                InputStream inputStream = urlConnection.getInputStream();
                //input stream
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();
                //send to post execute
                return JsonResponse;

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            progDailog.dismiss();
            onResponseReceived(response);
        }

    }

}
