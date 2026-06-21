package com.helirfid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class TaskAutomationActivity extends BaseNfcActivity {

    private TextView txtTagInfo, txtActions;
    private EditText editTagName, editParam1, editParam2, editIntParam;
    private CheckBox chkEnabled, chkBoolParam;
    private Spinner spinnerAction;
    private Button btnAddAction, btnSave, btnLoad, btnDelete, btnRunNow;
    private LinearLayout layoutInt, layoutBool;

    private Tag currentTag;
    private TaskProfile currentProfile;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_automation);

        txtTagInfo = findViewById(R.id.txtTaskTagInfo);
        txtActions = findViewById(R.id.txtTaskActions);
        editTagName = findViewById(R.id.editTaskTagName);
        editParam1 = findViewById(R.id.editTaskParam1);
        editParam2 = findViewById(R.id.editTaskParam2);
        editIntParam = findViewById(R.id.editTaskIntParam);
        chkEnabled = findViewById(R.id.chkTaskEnabled);
        chkBoolParam = findViewById(R.id.chkTaskBoolParam);
        spinnerAction = findViewById(R.id.spinnerTaskAction);
        btnAddAction = findViewById(R.id.btnTaskAddAction);
        btnSave = findViewById(R.id.btnTaskSave);
        btnLoad = findViewById(R.id.btnTaskLoad);
        btnDelete = findViewById(R.id.btnTaskDelete);
        btnRunNow = findViewById(R.id.btnTaskRunNow);
        layoutInt = findViewById(R.id.layoutTaskInt);
        layoutBool = findViewById(R.id.layoutTaskBool);

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
        btnRunNow.setOnClickListener(v -> runNow());

        updateActionsDisplay();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        currentUid = Converter.hex(tag.getId());

        StringBuilder sb = new StringBuilder("UID: ").append(currentUid);
        for (String t : tag.getTechList())
            sb.append("\n  ").append(t.substring(t.lastIndexOf('.') + 1));
        txtTagInfo.setText(sb.toString());

        // Try to load existing profile
        try {
            TaskProfile p = TaskProfile.load(this, currentUid);
            if (p != null) {
                currentProfile = p;
                editTagName.setText(p.tagName);
                chkEnabled.setChecked(p.enabled);
                updateActionsDisplay();
                Toast.makeText(this, "已載入現有設定檔", Toast.LENGTH_SHORT).show();
            } else {
                currentProfile = new TaskProfile();
                currentProfile.uid = currentUid;
                currentProfile.enabled = true;
                editTagName.setText("");
                chkEnabled.setChecked(true);
                Toast.makeText(this, "新標籤，請設定動作", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "載入錯誤: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void onActionSelected(int pos) {
        // Show/hide params based on action type
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
            case TaskExecutor.ACTION_LOCK_SCREEN:
                break;
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
        findViewById(R.id.layoutTaskParam1).setVisibility(hasParam1 ? View.VISIBLE : View.GONE);
        findViewById(R.id.layoutTaskParam2).setVisibility(hasParam2 ? View.VISIBLE : View.GONE);
        layoutInt.setVisibility(hasInt ? View.VISIBLE : View.GONE);
        layoutBool.setVisibility(hasBool ? View.VISIBLE : View.GONE);

        // Update hints
        switch (pos) {
            case TaskExecutor.ACTION_SET_SOUND_PROFILE:
                editParam1.setHint("silent / vibrate / normal"); break;
            case TaskExecutor.ACTION_SET_VOLUME:
                editParam1.setHint("ring / media / alarm / notification"); break;
            case TaskExecutor.ACTION_LAUNCH_APP:
                editParam1.setHint("套件名稱 (如 com.example.app)"); break;
            case TaskExecutor.ACTION_OPEN_URL:
                editParam1.setHint("https://example.com"); break;
            case TaskExecutor.ACTION_SEND_SMS:
                editParam1.setHint("電話號碼"); editParam2.setHint("訊息內容"); break;
            case TaskExecutor.ACTION_SET_ALARM:
                editParam1.setHint("HH:MM (如 07:00)"); editParam2.setHint("標籤 (選填)"); break;
            case TaskExecutor.ACTION_MAKE_CALL:
                editParam1.setHint("電話號碼"); break;
            case TaskExecutor.ACTION_OPEN_SETTINGS:
                editParam1.setHint("wifi / bluetooth / sound / display / nfc"); break;
            case TaskExecutor.ACTION_SET_WIFI_CONFIG:
                editParam1.setHint("SSID"); editParam2.setHint("密碼"); break;
            case TaskExecutor.ACTION_TTS_SPEAK:
                editParam1.setHint("朗讀文字"); break;
            case TaskExecutor.ACTION_ADD_CALENDAR_EVENT:
                editParam1.setHint("活動標題"); editParam2.setHint("描述"); break;
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
            case TaskExecutor.ACTION_WAIT:
                editIntParam.setHint("毫秒 (如 1000)"); break;
            case TaskExecutor.ACTION_RUN_PROFILE:
                editParam1.setHint("設定檔名稱"); break;
            case TaskExecutor.ACTION_SET_VARIABLE:
                editParam1.setHint("變數名稱"); editParam2.setHint("值"); break;
            case TaskExecutor.ACTION_ADD_VARIABLE:
                editParam1.setHint("變數名稱"); editParam2.setHint("增加值"); break;
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

        TaskProfile.TaskAction action = new TaskProfile.TaskAction(pos, p1, p2, iVal, bVal);
        if (currentProfile == null) {
            currentProfile = new TaskProfile();
            currentProfile.uid = currentUid != null ? currentUid : "unknown";
        }
        currentProfile.actions.add(action);
        updateActionsDisplay();
        editParam1.setText(""); editParam2.setText(""); editIntParam.setText("");
        Toast.makeText(this, "已增加: " + TaskExecutor.ACTION_NAMES[pos], Toast.LENGTH_SHORT).show();
    }

    private void saveProfile() {
        if (currentTag == null && currentUid == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentProfile == null || currentProfile.actions.isEmpty()) {
            Toast.makeText(this, "請至少增加一個動作", Toast.LENGTH_SHORT).show();
            return;
        }
        currentProfile.tagName = editTagName.getText().toString().trim();
        currentProfile.enabled = chkEnabled.isChecked();
        if (currentUid != null) currentProfile.uid = currentUid;
        try {
            currentProfile.save(this);
            Toast.makeText(this, "設定檔已儲存", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "儲存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoadDialog() {
        List<TaskProfile> list = TaskProfile.listAll(this);
        if (list.isEmpty()) {
            Toast.makeText(this, "無儲存的設定檔", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            TaskProfile p = list.get(i);
            items[i] = (p.tagName != null && !p.tagName.isEmpty() ? p.tagName : p.uid)
                    + " (" + p.actions.size() + " 動作)";
        }
        new AlertDialog.Builder(this)
                .setTitle("選擇設定檔")
                .setItems(items, (d, which) -> {
                    currentProfile = list.get(which);
                    currentUid = currentProfile.uid;
                    editTagName.setText(currentProfile.tagName);
                    chkEnabled.setChecked(currentProfile.enabled);
                    txtTagInfo.setText("UID: " + currentProfile.uid + "\n(從檔案載入)");
                    updateActionsDisplay();
                })
                .show();
    }

    private void deleteProfile() {
        if (currentUid == null) {
            Toast.makeText(this, "無設定檔可刪除", Toast.LENGTH_SHORT).show();
            return;
        }
        TaskProfile.delete(this, currentUid);
        currentProfile = null;
        currentUid = null;
        editTagName.setText("");
        txtTagInfo.setText("等待 NFC 標籤...");
        updateActionsDisplay();
        Toast.makeText(this, "設定檔已刪除", Toast.LENGTH_SHORT).show();
    }

    private void runNow() {
        if (currentProfile == null || currentProfile.actions.isEmpty()) {
            Toast.makeText(this, "無動作可執行", Toast.LENGTH_SHORT).show();
            return;
        }
        for (TaskProfile.TaskAction a : currentProfile.actions)
            TaskExecutor.execute(this, a.type, a.param1, a.param2, a.intParam, a.boolParam);
        Toast.makeText(this, "已執行 " + currentProfile.actions.size() + " 個動作", Toast.LENGTH_SHORT).show();
    }

    private void updateActionsDisplay() {
        if (currentProfile == null || currentProfile.actions.isEmpty()) {
            txtActions.setText("無動作 (請掃描標籤並增加動作)");
            return;
        }
        StringBuilder sb = new StringBuilder("=== 動作列表 (").append(currentProfile.actions.size()).append(") ===\n");
        for (int i = 0; i < currentProfile.actions.size(); i++) {
            TaskProfile.TaskAction a = currentProfile.actions.get(i);
            String name = a.type >= 0 && a.type < TaskExecutor.ACTION_NAMES.length
                    ? TaskExecutor.ACTION_NAMES[a.type] : "未知";
            sb.append(i + 1).append(". ").append(name);
            if (!TextUtils.isEmpty(a.param1)) sb.append(" [").append(a.param1).append("]");
            if (!TextUtils.isEmpty(a.param2)) sb.append(" / ").append(a.param2);
            if (a.intParam > 0) sb.append(" (").append(a.intParam).append(")");
            sb.append("\n");
        }
        txtActions.setText(sb.toString());
    }
}
