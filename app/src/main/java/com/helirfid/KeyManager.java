/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KeyManager {

    private static final String KEY_DIR = "HeliRFID/keys";

    public static File getKeyDir(Context context) {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), KEY_DIR);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static List<String> getKeyFileNames(Context context) {
        List<String> names = new ArrayList<>();
        File[] files = getKeyDir(context).listFiles((d, name) -> name.endsWith(".keys"));
        if (files != null) {
            for (File f : files) {
                names.add(f.getName());
            }
        }
        return names;
    }

    public static List<byte[]> loadKeys(Context context, String fileName) {
        List<byte[]> keys = new ArrayList<>();
        File file = new File(getKeyDir(context), fileName);
        if (!file.exists()) return keys;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String hex = line.replace(" ", "");
                if (hex.length() == 12) {
                    byte[] key = new byte[6];
                    for (int i = 0; i < 6; i++) {
                        key[i] = (byte) ((Character.digit(hex.charAt(i*2), 16) << 4)
                                + Character.digit(hex.charAt(i*2+1), 16));
                    }
                    keys.add(key);
                }
            }
        } catch (IOException e) {
            LogUtil.e("KeyManager", "Load failed: " + e.getMessage());
        }
        return keys;
    }

    public static boolean saveKeys(Context context, String fileName, List<String> keyHexLines) {
        File file = new File(getKeyDir(context), fileName);
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("# HeliRFID Key File\n");
            fw.write("# Format: one 6-byte key per line, hex (12 chars), spaces ignored\n");
            fw.write("# Lines starting with # are comments\n\n");
            for (String line : keyHexLines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String clean = line.replace(" ", "").toUpperCase();
                    if (clean.length() == 12) {
                        fw.write(clean + "\n");
                    }
                }
            }
            return true;
        } catch (IOException e) {
            LogUtil.e("KeyManager", "Save failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteKeyFile(Context context, String fileName) {
        File file = new File(getKeyDir(context), fileName);
        return file.delete();
    }

    /** Find key files whose name starts with the given UID prefix (e.g. "A1B2C3D4") */
    public static List<String> getUidPrefixFiles(Context context, String uidHex) {
        List<String> matches = new ArrayList<>();
        if (uidHex == null || uidHex.length() < 4) return matches;
        String prefix = uidHex.substring(0, 4).toUpperCase();
        File[] files = getKeyDir(context).listFiles((d, name) -> name.endsWith(".keys"));
        if (files != null) {
            for (File f : files) {
                if (f.getName().toUpperCase().startsWith(prefix)) {
                    matches.add(f.getName());
                }
            }
        }
        return matches;
    }

    /** Load keys from all files matching the given UID prefix */
    public static List<byte[]> findKeysByUid(Context context, String uidHex) {
        List<byte[]> allKeys = new ArrayList<>();
        List<String> files = getUidPrefixFiles(context, uidHex);
        for (String fileName : files) {
            allKeys.addAll(loadKeys(context, fileName));
        }
        return allKeys;
    }
}
