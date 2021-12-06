package net.jakehamzawi.familymap.model;

import java.util.Comparator;

import model.Person;

public class FamilyMember {
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

    public static class FamilyMemberComparator implements Comparator<FamilyMember> {
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

