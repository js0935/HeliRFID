/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Acr122uActivity extends AppCompatActivity {

    private UsbManager usbManager;
    private Acr122uManager reader;
    private PendingIntent permissionIntent;

    private static final String ACTION_USB_PERMISSION =
            "com.helirfid.USB_PERMISSION";

    TextView txtAcrInfo, txtAcrResult;
    EditText editAcrKeyA, editAcrSector;
    Button btnConnect, btnGetUid, btnReadSector, btnFullDump, btnDisconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acr122u);

        txtAcrInfo = findViewById(R.id.txtAcrInfo);
        txtAcrResult = findViewById(R.id.txtAcrResult);
        editAcrKeyA = findViewById(R.id.editAcrKeyA);
        editAcrSector = findViewById(R.id.editAcrSector);
        btnConnect = findViewById(R.id.btnAcrConnect);
        btnGetUid = findViewById(R.id.btnAcrGetUid);
        btnReadSector = findViewById(R.id.btnAcrReadSector);
        btnFullDump = findViewById(R.id.btnAcrFullDump);
        btnDisconnect = findViewById(R.id.btnAcrDisconnect);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        reader = new Acr122uManager();

        permissionIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_MUTABLE
                        : PendingIntent.FLAG_UPDATE_CURRENT);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);

        btnConnect.setOnClickListener(v -> connectReader());
        btnDisconnect.setOnClickListener(v -> disconnectReader());
        btnGetUid.setOnClickListener(v -> getUid());
        btnReadSector.setOnClickListener(v -> readSector());
        btnFullDump.setOnClickListener(v -> fullDump());
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            if (reader.connect(usbManager, device)) {
                                txtAcrInfo.setText("已連接 ACR122U 讀卡器");
                                appendResult("連線成功");
                                setButtonsEnabled(true);
                            } else {
                                txtAcrInfo.setText("連線失敗");
                            }
                        }
                    } else {
                        appendResult("使用者拒絕 USB 權限");
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                appendResult("USB 裝置已插入");
                connectReader();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                appendResult("USB 裝置已移除");
                disconnectReader();
            }
        }
    };

    private void connectReader() {
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (device.getVendorId() == Acr122uManager.VENDOR_ID
                    && device.getProductId() == Acr122uManager.PRODUCT_ID) {
                if (!usbManager.hasPermission(device)) {
                    usbManager.requestPermission(device, permissionIntent);
                    return;
                }
                if (reader.connect(usbManager, device)) {
                    txtAcrInfo.setText("已連接 ACR122U 讀卡器");
                    appendResult("韌體版本: " + getFirmwareString());
                    setButtonsEnabled(true);
                } else {
                    txtAcrInfo.setText("連線失敗");
                    appendResult("連線失敗");
                }
                return;
            }
        }
        Toast.makeText(this, "未找到 ACR122U 裝置", Toast.LENGTH_SHORT).show();
    }

    private String getFirmwareString() {
        byte[] fw = reader.getFirmwareVersion();
        if (fw != null) {
            StringBuilder sb = new StringBuilder();
            for (byte b : fw) {
                if (b >= 0x20 && b <= 0x7E) sb.append((char) b);
                else sb.append(String.format("%02X", b));
            }
            return sb.toString();
        }
        return "不明";
    }

    private void disconnectReader() {
        reader.disconnect();
        txtAcrInfo.setText("已斷開連線");
        setButtonsEnabled(false);
        appendResult("讀卡器已斷開");
    }

    private void setButtonsEnabled(boolean connected) {
        btnGetUid.setEnabled(connected);
        btnReadSector.setEnabled(connected);
        btnFullDump.setEnabled(connected);
        btnDisconnect.setEnabled(connected);
        btnConnect.setEnabled(!connected);
    }

    private void getUid() {
        new Thread(() -> {
            byte[] uid = reader.getUid();
            if (uid != null) {
                StringBuilder sb = new StringBuilder("UID: ");
                for (byte b : uid) sb.append(String.format("%02X:", b));
                final String uidStr = sb.substring(0, sb.length() - 1);
                runOnUiThread(() -> appendResult("UID: " + uidStr));
            } else {
                runOnUiThread(() -> appendResult("讀取 UID 失敗 (請將卡片靠近讀卡器)"));
            }
        }).start();
    }

    private void readSector() {
        int sector;
        try {
            sector = Integer.parseInt(editAcrSector.getText().toString().trim());
        } catch (Exception e) {
            Toast.makeText(this, "請輸入 Sector 編號", Toast.LENGTH_SHORT).show();
            return;
        }

        String keyStr = editAcrKeyA.getText().toString().trim().replace(" ", "");
        if (keyStr.length() != 12) keyStr = "FFFFFFFFFFFF";

        final int fSector = sector;
        final byte[] key = hexToBytes(keyStr);

        new Thread(() -> {
            try {
                if (!reader.loadKey(key)) {
                    runOnUiThread(() -> appendResult("載入金鑰失敗"));
                    return;
                }

                int sectorStartBlock = fSector * 4;
                int blockCount = fSector < 32 ? 4 : 16;
                StringBuilder sb = new StringBuilder("Sector " + fSector + ":\n");

                for (int b = 0; b < blockCount; b++) {
                    int absBlock = sectorStartBlock + b;
                    if (!reader.authenticate(absBlock, true)) {
                        sb.append(String.format("  [%03d] 認證失敗\n", absBlock));
                        continue;
                    }
                    byte[] data = reader.readBlock(absBlock);
                    if (data != null) {
                        sb.append(String.format("  [%03d] ", absBlock));
                        for (byte d : data) sb.append(String.format("%02X ", d));
                        sb.append("\n");
                    } else {
                        sb.append(String.format("  [%03d] 讀取失敗\n", absBlock));
                    }
                }

                final String result = sb.toString();
                runOnUiThread(() -> appendResult(result));

            } catch (Exception e) {
                runOnUiThread(() -> appendResult("錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void fullDump() {
        new Thread(() -> {
            runOnUiThread(() -> appendResult("開始完整轉儲... (請稍候)"));

            String keyStr = editAcrKeyA.getText().toString().trim().replace(" ", "");
            if (keyStr.length() != 12) keyStr = "FFFFFFFFFFFF";
            final byte[] key = hexToBytes(keyStr);

            if (!reader.loadKey(key)) {
                runOnUiThread(() -> appendResult("載入金鑰失敗"));
                return;
            }

            StringBuilder dump = new StringBuilder();
            int totalSectors = 16;

            for (int s = 0; s < totalSectors; s++) {
                int baseBlock = s * 4;
                int blockCount = s < 32 ? 4 : 16;

                dump.append("+ Sector ").append(s).append("\n");

                for (int b = 0; b < blockCount; b++) {
                    int absBlock = baseBlock + b;
                    if (b == 0 || b == blockCount - 1) {
                        if (!reader.authenticate(absBlock, true)) {
                            dump.append(String.format("%03d: 認證失敗\n", absBlock));
                            continue;
                        }
                    }
                    byte[] data = reader.readBlock(absBlock);
                    if (data != null) {
                        dump.append(String.format("%03d: ", absBlock));
                        for (byte d : data) dump.append(String.format("%02X ", d));
                        dump.append("\n");
                    } else {
                        dump.append(String.format("%03d: 讀取失敗\n", absBlock));
                    }
                }
            }

            final String dumpResult = dump.toString();
            runOnUiThread(() -> {
                txtAcrResult.setText(dumpResult);
                appendResult("轉儲完成 (16 sectors)");
            });

        }).start();
    }

    private void appendResult(String text) {
        String current = txtAcrResult.getText().toString();
        txtAcrResult.setText(text + "\n" + current);
    }

    private byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        return data;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(usbReceiver);
        } catch (Exception e) { }
        reader.disconnect();
    }
}
