package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NfcVActivity extends BaseNfcActivity {

    private TextView txtInfo, txtResult, txtAfiDisplay, txtDsfidDisplay;
    private EditText editBlock, editWriteData, editBlockCount;
    private EditText editSetAfi, editSetDsfid;
    private Button btnGetInfo, btnReadBlock, btnReadMulti, btnWriteBlock, btnLockBlock;
    private Button btnSetAfi, btnSetDsfid;

    private Tag currentTag;
    private int currentBlockSize = 4;
    private int currentMemSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcv);

        txtInfo = findViewById(R.id.txtNfcVInfo);
        txtResult = findViewById(R.id.txtNfcVResult);
        txtAfiDisplay = findViewById(R.id.txtNfcVAfi);
        txtDsfidDisplay = findViewById(R.id.txtNfcVDsfid);
        editBlock = findViewById(R.id.editNfcVBlock);
        editWriteData = findViewById(R.id.editNfcVWriteData);
        editBlockCount = findViewById(R.id.editNfcVBlockCount);
        editSetAfi = findViewById(R.id.editNfcVSetAfi);
        editSetDsfid = findViewById(R.id.editNfcVSetDsfid);
        btnGetInfo = findViewById(R.id.btnNfcVGetInfo);
        btnReadBlock = findViewById(R.id.btnNfcVReadBlock);
        btnReadMulti = findViewById(R.id.btnNfcVReadMulti);
        btnWriteBlock = findViewById(R.id.btnNfcVWriteBlock);
        btnLockBlock = findViewById(R.id.btnNfcVLockBlock);
        btnSetAfi = findViewById(R.id.btnNfcVSetAfi);
        btnSetDsfid = findViewById(R.id.btnNfcVSetDsfid);

        btnGetInfo.setOnClickListener(v -> getSystemInfo());
        btnReadBlock.setOnClickListener(v -> readBlock());
        btnReadMulti.setOnClickListener(v -> readMultiple());
        btnWriteBlock.setOnClickListener(v -> writeBlock());
        btnLockBlock.setOnClickListener(v -> lockBlock());
        btnSetAfi.setOnClickListener(v -> setAfi());
        btnSetDsfid.setOnClickListener(v -> setDsfid());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;

        StringBuilder sb = new StringBuilder("ISO 15693 卡片已偵測\n");
        sb.append("UID: ").append(Converter.hex(tag.getId())).append("\n");
        for (String t : tag.getTechList())
            sb.append("  ").append(t.substring(t.lastIndexOf('.') + 1)).append("\n");
        txtInfo.setText(sb.toString());
    }

    private void getSystemInfo() {
        if (currentTag == null) { toast("請先掃描卡片"); return; }
        transceive(new byte[]{0x01, (byte)0x2B, 0x00}, "Get System Info", true);
    }

    private void readBlock() {
        if (currentTag == null) { toast("請先掃描卡片"); return; }
        String blk = editBlock.getText().toString().trim();
        if (TextUtils.isEmpty(blk)) { toast("請輸入區塊號碼"); return; }
        int block = Integer.parseInt(blk);
        transceive(new byte[]{0x01, 0x20, (byte) block}, "Read Block " + block, false);
    }

    private void readMultiple() {
        if (currentTag == null) { toast("請先掃描卡片"); return; }
        String blk = editBlock.getText().toString().trim();
        String cnt = editBlockCount.getText().toString().trim();
        if (TextUtils.isEmpty(blk)) { toast("請輸入起始區塊"); return; }
        int start = Integer.parseInt(blk);
        int count;
        try { count = Integer.parseInt(cnt); } catch (NumberFormatException e) { count = 4; }
        if (count < 1 || count > 64) { toast("數量需在 1-64 之間"); return; }
        transceive(new byte[]{0x01, 0x23, (byte) start, (byte) count},
                "Read Multi (start=" + start + ", count=" + count + ")", false);
    }

    private void writeBlock() {
        if (currentTag == null) { toast("請先掃描卡片"); return; }
        String blk = editBlock.getText().toString().trim();
        String data = editWriteData.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(blk)) { toast("請輸入區塊號碼"); return; }
        if (data.length() != 8) { toast("資料需為 4 bytes (8 hex)"); return; }
        int block = Integer.parseInt(blk);
        byte[] cmd = new byte[5];
        cmd[0] = 0x01;
        cmd[1] = 0x21;
        cmd[2] = (byte) block;
        byte[] dataBytes = Converter.hexToBytes(data);
        System.arraycopy(dataBytes, 0, cmd, 3, 4);
        transceive(cmd, "Write Block " + block, false);
    }

    private void lockBlock() {
        if (currentTag == null) { toast("請先掃描卡片"); return; }
        String blk = editBlock.getText().toString().trim();
        if (TextUtils.isEmpty(blk)) { toast("請輸入區塊號碼"); return; }
        int block = Integer.parseInt(blk);
        transceive(new byte[]{0x01, 0x22, (byte) block}, "Lock Block " + block, false);
    }

    private void transceive(byte[] cmd, String label, boolean isInfo) {
        new Thread(() -> {
            try {
                NfcV nfcV = NfcV.get(currentTag);
                if (nfcV == null) { runOnUiThread(() -> txtResult.setText("不支援 NfcV")); return; }
                nfcV.connect();

                if (isInfo) {
                    byte[] resp = nfcV.transceive(cmd);
                    decodeSystemInfo(resp);
                } else {
                    byte[] resp = nfcV.transceive(cmd);
                    StringBuilder sb = new StringBuilder("=== ").append(label).append(" ===\n");
                    sb.append("CMD: ").append(Converter.hex(cmd)).append("\n");
                    if (resp != null && resp.length > 1) {
                        sb.append("RESP (").append(resp.length).append("):\n");
                        for (int i = 1; i < resp.length; i++)
                            sb.append(String.format("%02X ", resp[i]));
                        sb.append("\n");
                        // ASCII
                        sb.append("ASC: ");
                        for (int i = 1; i < resp.length; i++) {
                            byte b = resp[i];
                            sb.append((b >= 0x20 && b < 0x7F) ? (char) b : '.');
                        }
                        sb.append("\n");
                    } else {
                        sb.append("(無回應或錯誤 flag=")
                                .append(resp != null && resp.length > 0 ? String.format("%02X", resp[0]) : "?")
                                .append(")");
                    }
                    final String res = sb.toString();
                    runOnUiThread(() -> txtResult.setText(res));
                }
                nfcV.close();
            } catch (Exception e) {
                runOnUiThread(() -> txtResult.setText(label + " 錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private void decodeSystemInfo(byte[] resp) {
        if (resp == null || resp.length < 2) {
            runOnUiThread(() -> txtResult.setText("Get System Info 無回應"));
            return;
        }
        StringBuilder sb = new StringBuilder("=== System Info ===\n");
        sb.append("Flag: 0x").append(String.format("%02X", resp[0])).append("\n");

        final String[] afiDescriptions = {
                "0x00: All families", "0x01: Transportation", "0x02: Financial",
                "0x03: Identification", "0x04: Telecommunications", "0x05: Medical",
                "0x06: Broadcasting", "0x07: Logistics", "0x08: Automotive",
                "0x09: Tooling", "0x0A: Food", "0x0B: Animals",
                "0x0C: Library/Media", "0x0D: Electronics", "0x0E: Chemical",
                "0x0F: Other/Proprietary"
        };

        int idx = 1;
        if (resp.length > idx) {
            byte infoFlags = resp[idx++];
            sb.append("Info Flags: 0x").append(String.format("%02X", infoFlags)).append("\n");

            if ((infoFlags & 0x01) != 0 && idx + 8 <= resp.length) {
                sb.append("UID: ");
                for (int i = idx; i < idx + 8; i++)
                    sb.append(String.format("%02X", resp[i]));
                sb.append("\n");
                idx += 8;
            }

            final int dsfidVal;
            if ((infoFlags & 0x02) != 0 && idx < resp.length) {
                dsfidVal = resp[idx] & 0xFF;
                sb.append("DSFID: 0x").append(String.format("%02X", resp[idx++])).append("\n");
            } else {
                dsfidVal = -1;
            }

            final int afiVal;
            if ((infoFlags & 0x04) != 0 && idx < resp.length) {
                afiVal = resp[idx] & 0xFF;
                sb.append("AFI: 0x").append(String.format("%02X", resp[idx++])).append("\n");
            } else {
                afiVal = -1;
            }

            if ((infoFlags & 0x08) != 0 && idx + 2 <= resp.length) {
                currentMemSize = ((resp[idx] & 0xFF) << 8) | (resp[idx + 1] & 0xFF);
                sb.append("Memory Size: ").append(currentMemSize).append(" blocks\n");
                idx += 2;
            }
            if ((infoFlags & 0x10) != 0 && idx < resp.length) {
                sb.append("IC Ref: 0x").append(String.format("%02X", resp[idx++])).append("\n");
            }
            if ((infoFlags & 0x20) != 0 && idx < resp.length) {
                currentBlockSize = resp[idx++] & 0xFF;
                sb.append("Block Size: ").append(currentBlockSize).append(" bytes\n");
            }

            // Update AFI/DSFID displays
            String afiDesc = "";
            String dsfidStr = "N/A";
            if (afiVal >= 0) {
                String hexStr = String.format("0x%02X", afiVal);
                for (String d : afiDescriptions) {
                    if (d.startsWith(hexStr + ":")) { afiDesc = d; break; }
                }
                if (afiDesc.isEmpty()) afiDesc = hexStr + ": Proprietary/Unknown";
                txtAfiDisplay.setText(afiDesc);
            } else {
                txtAfiDisplay.setText("AFI: Not available");
            }
            if (dsfidVal >= 0) {
                dsfidStr = String.format("0x%02X", dsfidVal);
                txtDsfidDisplay.setText("DSFID: " + dsfidStr + " (Data Storage Family ID)");
            } else {
                txtDsfidDisplay.setText("DSFID: Not available");
            }
        }

        sb.append("\nRaw: ").append(Converter.hex(resp));
        final String res = sb.toString();
        runOnUiThread(() -> {
            txtResult.setText(res);
            String curInfo = txtInfo.getText().toString();
            txtInfo.setText(curInfo + "\n(System Info 已取得)");
        });
    }

    private void setAfi() {
        if (currentTag == null) { toast("請先掃描卡片"); return; }
        String val = editSetAfi.getText().toString().trim();
        if (TextUtils.isEmpty(val)) { toast("請輸入 AFI 值 (hex)"); return; }
        try {
            int afi = Integer.parseInt(val.replace("0x", "").replace("0X", ""), 16);
            if (afi < 0 || afi > 255) { toast("AFI 需在 0x00-0xFF 之間"); return; }
            transceive(new byte[]{0x01, 0x27, (byte) afi}, "Set AFI = 0x" + String.format("%02X", afi), false);
        } catch (NumberFormatException e) {
            toast("請輸入有效的 hex 值");
        }
    }

    private void setDsfid() {
        if (currentTag == null) { toast("請先掃描卡片"); return; }
        String val = editSetDsfid.getText().toString().trim();
        if (TextUtils.isEmpty(val)) { toast("請輸入 DSFID 值 (hex)"); return; }
        try {
            int dsfid = Integer.parseInt(val.replace("0x", "").replace("0X", ""), 16);
            if (dsfid < 0 || dsfid > 255) { toast("DSFID 需在 0x00-0xFF 之間"); return; }
            transceive(new byte[]{0x01, 0x29, (byte) dsfid}, "Set DSFID = 0x" + String.format("%02X", dsfid), false);
        } catch (NumberFormatException e) {
            toast("請輸入有效的 hex 值");
        }
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}
