package net.jakehamzawi.familymap.data;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import model.*;

public class DataCache {
    private static DataCache instance;
    private User user;
    private AuthToken authToken;
    private Event[] events;
    private HashMap<String, Person> personMap;
    private HashMap<String, Person> filteredPersons;
    private ArrayList<Event> filteredEvents;
    private final HashMap<String, Float> colorMap = new HashMap<>();
    private static final float[] MARKER_COLORS = { BitmapDescriptorFactory.HUE_RED,
                                                    BitmapDescriptorFactory.HUE_YELLOW,
                                                    BitmapDescriptorFactory.HUE_AZURE,
                                                    BitmapDescriptorFactory.HUE_GREEN,
                                                    BitmapDescriptorFactory.HUE_ROSE,
                                                    BitmapDescriptorFactory.HUE_ORANGE,
                                                    BitmapDescriptorFactory.HUE_VIOLET,
                                                    BitmapDescriptorFactory.HUE_MAGENTA };

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
        return instance;
    }

    public static DataCache getFilteredInstance(SharedPreferences prefs) {
        instance.filter(prefs.getBoolean("mother_side", false), prefs.getBoolean("father_side", false),
                prefs.getBoolean("female", false), prefs.getBoolean("male", false));
        return instance;
    }

    private DataCache() {}

    public void setUser(User user) {
        this.user = user;
    }

    public void setAuthToken(AuthToken token) {
        this.authToken = token;
    }

    public void setPersons(Person[] persons) {
        this.personMap = new HashMap<>();
        this.filteredPersons = new HashMap<>();
        for (Person person : persons) {
            this.personMap.put(person.getPersonID(), person);
            this.filteredPersons.put(person.getPersonID(), person);
        }
    }

    public void setEvents(Event[] events) {
        this.events = events;
        this.filteredEvents = new ArrayList<>();
        filteredEvents.addAll(Arrays.asList(events));
    }

    public User getUser() {
        return this.user;
    }

    public AuthToken getAuthToken() {
        return this.authToken;
    }

    public Person[] getPersons() {
        return this.filteredPersons.values().toArray(new Person[0]);
    }

    public HashMap<String, Person> getPersonMap() {
        return personMap;
    }

    public Event[] getEvents() {
        return filteredEvents.toArray(new Event[0]);
    }

    public float getColor(String eventType) {
        if (!colorMap.containsKey(eventType.toLowerCase(Locale.ROOT))) {
            colorMap.put(eventType.toLowerCase(Locale.ROOT), MARKER_COLORS[colorMap.size() % MARKER_COLORS.length]);
        }
        return colorMap.get(eventType.toLowerCase(Locale.ROOT));
    }

    public void invalidate() {
        this.user = null;
        this.authToken = null;
        this.events = null;
        this.personMap = null;
        this.filteredPersons = null;
        this.filteredEvents = null;
    }

    public Person getPersonByID(String id) {
        return filteredPersons.get(id);
    }

    public Event getEventByID(String id) {
        for (Event event : this.events) {
            if (id.equals(event.getEventID())) {
                return event;
            }
        }
        return null;
    }

    private void filter(boolean motherSide, boolean fatherSide, boolean female, boolean male) {
        this.filteredPersons.clear();
        Person rootPerson = this.personMap.get(this.user.getPersonID());
        assert rootPerson != null;
        Person spouse = this.personMap.get(rootPerson.getSpouseID());
        if (male || female) {
            if (male) {
                if (rootPerson.getGender().equals("m")) {
                    filteredPersons.put(rootPerson.getPersonID(), rootPerson);
                }
                if (spouse != null) {
                    if (spouse.getGender().equalsIgnoreCase("m")) {
                        filteredPersons.put(spouse.getPersonID(), spouse);
                    }
                }
            }
            else {
                if (rootPerson.getGender().equals("f")) {
                    filteredPersons.put(rootPerson.getPersonID(), rootPerson);
                }
                if (spouse != null) {
                    if (spouse.getGender().equalsIgnoreCase("f")) {
                        filteredPersons.put(spouse.getPersonID(), spouse);
                    }
                }
            }
        }
        else {
            filteredPersons.put(rootPerson.getPersonID(), rootPerson);
            if (spouse != null) filteredPersons.put(spouse.getPersonID(), spouse);
        }

        Person mother = this.personMap.get(rootPerson.getMotherID());
        Person father = this.personMap.get(rootPerson.getFatherID());
        if (motherSide || fatherSide) {
            if (motherSide) {
                addLineage(mother, female, male);
            }
            if (fatherSide) {
                addLineage(father, female, male);
            }
        }
        else {
            addLineage(mother, female, male);
            addLineage(father, female, male);
        }
        matchEventsToPersons();
    }

    private void addLineage(Person person, boolean female, boolean male) {
        if (person == null) return;
        addLineage(personMap.get(person.getMotherID()), female, male);
        addLineage(personMap.get(person.getFatherID()), female, male);
        if (!female && !male) {
            this.filteredPersons.put(person.getPersonID(), person);
        }
        else {
            if (female) {
                if (person.getGender().equals("f")) {
                    this.filteredPersons.put(person.getPersonID(), person);
                }
            }
            if (male) {
                if (person.getGender().equals("m")) {
                    this.filteredPersons.put(person.getPersonID(), person);
                }
            }
        }
    }

    private void matchEventsToPersons() {
        this.filteredEvents.clear();
        for (Event event : this.events) {
            if (filteredPersons.containsKey(event.getPersonID())) {
                this.filteredEvents.add(event);
            }
        }
    }
}
