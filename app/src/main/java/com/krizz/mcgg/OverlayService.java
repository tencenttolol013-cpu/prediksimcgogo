package com.krizz.mcgg;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OverlayService extends Service {
    private WindowManager wm;
    private WindowManager.LayoutParams params;
    private View currentView;
    private boolean minimized = false;
    private EditText historyInput;
    private EditText selfInput;
    private TextView candidatesView;
    private LinearLayout panel;
    private String savedHistory = "";
    private String savedSelf = "";

    private final String[] defaults = {
            "Player1", "Player2", "Player3", "Player4",
            "Player5", "Player6", "Player7"
    };

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
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

    private TextView label(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h));
    }

    private Button button(String text, int background, int textColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setTextColor(textColor);
        b.setBackground(shape(background, Color.rgb(55, 72, 101), 12));
        b.setPadding(dp(8), dp(2), dp(8), dp(2));
        return b;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        showPanel();
    }

    private WindowManager.LayoutParams makeParams(int width, int height) {
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                width, height, type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        p.y = dp(45);
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
        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(false);
        scroller.setBackground(shape(Color.rgb(12, 22, 43), Color.rgb(64, 79, 111), 24));

        panel = vertical();
        panel.setPadding(dp(14), dp(12), dp(14), dp(12));
        scroller.addView(panel);

        LinearLayout header = horizontal();
        header.setPadding(dp(4), dp(2), dp(2), dp(12));

        TextView logo = label("♟", 22, Color.WHITE, true);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(shape(Color.rgb(0, 142, 190), Color.rgb(55, 194, 226), 13));
        header.addView(logo, lp(42, 42));

        LinearLayout titleBox = vertical();
        titleBox.setPadding(dp(10), 0, 0, 0);
        titleBox.addView(label("Raja Gokgok", 17, Color.WHITE, true));
        titleBox.addView(label("HUD  •  MCGG", 10, Color.rgb(94, 224, 229), true));
        header.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));

        Button minimize = button("—", Color.rgb(43, 55, 82), Color.WHITE);
        header.addView(minimize, lp(45, 40));
        Button close = button("✕", Color.rgb(43, 55, 82), Color.WHITE);
        LinearLayout.LayoutParams closeP = lp(45, 40);
        closeP.leftMargin = dp(6);
        header.addView(close, closeP);
        panel.addView(header);

        LinearLayout tabs = horizontal();
        String[] tabNames = {"Scout", "Edit Nama", "Taktik", "Creator"};
        for (String tab : tabNames) {
            TextView t = label(tab, 11, Color.rgb(203, 214, 235), true);
            t.setGravity(Gravity.CENTER);
            t.setPadding(dp(4), dp(10), dp(4), dp(10));
            tabs.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        }
        panel.addView(tabs);
        addDivider(panel);

        TextView inputTitle = label("RIWAYAT LAWAN", 12, Color.rgb(132, 238, 221), true);
        inputTitle.setPadding(0, dp(12), 0, dp(7));
        panel.addView(inputTitle);

        selfInput = new EditText(this);
        selfInput.setSingleLine(true);
        selfInput.setTextSize(13);
        selfInput.setTextColor(Color.WHITE);
        selfInput.setHintTextColor(Color.rgb(133, 148, 175));
        selfInput.setHint("Nama kamu (opsional)");
        selfInput.setPadding(dp(12), dp(8), dp(12), dp(8));
        selfInput.setBackground(shape(Color.rgb(10, 19, 36), Color.rgb(38, 54, 79), 12));
        selfInput.setText(savedSelf);
        panel.addView(selfInput, lp(-1, 45));

        TextView helper = label("Masukkan lawan berurutan, pisahkan dengan koma atau baris baru.", 11,
                Color.rgb(157, 173, 199), false);
        helper.setPadding(0, dp(10), 0, dp(7));
        panel.addView(helper);

        historyInput = new EditText(this);
        historyInput.setGravity(Gravity.TOP | Gravity.START);
        historyInput.setTextSize(13);
        historyInput.setTextColor(Color.WHITE);
        historyInput.setHintTextColor(Color.rgb(126, 142, 169));
        historyInput.setHint("Player1, Player3, Player4, Player4...");
        historyInput.setMinLines(3);
        historyInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        historyInput.setBackground(shape(Color.rgb(9, 18, 34), Color.rgb(38, 54, 79), 12));
        historyInput.setText(savedHistory);
        panel.addView(historyInput, lp(-1, 100));

        LinearLayout actions = horizontal();
        Button save = button("Simpan data", Color.rgb(31, 45, 69), Color.WHITE);
        Button predict = button("✦ Prediksi", Color.rgb(0, 194, 155), Color.rgb(4, 24, 35));
        actions.addView(save, new LinearLayout.LayoutParams(0, dp(45), 1));
        LinearLayout.LayoutParams predP = new LinearLayout.LayoutParams(0, dp(45), 1);
        predP.leftMargin = dp(8);
        actions.addView(predict, predP);
        LinearLayout.LayoutParams actP = lp(-1, -2);
        actP.topMargin = dp(12);
        panel.addView(actions, actP);

        LinearLayout resultCard = vertical();
        resultCard.setPadding(dp(12), dp(12), dp(12), dp(12));
        resultCard.setBackground(shape(Color.rgb(9, 18, 34), Color.rgb(35, 52, 77), 15));
        LinearLayout.LayoutParams resultP = lp(-1, -2);
        resultP.topMargin = dp(12);
        panel.addView(resultCard, resultP);

        LinearLayout resultHead = horizontal();
        resultHead.addView(label("KANDIDAT LAWAN", 12, Color.rgb(111, 230, 205), true),
                new LinearLayout.LayoutParams(0, -2, 1));
        resultHead.addView(label("Skor relatif", 10, Color.rgb(157, 173, 199), false));
        resultCard.addView(resultHead);

        candidatesView = label("Masukkan riwayat lalu tekan Prediksi.", 13,
                Color.rgb(225, 233, 247), false);
        candidatesView.setPadding(0, dp(10), 0, dp(2));
        candidatesView.setLineSpacing(dp(4), 1f);
        resultCard.addView(candidatesView);

        TextView footer = label("⠿  Geser panel untuk memindahkan  •  Tutup", 10,
                Color.rgb(111, 220, 229), false);
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
        attach(scroller, dp(350), dp(590));
    }

    private void addDivider(LinearLayout parent) {
        View line = new View(this);
        line.setBackgroundColor(Color.rgb(44, 59, 83));
        LinearLayout.LayoutParams p = lp(-1, 1);
        p.topMargin = dp(3);
        parent.addView(line, p);
    }

    private void saveData() {
        savedHistory = historyInput.getText().toString();
        savedSelf = selfInput.getText().toString().trim();
        getSharedPreferences("raja", MODE_PRIVATE).edit()
                .putString("history", savedHistory)
                .putString("self", savedSelf)
                .apply();
        Toast.makeText(this, "Data disimpan", Toast.LENGTH_SHORT).show();
    }

