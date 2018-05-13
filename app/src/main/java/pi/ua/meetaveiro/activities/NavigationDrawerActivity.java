package pi.ua.meetaveiro.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pi.ua.meetaveiro.adapters.AttractionAdapter;
import pi.ua.meetaveiro.adapters.RouteAdapter;
import pi.ua.meetaveiro.fragments.AccountSettingsFragment;
import pi.ua.meetaveiro.fragments.AttractionListFragment;
import pi.ua.meetaveiro.fragments.HistoryFragment;
import pi.ua.meetaveiro.fragments.PhotoLogFragment;
import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.fragments.RouteListFragment;
import pi.ua.meetaveiro.models.Attraction;
import pi.ua.meetaveiro.models.Route;
import pi.ua.meetaveiro.others.Utils;
import pi.ua.meetaveiro.services.LocationUpdatesService;

public class NavigationDrawerActivity extends AppCompatActivity implements
        AttractionAdapter.OnAttractionSelectedListener,
        RouteAdapter.OnRouteItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        HistoryFragment.OnBottomHistoryNavigationInteractionListener {

    public static final int PERMISSIONS_REQUEST = 1889;
    private static final String TAG = NavigationDrawerActivity.class.getSimpleName();

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg;
    private RelativeLayout collapseContent;
    private TextView txtName, txtWebsite;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private FloatingActionButton newRouteFab;

    // urls to load navigation header background image
    // and profile image
    private static final String urlNavHeaderBg = "http://hybrids.web.ua.pt/cost2017/images/aveiro-ceu-cinema-pro-1.jpg?crc=503218561";

    //Currently logged user
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_PHOTO_LOG = "home";
    private static final String TAG_ATTRACTIONS = "attractions";
    private static final String TAG_ROUTES = "routes";
    private static final String TAG_HISTORY = "history";
    private static final String TAG_ACCOUNT_SETTINGS = "photos";
    public static String CURRENT_TAG = TAG_PHOTO_LOG;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    //Current home fragment
    Fragment currentFragment;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;
    private FirebaseAuth auth;

    private Bundle savedState;
    private boolean mPermissionsGranted = false;

    // A reference to the service used to get location updates.
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_navigation_drawer);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        newRouteFab = findViewById(R.id.new_route_fab);
        newRouteFab.setOnClickListener(v -> {
            Intent myIntent = new Intent(NavigationDrawerActivity.this, RouteActivity.class);
            NavigationDrawerActivity.this.startActivity(myIntent);
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mHandler = new Handler();

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = navHeader.findViewById(R.id.name);
        txtWebsite = navHeader.findViewById(R.id.website);
        imgNavHeaderBg = navHeader.findViewById(R.id.img_header_bg);

        //Collapse content
        collapseContent = findViewById(R.id.collapse_content);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        savedState = savedInstanceState;

        // First and foremost get permissions
        getPermissions();

        setupNavigationFragments();

    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToolbarTitle();
        handleCollapse();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s.equals(Utils.KEY_ROUTE_STATE)) {
            //on(sharedPreferences.getBoolean(Utils.KEY_ROUTE_STATE, false));
        }
    }

    //Called when a item is clicked on the attraction list fragment
    @Override
    public void onAttractionSelected(Attraction item) {
        Intent intent = new Intent(NavigationDrawerActivity.this, POIDetails.class);
        intent.putExtra("attraction", (Parcelable) item);
        startActivity(intent);
    }

    //Called when a item is clicked on the route list fragment
    @Override
    public void onRouteSelected(Route item) {

    }

    @Override
    public void onBottomHistoryItemSelected(Fragment fragment) {
        this.loadFragment(R.id.frame_container, fragment);
    }

    private void setupNavigationFragments(){
        if(mPermissionsGranted) {
            // load nav menu header data
            loadNavHeader();

            // initializing navigation menu
            setUpNavigationView();

            if (savedState == null) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_PHOTO_LOG;
                loadHomeFragment();
            }
        }
    }

    private void getPermissions() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            // Should we show an explanation?
            if(listPermissionsNeeded.size()>0){
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSIONS_REQUEST);
                return;
            }
        }
        mPermissionsGranted = true;
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    private void loadNavHeader() {
        // name, website
        txtName.setText(getString(R.string.app_name));
        txtWebsite.setText(user.getEmail());

        // loading header background image
        Glide.with(this).load(urlNavHeaderBg).into(imgNavHeaderBg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "Camera and Location permission granted");
                        mPermissionsGranted = true;
                        setupNavigationFragments();
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        // permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                      // shouldShowRequestPermissionRationale will return true
                        // show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showDialogOK("Camera and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    getPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    finish();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        // permission is denied (and never ask again is  checked)
                        // shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(navItemIndex==0)
            getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            ((PhotoLogFragment) currentFragment).showCurrentPlace();
        }
        return true;
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                currentFragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, currentFragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();

    }

    private void handleCollapse(){
        switch (navItemIndex) {
            case 0:
                disableCollapse();
                newRouteFab.setVisibility(View.GONE);
                break;
            case 1:
                enableCollapse();
                newRouteFab.setVisibility(View.GONE);
                break;
            case 2:
                enableCollapse();
                newRouteFab.setVisibility(View.VISIBLE);
                break;
            case 3:
                disableCollapse();
                newRouteFab.setVisibility(View.GONE);
                break;
            case 4:
                disableCollapse();
                newRouteFab.setVisibility(View.GONE);
                break;
            default:
                disableCollapse();
                newRouteFab.setVisibility(View.GONE);
                break;
        }
    }

    private Fragment getHomeFragment() {
        handleCollapse();
        switch (navItemIndex) {
            case 0:
                // Photo log fragment
                return new PhotoLogFragment();
            case 1:
                // Attraction list fragment
                return new AttractionListFragment();
            case 2:
                // Route List fragment
                return new RouteListFragment();
            case 3:
                // History fragment
                return new HistoryFragment();
            case 4:
                // Account settings fragment
                return new AccountSettingsFragment();
            default:
                return new PhotoLogFragment();
        }
    }

    public void disableCollapse() {
        collapseContent.setVisibility(View.GONE);
    }

    public void enableCollapse() {
        collapseContent.setVisibility(View.VISIBLE);
    }

    private void setToolbarTitle() {
        if (navItemIndex == 1) {
            collapsingToolbar.setTitleEnabled(true);
            collapsingToolbar.setTitle(activityTitles[navItemIndex]);
        } else if ( navItemIndex == 2 ) {
            collapsingToolbar.setTitleEnabled(true);
            collapsingToolbar.setTitle(activityTitles[navItemIndex]);

        } else {
            collapsingToolbar.setTitleEnabled(false);
            getSupportActionBar().setTitle(activityTitles[navItemIndex]);
        }
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.option_photo_log:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_PHOTO_LOG;
                        break;
                    case R.id.option_attractions:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_ATTRACTIONS;
                        break;
                    case R.id.option_routes:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_ROUTES;
                        break;
                    case R.id.option_history:
                        navItemIndex = 3;
                        CURRENT_TAG = TAG_HISTORY;
                        break;
                    case R.id.option_account_settings:
                        navItemIndex = 4;
                        CURRENT_TAG = TAG_ACCOUNT_SETTINGS;
                        break;
                    case R.id.option_logout:
                        //Sign user out
                        signOut();
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(NavigationDrawerActivity.this, LoginActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_about_us:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(NavigationDrawerActivity.this, AboutUsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_PHOTO_LOG;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    public void loadFragment(int id, Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(id, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
