package com.helirfid;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TraceViewerActivity extends AppCompatActivity {

    private static final int REQUEST_LOAD_TRACE = 400;

    Button btnLoad;
    TextView txtInfo, txtContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_viewer);

        btnLoad = findViewById(R.id.btnLoadTrace);
        txtInfo = findViewById(R.id.txtTraceInfo);
        txtContent = findViewById(R.id.txtTraceContent);

        btnLoad.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            startActivityForResult(intent, REQUEST_LOAD_TRACE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOAD_TRACE && resultCode == RESULT_OK && data != null) {
            loadTraceFile(data.getData());
        }
    }

    private void loadTraceFile(Uri uri) {
        try {
            StringBuilder rawText = new StringBuilder();
            int lineCount = 0;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    getContentResolver().openInputStream(uri)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    rawText.append(line).append("\n");
                    lineCount++;
                }
            }

            SpannableStringBuilder ssb = new SpannableStringBuilder();
            parseTraceToSpannable(rawText.toString(), ssb);

            txtContent.setText(ssb);

            String fileName = uri.getLastPathSegment();
            txtInfo.setText("已載入: " + fileName + " (" + lineCount + " 行)");

        } catch (Exception e) {
            Toast.makeText(this, "載入失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
            txtContent.setText("載入失敗");
        }
    }

    private void parseTraceToSpannable(String text, SpannableStringBuilder ssb) {
        Pattern linePattern = Pattern.compile("^\\s*(#.*|[+].*)$", Pattern.MULTILINE);
        Matcher m = linePattern.matcher(text);

        int lastEnd = 0;
        while (m.find()) {
            int start = m.start();
            if (start > lastEnd) {
                String raw = text.substring(lastEnd, start);
                int rawStart = ssb.length();
                ssb.append(raw);
                ssb.setSpan(new ForegroundColorSpan(Color.LTGRAY), rawStart, ssb.length(), 0);
            }

            String line = m.group(0);
            int lineStart = ssb.length();
            ssb.append(line).append("\n");

            if (line.startsWith("#")) {
                ssb.setSpan(new ForegroundColorSpan(Color.GRAY), lineStart, ssb.length(), 0);
            } else if (line.startsWith("+")) {
                boolean isTag = line.contains("TAG");
                boolean hasCrc = line.contains("!crc") || line.contains("!crc");

                String[] parts = line.split(":", 3);
                if (parts.length >= 2) {
                    int tsStart = ssb.length() - line.length() - 1;
                    int colLen = parts[0].length() + 1;
                    ssb.setSpan(new ForegroundColorSpan(Color.DKGRAY), lineStart, lineStart + colLen, 0);

                    if (isTag) {
                        ssb.setSpan(new ForegroundColorSpan(Color.rgb(0, 180, 0)), lineStart + colLen, ssb.length(), 0);
                    } else {
                        ssb.setSpan(new ForegroundColorSpan(Color.rgb(0, 120, 210)), lineStart + colLen, ssb.length(), 0);
                    }
                }

                if (hasCrc) {
                    int crcIdx = line.lastIndexOf("!crc");
                    if (crcIdx >= 0) {
                        int absCrc = lineStart + crcIdx;
                        ssb.setSpan(new ForegroundColorSpan(Color.RED), absCrc, ssb.length() - 1, 0);
                    }
                }
            } else {
                ssb.setSpan(new ForegroundColorSpan(Color.LTGRAY), lineStart, ssb.length(), 0);
            }

            lastEnd = m.end();
        }

        if (lastEnd < text.length()) {
            int remainStart = ssb.length();
            ssb.append(text.substring(lastEnd));
            ssb.setSpan(new ForegroundColorSpan(Color.LTGRAY), remainStart, ssb.length(), 0);
        }
    }
}
