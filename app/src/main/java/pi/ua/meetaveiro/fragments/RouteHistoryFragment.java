package pi.ua.meetaveiro.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pi.ua.meetaveiro.activities.RouteDetailsActivity;
import pi.ua.meetaveiro.adapters.RouteAdapter;
import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.interfaces.NetworkCheckResponse;
import pi.ua.meetaveiro.models.Route;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.MyDividerItemDecoration;
import pi.ua.meetaveiro.others.Utils;

import static pi.ua.meetaveiro.others.Constants.API_URL;
import static pi.ua.meetaveiro.others.Constants.URL_ROUTE_HISTORY;

/**
 * A fragment representing a list of routes
 * Activities containing this fragment MUST implement the {@link RouteAdapter.OnRouteItemSelectedListener}
 * interface.
 */
public class RouteHistoryFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        NetworkCheckResponse{

    private static final String TAG = RouteHistoryFragment.class.getSimpleName();

    private RouteAdapter.OnRouteItemSelectedListener mListener;

    private List<Route> routeList;
    private RouteAdapter mAdapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private RecyclerView.LayoutManager mLayoutManager;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ShimmerFrameLayout mShimmerViewContainer;

    private FastScroller fastScroller;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RouteHistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_list, container, false);

        mShimmerViewContainer = view.findViewById(R.id.route_list_shimmer_view_container);
        recyclerView = view.findViewById(R.id.recycler_view);
        fastScroller = view.findViewById(R.id.fastscroll);

        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this.getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        // Set the adapter
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(() -> {
            mShimmerViewContainer.setVisibility(View.VISIBLE);
            mShimmerViewContainer.startShimmerAnimation();
            (new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);
        });

        routeList = new ArrayList<>();
        mAdapter = new RouteAdapter(getContext(), routeList, mListener);
        recyclerView.setAdapter(mAdapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 0));
        recyclerView.setAdapter(mAdapter);

        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmerAnimation();

        (new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RouteAdapter.OnRouteItemSelectedListener) {
            mListener = (RouteAdapter.OnRouteItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInte ractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * fetches json by making http calls
     */
    private void fetchRoutes() {
        JsonArrayRequest request = new JsonArrayRequest(URL_ROUTE_HISTORY,
                response -> {
                    if (response == null) {
                        Toast.makeText(getActivity(), "Couldn't fetch the routes! Pleas try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<Route> items = new Gson().fromJson(response.toString(), new TypeToken<List<Route>>() {
                    }.getType());

                    // adding contacts to contacts list
                    routeList.clear();
                    routeList.addAll(items);

                    // refreshing recycler view
                    mAdapter.notifyDataSetChanged();
                    // stop animating Shimmer and hide the layout
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    // stopping swipe refresh
                    swipeRefreshLayout.setRefreshing(false);
                }, error -> {
                    // error in getting json
                    // stop animating Shimmer and hide the layout
                    fetchLocalRoutes();
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    Log.e(TAG, "Error: " + error.getMessage());
                    Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    // stopping swipe refresh
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        MyApplication.getInstance().addToRequestQueue(request);
    }

    /**
     * Gets all the route names saved in the phone.
     * The file name is route (name of route).json
     */
    private void fetchLocalRoutes() {
        List<Route> items = new ArrayList<>();
        try {
            File directory = getActivity().getFilesDir();
            File[] files = directory.listFiles();
            Route r;
            for (int i = 0; i < files.length; i++) {
                //Get all the files that are routes
                if (files[i].getName().startsWith("route")) {
                    String title = files[i].getName().replaceFirst("route", "").replaceFirst(".json","");
                    r = new Route(title);
                    items.add(r);
                }
            }

            // adding contacts to contacts list
            routeList.clear();
            routeList.addAll(items);

            // refreshing recycler view
            mAdapter.notifyDataSetChanged();
            // stop animating Shimmer and hide the layout
            mShimmerViewContainer.stopShimmerAnimation();
            mShimmerViewContainer.setVisibility(View.GONE);
            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    public void onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmerAnimation();
        (new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);
    }


    /**
     * Get the description from a json file without the need to go trough it all
     * @param datax
     * @return
     */
    private String getRouteDesc(String datax){
        try {
            JSONObject json = new JSONObject(datax.toString());
            return json.get("Description").toString();
        } catch (JSONException e) {
            return null;
        }

    }


    @Override
    public void onProcessFinished(boolean hasNetworkConnection) {
        if (hasNetworkConnection) {
            fetchRoutes();
        }else {
            fetchLocalRoutes();
        }
    }

}
