package com.example.familymap;

import net.jakehamzawi.familymap.data.DataCache;
import net.jakehamzawi.familymap.data.DataProcessor;
import net.jakehamzawi.familymap.ServerProxy;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.AuthToken;
import model.Event;
import model.Person;
import model.User;
import request.RegisterRequest;
import result.EventResult;
import result.PersonResult;
import result.RegisterResult;

public class EventSortTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";

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
    }

    @Test
    public void testSort() {
        DataCache dataCache = DataCache.getInstance();
        HashMap<String, Person> personMap = dataCache.getPersonMap();
        HashMap<Person, List<Event>> dataMap = new HashMap<>();

        for (Event event : dataCache.getEvents()) {
            Person person = personMap.get(event.getPersonID());
            if (!dataMap.containsKey(person)) {
                dataMap.put(person, new ArrayList<>());
            }
            dataMap.get(person).add(event);
        }

        for (Map.Entry<Person, List<Event>> entry : dataMap.entrySet()) {
            entry.getValue().sort(new Event.EventComparator());
        }

        for (Map.Entry<Person, List<Event>> entry : dataMap.entrySet()) {
            int min = -1;
            for (Event event : entry.getValue()) {
                assertTrue(event.getYear() > min);
                min = event.getYear();
            }
        }
    }

    @Test
    public void testSortSameYear() {
        Event event1 = new Event("00000", "test", "99999", 10f,
                10f, "USA", "Folsom", "Birth", 1999);
        Event event2 = new Event("00000", "test", "99999", 10f,
                10f, "USA", "Folsom", "Test", 1999);
        Event event3 = new Event("00000", "test", "99999", 10f,
                10f, "USA", "Folsom", "Death", 1999);

        List<Event> events = Stream.of(event1, event3, event2).collect(Collectors.toList());
        events.sort(new Event.EventComparator());

        assertEquals("Birth", events.get(0).getEventType());
        assertEquals("Test", events.get(1).getEventType());
        assertEquals("Death", events.get(2).getEventType());
    }

    @Test
    public void testBirthAfterDeath() {
        Event event1 = new Event("00000", "test", "99999", 10f,
                10f, "USA", "Folsom", "Birth", 2020);
        Event event2 = new Event("00000", "test", "99999", 10f,
                10f, "USA", "Folsom", "Death", 1999);

        List<Event> events = Stream.of(event2, event1).collect(Collectors.toList());
        events.sort(new Event.EventComparator());

        assertEquals("Birth", events.get(0).getEventType());
        assertEquals("Death", events.get(1).getEventType());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
