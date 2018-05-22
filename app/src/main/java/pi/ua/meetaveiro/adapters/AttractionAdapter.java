package pi.ua.meetaveiro.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.ArrayList;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.data.Attraction;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Attraction} and makes a call to the
 * specified {@link OnAttractionSelectedListener}.
 */
public class AttractionAdapter extends RecyclerView.Adapter<AttractionAdapter.MyViewHolder> implements
        Filterable,
        SectionTitleProvider {

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
        View mView;
        Attraction mItem;
        TextView name, city;
        ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            name = view.findViewById(R.id.attraction_name);
            city = view.findViewById(R.id.attraction_city);
            thumbnail = view.findViewById(R.id.attraction_thumbnail);
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
    public String getSectionTitle(int position) {
        //this String will be shown in a bubble for specified position
        return attractionListFiltered.get(position).getName().substring(0, 1);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Attraction attraction = attractionListFiltered.get(position);
        holder.mItem = attraction;
        holder.name.setText(attraction.getName());
        holder.city.setText(attraction.getCity());

        // loading album cover using Glide library
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_photo_camera_black_24dp)
                .error(R.drawable.ic_photo_camera_black_24dp)
                .priority(Priority.HIGH)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(mContext)
                .load(attraction.getImgName())
                .thumbnail(0.5f)
                .transition(
                        (new DrawableTransitionOptions())
                                .crossFade())
                .apply(options)
                .into(holder.thumbnail);

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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnAttractionSelectedListener {
        void onAttractionSelected(Attraction item);
    }
}
