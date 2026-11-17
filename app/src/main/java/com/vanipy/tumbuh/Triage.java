package com.vanipy.tumbuh;

import java.util.ArrayList;
import java.util.List;

/** The non-visual rules: what to look at first, first-step ideas, and drafts the person may send themself. */
public final class Triage {
    private Triage() {}

    /** The heaviest active item; ties go to the oldest. Null when nothing is active. */
    public static Item startHere(List<Item> items) {
        Item best = null;
        for (Item it : items) {
            if (it.isArchived()) continue;
            if (best == null || it.weight > best.weight
                    || (it.weight == best.weight && it.created < best.created)) best = it;
        }
        return best;
    }

    public static List<Item> active(List<Item> items, int area) {
        List<Item> out = new ArrayList<>();
        for (Item it : items) if (it.area == area && !it.isArchived()) out.add(it);
        out.sort((a, b) -> a.weight != b.weight ? b.weight - a.weight : Long.compare(a.created, b.created));
        return out;
    }

    public static List<Item> archived(List<Item> items) {
        List<Item> out = new ArrayList<>();
        for (Item it : items) if (it.isArchived()) out.add(it);
        out.sort((a, b) -> Long.compare(b.updated, a.updated));
        return out;
    }

    public static int countActive(List<Item> items, int area) {
        return active(items, area).size();
    }

    /** Small first steps, a few minutes each. Suggestions only; the person picks or writes their own. */
    public static String[] firstStepIdeas(int area) {
        switch (area) {
            case Item.COMMITMENTS:
                return new String[]{
                    "Write one sentence: what did I promise, to whom, and by when?",
                    "Decide the smallest version of this I could still honestly do.",
                    "Put a 15-minute slot for it in my calendar this week."};
            case Item.INBOX:
                return new String[]{
                    "Set a 10-minute timer and only archive things that need no reply.",
                    "Find the three oldest messages that need a reply and answer one.",
                    "Unsubscribe from the first three senders I never read."};
            case Item.SPACE:
                return new String[]{
                    "Clear one surface the size of a dinner plate.",
                    "Fill one bag with things to throw away or give away.",
                    "Put everything that belongs elsewhere into one box and label it."};
            default:
                return new String[]{
                    "Send one short message: thinking of you, how are you doing?",
                    "Write down what I would want to say to them, in one line.",
                    "Pick a day this week to call or meet them."};
        }
    }

    /**
     * A draft the person can edit and send themself. Tumbuh never sends anything.
     * Returns "" when there is no sensible draft for this decision.
     */
    public static String draft(Item it, String decision) {
        String what = it.title.trim();
        if (Item.RENEGOTIATE.equals(decision)) {
            if (it.area == Item.PEOPLE) {
                return "Hi, I owe you a proper reply about " + what + " and I let it slide. I'm sorry. "
                        + "Can I get back to you properly by [day]?";
            }
            return "Hi, about " + what + ": I said I would do it and I have fallen behind. "
                    + "I would rather be honest than keep it hanging. I can [smaller version] by [day], "
                    + "or we can drop it. Which works better for you?";
        }
        if (Item.HELP.equals(decision)) {
            return "Hi, I am stuck with " + what + ". Could you help me with [specific part] for about [time]? "
                    + "No problem if not.";
        }
        if (it.area == Item.PEOPLE && Item.KEEP.equals(decision)) {
            return "Hi! It has been a while and I was thinking of you. How have you been?";
        }
        return "";
    }
}
