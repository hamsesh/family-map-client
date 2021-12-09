package net.jakehamzawi.familymap;

import net.jakehamzawi.familymap.data.DataCache;
import net.jakehamzawi.familymap.data.DataProcessor;
import net.jakehamzawi.familymap.ServerProxy;
import net.jakehamzawi.familymap.model.SearchResult;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.AuthToken;
import model.Person;
import model.User;
import request.RegisterRequest;
import result.EventResult;
import result.PersonResult;
import result.RegisterResult;

public class SearchTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";


    @BeforeClass
    public static void setup() {
        ServerProxy proxy = new ServerProxy();
        DataCache dataCache = DataCache.getInstance();


        RegisterRequest request = new RegisterRequest("search", "secret", "test@test.com",
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
    public void testSearchPersons() {
        List<SearchResult> results = new ArrayList<>();
        HashMap<String, Person> personMap = DataCache.getInstance().getPersonMap();
        DataProcessor.searchPersons(results, "se");

        for (SearchResult result : results) {
            assertTrue(DataProcessor.containsIgnoreCase(result.getMainInfo(), "se") ||
                    DataProcessor.containsIgnoreCase(result.getSubInfo(), "se"));
            Person person = personMap.get(result.getId());
            assertNotNull(person);
            assertTrue(DataProcessor.containsIgnoreCase(person.getFirstName(), "se") ||
                    DataProcessor.containsIgnoreCase(person.getLastName(), "se"));
        }
    }

    @Test
    public void testSearchEvents() {
        List<SearchResult> results = new ArrayList<>();
        DataProcessor.searchEvents(results, "e");

        for (SearchResult result : results) {
            assertTrue(DataProcessor.containsIgnoreCase(result.getMainInfo(), "e") ||
                    DataProcessor.containsIgnoreCase(result.getSubInfo(), "e"));
        }
    }

    @Test
    public void testPersonWithSpaces() {
        List<SearchResult> results = new ArrayList<>();
        DataProcessor.searchPersons(results, "tester testington");
        assertFalse(results.isEmpty());
    }

    @Test
    public void testNoResults() {
        List<SearchResult> results = new ArrayList<>();
        DataProcessor.searchPersons(results, "`");
        DataProcessor.searchEvents(results, "=");
        DataProcessor.searchPersons(results, "");
        DataProcessor.searchEvents(results, "");
        DataProcessor.searchPersons(results, " ");
        DataProcessor.searchEvents(results, " ");

        assertTrue(results.isEmpty());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
