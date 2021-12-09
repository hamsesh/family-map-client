package net.jakehamzawi.familymap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.jakehamzawi.familymap.data.DataCache;
import net.jakehamzawi.familymap.data.DataProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Event;
import model.Person;

public class MapsFragment extends Fragment {

    private static final String EVENT_KEY = "eventID";
    private static final String PERSON_KEY = "personID";
    private static final String SUCCESS_KEY = "success";
    private static final String SPOUSE_KEY = "spouse";
    private static final String FAMILY_KEY = "family";
    private static final String LIFE_KEY = "life";
    private Person[] filteredPersons = null;
    private Event[] filteredEvents = null;
    ArrayList<LatLng> spouseLine = null;
    ArrayList<ArrayList<LatLng>> lifeLines = null;
    ArrayList<FamilyLine> familyLines = null;
    ArrayList<Polyline> polylines = new ArrayList<>();
    protected HashMap<Marker, Event> eventsOnMap = new HashMap<>();

    private Event selectedEvent = null;
    private Person selectedPerson = null;

    private final OnMapReadyCallback callback = googleMap -> {
        FilterHandler uiThreadHandler = new FilterHandler(this, googleMap);

        FilterTask filterTask = new FilterTask(uiThreadHandler, getContext());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(filterTask);

        googleMap.setOnMarkerClickListener(marker -> {
            selectedEvent = eventsOnMap.get(marker);
            assert selectedEvent != null;

            DataCache dataCache = DataCache.getInstance();
            selectedPerson = dataCache.getPersonByID(selectedEvent.getPersonID());
            assert selectedPerson != null;

            setInfoBar();

            LineHandler lineHandler = new LineHandler(this, googleMap);

            LineTask lineTask = new LineTask(lineHandler, getContext());
            executor.submit(lineTask);
            return false;
        });

        Bundle bundle = getArguments();
        if (bundle != null) {
            DataCache dataCache = DataCache.getInstance();
            selectedEvent = dataCache.getEventByID(bundle.getString(EVENT_KEY));
            selectedPerson = dataCache.getPersonByID(selectedEvent.getPersonID());
            setInfoBar();
        }

        if (selectedEvent != null) {
            LatLng location = new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 5f));

            LineHandler lineHandler = new LineHandler(this, googleMap);

            LineTask lineTask = new LineTask(lineHandler, getContext());
            executor.submit(lineTask);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (requireActivity().getClass() == MainActivity.class) {
            inflater.inflate(R.menu.menu_main_map, menu);
        }
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

