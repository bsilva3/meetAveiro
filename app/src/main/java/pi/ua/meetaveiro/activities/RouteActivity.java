package pi.ua.meetaveiro.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
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
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pi.ua.meetaveiro.BuildConfig;
import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.data.Attraction;
import pi.ua.meetaveiro.data.Photo;
import pi.ua.meetaveiro.data.Route;
import pi.ua.meetaveiro.data.RouteInstance;
import pi.ua.meetaveiro.fragments.PhotoLogFragment;
import pi.ua.meetaveiro.others.Constants;
import pi.ua.meetaveiro.others.GeofenceErrorMessages;
import pi.ua.meetaveiro.others.MultiDrawable;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.Utils;
import pi.ua.meetaveiro.receivers.GeofenceBroadcastReceiver;
import pi.ua.meetaveiro.services.LocationUpdatesService;

import static pi.ua.meetaveiro.others.Constants.*;

public class RouteActivity extends FragmentActivity implements
        OnMapReadyCallback,
        TextToSpeech.OnInitListener,
        OnCompleteListener<Void>,
        ClusterManager.OnClusterClickListener<Photo>,
        ClusterManager.OnClusterInfoWindowClickListener<Photo>,
        ClusterManager.OnClusterItemClickListener<Photo>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Photo>{

    //Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int CAMERA_REQUEST = 1888;
    public static final int CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE = 1777;
    private static final int DEFAULT_ZOOM = 18;
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private static final String KEY_CAMERA_POSITION = "lastCameraPosition";
    private static final String KEY_LOCATION = "lastLocation";
    private static final String TAG = "ERROR";
    private static final String TEMP_ROUTE_POINTS_FILE = "";

    //Current start/pause/stop buttons state
    private FloatingActionButton buttonStopRoute;
    private FloatingActionButton buttonStartRoute;
    private FloatingActionButton buttonPauseRoute;
    private FloatingActionButton buttonAddPhoto;

    // Default location
    private final LatLng mDefaultLocation = new LatLng(40.6442700, -8.6455400);
    // Last known map camera position
    private CameraPosition mCameraPosition;
    // Last known device location
    private Location mLastKnownLocation;
    // Map Object
    private GoogleMap mMap;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    //Provides access to the Location Settings API.
    private SettingsClient mSettingsClient;
    // The entry routePoints to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    //Receiver for device location
    LocationsReceiver locationsReceiver;
    //Tracks the status of the location updates request. Value changes when the user presses the Start Updates and Stop Updates buttons.
    private Boolean mRequestingLocationUpdates;

    //Text to speech converter
    private TextToSpeech tts;

    //store a route to follow
    private Route routeToFollow;
    //flag to check if we are following a tour already created, or recording one
    private boolean isFollowingTour;


    //To store in local
    private SharedPreferences prefs = null;

    //to draw the lines for the tour (== route) path
    private List<LatLng> routePoints;

    private Polyline line;

    //to keep track of the photoItem that is going to be updated
    private Photo photoToUpdate;
    List<Photo> photos;

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 10;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    /**
     * Used to start the Route
     */
    private long begginingDate;

    /**
     * True if it's a new route
     */
    private boolean newRoute;

    //costumize polyline
    List<PatternItem> pattern;

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_RED_ARGB = 0xff3333;

    private ClusterManager<Photo> mClusterManager;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if(Utils.getRouteState(RouteActivity.this).equals(ROUTE_STATE.STARTED))
                onRouteStateChanged(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        photos = new ArrayList<>();

        pattern = PATTERN_POLYGON_ALPHA;

        locationsReceiver = new LocationsReceiver();

        mRequestingLocationUpdates = false;

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        // Fetch simple attractions from server.
        fetchGeofences();

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mSettingsClient = LocationServices.getSettingsClient(this);

        routePoints = new ArrayList<>(); //stores all routePoints during the tour so that a trajectory line can be drawn using those routePoints
        //check if a route was passed
        isFollowingTour = false;
        newRoute = true;
        if (getIntent().hasExtra("route")){
            isFollowingTour = true;
            routeToFollow = getIntent().getExtras().getParcelable("route");
            newRoute = false;
        }
        // Inflate the layout for this fragment
        buttonAddPhoto = findViewById(R.id.take_photo);
        buttonStartRoute = findViewById(R.id.start);
        buttonPauseRoute = findViewById(R.id.pause);
        buttonStopRoute = findViewById(R.id.stop);

        buttonAddPhoto.setOnClickListener(v -> {
            startCameraIntent();
        });
        //just to make sure the buttons start correctly
        updateRouteButtons(Utils.getRouteState(RouteActivity.this));
        if (isFollowingTour){
            //we are following a pre created route
            buttonStartRoute.setOnClickListener(v -> {
                if (routePoints.isEmpty()) {
                    //we dont have any tour on map, we can begin
                    routePoints = routeToFollow.getRoutePoints();
                    redrawLine();
                    onRouteStateChanged(true);
                    Utils.setRouteState(this, ROUTE_STATE.STARTED);
                    updateRouteButtons(Utils.getRouteState(this));//we can procede to the tour
                } else {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //clean all points and markers from previous tour
                                    //and add the lines in map
                                    routePoints.clear();
                                    photos.clear();
                                    line.remove();
                                    routePoints = routeToFollow.getRoutePoints();
                                    redrawLine();
                                    //now procede to the tour
                                    onRouteStateChanged(true);
                                    Utils.setRouteState(RouteActivity.this, ROUTE_STATE.STARTED);
                                    updateRouteButtons(Utils.getRouteState(RouteActivity.this));
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //tour wont start
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(RouteActivity.this);
                    builder.setMessage(getString(R.string.start_tour_confirmation)).setPositiveButton(getString(R.string.procede), dialogClickListener)
                            .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
                }
            });

            buttonStopRoute.setOnClickListener(v -> {
                onRouteStateChanged(false);
                Utils.setRouteState(this, ROUTE_STATE.STOPPED);
                updateRouteButtons(Utils.getRouteState(this));
                endRoute();
                //save route to store
            });
        }
        else {
            //we are creating a new tour, and drawing the line as the user moves
            buttonStartRoute.setOnClickListener(v -> {
                if (routePoints.isEmpty()) {
                    onRouteStateChanged(true);
                    Utils.setRouteState(this, ROUTE_STATE.STARTED);
                    updateRouteButtons(Utils.getRouteState(this));//we can procede to the tour
                } else {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //clean all points and markers from previous tour
                                    routePoints.clear();
                                    photos.clear();
                                    mMap.clear();
                                    //now procede to the tour
                                    onRouteStateChanged(true);
                                    Utils.setRouteState(RouteActivity.this, ROUTE_STATE.STARTED);
                                    updateRouteButtons(Utils.getRouteState(RouteActivity.this));
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //tour wont start
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(RouteActivity.this);
                    builder.setMessage(getString(R.string.start_tour_confirmation)).setPositiveButton(getString(R.string.procede), dialogClickListener)
                            .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
                }
            });

            buttonStopRoute.setOnClickListener(v -> {
                onRouteStateChanged(false);
                Utils.setRouteState(this, ROUTE_STATE.STOPPED);
                updateRouteButtons(Utils.getRouteState(this));
                endRoute();
                //save route to store
            });

            buttonPauseRoute.setOnClickListener(v -> {
                onRouteStateChanged(false);
                Utils.setRouteState(this, ROUTE_STATE.PAUSED);
                updateRouteButtons(Utils.getRouteState(this));
            });
        }
        //back button (with image)
        ImageButton back = findViewById(R.id.back_image_btn);
        back.setOnClickListener(v -> {
            if (Utils.getRouteState(RouteActivity.this).equals(ROUTE_STATE.STARTED) || Utils.getRouteState(RouteActivity.this).equals(ROUTE_STATE.PAUSED)){
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                onBackPressed();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(RouteActivity.this);
                builder.setMessage(getString(R.string.exit_tour_confirmation)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();
            }
            else{
                onBackPressed();
            }

        });

        updateValuesFromBundle(savedInstanceState);

        updateRouteButtons(Utils.getRouteState(this));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.route_map);
        mapFragment.getMapAsync(this);


        Log.i("uncompletedCameraRqst", String.valueOf(Utils.uncompletedCameraRequest) );
        if (Utils.uncompletedCameraRequest && getIntent().getBooleanExtra(EXTRA_TAKE_PHOTO, false)) {
            Utils.uncompletedCameraRequest = false;
            startCameraIntent();
        }

        //start text to speech
        tts = new TextToSpeech(getApplicationContext(), this);
    }

    /**
     * Draws photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class PhotoRenderer extends DefaultClusterRenderer<Photo> {
        private final IconGenerator mIconGenerator = new IconGenerator(RouteActivity.this);
        private final IconGenerator mClusterIconGenerator = new IconGenerator(RouteActivity.this);
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PhotoRenderer() {
            super(RouteActivity.this, mMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(RouteActivity.this);
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
        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setRenderer(new RouteActivity.PhotoRenderer());
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

    /**
     * É suposto adicionar as fotos da storage ao cluster aqui
     */
    private void addItems() {
        for (Photo photo : photos) {
            mClusterManager.addItem(photo);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class).putExtra(NEW_ROUTE_EXTRA, newRoute), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateRouteButtons(Utils.getRouteState(this));
        Log.i("state....",  Utils.getRouteState(this).toString());
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(
                        locationsReceiver,
                        new IntentFilter(ACTION_BROADCAST)
                );
        Log.i("intent_photo",  String.valueOf(getIntent().getBooleanExtra(EXTRA_TAKE_PHOTO, false)));
        getCurrentPath();
    }

    /**
     * fetches json by making http calls
     */
    private void fetchGeofences() {
       JsonArrayRequest request = new JsonArrayRequest(URL_ATTRACTIONS,
                response -> {
                    if (response == null) {
                        Log.e(TAG, "Couldn't fetch the attractions.");
                        return;
                    }

                    List<Attraction> items = new Gson()
                            .fromJson(
                                    response.toString(),
                                    new TypeToken<List<Attraction>>() {}.getType()
                            );

                    // adding contacts to contacts list
                    this.populateGeofenceList(items);
                },
                error -> {
                    // error in getting json
                    Log.e(TAG, "Error: " + error.getMessage());
                }
        );
        MyApplication.getInstance().addToRequestQueue(request);

    }

    private void startCameraIntent(){
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //bigger than api 24 needs this
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file));
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }

        startActivityForResult(intent, CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE);
    }

    //Called when Start/Stop route button is pressed
    public void onRouteStateChanged(boolean started){
        if(started)
            mService.requestLocationUpdates(newRoute);
        else{
            mService.removeLocationUpdates();
            begginingDate = Calendar.getInstance().getTimeInMillis();

        }
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

    public void updateRouteButtons(Constants.ROUTE_STATE state){
        if(Constants.ROUTE_STATE.STARTED.equals(state)){
            buttonStartRoute.setVisibility( View.GONE );
            if (!isFollowingTour)
                buttonPauseRoute.setVisibility( View.VISIBLE );
            buttonStopRoute.setVisibility( View.VISIBLE );
            buttonAddPhoto.setVisibility( View.VISIBLE );
        }else if(Constants.ROUTE_STATE.PAUSED.equals(state)){
            buttonStopRoute.setVisibility( View.VISIBLE );
            buttonPauseRoute.setVisibility( View.GONE );
            buttonStartRoute.setVisibility( View.VISIBLE );
            buttonAddPhoto.setVisibility( View.VISIBLE );
        }else if(Constants.ROUTE_STATE.STOPPED.equals(state)){
            buttonStopRoute.setVisibility( View.GONE );
            buttonPauseRoute.setVisibility( View.GONE );
            buttonStartRoute.setVisibility( View.VISIBLE );
            buttonAddPhoto.setVisibility( View.GONE );
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    public void shareRouteLink(String routeInstanceId){
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_TEXT, "http://192.168.160.192:8080/resources/routes/instances/" + routeInstanceId + "/share");

        intent.putExtra(Intent.EXTRA_SUBJECT, "Check out my meetAveiro route!");

        startActivity(Intent.createChooser(intent, "Share"));
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());

            int messageId = getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;
            Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.GEOFENCES_ADDED_KEY, false);
    }

    /**
     * Stores whether geofences were added ore removed in {@link SharedPreferences};
     *
     * @param added Whether geofences were added or removed.
     */
    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     * PUT HERE THE RESULTS RETURNED BY THE SERVER (CONCEPTS)
     */
    private void populateGeofenceList(List<Attraction> attractions) {
        for (Attraction entry : attractions) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getName())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            Integer.valueOf(entry.getLatitude()),
                            Integer.valueOf(entry.getLongitude()),
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
        addGeofences();
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

        //Initialize Preferences*
        prefs = getSharedPreferences("LatLng",MODE_PRIVATE);
        startMarkerClusterer();
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
            locationResult.addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    // Set the map's camera position to the current location of the device.
                    mLastKnownLocation = task.getResult();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                } else {
                    Log.d("ERROR", "Current location is null. Using defaults.");
                    Log.e("ERROR", "Exception: %s", task.getException());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i("ERROR", "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings ");
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(RouteActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                Log.i("ERROR", "PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings.";
                            Log.e("ERROR", errorMessage);
                            Toast.makeText(RouteActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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
        Log.d("cal", "called in route activity");
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("ERROR", "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("ERROR", "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateLocationUI();
                        break;
                }
                break;
            case CAPTURE_IMAGE_FULLSIZE_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator +
                            "image.jpg");
                    Log.d("file", file.getAbsolutePath());
                    Bitmap photoHighQuality = Utils.decodeSampledBitmapFromFile(file.getAbsolutePath(), DEFAULT_WIDTH, DEFAULT_HEIGHT);
                    Bitmap photoThumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(file.getAbsolutePath()),
                            THUMBSIZE, THUMBSIZE);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    photoHighQuality.compress(Bitmap.CompressFormat.JPEG, 70, bos);

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
                    if (mLastKnownLocation != null) {
                        //create a new photo object with info to be completed by the server's response
                        photoToUpdate = new Photo(getString(R.string.unknown_string),
                                getString(R.string.unknown_string), new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),
                                Utils.convertTimeInMilisAndFormat(Calendar.getInstance().getTimeInMillis()), photoThumbnail);
                        //send a base 64 encoded photo to server
                        Log.d("req", jsonRequest.toString() + "");
                        new UploadFileToServerTask().execute(jsonRequest.toString(), IMAGE_SCAN_URL);
                    } else {
                        //Report error to user
                        Toast.makeText(this, "Location not known. Check your location settings.", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (resultCode == Activity.RESULT_CANCELED){
                }
                Utils.uncompletedCameraRequest = true;
                break;
        }
    }

    public void resetMarkers(){
        if (mMap != null){
            //mMap.clear();
            redrawLine();
        }

    }

    public void onImageResponseReceived(Object result) {
        JSONObject json = null;
        try {
            json = new JSONObject(result.toString());
            Log.d("json", json.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, "Connection error. Please try again later", Toast.LENGTH_LONG).show();
            return;
        } catch (NullPointerException e){
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, "Server didn't return a response. Please try again later", Toast.LENGTH_LONG).show();
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
        //update the photo item with the info obtained from the server
        photoToUpdate.setConcept(title);
        photoToUpdate.setDescription(this.getString(R.string.unknown_photo_dialog_description) +
                photoToUpdate.getDate()+"\n" +description);
        photoToUpdate.setConceptId(conceptID);
        photoToUpdate.setId(id);
        photos.add(photoToUpdate);
        // show a prompt for user feedback on the first dialog is closed
        //we only show the feedack prompt when the image is recognized
        createAndShowInfoDialog(photoToUpdate, true);
        photoToUpdate = new Photo();
        saveMarkersOnStorage();
        startMarkerClusterer();
    }

    //each marker is populated from info inside a Photo object
    //however, we cant get access to all info inside marker, such as the icon (photo)
    //this method returns the Photo object that has the info for a given marker
    private Photo getPhotoItemFromMarker(Marker m){
        Integer i = (Integer) m.getTag();
        for (Photo photo : photos){
            if (photo.getId() == i.intValue()){
                return photo;
            }
        }
        return null;
    }

    public void onFeedbackResponseReceived (Object result){
        JSONObject json = null;
        try {
            json = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        } catch (java.lang.NullPointerException e){
            Toast.makeText(this, this.getString(R.string.feedback_fail), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, this.getString(R.string.feedback_success), Toast.LENGTH_LONG).show();
    }

    public void onRouteSentResponseReceived (String result){
        JSONObject json = null;
        String routeInstanceId = "";
        try {
            Log.i("Resultttt", result + "");
            json = new JSONObject(result);
            routeInstanceId = json.getString("inst");
        } catch (Exception e) {
            Log.i("errorroute", e.getMessage());
            Toast.makeText(this, this.getString(R.string.feedback_fail), Toast.LENGTH_LONG).show();
            return;
        }

        String finalRouteInstanceId = routeInstanceId;
        new MaterialStyledDialog.Builder(this)
                .setTitle(this.getString(R.string.route_success))
                .setDescription(R.string.share_route)
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(R.drawable.ic_share_black_24dp)
                .setHeaderColor(R.color.colorAccent)
                .setCancelable(true)
                .withIconAnimation(true)
                .withDialogAnimation(true)
                .setPositiveText(this.getString(R.string.share))
                .onPositive(
                        (dialog12, which) -> {
                            shareRouteLink(finalRouteInstanceId);
                        }
                )
                .setNegativeText(this.getString(R.string.close))
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    public void onNewLocation(Location location){
        if (location == null) {
            return;
        }

        Log.i("PhotoLogActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());

        mLastKnownLocation = location;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), mMap.getCameraPosition().zoom));
    }

    public String bitMapToBase64 (Bitmap image){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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
            final MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(this)
                    .setHeaderDrawable(d)
                    .setIcon(R.drawable.ic_check_green_24dp)
                    .withDialogAnimation(true)
                    .setTitle(name)
                    .setDescription(description)
                    .setCancelable(false)
                    .setPositiveText(getString(R.string.ok))
                    .onPositive(
                            (dialog12, which) -> {
                                dialog12.dismiss();
                                if (showFeedback)
                                    showFeedbackDialogAndSend(name, p.getId(), d);
                            }
                    )
                        .onNeutral((dialog1, which) -> startActivity(new Intent(this, POIDetails.class)
                                .putExtra("attraction", conceptID)))
                        .setNeutralText(getString(R.string.see_more_info));
                dialog.setScrollable(true, 5);
                dialog.show();
            } else {
                final MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme))
                        .setHeaderDrawable(d)
                        .setIcon(R.drawable.ic_clear_red_24dp)
                        .withDialogAnimation(true)
                        .setTitle(name)
                        .setDescription(getString(R.string.image_rec_fail))
                        .setCancelable(false)
                        .setPositiveText(getString(R.string.ok))
                        .onPositive(
                                (dialog12, which) -> {
                                    dialog12.dismiss();
                                }
                        ).setScrollable(true, 5).build();

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
        final MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(this)
                .setHeaderDrawable(d)
                .setIcon(R.drawable.ic_help_outline_yellow_24dp)
                .withDialogAnimation(true)
                .setTitle(conceptName)
                .setDescription(this.getString(R.string.feedback))
                .setCancelable(true)
                .setPositiveText(this.getString(R.string.yes))
                .onPositive(
                        (dialog12, which) -> {
                            //Yes button clicked
                            try {
                                jsonRequest.put("answer", 1);
                                new UploadFileToServerTask().execute(jsonRequest.toString(), FEEDBACK_URL);
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                )
                .setNegativeText(this.getString(R.string.no))
                .onNegative(
                        (dialog12, which) -> {
                            //Yes button clicked
                            try {
                                jsonRequest.put("answer", 0);
                                new UploadFileToServerTask().execute(jsonRequest.toString(), FEEDBACK_URL);
                            } catch (JSONException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                )
                .setNeutralText(this.getString(R.string.dont_answer));
        dialog.show();
    }

    /*
    * When the app is in background and when a different fragment is selected
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
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationsReceiver);
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
        saveCurrentPath();
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
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(locationsReceiver);
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();

    }

    private class UploadFileToServerTask extends AsyncTask<String, Void, String> {
        ProgressDialog progDailog;
        String JsonUrl;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(RouteActivity.this);
            progDailog.setMessage(getString(R.string.upload_general));
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String JsonResponse;
            String JsonDATA = params[0];
            JsonUrl = params[1];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Log.d("response", "begin");
            try {
                //Create a URL object holding our url
                URL url = new URL(JsonUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setConnectTimeout(30000);//stop after 15 secs
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
                Log.e(TAG, e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ERROR", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            progDailog.dismiss();
            if (JsonUrl.equals(IMAGE_SCAN_URL))
                onImageResponseReceived(response);
            else if (JsonUrl.equals(FEEDBACK_URL))
                onFeedbackResponseReceived(response);
            else if (JsonUrl.equals(URL_SEND_ROUTE))
                onRouteSentResponseReceived(response);
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
        // Get the likely places - that is, the businesses and other routePoints of interest that
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
                Log.e("ERROR", "Exception: %s", task.getException());
            }
        });

    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = (dialog, which) -> {
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
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Receiver for broadcasts sent by {@link LocationUpdatesService}.
     */
    private class LocationsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(EXTRA_LOCATION);
            if (location != null) {
                //add routePoints and redraw the trajectory line
                routePoints.add(new LatLng(location.getLatitude(), location.getLongitude()));
                redrawLine();
                onNewLocation(location);
            }
        }
    }

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
            prefs.edit().putString("bmp"+i, Utils.bitMapToBase64(photo.getImgBitmap())).apply();
            i++;

        }
        //saveMarkersOnStorage(FILENAME, imageMarkers);
    }


    private void redrawLine(){
        PolylineOptions options = new PolylineOptions()
                .width(10)
                .color(Color.RED)
                .pattern(pattern)
                .geodesic(true);

        for (int i = 0; i < routePoints.size(); i++) {
            LatLng point = routePoints.get(i);
            options.add(point);
        }

        line = mMap.addPolyline(options); //add Polyline
    }

    //show dialog to name route and save it
    private void endRoute(){
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.save_tour_dialog_box, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText routeTitleBox = promptsView.findViewById(R.id.tour_title_box);
        //Log.d("box", routeTitleBox.toString()+"");
        final EditText routeDescriptionBox = promptsView.findViewById(R.id.tour_description_box);
        TextView dialogTitle = promptsView.findViewById(R.id.dialog_description);
        dialogTitle.setText(getString(R.string.save_tour_dialog_title));
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        (dialog, id) -> {
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

        Utils.setRouteState(this, ROUTE_STATE.STOPPED);

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String boxTitle = routeTitleBox.getText().toString();
            String boxDescription = routeDescriptionBox.getText().toString();
            if (TextUtils.isEmpty(boxTitle)){
                Log.d("text", "here");
                routeTitleBox.setError(getString(R.string.fill_out));
                routeTitleBox.requestFocus();
            }
            else if (TextUtils.isEmpty(boxDescription)){
                routeDescriptionBox.setError(getString(R.string.fill_out));
                routeDescriptionBox.requestFocus();
            }
            else {
                if (routePoints.isEmpty()){
                    Toast.makeText(this, getString(R.string.route_empty), Toast.LENGTH_SHORT).show();
                    photos.clear();
                    alertDialog.dismiss();
                }
                else {
                    // get user input and create a route object
                    Route route = new Route(routeTitleBox.getText().toString(), line,
                            routeDescriptionBox.getText().toString());
                    List <Photo> photoItemsToSend = new ArrayList<>();
                    for (Photo p : photos){
                        photoItemsToSend.add(p);
                    }
                    RouteInstance rt = new RouteInstance(new Date(begginingDate), new Date(), route, photoItemsToSend);

                    //Save to Storage and send to the server
                    saveRouteToFile(rt);
                    JSONObject jsonT = placeRoutesOnJson(rt);//
                    alertDialog.dismiss();
                    askForRoutePrivacity(jsonT);
                    //delete route data
                    getSharedPreferences(TEMP_ROUTE_POINTS_FILE, MODE_PRIVATE).edit().clear().commit();
                }
            }
        });
    }

    private void askForRoutePrivacity(JSONObject json){
        final MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(this)
                .setIcon(R.drawable.ic_assistant_photo_black_24dp)
                .withDialogAnimation(true)
                .setTitle(getString(R.string.route_privacy))
                .setDescription(getString(R.string.route_privacy_details))
                .setCancelable(false)
                .setPositiveText("Make it public")//public
                .onPositive(
                        (dialog12, which) -> {
                            dialog12.dismiss();
                            try {
                                json.put("state", 1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            new UploadFileToServerTask().execute(json.toString(), URL_SEND_ROUTE);
                        }
                )
                .setNegativeText("Make it private")
                .onNegative((dialog12, which) -> {
                    dialog12.dismiss();
                    try {
                        json.put("state", 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new UploadFileToServerTask().execute(json.toString(), URL_SEND_ROUTE);
                });
        dialog.setScrollable(true, 5);
        dialog.show();

    }


    /**
     * Saving in JSON format:
     * { Title : "",
     *   Description : "",
     *   StartDate: "",
     *   EndDate: "",
     *   Markers :[
     *
     *      {
     *      Titl : "" ,
     *      Snippet  : "",
     *      Latitude : "",
     *      Longitude : " " ,
     *      Icon : "bitmaptostring replaced / with // "
     *      }
     *
     *      ]
     * }
     * @param routeInstance RouteInstance to save
     */
    private void saveRouteToFile(RouteInstance routeInstance){
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        //Title and description
        sb.append("\"Title\" : " + "\"" + routeInstance.getRoute().getRouteTitle() +"\",\n");
        sb.append("\"Description\" : " + "\"" + routeInstance.getRoute().getRouteDescription() +"\",\n");
        sb.append("\"StartDate\" : " + "\"" + routeInstance.getStartDate().toString() +"\",\n");
        sb.append("\"EndDate\" : " + "\"" + routeInstance.getEndDate().toString() +"\",\n");
        sb.append("\"Markers\" : [\n");
        //Markers
        List<Photo> temp = routeInstance.getBitmaps();
        int tmp = 0;
        List<LatLng> polyPoints  = routeInstance.getRoute().getRoutePath().getPoints();

        for (Photo p : temp)
        {
            if (polyPoints.contains(p.getLatLngLocation())){
                //Marker number
                if (tmp > 0)
                    sb.append(",\n");
                sb.append("{");
                //Marker information
                sb.append("\"Titl\" : " + "\"" + p.getConcept() + "\",\n");
                sb.append("\"Snippet\" : " + "\"" + p.getDescription() + "\",\n");
                sb.append("\"Latitude\" : " + "\"" + p.getLatLngLocation().latitude + "\",\n");
                sb.append("\"Longitude\" : " + "\"" + p.getLatLngLocation().longitude + "\",\n");
                String tmpo = bitMapToBase64(p.getImgBitmap());
                String imageFix = tmpo.replaceAll("/","//");
                sb.append("\"Icon\" : " + "\"" + imageFix + "\"");
                //End marker
                sb.append("}");
                tmp++;
            }
        }
        sb.append("],\n");
        sb.append("\"Poly\" : [\n");

        //Poly routePoints

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

        String filename = "route"+ routeInstance.getRoute().getRouteTitle() +".json";
        FileOutputStream outputStream;
        try {
            outputStream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(sb.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    private void saveCurrentPath(){
        //Poly routePoints
        StringBuilder sb = new StringBuilder();
        prefs = getSharedPreferences(TEMP_ROUTE_POINTS_FILE, MODE_PRIVATE);
        //sb.append("{\n");
        //sb.append("\"Markers\" : [\n");
        int tmp = 0;
        //Gson gson = new Gson();
        tmp = 0;
        for (LatLng l : routePoints){
            prefs.edit().putString("Lat"+tmp, String.valueOf(l.latitude)).apply();
            prefs.edit().putString("Lng"+tmp, String.valueOf(l.longitude)).apply();
            /*sb.append("{");
            sb.append("\"Longitude\" : " + "\"" +l.longitude + "\", ");
            sb.append("\"Latitude\" : " + "\"" +l.latitude + "\"");
            sb.append("}");
            if (tmp < routePoints.size()-1){
                sb.append(",\n");
            }*/
            tmp++;
        }
        //sb.append("]\n");
        //sb.append("\n}");

        /*FileOutputStream outputStream;
        try {
            outputStream = this.openFileOutput(TEMP_ROUTE_POINTS_FILE, Context.MODE_PRIVATE);
            outputStream.write(sb.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }*/
    }

    private void getCurrentPath() {
        routePoints = new ArrayList<>();
        prefs = getSharedPreferences(TEMP_ROUTE_POINTS_FILE, MODE_PRIVATE);
        boolean stop = false;
        int tmp = 0;
        while (!stop) {
            if (prefs.contains("Lat" + tmp)) {
                String lat = prefs.getString("Lat" + tmp, "");
                String lng = prefs.getString("Lng" + tmp, "");
                LatLng l = null;
                try {
                    l = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                    routePoints.add(l);
                } catch (NumberFormatException e) {
                    //there were no double parselable coordinates found, marker will not be placed
                    Log.e("ERROR", e.toString());
                    tmp++;
                    continue;
                }
                tmp++;
            } else {
                stop = true;
            }
        }
    }


    //places a route on json (INCOMPLETE!!! missing title and description
    // put trajectory coordinates as string and marker's id as string)
    public JSONObject placeRoutesOnJson(RouteInstance rt){
        JSONObject j = new JSONObject();
        //String routePoints = "40.6442700,-8.6455400;40.6442704,-8.6455401;40.6442705,-8.6455402";
        //add markers
        //send image ids as string (server has problems converting from int[])
        String listOfMarkers = "";

        //int[] listOfMarkers = new int[markerID.size()];
        //save for each marker, the image id, and their coords
        int i = 0;
        for (Photo photo : photos){
            if (i == photos.size()-1)
                listOfMarkers+=photo.getId();
            else
                listOfMarkers+=photo.getId()+",";
            i++;
        }
        try {
            j.put("title", rt.getRoute().getRouteTitle());
            j.put("description", rt.getRoute().getRouteDescription());
            j.put("start", Utils.convertTimeInMilisAndFormat(begginingDate));
            j.put("end", Utils.convertTimeInMilisAndFormat(Calendar.getInstance().getTimeInMillis()));
            j.put("user", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            j.put("markers", listOfMarkers);
            if (isFollowingTour)
                j.put("route", routeToFollow.getId());//this route is not to be created, only instanciated
            else
                j.put("route", 0);
            //j.put("state", <0 - privado, 1 - publico>);
            String rpts = "";
            for(int z = 0; z < routePoints.size();z++){
                rpts+= routePoints.get(z).latitude + "," + routePoints.get(z).longitude + ";";
            }
            rpts = rpts.substring(0, rpts.length() - 1);

            j.put("trajectory", rpts);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("ROUTE", j.toString());
        return j;
    }

}