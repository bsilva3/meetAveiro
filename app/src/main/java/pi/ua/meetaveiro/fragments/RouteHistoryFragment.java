package pi.ua.meetaveiro.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.RouteInstanceAdapter;
import pi.ua.meetaveiro.data.Route;
import pi.ua.meetaveiro.data.RouteInstance;
import pi.ua.meetaveiro.interfaces.NetworkCheckResponse;
import pi.ua.meetaveiro.others.Constants;
import pi.ua.meetaveiro.others.MyDividerItemDecoration;
import pi.ua.meetaveiro.others.Utils;

import static pi.ua.meetaveiro.others.Constants.API_URL;
import static pi.ua.meetaveiro.others.Constants.INSTANCE_BY_USER;

/**
 * A fragment representing a list of routes
 * Activities containing this fragment MUST implement the {@link RouteInstanceAdapter.OnRouteInstanceItemSelectedListener}
 * interface.
 */
public class RouteHistoryFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        NetworkCheckResponse{

    private static final String TAG = RouteHistoryFragment.class.getSimpleName();

    private RouteInstanceAdapter.OnRouteInstanceItemSelectedListener mListener;

    private List<RouteInstance> routeList;
    private RouteInstanceAdapter mAdapter;
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
        mAdapter = new RouteInstanceAdapter(getContext(), routeList, mListener);
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
        if (context instanceof RouteInstanceAdapter.OnRouteInstanceItemSelectedListener) {
            mListener = (RouteInstanceAdapter.OnRouteInstanceItemSelectedListener) context;
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
     * Fetches all the instances from the server
     * Only for the current user
     */
    private void fetchAllInstances() {

        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("user", FirebaseAuth.getInstance().getCurrentUser().getEmail()+"");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Utils.refreshTokenAndExecuteAsyncTask(new UploadFileToServerTask(), jsonRequest.toString(), INSTANCE_BY_USER);
    }

    /**
     * Gets all the route names saved in the phone.
     * The file name is route (name of route).json
     */
    private void fetchAllLocal() {
        List<RouteInstance> items = new ArrayList<>();
        try {
            File directory = getActivity().getFilesDir();
            File[] files = directory.listFiles();
            Route r;
            for (int i = 0; i < files.length; i++) {
                //Get all the files that are routes
                if (files[i].getName().startsWith("route")) {
                    String title = files[i].getName().replaceFirst("route", "").replaceFirst(".json","");
                    r = new Route(title);
                    RouteInstance e = new RouteInstance(r);
                    items.add(e);
                }
            }
            // adding routes to routes list
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

    /**
     * Stops all animations
     */
    @Override
    public void onRefresh() {
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmerAnimation();
        (new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);
    }


    /**
     * Having a network connection will trigger the fetchAllInstances
     * Not having will trrigger the fetchAllLocal
     * @param hasNetworkConnection
     */
    @Override
    public void onProcessFinished(boolean hasNetworkConnection) {
        Log.i("conection", String.valueOf(hasNetworkConnection));
        if (hasNetworkConnection) {
            fetchAllInstances();
        }else {
            fetchAllLocal();
        }
    }


    /**
     * Used to send a POST call
     * Will recieve all the instances from the server.
     * Needs to include the email
     */
    private class UploadFileToServerTask extends AsyncTask<String, Void, String> {
        String serverUrl;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                urlConnection.setRequestProperty("Authorization", Constants.TOKEN);
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

            assert response != null;

            try {
                List<RouteInstance> items = new ArrayList<>();
                JSONObject js = new JSONObject(response);
                Log.d("res", js.toString()+"");
                JSONArray arr = js.getJSONArray("instances");
                Route r;
                RouteInstance e;

                if(arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject row = arr.getJSONObject(i);
                        String title = row.getString("route");
                        String idInstance = row.getString("id");
                        String dateStart = row.getString("start");
                        String dateEnd = row.getString("end");

                        r = new Route(title);
                        r.setType("Instance");
                        e = new RouteInstance(r);
                        e.setIdInstance(Integer.parseInt(idInstance));
                        e.setStartDate(new Date(dateStart));
                        e.setEndDate(new Date(dateEnd));
                        items.add(e);
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
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }









}
