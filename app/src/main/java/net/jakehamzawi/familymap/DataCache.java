package net.jakehamzawi.familymap;

import model.*;

public class DataCache {
    private static DataCache instance;
    private User user;
    private AuthToken authToken;
    private Person[] persons;
    private Event[] events;

    public static DataCache getInstance() {
        if (instance == null) {
            instance = new DataCache();
        }
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
        this.persons = persons;
    }

    public void setEvents(Event[] events) {
        this.events = events;
    }

    public User getUser() {
        return this.user;
    }

    public AuthToken getAuthToken() {
        return this.authToken;
    }

    public Person[] getPersons() {
        return this.persons;
    }

    public Event[] getEvents() {
        return events;
    }

    public void invalidate() {
        this.user = null;
        this.authToken = null;
        this.persons = null;
        this.events = null;
    }

    public Person getPersonByID(String id) {
        for (Person person : this.persons) {
            if (id.equals(person.getPersonID())) {
                return person;
            }
        }
        return null;
    }

    public Event getEventByID(String id) {
        for (Event event : this.events) {
            if (id.equals(event.getPersonID())) {
                return event;
            }
        }
        return null;
    }
}
