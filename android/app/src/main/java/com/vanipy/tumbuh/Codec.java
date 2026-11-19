package com.vanipy.tumbuh;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Reads and writes the backup/storage JSON. The same format is used for export and import. */
public final class Codec {
    public static final String FORMAT = "tumbuh-backup";
    public static final int VERSION = 1;

    private Codec() {}

    public static String encode(List<Item> items, String appVersion) throws JSONException {
        JSONObject root = new JSONObject();
        root.put("format", FORMAT);
        root.put("version", VERSION);
        root.put("appVersion", appVersion);
        JSONArray arr = new JSONArray();
        for (Item it : items) {
            JSONObject o = new JSONObject();
            o.put("id", it.id);
            o.put("area", it.area);
            o.put("title", it.title);
            o.put("note", it.note);
            o.put("weight", it.weight);
            o.put("status", it.status);
            o.put("firstStep", it.firstStep);
            o.put("created", it.created);
            o.put("updated", it.updated);
            JSONArray log = new JSONArray();
            for (String l : it.log) log.put(l);
            o.put("log", log);
            arr.put(o);
        }
        root.put("items", arr);
        return root.toString(2);
    }

    /** Throws JSONException for anything that is not a tumbuh backup. Never returns partial data. */
    public static List<Item> decode(String text) throws JSONException {
        JSONObject root = new JSONObject(text);
        if (!FORMAT.equals(root.optString("format"))) throw new JSONException("Not a tumbuh backup");
        if (root.optInt("version", 0) > VERSION) throw new JSONException("Backup is from a newer tumbuh");
        JSONArray arr = root.getJSONArray("items");
        List<Item> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Item it = new Item();
            it.id = o.getLong("id");
            it.area = Math.max(0, Math.min(3, o.optInt("area", 0)));
            it.title = o.optString("title", "");
            it.note = o.optString("note", "");
            it.weight = Math.max(1, Math.min(5, o.optInt("weight", 3)));
            it.status = o.optString("status", Item.OPEN);
            it.firstStep = o.optString("firstStep", "");
            it.created = o.optLong("created", 0);
            it.updated = o.optLong("updated", 0);
            JSONArray log = o.optJSONArray("log");
            if (log != null) for (int j = 0; j < log.length(); j++) it.log.add(log.getString(j));
            out.add(it);
        }
        return out;
    }
}
