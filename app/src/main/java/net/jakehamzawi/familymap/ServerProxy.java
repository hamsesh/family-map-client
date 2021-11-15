package net.jakehamzawi.familymap;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import request.RegisterRequest;
import result.RegisterResult;

public class ServerProxy {
    RegisterResult register(RegisterRequest request, String host, String port) {
        try {
            if (host == null || host.isEmpty()) {
                throw new MalformedURLException("Invalid host");
            }
            if (port == null || port.isEmpty()) {
                throw new MalformedURLException("Invalid port number");
            }
            Gson gson = new Gson();
            String jsonRequest = gson.toJson(request);
            URL url = new URL("HTTP", host, Integer.parseInt(port), "user/register");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            OutputStream os = connection.getOutputStream();
            os.write(jsonRequest.getBytes(StandardCharsets.UTF_8));
            os.close();

            Log.d("Register", "Opening response stream...");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String jsonResponse = null;
            while (reader.ready()) {
                jsonResponse = reader.readLine();
            }
            reader.close();
            Log.d("Register", "Response stream closed");
            RegisterResult result = gson.fromJson(jsonResponse, RegisterResult.class);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return result;
            }
            else {
                return new RegisterResult(null, null, null,
                        false, "Unable to connect to server");
            }
        }
        catch (IOException e) {
            Log.e("Register", e.getMessage(), e);
            return new RegisterResult(null, null, null, false,
                    e.getMessage());
        }
    }
}
