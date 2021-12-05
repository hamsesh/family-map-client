package net.jakehamzawi.familymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener {

    private LoginFragment loginFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getSupportFragmentManager();

        // assigning ID of the toolbar to a variable
        Toolbar toolbar = findViewById(R.id.toolbar);

        // using toolbar as ActionBar
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            Log.d("Main", "Saved instance state is null");
            if (!loggedIn()) {
                Fragment fragment = createLoginFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.frameLayout, fragment, "mainFragment")
                        .commit();
            } else {
                Fragment fragment = new MapsFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.frameLayout, fragment, "mainFragment")
                        .commit();
            }
        }
    }



    private boolean loggedIn() {
        DataCache dataCache = DataCache.getInstance();
        return dataCache.getAuthToken() != null;
    }

    private Fragment createLoginFragment() {
        LoginFragment fragment = new LoginFragment();
        fragment.registerListener(this);
        return fragment;
    }

    @Override
    public void notifyDone() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = new MapsFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }
}