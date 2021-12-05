package net.jakehamzawi.familymap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {

    private static final String PERSON_KEY = "personID";
    EventRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String personID = intent.getStringExtra(PERSON_KEY);
        DataCache dataCache = DataCache.getInstance();
        Person person = dataCache.getPersonByID(personID);

        TextView firstNameText = findViewById(R.id.first_name_person);
        firstNameText.setText(person.getFirstName());
        TextView lastNameText = findViewById(R.id.last_name_person);
        lastNameText.setText(person.getLastName());
        TextView genderText = findViewById(R.id.gender_person);
        genderText.setText(person.getGender().equals("f") ? R.string.female : R.string.male);

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.event_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<ArrayList<Event>> call = new EventTask(personID);
        Future<ArrayList<Event>> future = executor.submit(call);
        try {
            adapter = new EventRecyclerAdapter(this, future.get(), person.getFirstName(), person.getLastName());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        recyclerView.setAdapter(adapter);
    }

    private static class EventTask implements Callable<ArrayList<Event>> {
        private final String personID;

        public EventTask(String personID) {
            this.personID = personID;
        }

        @Override
        public ArrayList<Event> call() {
            DataCache dataCache = DataCache.getInstance();
            Set<Event> personEvents = new TreeSet<>();
            for (Event event : dataCache.getEvents()) {
                if (event.getPersonID().equals(personID)) {
                    personEvents.add(event);
                }
            }
            return new ArrayList<>(personEvents);
        }
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
}