package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ReportExportActivity extends BaseNfcActivity {

    private Button btnReportCsv, btnReportCsvImport, btnReportHours;
    private TextView txtReportStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_export);

        btnReportCsv = findViewById(R.id.btnReportCsv);
        btnReportCsvImport = findViewById(R.id.btnReportCsvImport);
        btnReportHours = findViewById(R.id.btnReportHours);
        txtReportStatus = findViewById(R.id.txtReportStatus);

        btnReportCsv.setOnClickListener(v -> {
            TaskExecutor.execute(this, 219, "", "", 0, false);
            txtReportStatus.setText("正在匯出 CSV 日誌...");
        });

        btnReportCsvImport.setOnClickListener(v -> {
            TaskExecutor.execute(this, 218, "", "", 0, false);
            txtReportStatus.setText("正在匯入 CSV 批次...");
        });

        btnReportHours.setOnClickListener(v -> {
            TaskExecutor.execute(this, 272, "", "", 0, false);
            txtReportStatus.setText("正在產生工時報表...");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtReportStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
