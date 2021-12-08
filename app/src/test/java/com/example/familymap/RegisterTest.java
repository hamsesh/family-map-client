package com.example.familymap;

import net.jakehamzawi.familymap.ServerProxy;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import request.RegisterRequest;
import result.RegisterResult;

public class RegisterTest {
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
    public void registerExistingUser() {
        ServerProxy proxy = new ServerProxy();
        RegisterRequest request = new RegisterRequest("test", "secret", "register@test.com",
                "Reggie", "Reggers", "m");
        RegisterResult result = proxy.register(request, HOST, PORT);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
