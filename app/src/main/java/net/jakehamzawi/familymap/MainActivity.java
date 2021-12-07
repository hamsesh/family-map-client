package net.jakehamzawi.familymap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements LoginFragment.Listener {

    private SharedPreferences tempPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            if (!loggedIn()) {
                Fragment fragment = createLoginFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment, "main_fragment")
                        .commit();
            } else {
                Fragment fragment = new MapsFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment, "main_fragment")
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
                .replace(R.id.frame_layout, fragment)
                .commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        copyPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tempPrefs == null) {
            this.tempPrefs = getSharedPreferences("tempPrefs", MODE_PRIVATE);
            return;
        }
        if (!preferencesEqual(tempPrefs, PreferenceManager.getDefaultSharedPreferences(this))) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, new MapsFragment())
                    .commit();
        }
    }

    private void copyPreferences() {
        SharedPreferences.Editor ed = tempPrefs.edit();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        ed.clear();
        for(Map.Entry<String,?> entry : sp.getAll().entrySet()){
            Object v = entry.getValue();
            String key = entry.getKey();
            if(v instanceof Boolean)
                ed.putBoolean(key, (Boolean) v);
            else if(v instanceof Float)
                ed.putFloat(key, (Float) v);
            else if(v instanceof Integer)
                ed.putInt(key, (Integer) v);
            else if(v instanceof Long)
                ed.putLong(key, (Long) v);
            else if(v instanceof String)
                ed.putString(key, ((String)v));
        }
        ed.apply();
    }

    private boolean preferencesEqual(SharedPreferences prefs1, SharedPreferences prefs2) {
        Map<String, ?> prefMap1 = prefs1.getAll();
        Map<String, ?> prefMap2 = prefs2.getAll();
        for (Map.Entry<String, ?> entry : prefMap1.entrySet()) {
            if (entry.getValue() != prefMap2.get(entry.getKey())) {
                return false;
            }
        }
        return true;
    }
}