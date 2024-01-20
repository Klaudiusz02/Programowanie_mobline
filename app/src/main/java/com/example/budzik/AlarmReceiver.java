package com.example.budzik;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;
    private static AlarmActionReceiver alarmActionReceiver;
    private static String currentAlarmTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmTime = intent.getStringExtra("ALARM_TIME");
        currentAlarmTime = alarmTime;

        // Inicjalizuj AlarmActionReceiver tylko raz
        if (alarmActionReceiver == null) {
            alarmActionReceiver = new AlarmActionReceiver();
        }

        // Przekazanie referencji do AlarmReceiver
        alarmActionReceiver.setMediaPlayerCallback(new AlarmActionReceiver.MediaPlayerCallback() {
            @Override
            public void stopMediaPlayer() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        });

        // Intenty dla akcji przycisków
        Intent snoozeIntent = new Intent(context, AlarmActionReceiver.class);
        snoozeIntent.setAction("SNOOZE_ACTION");
        snoozeIntent.putExtra("ALARM_TIME", alarmTime);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(context, AlarmActionReceiver.class);
        dismissIntent.setAction("DISMISS_ACTION");
        dismissIntent.putExtra("ALARM_TIME", alarmTime);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Odtwórz dźwięk w trybie pętli
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.alarm);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        // Utwórz powiadomienie
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "YOUR_CHANNEL_ID";
        CharSequence name = "YOUR_CHANNEL_NAME";
        String description = "YOUR_CHANNEL_DESCRIPTION";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, attributes);

            // Dodaj ustawienie, które pozwala na wybudzenie urządzenia przy nowym powiadomieniu
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            if (notificationManager != null)
                notificationManager.createNotificationChannel(mChannel);
        }

        // Utwórz wake lock, aby rozświetlić ekran
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = null;
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE, "AlarmReceiver::WakeLockTag"
            );
            wakeLock.acquire(10 * 60 * 1000L /* 10 minutes */);
        }
        long[] vibrationPattern = {0, 2000, 1000, 2000};

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("WakeUpCall")
                .setContentText("Czas na budzenie! Godzina: " + alarmTime)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(false)
                .setOngoing(true)
                .setVibrate(vibrationPattern)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .addAction(R.drawable.baseline_add_24, "Drzemka", snoozePendingIntent)
                .addAction(R.drawable.baseline_add_24, "Wyłącz", dismissPendingIntent);

        notificationManager.notify(1, builder.build());

        // Zwolnij wake lock po wyświetleniu powiadomienia
        if (wakeLock != null) {
            wakeLock.release();
        }
    }
}
