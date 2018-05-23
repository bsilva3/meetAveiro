package pi.ua.meetaveiro.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pi.ua.meetaveiro.BuildConfig;
import pi.ua.meetaveiro.activities.POIDetails;
import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.data.Photo;
import pi.ua.meetaveiro.interfaces.ImageDataReceiver;
import pi.ua.meetaveiro.others.MultiDrawable;
import pi.ua.meetaveiro.others.Utils;

import static android.content.Context.MODE_PRIVATE;
import static pi.ua.meetaveiro.others.Constants.*;

/**
 * Photo logging  {@link Fragment} subclass.
 * A user takes a photo, and sends the photo to the server. He also sends the current Date
 * When the server responds, we see if the image was recognized or not.
 * If it was, we show a dialog with the concept and its description
 * else we only show a dialog with the date in which the user took the photo
 */
public class PhotoLogFragment extends Fragment implements
        OnMapReadyCallback,
        ImageDataReceiver,
        TextToSpeech.OnInitListener,
        ClusterManager.OnClusterClickListener<Photo>,
        ClusterManager.OnClusterInfoWindowClickListener<Photo>,
        ClusterManager.OnClusterItemClickListener<Photo>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Photo>{

    //Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int CAMERA_REQUEST = 1888;
    public static final int CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE = 1777;
    private static final String TAG = "ERRO";
    private static final int DEFAULT_ZOOM = 17;
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private static final String KEY_CAMERA_POSITION = "lastCameraPosition";
    private static final String KEY_LOCATION = "lastLocation";

    // Default location
    private final LatLng mDefaultLocation = new LatLng(40.6442700, -8.6455400);
    // Last known map camera position
    private CameraPosition mCameraPosition;
    // Last known device location
    private Location mLastKnownLocation;
    // Map Object
    private GoogleMap mMap;
    //Text to speech converter
    private TextToSpeech tts;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    //Provides access to the Location Settings API.
    private SettingsClient mSettingsClient;
    //Tracks the status of the location updates request. Value changes when the user presses the Start Updates and Stop Updates buttons.
    private Boolean mRequestingLocationUpdates;

    //stores the id associated for the marker's image
    private Map<Marker, Integer> markerID = new HashMap<>();

    //to keep track of the photoItem that is going to be updated
    private Photo photoToUpdate;
    List<Photo> photos;


    private FloatingActionButton buttonAddPhoto;

    //To store in local
    private SharedPreferences prefs = null;

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 10;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;


    private ClusterManager<Photo> mClusterManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photos = new ArrayList<>();
        mRequestingLocationUpdates = false;

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(getContext());

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getContext());

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        mSettingsClient = LocationServices.getSettingsClient(getContext());

        tts = new TextToSpeech(getActivity().getApplicationContext(), this);

    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_CAMERA_POSITION)) {
                mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_log, container, false);
        buttonAddPhoto = view.findViewById(R.id.search);

        buttonAddPhoto.setOnClickListener(v -> {
            startCameraIntent();
        });
        buttonAddPhoto.setVisibility(View.GONE);

        updateValuesFromBundle(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        mapFragment.getMapAsync(this);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.map, mapFragment);
        transaction.commit();
        return view;
    }


    private void startCameraIntent(){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //bigger than api 24 needs this
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", file));
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }

        startActivityForResult(intent, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getMarkersFromStorage();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        buttonAddPhoto.setVisibility(View.VISIBLE);
        getMarkersFromStorage();
        //JSONObject j = placeRoutesOnJson();
        //Log.d("sendRoute", j.toString());
        //new uploadFileToServerTask().execute(j.toString(), URL_SEND_ROUTE);
        startMarkerClusterer();
    }

    /**
     * Draws photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class PhotoRenderer extends DefaultClusterRenderer<Photo> {
        private final IconGenerator mIconGenerator = new IconGenerator(getContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PhotoRenderer() {
            super(getContext(), mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Photo photo, MarkerOptions markerOptions) {
            // Draw a single Photo.
            // Set the info window to show their name.
            mImageView.setImageBitmap(photo.getImgBitmap());
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(photo.getConcept());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Photo> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Photo p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = new BitmapDrawable(getResources(), p.getImgBitmap());
                //Drawable drawable = getResources().getDrawable(p.profilePhoto);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Photo> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String date = cluster.getItems().iterator().next().getDate();
        //Toast.makeText(getContext(), cluster.getSize() + " (including photos taken at" + date + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Photo> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Photo item) {
        createAndShowInfoDialog(item, false);
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Photo item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

    protected void startMarkerClusterer() {
        mClusterManager = new ClusterManager<>(getContext(), mMap);
        mClusterManager.setRenderer(new PhotoRenderer());
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        addItems();
        mClusterManager.cluster();
        mClusterManager.setAnimation(true);
    }

    private void addItems() {
        for (Photo photo : photos) {
            mClusterManager.addItem(photo);
        }
    }

    public void getMarkersFromStorage(){
        //Initialize Preferences*
        photos.clear();
        if (mMap != null) {
            prefs = getActivity().getSharedPreferences("LatLng", MODE_PRIVATE);
        /*While we have markers stored iterate trough.
        * For each marker add to the map with all information related to it*/
            boolean stop = false;
            int tmp = 0;
            while (!stop) {
                if (prefs.contains("Lat" + tmp)) {
                    String lat = prefs.getString("Lat" + tmp, "");
                    String lng = prefs.getString("Lng" + tmp, "");
                    String btm = prefs.getString("bmp" + tmp, "");
                    int id = prefs.getInt("ID" + tmp, 0);
                    String date = prefs.getString("dateTime" + tmp, "");
                    String conceptID = prefs.getString("conceptID"+tmp, "");
                    String snip = prefs.getString("Snip" + tmp, "");
                    String titl = prefs.getString("Titl" + tmp, "");
                    LatLng l = null;
                    try {
                        l = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                    } catch(NumberFormatException e ) {
                        //there were no double parselable coordinates found, marker will not be placed
                        Log.e("ERROR", e.toString());
                        tmp++;
                        continue;
                    }
                    Bitmap img = Utils.StringToBitMap(btm);
                    try {
                        photos.add(new Photo(titl, snip, date, l, id, conceptID, img));
                        Log.d("load", "ids load: " + id);
                        //setClickListenersOnMap(id);

                    } catch (Exception e) {
                        Toast.makeText(getContext(), getString(R.string.problem_marker_load_from_storage), Toast.LENGTH_SHORT);
                    }
                    tmp++;
                } else {
                    stop = true;
                }
            }
            //add marker click listener
            //addMarkerListener();
        }
    }

    /*private void addMarkerListener(){
        mMap.setOnMarkerClickListener(m -> {
            createAndShowInfoDialog(m, getPhotoItemFromMarker(m).getId(), false);
            return true;
        });
    }*/

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
    //places a route on json (INCOMPLETE!!! missing title and description
    // put trajectory coordinates as string and marker's id as string)
    public JSONObject placeRoutesOnJson(){
        JSONObject j = new JSONObject();

        String routePoints = "40.6442700,-8.6455400;40.6442704,-8.6455401;40.6442705,-8.6455402";
        //add markers
        //send image ids as string (server has problems converting from int[])
        String listOfMarkers = "";

        //int[] listOfMarkers = new int[markerID.size()];
        //save for each marker, the image id, and their coords
        int i = 0;
        for (Map.Entry<Marker, Integer> entry : markerID.entrySet()){
            if (i == markerID.size()-1)
                listOfMarkers+=entry.getValue();
            else
                listOfMarkers+=entry.getValue()+",";
            i++;
        }
        try {
            j.put("title", "ola chico");
            j.put("description", "ta td??");
            j.put("start", Utils.convertTimeInMilisAndFormat(Calendar.getInstance().getTimeInMillis()));
            j.put("end", Utils.convertTimeInMilisAndFormat(Calendar.getInstance().getTimeInMillis()));
            j.put("user", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            j.put("markers", listOfMarkers);
            j.put("trajectory", routePoints);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("ROUTE", j.toString());
        return j;
    }

    //when the user taps a marker
    /*private void setClickListenersOnMap(int id){
        mMap.setOnMarkerClickListener(mkr -> {
            Log.d("markerClick", "clicked");
            if (mkr != null) {
                Log.d("markerClickI", mkr.getTitle()+", id: "+mkr.getId());
                //dont show dialog feedback
                createAndShowInfoDialog(mkr, id, false);
                return true;
            }
            return false;
        });
    }*/
    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings ");
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                Log.i(TAG, "PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings.";
                            Log.e(TAG, errorMessage);
                            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                            mRequestingLocationUpdates = false;
                    }

                    updateLocationUI();
                }
            });
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("cal", "called in photolog");
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateLocationUI();
                        break;
                }
                break;
            case CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    File file = new File(Environment.getExternalStorageDirectory()+File.separator +
                            "image.jpg");
                    Log.d("file", file.getAbsolutePath());
                    Bitmap photoHighQuality = Utils.decodeSampledBitmapFromFile(file.getAbsolutePath(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
                    Bitmap photoThumbnail= ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()),
                            THUMBSIZE, THUMBSIZE);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ByteArrayOutputStream bosThumb = new ByteArrayOutputStream();

                    photoHighQuality.compress(Bitmap.CompressFormat.JPEG, 60, bos);
                    //photoThumbnail.compress(Bitmap.CompressFormat.PNG, 100, bosThumb);

                    String base64Photo = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);

                    //create json with server request, and add the photo base 64 encoded
                    JSONObject jsonRequest = new JSONObject();
                    long date = Calendar.getInstance().getTimeInMillis();
                    try {
                        jsonRequest.put("date", date);
                        jsonRequest.put("image", base64Photo);
                        jsonRequest.put("lat", mLastKnownLocation.getLatitude());
                        jsonRequest.put("long", mLastKnownLocation.getLongitude());
                        jsonRequest.put("user", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    if (mLastKnownLocation!=null ) {
                        //create a new photo object with info to be completed by the server's response
                        photoToUpdate = new Photo(getContext().getString(R.string.unknown_string),
                                getContext().getString(R.string.unknown_string) , new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),
                                Utils.convertTimeInMilisAndFormat(Calendar.getInstance().getTimeInMillis()), photoThumbnail);
                        //send a base 64 encoded photo to server
                        Log.d("req", jsonRequest.toString()+"");
                        new uploadFileToServerTask().execute(jsonRequest.toString(), IMAGE_SCAN_URL);
                    }else {
                    }


                }
                else if (resultCode == Activity.RESULT_CANCELED){
                    Toast.makeText(getContext(), getString(R.string.foto_intent_fail), Toast.LENGTH_SHORT).show();
                }
                Utils.uncompletedCameraRequest = false;
                break;
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
    public void onImageResponseReceived(Object result) {
        JSONObject json = null;
        try {
            json = new JSONObject(result.toString());
            Log.d("res", json.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(getContext(), "Connection error. Please try again later", Toast.LENGTH_LONG).show();
            return;
        } catch (NullPointerException e){
            Log.e(TAG, e.getMessage());
            Toast.makeText(getContext(), "Server didn't return a response. Please try again later", Toast.LENGTH_LONG).show();
            photoToUpdate = new Photo();
            return;
        }

        String title = "";
        String description = "";
        String conceptID = "";
        int id = 0;
        //we get the data from the json
        //if the image was recognized, we get the title, description and id, else
        //we get unknown (desconhecido) as title, "" as description (still return an id)
        try {
            title = json.getString("name");
            description = json.getString("description");
            id = json.getInt("id");
            conceptID = json.getString("concept_id");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }
        //update the photo item with the info obtained from the server
        photoToUpdate.setConcept(title);
        photoToUpdate.setDescription(getContext().getString(R.string.unknown_photo_dialog_description) +
                photoToUpdate.getDate()+"\n" +description);
        photoToUpdate.setConceptId(conceptID);
        photoToUpdate.setId(id);
        //create a marker and place it on map with all the info

        photos.add(photoToUpdate);
        // show a prompt for user feedback on the first dialog is closed
        //we only show the feedack prompt when the image is recognized
        createAndShowInfoDialog(photoToUpdate, true);
        photoToUpdate = new Photo();
        //addMarkerListener();
        saveMarkersOnStorage();
        startMarkerClusterer();
    }

    //when we send feedback
    @Override
    public void onFeedbackResponseReceived (Object result){
        JSONObject json = null;
        try {
            json = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        } catch (java.lang.NullPointerException e){
            Toast.makeText(getContext(), getContext().getString(R.string.feedback_fail), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(getContext(), getContext().getString(R.string.feedback_success), Toast.LENGTH_LONG).show();
    }


    private void createAndShowInfoDialog(Photo p, boolean showFeedback){
        String name = p.getConcept();
        String conceptID = p.getConceptId();
        String description = p.getDescription();
        //String date = convertTimeInMilisAndFormat(markerDate.get(m));
        Drawable d = new BitmapDrawable(getResources(), p.getImgBitmap());
        //image was recognized
        if (!name.toLowerCase().equals("desconhecido") && !name.toLowerCase().equals("unknown") && !name.equals("")) {
            //when the dialog with the description is closed, we show a feedback box;
            //the user says if the app was able to identify the image succesfully, or close the dialog
            //without providing an answer
            Log.d("concept_id", conceptID + ", -" + name);
            final MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(getContext())
                    .setHeaderDrawable(d)
                    .setIcon(R.drawable.ic_check_green_24dp)
                    .withDialogAnimation(true)
                    .setTitle(name)
                    .setDescription(description)
                    .setCancelable(false)
                    .setPositiveText(getContext().getString(R.string.ok))
                    .onPositive(
                            (dialog12, which) -> {
                                dialog12.dismiss();
                                if (showFeedback)
                                    showFeedbackDialogAndSend(name, p.getId(), d);
                            }
                    )
                    .onNeutral((dialog1, which) -> startActivity(new Intent(getActivity(), POIDetails.class)
                            .putExtra("attraction", conceptID)))
                    .setNeutralText(getContext().getString(R.string.see_more_info));
            dialog.setScrollable(true, 5);
            dialog.show();
        } else { //image not recognized
            final MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppTheme))
                    .setHeaderDrawable(d)
                    .setIcon(R.drawable.ic_clear_red_24dp)
                    .withDialogAnimation(true)
                    .setTitle(name)
                    .setDescription(getContext().getString(R.string.image_rec_fail))
                    .setCancelable(false)
                    .setPositiveText(getContext().getString(R.string.ok))
                    .onPositive(
                            (dialog12, which) -> {
                                dialog12.dismiss();
                            }
                    ).setScrollable(true, 6).build();
            dialog.show();

        }
        //Log.d("tts", name+". "+ description);
        speakOut(name);
    }

    private void showFeedbackDialogAndSend(String conceptName, int imageId, Drawable d){
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("concept", conceptName);
            jsonRequest.put("image_id", imageId);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        final MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(getContext())
                .setHeaderDrawable(d)
                .setIcon(R.drawable.ic_help_outline_yellow_24dp)
                .withDialogAnimation(true)
                .setTitle(conceptName)
                .setDescription(getContext().getString(R.string.feedback))
                .setCancelable(true)
                .setPositiveText(getContext().getString(R.string.yes))
                .onPositive(
                        (dialog12, which) -> {
                            //Yes button clicked
                            try {
                                jsonRequest.put("answer", 1);
                                new uploadFileToServerTask().execute(jsonRequest.toString(), FEEDBACK_URL);
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                )
                .setNegativeText(getContext().getString(R.string.no))
                .onNegative(
                        (dialog12, which) -> {
                            //Yes button clicked
                            try {
                                jsonRequest.put("answer", 0);
                                new uploadFileToServerTask().execute(jsonRequest.toString(), FEEDBACK_URL);
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                )
                .setNeutralText(getContext().getString(R.string.dont_answer));
        dialog.show();
    }


    /*
    * When the app is in background and whren a different fragment is selected
    * this will be triggered.
    * Each marker has:
    * - Latitude    Lat
    * - Longitude   Lng
    * - Icon        bmp
    * - ID          ID
    * - conceptID   conceptID
    * - dateTime    dateTime
    * - Title       Titl
    * - Snippet     Snip
    */

    //save markers on storage
    public void saveMarkersOnStorage(){
        int i = 0;
        for (Photo photo : photos) {
            Log.d("save", "t: "+photo.getConcept());
            //Add to the preferences the information of the markers

            prefs.edit().putString("Lat"+i, String.valueOf(photo.getLatLngLocation().latitude)).apply();
            prefs.edit().putString("Lng"+i, String.valueOf(photo.getLatLngLocation().longitude)).apply();
            prefs.edit().putString("Titl"+i, String.valueOf(photo.getConcept())).apply();
            prefs.edit().putString("Snip"+i, String.valueOf(photo.getDescription())).apply();
            prefs.edit().putString("dateTime"+i, String.valueOf(photo.getDate())).apply();
            prefs.edit().putString("conceptID"+i, String.valueOf(photo.getConceptId())).apply();
            Log.d("num", "saveID: "+photo.getId()+"");
            prefs.edit().putInt("ID"+i,Integer.valueOf(photo.getId())).apply();
            prefs.edit().putString("bmp"+i, BitMapToString(photo.getImgBitmap())).apply();
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


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.ENGLISH);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    private void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        resetMarkers();
    }

    public void resetMarkers(){
        Log.d("destroy", "destroyed");
        if (mMap != null)
            mMap.clear();
    }

    private class uploadFileToServerTask extends AsyncTask<String, Void, String> {
        ProgressDialog progDailog;
        String serverUrl;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(getContext());
            progDailog.setMessage(getContext().getString(R.string.scanning_image_string));
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse = null;
            String JsonDATA = params[0];
            serverUrl = params[1];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Log.d("response", "begin");
            try {
                //Create a URL object holding our url
                URL url = new URL(serverUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(30000);//stop after 30 secs
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
                Log.e(TAG, e.getMessage());
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
            if (serverUrl.equals(IMAGE_SCAN_URL))
                onImageResponseReceived(response);
            else if (serverUrl.equals(FEEDBACK_URL))
                onFeedbackResponseReceived(response);
            Log.d("res", response+"");
        }
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    public void showCurrentPlace() {
        if (mMap == null) {
            return;
        }
        // Get the likely places - that is, the businesses and other points of interest that
        // are the best match for the device's current location.
        @SuppressWarnings("MissingPermission")
        final Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
        placeResult.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                // Set the count, handling cases where less than 5 entries are returned.
                int count;
                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                    count = likelyPlaces.getCount();
                } else {
                    count = M_MAX_ENTRIES;
                }

                int i = 0;
                mLikelyPlaceNames = new String[count];
                mLikelyPlaceAddresses = new String[count];
                mLikelyPlaceAttributions = new String[count];
                mLikelyPlaceLatLngs = new LatLng[count];

                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    // Build a list of likely places to show the user.
                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                            .getAddress();
                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                            .getAttributions();
                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                    i++;
                    if (i > (count - 1)) {
                        break;
                    }
                }

                // Release the place likelihood buffer, to avoid memory leaks.
                likelyPlaces.release();

                // Show a dialog offering the user the list of likely places, and add a
                // marker at the selected place.
                openPlacesDialog();

            } else {
                Log.e(TAG, "Exception: %s", task.getException());
            }
        });

    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Saving in JSON format:
     * { Title : "",
     *   Description : "",
     *   Markers :[
     *
     *      {
     *      Titl : "" ,
     *      Snippet  : "",
     *      Latitude : "",
     *      Longitude : " " ,
     *      dateTime : "",
     *      ID: "",
     *      Icon : "bitmaptostring replaced / with // "
     *      }
     *
     *      ]
     * }
     * //@param route Route to save
     */
    /*private void saveRouteToFile(Route route){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        //Title and description
        sb.append("\"Title\" : " + "\"" + route.getRouteTitle() +"\",\n");
        sb.append("\"Description\" : " + "\"" + route.getRouteDescription() +"\",\n");
        sb.append("\"Markers\" : [\n");
        //Markers
        Map<Marker,Bitmap> temp = route.getRouteMarkers();
        int tmp = 0;
        List<LatLng> polyPoints  = route.getRoutePath().getPoints();

        for (Map.Entry<Marker, Bitmap> entry : temp.entrySet())
        {
            if (polyPoints.contains(entry.getKey().getPosition())){
                //Marker number
                if (tmp > 0)
                    sb.append(",\n");
                sb.append("{");
                //Marker information
                sb.append("\"Titl\" : " + "\"" + entry.getKey().getTitle() + "\",\n");
                sb.append("\"Snippet\" : " + "\"" + entry.getKey().getSnippet() + "\",\n");
                sb.append("\"Latitude\" : " + "\"" + entry.getKey().getPosition().latitude + "\",\n");
                sb.append("\"Longitude\" : " + "\"" + entry.getKey().getPosition().longitude + "\",\n");
                sb.append("\"dateTime\" : " + "\"" + markerDate.get(entry.getKey()) + "\",\n");
                String tmpo = bitMapToBase64(entry.getValue());
                String imageFix = tmpo.replaceAll("/","//");
                Log.d("num", "getID: "+markerID.get(entry.getKey())+"");
                Log.d("num", "getDate: "+markerDate.get(entry.getKey())+"");
                sb.append("\"ID\" : " + "\"" +markerID.get(entry.getKey())+ "\",\n");
                sb.append("\"Icon\" : " + "\"" + imageFix + "\"");
                //End marker
                sb.append("}");
                tmp++;
            }
        }
        sb.append("],\n");
        sb.append("\"Poly\" : [\n");

        //Poly points
        Iterator<LatLng> it = polyPoints.iterator();
        tmp = 0;
        while (it.hasNext()){
            if (tmp>0){
                sb.append(",\n");
            }
            sb.append("{");
            LatLng l = it.next();
            sb.append("\"Longitude\" : " + "\"" +l.longitude + "\", ");
            sb.append("\"Latitude\" : " + "\"" +l.latitude + "\"");
            sb.append("}");
            tmp++;
        }
        sb.append("]\n");
        sb.append("\n}");

        String filename = "route"+ route.getRouteTitle() +".json";
        FileOutputStream outputStream;
        try {
            outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(sb.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }*/

    //converts date time from miliseconds to date in the format DD/MM/YY, HH:MM as a string

    //converts date time from miliseconds to date in the format DD/MM/YY, HH:MM as a string

}