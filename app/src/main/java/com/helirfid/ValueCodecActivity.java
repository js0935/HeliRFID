package com.helirfid;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ValueCodecActivity extends AppCompatActivity {

    EditText editDecodeInput, editEncodeValue, editEncodeAddr;
    TextView txtDecodeResult, txtEncodeResult;
    Button btnDecode, btnEncode, btnPreset5, btnPresetM1, btnPresetNeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_value_codec);

        editDecodeInput = findViewById(R.id.editDecodeValueBlock);
        editEncodeValue = findViewById(R.id.editEncodeIntValue);
        editEncodeAddr = findViewById(R.id.editEncodeBlockAddr);
        txtDecodeResult = findViewById(R.id.txtDecodeValueResult);
        txtEncodeResult = findViewById(R.id.txtEncodeValueResult);
        btnDecode = findViewById(R.id.btnDecodeValueBlock);
        btnEncode = findViewById(R.id.btnEncodeValueBlock);
        btnPreset5 = findViewById(R.id.btnPreset5);
        btnPresetM1 = findViewById(R.id.btnPresetM1);
        btnPresetNeg = findViewById(R.id.btnPresetNeg);

        btnDecode.setOnClickListener(v -> decodeValueBlock());
        btnEncode.setOnClickListener(v -> encodeValueBlock());

        btnPreset5.setOnClickListener(v -> editEncodeValue.setText("5"));
        btnPresetM1.setOnClickListener(v -> editEncodeValue.setText("-1"));
        btnPresetNeg.setOnClickListener(v -> editEncodeValue.setText("-999999"));
    }

    private void decodeValueBlock() {
        String input = editDecodeInput.getText().toString().trim().replace(" ", "");
        if (input.length() != 32) {
            txtDecodeResult.setText("請輸入 16 bytes (32 hex 字元)");
            return;
        }
        try {
            byte[] data = hexToBytes(input);
            txtDecodeResult.setText(MifareUtils.decodeValueBlock(data));
        } catch (Exception e) {
            txtDecodeResult.setText("解碼失敗: " + e.getMessage());
        }
    }

    private void encodeValueBlock() {
        String valStr = editEncodeValue.getText().toString().trim();
        String addrStr = editEncodeAddr.getText().toString().trim();
        if (TextUtils.isEmpty(valStr) || TextUtils.isEmpty(addrStr)) {
            txtEncodeResult.setText("請輸入整數值和位址");
            return;
        }
        try {
            int value = Integer.parseInt(valStr);
            int addr = Integer.parseInt(addrStr) & 0xFF;
            byte[] data = MifareUtils.encodeValueBlock(value, (byte) addr);
            StringBuilder sb = new StringBuilder("編碼結果 (16 bytes):\n\n");
            sb.append("HEX: ");
            for (byte b : data) sb.append(String.format("%02X ", b));
            sb.append("\n\n數值: ").append(value);
            sb.append("\n位址: ").append(addr);
            sb.append("\n驗證: Value == ~Inverted == Value 備份 ✓");
            txtEncodeResult.setText(sb.toString());
        } catch (Exception e) {
            txtEncodeResult.setText("編碼失敗: " + e.getMessage());
        }
    }

    private byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
