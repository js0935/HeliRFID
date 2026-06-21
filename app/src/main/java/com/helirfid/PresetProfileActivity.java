package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class PresetProfileActivity extends BaseNfcActivity {

    private EditText editProfileName, editProfileActions;
    private Button btnProfileSave, btnProfileLoad, btnProfileDelete, btnProfileList;
    private TextView txtProfileStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preset_profile);

        editProfileName = findViewById(R.id.editProfileName);
        editProfileActions = findViewById(R.id.editProfileActions);
        btnProfileSave = findViewById(R.id.btnProfileSave);
        btnProfileLoad = findViewById(R.id.btnProfileLoad);
        btnProfileDelete = findViewById(R.id.btnProfileDelete);
        btnProfileList = findViewById(R.id.btnProfileList);
        txtProfileStatus = findViewById(R.id.txtProfileStatus);

        btnProfileSave.setOnClickListener(v -> {
            String name = editProfileName.getText().toString().trim();
            String actionsJson = editProfileActions.getText().toString().trim();
            if (name.isEmpty()) {
                txtProfileStatus.setText("請輸入設定檔名稱");
                return;
            }
            try {
                TaskProfile p = new TaskProfile();
                p.uid = name;
                p.tagName = name;
                p.enabled = true;
                if (!actionsJson.isEmpty()) {
                    org.json.JSONArray arr = new org.json.JSONArray(actionsJson);
                    for (int i = 0; i < arr.length(); i++) {
                        org.json.JSONObject o = arr.getJSONObject(i);
                        TaskProfile.TaskAction a = new TaskProfile.TaskAction();
                        a.type = o.optInt("type");
                        a.param1 = o.optString("param1");
                        a.param2 = o.optString("param2");
                        a.intParam = o.optInt("intParam");
                        a.boolParam = o.optBoolean("boolParam");
                        p.actions.add(a);
                    }
                }
                p.save(this);
                txtProfileStatus.setText("已儲存設定檔: " + name + " (" + p.actions.size() + " 個動作)");
                Toast.makeText(this, "設定檔已儲存: " + name, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                txtProfileStatus.setText("儲存失敗: " + e.getMessage());
                Toast.makeText(this, "儲存設定檔失敗", Toast.LENGTH_SHORT).show();
            }
        });

        btnProfileLoad.setOnClickListener(v -> {
            String name = editProfileName.getText().toString().trim();
            if (name.isEmpty()) {
                txtProfileStatus.setText("請輸入設定檔名稱");
                return;
            }
            TaskExecutor.execute(this, 162, name, "", 0, false);
            txtProfileStatus.setText("正在載入設定檔: " + name);
        });

        btnProfileDelete.setOnClickListener(v -> {
            String name = editProfileName.getText().toString().trim();
            if (name.isEmpty()) {
                txtProfileStatus.setText("請輸入設定檔名稱");
                return;
            }
            File dir = TaskProfile.getProfilesDir(this);
            File profileFile = new File(dir, name + ".json");
            if (profileFile.exists()) {
                profileFile.delete();
                txtProfileStatus.setText("已刪除設定檔: " + name);
            } else {
                txtProfileStatus.setText("設定檔不存在: " + name);
            }
        });

        btnProfileList.setOnClickListener(v -> {
            File dir = TaskProfile.getProfilesDir(this);
            File[] files = dir.listFiles((d, n) -> n.endsWith(".json"));
            if (files == null || files.length == 0) {
                txtProfileStatus.setText("暫無已儲存的設定檔");
                return;
            }
            StringBuilder sb = new StringBuilder("已儲存設定檔 (" + files.length + " 個):\n");
            for (File f : files) {
                sb.append("  ").append(f.getName().replace(".json", "")).append("\n");
            }
            txtProfileStatus.setText(sb.toString().trim());
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtProfileStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
