package net.jakehamzawi.familymap;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

import model.Event;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder> {

    private final ArrayList<Event> events;
    private final String firstName;
    private final String lastName;

    public EventRecyclerAdapter(Context context, ArrayList<Event> events, String firstName, String lastName) {
        this.events = events;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventText;
        private final TextView nameText;

        public ViewHolder(View view) {
            super(view);
            // TODO: Define click listener for the ViewHolder's View
            eventText = (TextView) view.findViewById(R.id.locationInfo);
            nameText = (TextView) view.findViewById(R.id.nameText);
        }

        public TextView getEventText() {
            return eventText;
        }

        public TextView getNameText() {
            return nameText;
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.event_info_row, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        TextView eventText = viewHolder.getEventText();
        Resources res = eventText.getResources();
        Event event = events.get(position);
        eventText.setText(res.getString(R.string.event_info, event.getEventType().toUpperCase(Locale.ROOT),
                event.getCity(), event.getCountry(), event.getYear()));

        TextView nameText = viewHolder.getNameText();
        nameText.setText(res.getString(R.string.person_name, firstName, lastName));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return events.size();
    }
}
