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
import android.widget.TextView;

public class MediaControlActivity extends BaseNfcActivity {

    private Button btnMediaPlayPause, btnMediaNext, btnMediaPrev;
    private TextView txtMediaStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUseReaderMode(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_control);

        btnMediaPlayPause = findViewById(R.id.btnMediaPlayPause);
        btnMediaNext = findViewById(R.id.btnMediaNext);
        btnMediaPrev = findViewById(R.id.btnMediaPrev);
        txtMediaStatus = findViewById(R.id.txtMediaStatus);

        btnMediaPlayPause.setOnClickListener(v -> {
            TaskExecutor.execute(this, 113, "", "", 0, false);
            txtMediaStatus.setText("播放/暫停");
        });

        btnMediaNext.setOnClickListener(v -> {
            TaskExecutor.execute(this, 38, "", "", 0, false);
            txtMediaStatus.setText("下一首");
        });

        btnMediaPrev.setOnClickListener(v -> {
            TaskExecutor.execute(this, 39, "", "", 0, false);
            txtMediaStatus.setText("上一首");
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        txtMediaStatus.setText("感應到標籤: " + Converter.hex(tag.getId()));
        vibrate();
    }
}
