package com.krizz.mcgg;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

public class MainActivity extends Activity {
    private EditText history;
    private TextView result;
    private LinearLayout root;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(10, 14, 30));
        getWindow().setNavigationBarColor(Color.rgb(10, 14, 30));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(10, 14, 30));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView eyebrow = new TextView(this);
        eyebrow.setText("MCGG  /  SMART PREDICTOR");
        eyebrow.setTextColor(Color.rgb(139, 164, 255));
        eyebrow.setTextSize(11);
        eyebrow.setLetterSpacing(0.13f);
        eyebrow.setGravity(Gravity.CENTER);
        root.addView(eyebrow, matchWrap());

        TextView title = new TextView(this);
        title.setText("RAJA GOKGOK");
        title.setTextColor(Color.WHITE);
        title.setTextSize(29);
        title.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(7), 0, dp(2));
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Prediksi lawan berikutnya");
        subtitle.setTextColor(Color.rgb(176, 187, 219));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, dp(22));
        root.addView(subtitle, matchWrap());

        LinearLayout inputCard = card();
        root.addView(inputCard, matchWrap());

        TextView inputTitle = sectionTitle("RIWAYAT PERTANDINGAN");
        inputCard.addView(inputTitle, matchWrap());

        TextView helper = new TextView(this);
        helper.setText("Masukkan nama lawan sesuai urutan pertandingan. Pisahkan dengan koma atau baris baru.");
        helper.setTextColor(Color.rgb(167, 179, 211));
        helper.setTextSize(12);
        helper.setPadding(0, dp(6), 0, dp(12));
        inputCard.addView(helper, matchWrap());

        history = new EditText(this);
        history.setHint("Contoh: Alpha, Beta, Alpha");
        history.setHintTextColor(Color.rgb(124, 137, 170));
        history.setTextColor(Color.WHITE);
        history.setTextSize(14);
        history.setGravity(Gravity.TOP | Gravity.START);
        history.setMinLines(4);
        history.setPadding(dp(14), dp(13), dp(14), dp(13));
        history.setBackground(roundRect(Color.rgb(20, 28, 54), dp(16),
                Color.rgb(57, 73, 119), dp(1)));
        inputCard.addView(history, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(130)));

        Button predict = makeButton("ANALISIS RIWAYAT", true);
        LinearLayout.LayoutParams predictParams = matchWrap();
        predictParams.topMargin = dp(14);
        inputCard.addView(predict, predictParams);

        LinearLayout resultCard = card();
        LinearLayout.LayoutParams resultParams = matchWrap();
        resultParams.topMargin = dp(16);
        root.addView(resultCard, resultParams);

        TextView resultLabel = sectionTitle("HASIL PREDIKSI");
        resultCard.addView(resultLabel, matchWrap());

        result = new TextView(this);
        result.setText("Prediksi akan muncul di sini setelah riwayat dianalisis.");
        result.setTextColor(Color.rgb(220, 227, 249));
        result.setTextSize(14);
        result.setLineSpacing(dp(4), 1.0f);
        result.setPadding(0, dp(12), 0, dp(4));
        resultCard.addView(result, matchWrap());

        Button overlay = makeButton("AKTIFKAN FLOATING OVERLAY", false);
        LinearLayout.LayoutParams overlayParams = matchWrap();
        overlayParams.topMargin = dp(16);
        root.addView(overlay, overlayParams);

        TextView note = new TextView(this);
        note.setText("PREDIKSI BERDASARKAN RIWAYAT MANUAL • BUKAN JAMINAN");
        note.setTextColor(Color.rgb(118, 132, 170));
        note.setTextSize(10);
        note.setGravity(Gravity.CENTER);
        note.setPadding(dp(8), dp(18), dp(8), 0);
        root.addView(note, matchWrap());

        predict.setOnClickListener(v -> analyze());

        overlay.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                startService(new Intent(this, OverlayService.class));
                Toast.makeText(this, "Overlay dijalankan", Toast.LENGTH_SHORT).show();
            }
        });

        String saved = getSharedPreferences("raja", MODE_PRIVATE)
                .getString("history", "");
        history.setText(saved);

        setContentView(scroll);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout card() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(17), dp(17), dp(17), dp(17));
        box.setBackground(roundRect(Color.rgb(22, 30, 57), dp(22),
                Color.rgb(63, 79, 129), dp(1)));
        return box;
    }

    private TextView sectionTitle(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(Color.rgb(153, 174, 255));
        view.setTextSize(11);
        view.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        view.setLetterSpacing(0.09f);
        return view;
    }

    private Button makeButton(String text, boolean primary) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(12);
        button.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setBackground(roundRect(
                primary ? Color.rgb(83, 105, 235) : Color.rgb(35, 47, 82),
                dp(16),
                primary ? Color.rgb(117, 139, 255) : Color.rgb(76, 94, 145),
                dp(1)));
        return button;
    }

    private GradientDrawable roundRect(int color, int radius, int strokeColor, int strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    private void analyze() {
        String raw = history.getText().toString().trim();
        String[] names = raw.split("[,\\n;]");
        List<String> ordered = new ArrayList<>();
        Map<String, Double> scores = new LinkedHashMap<>();

        for (String value : names) {
            String name = value.trim();
            if (!name.isEmpty()) {
                ordered.add(name);
                scores.putIfAbsent(name, 1.0);
            }
        }

        if (ordered.isEmpty()) {
            result.setText("Masukkan riwayat lawan terlebih dahulu.");
            return;
        }

        int total = ordered.size();
        for (int i = 0; i < total; i++) {
            String name = ordered.get(i);
            double recencyWeight = (i + 1.0) / Math.max(1, total);
            scores.put(name, scores.get(name) + 1.0 + (recencyWeight * 0.65));
        }

        String latest = ordered.get(total - 1);
        scores.put(latest, scores.get(latest) * 0.25);

        List<Map.Entry<String, Double>> ranked = new ArrayList<>(scores.entrySet());
        ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        double sum = 0.0;
        for (Map.Entry<String, Double> entry : ranked) sum += entry.getValue();

        StringBuilder output = new StringBuilder();
        output.append("Prediksi dari pola riwayat:\n\n");
        int limit = Math.min(3, ranked.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> entry = ranked.get(i);
            int percent = (int) Math.round(entry.getValue() * 100.0 / sum);
            output.append(i + 1).append(". ")
                    .append(entry.getKey())
                    .append(" — ").append(percent).append("%\n");
        }

        output.append("\nTotal catatan: ").append(total);
        output.append("\nLawan terakhir: ").append(latest);
        output.append("\n\nPersentase adalah skor relatif dari riwayat yang dimasukkan, bukan peluang resmi atau jaminan lawan berikutnya.");

        result.setText(output.toString());

        getSharedPreferences("raja", MODE_PRIVATE).edit()
                .putString("history", raw)
                .putString("prediction", ranked.get(0).getKey())
                .putInt("percent", (int) Math.round(ranked.get(0).getValue() * 100.0 / sum))
                .apply();
    }
}
ring part : parts) {
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
