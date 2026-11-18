package com.vanipy.tumbuh;

import android.content.Context;

/** Opt-in switch that swaps the owner's real items for the bundled made-up profile. */
public final class DemoMode {
    private static final String PREFS = "tumbuh_prefs";
    private static final String KEY = "demo_mode";

    private DemoMode() {}

    public static boolean isOn(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, false);
    }

    /** Turning on seeds the separate demo file once; the real tumbuh.json is never read or written. */
    public static void set(Context c, boolean on) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, on).apply();
        if (on && Storage.load(c, Storage.DEMO_NAME).isEmpty()) {
            try { Storage.save(c, Storage.DEMO_NAME, DemoData.items()); }
            catch (Exception ignored) {
            }
        }
        TumbuhWidgetProvider.updateAll(c);
    }

    /** The store file for the current mode. */
    static String activeStore(Context c) {
        return isOn(c) ? Storage.DEMO_NAME : Storage.NAME;
    }
}
