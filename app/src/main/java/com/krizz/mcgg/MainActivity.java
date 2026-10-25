
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
import android.view.ViewGroup;
import android.widget.*;

import java.util.*;

public class MainActivity extends Activity {
    EditText history;
    TextView result;

    int dp(float n) {
        return (int)(n * getResources().getDisplayMetrics().density + 0.5f);
    }

    GradientDrawable bg(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(22));
        d.setStroke(dp(1), stroke);
        return d;
    }

    LinearLayout.LayoutParams lp() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(10, 14, 30));
        getWindow().setNavigationBarColor(Color.rgb(10, 14, 30));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(10, 14, 30));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(1);
        root.setPadding(dp(20), dp(28), dp(20), dp(28));
        scroll.addView(root);

        TextView brand = text("MCGG  /  SMART PREDICTOR", 11,
                Color.rgb(145, 168, 255), true);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand, lp());

        TextView title = text("RAJA GOKGOK", 29, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(3));
        root.addView(title, lp());

        TextView sub = text("Prediksi lawan berikutnya", 14,
                Color.rgb(180, 191, 220), false);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, dp(22));
        root.addView(sub, lp());

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(1);
        card.setPadding(dp(17), dp(17), dp(17), dp(17));
        card.setBackground(bg(Color.rgb(22, 30, 57),
                Color.rgb(65, 82, 137)));
        root.addView(card, lp());

        card.addView(text("RIWAYAT PERTANDINGAN", 11,
                Color.rgb(153, 174, 255), true), lp());

        TextView hint = text(
                "Masukkan riwayat lawan, pisahkan dengan koma atau baris baru.",
                12, Color.rgb(170, 182, 211), false);
        hint.setPadding(0, dp(8), 0, dp(12));
        card.addView(hint, lp());

        history = new EditText(this);
        history.setHint("Contoh: Alpha, Beta, Alpha");
        history.setHintTextColor(Color.GRAY);
        history.setTextColor(Color.WHITE);
        history.setTextSize(14);
        history.setGravity(Gravity.TOP | Gravity.START);
        history.setMinLines(4);
        history.setPadding(dp(14), dp(12), dp(14), dp(12));
        history.setBackground(bg(Color.rgb(17, 24, 46),
                Color.rgb(62, 78, 123)));
        card.addView(history, new LinearLayout.LayoutParams(-1, dp(130)));

        Button predict = new Button(this);
        predict.setText("ANALISIS RIWAYAT");
        predict.setTextColor(Color.WHITE);
        predict.setBackground(bg(Color.rgb(78, 101, 232),
                Color.rgb(123, 144, 255)));
        LinearLayout.LayoutParams bp = lp();
        bp.topMargin = dp(14);
        card.addView(predict, bp);

        LinearLayout resultCard = new LinearLayout(this);
        resultCard.setOrientation(1);
        resultCard.setPadding(dp(17), dp(17), dp(17), dp(17));
        resultCard.setBackground(bg(Color.rgb(22, 30, 57),
                Color.rgb(65, 82, 137)));
        LinearLayout.LayoutParams rp = lp();
        rp.topMargin = dp(16);
        root.addView(resultCard, rp);

        resultCard.addView(text("HASIL PREDIKSI", 11,
                Color.rgb(153, 174, 255), true), lp());

        result = text("Prediksi akan muncul di sini setelah riwayat dianalisis.",
                14, Color.WHITE, false);
        result.setPadding(0, dp(12), 0, dp(5));
        result.setLineSpacing(dp(4), 1f);
        resultCard.addView(result, lp());

        Button overlay = new Button(this);
        overlay.setText("AKTIFKAN FLOATING OVERLAY");
        overlay.setTextColor(Color.WHITE);
        overlay.setBackground(bg(Color.rgb(35, 47, 82),
                Color.rgb(76, 94, 145)));
        LinearLayout.LayoutParams op = lp();
        op.topMargin = dp(16);
        root.addView(overlay, op);

        TextView note = text(
                "ESTIMASI BERDASARKAN RIWAYAT MANUAL • BUKAN JAMINAN",
                10, Color.rgb(130, 143, 177), false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(18), 0, 0);
        root.addView(note, lp());

        predict.setOnClickListener(v -> analyze());

        overlay.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent i = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(i);
            } else {
                startService(new Intent(this, OverlayService.class));
                Toast.makeText(this, "Overlay dijalankan",
                        Toast.LENGTH_SHORT).show();
            }
        });

        history.setText(getSharedPreferences("raja", MODE_PRIVATE)
                .getString("history", ""));

        setContentView(scroll);
    }

    void analyze() {
        String raw = history.getText().toString().trim();
        String[] names = raw.split("[,\\n;]");

        List<String> ordered = new ArrayList<>();
        Map<String, Double> scores = new LinkedHashMap<>();

        for (String value : names) {
            String name = value.trim();
            if (!name.isEmpty()) {
                ordered.add(name);
                if (!scores.containsKey(name)) scores.put(name, 1.0);
            }
        }

        if (ordered.isEmpty()) {
            result.setText("Masukkan riwayat lawan terlebih dahulu.");
            return;
        }

        int total = ordered.size();

        for (int i = 0; i < total; i++) {
            String name = ordered.get(i);
            double weight = (i + 1.0) / Math.max(1, total);
            scores.put(name, scores.get(name) + 1.0 + weight * 0.65);
        }

        String latest = ordered.get(total - 1);
        scores.put(latest, scores.get(latest) * 0.25);

        List<Map.Entry<String, Double>> ranked =
                new ArrayList<>(scores.entrySet());

        ranked.sort((a, b) ->
                Double.compare(b.getValue(), a.getValue()));

        double sum = 0;
        for (Map.Entry<String, Double> e : ranked) {
            sum += e.getValue();
        }

        StringBuilder out = new StringBuilder();
        out.append("Prediksi dari pola riwayat:\n\n");

        int limit = Math.min(3, ranked.size());

        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> e = ranked.get(i);
            int percent = (int)Math.round(e.getValue() * 100 / sum);

            out.append(i + 1).append(". ")
                    .append(e.getKey())
                    .append(" — ").append(percent).append("%\n");
        }

        out.append("\nTotal catatan: ").append(total);
        out.append("\nLawan terakhir: ").append(latest);
        out.append("\n\nPersentase adalah skor relatif dari riwayat, bukan peluang resmi atau jaminan.");

        result.setText(out.toString());

        getSharedPreferences("raja", MODE_PRIVATE).edit()
                .putString("history", raw)
                .putString("prediction", ranked.get(0).getKey())
                .putInt("percent",
                        (int)Math.round(ranked.get(0).getValue() * 100 / sum))
                .apply();
    }
}
