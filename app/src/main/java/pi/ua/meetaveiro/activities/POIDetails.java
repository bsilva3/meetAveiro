package pi.ua.meetaveiro.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;
import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.AttractionImageSliderAdapter;
import pi.ua.meetaveiro.adapters.RouteExpandable;
import pi.ua.meetaveiro.interfaces.DataReceiver;
import pi.ua.meetaveiro.data.Attraction;
import pi.ua.meetaveiro.data.Route;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.Utils;

import static pi.ua.meetaveiro.others.Constants.URL_ATTRACTION_INFO;
import static pi.ua.meetaveiro.others.Constants.URL_ROUTES_IN_ATTRACTION;

//ToDo: Image slider only has one image... there should be two... is it parsing both correctly?
//is it overrding the first on the slider?
public class POIDetails extends AppCompatActivity {
    private Attraction attraction;
    private String attractionName;
    private TextView description;
    private static ViewPager mPager;
    FloatingActionButton map;
    private static int currentPage = 0;
    private List<Bitmap> imagesArray;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;
    private ShimmerFrameLayout mShimmerViewContainer;
    RouteExpandable listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<Route>> listDataChild;
    private List<Route> routeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poidetails);
        imagesArray = new ArrayList<>();
        //get the intent which tell us which concept this page contains
        if (getIntent().hasExtra("attraction")) {
            attractionName = getIntent().getExtras().getString("attraction");
            collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.attraction_collapsing_toolbar);
            collapsingToolbar.setTitleEnabled(true);
            collapsingToolbar.setTitle(attractionName);
            //attraction photos, description.. from intent if any
        }
        toolbar = findViewById(R.id.attraction_toolbar);
        setSupportActionBar(toolbar);
        // showss back button
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        //collapsingToolbar.post() { collapsingToolbar.requestLayout(); }

        //toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        map = (FloatingActionButton) findViewById(R.id.show_on_map);
        map.setVisibility(View.GONE);
        description = (TextView) findViewById(R.id.attraction_description);


        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.routes_with_attraction);
        routeList = new ArrayList<>();
        // preparing list data
        prepareListData();

        listAdapter = new RouteExpandable(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(POIDetails.this, RouteDetailsActivity.class);
                intent.putExtra("", "");

                startActivity(intent);
                return false;
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
            }
        });

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                setListViewHeight(parent, groupPosition);
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //requestRoutes();
        getAttractionInfo();
        getRoutesThatHaveAttraction();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Try to free up some memory!
        imagesArray = new ArrayList<>();
        System.gc();
    }


    private void initImageSlider() {

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new AttractionImageSliderAdapter(POIDetails.this, imagesArray));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == imagesArray.size()) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 2500, 7500);
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Route>>();

        // Adding child data
        listDataHeader.add(getString(R.string.routes_in_attr)+" ( 0 )");

        // Adding child data
        /*routeList = new ArrayList<Route>();
        routeList.add(new Route("Salreu", "Nice wal in salreu's corn"));
        routeList.add(new Route("Aveiro city", "Beatifull tour in Aveiro"));
        routeList.add(new Route("UA", "Route that goes around UA"));
        */
    }

    public void placeRoutesOnListExp(List<Route> routes){
        //add the number of routes found
        String s = listDataHeader.get(0);
        listDataHeader.clear();
        listDataHeader.add(getString(R.string.routes_in_attr)+" ("+routeList.size()+")");
        //add the routes to the list
        listDataChild.put(listDataHeader.get(0), routes);
    }

    private void setListViewHeight(ExpandableListView listView,
                                   int group) {
        ExpandableListAdapter listAdapter = (ExpandableListAdapter) listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void getAttractionInfo() {
        //start shimmer effect
        mShimmerViewContainer.startShimmerAnimation();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL_ATTRACTION_INFO+attractionName, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("res", response.toString());
                description = findViewById(R.id.attraction_description);
                String name;
                String lat;
                String longt;
                JSONArray photosArray;
                try {
                    photosArray = response.getJSONArray("photos");
                    //download the photos from url (usually, its just 2
                    //Log.d("url", photosArray.get(0).toString().replace("\\", ""));
                    for (int i = 0; i < photosArray.length(); i++){
                        downloadImageFromURL(photosArray.get(i).toString().replace("\\", ""));
                    }
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    name = response.getString("name");
                    collapsingToolbar.setTitle(name);
                    description.setText(response.get("description").toString());
                    lat = response.getString("latitude");
                    longt = response.getString("longitude");
                    if (lat != null && longt != null) {
                        //set a click listener to show the attraction on a map
                        map.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                //this intent simply opens the google maps app on the sent coordinates
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + longt + "?q=<"+lat+">,<"+longt+">("+name+")"));
                                startActivity(intent);
                            }
                        });
                        map.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //images.add();
                // stop animating Shimmer and hide the layout
                //initImageSlider();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                // stop animating Shimmer and hide the layout
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
            }
        });

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    public void downloadImageFromURL(String url){
        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        imagesArray.add(bitmap);
                        Log.d("imagesArray", imagesArray.size()+"");
                        initImageSlider();
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {

                    }
                });
// Access the RequestQueue through your singleton class.
        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void getRoutesThatHaveAttraction() {
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("concept", attractionName);
            jsonRequest.put("user", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        } catch (JSONException e) {
            Log.e("Request Route Error", e.toString());
        }
        //start shimmer effect
        mShimmerViewContainer.startShimmerAnimation();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                URL_ROUTES_IN_ATTRACTION, jsonRequest, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("ERROR", response.toString());
                routeList = new ArrayList<>();
                try {
                    // Parsing json object response
                    Log.d("res", response.toString());
                    JSONArray jsonArray = null;
                    try {
                        jsonArray = response.getJSONArray("routes");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (jsonArray != null) {
                        Route rt = new Route();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            rt.setRouteTitle(jsonArray.getJSONObject(i).getString("title"));
                            rt.setRouteDescription(jsonArray.getJSONObject(i).getString("description"));
                            //INCOMPLETE! fazer o assign do id
                            rt.setId(jsonArray.getJSONObject(i).getInt("id"));
                            routeList.add(rt);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(POIDetails.this, getString(R.string.server_connect_error), Toast.LENGTH_SHORT).show();
                }
                placeRoutesOnListExp(routeList);
                // stop animating Shimmer and hide the layout
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("ERROR", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        getString(R.string.server_connect_error), Toast.LENGTH_SHORT).show();
                // stop animating Shimmer and hide the layout
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
            }
        });

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(jsonObjReq);
    }
}