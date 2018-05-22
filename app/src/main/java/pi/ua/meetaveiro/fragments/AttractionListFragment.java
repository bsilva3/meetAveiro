package pi.ua.meetaveiro.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.AttractionAdapter;
import pi.ua.meetaveiro.interfaces.NetworkCheckResponse;
import pi.ua.meetaveiro.data.Attraction;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.Utils;

import static pi.ua.meetaveiro.others.Constants.API_URL;
import static pi.ua.meetaveiro.others.Constants.URL_ATTRACTIONS;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link AttractionAdapter.OnAttractionSelectedListener}
 * interface.
 */
public class AttractionListFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener,
        NetworkCheckResponse{

    private static final String TAG = AttractionListFragment.class.getSimpleName();

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 2;
    private AttractionAdapter.OnAttractionSelectedListener mListener;
    private RecyclerView recyclerView;
    private List<Attraction> attractionList;
    private AttractionAdapter adapter;


    private SwipeRefreshLayout swipeRefreshLayout;

    private ShimmerFrameLayout mShimmerViewContainer;

    /**
     * Options menu searchView
     */
    private SearchView searchView;


    private FastScroller fastScroller;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AttractionListFragment() {
    }

    @SuppressWarnings("unused")
    public static AttractionListFragment newInstance(int columnCount) {
        AttractionListFragment fragment = new AttractionListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attractionList = new ArrayList<>();

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aveiro);
        if(myBitmap !=null&&!myBitmap.isRecycled()) {
            Palette.from(myBitmap).generate(palette -> {

                int def = R.id.toolbar;
                CollapsingToolbarLayout toolbar = getActivity().findViewById(R.id.collapsing_toolbar);
                toolbar.setBackgroundColor(palette.getLightVibrantColor(def));

            });
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onProcessFinished(boolean hasNetworkConnection) {
        if (hasNetworkConnection)
            fetchAttractions();
        else {
            mShimmerViewContainer.setVisibility(View.GONE);
            mShimmerViewContainer.stopShimmerAnimation();
            swipeRefreshLayout.setRefreshing(false);
            //fetchLocalAttractions();
        }
    }


    @Override
    public void onRefresh() {
        // removing attractions
        attractionList.clear();
        adapter.notifyDataSetChanged();

        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmerAnimation();
        (new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attraction_list, container, false);

        mShimmerViewContainer = view.findViewById(R.id.attraction_list_shimmer_view_container);
        recyclerView = view.findViewById(R.id.attraction_list_recycler_view);
        fastScroller = view.findViewById(R.id.fastscroll);

        // Set the adapter
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        // Set the adapter
        swipeRefreshLayout = view.findViewById(R.id.attaction_swipe_refresh_layout);
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

        adapter = new AttractionAdapter(getContext(), attractionList, mListener);

        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        fastScroller.setRecyclerView(recyclerView);

        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmerAnimation();
        (new Utils.NetworkCheckTask(getContext(), this)).execute(API_URL);

        return view;
    }

    /**
     * fetches json by making http calls
     */
    private void fetchAttractions() {
        JsonArrayRequest request = new JsonArrayRequest(URL_ATTRACTIONS,
                response -> {
                    if (response == null) {
                        Toast.makeText(getActivity(), "Couldn't fetch the attractions! Pleas try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<Attraction> items = new Gson().fromJson(
                            response.toString(),
                            new TypeToken<List<Attraction>>() {}
                            .getType());

                    Collections.sort(items, (lhs, rhs) -> {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        return lhs.getName().trim().compareTo(rhs.getName().trim());
                    });

                    // adding contacts to contacts list
                    attractionList.clear();
                    attractionList.addAll(items);

                    // refreshing recycler view
                    adapter.notifyDataSetChanged();

                    // stop animating Shimmer and hide the layout
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    // stopping swipe refresh
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {

                    // error in getting json
                    // stop animating Shimmer and hide the layout
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    Log.e(TAG, "Error: Could not fetch attractions");
                    // stopping swipe refresh
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                8000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        MyApplication.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageView collImgView = getActivity().findViewById(R.id.collapsing_toolbar_image);
        try {
            Glide.with(getActivity()).load(R.drawable.moliceiro).into(collImgView);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof AttractionAdapter.OnAttractionSelectedListener) {
            mListener = (AttractionAdapter.OnAttractionSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAttractionSelectedListener");
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
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
