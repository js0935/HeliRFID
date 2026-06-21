/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ToolsActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] nfcFilters;
    private Tag pendingTag;

    EditText editAccessBytes, editValueBlock, editEncodeValue, editEncodeAddr, editBccUid, editHexAscii;
    TextView txtAccessResult, txtValueResult, txtBccResult, txtHexAsciiResult;
    Button btnDecodeAccess, btnDecodeValue, btnEncodeValue, btnCalcBcc, btnShowHexAscii;
    Button btnGoKeyMgmt, btnGoDiffTool, btnGoLogViewer, btnGoApduConsole, btnGoMagicCard, btnGoDumpEditor, btnGoUidGen, btnGoShare, btnGoDataConverter, btnGoScanLog, btnGoValueBlock, btnGoAdvancedKey, btnGoNfcV, btnGoHce, btnGoDesfire, btnGoFelica, btnGoSectorSelect, btnGoVerify, btnGoAcr122u, btnGoTraceViewer, btnGoKeyRecovery;
    Button btnGoAccessDecoder, btnGoDumpAscii, btnGoDumpHighlight, btnGoValueCodec, btnGoNtag, btnGoTagFormat, btnGoAuthMap, btnGoNfcPoller;
    Button btnGoEmvCard, btnGoWriteBlock0, btnGoNfcMonitor, btnGoFingerprint, btnGoNdefEditor, btnGoNfcRelay, btnGoNtagPwd, btnGoTaskAuto, btnGoNtag424, btnGoVasReader, btnGoReaderMode, btnGoBulkWriter, btnGoQrScanner, btnGoTagIdent, btnGoTagBench, btnGoTagHist;
    Button btnGoTagClone, btnGoNdefValidator, btnGoTapToLaunch, btnGoTagBackup, btnGoNtagConfig, btnGoSmartProfile, btnGoSignalStrength, btnGoMemoryMap, btnGoEpassport, btnGoPcCompanion, btnGoTagArchiver;
    Button btnGoDumpExport, btnGoNtagSignature, btnGoTamperCheck, btnGoConditionalTask, btnGoNfcShortcut, btnGoQrCodeDisplay, btnGoFileOperations, btnGoTaskVariables;
    Button btnGoTagLock, btnGoFormatTag, btnGoHabitTracker, btnGoNfcBackground, btnGoHceNdef;
    Button btnGoTts, btnGoFlashlight, btnGoMediaControl, btnGoNfcSafe, btnGoTagInventory;
    Button btnGoTagCycles, btnGoNfcWebhook, btnGoNfcAlarm, btnGoNfcTimeTracker;
    Button btnGoAccessibleActions, btnGoNfcAppBlocker, btnGoNfc2Qr;
    Button btnGoPresetProfile, btnGoReportExport, btnGoIcodeSlix, btnGoOriginalityCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tools);

        editAccessBytes = findViewById(R.id.editAccessBytes);
        editValueBlock = findViewById(R.id.editValueBlock);
        editEncodeValue = findViewById(R.id.editEncodeValue);
        editEncodeAddr = findViewById(R.id.editEncodeAddr);
        editBccUid = findViewById(R.id.editBccUid);
        editHexAscii = findViewById(R.id.editHexAscii);

        txtAccessResult = findViewById(R.id.txtAccessResult);
        txtValueResult = findViewById(R.id.txtValueResult);
        txtBccResult = findViewById(R.id.txtBccResult);
        txtHexAsciiResult = findViewById(R.id.txtHexAsciiResult);

        btnDecodeAccess = findViewById(R.id.btnDecodeAccess);
        btnDecodeValue = findViewById(R.id.btnDecodeValue);
        btnEncodeValue = findViewById(R.id.btnEncodeValue);
        btnCalcBcc = findViewById(R.id.btnCalcBcc);
        btnShowHexAscii = findViewById(R.id.btnShowHexAscii);
        btnGoKeyMgmt = findViewById(R.id.btnGoKeyManagement);
        btnGoDiffTool = findViewById(R.id.btnGoDiffTool);
        btnGoLogViewer = findViewById(R.id.btnGoLogViewer);
        btnGoApduConsole = findViewById(R.id.btnGoApduConsole);
        btnGoMagicCard = findViewById(R.id.btnGoMagicCard);
        btnGoDumpEditor = findViewById(R.id.btnGoDumpEditor);
        btnGoUidGen = findViewById(R.id.btnGoUidGenerator);
        btnGoShare = findViewById(R.id.btnGoShare);
        btnGoDataConverter = findViewById(R.id.btnGoDataConverter);
        btnGoScanLog = findViewById(R.id.btnGoScanLog);
        btnGoValueBlock = findViewById(R.id.btnGoValueBlock);
        btnGoAdvancedKey = findViewById(R.id.btnGoAdvancedKey);
        btnGoNfcV = findViewById(R.id.btnGoNfcV);
        btnGoHce = findViewById(R.id.btnGoHce);
        btnGoDesfire = findViewById(R.id.btnGoDesfire);
        btnGoFelica = findViewById(R.id.btnGoFelica);
        btnGoSectorSelect = findViewById(R.id.btnGoSectorSelect);
        btnGoVerify = findViewById(R.id.btnGoVerify);
        btnGoAcr122u = findViewById(R.id.btnGoAcr122u);
        btnGoTraceViewer = findViewById(R.id.btnGoTraceViewer);
        btnGoKeyRecovery = findViewById(R.id.btnGoKeyRecovery);
        btnGoAccessDecoder = findViewById(R.id.btnGoAccessDecoder);
        btnGoDumpAscii = findViewById(R.id.btnGoDumpAscii);
        btnGoDumpHighlight = findViewById(R.id.btnGoDumpHighlight);
        btnGoValueCodec = findViewById(R.id.btnGoValueCodec);
        btnGoNtag = findViewById(R.id.btnGoNtag);
        btnGoTagFormat = findViewById(R.id.btnGoTagFormat);
        btnGoAuthMap = findViewById(R.id.btnGoAuthMap);
        btnGoNfcPoller = findViewById(R.id.btnGoNfcPoller);
        btnGoEmvCard = findViewById(R.id.btnGoEmvCard);
        btnGoWriteBlock0 = findViewById(R.id.btnGoWriteBlock0);
        btnGoNfcMonitor = findViewById(R.id.btnGoNfcMonitor);
        btnGoFingerprint = findViewById(R.id.btnGoFingerprint);
        btnGoNdefEditor = findViewById(R.id.btnGoNdefEditor);
        btnGoNfcRelay = findViewById(R.id.btnGoNfcRelay);
        btnGoNtagPwd = findViewById(R.id.btnGoNtagPwd);
        btnGoTaskAuto = findViewById(R.id.btnGoTaskAuto);
        btnGoNtag424 = findViewById(R.id.btnGoNtag424);
        btnGoVasReader = findViewById(R.id.btnGoVasReader);
        btnGoReaderMode = findViewById(R.id.btnGoReaderMode);
        btnGoBulkWriter = findViewById(R.id.btnGoBulkWriter);
        btnGoQrScanner = findViewById(R.id.btnGoQrScanner);
        btnGoTagIdent = findViewById(R.id.btnGoTagIdent);
        btnGoTagBench = findViewById(R.id.btnGoTagBench);
        btnGoTagHist = findViewById(R.id.btnGoTagHist);
        btnGoTagClone = findViewById(R.id.btnGoTagClone);
        btnGoNdefValidator = findViewById(R.id.btnGoNdefValidator);
        btnGoTapToLaunch = findViewById(R.id.btnGoTapToLaunch);
        btnGoTagBackup = findViewById(R.id.btnGoTagBackup);
        btnGoNtagConfig = findViewById(R.id.btnGoNtagConfig);
        btnGoSmartProfile = findViewById(R.id.btnGoSmartProfile);
        btnGoSignalStrength = findViewById(R.id.btnGoSignalStrength);
        btnGoMemoryMap = findViewById(R.id.btnGoMemoryMap);
        btnGoEpassport = findViewById(R.id.btnGoEpassport);
        btnGoPcCompanion = findViewById(R.id.btnGoPcCompanion);
        btnGoTagArchiver = findViewById(R.id.btnGoTagArchiver);
        btnGoDumpExport = findViewById(R.id.btnGoDumpExport);
        btnGoNtagSignature = findViewById(R.id.btnGoNtagSignature);
        btnGoTamperCheck = findViewById(R.id.btnGoTamperCheck);
        btnGoConditionalTask = findViewById(R.id.btnGoConditionalTask);
        btnGoNfcShortcut = findViewById(R.id.btnGoNfcShortcut);
        btnGoQrCodeDisplay = findViewById(R.id.btnGoQrCodeDisplay);
        btnGoFileOperations = findViewById(R.id.btnGoFileOperations);
        btnGoTaskVariables = findViewById(R.id.btnGoTaskVariables);
        btnGoTagLock = findViewById(R.id.btnGoTagLock);
        btnGoFormatTag = findViewById(R.id.btnGoTagFormat);
        btnGoHabitTracker = findViewById(R.id.btnGoHabitTracker);
        btnGoNfcBackground = findViewById(R.id.btnGoNfcBackground);
        btnGoHceNdef = findViewById(R.id.btnGoHceNdefEmulation);
        btnGoTts = findViewById(R.id.btnGoTts);
        btnGoFlashlight = findViewById(R.id.btnGoFlashlight);
        btnGoMediaControl = findViewById(R.id.btnGoMediaControl);
        btnGoNfcSafe = findViewById(R.id.btnGoNfcSafe);
        btnGoTagInventory = findViewById(R.id.btnGoTagInventory);
        btnGoTagCycles = findViewById(R.id.btnGoTagCycles);
        btnGoNfcWebhook = findViewById(R.id.btnGoNfcWebhook);
        btnGoNfcAlarm = findViewById(R.id.btnGoNfcAlarm);
        btnGoNfcTimeTracker = findViewById(R.id.btnGoNfcTimeTracker);
        btnGoAccessibleActions = findViewById(R.id.btnGoAccessibleActions);
        btnGoNfcAppBlocker = findViewById(R.id.btnGoNfcAppBlocker);
        btnGoNfc2Qr = findViewById(R.id.btnGoNfc2Qr);
        btnGoPresetProfile = findViewById(R.id.btnGoPresetProfile);
        btnGoReportExport = findViewById(R.id.btnGoReportExport);
        btnGoIcodeSlix = findViewById(R.id.btnGoIcodeSlix);
        btnGoOriginalityCheck = findViewById(R.id.btnGoOriginalityCheck);

        btnDecodeAccess.setOnClickListener(v -> {
            String input = editAccessBytes.getText().toString().trim().replace(" ", "");
            if (input.length() < 12) {
                txtAccessResult.setText("請輸入至少 6 bytes (12 hex 字元)");
                return;
            }
            try {
                byte[] data = hexToBytes(input);
                txtAccessResult.setText(MifareUtils.decodeAccessConditions(data));
            } catch (Exception e) {
                txtAccessResult.setText("解碼失敗: " + e.getMessage());
            }
        });

        btnDecodeValue.setOnClickListener(v -> {
            String input = editValueBlock.getText().toString().trim().replace(" ", "");
            if (input.length() != 32) {
                txtValueResult.setText("請輸入 16 bytes (32 hex 字元)");
                return;
            }
            try {
                byte[] data = hexToBytes(input);
                txtValueResult.setText(MifareUtils.decodeValueBlock(data));
            } catch (Exception e) {
                txtValueResult.setText("解碼失敗: " + e.getMessage());
            }
        });

        btnEncodeValue.setOnClickListener(v -> {
            String valStr = editEncodeValue.getText().toString().trim();
            String addrStr = editEncodeAddr.getText().toString().trim();
            if (TextUtils.isEmpty(valStr) || TextUtils.isEmpty(addrStr)) {
                txtValueResult.setText("請輸入整數值和位址");
                return;
            }
            try {
                int value = Integer.parseInt(valStr);
                int addr = Integer.parseInt(addrStr) & 0xFF;
                byte[] data = MifareUtils.encodeValueBlock(value, (byte)addr);
                StringBuilder sb = new StringBuilder("編碼結果 (16 bytes):\n");
                for (byte b : data) sb.append(String.format("%02X ", b));
                txtValueResult.setText(sb.toString().trim());
            } catch (Exception e) {
                txtValueResult.setText("編碼失敗: " + e.getMessage());
            }
        });

        btnCalcBcc.setOnClickListener(v -> {
            String input = editBccUid.getText().toString().trim();
            if (TextUtils.isEmpty(input)) {
                txtBccResult.setText("請輸入 UID");
                return;
            }
            txtBccResult.setText(MifareUtils.calculateBcc(input));
        });

        btnShowHexAscii.setOnClickListener(v -> {
            String input = editHexAscii.getText().toString().trim().replace(" ", "");
            if (TextUtils.isEmpty(input)) {
                txtHexAsciiResult.setText("請輸入 hex 資料");
                return;
            }
            try {
                byte[] data = hexToBytes(input);
                txtHexAsciiResult.setText(MifareUtils.hexWithAscii(data));
            } catch (Exception e) {
                txtHexAsciiResult.setText("轉換失敗: " + e.getMessage());
            }
        });

        btnGoKeyMgmt.setOnClickListener(v -> startActivity(new Intent(this, KeyManagementActivity.class)));
        btnGoDiffTool.setOnClickListener(v -> startActivity(new Intent(this, DiffToolActivity.class)));
        btnGoLogViewer.setOnClickListener(v -> startActivity(new Intent(this, LogViewerActivity.class)));
        btnGoApduConsole.setOnClickListener(v -> startActivity(new Intent(this, ApduConsoleActivity.class)));
        btnGoMagicCard.setOnClickListener(v -> startActivity(new Intent(this, MagicCardActivity.class)));
        btnGoDumpEditor.setOnClickListener(v -> startActivity(new Intent(this, DumpEditorActivity.class)));
        btnGoUidGen.setOnClickListener(v -> startActivity(new Intent(this, UidGeneratorActivity.class)));
        btnGoShare.setOnClickListener(v -> startActivity(new Intent(this, ShareActivity.class)));
        btnGoDataConverter.setOnClickListener(v -> startActivity(new Intent(this, DataConverterActivity.class)));
        btnGoScanLog.setOnClickListener(v -> startActivity(new Intent(this, ScanLogActivity.class)));
        btnGoValueBlock.setOnClickListener(v -> startActivity(new Intent(this, ValueBlockActivity.class)));
        btnGoAdvancedKey.setOnClickListener(v -> startActivity(new Intent(this, AdvancedKeyManagerActivity.class)));
        btnGoNfcV.setOnClickListener(v -> startActivity(new Intent(this, NfcVActivity.class)));
        btnGoHce.setOnClickListener(v -> startActivity(new Intent(this, HceActivity.class)));
        btnGoDesfire.setOnClickListener(v -> startActivity(new Intent(this, DesfireActivity.class)));
        btnGoFelica.setOnClickListener(v -> startActivity(new Intent(this, FelicaActivity.class)));
        btnGoSectorSelect.setOnClickListener(v -> startActivity(new Intent(this, SectorSelectActivity.class)));
        btnGoVerify.setOnClickListener(v -> startActivity(new Intent(this, VerifyActivity.class)));
        btnGoAcr122u.setOnClickListener(v -> startActivity(new Intent(this, Acr122uActivity.class)));
        btnGoTraceViewer.setOnClickListener(v -> startActivity(new Intent(this, TraceViewerActivity.class)));
        btnGoKeyRecovery.setOnClickListener(v -> startActivity(new Intent(this, KeyRecoveryActivity.class)));
        btnGoAccessDecoder.setOnClickListener(v -> startActivity(new Intent(this, AccessDecoderActivity.class)));
        btnGoDumpAscii.setOnClickListener(v -> startActivity(new Intent(this, DumpAsciiActivity.class)));
        btnGoDumpHighlight.setOnClickListener(v -> startActivity(new Intent(this, DumpHighlightActivity.class)));
        btnGoValueCodec.setOnClickListener(v -> startActivity(new Intent(this, ValueCodecActivity.class)));
        btnGoNtag.setOnClickListener(v -> startActivity(new Intent(this, NtagActivity.class)));
        btnGoTagFormat.setOnClickListener(v -> startActivity(new Intent(this, TagFormatActivity.class)));
        btnGoAuthMap.setOnClickListener(v -> startActivity(new Intent(this, AuthMapActivity.class)));
        btnGoNfcPoller.setOnClickListener(v -> startActivity(new Intent(this, NfcPollerActivity.class)));
        btnGoEmvCard.setOnClickListener(v -> startActivity(new Intent(this, EmvCardActivity.class)));
        btnGoWriteBlock0.setOnClickListener(v -> startActivity(new Intent(this, WriteBlock0Activity.class)));
        btnGoNfcMonitor.setOnClickListener(v -> startActivity(new Intent(this, NfcMonitorActivity.class)));
        btnGoFingerprint.setOnClickListener(v -> startActivity(new Intent(this, CardFingerprintActivity.class)));
        btnGoNdefEditor.setOnClickListener(v -> startActivity(new Intent(this, NdefEditorActivity.class)));
        btnGoNfcRelay.setOnClickListener(v -> startActivity(new Intent(this, NfcRelayActivity.class)));
        btnGoNtagPwd.setOnClickListener(v -> startActivity(new Intent(this, NtagPasswordActivity.class)));
        btnGoTaskAuto.setOnClickListener(v -> startActivity(new Intent(this, TaskAutomationActivity.class)));
        btnGoNtag424.setOnClickListener(v -> startActivity(new Intent(this, Ntag424Activity.class)));
        btnGoVasReader.setOnClickListener(v -> startActivity(new Intent(this, VasReaderActivity.class)));
        btnGoReaderMode.setOnClickListener(v -> startActivity(new Intent(this, ReaderModeActivity.class)));
        btnGoBulkWriter.setOnClickListener(v -> startActivity(new Intent(this, BulkWriterActivity.class)));
        btnGoQrScanner.setOnClickListener(v -> startActivity(new Intent(this, QrScannerActivity.class)));
        btnGoTagIdent.setOnClickListener(v -> startActivity(new Intent(this, TagIdentifierActivity.class)));
        btnGoTagBench.setOnClickListener(v -> startActivity(new Intent(this, TagBenchmarkActivity.class)));
        btnGoTagHist.setOnClickListener(v -> startActivity(new Intent(this, TagHistoryActivity.class)));
        btnGoTagClone.setOnClickListener(v -> startActivity(new Intent(this, TagCloneActivity.class)));
        btnGoNdefValidator.setOnClickListener(v -> startActivity(new Intent(this, NdefValidatorActivity.class)));
        btnGoTapToLaunch.setOnClickListener(v -> startActivity(new Intent(this, TapToLaunchActivity.class)));
        btnGoTagBackup.setOnClickListener(v -> startActivity(new Intent(this, TagBackupActivity.class)));
        btnGoNtagConfig.setOnClickListener(v -> startActivity(new Intent(this, NtagConfigActivity.class)));
        btnGoSmartProfile.setOnClickListener(v -> startActivity(new Intent(this, SmartProfileActivity.class)));
        btnGoSignalStrength.setOnClickListener(v -> startActivity(new Intent(this, SignalStrengthActivity.class)));
        btnGoMemoryMap.setOnClickListener(v -> startActivity(new Intent(this, MemoryMapActivity.class)));
        btnGoEpassport.setOnClickListener(v -> startActivity(new Intent(this, EPassportActivity.class)));
        btnGoPcCompanion.setOnClickListener(v -> startActivity(new Intent(this, PcCompanionActivity.class)));
        btnGoTagArchiver.setOnClickListener(v -> startActivity(new Intent(this, TagArchiverActivity.class)));
        btnGoDumpExport.setOnClickListener(v -> startActivity(new Intent(this, DumpExportActivity.class)));
        btnGoNtagSignature.setOnClickListener(v -> startActivity(new Intent(this, NtagSignatureActivity.class)));
        btnGoTamperCheck.setOnClickListener(v -> startActivity(new Intent(this, TamperCheckActivity.class)));
        btnGoConditionalTask.setOnClickListener(v -> startActivity(new Intent(this, ConditionalTaskActivity.class)));
        btnGoNfcShortcut.setOnClickListener(v -> startActivity(new Intent(this, NfcShortcutActivity.class)));
        btnGoQrCodeDisplay.setOnClickListener(v -> startActivity(new Intent(this, QrCodeDisplayActivity.class)));
        btnGoFileOperations.setOnClickListener(v -> startActivity(new Intent(this, FileOperationsTaskActivity.class)));
        btnGoTaskVariables.setOnClickListener(v -> startActivity(new Intent(this, TaskVariablesActivity.class)));
        btnGoTagLock.setOnClickListener(v -> startActivity(new Intent(this, TagLockActivity.class)));
        btnGoFormatTag.setOnClickListener(v -> startActivity(new Intent(this, FormatTagActivity.class)));
        btnGoHabitTracker.setOnClickListener(v -> startActivity(new Intent(this, HabitTrackerActivity.class)));
        btnGoNfcBackground.setOnClickListener(v -> startActivity(new Intent(this, NfcBackgroundMonitorActivity.class)));
        btnGoHceNdef.setOnClickListener(v -> startActivity(new Intent(this, HceNdefEmulationActivity.class)));
        btnGoTts.setOnClickListener(v -> startActivity(new Intent(this, TtsActivity.class)));
        btnGoFlashlight.setOnClickListener(v -> startActivity(new Intent(this, FlashlightActivity.class)));
        btnGoMediaControl.setOnClickListener(v -> startActivity(new Intent(this, MediaControlActivity.class)));
        btnGoNfcSafe.setOnClickListener(v -> startActivity(new Intent(this, NfcSafeActivity.class)));
        btnGoTagInventory.setOnClickListener(v -> startActivity(new Intent(this, TagInventoryActivity.class)));
        btnGoTagCycles.setOnClickListener(v -> startActivity(new Intent(this, TagCyclesActivity.class)));
        btnGoNfcWebhook.setOnClickListener(v -> startActivity(new Intent(this, NfcWebhookActivity.class)));
        btnGoNfcAlarm.setOnClickListener(v -> startActivity(new Intent(this, NfcAlarmActivity.class)));
        btnGoNfcTimeTracker.setOnClickListener(v -> startActivity(new Intent(this, NfcTimeTrackerActivity.class)));
        btnGoAccessibleActions.setOnClickListener(v -> startActivity(new Intent(this, AccessibleActionsActivity.class)));
        btnGoNfcAppBlocker.setOnClickListener(v -> startActivity(new Intent(this, NfcAppBlockerActivity.class)));
        btnGoNfc2Qr.setOnClickListener(v -> startActivity(new Intent(this, Nfc2QrActivity.class)));
        btnGoPresetProfile.setOnClickListener(v -> startActivity(new Intent(this, PresetProfileActivity.class)));
        btnGoReportExport.setOnClickListener(v -> startActivity(new Intent(this, ReportExportActivity.class)));
        btnGoIcodeSlix.setOnClickListener(v -> startActivity(new Intent(this, IcodeSlixActivity.class)));
        btnGoOriginalityCheck.setOnClickListener(v -> startActivity(new Intent(this, OriginalityCheckActivity.class)));

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
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcFilters, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                pendingTag = tag;
                showNfcToolDialog();
            }
        }
    }

    private void showNfcToolDialog() {
        String[] items = {"標籤資訊 (TagInfoActivity)", "記憶體 Dump", "NTAG 進階工具",
                "EMV 信用卡讀取", "卡片指紋辨識", "NFC 流量監聽", "廠商區塊寫入",
                "金鑰測試", "寫入 (WriteActivity)", "格式化 (TagFormat)"};
        String[] activities = {"TagInfoActivity", "MemoryDumpActivity", "NtagActivity",
                "EmvCardActivity", "CardFingerprintActivity", "NfcMonitorActivity", "WriteBlock0Activity",
                "KeyManagementActivity", "WriteActivity", "TagFormatActivity"};

        new AlertDialog.Builder(this)
                .setTitle("偵測到 NFC 卡片 — 選擇工具")
                .setItems(items, (dialog, which) -> {
                    try {
                        Class<?> cls = Class.forName("com.helirfid." + activities[which]);
                        Intent i = new Intent(this, cls);
                        i.putExtra(NfcAdapter.EXTRA_TAG, pendingTag);
                        startActivity(i);
                    } catch (ClassNotFoundException e) {
                        Toast.makeText(this, "啟動失敗", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
