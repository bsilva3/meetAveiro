package pi.ua.meetaveiro.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import pi.ua.meetaveiro.models.RouteInstance;

/**
 * Created by joaos on 19/05/2018.
 */

public class RouteInstanceAdapter extends RecyclerView.Adapter<RouteInstanceAdapter.MyViewHolder> implements Filterable, SectionTitleProvider{

    private Context context;
    private List<RouteInstance> routeList;
    private List<RouteInstance> routeListFiltered;
    private RouteInstanceAdapter.OnRouteItemSelectedListener listener;

    @Override
    public String getSectionTitle(int position) {
        return routeList.get(position).getRoute().getRouteTitle().substring(0, 1);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public RouteInstance mItem;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.route_title);
            view.setOnClickListener(view1 -> {
                // send selected Route in callback
                listener.onRouteInstanceSelected(routeListFiltered.get(getAdapterPosition()));
            });
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }


    public RouteInstanceAdapter(Context context, List<RouteInstance> routeList, RouteInstanceAdapter.OnRouteItemSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        this.routeList = routeList;
        this.routeListFiltered = routeList;
    }

    @Override
    public RouteInstanceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new RouteInstanceAdapter.MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final RouteInstanceAdapter.MyViewHolder holder, int position) {
        holder.mItem = routeListFiltered.get(position);
        holder.mTitleView.setText(routeListFiltered.get(position).getRoute().getRouteTitle());

        //When it is clicked all these actions will follow
        holder.mView.setOnClickListener(v -> {
            if (null != listener) {
                //context.startActivity(new Intent(context, RouteHistoryDetailsActivity.class));
                Intent intent = new Intent(context, RouteDetailsActivity.class);

                //Add arguments to the Activity
                Bundle bun = new Bundle();

                //The type will determine if it is a UserRoute (that he made and its in the history)
                //OR if it is a Route that needs NO MARKERS ONLY DESCRIPTION TITLE AND THE POINTS
                bun.putString("Type",holder.mItem.getRoute().getType());


                //Verify if it is local or from the server
                //First we will check if it has an id
                //If not then it is local and it will trigger local read in the RouteDetailsActivity
                if(holder.mItem.getRoute().getType() == "Instance") {
                    if (holder.mItem.getIdInstance() != 0) {
                        bun.putString("RouteInstanceID", holder.mItem.getIdInstance() + "");
                    } else {
                        bun.putString("RouteInstanceID", "noNumber");
                        //All the names stored locally are in the format route + routeTitle + .json
                        bun.putString("fileName", "route" + holder.mItem.getRoute().getRouteTitle() + ".json");
                    }
                }

                if(holder.mItem.getRoute().getType() == "Route") {
                    if (holder.mItem.getRoute().getId() != 0) {
                        bun.putString("RouteInstanceID",holder.mItem.getRoute().getId() + "");
                    } else {
                        bun.putString("RouteInstanceID", "noNumber");
                        //All the names stored locally are in the format route + routeTitle + .json
                        bun.putString("fileName", "route" + holder.mItem.getRoute().getRouteTitle() + ".json");
                    }
                }



                //Send the route Title
                bun.putString("routeTitle",holder.mItem.getRoute().getRouteTitle()+"");


                //End and start date
                try {
                    bun.putString("StartDate",holder.mItem.getStartDate().toString());
                    bun.putString("EndDate",holder.mItem.getEndDate().toString());

                }catch (Exception e){}


                intent.putExtras(bun);
                context.startActivity(intent);

                //GO TO MAP AND SHOW THE ROUTE HERE
                listener.onRouteInstanceSelected(holder.mItem);
            }
        });
    }

    /**
     * Filters the results
     * @return
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    routeListFiltered = routeList;
                } else {
                    List<RouteInstance> filteredList = new ArrayList<>();
                    for (RouteInstance row : routeList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getRoute().getRouteTitle().toLowerCase().contains(charString.toLowerCase()) || row.getRoute().getRouteDescription().contains(charSequence)) {
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
                routeListFiltered = (ArrayList<RouteInstance>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * Get's the item count in the filtered list
     * @return
     */
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
        void onRouteInstanceSelected(RouteInstance item);
    }
}
