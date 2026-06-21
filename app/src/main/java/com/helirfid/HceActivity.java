package com.helirfid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Bundle;
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
    private static final String KEY_COUNT = "profile_count";
    private static final String KEY_PREFIX = "profile_";
    private static final int MAX_PROFILES = 10;

    private TextView txtScanStatus, txtSimStatus;
    private EditText editCardName;
    private Button btnSave, btnStartSim, btnStopSim, btnDelete;
    private ListView listSavedCards;

    private String lastScannedUid = "";
    private String lastScannedAtqa = "";
    private String lastScannedSak = "";
    private String lastScannedTech = "";

    private final List<HceCardProfile> profiles = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private final List<String> listDisplay = new ArrayList<>();

    private HceCardProfile selectedProfile;
    private boolean simulationActive;

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
        btnDelete = findViewById(R.id.btnHceDelete);
        listSavedCards = findViewById(R.id.listHceCards);

        btnSave.setEnabled(false);

        loadProfiles();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listDisplay);
        listSavedCards.setAdapter(listAdapter);
        refreshList();

        btnSave.setOnClickListener(v -> saveCard());
        btnStartSim.setOnClickListener(v -> startSimulation());
        btnStopSim.setOnClickListener(v -> stopSimulation());

        btnDelete.setOnClickListener(v -> deleteSelectedCard());

        listSavedCards.setOnItemClickListener((parent, view, position, id) -> {
            if (position < profiles.size()) {
                selectedProfile = profiles.get(position);
                btnDelete.setEnabled(true);
                txtSimStatus.setText("已選擇: " + selectedProfile.getName()
                        + "\nUID: " + selectedProfile.getUid()
                        + "\n按「啟動模擬」開始模擬");
            }
        });

        loadActiveSimState();
    }

    private void deleteSelectedCard() {
        if (selectedProfile == null) {
            Toast.makeText(this, "請先從清單選取一張卡片", Toast.LENGTH_SHORT).show();
            return;
        }
        int pos = profiles.indexOf(selectedProfile);
        if (pos >= 0) {
            profiles.remove(pos);
            saveProfileList();
            refreshList();
            selectedProfile = null;
            btnDelete.setEnabled(false);
            txtSimStatus.setText("未選擇模擬卡片");
            Toast.makeText(this, "已刪除卡片", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        try { vibrate(); } catch (Exception ignored) {}

        byte[] uidBytes = tag.getId();
        lastScannedUid = Converter.hex(uidBytes);

        StringBuilder techBuilder = new StringBuilder();
        String[] techList = tag.getTechList();
        if (techList != null) {
            for (String t : techList) {
                String shortName = t.substring(t.lastIndexOf('.') + 1);
                if (techBuilder.length() > 0) techBuilder.append(" ");
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
                lastScannedAtqa = Converter.bytesToHex(nfcA.getAtqa());
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

        txtScanStatus.setText("UID: " + lastScannedUid + "\n"
                + "技術: " + lastScannedTech + "\n"
                + "ATQA: " + lastScannedAtqa + "\n"
                + "SAK: " + lastScannedSak + "\n\n"
                + "輸入名稱後按「儲存卡片」");
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

        HceCardProfile profile = new HceCardProfile(
                UUID.randomUUID().toString().substring(0, 8), name,
                lastScannedUid, lastScannedAtqa, lastScannedSak,
                lastScannedTech, System.currentTimeMillis());

        profiles.add(0, profile);
        while (profiles.size() > MAX_PROFILES) {
            profiles.remove(profiles.size() - 1);
        }
        saveProfileList();
        refreshList();

        txtScanStatus.setText("已儲存: " + name + "\nUID: " + lastScannedUid
                + "\n\n請將 NFC 卡片靠近手機背面以掃描新卡片");
        editCardName.setText("");
        btnSave.setEnabled(false);
        lastScannedUid = "";
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
        serviceIntent.putExtra("profile_atqa", selectedProfile.getAtqa());
        serviceIntent.putExtra("profile_sak", selectedProfile.getSak());
        serviceIntent.putExtra("profile_tech", selectedProfile.getTechTypes());
        startService(serviceIntent);

        simulationActive = true;
        disableNfcDispatch();

        txtSimStatus.setText("正在模擬: " + selectedProfile.getName()
                + "\nUID: " + selectedProfile.getUid()
                + "\n\n請將另一台裝置靠近本機背面");
        txtSimStatus.setTextColor(0xFF4CAF50);
        btnStartSim.setEnabled(false);
    }

    private void stopSimulation() {
        try {
            stopService(new Intent(this, HceSimulationService.class));
        } catch (Exception ignored) {}

        HceSimulationService.setActiveProfile(this, null);
        simulationActive = false;
        enableNfcDispatch();

        txtSimStatus.setText("模擬已停止");
        txtSimStatus.setTextColor(0xFFFFFFFF);
        btnStartSim.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (simulationActive) {
            disableNfcDispatch();
        }
    }

    private void loadActiveSimState() {
        HceCardProfile active = HceSimulationService.getActiveProfile(this);
        if (active == null) return;
        for (HceCardProfile p : profiles) {
            if (p.getId().equals(active.getId())) {
                selectedProfile = p;
                simulationActive = true;
                txtSimStatus.setText("正在模擬: " + p.getName()
                        + "\nUID: " + p.getUid()
                        + "\n\n請將另一台裝置靠近本機背面");
                txtSimStatus.setTextColor(0xFF4CAF50);
                btnStartSim.setEnabled(false);
                break;
            }
        }
    }

    private void loadProfiles() {
        profiles.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int count = prefs.getInt(KEY_COUNT, 0);
        for (int i = 0; i < count; i++) {
            String id = prefs.getString(KEY_PREFIX + i + "_id", "");
            if (id.isEmpty()) continue;
            String name = prefs.getString(KEY_PREFIX + i + "_name", "");
            String uid = prefs.getString(KEY_PREFIX + i + "_uid", "");
            profiles.add(new HceCardProfile(id, name, uid,
                    prefs.getString(KEY_PREFIX + i + "_atqa", ""),
                    prefs.getString(KEY_PREFIX + i + "_sak", ""),
                    prefs.getString(KEY_PREFIX + i + "_tech", ""),
                    prefs.getLong(KEY_PREFIX + i + "_ts", 0)));
        }
    }

    private void saveProfileList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int oldCount = prefs.getInt(KEY_COUNT, 0);
        String[] suffixes = {"_id", "_name", "_uid", "_atqa", "_sak", "_tech", "_ts"};
        for (int i = 0; i < oldCount; i++) {
            for (String s : suffixes) editor.remove(KEY_PREFIX + i + s);
        }
        editor.putInt(KEY_COUNT, profiles.size());
        for (int i = 0; i < profiles.size(); i++) {
            HceCardProfile p = profiles.get(i);
            editor.putString(KEY_PREFIX + i + "_id", p.getId());
            editor.putString(KEY_PREFIX + i + "_name", p.getName());
            editor.putString(KEY_PREFIX + i + "_uid", p.getUid());
            editor.putString(KEY_PREFIX + i + "_atqa", p.getAtqa());
            editor.putString(KEY_PREFIX + i + "_sak", p.getSak());
            editor.putString(KEY_PREFIX + i + "_tech", p.getTechTypes());
            editor.putLong(KEY_PREFIX + i + "_ts", p.getTimestamp());
        }
        editor.commit();
    }

    private void refreshList() {
        listDisplay.clear();
        for (HceCardProfile p : profiles) {
            listDisplay.add(p.getName() + "  |  " + p.getUid());
        }
        listAdapter.notifyDataSetChanged();
    }
}
