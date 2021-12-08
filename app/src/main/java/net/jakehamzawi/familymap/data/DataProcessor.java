package net.jakehamzawi.familymap.data;

import net.jakehamzawi.familymap.model.FamilyMember;
import net.jakehamzawi.familymap.model.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import model.Event;
import model.Person;

public class DataProcessor {

    public static ArrayList<FamilyMember> findFamily(Person rootPerson) {
        ArrayList<FamilyMember> familyMembers = new ArrayList<>();
        for (Person person : DataCache.getInstance().getPersons()) {
            if (rootPerson.getMotherID() != null &&
                    rootPerson.getFatherID().equals(person.getPersonID())) {
                familyMembers.add(new FamilyMember("Father", person));
            }
            else if (rootPerson.getMotherID() != null &&
                    rootPerson.getMotherID().equals(person.getPersonID())) {
                familyMembers.add(new FamilyMember("Mother", person));
            }
            else if (rootPerson.getSpouseID() != null &&
                    rootPerson.getSpouseID().equals(person.getPersonID())) {
                familyMembers.add(new FamilyMember("Spouse", person));
            }
            else if (person.getFatherID() != null &&
                    person.getFatherID().equals(person.getPersonID()) ||
                    person.getMotherID() != null &&
                            person.getMotherID().equals(person.getPersonID())) {
                familyMembers.add(new FamilyMember("Child", person));
            }
        }
        familyMembers.sort(new FamilyMember.FamilyMemberComparator());
        return familyMembers;
    }

    public static ArrayList<Event> sortEvents(List<Event> events) {
        Set<Event> sortedEvents = new TreeSet<>(events);
        return new ArrayList<>(sortedEvents);
    }

    public static void searchPersons(List<SearchResult> results, String query) {
        if (query.isEmpty() || query.trim().isEmpty()) return;
        for (Person person : DataCache.getInstance().getPersons()) {
            if (containsIgnoreCase(person.getFirstName() + " " + person.getLastName(), query)) {
                results.add(new SearchResult(person));
            }
        }
    }

    public static void searchEvents(List<SearchResult> results, String query) {
        if (query.isEmpty() || query.trim().isEmpty()) return;
        DataCache dataCache = DataCache.getInstance();
        for (Event event : DataCache.getInstance().getEvents()) {
            if (containsIgnoreCase(event.getCountry(), query) ||
                    containsIgnoreCase(event.getCity(), query) ||
                    containsIgnoreCase(event.getEventType(), query) ||
                    containsIgnoreCase(String.valueOf(event.getYear()), query)) {

                Person person = dataCache.getPersonByID(event.getPersonID());
                assert person != null;
                results.add(new SearchResult(event, person.getFirstName(), person.getLastName()));
            }
        }
    }

    public static boolean containsIgnoreCase(String src, String what) {
        if (src == null) return false;
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

    public static HashMap<String, Person> generatePersonMap(Person[] filteredPersons) {
        HashMap<String, Person> personMap = new HashMap<>();
        for (Person person : filteredPersons) {
            personMap.put(person.getPersonID(), person);
        }
        return personMap;
    }

    public static HashMap<String, TreeSet<Event>> generateSortedEventMap(Event[] filteredEvents,
                                                            HashMap<String, Person> personMap) {
        HashMap<String, TreeSet<Event>> sortedEventsByPerson = new HashMap<>();
        for (Event event : filteredEvents) {
            Person person = personMap.get(event.getPersonID());
            assert person != null;
            if (sortedEventsByPerson.get(person.getPersonID()) == null) {
                TreeSet<Event> personEvents = new TreeSet<>();
                personEvents.add(event);
                sortedEventsByPerson.put(person.getPersonID(), personEvents);
            }
            else {
                Set<Event> sortedEvents = sortedEventsByPerson.get(person.getPersonID());
                assert sortedEvents != null;
                sortedEvents.add(event);
            }
        }
        return sortedEventsByPerson;
    }
}
