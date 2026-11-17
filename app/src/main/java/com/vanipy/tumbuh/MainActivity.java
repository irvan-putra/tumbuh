package com.vanipy.tumbuh;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int REQ_EXPORT = 11, REQ_IMPORT = 12;
    private static final int GREEN = 0xFF2E7D4F, BG = 0xFFF7F5EF, INK = 0xFF222222, MUTED = 0xFF666666, CARD = 0xFFFFFFFF;

    private List<Item> items = new ArrayList<>();
    private ScrollView scroll;
    private LinearLayout col;
    private int screen = 0;       // 0 home, 1 area, 2 item, 3 archive, 4 about
    private int currentArea = 0;
    private long currentItem = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        items = Storage.load(this);
        scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(18), dp(36), dp(18), dp(28));
        scroll.addView(col);
        setContentView(scroll);
        show();
    }

    // ---------- navigation ----------
    private void show() {
        col.removeAllViews();
        switch (screen) {
            case 1: areaScreen(); break;
            case 2: itemScreen(); break;
            case 3: archiveScreen(); break;
            case 4: aboutScreen(); break;
            default: homeScreen();
        }
        scroll.scrollTo(0, 0);
    }

    private void go(int s) { screen = s; show(); }

    @Override
    public void onBackPressed() {
        if (screen == 2) { Item it = find(currentItem); screen = (it != null && it.isArchived()) ? 3 : 1; show(); }
        else if (screen != 0) go(0);
        else super.onBackPressed();
    }

    // ---------- screens ----------
    private void homeScreen() {
        col.addView(title("Tumbuh"));
        col.addView(muted("Find your bearings. Everything stays on this phone."));

        Item start = Triage.startHere(items);
        if (start == null) {
            col.addView(card("Nothing here yet",
                "Add whatever is weighing on you, one line each. Rough is fine. You can sort it later.", null, null));
        } else {
            col.addView(card("Start here", start.title + "\n" + Item.AREA_NAMES[start.area]
                    + "  -  weight " + start.weight + "/5"
                    + (start.firstStep.isEmpty() ? "" : "\nFirst step: " + start.firstStep),
                "Open it", v -> { currentItem = start.id; go(2); }));
        }
        col.addView(spacer(8));
        col.addView(primary("+ Add something that is weighing on me", v -> addDialog(-1)));
        col.addView(spacer(8));
        for (int a = 0; a < 4; a++) {
            final int area = a;
            int n = Triage.countActive(items, a);
            col.addView(card(Item.AREA_NAMES[a] + "  (" + n + ")", Item.AREA_HINTS[a], "Open", v -> { currentArea = area; go(1); }));
        }
        col.addView(spacer(8));
        col.addView(secondary("Archive (let go or done): " + Triage.archived(items).size(), v -> go(3)));
        col.addView(secondary("Save a backup file", v -> exportBackup()));
        col.addView(secondary("Restore from a backup file", v -> importBackup()));
        col.addView(secondary("About and limits", v -> go(4)));
    }

    private void areaScreen() {
        col.addView(title(Item.AREA_NAMES[currentArea]));
        col.addView(muted(Item.AREA_HINTS[currentArea]));
        col.addView(spacer(8));
        col.addView(primary("+ Add to " + Item.AREA_NAMES[currentArea], v -> addDialog(currentArea)));
        List<Item> list = Triage.active(items, currentArea);
        if (list.isEmpty()) col.addView(muted("\nNothing open here."));
        for (Item it : list) col.addView(itemRow(it));
        col.addView(spacer(8));
        col.addView(secondary("Back", v -> go(0)));
    }

    private View itemRow(Item it) {
        return card(it.title, Item.statusLabel(it.status) + "  -  weight " + it.weight + "/5"
                + (it.firstStep.isEmpty() ? "" : "\nFirst step: " + it.firstStep),
            "Open", v -> { currentItem = it.id; go(2); });
    }

    private void archiveScreen() {
        col.addView(title("Archive"));
        col.addView(muted("Nothing is deleted. You can bring anything back."));
        List<Item> list = Triage.archived(items);
        if (list.isEmpty()) col.addView(muted("\nEmpty."));
        for (Item it : list) col.addView(itemRow(it));
        col.addView(spacer(8));
        col.addView(secondary("Back", v -> go(0)));
    }

    private void itemScreen() {
        final Item it = find(currentItem);
        if (it == null) { go(0); return; }
        col.addView(title(it.title));
        col.addView(muted(Item.AREA_NAMES[it.area] + "  -  " + Item.statusLabel(it.status) + "  -  weight " + it.weight + "/5"));
        if (!it.note.isEmpty()) col.addView(body("\n" + it.note));
        if (!it.firstStep.isEmpty()) col.addView(card("Your first step", it.firstStep, "Mark step done", v -> {
            note(it, "First step done: " + it.firstStep);
            it.firstStep = "";
            persist(); show();
        }));

        if (it.isArchived()) {
            col.addView(spacer(8));
            col.addView(primary("Bring it back", v -> { it.status = Item.OPEN; note(it, "Brought back"); persist(); show(); }));
        } else {
            col.addView(spacer(10));
            col.addView(section("What do you want to do with this?"));
            col.addView(choice("Keep it", "I will do it. Pick a small first step.", v -> decide(it, Item.KEEP)));
            col.addView(choice("Renegotiate it", "Change the deal. I get a draft to edit and send myself.", v -> decide(it, Item.RENEGOTIATE)));
            col.addView(choice("Ask for help", "I need someone. I get a draft to edit and send myself.", v -> decide(it, Item.HELP)));
            col.addView(choice("Let it go", "Archive it. It stays in the archive.", v -> {
                it.status = Item.LETGO; it.firstStep = ""; note(it, "Let go"); persist(); screen = 1; show(); }));
            col.addView(choice("It is done", "Finished. Archive it.", v -> {
                it.status = Item.DONE; it.firstStep = ""; note(it, "Done"); persist(); screen = 1; show(); }));
            col.addView(spacer(8));
            col.addView(secondary("Change the first step", v -> firstStepDialog(it)));
            col.addView(secondary("Draft a message to edit and send", v -> draftDialog(it, it.status)));
        }
        col.addView(secondary("Edit title, note or weight", v -> addDialog2(it)));
        if (!it.log.isEmpty()) {
            col.addView(spacer(10));
            col.addView(section("History"));
            SimpleDateFormat f = new SimpleDateFormat("d MMM HH:mm", Locale.getDefault());
            for (int i = it.log.size() - 1; i >= 0; i--) {
                String[] p = it.log.get(i).split("\\|", 2);
                String when = "";
                try { when = f.format(new Date(Long.parseLong(p[0]))); } catch (Exception e) { /* ignore */ }
                col.addView(muted(when + "  " + (p.length > 1 ? p[1] : "")));
            }
        }
        col.addView(spacer(8));
        col.addView(secondary("Back", v -> onBackPressed()));
    }

    private void aboutScreen() {
        col.addView(title("About Tumbuh " + BuildInfo.VERSION));
        col.addView(body("\nTumbuh helps you sort what is weighing on you: commitments, a full inbox, a messy space, and people you lost touch with. "
            + "Add things, decide what to keep, renegotiate, ask help for, or let go, and take one small step."));
        col.addView(section("\nWhat it does not do"));
        col.addView(body("- It does not read your email, messages, contacts or calendar. You add things by hand.\n"
            + "- It does not send anything. Drafts are text you copy or share yourself.\n"
            + "- It does not contact anyone, and it has no internet permission.\n"
            + "- It does not know how heavy something is unless you tell it. The weight is your own 1 to 5.\n"
            + "- It has no reminders yet, and no account or sync."));
        col.addView(section("\nYour data"));
        col.addView(body("Stored only in this app's private storage. Android app backup is switched off, "
            + "so use 'Save a backup file' to keep a copy. Restoring first saves a copy of what you have now. Uninstalling the app deletes the data."));
        col.addView(spacer(8));
        col.addView(secondary("Back", v -> go(0)));
    }

    // ---------- actions ----------
    private void decide(Item it, String decision) {
        it.status = decision;
        note(it, Item.statusLabel(decision));
        persist();
        firstStepDialog(it);
    }

    private void firstStepDialog(final Item it) {
        final String[] ideas = Triage.firstStepIdeas(it.area);
        final EditText input = edit("My own first step (a few minutes)", it.firstStep, false);
        LinearLayout box = vbox();
        box.addView(input);
        box.addView(muted("\nIdeas, tap to use:"));
        for (final String s : ideas) {
            Button b = flat(s);
            b.setOnClickListener(v -> input.setText(s));
            box.addView(b);
        }
        new AlertDialog.Builder(this).setTitle("One small first step").setView(wrapScroll(box))
            .setPositiveButton("Save", (d, w) -> {
                it.firstStep = input.getText().toString().trim();
                if (!it.firstStep.isEmpty()) note(it, "First step: " + it.firstStep);
                persist(); show();
                if (!Triage.draft(it, it.status).isEmpty()) draftDialog(it, it.status);
            })
            .setNegativeButton("Skip", (d, w) -> show()).show();
    }

    private void draftDialog(final Item it, String decision) {
        String d = Triage.draft(it, decision);
        if (d.isEmpty()) d = Triage.draft(it, Item.RENEGOTIATE);
        final EditText input = edit("Draft", d, true);
        new AlertDialog.Builder(this).setTitle("Draft for you to edit")
            .setMessage("Fill in the [brackets]. Tumbuh sends nothing; you choose how to send it.")
            .setView(wrapScroll(boxOf(input)))
            .setPositiveButton("Share...", (dd, w) -> {
                Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, input.getText().toString());
                startActivity(Intent.createChooser(send, "Send with"));
            })
            .setNeutralButton("Copy", (dd, w) -> {
                ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText("Tumbuh draft", input.getText().toString()));
                toast("Copied");
            })
            .setNegativeButton("Close", null).show();
    }

    /** area -1 means the person picks the area in the dialog. */
    private void addDialog(final int area) {
        final EditText t = edit("What is weighing on you?", "", false);
        final EditText n = edit("Note (optional)", "", true);
        final Spinner areaSp = new Spinner(this);
        areaSp.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Item.AREA_NAMES));
        if (area >= 0) areaSp.setSelection(area);
        final Spinner w = weightSpinner(3);
        LinearLayout box = vbox();
        box.addView(t); box.addView(n);
        box.addView(muted("\nArea")); box.addView(areaSp);
        box.addView(muted("\nHow heavy does it feel?")); box.addView(w);
        new AlertDialog.Builder(this).setTitle("Add").setView(wrapScroll(box))
            .setPositiveButton("Add", (d, x) -> {
                String title = t.getText().toString().trim();
                if (title.isEmpty()) { toast("Write a few words first"); return; }
                Item it = new Item();
                it.id = nextId(); it.area = areaSp.getSelectedItemPosition(); it.title = title;
                it.note = n.getText().toString().trim(); it.weight = w.getSelectedItemPosition() + 1;
                it.created = it.updated = System.currentTimeMillis();
                note(it, "Added");
                items.add(it);
                persist(); show();
            }).setNegativeButton("Cancel", null).show();
    }

    private void addDialog2(final Item it) {
        final EditText t = edit("Title", it.title, false);
        final EditText n = edit("Note", it.note, true);
        final Spinner w = weightSpinner(it.weight);
        LinearLayout box = vbox();
        box.addView(t); box.addView(n);
        box.addView(muted("\nHow heavy does it feel?")); box.addView(w);
        new AlertDialog.Builder(this).setTitle("Edit").setView(wrapScroll(box))
            .setPositiveButton("Save", (d, x) -> {
                String title = t.getText().toString().trim();
                if (!title.isEmpty()) it.title = title;
                it.note = n.getText().toString().trim();
                it.weight = w.getSelectedItemPosition() + 1;
                it.updated = System.currentTimeMillis();
                persist(); show();
            }).setNegativeButton("Cancel", null).show();
    }

    private void exportBackup() {
        String stamp = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(new Date());
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
            .setType("application/json")
            .putExtra(Intent.EXTRA_TITLE, "tumbuh-v" + BuildInfo.VERSION + "-backup-" + stamp + ".json");
        startActivityForResult(i, REQ_EXPORT);
    }

    private void importBackup() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*");
        startActivityForResult(i, REQ_IMPORT);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            if (req == REQ_EXPORT) {
                try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                    out.write(Codec.encode(items, BuildInfo.VERSION).getBytes(StandardCharsets.UTF_8));
                }
                toast("Backup saved");
            } else if (req == REQ_IMPORT) {
                String text;
                try (InputStream in = getContentResolver().openInputStream(uri)) { text = Storage.readAll(in); }
                final List<Item> loaded = Codec.decode(text);
                new AlertDialog.Builder(this).setTitle("Restore " + loaded.size() + " items?")
                    .setMessage("This replaces what is on the phone now. A copy of the current data is kept first.")
                    .setPositiveButton("Restore", (d, w) -> {
                        try {
                            Storage.keepCopy(this, items, "restore");
                            items = loaded;
                            Storage.save(this, items);
                            screen = 0; show(); toast("Restored");
                        } catch (IOException | JSONException e) { toast("Restore failed, nothing changed"); }
                    }).setNegativeButton("Cancel", null).show();
            }
        } catch (IOException | JSONException | RuntimeException e) {
            toast(req == REQ_IMPORT ? "That file is not a Tumbuh backup. Nothing changed." : "Could not save the backup");
        }
    }

    // ---------- data helpers ----------
    private Item find(long id) { for (Item it : items) if (it.id == id) return it; return null; }

    private long nextId() { long m = 0; for (Item it : items) m = Math.max(m, it.id); return m + 1; }

    private void note(Item it, String text) {
        it.log.add(System.currentTimeMillis() + "|" + text);
        it.updated = System.currentTimeMillis();
    }

    private void persist() {
        try { Storage.save(this, items); }
        catch (IOException | JSONException e) { toast("Could not save. Use 'Save a backup file' now."); }
    }

    // ---------- view helpers ----------
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private TextView text(String s, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }
    private TextView title(String s) { return text(s, 26, INK, true); }
    private TextView section(String s) { TextView t = text(s, 16, GREEN, true); t.setPadding(0, dp(6), 0, dp(4)); return t; }
    private TextView muted(String s) { return text(s, 14, MUTED, false); }
    private TextView body(String s) { return text(s, 16, INK, false); }
    private View spacer(int h) { View v = new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h))); return v; }

    private GradientDrawable round(int color) {
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(14)); return g;
    }

    private View card(String head, String text, String action, View.OnClickListener l) {
        LinearLayout c = vbox();
        c.setBackground(round(CARD));
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        c.setLayoutParams(lp);
        c.addView(text(head, 17, INK, true));
        c.addView(muted(text));
        if (action != null) {
            Button b = flat(action);
            b.setOnClickListener(l);
            c.addView(b);
        }
        return c;
    }

    private View choice(String head, String sub, View.OnClickListener l) {
        View c = card(head, sub, null, null);
        c.setOnClickListener(l);
        return c;
    }

    private Button primary(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackground(round(GREEN));
        b.setOnClickListener(l);
        b.setMinHeight(dp(52));
        return b;
    }
    private Button secondary(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s); b.setAllCaps(false); b.setTextColor(GREEN); b.setBackground(round(0xFFE6EFE9));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        b.setLayoutParams(lp);
        return b;
    }
    private Button flat(String s) {
        Button b = new Button(this);
        b.setText(s); b.setAllCaps(false); b.setTextColor(GREEN); b.setBackground(null);
        b.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        return b;
    }

    private LinearLayout vbox() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }
    private LinearLayout boxOf(View v) { LinearLayout l = vbox(); l.addView(v); return l; }
    private View wrapScroll(View v) {
        ScrollView s = new ScrollView(this);
        v.setPadding(dp(20), dp(8), dp(20), dp(8));
        s.addView(v);
        return s;
    }
    private EditText edit(String hint, String value, boolean multi) {
        EditText e = new EditText(this);
        e.setHint(hint); e.setText(value);
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            | (multi ? InputType.TYPE_TEXT_FLAG_MULTI_LINE : 0));
        return e;
    }
    private Spinner weightSpinner(int w) {
        Spinner s = new Spinner(this);
        s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
            new String[]{"1 - light", "2", "3 - medium", "4", "5 - very heavy"}));
        s.setSelection(Math.max(0, Math.min(4, w - 1)));
        return s;
    }
}
