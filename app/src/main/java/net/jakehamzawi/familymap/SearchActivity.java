package net.jakehamzawi.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.jakehamzawi.familymap.model.SearchResult;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Event;
import model.Person;

public class SearchActivity extends AppCompatActivity {

    private ArrayList<SearchResult> searchResults;
    private ArrayList<SearchTask> searchTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchResults = new ArrayList<>();
        searchTasks = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.searchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RelativeLayout loadingPanel = findViewById(R.id.loadingPanel);
        loadingPanel.setVisibility(View.GONE);

        SearchRecyclerAdapter adapter = new SearchRecyclerAdapter(searchResults);
        recyclerView.setAdapter(adapter);

        EditText searchText = findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                for (SearchTask searchTask : searchTasks) {
                    searchTask.interrupt();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                SearchTask searchTask = new SearchTask(new SearchHandler(adapter, loadingPanel),
                        s.toString().trim());
                searchTasks.add(searchTask);
                loadingPanel.setVisibility(View.VISIBLE);
                executor.submit(searchTask);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        ImageButton clearButton = findViewById(R.id.clearText);
        clearButton.setOnClickListener(v -> {
            searchText.setText("");
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class SearchHandler extends Handler {
        private SearchRecyclerAdapter adapter;
        private RelativeLayout loadingPanel;

        private SearchHandler(SearchRecyclerAdapter adapter, RelativeLayout loadingPanel) {
            this.adapter = adapter;
            this.loadingPanel = loadingPanel;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            loadingPanel.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    private class SearchTask implements Runnable {
        volatile boolean running = true;
        private final SearchHandler handler;
        private final String query;

        public SearchTask(SearchHandler handler, String query) {
            this.handler = handler;
            this.query = query;
        }

        @Override
        public void run() {
            searchResults.clear();
            if (!running) {
                return;
            }
            if (query.isEmpty()) {
                sendMessage();
                return;
            }
            DataCache dataCache = DataCache.getInstance();
            Person[] persons = dataCache.getPersons();
            Event[] events = dataCache.getEvents();
            for (Person person : persons) {
                if (containsIgnoreCase(person.getFirstName(), query) ||
                    containsIgnoreCase(person.getLastName(), query)) {
                    searchResults.add(new SearchResult(person));
                }
            }
            if (!running) {
                searchResults.clear();
                return;
            }
            for (Event event : events) {
                if (containsIgnoreCase(event.getCountry(), query) ||
                    containsIgnoreCase(event.getCity(), query) ||
                    containsIgnoreCase(event.getEventType(), query) ||
                    containsIgnoreCase(String.valueOf(event.getYear()), query)) {

                    Person person = getPersonByID(event.getPersonID(), persons);
                    assert person != null;
                    searchResults.add(new SearchResult(event, person.getFirstName(), person.getLastName()));
                }
            }
            sendMessage();
        }

        private void sendMessage() {
            Message message = Message.obtain();
            handler.sendMessage(message);
        }

        protected void interrupt() {
            running = false;
        }

        private boolean containsIgnoreCase(String src, String what) {
            final int length = what.length();
            if (length == 0)
                return true; // Empty string is contained

            final char firstLo = Character.toLowerCase(what.charAt(0));
            final char firstUp = Character.toUpperCase(what.charAt(0));

            for (int i = src.length() - length; i >= 0; i--) {
                // Quick check before calling the more expensive regionMatches() method:
                final char ch = src.charAt(i);
                if (ch != firstLo && ch != firstUp)
                    continue;

                if (src.regionMatches(true, i, what, 0, length))
                    return true;
            }

            return false;
        }

        private Person getPersonByID(String id, Person[] persons) {
            for (Person person : persons) {
                if (person.getPersonID().equals(id)) {
                    return person;
                }
            }
            return null;
        }
    }
}