    private void setInfoBar() {
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

    private static class FilterHandler extends Handler {
        private final MapsFragment fragment;
        private final GoogleMap googleMap;

        private FilterHandler(MapsFragment fragment, GoogleMap googleMap) {
            this.fragment = fragment;
            this.googleMap = googleMap;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d("Maps", "Placing event locations...");
            DataCache dataCache = DataCache.getInstance();
            fragment.eventsOnMap.clear();
            Bundle bundle = msg.getData();
            boolean success = bundle.getBoolean(SUCCESS_KEY);
            if (success) {
                for (Event event : fragment.filteredEvents) {
                    Log.d("Maps", String.format("Adding %s event", event.getEventType()));
                    fragment.eventsOnMap.put(googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(event.getLatitude(), event.getLongitude()))
                            .icon(BitmapDescriptorFactory.defaultMarker(dataCache.getColor(event.getEventType())))), event);
                }
            }
            else {
                Toast.makeText(fragment.getContext(), "Failed to add markers to map", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class FilterTask implements Runnable {
        private final FilterHandler handler;
        private final Context context;

        protected FilterTask(FilterHandler handler, Context context) {
            this.handler = handler;
            this.context = context;
        }

        @Override
        public void run() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            DataCache dataCache = DataCache.getFilteredInstance(prefs);
            filteredEvents = dataCache.getEvents();
            filteredPersons = dataCache.getFilteredPersons();
            sendMessage();
        }

        private void sendMessage() {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            boolean success = filteredEvents != null;
            messageBundle.putBoolean(SUCCESS_KEY, success);
            message.setData(messageBundle);
            handler.sendMessage(message);
        }
    }

    private static class LineHandler extends Handler {
        private final MapsFragment fragment;
        private final GoogleMap googleMap;

        private LineHandler(MapsFragment fragment, GoogleMap googleMap) {
            this.fragment = fragment;
            this.googleMap = googleMap;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d("Maps", "Placing marker lines...");
            clearPolylines();
            Bundle bundle = msg.getData();

            float baseWeight = 10f;

            boolean spouseLineValid = bundle.getBoolean(SPOUSE_KEY);
            boolean lifeLinesValid = bundle.getBoolean(LIFE_KEY);
            boolean familyLinesValid = bundle.getBoolean(FAMILY_KEY);
            if (spouseLineValid) {
                this.fragment.polylines.add(
                        googleMap.addPolyline(new PolylineOptions()
                                .color(Color.BLUE)
                                .addAll(fragment.spouseLine)));
            }
            if (lifeLinesValid) {
                for (ArrayList<LatLng> points : fragment.lifeLines) {
                    this.fragment.polylines.add(
                            googleMap.addPolyline(new PolylineOptions()
                                    .color(Color.GREEN)
                                    .addAll(points)));
                }
            }
            if (familyLinesValid) {
                for (FamilyLine familyLine : fragment.familyLines) {
                    float weight = baseWeight - familyLine.getGeneration() * 1.5f;
                    if (weight < 1f) weight = 1f;
                    this.fragment.polylines.add(
                            googleMap.addPolyline(new PolylineOptions()
                                    .color(Color.RED)
                                    .width(weight)
                                    .addAll(familyLine.getPoints())));
                }
            }

        }

        private void clearPolylines() {
            for (Polyline polyline : this.fragment.polylines) {
                polyline.remove();
            }
            this.fragment.polylines.clear();
        }
    }

    private class LineTask implements Runnable {
        private final LineHandler handler;
        private final Context context;

        protected LineTask(LineHandler handler, Context context) {
            this.handler = handler;
            this.context = context;
        }

        @Override
        public void run() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            spouseLine = null;
            lifeLines = null;
            familyLines = null;
            if (prefs.getBoolean("spouse_lines", false)) {
                spouseLine = findSpouseLine();
            }
            if (prefs.getBoolean("life_lines", false)) {
                lifeLines = findLifeLines();
            }
            if (prefs.getBoolean("family_lines", false)) {
                familyLines = findFamilyLines();
            }
            sendMessage();
        }

        private void sendMessage() {
            Message message = Message.obtain();
            Bundle messageBundle = new Bundle();
            messageBundle.putBoolean(SPOUSE_KEY, spouseLine != null);
            messageBundle.putBoolean(LIFE_KEY, lifeLines != null);
            messageBundle.putBoolean(FAMILY_KEY, familyLines != null);
            message.setData(messageBundle);
            handler.sendMessage(message);
        }

        private ArrayList<LatLng> findSpouseLine() {
            Event cachedEvent = null;
            for (Event event : filteredEvents) {
                if (event.getPersonID().equals(selectedPerson.getSpouseID())) {
                    if (event.getEventType().equalsIgnoreCase("birth")) {
                        ArrayList<LatLng> points = new ArrayList<>();
                        points.add(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude()));
                        points.add(new LatLng(event.getLatitude(), event.getLongitude()));
                        return points;
                    }
                    else {
                        if (cachedEvent == null || event.compareTo(cachedEvent) < 0) {
                            cachedEvent = event;
                        }
                    }
                }
            }
            if (cachedEvent == null) return null;
            else {
                ArrayList<LatLng> points = new ArrayList<>();
                points.add(new LatLng(selectedEvent.getLatitude(), selectedEvent.getLongitude()));
                points.add(new LatLng(cachedEvent.getLatitude(), cachedEvent.getLongitude()));
                return points;
            }
        }

        private ArrayList<ArrayList<LatLng>> findLifeLines() {
            Set<Event> storyEvents = new TreeSet<>();
            ArrayList<ArrayList<LatLng>> lifeLines = null;
            for (Event event : filteredEvents) {
                if (event.getPersonID().equals(selectedPerson.getPersonID())) {
                    storyEvents.add(event);
                }
            }
            Iterator<Event> iter = storyEvents.iterator();
            Event nextEvent = iter.next();
            for (Event event : storyEvents) {
                if (!iter.hasNext()) break;
                nextEvent = iter.next();
                if (lifeLines == null) {
                    lifeLines = new ArrayList<>();
                }
                ArrayList<LatLng> points = new ArrayList<>();
                points.add(new LatLng(event.getLatitude(), event.getLongitude()));
                points.add(new LatLng(nextEvent.getLatitude(), nextEvent.getLongitude()));
                lifeLines.add(points);
            }
            return lifeLines;
        }

        private ArrayList<FamilyLine> findFamilyLines() {
            HashMap<String, Person> personMap = DataProcessor.generatePersonMap(filteredPersons);
            HashMap<String, TreeSet<Event>> eventsByPerson = DataProcessor.generateSortedEventMap(filteredEvents, personMap);
            ArrayList<FamilyLine> familyLines = new ArrayList<>();

            addLineage(familyLines, personMap, eventsByPerson, selectedPerson, 0);
            if (familyLines.isEmpty()) return null;
            return familyLines;
        }

        private void addLineage(ArrayList<FamilyLine> familyLines,
                                HashMap<String, Person> personMap,
                                HashMap<String, TreeSet<Event>> eventsByPerson,
                                Person person,
                                int generation) {
            if (person == null) return;
            Person mother = personMap.get(person.getMotherID());
            Person father = personMap.get(person.getFatherID());
            addLineage(familyLines, personMap, eventsByPerson, mother, generation + 1);
            addLineage(familyLines, personMap, eventsByPerson, father, generation + 1);
            ArrayList<LatLng> points = new ArrayList<>();
            Event personEvent;

            // Get selected event, not first event of selectedPerson
            if (person == selectedPerson) {
                personEvent = selectedEvent;
            }
            else {
                personEvent = getFirstEvent(person.getPersonID(), eventsByPerson);
            }
            if (mother != null) {
                Event firstEventMother = getFirstEvent(mother.getPersonID(), eventsByPerson);
                points.add(new LatLng(personEvent.getLatitude(), personEvent.getLongitude()));
                points.add(new LatLng(firstEventMother.getLatitude(), firstEventMother.getLongitude()));
                familyLines.add(new FamilyLine(generation, points));
            }
            if (father != null) {
                Event firstEventFather = getFirstEvent(father.getPersonID(), eventsByPerson);
                points.add(new LatLng(personEvent.getLatitude(), personEvent.getLongitude()));
                points.add(new LatLng(firstEventFather.getLatitude(), firstEventFather.getLongitude()));
                familyLines.add(new FamilyLine(generation, points));
            }
        }

        private Event getFirstEvent(String personID, HashMap<String, TreeSet<Event>> eventsByPerson) {
            TreeSet<Event> orderedEvents = eventsByPerson.get(personID);
            assert orderedEvents != null;
            return orderedEvents.iterator().next();
        }
    }

    protected static class FamilyLine {
        private final int generation;
        private final ArrayList<LatLng> points;

        public FamilyLine(int generation, ArrayList<LatLng> points) {
            this.generation = generation;
            this.points = points;
        }

        public int getGeneration() {
            return generation;
        }

        public ArrayList<LatLng> getPoints() {
            return points;
        }
    }
}