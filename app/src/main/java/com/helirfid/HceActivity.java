package com.helirfid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HceActivity extends BaseNfcActivity {

    private static final String PREFS_NAME = "hce_profiles";
    private static final String KEY_LIST = "profile_list";

    private TextView txtScanStatus, txtSimStatus;
    private EditText editCardName;
    private Button btnSave, btnStartSim, btnStopSim, btnClear;
    private ListView listSavedCards;

    private Tag currentTag;
    private String lastScannedUid = "";
    private String lastScannedAtqa = "";
    private String lastScannedSak = "";
    private String lastScannedTech = "";

    private List<HceCardProfile> profiles = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private List<String> listDisplay = new ArrayList<>();

    private HceCardProfile selectedProfile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hce);

        txtScanStatus = findViewById(R.id.txtHceScanStatus);
        txtSimStatus = findViewById(R.id.txtHceSimStatus);
        editCardName = findViewById(R.id.editHceCardName);
        btnSave = findViewById(R.id.btnHceSave);
        btnStartSim = findViewById(R.id.btnHceStart);
        btnStopSim = findViewById(R.id.btnHceStop);
        btnClear = findViewById(R.id.btnHceClear);
        listSavedCards = findViewById(R.id.listHceCards);

        txtScanStatus.setText("請將 NFC 卡片靠近手機背面");
        txtSimStatus.setText("未選擇模擬卡片");

        btnSave.setEnabled(false);

        loadProfiles();

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listDisplay);
        listSavedCards.setAdapter(listAdapter);

        btnSave.setOnClickListener(v -> saveCard());

        btnStartSim.setOnClickListener(v -> startSimulation());

        btnStopSim.setOnClickListener(v -> stopSimulation());

        btnClear.setOnClickListener(v -> {
            profiles.clear();
            saveProfileList();
            refreshList();
            txtSimStatus.setText("未選擇模擬卡片");
            selectedProfile = null;
            stopSimulation();
            Toast.makeText(this, "已清除所有卡片", Toast.LENGTH_SHORT).show();
        });

        listSavedCards.setOnItemClickListener((parent, view, position, id) -> {
            if (position < profiles.size()) {
                selectedProfile = profiles.get(position);
                txtSimStatus.setText("已選擇: " + selectedProfile.getName()
                        + "\nUID: " + selectedProfile.getUid()
                        + "\n按「啟動模擬」開始模擬");
            }
        });

        listSavedCards.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < profiles.size()) {
                HceCardProfile p = profiles.get(position);
                profiles.remove(position);
                saveProfileList();
                refreshList();
                if (selectedProfile == p) {
                    selectedProfile = null;
                    txtSimStatus.setText("未選擇模擬卡片");
                }
                Toast.makeText(this, "已刪除: " + p.getName(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        loadActiveSimState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        currentTag = tag;
        byte[] uidBytes = tag.getId();
        lastScannedUid = Converter.hex(uidBytes);

        StringBuilder techBuilder = new StringBuilder();
        String[] techList = tag.getTechList();
        if (techList != null) {
            for (String t : techList) {
                String shortName = t.substring(t.lastIndexOf('.') + 1);
                if (techBuilder.length() > 0) techBuilder.append(", ");
                techBuilder.append(shortName);
            }
        }
        lastScannedTech = techBuilder.toString();

        lastScannedAtqa = "";
        lastScannedSak = "";

        NfcA nfcA = NfcA.get(tag);
        if (nfcA != null) {
            try {
                nfcA.connect();
                byte[] atqa = nfcA.getAtqa();
                lastScannedAtqa = Converter.bytesToHex(atqa);
                lastScannedSak = String.format("%02X", nfcA.getSak() & 0xFF);
                nfcA.close();
            } catch (Exception ignored) {}
        }

        if (lastScannedAtqa.isEmpty()) {
            NfcB nfcB = NfcB.get(tag);
            if (nfcB != null) {
                try {
                    nfcB.connect();
                    byte[] appData = nfcB.getApplicationData();
                    lastScannedAtqa = (appData != null) ? Converter.bytesToHex(appData) : "(NFC-B)";
                    nfcB.close();
                } catch (Exception ignored) {}
            }
        }

        String displayInfo = "UID: " + lastScannedUid + "\n"
                + "技術: " + lastScannedTech + "\n"
                + "ATQA: " + lastScannedAtqa + "\n"
                + "SAK: " + lastScannedSak + "\n\n"
                + "輸入名稱後按「儲存卡片」";

        txtScanStatus.setText(displayInfo);
        editCardName.setText("");
        editCardName.setHint("輸入卡片名稱...");
        btnSave.setEnabled(true);
    }

    private void saveCard() {
        if (lastScannedUid.isEmpty()) {
            Toast.makeText(this, "請先掃描卡片", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = editCardName.getText().toString().trim();
        if (name.isEmpty()) {
            name = "卡片_" + lastScannedUid.substring(0, Math.min(8, lastScannedUid.length()));
        }

        String id = UUID.randomUUID().toString().substring(0, 8);
        HceCardProfile profile = new HceCardProfile(id, name, lastScannedUid,
                lastScannedAtqa, lastScannedSak, lastScannedTech, System.currentTimeMillis());

        profiles.add(0, profile);
        saveProfileList();
        refreshList();

        txtScanStatus.setText("已儲存: " + name + "\nUID: " + lastScannedUid
                + "\n\n請將 NFC 卡片靠近手機背面以掃描新卡片");
        editCardName.setText("");
        btnSave.setEnabled(false);
        lastScannedUid = "";

        Toast.makeText(this, "已儲存卡片: " + name, Toast.LENGTH_SHORT).show();
    }

    private void startSimulation() {
        if (selectedProfile == null) {
            Toast.makeText(this, "請先從清單選擇一張卡片", Toast.LENGTH_SHORT).show();
            return;
        }

        HceSimulationService.setActiveProfile(this, selectedProfile);

        Intent serviceIntent = new Intent(this, HceSimulationService.class);
        serviceIntent.putExtra("profile_data", true);
        serviceIntent.putExtra("profile_uid", selectedProfile.getUid());
        serviceIntent.putExtra("profile_name", selectedProfile.getName());
        startService(serviceIntent);

        txtSimStatus.setText("正在模擬: " + selectedProfile.getName()
                + "\nUID: " + selectedProfile.getUid()
                + "\n\n請將另一台裝置靠近本機背面");
        txtSimStatus.setTextColor(0xFF4CAF50);
        btnStartSim.setEnabled(false);

        Toast.makeText(this, "HCE 模擬已啟動: " + selectedProfile.getName(), Toast.LENGTH_LONG).show();
    }

    private void stopSimulation() {
        try {
            stopService(new Intent(this, HceSimulationService.class));
        } catch (Exception ignored) {}

        HceSimulationService.setActiveProfile(this, null);

        txtSimStatus.setText("模擬已停止");
        txtSimStatus.setTextColor(0xFFFFFFFF);
        btnStartSim.setEnabled(true);
    }

    private void loadActiveSimState() {
        HceCardProfile active = HceSimulationService.getActiveProfile(this);
        if (active != null) {
            for (HceCardProfile p : profiles) {
                if (p.getId().equals(active.getId())) {
                    selectedProfile = p;
                    txtSimStatus.setText("正在模擬: " + p.getName()
                            + "\nUID: " + p.getUid()
                            + "\n\n請將另一台裝置靠近本機背面");
                    txtSimStatus.setTextColor(0xFF4CAF50);
                    btnStartSim.setEnabled(false);
                    break;
                }
            }
        }
    }

    private void loadProfiles() {
        profiles.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String raw = prefs.getString(KEY_LIST, "");
        if (!raw.isEmpty()) {
            for (String item : raw.split(",")) {
                HceCardProfile p = HceCardProfile.fromStorageString(item);
                if (p != null) profiles.add(p);
            }
        }
    }

    private void saveProfileList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < profiles.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(profiles.get(i).toStorageString());
        }
        prefs.edit().putString(KEY_LIST, sb.toString()).apply();
    }

    private void refreshList() {
        listDisplay.clear();
        for (HceCardProfile p : profiles) {
            String name = p.getName();
            String uid = p.getUid();
            String display = name + "  |  " + uid;
            listDisplay.add(display);
        }
        listAdapter.notifyDataSetChanged();
    }
}
