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
import android.provider.Settings;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static MediaPlayer mediaPlayer;
    private static AlarmActionReceiver alarmActionReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmTime = intent.getStringExtra("ALARM_TIME");

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

            if (notificationManager != null)
                notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Budzik")
                .setContentText("Czas na budzenie! Godzina: " + alarmTime)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(false)
                .setOngoing(true)

                // Ustaw dźwięk powiadomienia
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)

                // Dodaj akcję dla przycisku Drzemka
                .addAction(R.drawable.baseline_add_24, "Drzemka", snoozePendingIntent)

                // Dodaj akcję dla przycisku Wyłącz
                .addAction(R.drawable.baseline_add_24, "Wyłącz", dismissPendingIntent);

        notificationManager.notify(1, builder.build());
    }
}
