
package com.example.budzik;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String alarmTime = intent.getStringExtra("ALARM_TIME");

        // Intenty dla akcji przycisków
        Intent snoozeIntent = new Intent(context, AlarmActionReceiver.class);
        snoozeIntent.setAction("SNOOZE_ACTION");
        snoozeIntent.putExtra("ALARM_TIME", alarmTime);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissIntent = new Intent(context, AlarmActionReceiver.class);
        dismissIntent.setAction("DISMISS_ACTION");
        dismissIntent.putExtra("ALARM_TIME", alarmTime);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        // Utwórz powiadomienie
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String CHANNEL_ID = "YOUR_CHANNEL_ID";
        CharSequence name = "YOUR_CHANNEL_NAME";
        String description = "YOUR_CHANNEL_DESCRIPTION";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/raw/alarm.mp3");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();


            mChannel.setDescription(description);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setSound(soundUri, attributes); // Ustaw niestandardowy dźwięk

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
                .setSound(soundUri)

                // Dodaj akcję dla przycisku Drzemka
                .addAction(R.drawable.baseline_add_24, "Drzemka", snoozePendingIntent)

                // Dodaj akcję dla przycisku Wyłącz
                .addAction(R.drawable.baseline_add_24, "Wyłącz", dismissPendingIntent);

        notificationManager.notify(1, builder.build());

    }
}