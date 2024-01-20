package com.example.budzik;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AlarmActionReceiver extends BroadcastReceiver implements SensorEventListener {
    private Context context;
    public interface MediaPlayerCallback {
        void stopMediaPlayer();
    }

    private static MediaPlayerCallback mediaPlayerCallback;
    private static final float SHAKE_THRESHOLD = 10.0f;
    private String alarmTime;

    public void setMediaPlayerCallback(MediaPlayerCallback callback) {
        mediaPlayerCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String alarmTime = intent.getStringExtra("ALARM_TIME");

        if (action != null && alarmTime != null) {
            switch (action) {
                case "SNOOZE_ACTION":
                    handleSnooze(context, alarmTime);
                    break;

                case "DISMISS_ACTION":
                    handleDismiss(context, alarmTime);
                    break;

                case "SHAKE_ACTION":
                    handleShake(context, alarmTime);
                    break;
            }
        }
    }

    private void handleSnooze(Context context, String alarmTime) {

        Toast.makeText(context, "Alarm z godziny " + alarmTime + " zostanie przesunięty o 5 minut.", Toast.LENGTH_SHORT).show();
    }

    private void handleDismiss(Context context, String alarmTime) {
        Toast.makeText(context, "Alarm został wyłączony", Toast.LENGTH_SHORT).show();

        if (mediaPlayerCallback != null) {
            mediaPlayerCallback.stopMediaPlayer();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
    }

    private void handleShake(Context context, String alarmTime) {

        Toast.makeText(context, "Potrząśnięcie telefonem. Alarm o " + alarmTime + " zostanie wyłączony.", Toast.LENGTH_SHORT).show();

        if (mediaPlayerCallback != null) {
            mediaPlayerCallback.stopMediaPlayer();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];


            if ((Math.abs(x) + Math.abs(y) + Math.abs(z)) > SHAKE_THRESHOLD) {
                Intent shakeIntent = new Intent(context, AlarmActionReceiver.class);
                shakeIntent.setAction("SHAKE_ACTION");
                shakeIntent.putExtra("ALARM_TIME", alarmTime);
                context.sendBroadcast(shakeIntent);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Obsługa zmiany dokładności czujnika (opcjonalne)
    }
}
