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
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
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

        TextView firstNameText = findViewById(R.id.first_name_person);
        firstNameText.setText(person.getFirstName());
        TextView lastNameText = findViewById(R.id.last_name_person);
        lastNameText.setText(person.getLastName());
        TextView genderText = findViewById(R.id.gender_person);
        genderText.setText(person.getGender().equals("f") ? R.string.female : R.string.male);

        // set up the RecyclerView
        ExpandableListView listView = findViewById(R.id.exp_list);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<ArrayList<Event>> eventCall = new EventTask(personID);
        Future<ArrayList<Event>> eventFuture = executor.submit(eventCall);
        Callable<HashMap<String, ArrayList<Person>>> familyCall = new FamilyTask(personID);
        Future<HashMap<String, ArrayList<Person>>> familyFuture = executor.submit(familyCall);
        String[] titles = { "LIFE EVENTS", "FAMILY" };
        try {
            adapter = new PersonExpandableListAdapter(this, titles, eventFuture.get(),
                    familyFuture.get(), person.getFirstName(), person.getLastName());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        listView.setAdapter(adapter);
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

    private static class FamilyTask implements Callable<HashMap<String, ArrayList<Person>>> {
        private final String personID;

        public FamilyTask(String personID) {
            this.personID = personID;
        }

        @Override
        public HashMap<String, ArrayList<Person>> call() {
            DataCache dataCache = DataCache.getInstance();
            HashMap<String, ArrayList<Person>> familyMembers = new HashMap<>();
            Person rootPerson = dataCache.getPersonByID(personID);
            for (Person person : dataCache.getPersons()) {
                if (rootPerson.getMotherID() != null &&
                        rootPerson.getFatherID().equals(person.getPersonID())) {
                    if (!familyMembers.containsKey("Father")) {
                        ArrayList<Person> newList = new ArrayList<>();
                        newList.add(person);
                        familyMembers.put("Father", newList);
                    }
                    else {
                        familyMembers.get("Father").add(person);
                    }
                }
                else if (rootPerson.getMotherID() != null &&
                         rootPerson.getMotherID().equals(person.getPersonID())) {
                    if (!familyMembers.containsKey("Mother")) {
                        ArrayList<Person> newList = new ArrayList<>();
                        newList.add(person);
                        familyMembers.put("Mother", newList);
                    }
                    else {
                        familyMembers.get("Mother").add(person);
                    }
                }
                else if (rootPerson.getSpouseID() != null &&
                         rootPerson.getSpouseID().equals(person.getPersonID())) {
                    if (!familyMembers.containsKey("Spouse")) {
                        ArrayList<Person> newList = new ArrayList<>();
                        newList.add(person);
                        familyMembers.put("Spouse", newList);
                    }
                    else {
                        familyMembers.get("Spouse").add(person);
                    }
                }
                else if (person.getFatherID() != null &&
                         person.getFatherID().equals(personID) ||
                         person.getMotherID() != null &&
                         person.getMotherID().equals(personID)) {
                    if (!familyMembers.containsKey("Child")) {
                        ArrayList<Person> newList = new ArrayList<>();
                        newList.add(person);
                        familyMembers.put("Child", newList);
                    }
                    else {
                        familyMembers.get("Child").add(person);
                    }
                }
            }
            return familyMembers;
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