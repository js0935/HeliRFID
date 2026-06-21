package com.helirfid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileOperationsTaskActivity extends AppCompatActivity {

    private static final String[] OPERATIONS = {
            "建立資料夾", "複製檔案", "移動檔案", "刪除檔案",
            "刪除資料夾", "壓縮資料夾", "解壓縮", "寫入文字檔", "讀取文字檔"
    };

    private Spinner spinnerOp;
    private EditText editParam1, editParam2;
    private TextView txtResult;
    private Button btnExecute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_operations_task);

        spinnerOp = findViewById(R.id.spinnerFileOp);
        editParam1 = findViewById(R.id.editFileParam1);
        editParam2 = findViewById(R.id.editFileParam2);
        txtResult = findViewById(R.id.txtFileResult);
        btnExecute = findViewById(R.id.btnFileExecute);

        ArrayAdapter<String> opAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, OPERATIONS);
        opAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOp.setAdapter(opAdapter);

        spinnerOp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { updateUi(pos); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        btnExecute.setOnClickListener(v -> executeOperation());
        updateUi(0);
    }

    private void updateUi(int pos) {
        boolean hasParam2 = pos == 1 || pos == 2 || pos == 5 || pos == 6 || pos == 7;
        editParam2.setVisibility(hasParam2 ? View.VISIBLE : View.GONE);

        switch (pos) {
            case 0: editParam1.setHint("資料夾路徑"); editParam2.setHint(""); break;
            case 1: editParam1.setHint("來源檔案路徑"); editParam2.setHint("目的地路徑"); break;
            case 2: editParam1.setHint("來源檔案路徑"); editParam2.setHint("目的地路徑"); break;
            case 3: editParam1.setHint("檔案路徑"); editParam2.setHint(""); break;
            case 4: editParam1.setHint("資料夾路徑"); editParam2.setHint(""); break;
            case 5: editParam1.setHint("來源資料夾路徑"); editParam2.setHint("輸出 .zip 路徑"); break;
            case 6: editParam1.setHint("來源 .zip 路徑"); editParam2.setHint("目的地資料夾"); break;
            case 7: editParam1.setHint("檔案路徑"); editParam2.setHint("檔案內容"); break;
            case 8: editParam1.setHint("檔案路徑"); editParam2.setHint(""); break;
        }
    }

    private void executeOperation() {
        int op = spinnerOp.getSelectedItemPosition();
        String p1 = editParam1.getText().toString().trim();
        String p2 = editParam2.getText().toString().trim();

        if (p1.isEmpty() && op != 7 && op != 8) {
            Toast.makeText(this, "請輸入參數", Toast.LENGTH_SHORT).show(); return;
        }

        try {
            String result;
            switch (op) {
                case 0: result = createFolder(p1); break;
                case 1: result = copyFile(p1, p2); break;
                case 2: result = moveFile(p1, p2); break;
                case 3: result = deleteFileOp(p1); break;
                case 4: result = deleteFolder(p1); break;
                case 5: result = compressFolder(p1, p2); break;
                case 6: result = extractArchive(p1, p2); break;
                case 7: result = writeTextFile(p1, p2); break;
                case 8: result = readTextFile(p1); break;
                default: result = "未知操作";
            }
            txtResult.setText("結果:\n" + result);
        } catch (Exception e) {
            txtResult.setText("錯誤: " + e.getMessage());
        }
    }

    private String resolvePath(String path) {
        if (path == null || path.isEmpty()) return path;
        return path.replace("%FILES_DIR%", getFilesDir().getAbsolutePath())
                .replace("%CACHE_DIR%", getCacheDir().getAbsolutePath())
                .replace("%EXT_STORAGE%", Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    private String createFolder(String path) {
        File dir = new File(resolvePath(path));
        if (dir.exists()) return "資料夾已存在: " + dir.getAbsolutePath();
        if (dir.mkdirs()) return "資料夾建立成功: " + dir.getAbsolutePath();
        return "建立失敗: " + dir.getAbsolutePath();
    }

    private String copyFile(String src, String dst) {
        File srcFile = new File(resolvePath(src));
        File dstFile = new File(resolvePath(dst));
        if (!srcFile.exists()) return "來源檔案不存在: " + srcFile.getAbsolutePath();
        dstFile.getParentFile().mkdirs();
        try (InputStream is = new FileInputStream(srcFile); OutputStream os = new FileOutputStream(dstFile)) {
            byte[] buf = new byte[8192]; int len; long total = 0;
            while ((len = is.read(buf)) > 0) { os.write(buf, 0, len); total += len; }
            return "複製成功 (" + total + " bytes): " + dstFile.getAbsolutePath();
        } catch (Exception e) { return "複製失敗: " + e.getMessage(); }
    }

    private String moveFile(String src, String dst) {
        File srcFile = new File(resolvePath(src));
        File dstFile = new File(resolvePath(dst));
        if (!srcFile.exists()) return "來源檔案不存在";
        dstFile.getParentFile().mkdirs();
        if (srcFile.renameTo(dstFile)) return "移動成功";
        String copyResult = copyFile(src, dst);
        if (copyResult.startsWith("複製成功")) { srcFile.delete(); return "移動成功 (複製+刪除)"; }
        return "移動失敗";
    }

    private String deleteFileOp(String path) {
        File file = new File(resolvePath(path));
        if (!file.exists()) return "檔案不存在";
        return file.delete() ? "刪除成功" : "刪除失敗";
    }

    private String deleteFolder(String path) {
        File dir = new File(resolvePath(path));
        if (!dir.exists()) return "資料夾不存在";
        return deleteRecursive(dir) ? "資料夾已刪除" : "刪除失敗";
    }

    private boolean deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) for (File c : children) deleteRecursive(c);
        }
        return f.delete();
    }

    private String compressFolder(String src, String dst) {
        File srcDir = new File(resolvePath(src));
        File dstFile = new File(resolvePath(dst));
        if (!srcDir.exists() || !srcDir.isDirectory()) return "來源資料夾不存在";
        dstFile.getParentFile().mkdirs();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dstFile))) {
            zipRecursive(srcDir, srcDir.getName(), zos);
            return "壓縮成功: " + dstFile.getAbsolutePath();
        } catch (Exception e) { return "壓縮失敗: " + e.getMessage(); }
    }

    private void zipRecursive(File dir, String baseName, ZipOutputStream zos) throws Exception {
        File[] files = dir.listFiles();
        if (files == null) return;
        byte[] buf = new byte[8192];
        for (File f : files) {
            String entryName = baseName + "/" + f.getName();
            if (f.isDirectory()) { zipRecursive(f, entryName, zos); }
            else {
                zos.putNextEntry(new ZipEntry(entryName));
                try (FileInputStream fis = new FileInputStream(f)) { int len; while ((len = fis.read(buf)) > 0) zos.write(buf, 0, len); }
                zos.closeEntry();
            }
        }
    }

    private String extractArchive(String src, String dst) {
        File srcFile = new File(resolvePath(src));
        File dstDir = new File(resolvePath(dst));
        if (!srcFile.exists()) return "壓縮檔不存在";
        dstDir.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(srcFile))) {
            ZipEntry entry; byte[] buf = new byte[8192]; int count = 0;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(dstDir, entry.getName());
                if (entry.isDirectory()) { outFile.mkdirs(); }
                else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) { int len; while ((len = zis.read(buf)) > 0) fos.write(buf, 0, len); }
                    count++;
                }
                zis.closeEntry();
            }
            return "解壓縮成功: " + count + " 個檔案";
        } catch (Exception e) { return "解壓縮失敗: " + e.getMessage(); }
    }

    private String writeTextFile(String path, String content) {
        File file = new File(resolvePath(path));
        file.getParentFile().mkdirs();
        try (FileWriter fw = new FileWriter(file)) {
            fw.write(content);
            return "寫入成功: " + file.getAbsolutePath() + " (" + content.length() + " 字符)";
        } catch (Exception e) { return "寫入失敗: " + e.getMessage(); }
    }

    private String readTextFile(String path) {
        File file = new File(resolvePath(path));
        if (!file.exists()) return "檔案不存在: " + path;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder(); String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            return "=== " + file.getAbsolutePath() + " ===\n" + sb.toString();
        } catch (Exception e) { return "讀取失敗: " + e.getMessage(); }
    }
}
