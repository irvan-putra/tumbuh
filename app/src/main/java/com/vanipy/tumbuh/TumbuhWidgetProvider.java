package com.vanipy.tumbuh;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import java.util.List;

/** Home-screen widget: one small step plus open counts per area. Tap opens tumbuh. */
public class TumbuhWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {
        for (int id : appWidgetIds) mgr.updateAppWidget(id, buildViews(context));
    }

    public static void updateAll(Context context) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, TumbuhWidgetProvider.class));
        for (int id : ids) mgr.updateAppWidget(id, buildViews(context));
    }

    static RemoteViews buildViews(Context c) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_tumbuh);
        boolean demo = DemoMode.isOn(c);
        v.setViewVisibility(R.id.widget_demo_badge, demo ? View.VISIBLE : View.GONE);

        List<Item> items = Storage.load(c, DemoMode.activeStore(c));
        Item start = Triage.startHere(items);
        if (start == null) {
            v.setTextViewText(R.id.widget_step_title, "Nothing here yet");
            v.setTextViewText(R.id.widget_step_detail, "Open tumbuh and add whatever is weighing on you.");
        } else {
            v.setTextViewText(R.id.widget_step_title, start.title);
            v.setTextViewText(R.id.widget_step_detail, start.firstStep.isEmpty()
                    ? Item.AREA_NAMES[start.area] + "  -  weight " + start.weight + "/5"
                    : "First step: " + start.firstStep);
        }
        StringBuilder counts = new StringBuilder();
        for (int a = 0; a < 4; a++) {
            if (a > 0) counts.append("  ·  ");
            counts.append(Item.AREA_NAMES[a]).append(' ').append(Triage.countActive(items, a));
        }
        v.setTextViewText(R.id.widget_counts, counts.toString());

        PendingIntent pi = PendingIntent.getActivity(c, 0,
                new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.widget_root, pi);
        return v;
    }
}
