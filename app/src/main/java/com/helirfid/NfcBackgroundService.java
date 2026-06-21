package com.helirfid;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NfcBackgroundService extends Service {

    private static final String CHANNEL_ID = "nfc_background";
    private static final int NOTIF_ID = 1001;

    private NfcAdapter nfcAdapter;
    private boolean monitoring = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NFC 背景監控中")
                .setContentText("感應到 NFC 標籤時將自動記錄")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        startForeground(NOTIF_ID, notification);
        monitoring = true;
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    public static void saveLogToCsv(Context context, String uid, String time) {
        try {
            File dir = new File(context.getExternalFilesDir(null), "nfc_logs");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "nfc_log_" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()) + ".csv");
            boolean exists = file.exists();
            FileOutputStream fos = new FileOutputStream(file, true);
            if (!exists) {
                fos.write("時間,UID,技術\n".getBytes(StandardCharsets.UTF_8));
            }
            fos.write((time + "," + uid + ",").getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (Exception ignored) {}
    }

    private void updateNotification(String text) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("NFC 背景監控中")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "NFC 背景監控", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("NFC 背景監控服務通知");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        monitoring = false;
        super.onDestroy();
    }
}
