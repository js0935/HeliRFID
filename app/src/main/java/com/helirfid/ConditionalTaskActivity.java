package com.helirfid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConditionalTaskActivity extends BaseNfcActivity {

    private static final String[] CONDITION_TYPES = {
            "Tag UID 等於", "Tag 已偵測", "時間區間", "電量等級", "WiFi 已連線", "藍牙已連線"
    };
    private static final String[] COMPARE_OPS = {"等於", "不等於", "大於", "小於"};

    private Spinner spinnerCondition, spinnerCompare;
    private EditText editValue, editParam1, editParam2, editIntParam, editElseParam1, editElseParam2, editElseIntParam;
    private Button btnAddAction, btnAddElseAction, btnSave, btnLoad, btnDelete, btnTest;
    private Spinner spinnerAction, spinnerElseAction;
    private TextView txtActions, txtElseActions, txtResult;
    private CheckBox chkBoolParam, chkElseBoolParam;
    private LinearLayout layoutInt, layoutBool, layoutElseInt, layoutElseBool, layoutActions, layoutElseActions;

    private Tag currentTag;
    private String currentUid;
    private final List<Condition> conditions = new ArrayList<>();
    private final List<TaskProfile.TaskAction> thenActions = new ArrayList<>();
    private final List<TaskProfile.TaskAction> elseActions = new ArrayList<>();

    private static class Condition {
        int type; // 0=UID equals, 1=Tag detected, 2=Time between, 3=Battery level, 4=WiFi connected, 5=Bluetooth connected
        int compareOp; // 0=equals, 1=not equals, 2=greater than, 3=less than
        String value;

        Condition(int type, int compareOp, String value) {
            this.type = type;
            this.compareOp = compareOp;
            this.value = value;
        }

        JSONObject toJson() throws Exception {
            JSONObject o = new JSONObject();
            o.put("type", type);
            o.put("compareOp", compareOp);
            o.put("value", value != null ? value : "");
            return o;
        }

        static Condition fromJson(JSONObject o) throws Exception {
            return new Condition(o.getInt("type"), o.getInt("compareOp"), o.optString("value"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conditional_task);

        spinnerCondition = findViewById(R.id.spinnerConditionType);
        spinnerCompare = findViewById(R.id.spinnerCompareOp);
        editValue = findViewById(R.id.editConditionValue);
        editParam1 = findViewById(R.id.editThenParam1);
        editParam2 = findViewById(R.id.editThenParam2);
        editIntParam = findViewById(R.id.editThenIntParam);
        editElseParam1 = findViewById(R.id.editElseParam1);
        editElseParam2 = findViewById(R.id.editElseParam2);
        editElseIntParam = findViewById(R.id.editElseIntParam);
        spinnerAction = findViewById(R.id.spinnerThenAction);
        spinnerElseAction = findViewById(R.id.spinnerElseAction);
        btnAddAction = findViewById(R.id.btnAddThenAction);
        btnAddElseAction = findViewById(R.id.btnAddElseAction);
        btnSave = findViewById(R.id.btnCondSave);
        btnLoad = findViewById(R.id.btnCondLoad);
        btnDelete = findViewById(R.id.btnCondDelete);
        btnTest = findViewById(R.id.btnCondTest);
        txtActions = findViewById(R.id.txtThenActions);
        txtElseActions = findViewById(R.id.txtElseActions);
        txtResult = findViewById(R.id.txtCondResult);
        chkBoolParam = findViewById(R.id.chkThenBoolParam);
        chkElseBoolParam = findViewById(R.id.chkElseBoolParam);
        layoutInt = findViewById(R.id.layoutThenInt);
        layoutBool = findViewById(R.id.layoutThenBool);
        layoutElseInt = findViewById(R.id.layoutElseInt);
        layoutElseBool = findViewById(R.id.layoutElseBool);
        layoutActions = findViewById(R.id.layoutThenActions);
        layoutElseActions = findViewById(R.id.layoutElseActions);

        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CONDITION_TYPES);
        condAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(condAdapter);

        ArrayAdapter<String> cmpAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, COMPARE_OPS);
        cmpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCompare.setAdapter(cmpAdapter);

        ArrayAdapter<String> actAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, TaskExecutor.ACTION_NAMES);
        actAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAction.setAdapter(actAdapter);
        spinnerElseAction.setAdapter(actAdapter);

        spinnerCondition.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { onCondTypeChanged(pos); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        btnAddAction.setOnClickListener(v -> addAction(true));
        btnAddElseAction.setOnClickListener(v -> addAction(false));
        btnSave.setOnClickListener(v -> saveProfile());
        btnLoad.setOnClickListener(v -> showLoadDialog());
        btnDelete.setOnClickListener(v -> deleteProfile());
        btnTest.setOnClickListener(v -> testCondition());

        updateDisplay();
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
        txtResult.setText(sb.toString());
        try {
            ConditionalProfile p = ConditionalProfile.load(this, currentUid);
            if (p != null) loadProfile(p);
        } catch (Exception ignored) {}
    }

    private void onCondTypeChanged(int pos) {
        boolean showCompare = pos == 0 || pos == 3;
        boolean showValue = pos == 0 || pos == 2 || pos == 3;
        spinnerCompare.setVisibility(showCompare ? View.VISIBLE : View.GONE);
        editValue.setVisibility(showValue ? View.VISIBLE : View.GONE);
        switch (pos) {
            case 0: editValue.setHint("UID 值 (hex)"); break;
            case 2: editValue.setHint("HH:MM-HH:MM"); break;
            case 3: editValue.setHint("0-100"); break;
            default: editValue.setHint("值");
        }
    }

    private void addAction(boolean isThen) {
        int pos = isThen ? spinnerAction.getSelectedItemPosition() : spinnerElseAction.getSelectedItemPosition();
        String p1 = (isThen ? editParam1 : editElseParam1).getText().toString().trim();
        String p2 = (isThen ? editParam2 : editElseParam2).getText().toString().trim();
        int iVal = 0;
        try { iVal = Integer.parseInt((isThen ? editIntParam : editElseIntParam).getText().toString().trim()); } catch (NumberFormatException ignored) {}
        boolean bVal = (isThen ? chkBoolParam : chkElseBoolParam).isChecked();
        TaskProfile.TaskAction action = new TaskProfile.TaskAction(pos, p1, p2, iVal, bVal);
        if (isThen) thenActions.add(action);
        else elseActions.add(action);
        (isThen ? editParam1 : editElseParam1).setText("");
        (isThen ? editParam2 : editElseParam2).setText("");
        (isThen ? editIntParam : editElseIntParam).setText("");
        updateDisplay();
        Toast.makeText(this, "已增加" + (isThen ? " Then" : " Else") + ": " + TaskExecutor.ACTION_NAMES[pos], Toast.LENGTH_SHORT).show();
    }

    private void saveProfile() {
        if (currentUid == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        if (conditions.isEmpty() && thenActions.isEmpty()) {
            Toast.makeText(this, "請至少設定條件或動作", Toast.LENGTH_SHORT).show();
            return;
        }
        int condType = spinnerCondition.getSelectedItemPosition();
        int cmpOp = spinnerCompare.getSelectedItemPosition();
        String val = editValue.getText().toString().trim();
        conditions.clear();
        if (condType == 0 || condType == 2 || condType == 3) {
            conditions.add(new Condition(condType, cmpOp, val));
        } else {
            conditions.add(new Condition(condType, 0, ""));
        }
        try {
            ConditionalProfile p = new ConditionalProfile();
            p.uid = currentUid;
            p.conditions = conditions;
            p.thenActions = thenActions;
            p.elseActions = elseActions;
            p.save(this);
            Toast.makeText(this, "條件設定檔已儲存", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "儲存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoadDialog() {
        List<ConditionalProfile> list = ConditionalProfile.listAll(this);
        if (list.isEmpty()) { Toast.makeText(this, "無儲存設定", Toast.LENGTH_SHORT).show(); return; }
        String[] items = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Condition p = list.get(i).conditions.isEmpty() ? null : list.get(i).conditions.get(0);
            items[i] = "UID: " + list.get(i).uid + " (" + list.get(i).thenActions.size() + " Then / " + list.get(i).elseActions.size() + " Else)";
        }
        new AlertDialog.Builder(this).setTitle("選擇條件設定").setItems(items, (d, which) -> loadProfile(list.get(which))).show();
    }

    private void loadProfile(ConditionalProfile p) {
        currentUid = p.uid;
        conditions.clear(); conditions.addAll(p.conditions);
        thenActions.clear(); thenActions.addAll(p.thenActions);
        elseActions.clear(); elseActions.addAll(p.elseActions);
        if (!p.conditions.isEmpty()) {
            Condition c = p.conditions.get(0);
            spinnerCondition.setSelection(c.type);
            spinnerCompare.setSelection(c.compareOp);
            editValue.setText(c.value);
        }
        updateDisplay();
        txtResult.setText("已載入: UID=" + p.uid);
    }

    private void deleteProfile() {
        if (currentUid == null) { Toast.makeText(this, "無設定可刪除", Toast.LENGTH_SHORT).show(); return; }
        ConditionalProfile.delete(this, currentUid);
        conditions.clear(); thenActions.clear(); elseActions.clear(); currentUid = null;
        updateDisplay();
        txtResult.setText("設定已刪除");
        Toast.makeText(this, "已刪除", Toast.LENGTH_SHORT).show();
    }

    private void testCondition() {
        if (conditions.isEmpty()) { Toast.makeText(this, "無條件可測試", Toast.LENGTH_SHORT).show(); return; }
        boolean result = evaluateConditions();
        StringBuilder sb = new StringBuilder("=== 條件測試結果 ===\n");
        sb.append("條件結果: ").append(result ? "✓ 成立 (執行 Then)" : "✗ 不成立 (執行 Else)").append("\n\n");
        List<TaskProfile.TaskAction> exec = result ? thenActions : elseActions;
        if (exec.isEmpty()) {
            sb.append("無對應動作");
        } else {
            sb.append("將執行 ").append(exec.size()).append(" 個動作:\n");
            for (TaskProfile.TaskAction a : exec) {
                String name = a.type >= 0 && a.type < TaskExecutor.ACTION_NAMES.length ? TaskExecutor.ACTION_NAMES[a.type] : "未知";
                sb.append("  - ").append(name).append("\n");
                TaskExecutor.execute(this, a.type, a.param1, a.param2, a.intParam, a.boolParam);
            }
        }
        txtResult.setText(sb.toString());
    }

    private boolean evaluateConditions() {
        for (Condition c : conditions) {
            switch (c.type) {
                case 0: // UID equals
                    String uid = currentTag != null ? Converter.hex(currentTag.getId()) : "";
                    return compare(uid, c.value, c.compareOp);
                case 1: // Tag detected
                    return currentTag != null;
                case 2: { // Time between
                    String[] parts = c.value.split("-");
                    if (parts.length != 2) return false;
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String now = sdf.format(new Date());
                    return now.compareTo(parts[0]) >= 0 && now.compareTo(parts[1]) <= 0;
                }
                case 3: { // Battery level
                    BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                    int level = bm != null ? bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) : 0;
                    return compare(String.valueOf(level), c.value, c.compareOp);
                }
                case 4: // WiFi connected
                    return isWifiConnected();
                case 5: // Bluetooth connected
                    return isBluetoothConnected();
            }
        }
        return false;
    }

    private boolean compare(String a, String b, int op) {
        switch (op) {
            case 0: return a.equalsIgnoreCase(b);
            case 1: return !a.equalsIgnoreCase(b);
            case 2:
                try { return Double.parseDouble(a) > Double.parseDouble(b); } catch (NumberFormatException e) { return false; }
            case 3:
                try { return Double.parseDouble(a) < Double.parseDouble(b); } catch (NumberFormatException e) { return false; }
        }
        return false;
    }

    private boolean isWifiConnected() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm == null) return false;
        WifiInfo info = wm.getConnectionInfo();
        return info != null && info.getNetworkId() != -1;
    }

    private boolean isBluetoothConnected() {
        android.bluetooth.BluetoothAdapter ba = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        return ba != null && ba.isEnabled();
    }

    private void updateDisplay() {
        StringBuilder sb = new StringBuilder("=== Then 動作 (").append(thenActions.size()).append(") ===\n");
        for (int i = 0; i < thenActions.size(); i++) {
            TaskProfile.TaskAction a = thenActions.get(i);
            String name = a.type >= 0 && a.type < TaskExecutor.ACTION_NAMES.length ? TaskExecutor.ACTION_NAMES[a.type] : "未知";
            sb.append(i + 1).append(". ").append(name);
            if (!TextUtils.isEmpty(a.param1)) sb.append(" [").append(a.param1).append("]");
            if (!TextUtils.isEmpty(a.param2)) sb.append(" / ").append(a.param2);
            sb.append("\n");
        }
        txtActions.setText(sb.toString());

        sb = new StringBuilder("=== Else 動作 (").append(elseActions.size()).append(") ===\n");
        for (int i = 0; i < elseActions.size(); i++) {
            TaskProfile.TaskAction a = elseActions.get(i);
            String name = a.type >= 0 && a.type < TaskExecutor.ACTION_NAMES.length ? TaskExecutor.ACTION_NAMES[a.type] : "未知";
            sb.append(i + 1).append(". ").append(name);
            if (!TextUtils.isEmpty(a.param1)) sb.append(" [").append(a.param1).append("]");
            if (!TextUtils.isEmpty(a.param2)) sb.append(" / ").append(a.param2);
            sb.append("\n");
        }
        txtElseActions.setText(sb.toString());

        boolean hasThen = !thenActions.isEmpty();
        boolean hasElse = !elseActions.isEmpty();
        btnSave.setEnabled(hasThen || hasElse);
    }

    private static class ConditionalProfile {
        String uid;
        List<Condition> conditions = new ArrayList<>();
        List<TaskProfile.TaskAction> thenActions = new ArrayList<>();
        List<TaskProfile.TaskAction> elseActions = new ArrayList<>();

        static File getDir(Context context) {
            File dir = new File(context.getFilesDir(), "conditional_profiles");
            if (!dir.exists()) dir.mkdirs();
            return dir;
        }

        void save(Context context) throws Exception {
            JSONObject o = new JSONObject();
            o.put("uid", uid);
            JSONArray condArr = new JSONArray();
            for (Condition c : conditions) condArr.put(c.toJson());
            o.put("conditions", condArr);
            JSONArray thenArr = new JSONArray();
            for (TaskProfile.TaskAction a : thenActions) thenArr.put(a.toJson());
            o.put("thenActions", thenArr);
            JSONArray elseArr = new JSONArray();
            for (TaskProfile.TaskAction a : elseActions) elseArr.put(a.toJson());
            o.put("elseActions", elseArr);
            File f = new File(getDir(context), uid + ".json");
            try (FileWriter w = new FileWriter(f)) { w.write(o.toString(2)); }
        }

        static ConditionalProfile load(Context context, String uid) throws Exception {
            File f = new File(getDir(context), uid + ".json");
            if (!f.exists()) return null;
            try (FileReader r = new FileReader(f)) {
                StringBuilder sb = new StringBuilder(); int c;
                while ((c = r.read()) != -1) sb.append((char) c);
                JSONObject o = new JSONObject(sb.toString());
                ConditionalProfile p = new ConditionalProfile();
                p.uid = o.getString("uid");
                JSONArray condArr = o.getJSONArray("conditions");
                for (int i = 0; i < condArr.length(); i++) p.conditions.add(Condition.fromJson(condArr.getJSONObject(i)));
                JSONArray thenArr = o.getJSONArray("thenActions");
                for (int i = 0; i < thenArr.length(); i++) p.thenActions.add(TaskProfile.TaskAction.fromJson(thenArr.getJSONObject(i)));
                JSONArray elseArr = o.getJSONArray("elseActions");
                for (int i = 0; i < elseArr.length(); i++) p.elseActions.add(TaskProfile.TaskAction.fromJson(elseArr.getJSONObject(i)));
                return p;
            }
        }

        static void delete(Context context, String uid) { new File(getDir(context), uid + ".json").delete(); }

        static List<ConditionalProfile> listAll(Context context) {
            List<ConditionalProfile> list = new ArrayList<>();
            File[] files = getDir(context).listFiles((d, n) -> n.endsWith(".json"));
            if (files == null) return list;
            for (File f : files) {
                try { ConditionalProfile p = load(context, f.getName().replace(".json", "")); if (p != null) list.add(p); } catch (Exception ignored) {}
            }
            return list;
        }
    }
}
