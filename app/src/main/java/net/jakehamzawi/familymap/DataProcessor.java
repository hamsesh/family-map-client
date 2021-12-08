package net.jakehamzawi.familymap;

import net.jakehamzawi.familymap.model.FamilyMember;

import java.util.ArrayList;
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
}
