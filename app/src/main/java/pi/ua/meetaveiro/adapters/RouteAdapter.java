package pi.ua.meetaveiro.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.util.ArrayList;
import java.util.List;

import pi.ua.meetaveiro.R;
import pi.ua.meetaveiro.activities.RouteDetailsActivity;
import pi.ua.meetaveiro.data.Route;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Route} and makes a call to the
 * specified {@link OnRouteItemSelectedListener}.
 */
public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.MyViewHolder> implements
        Filterable,
        SectionTitleProvider {

    private Context context;
    private List<Route> routeList;

    public List<Route> getRouteList() {
        return routeList;
    }

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
        holder.mItem = routeListFiltered.get(position);
        holder.mTitleView.setText(routeListFiltered.get(position).getRouteTitle());

        holder.mView.setOnClickListener(v -> {
            if (null != listener) {
                Log.d("clicked!", "route"+holder.mItem +".json" );

                //context.startActivity(new Intent(context, RouteHistoryDetailsActivity.class));
                Intent intent = new Intent(context, RouteDetailsActivity.class);
                //Add arguments to the Activity
                Bundle bun = new Bundle();
                //All the names stored locally are in the format route + routeTitle + .json
                bun.putString("fileName", "route"+holder.mItem+".json");
                //Send the route Title
                bun.putString("routeTitle",holder.mItem.getRouteTitle()+"");
                bun.putString("routeDescription",holder.mItem.getRouteDescription());

                bun.putString("Type","Route");
                bun.putString("RouteInstanceID",holder.mItem.getId()+"");
                intent.putExtras(bun);
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
