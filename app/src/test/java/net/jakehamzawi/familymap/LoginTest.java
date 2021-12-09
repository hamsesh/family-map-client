package net.jakehamzawi.familymap;

import net.jakehamzawi.familymap.ServerProxy;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

public class LoginTest {
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
    public void loginWrongPassword() {
        ServerProxy proxy = new ServerProxy();
        LoginRequest request = new LoginRequest("test", "wrong_pass");
        LoginResult result = proxy.login(request, HOST, PORT);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
