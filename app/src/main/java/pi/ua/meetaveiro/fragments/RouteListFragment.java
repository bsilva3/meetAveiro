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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.RouteAdapter;
import pi.ua.meetaveiro.interfaces.NetworkCheckResponse;
import pi.ua.meetaveiro.data.Route;
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
        NetworkCheckResponse {

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

    /**
     *  The URL to fetch them routes
     */
    private String url;

    /**
    * Options menu searchView
     */
    private SearchView searchView;

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
                // filter recycler view when query submitted
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
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("user", FirebaseAuth.getInstance().getCurrentUser().getEmail() + "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //routeList.add(new Route("route 1", "desc"));
        new uploadFileToServerTask().execute(jsonRequest.toString(), url);

        // refreshing recycler view
        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);
        // stopping swipe refresh
        swipeRefreshLayout.setRefreshing(false);


    }


    /**
     * Gets all the route names saved in the phone.
     * SHOOULD APPEAR IN THE LIST ITEMS THE Name of the route
     */
    private void fetchLocalRoutes() {

        List<Route> items = new ArrayList<>();
        try {
            File directory = getActivity().getFilesDir();
            File[] files = directory.listFiles();
            Route r;
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().startsWith("route")) {
                    String title = files[i].getName().replaceFirst("route", "");
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
        } catch (Exception e) {
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
     *
     * @param filename
     * @return String (json format) with all the information
     * <p>
     * Must be sent to RouteDetais as an argument
     */
    private String getRouteFromFile(String filename) {

        StringBuffer datax = new StringBuffer("");
        try {
            FileInputStream fIn = getContext().openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fIn);
            BufferedReader buffreader = new BufferedReader(isr);
            String readString = buffreader.readLine();
            while (readString != null) {
                datax.append(readString);
                readString = buffreader.readLine();
            }
            isr.close();

        } catch (IOException e) {
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

    /**
     * Used to send a POST call
     * Will recieve all the instances from the server.
     * Needs to include the email
     */
    private class uploadFileToServerTask extends AsyncTask<String, Void, String> {
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
            Log.d("resComm", response+"");
            try {
                List<Route> items = new ArrayList<>();
                JSONObject js = new JSONObject(response);
                JSONArray arr = js.getJSONArray("routes");
                Route r;
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject row = arr.getJSONObject(i);
                        String idRoute = row.getString("id");
                        String title = row.getString("title");
                        String desc = row.getString("description");
                        r = new Route(title,desc);
                        r.setType("Route");
                        r.setId(Integer.parseInt(idRoute));
                        items.add(r);
                    }
                    // adding contacts to contacts list
                    routeList.clear();
                    routeList.addAll(items);
                    mAdapter.notifyDataSetChanged();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

    }


}
