package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FelicaActivity extends BaseNfcActivity {

    private TextView txtInfo, txtResult;
    private EditText editService, editBlock, editWriteData;
    private Button btnPoll, btnReadBlock, btnWriteBlock, btnGetSystemCode;

    private Tag currentTag;
    private byte[] currentIdm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_felica);

        txtInfo = findViewById(R.id.txtFelicaInfo);
        txtResult = findViewById(R.id.txtFelicaResult);
        editService = findViewById(R.id.editFelicaService);
        editBlock = findViewById(R.id.editFelicaBlock);
        editWriteData = findViewById(R.id.editFelicaWriteData);
        btnPoll = findViewById(R.id.btnFelicaPoll);
        btnReadBlock = findViewById(R.id.btnFelicaReadBlock);
        btnWriteBlock = findViewById(R.id.btnFelicaWriteBlock);
        btnGetSystemCode = findViewById(R.id.btnFelicaGetSystemCode);

        btnPoll.setOnClickListener(v -> polling());
        btnReadBlock.setOnClickListener(v -> readBlock());
        btnWriteBlock.setOnClickListener(v -> writeBlock());
        btnGetSystemCode.setOnClickListener(v -> getSystemCode());

        editService.setText("0009");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;
        currentIdm = tag.getId();

        StringBuilder sb = new StringBuilder("FeliCa 卡片已偵測\n");
        sb.append("IDm: ").append(Converter.hex(tag.getId())).append("\n");
        for (String t : tag.getTechList())
            sb.append("  ").append(t.substring(t.lastIndexOf('.') + 1)).append("\n");
        txtInfo.setText(sb.toString());
    }

    private void polling() {
        if (currentTag == null) { toast("請先掃描 FeliCa 卡片"); return; }
        transceive(new byte[]{0x00, (byte)0xFF, (byte)0xFF, 0x01, 0x00}, "Polling");
    }

    private void getSystemCode() {
        if (currentIdm == null) { toast("請先掃描卡片"); return; }
        byte[] cmd = new byte[9];
        cmd[0] = 0x0C;
        System.arraycopy(currentIdm, 0, cmd, 1, 8);
        transceive(cmd, "Get System Code");
    }

    private void readBlock() {
        if (currentIdm == null) { toast("請先掃描卡片"); return; }
        String svc = editService.getText().toString().trim().replace(" ", "");
        String blk = editBlock.getText().toString().trim();
        if (svc.length() != 4) { toast("Service Code 需 4 hex"); return; }
        if (TextUtils.isEmpty(blk)) { toast("請輸入區塊號碼"); return; }

        int blockNum;
        try {
            blockNum = Integer.parseInt(blk);
        } catch (NumberFormatException e) {
            toast("區塊號碼需為數字"); return;
        }

        byte[] cmd = new byte[1 + 8 + 2 + 2];
        cmd[0] = 0x06;
        System.arraycopy(currentIdm, 0, cmd, 1, 8);
        cmd[9] = (byte) Integer.parseInt(svc.substring(2, 4), 16);
        cmd[10] = (byte) Integer.parseInt(svc.substring(0, 2), 16);
        cmd[11] = 0x01;
        cmd[12] = (byte) blockNum;
        cmd[13] = (byte) 0x80;
        transceive(cmd, "Read Block " + blockNum + " (Svc " + svc + ")");
    }

    private void writeBlock() {
        if (currentIdm == null) { toast("請先掃描卡片"); return; }
        String svc = editService.getText().toString().trim().replace(" ", "");
        String blk = editBlock.getText().toString().trim();
        String data = editWriteData.getText().toString().trim().replace(" ", "");
        if (svc.length() != 4) { toast("Service Code 需 4 hex"); return; }
        if (data.length() != 32) { toast("寫入資料需 16 bytes (32 hex)"); return; }
        int blockNum;
        try {
            blockNum = Integer.parseInt(blk);
        } catch (NumberFormatException e) {
            toast("區塊號碼需為數字"); return;
        }

        byte[] cmd = new byte[1 + 8 + 2 + 2 + 16];
        cmd[0] = 0x08;
        System.arraycopy(currentIdm, 0, cmd, 1, 8);
        cmd[9] = (byte) Integer.parseInt(svc.substring(2, 4), 16);
        cmd[10] = (byte) Integer.parseInt(svc.substring(0, 2), 16);
        cmd[11] = 0x01;
        cmd[12] = (byte) blockNum;
        cmd[13] = (byte) 0x80;
        byte[] writeBytes = Converter.hexToBytes(data);
        System.arraycopy(writeBytes, 0, cmd, 14, 16);
        transceive(cmd, "Write Block " + blockNum);
    }

    private void transceive(byte[] cmd, String label) {
        new Thread(() -> {
            try {
                NfcF nfcF = NfcF.get(currentTag);
                if (nfcF == null) { runOnUiThread(() -> txtResult.setText("不支援 NfcF")); return; }
                nfcF.connect();
                byte[] resp = nfcF.transceive(cmd);
                nfcF.close();

                StringBuilder sb = new StringBuilder("=== ").append(label).append(" ===\n");
                sb.append("CMD: ").append(Converter.hex(cmd)).append("\n");
                if (resp != null) {
                    sb.append("RESP (").append(resp.length).append("):\n");
                    for (int i = 0; i < resp.length; i += 16) {
                        sb.append(String.format("%04X: ", i));
                        for (int j = 0; j < 16 && i + j < resp.length; j++)
                            sb.append(String.format("%02X ", resp[i + j]));
                        sb.append(" ");
                        for (int j = 0; j < 16 && i + j < resp.length; j++) {
                            byte b = resp[i + j];
                            sb.append((b >= 0x20 && b < 0x7F) ? (char) b : '.');
                        }
                        sb.append("\n");
                    }
                } else {
                    sb.append("(無回應)");
                }
                final String res = sb.toString();
                runOnUiThread(() -> txtResult.setText(res));
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText(label + " 錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
