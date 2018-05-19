package pi.ua.meetaveiro.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.RouteAdapter;
import pi.ua.meetaveiro.interfaces.NetworkCheckResponse;
import pi.ua.meetaveiro.data.Route;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.MyDividerItemDecoration;
import pi.ua.meetaveiro.others.Utils;

import static pi.ua.meetaveiro.others.Constants.*;

/**
 * A fragment representing a list of routes
 * Activities containing this fragment MUST implement the {@link RouteAdapter.OnRouteItemSelectedListener}
 * interface.
 */
public class RouteListFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        NetworkCheckResponse{

    private static final String TAG = RouteListFragment.class.getSimpleName();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    private RouteAdapter.OnRouteItemSelectedListener mListener;

    private List<Route> routeList;
    private RouteAdapter mAdapter;

    private RecyclerView recyclerView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ShimmerFrameLayout mShimmerViewContainer;

    private FastScroller fastScroller;

    private String url;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RouteListFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param url Parameter 1.
     * @return A new instance of fragment RouteListFragment.
     */
    public static RouteListFragment newInstance(String url) {
        RouteListFragment fragment = new RouteListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.url = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_list, container, false);

        mShimmerViewContainer = view.findViewById(R.id.route_list_shimmer_view_container);
        recyclerView = view.findViewById(R.id.recycler_view);

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

        fastScroller = view.findViewById(R.id.fastscroll);

        routeList = new ArrayList<>();
        mAdapter = new RouteAdapter(getContext(), routeList, mListener);

        recyclerView.setAdapter(mAdapter);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 0));
        recyclerView.setAdapter(mAdapter);
        fastScroller.setRecyclerView(recyclerView);

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
                    + " must implement OnListFragmentInteractionListener");
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
        JsonArrayRequest request = new JsonArrayRequest(url,
                response -> {
                    if (response == null) {
                        Toast.makeText(getActivity(), "Couldn't fetch the routes! Pleas try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String str = new Gson().toJson(new Object());
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
                    // stopping swipe refresh
                    swipeRefreshLayout.setRefreshing(false);


            // adding contacts to contacts list
            routeList.clear();
            routeList.add(new Route("route 1", "desc"));

            // refreshing recycler view
            mAdapter.notifyDataSetChanged();
                }
        );

        MyApplication.getInstance().addToRequestQueue(request);
    }


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

            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmerAnimation();

            // refreshing recycler view
            mAdapter.notifyDataSetChanged();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

    }

    @Override
    public void onRefresh() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmerAnimation();
        (new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);
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

        } catch (IOException e ) {
            Log.e(TAG, e.getMessage());
        }

        return datax.toString();

    }

    @Override
    public void onProcessFinished(boolean hasNetworkConnection) {
        if (hasNetworkConnection)
            fetchRoutes();
        else {
            fetchLocalRoutes();
        }
    }
}
