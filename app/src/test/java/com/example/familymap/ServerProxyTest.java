package com.example.familymap;


import net.jakehamzawi.familymap.DataCache;
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
import result.PersonResult;
import result.RegisterResult;

public class ServerProxyTest {
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
    public void successfulRegister() {
        ServerProxy proxy = new ServerProxy();
        RegisterRequest request = new RegisterRequest("register", "secret", "register@test.com",
                "Reggie", "Reggers", "m");
        RegisterResult result = proxy.register(request, HOST, PORT);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getUsername());
        assertNotNull(result.getAuthtoken());
        assertNotNull(result.getPersonID());
    }

    @Test
    public void successfulLogin() {
        ServerProxy proxy = new ServerProxy();
        LoginRequest request = new LoginRequest("test", "secret");
        LoginResult result = proxy.login(request, HOST, PORT);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getAuthtoken());
        assertNotNull(result.getUsername());
        assertNotNull(result.getPersonID());
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

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
