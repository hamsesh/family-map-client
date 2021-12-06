package net.jakehamzawi.familymap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Locale;

import model.Event;
import model.Person;

public class MapsFragment extends Fragment {

    private static final String EVENT_KEY = "eventID";
    private static final String PERSON_KEY = "personID";
    private final HashMap<Marker, Event> eventsOnMap = new HashMap<>();
    private static final float[] MARKER_COLORS = { BitmapDescriptorFactory.HUE_AZURE,
                                                  BitmapDescriptorFactory.HUE_YELLOW,
                                                  BitmapDescriptorFactory.HUE_GREEN,
                                                  BitmapDescriptorFactory.HUE_RED,
                                                  BitmapDescriptorFactory.HUE_ROSE,
                                                  BitmapDescriptorFactory.HUE_ORANGE,
                                                  BitmapDescriptorFactory.HUE_VIOLET,
                                                  BitmapDescriptorFactory.HUE_MAGENTA };
    private Event selectedEvent = null;
    private Person selectedPerson = null;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            if (selectedEvent != null) {
                LatLng location = new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude());
                googleMap.addMarker(new MarkerOptions().position(location));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            }
            setMarkers(googleMap);

            googleMap.setOnMarkerClickListener(marker -> {
                selectedEvent = eventsOnMap.get(marker);
                assert selectedEvent != null;

                DataCache dataCache = DataCache.getInstance();
                selectedPerson = dataCache.getPersonByID(selectedEvent.getPersonID());
                assert selectedPerson != null;

                setInfo();
                return false;
            });

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


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(EVENT_KEY, selectedEvent == null ? null : selectedEvent.getEventID());
        outState.putString(PERSON_KEY, selectedPerson == null ? null : selectedPerson.getPersonID());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            String eventID = savedInstanceState.getString(EVENT_KEY);
            String personID = savedInstanceState.getString(PERSON_KEY);
            if (eventID != null && personID != null) {
                DataCache dataCache = DataCache.getInstance();
                selectedEvent = dataCache.getEventByID(eventID);
                selectedPerson = dataCache.getPersonByID(personID);
                setInfo();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        ImageView personImage = view.findViewById(R.id.personImage);
        personImage.setImageResource(R.drawable.default_person);

        TextView infoText = view.findViewById(R.id.infoText);
        infoText.setText(R.string.default_info);
        return view;
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
        HashMap<String, Float> eventTypeColors = new HashMap<>();
        int i = 0;
        for (Event event : dataCache.getEvents()) {
            if (i == MARKER_COLORS.length) i = 0; // Reset colors if reached limit
            if (!eventTypeColors.containsKey(event.getEventType())) {
                eventTypeColors.put(event.getEventType(), MARKER_COLORS[i]);
            }
            Log.d("Maps", String.format("Adding %s event", event.getEventType()));
            eventsOnMap.put(googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(event.getLatitude(), event.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(eventTypeColors.get(event.getEventType())))
                    .title(event.getEventType())), event);
            i++;
        }
    }

    private void setInfo() {
        View view = getView();
        assert view != null;
        TextView infoText = view.findViewById(R.id.infoText);
        ImageView personImage = view.findViewById(R.id.personImage);

        personImage.setImageResource(selectedPerson.getGender().equals("f") ? R.drawable.female :
                R.drawable.male);
        infoText.setText(getResources().getString(R.string.info_bar_text,
                getResources().getString(R.string.person_name, selectedPerson.getFirstName(),
                        selectedPerson.getLastName()), getResources().getString(R.string.event_info,
                        selectedEvent.getEventType().toUpperCase(Locale.ROOT), selectedEvent.getCity(),
                        selectedEvent.getCountry(), selectedEvent.getYear())));

        LinearLayout infoBar = view.findViewById(R.id.infoBar);
        infoBar.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PersonActivity.class);
            intent.putExtra(PERSON_KEY, selectedPerson.getPersonID());
            startActivity(intent);
        });
    }
}