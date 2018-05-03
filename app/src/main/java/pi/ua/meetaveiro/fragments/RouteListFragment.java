package pi.ua.meetaveiro.fragments;

import android.app.SearchManager;
import android.content.Context;
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

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.RouteAdapter;
import pi.ua.meetaveiro.models.Route;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.MyDividerItemDecoration;

import static pi.ua.meetaveiro.others.Constants.URL_ROUTE_HISTORY;

/**
 * A fragment representing a list of routes
 * Activities containing this fragment MUST implement the {@link RouteAdapter.OnRouteItemSelectedListener}
 * interface.
 */
public class RouteListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = RouteListFragment.class.getSimpleName();

    private RouteAdapter.OnRouteItemSelectedListener mListener;

    private List<Route> routeList;
    private RouteAdapter mAdapter;
    private RecyclerView recyclerView;
    private SearchView searchView;

    private SwipeRefreshLayout swipeRefreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RouteListFragment() {
    }


    public static RouteListFragment newInstance() {
        return new RouteListFragment();
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
        // Set the adapter

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(true);

                    fetchLocalRoutes();

        }
        );

        recyclerView = view.findViewById(R.id.recycler_view);

        routeList = new ArrayList<>();
        mAdapter = new RouteAdapter(getContext(), routeList, mListener);
        recyclerView.setAdapter(mAdapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 0));
        recyclerView.setAdapter(mAdapter);

        fetchLocalRoutes();

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
    /*private void fetchRoutes() {
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
            mShimmerViewContainer.stopShimmerAnimation();
            mShimmerViewContainer.setVisibility(View.GONE);
            Log.e(TAG, "Error: " + error.getMessage());
            Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);
        });

        MyApplication.getInstance().addToRequestQueue(request);
    }*/


    /**
     * Gets all the route names saved in the phone.
     * SHOOULD APPEAR IN THE LIST ITEMS THE Name of the route
     *
     */
    private void fetchLocalRoutes(){
        List<Route> items = new ArrayList<>();
        try {
            File directory = getActivity().getFilesDir();
            File[] files = directory.listFiles();
            Route r;
            for (int i = 0; i < files.length; i++) {
                if(files[i].getName().startsWith("route")) {
                    String title = files[i].getName().replaceFirst("route","");
                    getRouteFromFile(title);
                    r = new Route(title);
                    items.add(r);
                }
            }

            // adding contacts to contacts list
            routeList.clear();
            routeList.addAll(items);

            // refreshing recycler view
            mAdapter.notifyDataSetChanged();

            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);


        }catch (Exception e){
            e.printStackTrace();
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
        fetchLocalRoutes();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }



    /**
     * Opens the file with the route and reconctructs it
     * File name format: route+++.json
     * +++ = route name
     * @param filename
     * @return String (json format) with all the information
     *
     * Must be sent to RouteDetais as an argument
     */
    private String getRouteFromFile(String filename){

        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = getContext().openFileInput ( filename ) ;
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;
            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }
            isr.close ( ) ;

        } catch (IOException ioe ) {
            ioe.printStackTrace() ;
        }

        return datax.toString();


    }

}
