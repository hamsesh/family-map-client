package net.jakehamzawi.familymap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import model.Event;
import model.Person;

public class PersonExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final String[] expandableListTitle;
    private final ArrayList<Event> events;
    private final ArrayList<FamilyMember> familyMembers;
    private final String firstName;
    private final String lastName;

    public PersonExpandableListAdapter(Context context, String[] expandableListTitle,
                                       ArrayList<Event> events, HashMap<String, ArrayList<Person>> family,
                                       String firstName, String lastName) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.events = events;
        this.firstName = firstName;
        this.lastName = lastName;

        this.familyMembers = new ArrayList<>();
        for (Map.Entry<String, ArrayList<Person>> entry : family.entrySet()) {
            for (Person person : entry.getValue()) {
                this.familyMembers.add(new FamilyMember(entry.getKey(), person));
            }
        }
        familyMembers.sort(new FamilyMemberComparator());
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        if (this.expandableListTitle[listPosition].equals("LIFE EVENTS")) { //FIXME: use resource
            return events.get(expandedListPosition);
        }
        else {
            return familyMembers.get(expandedListPosition);
        }
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final Object child = getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.event_info_row, null);
        }
        if (child.getClass() == Event.class) {
            writeEvent(convertView, (Event) child);
        }
        else {
            writeFamilyMember(convertView, (FamilyMember) child);
        }


        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        if (this.expandableListTitle[listPosition].equals("LIFE EVENTS")) {
            return this.events.size();
        }
        else {
            return familyMembers.size();
        }
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle[listPosition];
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.length;
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.event_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.event_group_title);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

    private void writeEvent(View view, Event event) {
        Resources res = view.getResources();

        TextView mainText = (TextView) view.findViewById(R.id.main_info);
        String infoText = String.format(Locale.ROOT, "%s: %s, %s (%d)",
                event.getEventType().toUpperCase(Locale.ROOT), event.getCity(),
                event.getCountry(), event.getYear());
        mainText.setText(infoText);

        TextView subText = (TextView) view.findViewById(R.id.sub_info);
        subText.setText(res.getString(R.string.person_name, firstName, lastName));
    }

    private void writeFamilyMember(View view, FamilyMember familyMember) {
        TextView mainText = (TextView) view.findViewById(R.id.main_info);
        String infoText = String.format(Locale.ROOT, "%s %s", familyMember.person.getFirstName(),
                familyMember.person.getLastName());
        mainText.setText(infoText);

        TextView subText = (TextView) view.findViewById(R.id.sub_info);
        subText.setText(familyMember.relation);
    }

    private static class FamilyMember {
        private final String relation;
        private final Person person;

        public FamilyMember(String relation, Person person) {
            this.relation = relation;
            this.person = person;
        }

        public String getRelation() {
            return relation;
        }

        public Person getPerson() {
            return person;
        }
    }

    static class FamilyMemberComparator implements Comparator<FamilyMember> {
        @Override
        public int compare(FamilyMember o1, FamilyMember o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1.relation.equals("Father") ||
                    o1.relation.equals("Mother") && !o2.relation.equals("Father") ||
                    o1.relation.equals("Spouse") && (!o2.relation.equals("Father") &&
                            !o2.relation.equals("Mother"))) {
                return -1;
            }
            else {
                return 1;
            }
        }
    }
}