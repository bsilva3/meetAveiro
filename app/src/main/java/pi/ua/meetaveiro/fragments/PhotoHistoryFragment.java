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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.adapters.PhotoAdapter;
import pi.ua.meetaveiro.data.Photo;
import pi.ua.meetaveiro.others.Constants;
import pi.ua.meetaveiro.others.CustomRequest;
import pi.ua.meetaveiro.others.MyApplication;
import pi.ua.meetaveiro.others.Utils;

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

        fetchImages();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
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

        pDialog.setMessage(getString(R.string.download_images));
        pDialog.show();

        JSONObject json = new JSONObject();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        try {
            json.put("user", user.getEmail());
        } catch (JSONException e) {}

        CustomRequest request = new CustomRequest(
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
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Constants.TOKEN);
                return headers;
            }
        };

        // Adding request to request queue
        Utils.refreshTokenAndQueueRequest(request);
    }

}
