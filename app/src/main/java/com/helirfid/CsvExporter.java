package com.helirfid;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.*;
import java.util.List;

public class CsvExporter {

    public static boolean export(Context context, List<String> history) {
        try {
            String fileName = "HeliRFID_History.csv";
            String csvContent = generateContent(history);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return exportWithMediaStore(context, fileName, csvContent);
            } else {
                return exportLegacy(context, fileName, csvContent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String generateContent(List<String> history) {
        StringBuilder content = new StringBuilder();
        content.append("Card Number\n");
        for (String card : history) {
            content.append(card).append("\n");
        }
        return content.toString();
    }

    private static boolean exportWithMediaStore(Context context, String fileName, String csvContent) {
        try {
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            android.net.Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                return false;
            }

            try (OutputStream outputStream = resolver.openOutputStream(uri);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                writer.write(csvContent);
                writer.flush();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean exportLegacy(Context context, String fileName, String csvContent) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, fileName);

            if (!path.exists()) {
                path.mkdirs();
            }

            try (FileWriter fw = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(csvContent);
                bw.flush();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
