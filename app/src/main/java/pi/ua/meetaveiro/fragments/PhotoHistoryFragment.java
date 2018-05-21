package pi.ua.meetaveiro.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.PhotoAdapter;
import pi.ua.meetaveiro.data.Photo;
import pi.ua.meetaveiro.others.CustomRequest;
import pi.ua.meetaveiro.others.MyApplication;

import static pi.ua.meetaveiro.others.Constants.URL_PHOTO_HISTORY;

/**
 * A fragment representing a list of routes
 * Activities containing this fragment MUST implement the {@link pi.ua.meetaveiro.adapters.AttractionAdapter.OnAttractionSelectedListener}
 * interface.
 */
public class PhotoHistoryFragment extends Fragment  {

    private static final String TAG = PhotoHistoryFragment.class.getSimpleName();

    private ArrayList<Photo> images;
    private ProgressDialog pDialog;
    private PhotoAdapter mAdapter;
    private RecyclerView recyclerView;

    private PhotoAdapter.OnPhotoItemSelectedListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhotoHistoryFragment() {
    }


    public static PhotoHistoryFragment newInstance() {
        return new PhotoHistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        pDialog = new ProgressDialog(getContext());
        images = new ArrayList<>();
        mAdapter = new PhotoAdapter(getContext(), images);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(
                new PhotoAdapter.RecyclerTouchListener(
                        getContext(),
                        recyclerView,
                        mListener,
                        images
                )
        );

        //(new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        fetchImages();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PhotoAdapter.OnPhotoItemSelectedListener) {
            mListener = (PhotoAdapter.OnPhotoItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPhotoItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void fetchImages() {

        pDialog.setMessage("Downloading json...");
        pDialog.show();

        JSONObject json = new JSONObject();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        try {
            json.put("user", user.getEmail());
        } catch (JSONException e) {}

        CustomRequest req = new CustomRequest(
                Request.Method.POST,
                URL_PHOTO_HISTORY,
                json,
                response -> {
                    Log.d(TAG, response.toString());
                    pDialog.hide();

                    images.clear();
                    List<Photo> items = new Gson()
                            .fromJson(response.toString(),
                                    new TypeToken<List<Photo>>() {}.getType()
                            );

                    images.addAll(items);

                    // refreshing recycler view
                    mAdapter.notifyDataSetChanged();
                }, error -> {
                            Log.e(TAG, "Error: " + error.getMessage());
                            pDialog.hide();
                }
        );

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(req);
    }
/*
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

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
        super.onCreateOptionsMenu(menu, inflater);
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
        // start animating Shimmer and hide the layout
        mShimmerViewContainer.startShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        (new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);
    }

    @Override
    public void onProcessFinished(boolean hasNetworkConnection) {
        if (hasNetworkConnection)
            fetchAttractions();
        else {
            // stop animating Shimmer and hide the layout
            mShimmerViewContainer.stopShimmerAnimation();
            mShimmerViewContainer.setVisibility(View.GONE);
            // stopping swipe refresh
            swipeRefreshLayout.setRefreshing(false);
            //fetchLocalAttractions();
        }
    }*/
}
