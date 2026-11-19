package com.vanipy.tumbuh;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

/** Resource rendering checks only. Real host qualification is separate. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class WidgetRenderTest {
    private int W = 660, H = 320;

    private void snapshot(RemoteViews rv, Context c, String outName) throws Exception {
        FrameLayout parent = new FrameLayout(c);
        View v = rv.apply(c, parent);
        v.measure(View.MeasureSpec.makeMeasureSpec(W, View.MeasureSpec.EXACTLY),
                  View.MeasureSpec.makeMeasureSpec(H, View.MeasureSpec.EXACTLY));
        v.layout(0, 0, W, v.getMeasuredHeight());
        Bitmap bmp = Bitmap.createBitmap(W, v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        v.draw(new Canvas(bmp));
        File out = new File("build/widget-snapshots", outName);
        out.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(out)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        assertTrue(out.length() > 300);
    }

    @Test
    @Config(qualifiers = "mdpi")
    public void allThirtyGridSpans() throws Exception {
        Context c = RuntimeEnvironment.getApplication();
        DemoMode.set(c, true);
        for (int columns = 1; columns <= 5; columns++) {
            for (int rows = 1; rows <= 6; rows++) {
                W = columns * 73 - 16;
                H = rows * 96 - 16;
                android.os.Bundle options = new android.os.Bundle();
                options.putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, W);
                options.putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, H);
                RemoteViews rv = TumbuhWidgetProvider.buildViews(c, options);
                View view = rv.apply(c, new FrameLayout(c));
                assertEquals(View.GONE, view.findViewById(R.id.widget_demo_badge).getVisibility());
                snapshot(rv, c, "span-" + columns + "x" + rows + ".png");
            }
        }
        DemoMode.set(c, false);
    }

    @Test
    @Config(qualifiers = "mdpi")
    public void counterAndCompactResize() throws Exception {
        Context c = RuntimeEnvironment.getApplication();
        DemoMode.set(c, true);
        android.os.Bundle options = new android.os.Bundle();
        options.putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 64);
        options.putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 64);
        W = 48; H = 48;
        snapshot(TumbuhWidgetProvider.buildViews(c, options), c, "widget-counter-48dp.png");
        W = 64; H = 64;
        snapshot(TumbuhWidgetProvider.buildViews(c, options), c, "widget-counter-64dp.png");
        options.putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 350);
        W = 350;
        snapshot(TumbuhWidgetProvider.buildViews(c, options), c, "widget-strip-350x64dp.png");
        options.putInt(android.appwidget.AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180);
        H = 180;
        snapshot(TumbuhWidgetProvider.buildViews(c, options), c, "widget-expanded-350x180dp.png");
        DemoMode.set(c, false);
    }

    @Test
    public void widgetDemoMode() throws Exception {
        Context c = RuntimeEnvironment.getApplication();
        DemoMode.set(c, true);
        snapshot(TumbuhWidgetProvider.buildViews(c), c, "widget-demo.png");
        List<Item> demo = Storage.load(c, Storage.DEMO_NAME);
        assertEquals(13, demo.size());
        assertEquals("Reply to Rani about the workshop", Triage.startHere(demo).title);
        c.getSharedPreferences("tumbuh_prefs", Context.MODE_PRIVATE).edit().clear().commit();
        new File(c.getFilesDir(), Storage.DEMO_NAME).delete();
    }

    @Test
    public void widgetEmptyRealProfile() throws Exception {
        Context c = RuntimeEnvironment.getApplication();
        snapshot(TumbuhWidgetProvider.buildViews(c), c, "widget-empty.png");
    }

    @Test
    public void demoModeNeverTouchesRealStore() throws Exception {
        Context c = RuntimeEnvironment.getApplication();
        DemoMode.set(c, true);
        Item extra = new Item();
        extra.id = 99; extra.area = Item.SPACE; extra.title = "Edited in demo";
        List<Item> demo = Storage.load(c, Storage.DEMO_NAME);
        demo.add(extra);
        Storage.save(c, Storage.DEMO_NAME, demo);
        assertTrue(Storage.load(c, Storage.NAME).isEmpty());
        c.getSharedPreferences("tumbuh_prefs", Context.MODE_PRIVATE).edit().clear().commit();
        new File(c.getFilesDir(), Storage.DEMO_NAME).delete();
    }
}
