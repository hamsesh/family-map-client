package net.jakehamzawi.familymap;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.TextView;

import net.jakehamzawi.familymap.adapter.PersonExpandableListAdapter;
import net.jakehamzawi.familymap.data.DataCache;
import net.jakehamzawi.familymap.data.DataProcessor;
import net.jakehamzawi.familymap.model.FamilyMember;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {

    private static final String PERSON_KEY = "personID";
    private static final String EVENT_KEY = "eventID";
    PersonExpandableListAdapter adapter;

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

        setPersonInfo(person);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<List<Event>> eventCall = new EventTask(personID);
        Future<List<Event>> eventFuture = executor.submit(eventCall);
        Callable<List<FamilyMember>> familyCall = new FamilyTask(personID);
        Future<List<FamilyMember>> familyFuture = executor.submit(familyCall);

        try {
            createExpList(eventFuture.get(), familyFuture.get(), person);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class EventTask implements Callable<List<Event>> {
        private final String personID;

        public EventTask(String personID) {
            this.personID = personID;
        }

        @Override
        public List<Event> call() {
            DataCache dataCache = DataCache.getInstance();
            ArrayList<Event> personEvents = new ArrayList<>();
            for (Event event : dataCache.getEvents()) {
                if (event.getPersonID().equals(personID)) {
                    personEvents.add(event);
                }
            }
            personEvents.sort(new Event.EventComparator());
            return personEvents;
        }
    }

    private static class FamilyTask implements Callable<List<FamilyMember>> {
        private final String personID;

        public FamilyTask(String personID) {
            this.personID = personID;
        }

        @Override
        public List<FamilyMember> call() {
            DataCache dataCache = DataCache.getInstance();
            Person rootPerson = dataCache.getPersonByID(personID);
            return DataProcessor.findFamily(rootPerson);
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

    private void setPersonInfo(Person person) {
        TextView firstNameText = findViewById(R.id.firstNamePerson);
        firstNameText.setText(person.getFirstName());
        TextView lastNameText = findViewById(R.id.lastNamePerson);
        lastNameText.setText(person.getLastName());
        TextView genderText = findViewById(R.id.genderPerson);
        genderText.setText(person.getGender().equals("f") ? R.string.female : R.string.male);
    }

    private void createExpList(List<Event> events, List<FamilyMember> family, Person rootPerson) {
        ExpandableListView listView = findViewById(R.id.expList);
        String[] titles = { "LIFE EVENTS", "FAMILY" };
        adapter = new PersonExpandableListAdapter(this, titles, events,
                family, rootPerson.getFirstName(), rootPerson.getLastName());

        listView.setAdapter(adapter);
        listView.expandGroup(0);
        listView.expandGroup(1);
        listView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            if (titles[groupPosition].equals("LIFE EVENTS")) {
                Intent newEventIntent = new Intent(this, EventActivity.class);
                newEventIntent.putExtra(EVENT_KEY, events.get(childPosition).getEventID());
                startActivity(newEventIntent);
            }
            else {
                Intent newPersonIntent = new Intent(this, PersonActivity.class);
                newPersonIntent.putExtra(PERSON_KEY,
                        family.get(childPosition).getPerson().getPersonID());
                startActivity(newPersonIntent);
            }
            return false;
        });
    }
}