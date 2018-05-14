package pi.ua.meetaveiro.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pi.ua.meetaveiro.activities.POIDetails;
import pi.ua.meetaveiro.adapters.TourOptionsAdapter;
import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.interfaces.DataReceiver;
import pi.ua.meetaveiro.interfaces.ImageDataReceiver;
import pi.ua.meetaveiro.models.Attraction;
import pi.ua.meetaveiro.models.Route;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.Utils;

import static android.content.Context.MODE_PRIVATE;
import static pi.ua.meetaveiro.others.Constants.*;

/**
 * Photo logging and route making {@link Fragment} subclass.
 */
public class PhotoLogFragment extends Fragment implements
        OnMapReadyCallback,
        ImageDataReceiver,
        TextToSpeech.OnInitListener{

    //Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int CAMERA_REQUEST = 1888;
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

    //map to store marker that are not yet updated with information
    private Map<Marker, Boolean> markers;
    //map that stores the image in each map
    private Map<Marker, Bitmap> imageMarkers = new HashMap<>();

    private FloatingActionButton buttonAddPhoto;

    //To store in local
    private SharedPreferences prefs = null;

    //to store the points obtained for the path being drawn at the moment
    private ArrayList<LatLng> points;
    //used to store the line being drawn at the moment
    private Polyline currentDrawingLine;
    //stores the tours currently showing on the map
    private List<Route> routes;
    private int[] lineColors = {Color.BLUE, Color.GREEN, Color.RED, Color.GRAY, Color.CYAN, Color.YELLOW,
            Color.MAGENTA, Color.WHITE};
    //each time a new route is drawn, the path will have another color
    private static int colorIndex;
    //used to preserve the routes displayed on map
    private MyApplication myApp;
    //bottom sheet
    private BottomSheetBehavior mBottomSheetBehavior;
    //bottom sheet's list of options
    private ListView tourOptionsListView;
    private TextView tourTitleText;
    private TextView tourDescriptionText;
    //name of the option
    private String[] optionsText;
    //icon for the option
    private Integer[] optionsImages;



    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 10;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markers = new HashMap<>();
        imageMarkers = new HashMap<>();
        mRequestingLocationUpdates = false;

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(getContext());

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(getContext());

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        mSettingsClient = LocationServices.getSettingsClient(getContext());

        points = new ArrayList<>(); //stores all points during the tour so that a trajectory line can be drawn using those points
        routes = new ArrayList<>();
        colorIndex = 0;
        //for now, there's only the options to remove the tour from the marker
        // and to rename tours....
        optionsText = new String[]{getContext().getString(R.string.remove_tour_from_map_btn),
                getContext().getString(R.string.edit_tour_name)}; //options
        optionsImages = new Integer[]{R.drawable.ic_highlight_off_black_24dp,
                R.drawable.ic_edit_black_24dp}; //icons for the options

        tts = new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

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
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        });

        //start bottom sheet
        mBottomSheetBehavior = BottomSheetBehavior.from(getActivity().findViewById(R.id.bottom_sheet_tour));
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setPeekHeight(0);//completely hide bottom sheet on not expanded
        //start listview for bottom sheet
        TourOptionsAdapter adapter = new TourOptionsAdapter(getActivity(), optionsText, optionsImages);
        tourOptionsListView = getActivity().findViewById(R.id.list_view_tour_options);
        tourTitleText = getActivity().findViewById(R.id.tour_name);
        tourDescriptionText = getActivity().findViewById(R.id.tour_description);
        tourOptionsListView.setAdapter(adapter);
        myApp = (MyApplication) getActivity().getApplicationContext();

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

    @Override
    public void onResume() {
        super.onResume();

        routes = myApp.getRoutes();
        if (routes == null)
            routes = new ArrayList<>();
        Log.d("route", routes.size()+"");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //leaving fragment, store the routes so that we can display them again later
        Log.d("route", routes.size()+" saving");
        myApp.setRoutes(routes);
    }

    public void placeRoutesOnMap(List<Route> routes){
        mMap.clear();
        Boolean firstRoute = true;
        for (Route r : routes) {
            Log.d("redraw", "redrawing");
            //place the lines..
            if (firstRoute) {
                redrawLine(r.getRoutePoints(), false);//dont switch line color yet
                firstRoute = false;
            }
            else
                redrawLine(r.getRoutePoints(), true);
            //place the markers with the photos
            for (Map.Entry<Marker, Bitmap> entry : r.getRouteMarkers().entrySet()) {
                mMap.addMarker(new MarkerOptions().position(entry.getKey().getPosition())
                        .title(entry.getKey().getTitle()).snippet(entry.getKey().getSnippet())
                        .icon(BitmapDescriptorFactory.fromBitmap(entry.getValue())));
            }
        }
        //when a path is clicked, a menu will appear
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Log.d("clicked", "line clicked");
                    Route route = checkWhatTourPolyBelongs(polyline);
                    if (route != null) {
                        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            tourTitleText.setText(route.getRouteTitle());
                            tourDescriptionText.setText(route.getRouteDescription());
                            tourOptionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view,
                                                        int position, long id) {
                                    String selecteditem = optionsText[+position];
                                    //remove this route from the map
                                    if (selecteditem.equals(getContext().getString(R.string.remove_tour_from_map_btn))) {
                                        routes.remove(route);
                                        placeRoutesOnMap(routes);
                                    } //edit tour
                                    else if (selecteditem.equals(getContext().getString(R.string.edit_tour_name))) {
                                        LayoutInflater li = LayoutInflater.from(getContext());
                                        View promptsView = li.inflate(R.layout.save_tour_dialog_box, null);
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                        alertDialogBuilder.setView(promptsView);
                                        final EditText routeTitleBox = (EditText) promptsView.findViewById(R.id.tour_title_box);
                                        final EditText routeDescriptionBox = (EditText) promptsView.findViewById(R.id.tour_description_box);
                                        TextView dialogTitle = (TextView) promptsView.findViewById(R.id.dialog_description);
                                        dialogTitle.setText(getString(R.string.edit_tour_dialog_title));
                                        routeTitleBox.setText(route.getRouteTitle());
                                        routeDescriptionBox.setText(route.getRouteDescription());

                                        alertDialogBuilder
                                                .setCancelable(false)
                                                .setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                //update route in the array that has all routes on the map
                                                                //and update info in bottom sheet
                                                                routes.remove(route);
                                                                route.setRouteTitle(routeTitleBox.getText().toString());
                                                                route.setRouteDescription(routeDescriptionBox.getText().toString());
                                                                routes.add(route);
                                                                saveRouteToFile(route);
                                                                tourTitleText.setText(route.getRouteTitle());
                                                                tourDescriptionText.setText(route.getRouteDescription());
                                                            }
                                                        })
                                                .setNegativeButton(getString(R.string.cancel),
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                            }
                                                        });
                                         // create alert dialog
                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        // show it
                                        alertDialog.show();
                                    }
                                    //Toast.makeText(getContext(), selecteditem, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        //mButton1.setText(R.string.button1);
                    }

             }
        });
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
        prefs = getActivity().getSharedPreferences("LatLng",MODE_PRIVATE);
        Log.d("route", routes.size()+" placing");
        if (!routes.isEmpty())
            placeRoutesOnMap(routes);

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
                Bitmap img = Utils.StringToBitMap(btm);
                try {
                    Marker m = mMap.addMarker(new MarkerOptions().position(l)
                            .icon(BitmapDescriptorFactory.fromBitmap(img))
                            .title(titl)
                            .snippet(snip));
                    imageMarkers.put(m,img);
                    markers.put(m,true);

                }catch (Exception e){
                    Marker m = mMap.addMarker(new MarkerOptions().position(l)
                            .title(titl)
                            .snippet(snip));
                    markers.put(m,true);
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
            case CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 70, bos);
                    String base64Photo = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT);
                    //create json with server request, and add the photo base 64 encoded
                    Log.d("lst", mLastKnownLocation.toString());
                    JSONObject jsonRequest = new JSONObject();
                    try {
                        jsonRequest.put("image", base64Photo);
                        jsonRequest.put("lat", mLastKnownLocation.getLatitude());
                        jsonRequest.put("long", mLastKnownLocation.getLongitude());
                        jsonRequest.put("user", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    if (mLastKnownLocation!=null) {
                        //add the marker to the map of markers, but indicate that this marker
                        //does not have an updated info yet
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                                .title(getContext().getString(R.string.unknown_string))
                                .snippet(getContext().getString(R.string.unknown_string))
                                .icon(BitmapDescriptorFactory.fromBitmap(photo)));
                        markers.put(m, false);
                        imageMarkers.put(m, photo);
                        //send a base 64 encoded photo to server
                        Log.d("req", jsonRequest.toString()+"");
                        new uploadFileToServerTask().execute(jsonRequest.toString(), IMAGE_SCAN_URL);
                    }else {
                        //Report error to user
                        Toast.makeText(getContext(), "Location not known. Check your location settings.", Toast.LENGTH_SHORT).show();
                    }
                }
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

    public String bitMapToBase64 (Bitmap image){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onImageResponseReceived(Object result) {
        JSONObject json = null;
        try {
            json = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        } catch (java.lang.NullPointerException e){
            Toast.makeText(getContext(), "Connection error. Please try again later", Toast.LENGTH_LONG).show();
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
        int id = 0;
        String concept = "";
        int imageId = 0;
        try {
            markerToUpdate.setTitle(json.get("name").toString());
            markerToUpdate.setSnippet(json.get("description").toString());
            concept = json.get("concept").toString();
            id = Integer.parseInt(json.get("answer").toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        markers.put(markerToUpdate, true);

        //update the marker in the map that stores every marker and the image in the marker
        final int idFinal = id;
        updateMarkerOnMap(oldMarker, markerToUpdate);
        mMap.setOnMarkerClickListener(m -> {
            Log.d("markerClick", "clicked");
            if (m != null) {
                Log.d("markerClickI", "clicked with info");
                createAndShowInfoDialog(m, idFinal);
                return true;
            }
            return false;
        });
        //shows a dialog with info about the concept. It also shows a prompt for user feedback on the first dialog is closed
        createAndShowInfoDialog(markerToUpdate, id);
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

    //used to save the updated marker with the info obtained from the server
    private void updateMarkerOnMap(Marker old, Marker newMarker){
        Bitmap bit = imageMarkers.get(old);
        imageMarkers.remove(old);
        imageMarkers.put(newMarker, bit);
    }

    private void createAndShowInfoDialog(Marker m, int id){
        String name = m.getTitle();
        String description = m.getSnippet();
        Drawable d = new BitmapDrawable(getResources(), imageMarkers.get(m));
        //image was recognized
        if (!name.toLowerCase().equals("desconhecido")) {
            //when the dialog with the description is closed, we show a feedback box;
            //the user says if the app was able to identify the image succesfully, or close the dialog
            //without providing an answer
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
                                showFeedbackDialogAndSend(name, id, d);
                            }
                    )
                    .onNeutral((dialog1, which) -> startActivity(new Intent(getActivity(), POIDetails.class)
                            .putExtra("attraction", name)))
                    .setNeutralText(getContext().getString(R.string.see_more_info));
            dialog.setScrollable(true, 5);
            dialog.show();
        }
        else{
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
        byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(new Locale("pt", "PT"));

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
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
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

    private Polyline redrawLine(List<LatLng> points, boolean nextColor){
        if (currentDrawingLine!=null) {
            if (currentDrawingLine.getPoints().equals(points))
                currentDrawingLine.remove();
        }

        PolylineOptions options = new PolylineOptions().width(10)
                .color(getNextColor(nextColor)).geodesic(true);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            options.add(point);
        }
        currentDrawingLine = mMap.addPolyline(options); //add Polyline
        currentDrawingLine.setClickable(true);
        return currentDrawingLine;
    }
    //simple method to change the color of a polyline. if false, returns the color of the
    // current index (usefull when on route and we want to maintain the same color)
    //if true, increments the index and returns the next color (usefull when placing all routes
    //and we want to change the color of each path
    private int getNextColor(boolean nextColor){
        if (nextColor) {
            colorIndex++;
            if (colorIndex == lineColors.length)
                colorIndex = 0;
        }
        return lineColors[colorIndex];

    }

    //used to check to which route these points belong to
    //returns null if these points are not in any current path show on map
    private Route checkWhatTourPolyBelongs(Polyline polyline){
        for (Route rout : routes){
            Log.d("route", rout.getRoutePath()+""+polyline);
            if (rout.getRoutePoints().equals(polyline.getPoints()))
                return rout;
        }
        return null;
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
     *      Icon : "bitmaptostring replaced / with // "
     *      }
     *
     *      ]
     * }
     * @param route Route to save
     */
    private void saveRouteToFile(Route route){
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
                String tmpo = bitMapToBase64(entry.getValue());
                String imageFix = tmpo.replaceAll("/","//");
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
        }

    }

}