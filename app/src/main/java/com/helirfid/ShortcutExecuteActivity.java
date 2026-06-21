package com.helirfid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class ShortcutExecuteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new android.widget.TextView(this));

        String uid = getIntent().getStringExtra("profile_uid");
        if (uid == null) {
            Toast.makeText(this, "無設定檔 UID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            TaskProfile profile = TaskProfile.load(this, uid);
            if (profile == null) {
                Toast.makeText(this, "找不到設定檔", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            String label = (profile.tagName != null && !profile.tagName.isEmpty()) ? profile.tagName : uid;
            Toast.makeText(this, "正在執行: " + label, Toast.LENGTH_SHORT).show();

            for (TaskProfile.TaskAction a : profile.actions) {
                TaskExecutor.execute(this, a.type, a.param1, a.param2, a.intParam, a.boolParam);
            }

            Toast.makeText(this, "已執行 " + profile.actions.size() + " 個動作", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "執行失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
