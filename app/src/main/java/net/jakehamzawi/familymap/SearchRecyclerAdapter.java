package net.jakehamzawi.familymap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.jakehamzawi.familymap.model.SearchResult;

import java.util.ArrayList;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder> {

    private final ArrayList<SearchResult> searchResults;

    public SearchRecyclerAdapter(ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView mainInfo;
        private final TextView subInfo;

        public ViewHolder(View view) {
            super(view);
            // TODO: Define click listener for the ViewHolder's View
            imageView = (ImageView) view.findViewById(R.id.searchImage);
            mainInfo = (TextView) view.findViewById(R.id.mainInfo);
            subInfo = (TextView) view.findViewById(R.id.subInfo);
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getMainInfo() {
            return mainInfo;
        }

        public TextView getSubInfo() {
            return subInfo;
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_search, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        ImageView imageView = viewHolder.getImageView();
        TextView mainInfo = viewHolder.getMainInfo();
        TextView subInfo = viewHolder.getSubInfo();

        SearchResult result = this.searchResults.get(position);
        assert result != null;
        if (result.getType() == SearchResult.Type.PERSON) {
            if (result.getGender().equals("f")) {
                imageView.setImageResource(R.drawable.female);
            }
            else {
                imageView.setImageResource(R.drawable.male);
            }
            mainInfo.setText(result.getMainInfo());
            subInfo.setText("");
        }
        else {
            imageView.setImageResource(R.drawable.location);
            mainInfo.setText(result.getMainInfo());
            subInfo.setText(result.getSubInfo());
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return searchResults.size();
    }


}