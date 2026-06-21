/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class DumpEditorActivity extends BaseNfcActivity {

    private static final int REQUEST_IMPORT_DUMP = 300;

    ListView listBlocks;
    Button btnSave, btnImport, btnClear;
    TextView txtDumpInfo;

    BlockAdapter adapter;
    ArrayList<byte[]> blocks = new ArrayList<>();
    String currentFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dump_editor);

        listBlocks = findViewById(R.id.listDumpBlocks);
        btnSave = findViewById(R.id.btnSaveEditedDump);
        btnImport = findViewById(R.id.btnImportDump);
        btnClear = findViewById(R.id.btnClearDump);
        txtDumpInfo = findViewById(R.id.txtDumpInfo);

        adapter = new BlockAdapter();
        listBlocks.setAdapter(adapter);

        byte[] dumpData = DumpStore.getDumpData();
        if (dumpData != null) {
            loadDump(dumpData);
        }

        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/octet-stream", "text/plain"});
            startActivityForResult(intent, REQUEST_IMPORT_DUMP);
        });

        btnSave.setOnClickListener(v -> {
            if (blocks.isEmpty()) {
                Toast.makeText(this, "沒有資料可儲存", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                File dir = new File(getExternalFilesDir(null), "Dumps");
                dir.mkdirs();
                File file = new File(dir, "edited_dump_" + System.currentTimeMillis() + ".bin");
                FileOutputStream fos = new FileOutputStream(file);
                for (byte[] blk : blocks) fos.write(blk);
                fos.close();
                currentFilePath = file.getAbsolutePath();
                txtDumpInfo.setText("已儲存: " + file.getName() + " (" + (blocks.size() * 16) + " bytes)");
                Toast.makeText(this, "儲存成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "儲存失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnClear.setOnClickListener(v -> {
            blocks.clear();
            adapter.notifyDataSetChanged();
            txtDumpInfo.setText("已清除");
            DumpStore.clearDump();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT_DUMP && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            importDumpFile(uri);
        }
    }

    private void importDumpFile(Uri uri) {
        try {
            String fileName = uri.getLastPathSegment();
            if (fileName == null) fileName = "";
            boolean isEml = fileName.toLowerCase().endsWith(".eml");

            byte[] fileBytes;
            try (java.io.InputStream is = getContentResolver().openInputStream(uri)) {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[4096];
                int n;
                while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
                fileBytes = baos.toByteArray();
            }

            if (isEml) {
                String content = new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
                ArrayList<Byte> allBytes = new ArrayList<>();
                String[] lines = content.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("+") || line.isEmpty()) continue;
                    String[] parts = line.split(":\\s+");
                    if (parts.length == 2) {
                        String hex = parts[1].replace(" ", "");
                        for (int i = 0; i < hex.length(); i += 2) {
                            allBytes.add((byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                    + Character.digit(hex.charAt(i + 1), 16)));
                        }
                    }
                }
                byte[] data = new byte[allBytes.size()];
                for (int i = 0; i < allBytes.size(); i++) data[i] = allBytes.get(i);
                loadDump(data);
            } else {
                loadDump(fileBytes);
            }
            Toast.makeText(this, "已匯入: " + fileName + " (" + (blocks.size() * 16) + " bytes)", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "匯入失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDump(byte[] data) {
        blocks.clear();
        int blockCount = data.length / 16;
        for (int i = 0; i < blockCount; i++) {
            byte[] block = new byte[16];
            System.arraycopy(data, i * 16, block, 0, 16);
            blocks.add(block);
        }
        adapter.notifyDataSetChanged();
        txtDumpInfo.setText("已載入 " + blockCount + " 區塊 (" + data.length + " bytes)");
        DumpStore.setDumpData(data);
    }

    private class BlockAdapter extends BaseAdapter {
        @Override
        public int getCount() { return blocks.size(); }

        @Override
        public Object getItem(int pos) { return blocks.get(pos); }

        @Override
        public long getItemId(int pos) { return pos; }

        @Override
        public View getView(int pos, View convert, ViewGroup parent) {
            if (convert == null)
                convert = getLayoutInflater().inflate(R.layout.dump_editor_item, parent, false);

            TextView txtBlockNum = convert.findViewById(R.id.txtEditorBlockNum);
            TextView txtBlockHex = convert.findViewById(R.id.txtEditorBlockHex);
            TextView txtBlockAscii = convert.findViewById(R.id.txtEditorBlockAscii);

            byte[] blk = blocks.get(pos);
            txtBlockNum.setText(String.format("[%03d]", pos));

            StringBuilder hex = new StringBuilder();
            StringBuilder ascii = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                hex.append(String.format("%02X ", blk[i]));
                byte b = blk[i];
                ascii.append((b >= 0x20 && b <= 0x7E) ? (char) b : '.');
            }
            txtBlockHex.setText(hex.toString().trim());
            txtBlockAscii.setText(ascii.toString());

            if (pos % 4 == 3) {
                convert.setBackgroundColor(0x20FFAA00);
            } else {
                convert.setBackgroundColor(0x00000000);
            }

            convert.setOnClickListener(v -> showEditDialog(pos));
            return convert;
        }
    }

    private void showEditDialog(int pos) {
        byte[] blk = blocks.get(pos);
        StringBuilder sb = new StringBuilder();
        for (byte b : blk) sb.append(String.format("%02X ", b));

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(47)});
        input.setText(sb.toString().trim());
        input.setSelection(input.getText().length());
        input.setSingleLine(true);
        input.setTypeface(android.graphics.Typeface.MONOSPACE);

        new AlertDialog.Builder(this)
                .setTitle("編輯區塊 " + pos)
                .setMessage("輸入 16 bytes hex (空格分隔)")
                .setView(input)
                .setPositiveButton("確定", (DialogInterface dialog, int which) -> {
                    String hex = input.getText().toString().trim().replace(" ", "");
                    if (hex.length() != 32) {
                        Toast.makeText(this, "需要 32 hex 字元", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    byte[] newBlk = new byte[16];
                    for (int i = 0; i < 16; i++)
                        newBlk[i] = (byte) ((Character.digit(hex.charAt(i * 2), 16) << 4)
                                + Character.digit(hex.charAt(i * 2 + 1), 16));
                    blocks.set(pos, newBlk);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
