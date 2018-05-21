package pi.ua.meetaveiro.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.ViewPagerAdapter;

import static pi.ua.meetaveiro.others.Constants.URL_COMMUNITY_ROUTES;
import static pi.ua.meetaveiro.others.Constants.URL_CREATED_ROUTES;

public class RouteListsFragment extends Fragment {

    private static final String TAG = RouteListsFragment.class.getSimpleName();

    private FloatingActionButton newRouteFab;

    private OnNewRouteListener mListener;

    private ViewPager viewPager;

    public RouteListsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route_lists, container, false);

        viewPager = view.findViewById(R.id.htab_viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = getActivity().findViewById(R.id.collapsing_tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
                Log.d(TAG, "onTabSelected: pos: " + tab.getPosition());
                //invalidateFragmentMenus(tab.getPosition());
                switch (tab.getPosition()) {
                    case 0:
                        break;
                    case 1:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        newRouteFab = view.findViewById(R.id.new_route_fab);
        newRouteFab.setOnClickListener(v -> {
            mListener.onNewRoute();
        });

        return view;
    }

    private void invalidateFragmentMenus(int position){
        for(int i = 0; i < viewPager.getAdapter().getCount(); i++){
            ((ViewPagerAdapter)viewPager.getAdapter()).getItem(i).setHasOptionsMenu(i == position);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewRouteListener) {
            mListener = (OnNewRouteListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewRouteListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFrag(RouteListFragment.newInstance(URL_COMMUNITY_ROUTES), "Community");
        adapter.addFrag(RouteListFragment.newInstance(URL_CREATED_ROUTES), "Created");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageView collImgView = getActivity().findViewById(R.id.collapsing_toolbar_image);

        try {
            Glide.with(getActivity()).load(R.drawable.beach).into(collImgView);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnNewRouteListener {
        void onNewRoute();
    }

}
