package net.jakehamzawi.familymap;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.AuthToken;
import model.User;
import request.LoginRequest;
import request.RegisterRequest;
import result.LoginResult;
import result.RegisterResult;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private Listener listener;

    public interface Listener {
        void notifyDone();
    }

    public void registerListener(Listener listener) { this.listener = listener; }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String STATUS_KEY = "success";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button registerButton = view.findViewById(R.id.registerButton);
        Context currentContext = getContext();
        Log.d("Login", "About to set onClickListeners...");

        loginButton.setOnClickListener(v -> {
            Log.d("Login", "Register button pressed!");
            EditText editText = view.findViewById(R.id.hostField);
            String host = editText.getText().toString();
            editText = view.findViewById(R.id.portField);
            String port = editText.getText().toString();
            editText = view.findViewById(R.id.usernameField);
            String username = editText.getText().toString();
            editText = view.findViewById(R.id.passwordField);
            String password = editText.getText().toString();
            editText = view.findViewById(R.id.firstNameField);
            String firstName = editText.getText().toString();
            editText = view.findViewById(R.id.lastNameField);
            String lastName = editText.getText().toString();
            editText = view.findViewById(R.id.emailField);
            String email = editText.getText().toString();
            String gender = null;
            RadioGroup rg = view.findViewById(R.id.genderSelection);

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
                    Log.d("Login", "Handling login message...");
                    if (status != null && status.equals("success")) {
                        Toast.makeText(currentContext, bundle.getString(FIRST_NAME_KEY) + " " +
                                bundle.getString(LAST_NAME_KEY), Toast.LENGTH_LONG);
                    }
                    else {
                        Log.d("Login", "About to send login failure toast...");
                        Toast.makeText(currentContext, "Login failed", Toast.LENGTH_LONG);
                    }
                }
            };

            LoginTask loginTask = new LoginTask(uiThreadHandler, loginData);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(loginTask);
        });

        registerButton.setOnClickListener(v -> {

            Log.d("Login", "Register button pressed!");
            EditText editText = view.findViewById(R.id.hostField);
            String host = editText.getText().toString();
            editText = view.findViewById(R.id.portField);
            String port = editText.getText().toString();
            editText = view.findViewById(R.id.usernameField);
            String username = editText.getText().toString();
            editText = view.findViewById(R.id.passwordField);
            String password = editText.getText().toString();
            editText = view.findViewById(R.id.firstNameField);
            String firstName = editText.getText().toString();
            editText = view.findViewById(R.id.lastNameField);
            String lastName = editText.getText().toString();
            editText = view.findViewById(R.id.emailField);
            String email = editText.getText().toString();
            String gender = null;
            RadioGroup rg = view.findViewById(R.id.genderSelection);

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
                        Toast.makeText(currentContext, bundle.getString(FIRST_NAME_KEY) + " " +
                                bundle.getString(LAST_NAME_KEY), Toast.LENGTH_LONG);
                    }
                    else {
                        Log.d("Register", "About to send failure toast...");
                        Toast.makeText(currentContext, "Register failed", Toast.LENGTH_LONG);
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
            if (result == null) {
                sendMessage(new LoginResult(null, null, null,
                        "Null result", false));
                return;
            }
            Log.d("Login", String.format("Login result: %s, %s",
                    result.isSuccess()?"success":"failure", result.getMessage()));
            if (result.isSuccess()) {
                DataCache dataCache = DataCache.getInstance();
                dataCache.setUser(new User(result.getUsername(), loginData.password, loginData.email,
                        loginData.firstName, loginData.lastName, loginData.gender,
                        result.getPersonID()));
                dataCache.setAuthToken(new AuthToken(result.getAuthtoken(), result.getUsername()));
            }
            Log.d("Register", "About to send register message...");
            sendMessage(result);
        }

        private void sendMessage(LoginResult result) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            if (!result.isSuccess()) {
                messageBundle.putString(STATUS_KEY, result.isSuccess() ? "success" : "failure");
            }
            else {
                DataCache dataCache = DataCache.getInstance();
                messageBundle.putString(FIRST_NAME_KEY, dataCache.getUser().getFirstName());
                messageBundle.putString(LAST_NAME_KEY, dataCache.getUser().getLastName());
            }
            message.setData(messageBundle);
            handler.sendMessage(message);
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
            if (result == null) {
                sendMessage(new RegisterResult(null, null, null,
                        false, "Null result"));
                return;
            }
            Log.d("Register", String.format("Register result: %s, %s",
                    result.isSuccess()?"success":"failure", result.getMessage()));
            if (result.isSuccess()) {
                DataCache dataCache = DataCache.getInstance();
                dataCache.setUser(new User(result.getUsername(), loginData.password, loginData.email,
                        loginData.firstName, loginData.lastName, loginData.gender,
                        result.getPersonID()));
                dataCache.setAuthToken(new AuthToken(result.getAuthtoken(), result.getUsername()));
            }
            Log.d("Register", "About to send register message...");
            sendMessage(result);
        }

        private void sendMessage(RegisterResult result) {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            if (!result.isSuccess()) {
                messageBundle.putString(STATUS_KEY, result.isSuccess() ? "success" : "failure");
            }
            else {
                DataCache dataCache = DataCache.getInstance();
                messageBundle.putString(FIRST_NAME_KEY, dataCache.getUser().getFirstName());
                messageBundle.putString(LAST_NAME_KEY, dataCache.getUser().getLastName());
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
}