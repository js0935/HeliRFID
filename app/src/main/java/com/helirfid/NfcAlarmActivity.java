/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class NfcAlarmActivity extends BaseNfcActivity {

    private EditText editAlarmTime, editAlarmLabel;
    private CheckBox chkAlarmWeekday;
    private Button btnAlarmSet, btnAlarmWeekday;
    private TextView txtAlarmStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_alarm);

        editAlarmTime = findViewById(R.id.editAlarmTime);
        editAlarmLabel = findViewById(R.id.editAlarmLabel);
        chkAlarmWeekday = findViewById(R.id.chkAlarmWeekday);
        btnAlarmSet = findViewById(R.id.btnAlarmSet);
        btnAlarmWeekday = findViewById(R.id.btnAlarmWeekday);
        txtAlarmStatus = findViewById(R.id.txtAlarmStatus);

        btnAlarmSet.setOnClickListener(v -> {
            String time = editAlarmTime.getText().toString().trim();
            if (time.isEmpty()) {
                txtAlarmStatus.setText("請輸入鬧鐘時間");
                return;
            }
            String label = editAlarmLabel.getText().toString().trim();
            TaskExecutor.execute(this, 8, time, label, 0, false);
            txtAlarmStatus.setText("鬧鐘已設定於 " + time);
        });

        btnAlarmWeekday.setOnClickListener(v -> {
            String time = editAlarmTime.getText().toString().trim();
            if (time.isEmpty()) {
                txtAlarmStatus.setText("請輸入鬧鐘時間");
                return;
            }
            String label = editAlarmLabel.getText().toString().trim();
            TaskExecutor.execute(this, 273, time, label, 0, false);
            txtAlarmStatus.setText("工作日鬧鐘已設定於 " + time);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtAlarmStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
