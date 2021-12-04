package net.jakehamzawi.familymap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.AuthToken;
import model.Event;
import model.User;
import request.LoginRequest;
import result.LoginResult;

public class MapsFragment extends Fragment {

    private static final String STATUS_KEY = "success";

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            Context currentContext = getContext();

            setMarkers(googleMap);

            /*
            Handler uiThreadHandler = new Handler() {
                @Override
                public void handleMessage(Message message) {
                    Bundle bundle = message.getData();
                    String status = bundle.getString(STATUS_KEY);
                    if (status == null || status.equals("failure")) {
                        Toast.makeText(currentContext, "Failed to get ancestral events!", Toast.LENGTH_LONG).show();
                        Log.d("Maps", "Failed to get ancestral events");
                    }
                }
            };

            LocationTask locationTask = new MapsFragment.LocationTask(uiThreadHandler, googleMap);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(locationTask);
             */
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.searchButton:
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                return true;
            case R.id.settingsButton:
                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
    private static class LocationTask implements Runnable {
        private final Handler handler;
        private final GoogleMap googleMap;

        public LocationTask(Handler handler, GoogleMap googleMap) {
            this.handler = handler;
            this.googleMap = googleMap;
        }

        @Override
        public void run() {
            Log.d("Maps", "Placing event locations...");
            DataCache dataCache = DataCache.getInstance();
            if (dataCache == null || dataCache.getEvents() == null) {
                Message message = handler.obtainMessage();
                Bundle messageBundle = new Bundle();
                messageBundle.putString(STATUS_KEY, "failure");
                message.setData(messageBundle);
                handler.sendMessage(message);
            }
            else {

                for (Event event : dataCache.getEvents()) {
                    Log.d("Maps", String.format("Adding %s event", event.getEventType()));
                    googleMap.addMarker(new MarkerOptions().position(new LatLng(event.getLatitude(), event.getLongitude())));
                }
            }
        }
    }
     */

    private void setMarkers(GoogleMap googleMap) {
        Log.d("Maps", "Placing event locations...");
        DataCache dataCache = DataCache.getInstance();
        for (Event event : dataCache.getEvents()) {
            float color = 0f;
            switch (event.getEventType()) {
                case ("birth"):
                    color = BitmapDescriptorFactory.HUE_AZURE;
                    break;
                case ("death"):
                    color = BitmapDescriptorFactory.HUE_YELLOW;
                    break;
                case ("marriage"):
                    color = BitmapDescriptorFactory.HUE_VIOLET;
                    break;
                case ("christening"):
                    color = BitmapDescriptorFactory.HUE_GREEN;
                    break;
                case ("baptism"):
                    color = BitmapDescriptorFactory.HUE_ROSE;
                    break;
            }
            Log.d("Maps", String.format("Adding %s event", event.getEventType()));
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(event.getLatitude(), event.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                    .title(event.getEventType()));
        }
    }
}