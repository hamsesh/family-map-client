package net.jakehamzawi.familymap;

import net.jakehamzawi.familymap.data.DataCache;
import net.jakehamzawi.familymap.data.DataProcessor;
import net.jakehamzawi.familymap.ServerProxy;
import net.jakehamzawi.familymap.model.FamilyMember;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.AuthToken;
import model.Person;
import model.User;
import request.RegisterRequest;
import result.EventResult;
import result.PersonResult;
import result.RegisterResult;

public class RelationshipTest {
    private static final String HOST = "localhost";
    private static final String PORT = "8080";


    @BeforeClass
    public static void setup() {
        ServerProxy proxy = new ServerProxy();
        DataCache dataCache = DataCache.getInstance();


        RegisterRequest request = new RegisterRequest("test", "secret", "test@test.com",
                "Tester", "Testington", "m");
        RegisterResult result = proxy.register(request, HOST, PORT);
        dataCache.setUser(new User(result.getUsername(), "secret", "test@test.com",
                "Tester", "Testington", "m", result.getPersonID()));
        dataCache.setAuthToken(new AuthToken(result.getAuthtoken(), result.getUsername()));

        PersonResult personResult = proxy.persons(HOST, PORT);
        dataCache.setPersons(personResult.getData());

        EventResult eventResult = proxy.events(HOST, PORT);
        dataCache.setEvents(eventResult.getData());
    }

    @Test
    public void testCorrectFamily() {
        DataCache dataCache = DataCache.getInstance();
        HashMap<String, Person> personMap = dataCache.getPersonMap();
        Person rootPerson = personMap.get(dataCache.getUser().getPersonID());
        List<FamilyMember> familyMembers = DataProcessor.findFamily(rootPerson);

        int totalFamilyMembers = 0;
        assert rootPerson != null;
        if (rootPerson.getMotherID() != null) {
            assertEquals(rootPerson.getMotherID(),
                    getMember(familyMembers, "mother").getPerson().getPersonID());
            totalFamilyMembers++;
        }
        if (rootPerson.getFatherID() != null) {
            assertEquals(rootPerson.getFatherID(),
                    getMember(familyMembers, "father").getPerson().getPersonID());
            totalFamilyMembers++;
        }
        if (rootPerson.getSpouseID() != null) {
            assertEquals(rootPerson.getSpouseID(),
                    getMember(familyMembers, "spouse").getPerson().getPersonID());
            totalFamilyMembers++;
        }

        List<FamilyMember> children = getChildren(familyMembers);
        for (FamilyMember familyMember : children) {
            Person childOnMap = personMap.get(familyMember.getPerson().getPersonID());
            assertTrue(childOnMap.getFatherID().equals(rootPerson.getPersonID()) ||
                    childOnMap.getMotherID().equals(rootPerson.getPersonID()));
        }

        // Check if all children were found
        for (Person person : dataCache.getPersons()) {
            if (rootPerson.getPersonID().equals(person.getMotherID()) ||
                    rootPerson.getPersonID().equals(person.getFatherID())) {
                totalFamilyMembers++;
            }
        }

        assertEquals(totalFamilyMembers, familyMembers.size());
    }

    private FamilyMember getMember(List<FamilyMember> familyMembers, String relation) {
        for (FamilyMember familyMember : familyMembers) {
            if (familyMember.getRelation().equalsIgnoreCase(relation)) {
                return familyMember;
            }
        }
        return null;
    }

    private List<FamilyMember> getChildren(List<FamilyMember> familyMembers) {
        List<FamilyMember> children = new ArrayList<>();
        for (FamilyMember familyMember : familyMembers) {
            if (familyMember.getRelation().equalsIgnoreCase("child")) {
                children.add(familyMember);
            }
        }
        return children;
    }

    @AfterClass
    public static void cleanUp() {
        ServerProxy proxy = new ServerProxy();
        proxy.clear(HOST, PORT);
    }
}
