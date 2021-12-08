package net.jakehamzawi.familymap;

import android.util.Log;

import com.google.gson.Gson;

import net.jakehamzawi.familymap.data.DataCache;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import request.LoginRequest;
import request.RegisterRequest;
import result.ClearResult;
import result.EventResult;
import result.LoginResult;
import result.PersonResult;
import result.RegisterResult;

public class ServerProxy {
    public RegisterResult register(RegisterRequest request, String host, String port) {
        try {
            if (host == null || host.isEmpty()) {
                throw new MalformedURLException("Invalid host");
            }
            if (port == null || port.isEmpty()) {
                throw new MalformedURLException("Invalid port number");
            }
            URL url = new URL("HTTP", host, Integer.parseInt(port), "/user/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty("Accept", "application/json");
            connection.connect();

            Gson gson = new Gson();
            String jsonRequest = gson.toJson(request);
            OutputStream os = connection.getOutputStream();

            os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            os.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d("Proxy", "Opening response stream...");
                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                RegisterResult result = gson.fromJson(reader, RegisterResult.class);
                Log.d("Proxy", "Response stream closed");
                is.close();
                return result;
            }
            else {
                return new RegisterResult(null, null, null,
                        false, "Unable to connect to server");
            }
        }
        catch (IOException e) {
            Log.e("Proxy", "Register: " + e.getMessage());
            return new RegisterResult(null, null, null, false,
                    e.getMessage());
        }
    }

    public LoginResult login(LoginRequest request, String host, String port) {
        try {
            if (host == null || host.isEmpty()) {
                throw new MalformedURLException("Invalid host");
            }
            if (port == null || port.isEmpty()) {
                throw new MalformedURLException("Invalid port number");
            }
            URL url = new URL("HTTP", host, Integer.parseInt(port), "/user/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty("Accept", "application/json");
            connection.connect();

            Gson gson = new Gson();
            String jsonRequest = gson.toJson(request);
            OutputStream os = connection.getOutputStream();
            os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            os.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                Log.d("Proxy", "Response stream closed");
                LoginResult result = gson.fromJson(reader, LoginResult.class);
                is.close();
                return result;
            }
            else {
                return new LoginResult(null, null, null,
                        "Unable to connect to server", false);
            }
        }
        catch (IOException e) {
            Log.e("Proxy", "Login: " + e.getMessage());
            return new LoginResult(null, null, null,
                    "Invalid URL", false);
        }
    }

    public PersonResult persons(String host, String port) {
        try {
            if (host == null || host.isEmpty()) {
                throw new MalformedURLException("Invalid host");
            }
            if (port == null || port.isEmpty()) {
                throw new MalformedURLException("Invalid port number");
            }
            DataCache dataCache = DataCache.getInstance();
            URL url = new URL("HTTP", host, Integer.parseInt(port), "/person/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", dataCache.getAuthToken().getToken());
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                Log.d("Proxy", "Response stream closed");
                PersonResult result = gson.fromJson(reader, PersonResult.class);
                is.close();
                return result;
            }
            else {
                return new PersonResult(null,
                        "Unable to connect to server", false);
            }
        }
        catch (IOException e) {
            Log.e("Proxy", "Persons: " + e.getMessage());
            return new PersonResult(null,
                    "Invalid URL", false);
        }
    }

    public EventResult events(String host, String port) {
        try {
            if (host == null || host.isEmpty()) {
                throw new MalformedURLException("Invalid host");
            }
            if (port == null || port.isEmpty()) {
                throw new MalformedURLException("Invalid port number");
            }
            DataCache dataCache = DataCache.getInstance();
            URL url = new URL("HTTP", host, Integer.parseInt(port), "/event/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", dataCache.getAuthToken().getToken());
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                Log.d("Proxy", "Events: Response stream closed");
                EventResult result = gson.fromJson(reader, EventResult.class);
                is.close();
                return result;
            }
            else {
                return new EventResult(null,
                        "Unable to connect to server", false);
            }
        }
        catch (IOException e) {
            Log.e("Proxy", "Events: " + e.getMessage());
            return new EventResult(null,
                    "Invalid URL", false);
        }
    }

    public ClearResult clear(String host, String port) {
        try {
            if (host == null || host.isEmpty()) {
                throw new MalformedURLException("Invalid host");
            }
            if (port == null || port.isEmpty()) {
                throw new MalformedURLException("Invalid port number");
            }
            URL url = new URL("HTTP", host, Integer.parseInt(port), "/clear/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Gson gson = new Gson();
                InputStream is = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(is);
                ClearResult result = gson.fromJson(reader, ClearResult.class);
                is.close();
                Log.d("Proxy", "Clear: clear command successful");
                return result;
            }
            else {
                return new ClearResult("Unable to connect to server", false);
            }
        }
        catch (IOException e) {
            Log.e("Proxy", e.getMessage());
            return new ClearResult("Invalid URL", false);
        }
    }
}
