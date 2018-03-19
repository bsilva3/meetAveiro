package pi.ua.meetaveiro.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Map;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.interfaces.DataReceiver;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoLogFragment extends Fragment implements OnMapReadyCallback, DataReceiver {

    private static final int CAMERA_REQUEST = 1888;
    private static final int FINE_LOCATION_REQUEST = 1889;
    private static final String TAG = "ERRO";
    private static final int DEFAULT_ZOOM = 18;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private final LatLng mDefaultLocation = new LatLng(40.6442700, -8.6455400);
    private GoogleMap mMap;
    private FloatingActionButton buttonAddPhoto;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted;
    //map to store marker that are not yet updated with information
    private Map<Marker, Boolean> markers;
    //map that stores the image in each map
    private Map<Marker, Bitmap> imageMarkers;
    //Alterar consoante o IP da máquina (que pode ser consultado com ip addr show)
    private final static String API_URL = "http://192.168.1.3:8080/search";
    private final static String FILENAME = "pi.ua.meetaveirocom.PREFERENCE_FILE_KEY";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markers = new HashMap<>();
        imageMarkers = new HashMap<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            // Location Permissions for API 23 and above
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST);
            }
        } //no need to aks for permissions for api below 23

        if (savedInstanceState != null) {
            this.mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            this.mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_log, container, false);
        this.buttonAddPhoto = view.findViewById(R.id.search);
        buttonAddPhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if (getContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                    } else {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                } else {
                    //api's below 23 get the permission to access camera immediately
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
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
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case FINE_LOCATION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    updateLocationUI();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getContext(), "Sem acesso á localização será impossivel ter acesso a esta funcionalidade", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
                break;
            }
            case CAMERA_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                } else {
                    Toast.makeText(getContext(), "Sem a camara será impossivel ter acesso a esta funcionalidade", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

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

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                getDeviceLocation();
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
         /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if(mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }else {
                                Toast.makeText(getContext(), "Não foi possivel obter a localização por favor verifique que tem o GPS ligado e tente novamente", Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            }
                        } else {
                            Toast.makeText( getContext(), "Não foi possivel obter a localização por favor verifique que tem o GPS ligado e tente novamente", Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            //create json with server request, and add the photo base 64 encoded
            String base64Photo = bitMapToBase64(photo);
            JSONObject jsonRequest = new JSONObject();
            try {
                jsonRequest.put("image", base64Photo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            getDeviceLocation();
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

    public String bitMapToBase64 (Bitmap image){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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
        saveMarkersOnStorage(FILENAME, imageMarkers);
        createAndShowInfoDialog(markerToUpdate);
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

    public void saveMarkersOnStorage(String fileName, Map m){
        //http://www.androidinterview.com/android-data-storage-options-sharedpreferences-tutorial/
        /*File file = new File(getContext().getFilesDir(),fileName);
        if(!file.exists()){
            file.mkdir();
        }
        FileOutputStream f = null;
        try {
            f = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            f.write(mapToByteArray(m));
            f.flush();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public Map loadMarkersFromStorage(String fileName){
        File file = new File(fileName);
        if(file.exists()){
            int size = (int) file.length();
            byte[] bytes = new byte[size];
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(getContext(), "File not found", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return byteArrayToMap(bytes);
        }
        return null;
    }

    private byte[] mapToByteArray(Map map){
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(map);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteOut.toByteArray();
    }

    private Map byteArrayToMap (byte[] b){
        ByteArrayInputStream byteIn = new ByteArrayInputStream(b);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(byteIn);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<Marker, Bitmap> data = null;
        try {
            data = (Map<Marker, Bitmap>) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return data;
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
