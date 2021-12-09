package net.jakehamzawi.familymap;

import net.jakehamzawi.familymap.data.DataCache;
import net.jakehamzawi.familymap.ServerProxy;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import model.AuthToken;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.PersonResult;
import result.RegisterResult;

public class PersonsTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";

    @BeforeClass
    public static void setup() {
        ServerProxy proxy = new ServerProxy();
        RegisterRequest request = new RegisterRequest("test", "secret", "test@test.com",
                "Tester", "Testington", "m");
        RegisterResult result = proxy.register(request, HOST, PORT);
    }

    @Test
    public void getPersons() {
        ServerProxy proxy = new ServerProxy();
        LoginRequest request = new LoginRequest("test", "secret");
        LoginResult loginResult = proxy.login(request, HOST, PORT);

        DataCache dataCache = DataCache.getInstance();
        dataCache.setAuthToken(new AuthToken(loginResult.getAuthtoken(), loginResult.getUsername()));

        PersonResult personResult = proxy.persons(HOST, PORT);

        assertNotNull(personResult);
        assertTrue(personResult.isSuccess());
        assertNotNull(personResult.getData());
        assertEquals(31, personResult.getData().length);
    }

    @Test
    public void getPersonsWrongAuthToken() {
        ServerProxy proxy = new ServerProxy();
        DataCache dataCache = DataCache.getInstance();
        dataCache.setAuthToken(new AuthToken("NULL", "test"));
        PersonResult personResult = proxy.persons(HOST, PORT);

        assertNotNull(personResult);
        assertFalse(personResult.isSuccess());
        assertNull(personResult.getData());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
