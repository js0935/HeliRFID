/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Map;

public class TapToLaunchActivity extends BaseNfcActivity {

    private static final String PREFS_NAME = "tap_to_launch";
    private static final String[] ACTION_TYPES = {"開啟網址 (URL)", "啟動 App", "撥打電話", "發送簡訊", "開啟設定頁"};

    private TextView txtTagInfo, txtMappings;
    private EditText editTagName, editParam;
    private Spinner spinnerActionType;
    private Button btnSave, btnDelete, btnRefresh;

    private Tag currentTag;
    private String currentUid;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_to_launch);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        txtTagInfo = findViewById(R.id.txtTtlTagInfo);
        txtMappings = findViewById(R.id.txtTtlMappings);
        editTagName = findViewById(R.id.editTtlTagName);
        editParam = findViewById(R.id.editTtlParam);
        spinnerActionType = findViewById(R.id.spinnerTtlActionType);
        btnSave = findViewById(R.id.btnTtlSave);
        btnDelete = findViewById(R.id.btnTtlDelete);
        btnRefresh = findViewById(R.id.btnTtlRefresh);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ACTION_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActionType.setAdapter(adapter);

        editParam.setHint("https://example.com");

        spinnerActionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                switch (pos) {
                    case 0: editParam.setHint("https://example.com"); break;
                    case 1: editParam.setHint("com.example.app"); break;
                    case 2: editParam.setHint("+886912345678"); break;
                    case 3: editParam.setHint("號碼:訊息"); break;
                    case 4: editParam.setHint("wifi / bluetooth / sound / nfc"); break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        btnSave.setOnClickListener(v -> saveMapping());
        btnDelete.setOnClickListener(v -> deleteMapping());
        btnRefresh.setOnClickListener(v -> refreshMappings());

        refreshMappings();
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
        String existing = prefs.getString(currentUid, null);
        if (existing != null) {
            try {
                JSONObject o = new JSONObject(existing);
                editTagName.setText(o.optString("tagName"));
                spinnerActionType.setSelection(o.optInt("actionType", 0));
                editParam.setText(o.optString("param"));
                Toast.makeText(this, "已載入現有對應", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "載入錯誤", Toast.LENGTH_SHORT).show();
            }
        } else {
            editTagName.setText("");
            spinnerActionType.setSelection(0);
            editParam.setText("");
            Toast.makeText(this, "新標籤，請設定動作", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMapping() {
        if (currentUid == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        String tagName = editTagName.getText().toString().trim();
        int actionType = spinnerActionType.getSelectedItemPosition();
        String param = editParam.getText().toString().trim();
        if (param.isEmpty()) {
            Toast.makeText(this, "請輸入參數", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject o = new JSONObject();
            o.put("tagName", tagName);
            o.put("actionType", actionType);
            o.put("param", param);
            prefs.edit().putString(currentUid, o.toString()).apply();
            Toast.makeText(this, "對應已儲存", Toast.LENGTH_SHORT).show();
            refreshMappings();
        } catch (Exception e) {
            Toast.makeText(this, "儲存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMapping() {
        if (currentUid == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.edit().remove(currentUid).apply();
        currentUid = null;
        currentTag = null;
        editTagName.setText("");
        editParam.setText("");
        txtTagInfo.setText("等待 NFC 卡片...");
        Toast.makeText(this, "對應已刪除", Toast.LENGTH_SHORT).show();
        refreshMappings();
    }

    private void refreshMappings() {
        Map<String, ?> all = prefs.getAll();
        if (all.isEmpty()) {
            txtMappings.setText("無儲存的對應");
            return;
        }
        StringBuilder sb = new StringBuilder("=== 已儲存對應 (").append(all.size()).append(") ===\n");
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            try {
                JSONObject o = new JSONObject((String) entry.getValue());
                String name = o.optString("tagName");
                int at = o.optInt("actionType");
                String p = o.optString("param");
                String uid = entry.getKey();
                sb.append("UID: ").append(uid.substring(0, Math.min(8, uid.length()))).append("...\n");
                if (!name.isEmpty()) sb.append("  名稱: ").append(name).append("\n");
                sb.append("  動作: ").append(ACTION_TYPES[at >= 0 && at < ACTION_TYPES.length ? at : 0]).append("\n");
                sb.append("  參數: ").append(p).append("\n\n");
            } catch (Exception ignored) {}
        }
        txtMappings.setText(sb.toString());
    }
}
