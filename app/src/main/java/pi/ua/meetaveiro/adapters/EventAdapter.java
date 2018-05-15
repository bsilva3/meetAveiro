package pi.ua.meetaveiro.adapters;

import android.content.Context;
import android.content.Intent;
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
import pi.ua.meetaveiro.activities.EventDetailsActivity;
import pi.ua.meetaveiro.models.Event;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Event} and makes a call to the
 * specified {@link OnEventItemSelectedListener}.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> implements Filterable, SectionTitleProvider {

    private Context context;
    private List<Event> eventList;
    private List<Event> eventListFiltered;
    private OnEventItemSelectedListener listener;

    @Override
    public String getSectionTitle(int position) {
        return eventList.get(position).getTitle().substring(0, 1);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitleView;
        public Event mItem;

        public MyViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.route_title);
            view.setOnClickListener(view1 -> {
                // send selected Route in callback
                listener.onEventSelected(eventListFiltered.get(getAdapterPosition()));
            });
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }


    public EventAdapter(Context context, List<Event> eventList, OnEventItemSelectedListener listener) {
        this.context = context;
        this.listener = listener;
        this.eventList = eventList;
        this.eventListFiltered = eventList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.mItem = eventListFiltered.get(position);
        holder.mTitleView.setText(eventListFiltered.get(position).getTitle());

        holder.mView.setOnClickListener(v -> {
            if (null != listener) {
                Log.d("clicked!", "Event"+holder.mItem +".json" );

                //context.startActivity(new Intent(context, EventHistoryDetailsActivity.class));
                Intent intent = new Intent(context, EventDetailsActivity.class);
                intent.putExtra("Event", holder.mItem);
                context.startActivity(intent);

                //GO TO MAP AND SHOW THE ROUTE HERE
                listener.onEventSelected(holder.mItem);
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
                    eventListFiltered = eventList;
                } else {
                    List<Event> filteredList = new ArrayList<>();
                    for (Event row : eventList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                            Log.i("title", row.getTitle());
                        }
                    }

                    eventListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = eventListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                eventListFiltered = (ArrayList<Event>) filterResults.values;
                Log.i("eventListFiltered", eventListFiltered.toString());
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return eventListFiltered.size();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnEventItemSelectedListener {
        void onEventSelected(Event item);
    }

}
