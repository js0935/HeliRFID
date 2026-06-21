/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HceActivity extends BaseNfcActivity {

    EditText editHceUid, editHceData;
    Button btnStart, btnStop;
    TextView txtHceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hce);

        editHceUid = findViewById(R.id.editHceUid);
        editHceData = findViewById(R.id.editHceData);
        btnStart = findViewById(R.id.btnStartHce);
        btnStop = findViewById(R.id.btnStopHce);
        txtHceStatus = findViewById(R.id.txtHceStatus);

        btnStart.setOnClickListener(v -> {
            String uid = editHceUid.getText().toString().trim().replace(" ", "");
            String data = editHceData.getText().toString().trim();

            if (uid.isEmpty()) uid = "04123456";
            if (uid.length() < 8) {
                Toast.makeText(this, "UID 至少 4 bytes (8 hex)", Toast.LENGTH_SHORT).show();
                return;
            }

            txtHceStatus.setText("HCE 服務狀態:\n"
                    + "模擬 UID: " + uid + "\n"
                    + "資料: " + (data.isEmpty() ? "(無)" : data) + "\n"
                    + "狀態: 已啟動 (需 Android HCE 支援)\n"
                    + "請將另一台裝置靠近本機背面\n\n"
                    + "⚠ 注意: HCE 需要在 AndroidManifest.xml\n"
                    + "中註冊服務並使用 NFC 模擬卡片技術。\n"
                    + "此功能為基礎展示框架。");

            Toast.makeText(this, "HCE 啟動 (展示模式)", Toast.LENGTH_SHORT).show();
        });

        btnStop.setOnClickListener(v -> {
            txtHceStatus.setText("HCE 服務已停止");
            Toast.makeText(this, "HCE 已停止", Toast.LENGTH_SHORT).show();
        });
    }
}
