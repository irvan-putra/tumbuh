package com.vanipy.tumbuh;

import java.util.ArrayList;
import java.util.List;

/** One loose end the person wants to look at. Plain data, no Android types. */
public final class Item {
    public static final int COMMITMENTS = 0, INBOX = 1, SPACE = 2, PEOPLE = 3;
    public static final String[] AREA_NAMES = {"Commitments", "Inbox", "Space", "People"};
    public static final String[] AREA_HINTS = {
        "Promises, dropped balls, things you said yes to",
        "Mail and messages piling up",
        "Rooms, desks, piles, things to put away",
        "People you lost touch with or owe a reply"
    };

    public static final String OPEN = "open";             // not decided yet
    public static final String KEEP = "keep";             // decided: I will do it
    public static final String RENEGOTIATE = "renegotiate"; // decided: change the deal
    public static final String HELP = "help";             // decided: I need someone's help
    public static final String LETGO = "letgo";           // archived: let go
    public static final String DONE = "done";             // archived: finished

    public long id;
    public int area;
    public String title = "";
    public String note = "";
    public int weight = 3;           // 1 light .. 5 heavy
    public String status = OPEN;
    public String firstStep = "";
    public long created;
    public long updated;
    public List<String> log = new ArrayList<>();   // "epochMillis|text", newest last

    public boolean isArchived() {
        return LETGO.equals(status) || DONE.equals(status);
    }

    public static String statusLabel(String s) {
        switch (s) {
            case KEEP: return "Keeping it";
            case RENEGOTIATE: return "Renegotiating";
            case HELP: return "Asking for help";
            case LETGO: return "Let go";
            case DONE: return "Done";
            default: return "Not decided";
        }
    }
}
