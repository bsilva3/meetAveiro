package pi.ua.meetaveiro.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.models.Attraction;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Attraction} and makes a call to the
 * specified {@link OnAttractionSelectedListener}.
 */
public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.MyViewHolder> implements Filterable {

    private Context mContext;

    private List<Attraction> attractionList;
    private List<Attraction> attractionListFiltered;

    private OnAttractionSelectedListener mListener;

    public AttractionAdapter(Context context,
                             List<Attraction> items,
                             OnAttractionSelectedListener listener) {
        attractionList = items;
        attractionListFiltered = items;
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
            name = view.findViewById(R.id.attraction_name);
            city = view.findViewById(R.id.attraction_city);
            thumbnail = view.findViewById(R.id.attraction_thumbnail);
            overflow = view.findViewById(R.id.overflow);
            mView.setOnClickListener(view1 -> {
                // send selected Route in callback
                mListener.onAttractionSelected(attractionListFiltered.get(getAdapterPosition()));
            });
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
        Attraction attraction = attractionList.get(position);
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
        return attractionListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    attractionListFiltered = attractionList;
                } else {
                    List<Attraction> filteredList = new ArrayList<>();
                    for (Attraction row : attractionList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getName().toLowerCase().contains(charString.toLowerCase()) ||
                                row.getCity().contains(charSequence) ||
                                row.getDescription().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    attractionListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = attractionListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                attractionListFiltered = (ArrayList<Attraction>) filterResults.values;
                notifyDataSetChanged();
            }
        };
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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnAttractionSelectedListener {
        void onAttractionSelected(Attraction item);
    }
}
