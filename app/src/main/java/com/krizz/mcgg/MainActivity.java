
package com.krizz.mcgg;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import java.util.*;

public class OverlayService extends Service {
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private View currentView;
    private EditText historyInput, selfInput;
    private TextView candidatesView;
    private boolean minimized = false;
    private String savedHistory = "", savedSelf = "";

    private int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color, int stroke, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        d.setStroke(dp(1), stroke);
        return d;
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(
                w < 0 ? w : dp(w), h < 0 ? h : dp(h));
    }

    private Button button(String text, int color, int textColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setTextColor(textColor);
        b.setBackground(shape(color, Color.rgb(55, 72, 101), 12));
        return b;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);

        savedHistory = getSharedPreferences("raja", MODE_PRIVATE)
                .getString("history", "");
        savedSelf = getSharedPreferences("raja", MODE_PRIVATE)
                .getString("self", "");

        showPanel();
    }

    private WindowManager.LayoutParams makeParams(int width, int height) {
        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                width, height, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);

        p.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        p.y = dp(35);
        return p;
    }

    private void attach(View view, int width, int height) {
        try {
            if (currentView != null) wm.removeView(currentView);
        } catch (Exception ignored) {}

        currentView = view;
        params = makeParams(width, height);
        wm.addView(view, params);
    }

    private void showPanel() {
        minimized = false;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setBackground(shape(
                Color.rgb(12, 22, 43),
                Color.rgb(64, 79, 111), 24));

        LinearLayout panel = vertical();
        panel.setPadding(dp(14), dp(12), dp(14), dp(12));
        scroll.addView(panel);

        LinearLayout header = horizontal();

        TextView logo = label("♟", 24, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(shape(
                Color.rgb(0, 142, 190),
                Color.rgb(55, 194, 226), 13));
        header.addView(logo, lp(44, 44));

        LinearLayout title = vertical();
        title.setPadding(dp(10), 0, 0, 0);
        title.addView(label("Raja Gokgok", 17, Color.WHITE, true));
        title.addView(label("HUD  •  MCGG", 10,
                Color.rgb(94, 224, 229), true));

        header.addView(title,
                new LinearLayout.LayoutParams(0, -2, 1));

        Button minimize = button("—",
                Color.rgb(43, 55, 82), Color.WHITE);
        header.addView(minimize, lp(42, 40));

        Button close = button("✕",
                Color.rgb(43, 55, 82), Color.WHITE);
        LinearLayout.LayoutParams cp = lp(42, 40);
        cp.leftMargin = dp(5);
        header.addView(close, cp);
        panel.addView(header);

        LinearLayout tabs = horizontal();
        for (String name : new String[]{"⚔ Scout", "✎ Edit Nama",
                "♢ Taktik", "✧ Creator"}) {
            TextView tab = label(name, 11,
                    Color.rgb(203, 214, 235), true);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(3), dp(12), dp(3), dp(12));
            tabs.addView(tab,
                    new LinearLayout.LayoutParams(0, -2, 1));
        }
        panel.addView(tabs);

        addDivider(panel);

        TextView selfLabel = label("NAMA KAMU (OPSIONAL)", 12,
                Color.rgb(132, 238, 221), true);
        selfLabel.setPadding(0, dp(12), 0, dp(6));
        panel.addView(selfLabel);

        selfInput = new EditText(this);
        selfInput.setSingleLine(true);
        selfInput.setTextSize(13);
        selfInput.setTextColor(Color.WHITE);
        selfInput.setHintTextColor(Color.GRAY);
        selfInput.setHint("Nama kamu (agar tidak terhitung)");
        selfInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        selfInput.setBackground(shape(
                Color.rgb(10, 19, 36),
                Color.rgb(38, 54, 79), 12));
        selfInput.setText(savedSelf);
        panel.addView(selfInput, lp(-1, 46));

        TextView historyLabel = label("RIWAYAT LAWAN", 12,
                Color.rgb(132, 238, 221), true);
        historyLabel.setPadding(0, dp(12), 0, dp(6));
        panel.addView(historyLabel);

        historyInput = new EditText(this);
        historyInput.setGravity(Gravity.TOP | Gravity.START);
        historyInput.setTextSize(13);
        historyInput.setTextColor(Color.WHITE);
        historyInput.setHintTextColor(Color.GRAY);
        historyInput.setHint("Player1, Player3, Player4...");
        historyInput.setMinLines(3);
        historyInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        historyInput.setBackground(shape(
                Color.rgb(9, 18, 34),
                Color.rgb(38, 54, 79), 12));
        historyInput.setText(savedHistory);
        panel.addView(historyInput, lp(-1, 100));

        LinearLayout actions = horizontal();

        Button save = button("Simpan data",
                Color.rgb(31, 45, 69), Color.WHITE);
        Button predict = button("✧ Prediksi",
                Color.rgb(0, 194, 155), Color.rgb(4, 24, 35));

        actions.addView(save,
                new LinearLayout.LayoutParams(0, dp(46), 1));

        LinearLayout.LayoutParams pp =
                new LinearLayout.LayoutParams(0, dp(46), 1);
        pp.leftMargin = dp(8);
        actions.addView(predict, pp);

        LinearLayout.LayoutParams ap = lp(-1, -2);
        ap.topMargin = dp(12);
        panel.addView(actions, ap);

        LinearLayout resultCard = vertical();
        resultCard.setPadding(dp(12), dp(12), dp(12), dp(12));
        resultCard.setBackground(shape(
                Color.rgb(9, 18, 34),
                Color.rgb(35, 52, 77), 15));

        LinearLayout.LayoutParams rcp = lp(-1, -2);
        rcp.topMargin = dp(12);
        panel.addView(resultCard, rcp);

        resultCard.addView(label("KANDIDAT LAWAN", 12,
                Color.rgb(111, 230, 205), true));

        candidatesView = label(
                "Masukkan riwayat lalu tekan Prediksi.",
                13, Color.rgb(225, 233, 247), false);
        candidatesView.setPadding(0, dp(10), 0, dp(3));
        candidatesView.setLineSpacing(dp(4), 1f);
        resultCard.addView(candidatesView);

        TextView footer = label(
                "⠿  Geser panel untuk memindahkan",
                10, Color.rgb(111, 220, 229), false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(12), 0, 0);
        panel.addView(footer);

        minimize.setOnClickListener(v -> showBubble());
        close.setOnClickListener(v -> stopSelf());
        save.setOnClickListener(v -> saveData());
        predict.setOnClickListener(v -> {
            saveData();
            runPrediction();
        });

        makeDraggable(header);
        attach(scroll, dp(350), dp(590));
    }

    private void addDivider(LinearLayout parent) {
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(44, 59, 83));
        parent.addView(line, lp(-1, 1));
    }

    private void saveData() {
        savedHistory = historyInput.getText().toString();
        savedSelf = selfInput.getText().toString().trim();

        getSharedPreferences("raja", MODE_PRIVATE).edit()
                .putString("history", savedHistory)
                .putString("self", savedSelf)
                .apply();

        Toast.makeText(this, "Data disimpan",
                Toast.LENGTH_SHORT).show();
    }

    private void runPrediction() {
        String[] parts = savedHistory.split("[,;\\n]");
        List<String> ordered = new ArrayList<>();
        Map<String, Double> scores = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();

        for (String part : parts) {
            String name = part.trim();

            if (!name.isEmpty()
                    && !name.equalsIgnoreCase(savedSelf)) {
                ordered.add(name);
                counts.put(name, counts.containsKey(name)
                        ? counts.get(name) + 1 : 1);
                if (!scores.containsKey(name)) scores.put(name, 1.0);
            }
        }

        if (ordered.isEmpty()) {
            candidatesView.setText(
                    "Riwayat masih kosong. Masukkan nama lawan dahulu.");
            return;
        }

        int n = ordered.size();

        for (int i = 0; i < n; i++) {
            String name = ordered.get(i);
            double recency = (i + 1.0) / n;
            scores.put(name,
                    scores.get(name) + 1.0 + recency * 0.65);
        }

        String latest = ordered.get(n - 1);
        scores.put(latest, scores.get(latest) * 0.25);

        List<Map.Entry<String, Double>> ranked =
                new ArrayList<>(scores.entrySet());
        ranked.sort((a, b) ->
                Double.compare(b.getValue(), a.getValue()));

        StringBuilder out = new StringBuilder();

        if (ranked.size() >= 2) {
            out.append("MODE DUA KANDIDAT • 50:50 TAMPILAN\n\n");
            out.append("#1  ").append(ranked.get(0).getKey())
                    .append("  —  50%\n");
            out.append("#2  ").append(ranked.get(1).getKey())
                    .append("  —  50%\n\n");
            out.append("PERINGKAT BERDASARKAN RIWAYAT\n");

            double totalScore = 0;
            for (Map.Entry<String, Double> e : ranked)
                totalScore += e.getValue();

            int limit = Math.min(5, ranked.size());

            for (int i = 0; i < limit; i++) {
                Map.Entry<String, Double> e = ranked.get(i);
                int relative = (int)Math.round(
                        e.getValue() * 100.0 / totalScore);

                out.append("#").append(i + 1).append(" ")
                        .append(e.getKey())
                        .append("  | skor relatif ")
                        .append(relative).append("%")
                        .append("  | tercatat ")
                        .append(counts.get(e.getKey())).append("x\n");
            }

            out.append("\n50:50 adalah mode tampilan dua kandidat, bukan peluang matchmaking yang terverifikasi.");
        } else {
            out.append("Kandidat: ").append(ranked.get(0).getKey())
                    .append("\nData belum cukup untuk membandingkan dua kandidat.");
        }

        candidatesView.setText(out.toString());

        getSharedPreferences("raja", MODE_PRIVATE).edit()
                .putString("prediction", ranked.get(0).getKey())
                .putInt("percent", 50)
                .apply();
    }

    private void showBubble() {
        minimized = true;

        TextView bubble = label("♟", 27, Color.WHITE, true);
        bubble.setGravity(Gravity.CENTER);
        bubble.setBackground(shape(
                Color.rgb(0, 143, 190),
                Color.rgb(102, 227, 240), 22));
        bubble.setElevation(dp(8));
        bubble.setOnClickListener(v -> showPanel());

        makeDraggable(bubble);
        attach(bubble, dp(62), dp(62));
    }

    private void makeDraggable(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            int startX, startY;
            boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (params == null) return false;

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        startX = params.x;
                        startY = params.y;
                        moved = false;
                        return false;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;

                        if (Math.abs(dx) > dp(5)
                                || Math.abs(dy) > dp(5)) moved = true;

                        if (moved && !minimized) {
                            params.x = startX + (int)dx;
                            params.y = startY + (int)dy;
                            try {
                                wm.updateViewLayout(currentView, params);
                            } catch (Exception ignored) {}
                        }
                        return moved;
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (currentView == null) showPanel();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            if (currentView != null) wm.removeView(currentView);
        } catch (Exception ignored) {}
        currentView = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
