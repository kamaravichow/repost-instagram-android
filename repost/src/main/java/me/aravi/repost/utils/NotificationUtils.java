/*
 * Copyright (c) 2021. Aravind Chowdary
 */

package me.aravi.repost.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import me.aravi.repost.R;


public class NotificationUtils {
    private static String TAG = NotificationUtils.class.getSimpleName();
    private Context mContext;
    private int count;

    public NotificationUtils(Context mContext) {
        count = 0;
        this.mContext = mContext;
    }

    public void showNotification(int id, String groupId, String title, String message, boolean ongoing, PendingIntent pendingIntent) {
        count++;
        NotificationCompat.Builder mBuilder;
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);

        mBuilder = new NotificationCompat.Builder(mContext, "Repost");

        Notification notification;
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);

        if (pendingIntent != null) {
            if (!TextUtils.isEmpty(groupId)) {
                notification = mBuilder
                        .setAutoCancel(true)
                        .setOngoing(ongoing)
                        .setContentTitle(title)
                        .setGroup(groupId)
                        .setContentText(message)
                        .setTicker(title)
                        .setStyle(bigTextStyle)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_baseline_refresh_24)
                        .build();
            } else {
                notification = mBuilder
                        .setAutoCancel(true)
                        .setOngoing(ongoing)
                        .setContentTitle(title)
                        .setStyle(bigTextStyle)
                        .setContentText(message)
                        .setTicker(title)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_baseline_refresh_24)
                        .build();
            }
        } else {
            if (!TextUtils.isEmpty(groupId)) {
                notification = mBuilder
                        .setOngoing(ongoing)
                        .setContentTitle(title)
                        .setStyle(bigTextStyle)
                        .setContentText(message)
                        .setTicker(title)
                        .setSmallIcon(R.drawable.ic_baseline_refresh_24)
                        .build();
            } else {
                notification = mBuilder
                        .setOngoing(ongoing)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(bigTextStyle)
                        .setTicker(title)
                        .setGroup(groupId)
                        .setSmallIcon(R.drawable.ic_baseline_refresh_24)
                        .build();
            }
        }


        notificationManagerCompat.notify(id, notification);

    }

}
