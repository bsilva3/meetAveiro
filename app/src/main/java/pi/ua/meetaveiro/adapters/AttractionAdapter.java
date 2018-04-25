package pi.ua.meetaveiro.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.fragments.AttractionListFragment;
import pi.ua.meetaveiro.models.Attraction;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Attraction} and makes a call to the
 * specified {@link AttractionListFragment.OnAttractionSelectedListener}.
 */
public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.MyViewHolder> {

    private final Context mContext;
    private final List<Attraction> mValues;
    private final AttractionListFragment.OnAttractionSelectedListener mListener;

    public AttractionAdapter(Context context, List<Attraction> items, AttractionListFragment.OnAttractionSelectedListener listener) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public Attraction mItem;
        public TextView name, city;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            name = (TextView) view.findViewById(R.id.attraction_name);
            city = (TextView) view.findViewById(R.id.attraction_city);
            thumbnail = (ImageView) view.findViewById(R.id.attraction_thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attraction, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Attraction attraction = mValues.get(position);
        holder.mItem = attraction;
        holder.name.setText(attraction.getName());
        holder.city.setText(attraction.getCity());

        // loading album cover using Glide library
        Glide.with(mContext).load(attraction.getImage()).into(holder.thumbnail);

        holder.overflow.setOnClickListener(view -> showPopupMenu(holder.overflow));

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onAttractionSelected(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        /*PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();*/
    }
}
