
package com.krizz.mcgg;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;

public class MainActivity extends Activity {
    EditText history;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 32, 24, 24);
        root.setBackgroundColor(Color.rgb(12, 17, 38));

        TextView title = new TextView(this);
        title.setText("RAJA GOKGOK");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
                -1, 80));

        TextView subtitle = new TextView(this);
        subtitle.setText("PREDIKSI LAWAN MCGG");
        subtitle.setTextColor(Color.rgb(135, 160, 255));
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        history = new EditText(this);
        history.setHint("Riwayat lawan, pisahkan dengan koma\nContoh: Alpha, Beta, Alpha");
        history.setTextColor(Color.WHITE);
        history.setHintTextColor(Color.GRAY);
        history.setGravity(Gravity.TOP);
        history.setMinLines(4);
        root.addView(history, new LinearLayout.LayoutParams(
                -1, 180));

        Button predict = new Button(this);
        predict.setText("ANALISIS RIWAYAT");
        root.addView(predict);

        result = new TextView(this);
        result.setTextColor(Color.WHITE);
        result.setTextSize(17);
        result.setPadding(8, 20, 8, 20);
        root.addView(result);

        Button overlay = new Button(this);
        overlay.setText("AKTIFKAN FLOATING OVERLAY");
        root.addView(overlay);

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

        setContentView(root);
    }

    void analyze() {
        String[] names = history.getText().toString().split("[,\\n]");
        java.util.Map<String, Integer> counts =
                new java.util.LinkedHashMap<>();

        for (String name : names) {
            name = name.trim();
            if (!name.isEmpty()) {
                counts.put(name, counts.getOrDefault(name, 0) + 1);
            }
        }

        if (counts.isEmpty()) {
            result.setText("Masukkan riwayat lawan terlebih dahulu.");
            return;
        }

        String best = "";
        int max = 0;
        for (String name : counts.keySet()) {
            if (counts.get(name) > max) {
                best = name;
                max = counts.get(name);
            }
        }

        int total = 0;
        for (int count : counts.values()) total += count;

        int percent = Math.round(max * 100f / total);
        result.setText("Prediksi dari riwayat manual:\n"
                + "Lawan paling sering: " + best
                + "\nFrekuensi: " + percent + "%"
                + "\nTotal catatan: " + total
                + "\n\nIni estimasi frekuensi, bukan jaminan lawan berikutnya.");

        getSharedPreferences("raja", MODE_PRIVATE).edit()
                .putString("prediction", best)
                .putInt("percent", percent)
                .apply();
    }
}
