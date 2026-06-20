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

public class DataConverterActivity extends AppCompatActivity {

    EditText editInput;
    RadioGroup rgInputType, rgOutputType;
    TextView txtOutput;
    Button btnConvert, btnSwap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_converter);

        editInput = findViewById(R.id.editConverterInput);
        rgInputType = findViewById(R.id.rgInputType);
        rgOutputType = findViewById(R.id.rgOutputType);
        txtOutput = findViewById(R.id.txtConverterOutput);
        btnConvert = findViewById(R.id.btnConvert);
        btnSwap = findViewById(R.id.btnSwapConverter);

        btnConvert.setOnClickListener(v -> convert());

        btnSwap.setOnClickListener(v -> {
            String output = txtOutput.getText().toString();
            if (!output.isEmpty()) {
                editInput.setText(output.replace(" ", "").replace("\n", " "));
                int inType = rgInputType.getCheckedRadioButtonId();
                int outType = rgOutputType.getCheckedRadioButtonId();
                rgInputType.check(outType);
                rgOutputType.check(inType);
                convert();
            }
        });
    }

    private void convert() {
        String input = editInput.getText().toString().trim();
        if (input.isEmpty()) {
            txtOutput.setText("請輸入資料");
            return;
        }

        int inType = getId(rgInputType.getCheckedRadioButtonId());
        int outType = getId(rgOutputType.getCheckedRadioButtonId());

        try {
            byte[] data = parseInput(input, inType);
            txtOutput.setText(formatOutput(data, outType));
        } catch (Exception e) {
            txtOutput.setText("轉換失敗: " + e.getMessage());
        }
    }

    private int getId(int radioId) {
        if (radioId == R.id.radioInHex || radioId == R.id.radioOutHex) return 0;
        if (radioId == R.id.radioInAscii || radioId == R.id.radioOutAscii) return 1;
        return 2;
    }

    private byte[] parseInput(String input, int type) {
        switch (type) {
            case 0: {
                String hex = input.replace(" ", "").replace(":", "").replace("\n", "").replace("\r", "");
                int len = hex.length();
                byte[] data = new byte[len / 2];
                for (int i = 0; i < len; i += 2)
                    data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                            + Character.digit(hex.charAt(i + 1), 16));
                return data;
            }
            case 1:
                return input.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            case 2: {
                String bin = input.replace(" ", "").replace("\n", "").replace("\r", "");
                int len = bin.length();
                byte[] data = new byte[(len + 7) / 8];
                for (int i = 0; i < len; i++) {
                    if (bin.charAt(i) == '1')
                        data[i / 8] |= (byte) (1 << (7 - (i % 8)));
                }
                return data;
            }
        }
        return null;
    }

    private String formatOutput(byte[] data, int type) {
        switch (type) {
            case 0: {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < data.length; i++) {
                    sb.append(String.format("%02X ", data[i]));
                    if ((i + 1) % 16 == 0) sb.append('\n');
                }
                return sb.toString().trim();
            }
            case 1: {
                StringBuilder sb = new StringBuilder();
                for (byte b : data)
                    sb.append((char) (b & 0xFF));
                return sb.toString();
            }
            case 2: {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < data.length; i++) {
                    for (int j = 7; j >= 0; j--)
                        sb.append((data[i] >> j) & 1);
                    sb.append(' ');
                }
                return sb.toString().trim();
            }
        }
        return "";
    }
}
