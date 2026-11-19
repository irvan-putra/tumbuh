package com.vanipy.tumbuh;

import android.content.Context;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Private on-device storage. Writes are atomic and the previous version is kept as .bak. */
final class Storage {
    static final String NAME = "tumbuh.json";
    static final String DEMO_NAME = "tumbuh-demo.json";

    private Storage() {}

    static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    static List<Item> load(Context c) {
        return load(c, NAME);
    }

    static List<Item> load(Context c, String name) {
        File f = new File(c.getFilesDir(), name);
        File bak = new File(c.getFilesDir(), name + ".bak");
        for (File candidate : new File[]{f, bak}) {
            if (!candidate.exists()) continue;
            try (FileInputStream in = new FileInputStream(candidate)) {
                return Codec.decode(readAll(in));
            } catch (IOException | JSONException e) {
                // fall through to the backup copy
            }
        }
        return new ArrayList<>();
    }

    static void save(Context c, List<Item> items) throws IOException, JSONException {
        save(c, NAME, items);
    }

    static void save(Context c, String name, List<Item> items) throws IOException, JSONException {
        File dir = c.getFilesDir();
        File f = new File(dir, name);
        File tmp = new File(dir, name + ".tmp");
        File bak = new File(dir, name + ".bak");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(Codec.encode(items, BuildInfo.VERSION).getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }
        if (f.exists()) {
            if (bak.exists()) bak.delete();
            f.renameTo(bak);
        }
        if (!tmp.renameTo(f)) throw new IOException("Could not save");
    }

    /** Keeps a dated copy of the current data before an import replaces it. */
    static void keepCopy(Context c, List<Item> items, String tag) throws IOException, JSONException {
        File f = new File(c.getFilesDir(), "before-" + tag + "-" + System.currentTimeMillis() + ".json");
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(Codec.encode(items, BuildInfo.VERSION).getBytes(StandardCharsets.UTF_8));
        }
    }
}
