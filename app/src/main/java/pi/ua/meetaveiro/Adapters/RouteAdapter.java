package pi.ua.meetaveiro.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.fragments.RouteHistoryFragment.OnListFragmentInteractionListener;
import pi.ua.meetaveiro.models.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Route} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.MyViewHolder> implements Filterable{

    private Context context;
    private List<Route> routeList;
    private List<Route> routeListFiltered;
    private OnListFragmentInteractionListener listener;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView, mDescriptionView;
        public Route mItem;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.route_title);
            mDescriptionView = (TextView) view.findViewById(R.id.route_description);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected Route in callback
                    listener.onListFragmentInteraction(routeListFiltered.get(getAdapterPosition()));
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDescriptionView.getText() + "'";
        }
    }

    public RouteAdapter(Context context, List<Route> routeList, OnListFragmentInteractionListener listener) {
        this.context = context;
        this.listener = listener;
        this.routeList = routeList;
        this.routeListFiltered = routeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.mItem = routeList.get(position);
        holder.mTitleView.setText(routeList.get(position).getRouteTitle());
        holder.mDescriptionView.setText(routeList.get(position).getRouteDescription());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    Log.d("clicked!", holder.mItem+"");
                    //GO TO MAP AND SHOW THE ROUTE HERE
                    listener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    routeListFiltered = routeList;
                } else {
                    List<Route> filteredList = new ArrayList<>();
                    for (Route row : routeList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getRouteTitle().toLowerCase().contains(charString.toLowerCase()) || row.getRouteDescription().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    routeListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = routeListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                routeListFiltered = (ArrayList<Route>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return routeListFiltered.size();
    }
}
