package com.vanipy.tumbuh;

import java.util.ArrayList;
import java.util.List;

/**
 * Made-up profile for showing tumbuh to friends ("Andi"). Every item below is
 * invented sample content bundled with the app; none of it is the owner's real
 * data. Demo mode is opt-in, stores its edits in a separate file
 * (tumbuh-demo.json) and never touches the real tumbuh.json.
 */
public final class DemoData {
    private DemoData() {}

    public static List<Item> items() {
        List<Item> out = new ArrayList<>();
        long now = System.currentTimeMillis();
        long day = 86_400_000L;
        out.add(make(1, Item.PEOPLE, "Reply to Rani about the workshop", 5, "Write two honest sentences", now - 6 * day));
        out.add(make(2, Item.COMMITMENTS, "Prepare slides for the community talk", 4, "Draft the three bullet points", now - 5 * day));
        out.add(make(3, Item.INBOX, "Answer the invoice question", 3, "Find the invoice number first", now - 5 * day));
        out.add(make(4, Item.INBOX, "Unsubscribe from unread newsletters", 1, "Set a 10-minute timer", now - 4 * day));
        out.add(make(5, Item.SPACE, "Clear the desk pile", 3, "Clear one dinner-plate-sized spot", now - 4 * day));
        out.add(make(6, Item.INBOX, "Reply to the event invitation", 2, "Check Saturday's calendar", now - 3 * day));
        out.add(make(7, Item.COMMITMENTS, "Renew the gym membership", 2, "Look up the renewal price", now - 3 * day));
        out.add(make(8, Item.PEOPLE, "Call grandmother", 2, "Pick a day this week to call", now - 2 * day));
        out.add(make(9, Item.SPACE, "Sort the cable drawer", 2, "Fill one bag to give away", now - 2 * day));
        out.add(make(10, Item.INBOX, "Archive last month's receipts", 1, "Archive, do not sort", now - day));
        out.add(make(11, Item.PEOPLE, "Thank Dewi for the notes", 1, "Send one short message", now - day));
        Item letgo = make(12, Item.COMMITMENTS, "Old language course", 4, "", now - 20 * day);
        letgo.status = Item.LETGO;
        letgo.updated = now - 12 * day;
        out.add(letgo);
        Item done = make(13, Item.SPACE, "Fix the bike light", 2, "", now - 15 * day);
        done.status = Item.DONE;
        done.updated = now - 8 * day;
        out.add(done);
        return out;
    }

    private static Item make(long id, int area, String title, int weight, String firstStep, long created) {
        Item it = new Item();
        it.id = id;
        it.area = area;
        it.title = title;
        it.weight = weight;
        it.firstStep = firstStep;
        it.created = created;
        it.updated = created;
        return it;
    }
}