private void runPrediction() {
        String[] parts = savedHistory.split("[,;\\n]");
        List<String> ordered = new ArrayList<>();
        Map<String, Double> score = new LinkedHashMap<>();
        Map<String, Integer> count = new LinkedHashMap<>();

        for (String part : parts) {
            String name = part.trim();
            if (!name.isEmpty() && !name.equalsIgnoreCase(savedSelf)) {
                ordered.add(name);
                count.put(name, count.containsKey(name) ? count.get(name) + 1 : 1);
                if (!score.containsKey(name)) score.put(name, 1.0);
            }
        }

        if (ordered.isEmpty()) {
            candidatesView.setText("Riwayat masih kosong. Masukkan beberapa nama lawan dahulu.");
            return;
        }

        int n = ordered.size();
        for (int i = 0; i < n; i++) {
            String name = ordered.get(i);
            double recency = (i + 1.0) / n;
            score.put(name, score.get(name) + 1.0 + recency * 0.65);
        }

        String latest = ordered.get(n - 1);
        score.put(latest, score.get(latest) * 0.25);

        List<Map.Entry<String, Double>> ranked = new ArrayList<>(score.entrySet());
        ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Mode dua kandidat 50:50 adalah tampilan pilihan setara, bukan probabilitas faktual.
        StringBuilder out = new StringBuilder();
        if (ranked.size() >= 2) {
            out.append("MODE 50 : 50 — DUA PILIHAN UTAMA\n");
            out.append("1. ").append(ranked.get(0).getKey())
                    .append("  •  50% tampilan\n");
            out.append("2. ").append(ranked.get(1).getKey())
                    .append("  •  50% tampilan\n\n");
            out.append("Skor riwayat:\n");
            int limit = Math.min(5, ranked.size());
            double sum = 0;
            for (Map.Entry<String, Double> e : ranked) sum += e.getValue();
            for (int i = 0; i < limit; i++) {
                Map.Entry<String, Double> e = ranked.get(i);
                int pct = (int) Math.round(e.getValue() * 100.0 / sum);
                out.append(i + 1).append(". ").append(e.getKey())
                        .append(" — skor relatif ").append(pct).append("%")
                        .append(" (tercatat ").append(count.get(e.getKey())).append("×)\n");
            }
            out.append("\n50:50 adalah mode tampilan dua kandidat, bukan peluang matchmaking yang terverifikasi.");
        } else {
            out.append("Kandidat: ").append(ranked.get(0).getKey())
                    .append("\nCatatan: ").append(n)
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
        TextView bubble = label("♟", 26, Color.WHITE, true);
        bubble.setGravity(Gravity.CENTER);
        bubble.setBackground(shape(Color.rgb(0, 143, 190),
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
            @Override public boolean onTouch(View v, MotionEvent event) {
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
                        if (Math.abs(dx) > dp(5) || Math.abs(dy) > dp(5)) moved = true;
                        if (moved && !minimized) {
                            params.x = startX + (int) dx;
                            params.y = startY + (int) dy;
                            try { wm.updateViewLayout(currentView, params); } catch (Exception ignored) {}
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
