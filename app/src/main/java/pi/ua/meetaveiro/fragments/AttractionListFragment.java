package pi.ua.meetaveiro.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.AttractionAdapter;
import pi.ua.meetaveiro.models.Attraction;
import pi.ua.meetaveiro.models.Route;
import pi.ua.meetaveiro.others.MyApplication;

import static pi.ua.meetaveiro.others.Constants.URL_ATTRACTIONS;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link AttractionAdapter.OnAttractionSelectedListener}
 * interface.
 */
public class AttractionListFragment extends Fragment {

    private static final String TAG = AttractionListFragment.class.getSimpleName();

    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 2;
    private AttractionAdapter.OnAttractionSelectedListener mListener;
    private RecyclerView recyclerView;
    private List<Attraction> attractionList;
    private AttractionAdapter adapter;

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

        recyclerView = view.findViewById(R.id.recycler_view);
        // Set the adapter
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        adapter = new AttractionAdapter(getContext(), attractionList, mListener);

        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        fetchAttractions();

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

                    List<Attraction> items = new Gson().fromJson(response.toString(), new TypeToken<List<Route>>() {
                    }.getType());

                    // adding contacts to contacts list
                    attractionList.clear();
                    attractionList.addAll(items);

                    // refreshing recycler view
                    adapter.notifyDataSetChanged();
                    /*
                    // stop animating Shimmer and hide the layout
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                    // stopping swipe refresh
                    swipeRefreshLayout.setRefreshing(false);*/
                }, error -> {
  /*
            // error in getting json
            // stop animating Shimmer and hide the layout
            mShimmerViewContainer.stopShimmerAnimation();
            mShimmerViewContainer.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
*/
            Log.e(TAG, "Error: " + error.getMessage());
            Toast.makeText(getActivity(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            // stopping swipe refresh
        });
        Attraction attraction = new Attraction();
        attraction.setName("Moliceiro");
        attraction.setCity("Aveiro");
        attraction.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.moliceiro));
        Attraction attraction2 = new Attraction();
        attraction2.setName("Moliceiro");
        attraction2.setCity("Porto");
        attraction2.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.moliceiro));
        attractionList.add(attraction);
        attractionList.add(attraction2);
        adapter.notifyDataSetChanged();
        MyApplication.getInstance().addToRequestQueue(request);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageView collImgView = getActivity().findViewById(R.id.collapsing_toolbar_image);
        TextView collTitlteView = getActivity().findViewById(R.id.collapsing_toolbar_title);
        TextView collSubtitleView = getActivity().findViewById(R.id.collapsing_toolbar_subtitle);
        try {
            Glide.with(getActivity()).load(R.drawable.moliceiro).into(collImgView);
        } catch (Exception e) {
            e.printStackTrace();
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
