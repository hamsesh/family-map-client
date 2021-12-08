package com.example.familymap;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockContext;

import androidx.preference.PreferenceManager;

import com.github.ivanshafran.sharedpreferencesmock.SPMockBuilder;

import net.jakehamzawi.familymap.DataCache;
import net.jakehamzawi.familymap.R;
import net.jakehamzawi.familymap.ServerProxy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
    public void filterFemales() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("male", true).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);
        Person[] filteredPersons = dataCache.getPersons();
        Assert.assertEquals(personMap.size() / 2 + 1, filteredPersons.length);
        for (Person person : dataCache.getPersons()) {
            Assert.assertNotEquals("f", person.getGender());
        }

        for (Event event : dataCache.getEvents()) {
            Assert.assertNotEquals("f", personMap.get(event.getPersonID()).getGender());
        }
    }

    @Test
    public void filterMales() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("female", true).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);
        Person[] filteredPersons = dataCache.getPersons();
        Assert.assertEquals(personMap.size() / 2, filteredPersons.length);
        for (Person person : dataCache.getPersons()) {
            Assert.assertNotEquals("m", person.getGender());
        }

        for (Event event : dataCache.getEvents()) {
            Assert.assertNotEquals("m", personMap.get(event.getPersonID()).getGender());
        }
    }

    @Test
    public void filterFamily() {
        SharedPreferences prefs = new SPMockBuilder().createSharedPreferences();
        prefs.edit().putBoolean("family_lines", true).commit();
        DataCache dataCache = DataCache.getFilteredInstance(prefs);

        Person rootPerson = personMap.get(dataCache.getUser().getPersonID());
        Assert.assertNotNull(rootPerson);

        HashMap<String, Person> familyMap = new HashMap<>();
        addFamilyMember(rootPerson, familyMap);

        for (Person person : dataCache.getPersons()) {
            Assert.assertNotNull(personMap.get(person.getPersonID()));
        }

        for (Event event : dataCache.getEvents()) {
            Assert.assertNotNull(personMap.get(event.getPersonID()));
        }
    }

    private void addFamilyMember(Person person, HashMap<String, Person> familyMap) {
        if (person == null) return;
        Person mother = personMap.get(person.getMotherID());
        Person father = personMap.get(person.getFatherID());
        addFamilyMember(mother, familyMap);
        addFamilyMember(father, familyMap);
        familyMap.put(person.getPersonID(), person);
    }


    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }

}
