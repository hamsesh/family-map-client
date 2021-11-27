package net.jakehamzawi.familymap;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.AuthToken;
import model.Person;
import model.User;
import request.LoginRequest;
import request.RegisterRequest;
import result.EventResult;
import result.LoginResult;
import result.PersonResult;
import result.RegisterResult;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    enum Field { HOST, PORT, USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, GENDER }
    protected Listener listener;

    public interface Listener {
        void notifyDone();
    }

    public void registerListener(Listener listener) { this.listener = listener; }

    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String STATUS_KEY = "success";

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);
        RadioGroup rg = view.findViewById(R.id.genderSelection);
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        Map<Field, EditText> loginFields = new EnumMap<>(Field.class);
        Map<Field, EditText> registerFields = new EnumMap<>(Field.class);
        Map<Field, Boolean> activeFields = new EnumMap<>(Field.class);
        // Initialize current active fields
        for (Field field : Field.values()) {
            activeFields.put(field, false);
        }

        loginFields.put(Field.HOST, view.findViewById(R.id.hostField));
        loginFields.put(Field.PORT, view.findViewById(R.id.portField));
        loginFields.put(Field.USERNAME, view.findViewById(R.id.usernameField));
        loginFields.put(Field.PASSWORD, view.findViewById(R.id.passwordField));
        registerFields.put(Field.FIRST_NAME, view.findViewById(R.id.firstNameField));
        registerFields.put(Field.LAST_NAME, view.findViewById(R.id.lastNameField));
        registerFields.put(Field.EMAIL, view.findViewById(R.id.emailField));

        for (Map.Entry<Field, EditText> entry : Stream.concat(loginFields.entrySet().stream(),
                registerFields.entrySet().stream()).collect(Collectors.toSet())) {
            entry.getValue().addTextChangedListener(new TextWatcher() {
                boolean changeButtons = false;
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // If field is empty, buttons will change on text update
                    if (s.toString().trim().length() == 0) {
                        changeButtons = true;
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.toString().trim().length() > 0) {
                        activeFields.put(entry.getKey(), true);
                    }
                    else {
                        activeFields.put(entry.getKey(), false);
                        changeButtons = true;
                    }
                    if (changeButtons) {
                        updateButtons(activeFields, loginFields, loginButton, registerButton);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        rg.setOnCheckedChangeListener((group, checkedId) -> {
            boolean changeButtons = false;
            if (activeFields.containsKey(Field.GENDER)) {
                changeButtons = true;
            }
            activeFields.put(Field.GENDER, true);
            if (changeButtons) {
                updateButtons(activeFields, loginFields, loginButton, registerButton);
            }
        });
        Context currentContext = getContext();

        loginButton.setOnClickListener(v -> {
            Log.d("Login", "Register button pressed!");
            String host = Objects.requireNonNull(loginFields.get(Field.HOST)).getText().toString();
            String port = Objects.requireNonNull(loginFields.get(Field.PORT)).getText().toString();
            String username = Objects.requireNonNull(loginFields.get(Field.USERNAME)).getText().toString();
            String password = Objects.requireNonNull(loginFields.get(Field.PASSWORD)).getText().toString();

            LoginData loginData = new LoginData(host, port, username, password);

            Handler uiThreadHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    String status = bundle.getString(STATUS_KEY);
                    Log.d("Login", "Handling login message...");
                    if (status != null && status.equals("success")) {
                        listener.notifyDone();
                        Toast.makeText(currentContext, bundle.getString(FIRST_NAME_KEY) + " " +
                                bundle.getString(LAST_NAME_KEY), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Log.d("Login", "About to send login failure toast...");
                        Toast.makeText(currentContext, "Login failed", Toast.LENGTH_LONG).show();
                    }
                }
            };

            LoginTask loginTask = new LoginTask(uiThreadHandler, loginData);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(loginTask);
        });

        registerButton.setOnClickListener(v -> {

            Log.d("Register", "Register button pressed!");
            String host = Objects.requireNonNull(loginFields.get(Field.HOST)).getText().toString();
            String port = Objects.requireNonNull(loginFields.get(Field.PORT)).getText().toString();
            String username = Objects.requireNonNull(loginFields.get(Field.USERNAME)).getText().toString();
            String password = Objects.requireNonNull(loginFields.get(Field.PASSWORD)).getText().toString();
            String firstName = Objects.requireNonNull(registerFields.get(Field.FIRST_NAME)).getText().toString();
            String lastName = Objects.requireNonNull(registerFields.get(Field.LAST_NAME)).getText().toString();
            String email = Objects.requireNonNull(registerFields.get(Field.EMAIL)).getText().toString();
            String gender = null;

            int radioButtonID = rg.getCheckedRadioButtonId();
            if (radioButtonID == R.id.male) {
                gender = "m";
            }
            else if (radioButtonID == R.id.female) {
                gender = "f";
            }
            LoginData loginData = new LoginData(host, port, username, password, firstName,
                    lastName, email, gender);

            Handler uiThreadHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    String status = bundle.getString(STATUS_KEY);
                    Log.d("Register", "Handling register message...");
                    if (status != null && status.equals("success")) {
                        listener.notifyDone();
                        Toast.makeText(currentContext, bundle.getString(FIRST_NAME_KEY) + " " +
                                bundle.getString(LAST_NAME_KEY), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Log.d("Register", "About to send failure toast...");
                        Toast.makeText(currentContext, "Register failed", Toast.LENGTH_LONG).show();
                    }
                }
            };

            RegisterTask registerTask = new RegisterTask(uiThreadHandler, loginData);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(registerTask);
        });


        return view;
    }

    private static class LoginTask implements Runnable {
        private final Handler handler;
        private final LoginData loginData;

        public LoginTask(Handler handler, LoginData loginData) {
            this.handler = handler;
            this.loginData = loginData;
        }

        @Override
        public void run() {
            LoginRequest request = new LoginRequest(loginData.username, loginData.password);
            ServerProxy proxy = new ServerProxy();
            LoginResult result = proxy.login(request, loginData.host, loginData.port);
            Log.d("Login", String.format("Login result: %s, %s",
                    result.isSuccess()?"success":"failure", result.getMessage()));
            if (!result.isSuccess()) {
                Message message = handler.obtainMessage();
                Bundle messageBundle = new Bundle();
                messageBundle.putString(STATUS_KEY, "failure");
                message.setData(messageBundle);
                handler.sendMessage(message);
                return;
            }
            else {
                DataCache dataCache = DataCache.getInstance();
                dataCache.setUser(new User(result.getUsername(), loginData.password, loginData.email,
                        loginData.firstName, loginData.lastName, loginData.gender,
                        result.getPersonID()));
                dataCache.setAuthToken(new AuthToken(result.getAuthtoken(), result.getUsername()));
            }
            Log.d("Register", "About to send register message...");

            // Get user data on new thread
            DataTask dataTask = new DataTask(handler, loginData.host, loginData.port, result.getPersonID());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(dataTask);

        }
    }

    private static class RegisterTask implements Runnable {
        private final Handler handler;
        private final LoginData loginData;

        public RegisterTask(Handler handler, LoginData loginData) {
            this.handler = handler;
            this.loginData = loginData;
        }

        @Override
        public void run() {
            RegisterRequest request = new RegisterRequest(loginData.username, loginData.password,
                    loginData.email, loginData.firstName, loginData.lastName, loginData.gender);
            ServerProxy proxy = new ServerProxy();
            RegisterResult result = proxy.register(request, loginData.host, loginData.port);
            Log.d("Register", String.format("Register result: %s, %s",
                    result.isSuccess()?"success":"failure", result.getMessage()));
            if (!result.isSuccess()) {
                Message message = handler.obtainMessage();
                Bundle messageBundle = new Bundle();
                messageBundle.putString(STATUS_KEY, "failure");
                message.setData(messageBundle);
                handler.sendMessage(message);
                return;
            }
            else {
                DataCache dataCache = DataCache.getInstance();
                dataCache.setUser(new User(result.getUsername(), loginData.password, loginData.email,
                        loginData.firstName, loginData.lastName, loginData.gender,
                        result.getPersonID()));
                dataCache.setAuthToken(new AuthToken(result.getAuthtoken(), result.getUsername()));
            }
            Log.d("Register", "About to send register message...");


            // Get user data on new thread
            DataTask dataTask = new DataTask(handler, loginData.host, loginData.port, result.getPersonID());
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(dataTask);
        }
    }

    private static class DataTask implements Runnable {
        private final Handler handler;
        private final String host;
        private final String port;
        private final String personID;

        public DataTask(Handler handler, String host, String port, String personID) {
            this.handler = handler;
            this.host = host;
            this.port = port;
            this.personID = personID;
        }

        @Override
        public void run() {
            DataCache dataCache = DataCache.getInstance();
            ServerProxy proxy = new ServerProxy();
            PersonResult personResult = proxy.persons(host, port);
            EventResult eventResult = proxy.events(host, port);
            Log.d("Data", String.format("Persons result: %s, %s",
                    personResult.isSuccess()?"success":"failure", personResult.getMessage()));
            Log.d("Data", String.format("Events result: %s, %s",
                    personResult.isSuccess()?"success":"failure", eventResult.getMessage()));
            if (personResult.isSuccess() && eventResult.isSuccess()) {
                dataCache.setPersons(personResult.getData());
                dataCache.setEvents(eventResult.getData());
            }
            else {
                Log.e("Data", "Unable to download user data from server");
            }
            sendMessage(personResult, eventResult);
        }

        private void sendMessage(PersonResult personResult, EventResult eventResult) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            boolean success = personResult.isSuccess() && eventResult.isSuccess();
            messageBundle.putString(STATUS_KEY, success ? "success" : "failure");

            if (success) {
                DataCache dataCache = DataCache.getInstance();
                Person userPerson = dataCache.getPersonByID(personID);
                messageBundle.putString(FIRST_NAME_KEY, userPerson.getFirstName());
                messageBundle.putString(LAST_NAME_KEY, userPerson.getLastName());
            }
            message.setData(messageBundle);
            handler.sendMessage(message);
        }

    }

    private static class LoginData {
        private final String host;
        private final String port;
        private final String username;
        private final String password;
        private final String firstName;
        private final String lastName;
        private final String email;
        private final String gender;

        public LoginData(String host, String port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this. firstName = null;
            this. lastName = null;
            this.email = null;
            this.gender = null;
        }

        public LoginData(String host, String port, String username, String password,
                         String firstName, String lastName, String email, String gender) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this. firstName = firstName;
            this. lastName = lastName;
            this.email = email;
            this.gender = gender;
        }
    }

    private boolean registerReady(Map<Field, Boolean> activeFields) {
        boolean valid = true;
        for (Boolean filled : activeFields.values()) {
            if (!filled) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    private boolean loginReady(Map<Field, Boolean> activeFields, Map<Field, EditText> loginFields) {
        boolean valid = true;
        for (Field field : loginFields.keySet()) {
            if (activeFields.get(field) == null || !activeFields.get(field)) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    private void updateButtons(Map<Field, Boolean> activeFields, Map<Field, EditText> loginFields,
                               Button loginButton, Button registerButton) {
        loginButton.setEnabled(loginReady(activeFields, loginFields));
        registerButton.setEnabled(registerReady(activeFields));
    }
}