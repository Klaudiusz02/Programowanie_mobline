package com.example.budzik;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmActionReceiver extends BroadcastReceiver {

    public interface MediaPlayerCallback {
        void stopMediaPlayer();
    }

    private static MediaPlayerCallback mediaPlayerCallback;

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
            }
        }
    }

    private void handleSnooze(Context context, String alarmTime) {
        // Tutaj dodaj kod obsługujący akcję Drzemka
        Toast.makeText(context, "Akcja Drzemka. Alarm o " + alarmTime + " zostanie przesunięty.", Toast.LENGTH_SHORT).show();
        // Dodaj dodatkową logikę związana z obsługą akcji Drzemka
    }

    private void handleDismiss(Context context, String alarmTime) {
        Toast.makeText(context, "Alarm został wyłączony", Toast.LENGTH_SHORT).show();

        // Dodaj dodatkową logikę związaną z obsługą akcji Wyłącz

        // Check if the callback is set
        if (mediaPlayerCallback != null) {
            // Invoke the callback to stop the MediaPlayer
            mediaPlayerCallback.stopMediaPlayer();
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Anuluj powiadomienie o określonym ID (1 w tym przypadku)
            notificationManager.cancel(1);

        }
    }
}