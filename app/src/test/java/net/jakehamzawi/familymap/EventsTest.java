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
import result.EventResult;
import result.LoginResult;
import result.RegisterResult;

public class EventsTest {
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
    public void getEvents() {
        ServerProxy proxy = new ServerProxy();
        LoginRequest request = new LoginRequest("test", "secret");
        LoginResult loginResult = proxy.login(request, HOST, PORT);

        DataCache dataCache = DataCache.getInstance();
        dataCache.setAuthToken(new AuthToken(loginResult.getAuthtoken(), loginResult.getUsername()));

        EventResult eventResult = proxy.events(HOST, PORT);

        assertNotNull(eventResult);
        assertTrue(eventResult.isSuccess());
        assertNotNull(eventResult.getData());
        assertEquals(91, eventResult.getData().length);
    }

    @Test
    public void getPersonsWrongAuthToken() {
        ServerProxy proxy = new ServerProxy();
        DataCache dataCache = DataCache.getInstance();
        dataCache.setAuthToken(new AuthToken("NULL", "test"));
        EventResult eventResult = proxy.events(HOST, PORT);

        assertNotNull(eventResult);
        assertFalse(eventResult.isSuccess());
        assertNull(eventResult.getData());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
