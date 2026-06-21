package com.helirfid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QrCodeDisplayActivity extends AppCompatActivity {

    private EditText editInput;
    private ImageView imgQr;
    private TextView txtFallback;
    private Button btnGenerate, btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code_display);

        editInput = findViewById(R.id.editQrInput);
        imgQr = findViewById(R.id.imgQrCode);
        txtFallback = findViewById(R.id.txtQrFallback);
        btnGenerate = findViewById(R.id.btnQrGenerate);
        btnShare = findViewById(R.id.btnQrShare);

        String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            editInput.setText(sharedText);
            generateQr(sharedText);
        }

        btnGenerate.setOnClickListener(v -> {
            String text = editInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "請輸入文字或網址", Toast.LENGTH_SHORT).show();
                return;
            }
            generateQr(text);
        });

        btnShare.setOnClickListener(v -> {
            String text = editInput.getText().toString().trim();
            if (text.isEmpty()) return;
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(share, "分享 QR Code 內容"));
        });
    }

    private void generateQr(String text) {
        try {
            Class<?> writerClass = Class.forName("com.google.zxing.qrcode.QRCodeWriter");
            Object writer = writerClass.getDeclaredConstructor().newInstance();
            java.lang.reflect.Method encodeMethod = writerClass.getMethod("encode",
                    String.class, Class.forName("com.google.zxing.BarcodeFormat"), int.class, int.class);

            Class<?> hintsClass = Class.forName("com.google.zxing.EncodeHintType");
            Object errorCorrection = hintsClass.getField("ERROR_CORRECTION").get(null);
            Class<?> errorLevelClass = Class.forName("com.google.zxing.qrcode.decoder.ErrorCorrectionLevel");
            Object errorLevel = errorLevelClass.getField("L").get(null);

            java.util.Map<Object, Object> hints = new java.util.HashMap<>();
            hints.put(errorCorrection, errorLevel);

            Object bitMatrix = encodeMethod.invoke(writer, text,
                    Class.forName("com.google.zxing.BarcodeFormat").getField("QR_CODE").get(null),
                    512, 512, hints);

            java.lang.reflect.Method widthMethod = bitMatrix.getClass().getMethod("getWidth");
            java.lang.reflect.Method heightMethod = bitMatrix.getClass().getMethod("getHeight");
            java.lang.reflect.Method getMethod = bitMatrix.getClass().getMethod("get", int.class, int.class);

            int width = (int) widthMethod.invoke(bitMatrix);
            int height = (int) heightMethod.invoke(bitMatrix);

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    boolean bit = (boolean) getMethod.invoke(bitMatrix, x, y);
                    bmp.setPixel(x, y, bit ? Color.BLACK : Color.WHITE);
                }
            }

            imgQr.setImageBitmap(bmp);
            imgQr.setVisibility(View.VISIBLE);
            txtFallback.setVisibility(View.GONE);
        } catch (Exception e) {
            imgQr.setVisibility(View.GONE);
            txtFallback.setVisibility(View.VISIBLE);
            txtFallback.setText(text);
        }
    }
}
