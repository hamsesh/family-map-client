package com.example.familymap;

import android.content.SharedPreferences;

import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder;

import net.jakehamzawi.familymap.data.DataCache;
import net.jakehamzawi.familymap.ServerProxy;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.HashMap;

import model.AuthToken;
import model.Event;
import model.Person;
import model.User;
import request.RegisterRequest;
import result.EventResult;
import result.PersonResult;
import result.RegisterResult;

public class FilterTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";
    private static HashMap<String, Person> personMap = null;


    @BeforeClass
    public static void setup() {
        ServerProxy proxy = new ServerProxy();
        DataCache dataCache = DataCache.getInstance();


        RegisterRequest request = new RegisterRequest("test", "secret", "test@test.com",
                "Tester", "Testington", "m");
        RegisterResult result = proxy.register(request, HOST, PORT);
        dataCache.setUser(new User(result.getUsername(), "secret", "test@test.com",
                "Tester", "Testington", "m", result.getPersonID()));
        dataCache.setAuthToken(new AuthToken(result.getAuthtoken(), result.getUsername()));

        PersonResult personResult = proxy.persons(HOST, PORT);
        dataCache.setPersons(personResult.getData());

        EventResult eventResult = proxy.events(HOST, PORT);
        dataCache.setEvents(eventResult.getData());

        personMap = dataCache.getPersonMap();
    }

    @Test
    public void showFemales() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("male", true).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);
        Person[] filteredPersons = dataCache.getPersons();
        assertEquals(personMap.size() / 2 + 1, filteredPersons.length);
        for (Person person : dataCache.getPersons()) {
            assertNotEquals("f", person.getGender());
        }

        for (Event event : dataCache.getEvents()) {
            Person person = personMap.get(event.getPersonID());
            assert person != null;
            assertNotEquals("f", person.getGender());
        }
    }

    @Test
    public void showMales() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("female", true).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);
        Person[] filteredPersons = dataCache.getPersons();

        assertEquals(personMap.size() / 2, filteredPersons.length);
        for (Person person : dataCache.getPersons()) {
            assertNotEquals("m", person.getGender());
        }

        for (Event event : dataCache.getEvents()) {
            Person person = personMap.get(event.getPersonID());
            assert person != null;
            assertNotEquals("m", person.getGender());
        }
    }

    @Test
    public void showNothing() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("female", true).commit();
        prefs.edit().putBoolean("male", true).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);
        Person[] filteredPersons = dataCache.getPersons();

        assertEquals(personMap.size(), filteredPersons.length);
        boolean foundFemale = false;
        boolean foundMale = false;
        for (Person person : dataCache.getPersons()) {
            if (person.getGender().equalsIgnoreCase("m")) foundMale = true;
            else if (person.getGender().equalsIgnoreCase("f")) foundFemale = true;
        }
        assertTrue(foundFemale);
        assertTrue(foundMale);

        foundFemale = false;
        foundMale = false;
        for (Event event : dataCache.getEvents()) {
            Person person = personMap.get(event.getPersonID());
            assert person != null;
            if (person.getGender().equalsIgnoreCase("m")) foundMale = true;
            else if (person.getGender().equalsIgnoreCase("f")) foundFemale = true;
        }
        assertTrue(foundFemale);
        assertTrue(foundMale);
    }


    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }

    private void addFamilyMember(Person person, HashMap<String, Person> familyMap) {
        if (person == null) return;
        Person mother = personMap.get(person.getMotherID());
        Person father = personMap.get(person.getFatherID());
        addFamilyMember(mother, familyMap);
        addFamilyMember(father, familyMap);
        familyMap.put(person.getPersonID(), person);
    }

}
