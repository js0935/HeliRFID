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
import android.text.InputType;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class KeyManagementActivity extends BaseNfcActivity {

    private static final int REQUEST_IMPORT_KEYS = 200;

    EditText editFileName;
    Button btnCreate, btnImport;
    ListView listView;
    List<String> fileNames;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_management);

        editFileName = findViewById(R.id.editNewKeyFileName);
        btnCreate = findViewById(R.id.btnCreateKeyFile);
        btnImport = findViewById(R.id.btnImportKeyFile);
        listView = findViewById(R.id.listKeyFiles);

        refreshList();

        btnImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, REQUEST_IMPORT_KEYS);
        });

        btnCreate.setOnClickListener(v -> {
            String name = editFileName.getText().toString().trim();
            if (!name.endsWith(".keys")) name += ".keys";
            if (name.isEmpty() || name.equals(".keys")) {
                Toast.makeText(this, "請輸入檔名", Toast.LENGTH_SHORT).show();
                return;
            }
            if (KeyManager.saveKeys(this, name, new ArrayList<>())) {
                Toast.makeText(this, "金鑰檔案已建立", Toast.LENGTH_SHORT).show();
                refreshList();
                editFileName.setText("");
            } else {
                Toast.makeText(this, "建立失敗", Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String fileName = fileNames.get(position);
            showKeyFileDialog(fileName);
        });
    }

    private void refreshList() {
        fileNames = KeyManager.getKeyFileNames(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
        listView.setAdapter(adapter);
    }

    private void showKeyFileDialog(String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fileName);

        StringBuilder content = new StringBuilder();
        List<byte[]> keys = KeyManager.loadKeys(this, fileName);
        for (int i = 0; i < keys.size(); i++) {
            byte[] key = keys.get(i);
            StringBuilder hex = new StringBuilder();
            for (byte b : key) hex.append(String.format("%02X ", b));
            content.append("Key ").append(i+1).append(": ").append(hex.toString().trim()).append("\n");
        }
        if (keys.isEmpty()) content.append("(空檔案)");

        builder.setMessage(content.toString());
        builder.setPositiveButton("新增金鑰", (DialogInterface dialog, int which) -> showAddKeyDialog(fileName));
        builder.setNeutralButton("刪除檔案", (DialogInterface dialog, int which) -> {
            if (KeyManager.deleteKeyFile(this, fileName)) {
                Toast.makeText(this, "已刪除", Toast.LENGTH_SHORT).show();
                refreshList();
            }
        });
        builder.setNegativeButton("關閉", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMPORT_KEYS && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            importKeyFile(uri);
        }
    }

    private void importKeyFile(Uri uri) {
        try {
            List<String> hexLines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    String hex = line.toUpperCase().replace(" ", "");
                    if (hex.length() == 12 && hex.matches("[0-9A-F]+")) {
                        hexLines.add(hex);
                    }
                }
            }

            String fileName = uri.getLastPathSegment();
            if (fileName == null) fileName = "imported.keys";
            if (!fileName.endsWith(".keys")) fileName += ".keys";

            if (KeyManager.saveKeys(this, fileName, hexLines)) {
                Toast.makeText(this, "已匯入 " + hexLines.size() + " 個金鑰到 " + fileName, Toast.LENGTH_LONG).show();
                refreshList();
            } else {
                Toast.makeText(this, "匯入失敗", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "匯入失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddKeyDialog(String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("新增金鑰到 " + fileName);

        EditText input = new EditText(this);
        input.setHint("6 bytes hex (12 字元，如 AABBCCDDEEFF)");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTypeface(android.graphics.Typeface.MONOSPACE);
        builder.setView(input);

        builder.setPositiveButton("新增", (DialogInterface dialog, int which) -> {
            String keyHex = input.getText().toString().trim().replace(" ", "").toUpperCase();
            if (keyHex.length() != 12) {
                Toast.makeText(this, "請輸入 12 個 16 進制字元 (6 bytes)", Toast.LENGTH_SHORT).show();
                return;
            }
            List<byte[]> existing = KeyManager.loadKeys(this, fileName);
            List<String> hexLines = new ArrayList<>();
            for (byte[] k : existing) {
                StringBuilder h = new StringBuilder();
                for (byte b : k) h.append(String.format("%02X", b));
                hexLines.add(h.toString());
            }
            hexLines.add(keyHex);
            if (KeyManager.saveKeys(this, fileName, hexLines)) {
                Toast.makeText(this, "金鑰已新增", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
}
