package pi.ua.meetaveiro.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
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

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;
import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.AttractionImageSliderAdapter;
import pi.ua.meetaveiro.adapters.RouteAdapter;
import pi.ua.meetaveiro.adapters.RouteExpandable;
import pi.ua.meetaveiro.fragments.PhotoLogFragment;
import pi.ua.meetaveiro.interfaces.DataReceiver;
import pi.ua.meetaveiro.models.Attraction;
import pi.ua.meetaveiro.models.Route;
import pi.ua.meetaveiro.others.MyApplication;

import static pi.ua.meetaveiro.activities.NavigationDrawerActivity.navItemIndex;
import static pi.ua.meetaveiro.others.Constants.API_URL;
import static pi.ua.meetaveiro.others.Constants.URL_ATTRACTIONS;
import static pi.ua.meetaveiro.others.Constants.URL_ROUTES_ATTRACTION;

/*
eu imagino uma collapsing toolbar
        com o slide show
        1 botao entre a informaçao do conceito e o slideshow
        por cima entre ambas as views
        a informaçao em si em card views
        e no final uma expandable com a listview dos percursos
        ah esse botao faz o tal intent
        pro google maps
        */
/*funcionalidades:
- um intent pro google maps pra ir pro conceito em causa;
- ver todos os percursos em que esse conceito aparece;
- um slide show com imagens tiradas do conceito;
 */
public class POIDetails extends AppCompatActivity implements DataReceiver {
    private Attraction attraction;
    private TextView description;
    private static ViewPager mPager;
    FloatingActionButton map;
    private static int currentPage = 0;
    private static final Integer[] images = {R.drawable.moliceiro, R.drawable.moliceiro};
    private List<Integer> imagesArray = new ArrayList<Integer>();
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
        attraction = (Attraction) getIntent().getParcelableExtra("attraction");
        Log.d("attr", attraction.toString()+"");
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.attraction_collapsing_toolbar);
        collapsingToolbar.setTitleEnabled(true);
        collapsingToolbar.setTitle(attraction.getName());
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
        description = (TextView) findViewById(R.id.attraction_description);
        initImageSlider();

        map.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //this intent simply opens the google maps app on the sent coordinates
                //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:40.6442700,-8.6455400?q=<lat>,<long>(Label+Name)"));
                //startActivity(intent);
                Intent intent = new Intent(v.getContext(), AttractionMapActivity.class);
                //intent.putExtra("lat", attraction.getLocation().latitude);
                //intent.putExtra("long", attraction.getLocation().longitude);
                startActivity(intent);
            }
        });

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
                Toast.makeText(
                        getApplicationContext(),
                        listDataHeader.get(groupPosition)
                                + " : "
                                + listDataChild.get(
                                listDataHeader.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });

        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
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
        requestRoutes();
    }



    private void initImageSlider() {
        for(int i=0; i < images.length; i++)
            imagesArray.add(images[i]);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new AttractionImageSliderAdapter(POIDetails.this, imagesArray));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        // Auto start of viewpager
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == images.length) {
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
        }, 2500, 2500);
    }

    /*private void fetchAttractions() {
        JsonObjectRequest request = new JsonObjectRequest(URL_ROUTES_ATTRACTION,
                response -> {
                    if (response == null) {
                        Toast.makeText(this, "Couldn't fetch the routes! Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<Route> items = new Gson().fromJson(response.toString(), new TypeToken<List<Route>>() {
                    }.getType());

                    // adding contacts to contacts list
                    routeList.clear();
                    routeList.addAll(items);

                    // refreshing recycler view
                    listAdapter.notifyDataSetChanged();
                    listDataChild.put(listDataHeader.get(0), routeList);

                    // stop animating Shimmer and hide the layout
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    // stopping swipe refresh
                }, error -> {
            // error in getting json
            // stop animating Shimmer and hide the layout
            mShimmerViewContainer.stopShimmerAnimation();
            mShimmerViewContainer.setVisibility(View.GONE);


            //Log.e("fetch error", "Error: " + error.getMessage());
            //Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            // stopping swipe refresh
        });
        MyApplication.getInstance().addToRequestQueue(request);
    }*/

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Route>>();

        // Adding child data
        listDataHeader.add("Routes that pass in this place");

        // Adding child data
        routeList = new ArrayList<Route>();
        routeList.add(new Route("Salreu", "Nice wal in salreu's corn"));
        routeList.add(new Route("Aveiro city", "Beatifull tour in Aveiro"));
        routeList.add(new Route("UA", "Route that goes around UA"));


        listDataChild.put(listDataHeader.get(0), routeList); // Header, Child data
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

    public void requestRoutes(){
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("conceptName", attraction.getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new POIDetails.getRoutesFromServerTask().execute(jsonRequest.toString());
    }

    private class getRoutesFromServerTask extends AsyncTask<String, Void, String> {
        ProgressDialog progDailog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mShimmerViewContainer.startShimmerAnimation();
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
                URL url = new URL(URL_ROUTES_ATTRACTION);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                // is output buffer writter
                urlConnection.setRequestMethod("GET");
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
                        Log.e("Error", "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            onResponseReceived(response);
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
            Toast.makeText(POIDetails.this, "Connection error", Toast.LENGTH_LONG).show();
            return;
        }
        // stop animating Shimmer and hide the layout
        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);
        //.... recriar aqui objetos route e colocá-los numa lista
        //List<LatLng> trajectory = json.get("trajectory");
    }
}
