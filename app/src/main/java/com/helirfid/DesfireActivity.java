package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DesfireActivity extends BaseNfcActivity {

    TextView txtInfo, txtResult;
    Button btnGetInfo, btnListApps, btnSelect, btnKeyAuth, btnReadFile;
    EditText editAid, editKeyNo, editFileNo;
    LinearLayout layoutAuth, layoutFile;

    private Tag currentTag;
    private IsoDep isoDep;
    private Desfire desfire;
    private boolean connected = false;
    private List<String> resultLines = new ArrayList<>();
    private byte[] defaultKey = new byte[8];

    private int selectedAid = -1;
    private List<Integer> aidList = new ArrayList<>();
    private List<Integer> fileIdList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desfire);

        txtInfo = findViewById(R.id.txtDesfireInfo);
        txtResult = findViewById(R.id.txtDesfireResult);
        btnGetInfo = findViewById(R.id.btnDesfireGetInfo);
        btnListApps = findViewById(R.id.btnDesfireListApps);
        btnSelect = findViewById(R.id.btnDesfireSelect);
        btnKeyAuth = findViewById(R.id.btnDesfireKeyAuth);
        btnReadFile = findViewById(R.id.btnDesfireReadFile);
        editAid = findViewById(R.id.editDesfireAid);
        editKeyNo = findViewById(R.id.editDesfireKeyNo);
        editFileNo = findViewById(R.id.editDesfireFileNo);
        layoutAuth = findViewById(R.id.layoutDesfireAuth);
        layoutFile = findViewById(R.id.layoutDesfireFile);

        layoutAuth.setVisibility(View.GONE);
        layoutFile.setVisibility(View.GONE);

        btnGetInfo.setOnClickListener(v -> doGetInfo());
        btnListApps.setOnClickListener(v -> doListApps());
        btnSelect.setOnClickListener(v -> doSelectApp());
        btnKeyAuth.setOnClickListener(v -> doKeyAuth());
        btnReadFile.setOnClickListener(v -> doReadFile());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        currentTag = tag;
        isoDep = IsoDep.get(tag);
        if (isoDep == null) {
            txtInfo.setText("不支援 IsoDep — 非 DESFire 卡片");
            return;
        }

        String uid = Converter.hex(tag.getId());
        StringBuilder sb = new StringBuilder("DESFire 卡片已偵測\nUID: ").append(uid);
        for (String t : tag.getTechList()) {
            sb.append("\n  ").append(t.substring(t.lastIndexOf('.') + 1));
        }
        txtInfo.setText(sb.toString());
        txtResult.setText("連線中...");

        new Thread(() -> {
            try {
                isoDep.connect();
                isoDep.setTimeout(5000);
                connected = true;
                desfire = new Desfire(isoDep);
                runOnUiThread(() -> txtResult.setText("已連線，請選擇操作"));
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText("連線失敗: " + e.getMessage()));
            }
        }).start();
    }

    private void doGetInfo() {
        if (!checkReady()) return;
        resultLines.clear();
        layoutAuth.setVisibility(View.GONE);
        layoutFile.setVisibility(View.GONE);

        new Thread(() -> {
            try {
                Desfire.DesfireVersion ver = desfire.getVersion();
                if (ver == null) {
                    addLine("無法讀取版本資訊");
                    return;
                }
                addLine("=== DESFire 卡片資訊 ===");
                addLine("硬體: 廠商 0x" + String.format("%02X", ver.hardwareVendor)
                        + " 型號 " + ver.getHardwareTypeName());
                addLine("硬體版本: " + ver.getHardwareName()
                        + " 儲存 " + ver.getStorageName());
                addLine("軟體版本: " + ver.getSoftwareName());
                addLine("UID: " + ver.getUidString());

                int ks = desfire.getKeySettings();
                if (ks >= 0) {
                    addLine("金鑰設定: 0x" + String.format("%04X", ks));
                }

                int[] aids = desfire.getApplicationIDs();
                if (aids != null && aids.length > 0) {
                    addLine("應用程式 (" + aids.length + " 個):");
                    aidList.clear();
                    for (int a : aids) {
                        addLine("  AID: " + String.format("%06X", a));
                        aidList.add(a);
                    }
                } else {
                    addLine("無應用程式（卡片未格式化）");
                }

                refreshResult();
            } catch (Exception e) {
                addLine("錯誤: " + e.getMessage());
                refreshResult();
            }
        }).start();
    }

    private void doListApps() {
        if (!checkReady()) return;
        resultLines.clear();

        new Thread(() -> {
            try {
                int[] aids = desfire.getApplicationIDs();
                if (aids == null || aids.length == 0) {
                    addLine("無應用程式");
                } else {
                    addLine("=== 應用程式清單 (" + aids.length + ") ===");
                    aidList.clear();
                    for (int a : aids) {
                        addLine("AID: " + String.format("%06X", a));
                        aidList.add(a);
                    }
                    addLine("在 AID 輸入欄填入後按「選擇應用」");
                }
                refreshResult();
            } catch (Exception e) {
                addLine("錯誤: " + e.getMessage());
                refreshResult();
            }
        }).start();
    }

    private void doSelectApp() {
        if (!checkReady()) return;
        layoutFile.setVisibility(View.GONE);

        String aidStr = editAid.getText().toString().trim();
        if (aidStr.isEmpty()) {
            Toast.makeText(this, "請輸入 AID (6位 hex)", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            selectedAid = Integer.parseInt(aidStr, 16);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "AID 格式錯誤", Toast.LENGTH_SHORT).show();
            return;
        }

        resultLines.clear();
        new Thread(() -> {
            try {
                if (selectedAid == 0) {
                    desfire.selectPICC();
                    addLine("已選擇 PICC 層級");
                } else {
                    if (desfire.selectApplication(selectedAid)) {
                        addLine("已選擇應用: " + aidStr.toUpperCase());
                        int[] fids = desfire.getFileIDs();
                        if (fids != null && fids.length > 0) {
                            addLine("檔案 (" + fids.length + " 個):");
                            fileIdList.clear();
                            for (int fid : fids) {
                                addLine("  檔案 " + fid);
                                fileIdList.add(fid);
                            }
                        } else {
                            addLine("無檔案");
                        }
                        runOnUiThread(() -> {
                            layoutAuth.setVisibility(View.VISIBLE);
                            layoutFile.setVisibility(View.VISIBLE);
                        });
                    } else {
                        addLine("選擇應用失敗");
                    }
                }
                refreshResult();
            } catch (Exception e) {
                addLine("錯誤: " + e.getMessage());
                refreshResult();
            }
        }).start();
    }

    private void doKeyAuth() {
        if (!checkReady()) return;
        String keyNoStr = editKeyNo.getText().toString().trim();
        int keyNo = 0;
        if (!keyNoStr.isEmpty()) {
            try { keyNo = Integer.parseInt(keyNoStr); } catch (NumberFormatException ignored) {}
        }

        final int fKeyNo = keyNo;
        resultLines.clear();
        new Thread(() -> {
            try {
                addLine("認證中 (金鑰 " + fKeyNo + ")...");
                boolean ok = desfire.authenticateLegacy(defaultKey, fKeyNo);
                if (ok) {
                    addLine("✓ 認證成功 (Key " + fKeyNo + ")");
                } else {
                    addLine("✗ 認證失敗（可能需 AES 或非預設金鑰）");
                }
                refreshResult();
            } catch (Exception e) {
                addLine("錯誤: " + e.getMessage());
                refreshResult();
            }
        }).start();
    }

    private void doReadFile() {
        if (!checkReady()) return;
        String fileNoStr = editFileNo.getText().toString().trim();
        if (fileNoStr.isEmpty()) {
            Toast.makeText(this, "請輸入檔案編號", Toast.LENGTH_SHORT).show();
            return;
        }
        int fileNo;
        try { fileNo = Integer.parseInt(fileNoStr); } catch (NumberFormatException e) {
            Toast.makeText(this, "檔案編號格式錯誤", Toast.LENGTH_SHORT).show();
            return;
        }

        final int fFileNo = fileNo;
        resultLines.clear();
        new Thread(() -> {
            try {
                Desfire.DesfireFileSettings fs = desfire.getFileSettings(fFileNo);
                if (fs != null) {
                    addLine("=== 檔案 " + fFileNo + " 設定 ===");
                    addLine("類型: " + fs.getFileTypeName());
                    addLine("通訊模式: " + fs.getCommModeName());
                    addLine("讀取金鑰: " + fs.readKey + " 寫入金鑰: " + fs.writeKey);
                    if (fs.fileSize > 0) addLine("大小: " + fs.fileSize + " bytes");
                }

                byte[] data = desfire.readDataPlain(fFileNo, 0, 48);
                if (data != null && data.length > 0) {
                    addLine("資料 (" + data.length + " bytes):");
                    StringBuilder hex = new StringBuilder();
                    StringBuilder ascii = new StringBuilder();
                    for (int i = 0; i < data.length; i++) {
                        hex.append(String.format("%02X ", data[i] & 0xFF));
                        ascii.append((data[i] >= 32 && data[i] < 127) ? (char) data[i] : '.');
                        if ((i + 1) % 16 == 0 || i == data.length - 1) {
                            addLine(String.format("  %04X: ", i / 16 * 16) + hex + " " + ascii);
                            hex = new StringBuilder();
                            ascii = new StringBuilder();
                        }
                    }
                } else {
                    addLine("無資料或需先認證");
                }
                refreshResult();
            } catch (Exception e) {
                addLine("錯誤: " + e.getMessage());
                refreshResult();
            }
        }).start();
    }

    private boolean checkReady() {
        if (isoDep == null || !connected) {
            Toast.makeText(this, "請先掃描 DESFire 卡片", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void addLine(String line) {
        synchronized (resultLines) {
            resultLines.add(line);
        }
    }

    private void refreshResult() {
        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder();
            synchronized (resultLines) {
                for (String line : resultLines) {
                    sb.append(line).append("\n");
                }
            }
            txtResult.setText(sb.toString());
        });
    }
}
