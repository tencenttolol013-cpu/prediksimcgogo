
package com.krizz.mcgg;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private WindowManager wm;
    private View panel;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(18, 14, 18, 14);
        box.setBackgroundColor(Color.rgb(24, 31, 62));

        TextView header = new TextView(this);
        header.setText("RAJA GOKGOK  ⋮");
        header.setTextColor(Color.rgb(150, 175, 255));
        header.setTextSize(17);
        header.setPadding(4, 4, 4, 12);
        box.addView(header);

        TextView prediction = new TextView(this);
        prediction.setTextColor(Color.WHITE);
        prediction.setTextSize(15);

        android.content.SharedPreferences prefs =
                getSharedPreferences("raja", MODE_PRIVATE);
        String name = prefs.getString("prediction", "Belum ada prediksi");
        int percent = prefs.getInt("percent", 0);
        prediction.setText("Prediksi: " + name + "\nFrekuensi: " + percent + "%");
        box.addView(prediction);

        TextView close = new TextView(this);
        close.setText("   MINIMALKAN       TUTUP");
        close.setTextColor(Color.WHITE);
        close.setPadding(4, 14, 4, 4);
        box.addView(close);

        close.setOnClickListener(v -> stopSelf());

        header.setOnTouchListener(new View.OnTouchListener() {
            int startX, startY;
            float touchX, touchY;

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    startX = params.x;
                    startY = params.y;
                    touchX = e.getRawX();
                    touchY = e.getRawY();
                    return true;
                } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    params.x = startX + (int) (e.getRawX() - touchX);
                    params.y = startY + (int) (e.getRawY() - touchY);
                    wm.updateViewLayout(panel, params);
                    return true;
                }
                return true;
            }
        });

        int type = Build.VERSION.SDK_INT >= 26
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 24;
        params.y = 120;

        panel = box;
        wm.addView(panel, params);
    }

    @Override
    public void onDestroy() {
        if (wm != null && panel != null) {
            try {
                wm.removeView(panel);
            } catch (Exception ignored) {}
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
