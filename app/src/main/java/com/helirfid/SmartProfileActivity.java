/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.app.AlertDialog;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class SmartProfileActivity extends BaseNfcActivity {

    private static final String PROFILES_DIR = "smart_profiles";

    private TextView txtTagInfo, txtActions, txtTriggers, txtProfileList;
    private EditText editProfileName, editParam1, editParam2, editIntParam;
    private CheckBox chkEnabled, chkBoolParam;
    private Spinner spinnerAction;
    private Button btnAddAction, btnSave, btnLoad, btnDelete, btnAddTrigger, btnRunNow;
    private LinearLayout layoutInt, layoutBool;

    private Tag currentTag;
    private String currentProfileName;
    private final List<TaskProfile.TaskAction> actions = new ArrayList<>();
    private final List<String> triggerUids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_profile);

        txtTagInfo = findViewById(R.id.txtSpTagInfo);
        txtActions = findViewById(R.id.txtSpActions);
        txtTriggers = findViewById(R.id.txtSpTriggers);
        txtProfileList = findViewById(R.id.txtSpProfileList);
        editProfileName = findViewById(R.id.editSpProfileName);
        editParam1 = findViewById(R.id.editSpParam1);
        editParam2 = findViewById(R.id.editSpParam2);
        editIntParam = findViewById(R.id.editSpIntParam);
        chkEnabled = findViewById(R.id.chkSpEnabled);
        chkBoolParam = findViewById(R.id.chkSpBoolParam);
        spinnerAction = findViewById(R.id.spinnerSpAction);
        btnAddAction = findViewById(R.id.btnSpAddAction);
        btnSave = findViewById(R.id.btnSpSave);
        btnLoad = findViewById(R.id.btnSpLoad);
        btnDelete = findViewById(R.id.btnSpDelete);
        btnAddTrigger = findViewById(R.id.btnSpAddTrigger);
        btnRunNow = findViewById(R.id.btnSpRunNow);
        layoutInt = findViewById(R.id.layoutSpInt);
        layoutBool = findViewById(R.id.layoutSpBool);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, TaskExecutor.ACTION_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAction.setAdapter(adapter);

        spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { onActionSelected(pos); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        btnAddAction.setOnClickListener(v -> addAction());
        btnSave.setOnClickListener(v -> saveProfile());
        btnLoad.setOnClickListener(v -> showLoadDialog());
        btnDelete.setOnClickListener(v -> deleteProfile());
        btnAddTrigger.setOnClickListener(v -> addTrigger());
        btnRunNow.setOnClickListener(v -> runNow());

        refreshProfileList();
        updateDisplay();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        String uid = Converter.hex(tag.getId());
        StringBuilder sb = new StringBuilder("UID: ").append(uid);
        for (String t : tag.getTechList())
            sb.append("\n  ").append(t.substring(t.lastIndexOf('.') + 1));
        txtTagInfo.setText(sb.toString());
    }

    private void onActionSelected(int pos) {
        boolean hasParam1 = false, hasParam2 = false, hasInt = false, hasBool = false;
        switch (pos) {
            case TaskExecutor.ACTION_TOGGLE_WIFI:
            case TaskExecutor.ACTION_TOGGLE_BLUETOOTH:
            case TaskExecutor.ACTION_SET_AIRPLANE_MODE:
            case TaskExecutor.ACTION_TOGGLE_AUTO_ROTATE:
                hasBool = true; break;
            case TaskExecutor.ACTION_SET_SOUND_PROFILE:
                hasParam1 = true; break;
            case TaskExecutor.ACTION_SET_VOLUME:
                hasParam1 = true; hasInt = true; break;
            case TaskExecutor.ACTION_SET_BRIGHTNESS:
                hasInt = true; break;
            case TaskExecutor.ACTION_LAUNCH_APP:
            case TaskExecutor.ACTION_OPEN_URL:
            case TaskExecutor.ACTION_TTS_SPEAK:
            case TaskExecutor.ACTION_MAKE_CALL:
                hasParam1 = true; break;
            case TaskExecutor.ACTION_SEND_SMS:
                hasParam1 = true; hasParam2 = true; break;
            case TaskExecutor.ACTION_SET_ALARM:
                hasParam1 = true; hasParam2 = true; break;
            case TaskExecutor.ACTION_OPEN_SETTINGS:
                hasParam1 = true; break;
            case TaskExecutor.ACTION_SET_WIFI_CONFIG:
                hasParam1 = true; hasParam2 = true; break;
            case TaskExecutor.ACTION_ADD_CALENDAR_EVENT:
                hasParam1 = true; hasParam2 = true; break;
            case TaskExecutor.ACTION_START_TIMER:
                hasInt = true; break;
            // Connectivity Extended
            case TaskExecutor.ACTION_TOGGLE_AUTO_SYNC:
                hasBool = true; break;
            // Audio Extended
            case TaskExecutor.ACTION_SET_VOLUME_MUTE:
            case TaskExecutor.ACTION_TOGGLE_HAPTIC_FEEDBACK:
            case TaskExecutor.ACTION_TOGGLE_DTMF_TONE:
            case TaskExecutor.ACTION_TOGGLE_SOUND_EFFECTS:
            case TaskExecutor.ACTION_TOGGLE_VIBRATE_ON_NOTIF:
                hasBool = true; break;
            case TaskExecutor.ACTION_SET_AUDIO_BALANCE:
                hasInt = true; break;
            // Notifications Extended
            case TaskExecutor.ACTION_OPEN_NOTIF_SETTINGS:
                hasParam1 = true; break;
            // System Settings
            case TaskExecutor.ACTION_OPEN_APP_INFO:
            case TaskExecutor.ACTION_OPEN_APP_PERMISSIONS:
                hasParam1 = true; break;
            // Share & Launch
            case TaskExecutor.ACTION_SHARE_TEXT:
                hasParam1 = true; hasParam2 = true; break;
            case TaskExecutor.ACTION_COPY_TO_CLIPBOARD:
                hasParam1 = true; break;
            // Flow Control
            case TaskExecutor.ACTION_WAIT:
                hasInt = true; break;
            case TaskExecutor.ACTION_RUN_PROFILE:
                hasParam1 = true; break;
            case TaskExecutor.ACTION_SET_VARIABLE:
            case TaskExecutor.ACTION_ADD_VARIABLE:
                hasParam1 = true; hasParam2 = true; break;
        }
        findViewById(R.id.layoutSpParam1).setVisibility(hasParam1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.layoutSpParam2).setVisibility(hasParam2 ? View.VISIBLE : View.GONE);
        layoutInt.setVisibility(hasInt ? View.VISIBLE : View.GONE);
        layoutBool.setVisibility(hasBool ? View.VISIBLE : View.GONE);

        switch (pos) {
            case TaskExecutor.ACTION_SET_SOUND_PROFILE:
                editParam1.setHint("silent / vibrate / normal"); break;
            case TaskExecutor.ACTION_SET_VOLUME:
                editParam1.setHint("ring / media / alarm / notification"); break;
            case TaskExecutor.ACTION_LAUNCH_APP:
                editParam1.setHint("套件名稱"); break;
            case TaskExecutor.ACTION_OPEN_URL:
                editParam1.setHint("https://example.com"); break;
            case TaskExecutor.ACTION_SEND_SMS:
                editParam1.setHint("電話號碼"); editParam2.setHint("訊息內容"); break;
            case TaskExecutor.ACTION_SET_ALARM:
                editParam1.setHint("HH:MM"); editParam2.setHint("標籤"); break;
            case TaskExecutor.ACTION_MAKE_CALL:
                editParam1.setHint("電話號碼"); break;
            case TaskExecutor.ACTION_OPEN_SETTINGS:
                editParam1.setHint("wifi / bluetooth / sound / nfc"); break;
            case TaskExecutor.ACTION_SET_WIFI_CONFIG:
                editParam1.setHint("SSID"); editParam2.setHint("密碼"); break;
            case TaskExecutor.ACTION_TTS_SPEAK:
                editParam1.setHint("朗讀文字"); break;
            case TaskExecutor.ACTION_ADD_CALENDAR_EVENT:
                editParam1.setHint("活動標題"); editParam2.setHint("描述"); break;
            case TaskExecutor.ACTION_WAIT:
                editIntParam.setHint("毫秒 (如 1000)"); break;
            case TaskExecutor.ACTION_RUN_PROFILE:
                editParam1.setHint("設定檔名稱"); break;
            case TaskExecutor.ACTION_SET_VARIABLE:
                editParam1.setHint("變數名稱"); editParam2.setHint("值"); break;
            case TaskExecutor.ACTION_ADD_VARIABLE:
                editParam1.setHint("變數名稱"); editParam2.setHint("增加值"); break;
            case TaskExecutor.ACTION_OPEN_NOTIF_SETTINGS:
                editParam1.setHint("套件名稱"); break;
            case TaskExecutor.ACTION_OPEN_APP_INFO:
            case TaskExecutor.ACTION_OPEN_APP_PERMISSIONS:
                editParam1.setHint("套件名稱"); break;
            case TaskExecutor.ACTION_SHARE_TEXT:
                editParam1.setHint("分享文字"); editParam2.setHint("主旨 (選填)"); break;
            case TaskExecutor.ACTION_COPY_TO_CLIPBOARD:
                editParam1.setHint("要複製的文字"); break;
            case TaskExecutor.ACTION_SET_AUDIO_BALANCE:
                editIntParam.setHint("-100 ~ 100"); break;
            default:
                editParam1.setHint("參數 1");
                editParam2.setHint("參數 2");
        }
    }

    private void addAction() {
        int pos = spinnerAction.getSelectedItemPosition();
        String p1 = editParam1.getText().toString().trim();
        String p2 = editParam2.getText().toString().trim();
        int iVal = 0;
        try { iVal = Integer.parseInt(editIntParam.getText().toString().trim()); } catch (NumberFormatException ignored) {}
        boolean bVal = chkBoolParam.isChecked();

        actions.add(new TaskProfile.TaskAction(pos, p1, p2, iVal, bVal));
        editParam1.setText(""); editParam2.setText(""); editIntParam.setText("");
        Toast.makeText(this, "已增加: " + TaskExecutor.ACTION_NAMES[pos], Toast.LENGTH_SHORT).show();
        updateDisplay();
    }

    private void addTrigger() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤以新增觸發 UID", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = Converter.hex(currentTag.getId());
        if (triggerUids.contains(uid)) {
            Toast.makeText(this, "此 UID 已在觸發列表中", Toast.LENGTH_SHORT).show();
            return;
        }
        triggerUids.add(uid);
        Toast.makeText(this, "已新增觸發 UID", Toast.LENGTH_SHORT).show();
        updateDisplay();
    }

    private void saveProfile() {
        String name = editProfileName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "請輸入設定檔名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (actions.isEmpty()) {
            Toast.makeText(this, "請至少增加一個動作", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject o = new JSONObject();
            o.put("profileName", name);
            o.put("enabled", chkEnabled.isChecked());
            JSONArray triggers = new JSONArray();
            for (String u : triggerUids) triggers.put(u);
            o.put("triggerUids", triggers);
            JSONArray actArr = new JSONArray();
            for (TaskProfile.TaskAction a : actions) actArr.put(a.toJson());
            o.put("actions", actArr);
            File dir = new File(getFilesDir(), PROFILES_DIR);
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, name + ".json");
            try (FileWriter w = new FileWriter(f)) {
                w.write(o.toString(2));
            }
            currentProfileName = name;
            Toast.makeText(this, "設定檔已儲存", Toast.LENGTH_SHORT).show();
            refreshProfileList();
        } catch (Exception e) {
            Toast.makeText(this, "儲存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoadDialog() {
        File dir = new File(getFilesDir(), PROFILES_DIR);
        File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null || files.length == 0) {
            Toast.makeText(this, "無儲存的設定檔", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            items[i] = files[i].getName().replace(".json", "");
        }
        new AlertDialog.Builder(this)
                .setTitle("選擇設定檔")
                .setItems(items, (d, which) -> loadProfile(items[which]))
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadProfile(String name) {
        try {
            File f = new File(new File(getFilesDir(), PROFILES_DIR), name + ".json");
            if (!f.exists()) {
                Toast.makeText(this, "設定檔不存在", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder sb = new StringBuilder();
            try (FileReader r = new FileReader(f)) {
                int c;
                while ((c = r.read()) != -1) sb.append((char) c);
            }
            JSONObject o = new JSONObject(sb.toString());
            currentProfileName = name;
            editProfileName.setText(o.getString("profileName"));
            chkEnabled.setChecked(o.optBoolean("enabled", true));
            triggerUids.clear();
            JSONArray triggers = o.optJSONArray("triggerUids");
            if (triggers != null) {
                for (int i = 0; i < triggers.length(); i++)
                    triggerUids.add(triggers.getString(i));
            }
            actions.clear();
            JSONArray actArr = o.getJSONArray("actions");
            for (int i = 0; i < actArr.length(); i++)
                actions.add(TaskProfile.TaskAction.fromJson(actArr.getJSONObject(i)));
            updateDisplay();
            Toast.makeText(this, "已載入: " + name, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "載入失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProfile() {
        if (currentProfileName == null) {
            Toast.makeText(this, "請先載入設定檔", Toast.LENGTH_SHORT).show();
            return;
        }
        File f = new File(new File(getFilesDir(), PROFILES_DIR), currentProfileName + ".json");
        if (f.delete()) {
            currentProfileName = null;
            editProfileName.setText("");
            triggerUids.clear();
            actions.clear();
            updateDisplay();
            Toast.makeText(this, "設定檔已刪除", Toast.LENGTH_SHORT).show();
            refreshProfileList();
        } else {
            Toast.makeText(this, "刪除失敗", Toast.LENGTH_SHORT).show();
        }
    }

    private void runNow() {
        if (actions.isEmpty()) {
            Toast.makeText(this, "無動作可執行", Toast.LENGTH_SHORT).show();
            return;
        }
        for (TaskProfile.TaskAction a : actions)
            TaskExecutor.execute(this, a.type, a.param1, a.param2, a.intParam, a.boolParam);
        Toast.makeText(this, "已執行 " + actions.size() + " 個動作", Toast.LENGTH_SHORT).show();
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder("=== 動作列表 (").append(actions.size()).append(") ===\n");
        for (int i = 0; i < actions.size(); i++) {
            TaskProfile.TaskAction a = actions.get(i);
            String name = a.type >= 0 && a.type < TaskExecutor.ACTION_NAMES.length
                    ? TaskExecutor.ACTION_NAMES[a.type] : "未知";
            sb.append(i + 1).append(". ").append(name);
            if (!TextUtils.isEmpty(a.param1)) sb.append(" [").append(a.param1).append("]");
            if (!TextUtils.isEmpty(a.param2)) sb.append(" / ").append(a.param2);
            if (a.intParam > 0) sb.append(" (").append(a.intParam).append(")");
            sb.append("\n");
        }
        txtActions.setText(sb.toString());

        StringBuilder trig = new StringBuilder("=== 觸發 UID (").append(triggerUids.size()).append(") ===\n");
        for (String u : triggerUids) {
            trig.append(u.substring(0, Math.min(16, u.length()))).append("...\n");
        }
        txtTriggers.setText(trig.toString());
    }

    private void refreshProfileList() {
        File dir = new File(getFilesDir(), PROFILES_DIR);
        File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null || files.length == 0) {
            txtProfileList.setText("無儲存的設定檔");
            return;
        }
        StringBuilder sb = new StringBuilder("=== 已儲存設定檔 (").append(files.length).append(") ===\n");
        for (File f : files) {
            sb.append(f.getName().replace(".json", "")).append("\n");
        }
        txtProfileList.setText(sb.toString());
    }
}
