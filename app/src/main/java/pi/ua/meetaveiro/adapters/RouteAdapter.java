package pi.ua.meetaveiro.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.activities.RouteHistoryDetailsActivity;
import pi.ua.meetaveiro.fragments.PhotoLogFragment;
import pi.ua.meetaveiro.models.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Route} and makes a call to the
 * specified {@link OnRouteItemSelectedListener}.
 */
public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.MyViewHolder> implements Filterable, SectionTitleProvider {

    private Context context;
    private List<Route> routeList;
    private List<Route> routeListFiltered;
    private OnRouteItemSelectedListener listener;

    @Override
    public String getSectionTitle(int position) {
        return routeList.get(position).getRouteTitle().substring(0, 1);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public Route mItem;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.route_title);
            view.setOnClickListener(view1 -> {
                // send selected Route in callback
                listener.onRouteSelected(routeListFiltered.get(getAdapterPosition()));
            });
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }


    public RouteAdapter(Context context, List<Route> routeList, OnRouteItemSelectedListener listener) {
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

        holder.mView.setOnClickListener(v -> {
            if (null != listener) {
                Log.d("clicked!", "route"+holder.mItem +".json" );

                //context.startActivity(new Intent(context, RouteHistoryDetailsActivity.class));

                Intent intent = new Intent(context, RouteHistoryDetailsActivity.class);
                Bundle b = new Bundle();
                b.putString("name", "route"+holder.mItem +".json" ); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                context.startActivity(intent);

                //GO TO MAP AND SHOW THE ROUTE HERE
                listener.onRouteSelected(holder.mItem);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnRouteItemSelectedListener {
        void onRouteSelected(Route item);
    }

}
