package com.helirfid;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BaseNfcActivity extends AppCompatActivity {
    protected NfcAdapter nfcAdapter;
    protected PendingIntent pendingIntent;
    protected IntentFilter[] nfcFilters;
    protected boolean useReaderMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags |= PendingIntent.FLAG_MUTABLE;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
        nfcFilters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (useReaderMode) enableReaderMode();
            else nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            if (useReaderMode) nfcAdapter.disableReaderMode(this);
            else nfcAdapter.disableForegroundDispatch(this);
        }
    }

    protected void enableReaderMode() {
        if (nfcAdapter == null) return;
        int flags = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B
                | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V
                | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        nfcAdapter.enableReaderMode(this, tag -> {
            Intent intent = new Intent(NfcAdapter.ACTION_TAG_DISCOVERED);
            intent.putExtra(NfcAdapter.EXTRA_TAG, tag);
            onNewIntent(intent);
        }, flags, null);
    }

    protected void setUseReaderMode(boolean enabled) {
        useReaderMode = enabled;
    }

    protected void enableNfcDispatch() {
        if (nfcAdapter == null) return;
        if (useReaderMode) enableReaderMode();
        else nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
    }

    protected void disableNfcDispatch() {
        if (nfcAdapter == null) return;
        if (useReaderMode) nfcAdapter.disableReaderMode(this);
        else nfcAdapter.disableForegroundDispatch(this);
    }

    protected void vibrate() {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(100);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null)
                Toast.makeText(this, "偵測到 NFC 卡片 (UID: " + Converter.hex(tag.getId()) + ")", Toast.LENGTH_SHORT).show();
        }
    }
}
