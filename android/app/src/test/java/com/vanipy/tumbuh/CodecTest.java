package com.vanipy.tumbuh;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.junit.Test;

public class CodecTest {
    private Item item(long id, int area, String title, int weight, long created) {
        Item it = new Item();
        it.id = id; it.area = area; it.title = title; it.weight = weight; it.created = created; it.updated = created;
        return it;
    }

    @Test public void roundTripKeepsEverything() throws Exception {
        List<Item> in = new ArrayList<>();
        Item a = item(1, Item.PEOPLE, "Call Sam \"soon\"\nnewline", 4, 100);
        a.status = Item.KEEP; a.firstStep = "Send one message"; a.note = "n"; a.log.add("5|decided");
        in.add(a);
        in.add(item(2, Item.SPACE, "Desk", 2, 200));
        List<Item> out = Codec.decode(Codec.encode(in, "0.1.0"));
        assertEquals(2, out.size());
        assertEquals(a.title, out.get(0).title);
        assertEquals(Item.KEEP, out.get(0).status);
        assertEquals("5|decided", out.get(0).log.get(0));
        assertEquals(Item.SPACE, out.get(1).area);
    }

    @Test(expected = JSONException.class) public void rejectsOtherJson() throws Exception {
        Codec.decode("{\"items\":[]}");
    }

    @Test(expected = JSONException.class) public void rejectsGarbage() throws Exception {
        Codec.decode("not json");
    }

    @Test public void startHerePicksHeaviestThenOldest() {
        List<Item> l = new ArrayList<>();
        l.add(item(1, 0, "a", 3, 10));
        l.add(item(2, 1, "b", 5, 30));
        l.add(item(3, 2, "c", 5, 20));
        assertEquals(3, Triage.startHere(l).id);
    }

    @Test public void startHereSkipsArchivedAndHandlesEmpty() {
        List<Item> l = new ArrayList<>();
        assertNull(Triage.startHere(l));
        Item a = item(1, 0, "a", 5, 10); a.status = Item.LETGO;
        l.add(a);
        assertNull(Triage.startHere(l));
        l.add(item(2, 0, "b", 1, 10));
        assertEquals(2, Triage.startHere(l).id);
        assertEquals(1, Triage.archived(l).size());
    }

    @Test public void draftsAreEditableTemplatesAndOnlyWhereSensible() {
        Item p = item(1, Item.PEOPLE, "Maya's message", 3, 1);
        assertTrue(Triage.draft(p, Item.RENEGOTIATE).contains("[day]"));
        assertTrue(Triage.draft(p, Item.KEEP).length() > 10);
        Item s = item(2, Item.SPACE, "Desk", 3, 1);
        assertEquals("", Triage.draft(s, Item.KEEP));
        assertEquals("", Triage.draft(s, Item.LETGO));
    }

    @Test public void everyAreaHasFirstSteps() {
        for (int a = 0; a < 4; a++) assertEquals(3, Triage.firstStepIdeas(a).length);
    }
}
