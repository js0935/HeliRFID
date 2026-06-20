/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;

public class UidGeneratorActivity extends AppCompatActivity {

    EditText editPrefix, editCount;
    RadioGroup rgUidLength;
    TextView txtResult;
    Button btnGenerate, btnCopy;
    SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uid_generator);

        editPrefix = findViewById(R.id.editUidPrefix);
        editCount = findViewById(R.id.editUidCount);
        rgUidLength = findViewById(R.id.rgUidLength);
        txtResult = findViewById(R.id.txtUidResult);
        btnGenerate = findViewById(R.id.btnGenerateUid);
        btnCopy = findViewById(R.id.btnCopyUids);

        btnGenerate.setOnClickListener(v -> generateUids());

        btnCopy.setOnClickListener(v -> {
            String text = txtResult.getText().toString();
            if (!text.isEmpty()) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                        getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("UIDs", text));
                android.widget.Toast.makeText(this, "已複製到剪貼簿", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateUids() {
        String prefix = editPrefix.getText().toString().trim().replace(" ", "").replace(":", "");
        int count;
        try {
            count = Integer.parseInt(editCount.getText().toString().trim());
            if (count < 1 || count > 100) count = 10;
        } catch (Exception e) {
            count = 10;
        }

        int uidBytes = 4;
        int checkedId = rgUidLength.getCheckedRadioButtonId();
        if (checkedId == R.id.radioUid7) uidBytes = 7;
        else if (checkedId == R.id.radioUid10) uidBytes = 10;

        int prefixBytes = prefix.length() / 2;
        int remaining = uidBytes - prefixBytes;
        if (remaining < 0) remaining = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("產生 ").append(count).append(" 組 UID (").append(uidBytes).append(" bytes)\n\n");

        for (int i = 0; i < count; i++) {
            StringBuilder uid = new StringBuilder(prefix);
            for (int j = 0; j < remaining; j++) {
                uid.append(String.format("%02X", random.nextInt(256)));
            }
            if (uid.length() > uidBytes * 2) {
                uid.setLength(uidBytes * 2);
            }
            sb.append(uid.toString());
            if (uidBytes == 4) {
                byte bcc = 0;
                String u = uid.toString();
                for (int j = 0; j < 8; j += 2)
                    bcc ^= (byte) Integer.parseInt(u.substring(j, j + 2), 16);
                sb.append("  BCC=").append(String.format("%02X", bcc));
            }
            sb.append("\n");
        }

        txtResult.setText(sb.toString());
    }
}
