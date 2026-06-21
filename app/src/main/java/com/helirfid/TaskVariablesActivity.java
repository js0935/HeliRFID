package com.helirfid;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class TaskVariablesActivity extends BaseNfcActivity {

    private Spinner spinnerCategory, spinnerFunction;
    private EditText editInput1, editInput2, editInput3;
    private TextView txtResult, txtPreview;
    private Button btnEvaluate;

    private static final String[] CATEGORIES = {"數學 (Math)", "字串 (String)", "加密 (Crypto)", "轉換 (Convert)", "邏輯 (Logic)"};

    private static final String[][] FUNCTIONS = {
            {"加法 (+)", "減法 (-)", "乘法 (×)", "除法 (÷)", "絕對值 (Abs)", "四捨五入 (Round)", "無條件捨去 (Floor)", "無條件進位 (Ceil)", "最小值 (Min)", "最大值 (Max)", "隨機數 (Random)"},
            {"轉小寫 (Lowercase)", "轉大寫 (Uppercase)", "去除空白 (Trim)", "長度 (Length)", "子字串 (Substring)", "取代 (Replace)"},
            {"MD5 雜湊", "SHA1 雜湊", "SHA256 雜湊", "Base64 編碼", "Base64 解碼"},
            {"Hex → Dec", "Dec → Hex", "文字 → Hex", "Hex → 文字", "二進位 → Hex", "Hex → 二進位"},
            {"IF 條件", "AND (且)", "OR (或)", "NOT (非)", "EQUALS (等於)", "GREATER (大於)", "LESS (小於)"}
    };

    private static final String[][] FUNC_PARAMS = {
            {"2", "2", "2", "2", "1", "1", "1", "1", "2", "2", "0"},
            {"1", "1", "1", "1", "2", "3"},
            {"1", "1", "1", "1", "1"},
            {"1", "1", "1", "1", "1", "1"},
            {"3", "2", "1", "1", "2", "2", "2"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_variables);

        spinnerCategory = findViewById(R.id.spinnerVarCategory);
        spinnerFunction = findViewById(R.id.spinnerVarFunction);
        editInput1 = findViewById(R.id.editVarInput1);
        editInput2 = findViewById(R.id.editVarInput2);
        editInput3 = findViewById(R.id.editVarInput3);
        txtResult = findViewById(R.id.txtVarResult);
        txtPreview = findViewById(R.id.txtVarPreview);
        btnEvaluate = findViewById(R.id.btnVarEvaluate);

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) { updateFunctions(pos); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        spinnerFunction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, android.view.View v, int pos, long id) { updateHints(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        btnEvaluate.setOnClickListener(v -> evaluate());

        updateFunctions(0);
        loadVariablePreview();
    }

    private void updateFunctions(int category) {
        ArrayAdapter<String> funcAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, FUNCTIONS[category]);
        funcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFunction.setAdapter(funcAdapter);
        updateHints();
    }

    private void updateHints() {
        int cat = spinnerCategory.getSelectedItemPosition();
        int func = spinnerFunction.getSelectedItemPosition();
        String paramCount = FUNC_PARAMS[cat][func];
        editInput1.setVisibility(View.VISIBLE);
        editInput2.setVisibility(View.VISIBLE);
        editInput3.setVisibility(View.VISIBLE);

        switch (cat) {
            case 0: // Math
                editInput1.setHint("數值 1"); editInput2.setHint("數值 2"); editInput3.setHint("");
                editInput3.setVisibility(View.GONE);
                if ("0".equals(paramCount)) { editInput1.setVisibility(View.GONE); editInput2.setVisibility(View.GONE); }
                else if ("1".equals(paramCount)) editInput2.setVisibility(View.GONE);
                break;
            case 1: // String
                editInput1.setHint("字串"); editInput2.setHint("起始位置/舊字串"); editInput3.setHint("長度/新字串");
                if ("1".equals(paramCount)) { editInput2.setVisibility(View.GONE); editInput3.setVisibility(View.GONE); }
                else if ("2".equals(paramCount)) editInput3.setVisibility(View.GONE);
                break;
            case 2: // Crypto
                editInput1.setHint("輸入文字"); editInput2.setVisibility(View.GONE); editInput3.setVisibility(View.GONE);
                break;
            case 3: // Convert
                editInput1.setHint("輸入值"); editInput2.setVisibility(View.GONE); editInput3.setVisibility(View.GONE);
                break;
            case 4: // Logic
                editInput1.setHint("條件/值 A"); editInput2.setHint("值 B (可選)"); editInput3.setHint("true時的輸出");
                if ("1".equals(paramCount)) { editInput2.setVisibility(View.GONE); editInput3.setVisibility(View.GONE); }
                else if ("2".equals(paramCount)) editInput3.setVisibility(View.GONE);
                break;
        }
    }

    private void evaluate() {
        int cat = spinnerCategory.getSelectedItemPosition();
        int func = spinnerFunction.getSelectedItemPosition();
        String in1 = editInput1.getText().toString().trim();
        String in2 = editInput2.getText().toString().trim();
        String in3 = editInput3.getText().toString().trim();

        try {
            String result = compute(cat, func, in1, in2, in3);
            txtResult.setText("結果: " + result);
            txtPreview.setText(String.format("函數: %s\n輸入: %s, %s, %s\n輸出: %s",
                    FUNCTIONS[cat][func], in1, in2, in3, result));
        } catch (Exception e) {
            txtResult.setText("錯誤: " + e.getMessage());
        }
    }

    private String compute(int cat, int func, String a, String b, String c) throws Exception {
        switch (cat) {
            case 0: return mathFunc(func, a, b);
            case 1: return stringFunc(func, a, b, c);
            case 2: return cryptoFunc(func, a);
            case 3: return convertFunc(func, a);
            case 4: return logicFunc(func, a, b, c);
            default: return "未知分類";
        }
    }

    private String mathFunc(int func, String a, String b) throws Exception {
        double x = a.isEmpty() ? 0 : Double.parseDouble(a);
        double y = b.isEmpty() ? 0 : Double.parseDouble(b);
        switch (func) {
            case 0: return String.valueOf(x + y);
            case 1: return String.valueOf(x - y);
            case 2: return String.valueOf(x * y);
            case 3: return y == 0 ? "錯誤: 除數為零" : String.valueOf(x / y);
            case 4: return String.valueOf(Math.abs(x));
            case 5: return String.valueOf(Math.round(x));
            case 6: return String.valueOf(Math.floor(x));
            case 7: return String.valueOf(Math.ceil(x));
            case 8: return String.valueOf(Math.min(x, y));
            case 9: return String.valueOf(Math.max(x, y));
            case 10: return String.valueOf(Math.random());
            default: return "未知";
        }
    }

    private String stringFunc(int func, String a, String b, String c) throws Exception {
        if (a == null) a = "";
        switch (func) {
            case 0: return a.toLowerCase();
            case 1: return a.toUpperCase();
            case 2: return a.trim();
            case 3: return String.valueOf(a.length());
            case 4:
                int start = Integer.parseInt(b);
                int len = c.isEmpty() ? a.length() - start : Integer.parseInt(c);
                return a.substring(start, Math.min(a.length(), start + len));
            case 5:
                return a.replace(b, c);
            default: return "未知";
        }
    }

    private String cryptoFunc(int func, String a) throws Exception {
        if (a.isEmpty()) return "請輸入文字";
        byte[] input = a.getBytes(StandardCharsets.UTF_8);
        switch (func) {
            case 0: return md5(input);
            case 1: return sha1(input);
            case 2: return sha256(input);
            case 3:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    return Base64.getEncoder().encodeToString(input);
                return android.util.Base64.encodeToString(input, android.util.Base64.DEFAULT).trim();
            case 4:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    return new String(Base64.getDecoder().decode(a), StandardCharsets.UTF_8);
                return new String(android.util.Base64.decode(a, android.util.Base64.DEFAULT), StandardCharsets.UTF_8);
            default: return "未知";
        }
    }

    private String convertFunc(int func, String a) throws Exception {
        if (a.isEmpty()) return "請輸入值";
        switch (func) {
            case 0: return String.valueOf(Long.parseLong(a.replaceAll("[^0-9A-Fa-f]", ""), 16));
            case 1: return Long.toHexString(Long.parseLong(a.replaceAll("[^0-9]", ""))).toUpperCase();
            case 2: {
                StringBuilder sb = new StringBuilder();
                for (byte b : a.getBytes(StandardCharsets.UTF_8)) sb.append(String.format("%02X", b));
                return sb.toString();
            }
            case 3: {
                byte[] bytes = Converter.hexToBytes(a);
                return new String(bytes, StandardCharsets.UTF_8);
            }
            case 4: {
                String clean = a.replaceAll("[^01]", "");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < clean.length(); i += 8) {
                    int end = Math.min(i + 8, clean.length());
                    sb.append(String.format("%02X", Integer.parseInt(clean.substring(i, end), 2)));
                }
                return sb.toString();
            }
            case 5: {
                String clean = a.replaceAll("[^0-9A-Fa-f]", "");
                StringBuilder sb = new StringBuilder();
                for (char ch : clean.toCharArray()) {
                    int val = Character.digit(ch, 16);
                    for (int i = 3; i >= 0; i--) sb.append((val >> i) & 1);
                }
                return sb.toString();
            }
            default: return "未知";
        }
    }

    private String logicFunc(int func, String a, String b, String c) throws Exception {
        switch (func) {
            case 0: // IF
                boolean cond = !a.isEmpty() && !"false".equalsIgnoreCase(a) && !"0".equals(a);
                return cond ? (!b.isEmpty() ? b : "true") : (!c.isEmpty() ? c : "false");
            case 1: return String.valueOf(!a.isEmpty() && !b.isEmpty());
            case 2: return String.valueOf(!a.isEmpty() || !b.isEmpty());
            case 3: return String.valueOf(a.isEmpty());
            case 4: return String.valueOf(a.equals(b));
            case 5:
                double gx = Double.parseDouble(a);
                double gy = Double.parseDouble(b);
                return String.valueOf(gx > gy);
            case 6:
                double lx = Double.parseDouble(a);
                double ly = Double.parseDouble(b);
                return String.valueOf(lx < ly);
            default: return "未知";
        }
    }

    private String md5(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String sha1(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(input);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String sha256(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private void loadVariablePreview() {
        StringBuilder sb = new StringBuilder("=== 預設變數 ===\n");
        sb.append("%TIME% - 目前時間\n");
        sb.append("%DATE% - 目前日期\n");
        sb.append("%BATTERY% - 電量\n");
        sb.append("%WIFI_SSID% - WiFi 名稱\n");
        sb.append("%DEVICE_NAME% - 裝置名稱\n");
        sb.append("%RANDOM% - 隨機數\n");
        sb.append("%TIMESTAMP% - Unix 時間戳\n");
        sb.append("=== 使用範例 ===\n");
        sb.append("將 %TIME% 取代為目前時間\n");
        sb.append("於任務自動化中使用變數\n");
    }
}
