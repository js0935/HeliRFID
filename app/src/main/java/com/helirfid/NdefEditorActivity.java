package com.helirfid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.ContactsContract;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.content.Context.WIFI_SERVICE;

public class NdefEditorActivity extends BaseNfcActivity {

    private static final int REQ_PICK_CONTACT_PHONE = 1001;
    private static final int REQ_PICK_CONTACT_EMAIL = 1002;
    private static final int REQ_PICK_CONTACT_SMS = 1003;
    private static final int REQ_PICK_CONTACT_VCARD = 1004;
    private static final int REQ_PICK_CONTACT_ICE = 1005;

    private static final String[] RECORD_TYPES = {
            "純文字 (Text)", "網址 (URL)", "電子郵件 (Email)", "電話 (Phone)",
            "簡訊 (SMS)", "WiFi 設定", "聯絡人 (vCard)", "地理位置 (Geo)", "藍牙 (Bluetooth)",
            "Smart Poster (含標題/URL/動作)", "AAR (Android App 記錄)",
            "ICE 急難聯絡人", "自訂資料 (Custom Data)", "WiFi WSC (Simple Config)",
            "比特幣/加密貨幣 (Bitcoin)", "社群網路 (Facebook/Twitter/IG)",
            "LINE/WhatsApp/Telegram"
    };

    private Spinner spinnerType;
    private EditText editText, editUrl, editEmailTo, editEmailSubj, editEmailBody;
    private EditText editPhone, editSmsNum, editSmsMsg, editSsid, editWifiPwd;
    private EditText editVCardName, editVCardPhone, editVCardEmail;
    private EditText editLat, editLng, editBtMac, editBtName;
    private EditText editSpTitle, editSpUrl, editSpAction, editAarPkg;
    private EditText editIceName, editIcePhone, editIceRelation;
    private EditText editCustomType, editCustomData;
    private Spinner spinnerNdefCustomTnf;
    private EditText editWscSsid, editWscPwd;
    private Spinner spinnerWscAuth, spinnerWscEncrypt;

    private EditText editNdefBtcAddress, editNdefBtcAmount, editNdefBtcLabel;
    private Spinner spinnerNdefSocialPlatform;
    private EditText editNdefSocialHandle;
    private Spinner spinnerNdefMessengerType;
    private EditText editNdefMessengerId;
    private Button btnContactPickPhone, btnContactPickEmail, btnContactPickSms, btnContactPickVCard, btnContactPickIce;
    private TextView txtNdefInfo, txtNdefResult;
    private Button btnWrite, btnRead, btnClear;

    private Tag currentTag;
    private NdefMessage pendingMessage;

    private final List<View> allFields = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ndef_editor);

        spinnerType = findViewById(R.id.spinnerNdefType);
        txtNdefInfo = findViewById(R.id.txtNdefInfo);
        txtNdefResult = findViewById(R.id.txtNdefResult);
        btnWrite = findViewById(R.id.btnNdefWrite);
        btnRead = findViewById(R.id.btnNdefRead);
        btnClear = findViewById(R.id.btnNdefClear);

        editText = findViewById(R.id.editNdefText);
        editUrl = findViewById(R.id.editNdefUrl);
        editEmailTo = findViewById(R.id.editNdefEmailTo);
        editEmailSubj = findViewById(R.id.editNdefEmailSubj);
        editEmailBody = findViewById(R.id.editNdefEmailBody);
        editPhone = findViewById(R.id.editNdefPhone);
        editSmsNum = findViewById(R.id.editNdefSmsNum);
        editSmsMsg = findViewById(R.id.editNdefSmsMsg);
        editSsid = findViewById(R.id.editNdefSsid);
        editWifiPwd = findViewById(R.id.editNdefWifiPwd);
        editVCardName = findViewById(R.id.editNdefVCardName);
        editVCardPhone = findViewById(R.id.editNdefVCardPhone);
        editVCardEmail = findViewById(R.id.editNdefVCardEmail);
        editLat = findViewById(R.id.editNdefLat);
        editLng = findViewById(R.id.editNdefLng);
        editBtMac = findViewById(R.id.editNdefBtMac);
        editBtName = findViewById(R.id.editNdefBtName);
        editBtMac.setHint("00:11:22:33:44:55");

        editSpTitle = findViewById(R.id.editNdefSpTitle);
        editSpUrl = findViewById(R.id.editNdefSpUrl);
        editSpAction = findViewById(R.id.editNdefSpAction);
        editAarPkg = findViewById(R.id.editNdefAarPkg);

        editIceName = findViewById(R.id.editNdefIceName);
        editIcePhone = findViewById(R.id.editNdefIcePhone);
        editIceRelation = findViewById(R.id.editNdefIceRelation);
        editCustomType = findViewById(R.id.editNdefCustomType);
        editCustomData = findViewById(R.id.editNdefCustomData);
        spinnerNdefCustomTnf = findViewById(R.id.spinnerNdefCustomTnf);
        editWscSsid = findViewById(R.id.editNdefWscSsid);
        editWscPwd = findViewById(R.id.editNdefWscPwd);
        spinnerWscAuth = findViewById(R.id.spinnerNdefWscAuth);
        spinnerWscEncrypt = findViewById(R.id.spinnerNdefWscEncrypt);

        editNdefBtcAddress = findViewById(R.id.editNdefBtcAddress);
        editNdefBtcAmount = findViewById(R.id.editNdefBtcAmount);
        editNdefBtcLabel = findViewById(R.id.editNdefBtcLabel);
        spinnerNdefSocialPlatform = findViewById(R.id.spinnerNdefSocialPlatform);
        editNdefSocialHandle = findViewById(R.id.editNdefSocialHandle);
        spinnerNdefMessengerType = findViewById(R.id.spinnerNdefMessengerType);
        editNdefMessengerId = findViewById(R.id.editNdefMessengerId);

        btnContactPickPhone = findViewById(R.id.btnContactPickPhone);
        btnContactPickEmail = findViewById(R.id.btnContactPickEmail);
        btnContactPickSms = findViewById(R.id.btnContactPickSms);
        btnContactPickVCard = findViewById(R.id.btnContactPickVCard);
        btnContactPickIce = findViewById(R.id.btnContactPickIce);

        allFields.add(findViewById(R.id.layoutNdefText));
        allFields.add(findViewById(R.id.layoutNdefUrl));
        allFields.add(findViewById(R.id.layoutNdefEmail));
        allFields.add(findViewById(R.id.layoutNdefPhone));
        allFields.add(findViewById(R.id.layoutNdefSms));
        allFields.add(findViewById(R.id.layoutNdefWifi));
        allFields.add(findViewById(R.id.layoutNdefVCard));
        allFields.add(findViewById(R.id.layoutNdefGeo));
        allFields.add(findViewById(R.id.layoutNdefBt));
        allFields.add(findViewById(R.id.layoutNdefSmartPoster));
        allFields.add(findViewById(R.id.layoutNdefAar));
        allFields.add(findViewById(R.id.layoutNdefIce));
        allFields.add(findViewById(R.id.layoutNdefCustom));
        allFields.add(findViewById(R.id.layoutNdefWsc));
        allFields.add(findViewById(R.id.layoutNdefBitcoin));
        allFields.add(findViewById(R.id.layoutNdefSocial));
        allFields.add(findViewById(R.id.layoutNdefMessenger));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, RECORD_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { showFields(pos); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        btnWrite.setOnClickListener(v -> doWrite());
        btnRead.setOnClickListener(v -> doRead());
        btnClear.setOnClickListener(v -> doClear());

        btnContactPickPhone.setOnClickListener(v -> pickContact(REQ_PICK_CONTACT_PHONE));
        btnContactPickEmail.setOnClickListener(v -> pickContact(REQ_PICK_CONTACT_EMAIL));
        btnContactPickSms.setOnClickListener(v -> pickContact(REQ_PICK_CONTACT_SMS));
        btnContactPickVCard.setOnClickListener(v -> pickContact(REQ_PICK_CONTACT_VCARD));
        btnContactPickIce.setOnClickListener(v -> pickContact(REQ_PICK_CONTACT_ICE));

        ArrayAdapter<String> tnfAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"TNF_EMPTY (0)", "TNF_WELL_KNOWN (1)", "TNF_MIME_MEDIA (2)",
                        "TNF_ABSOLUTE_URI (3)", "TNF_EXTERNAL_TYPE (4)", "TNF_UNKNOWN (5)", "TNF_UNCHANGED (6)"});
        tnfAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNdefCustomTnf.setAdapter(tnfAdapter);

        ArrayAdapter<String> wscAuthAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Open", "WPA", "WPA2", "WPA2PSK"});
        wscAuthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWscAuth.setAdapter(wscAuthAdapter);

        ArrayAdapter<String> wscEncAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"None", "TKIP", "AES", "AES/TKIP"});
        wscEncAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWscEncrypt.setAdapter(wscEncAdapter);

        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Facebook", "Twitter", "Instagram", "LinkedIn", "TikTok"});
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNdefSocialPlatform.setAdapter(socialAdapter);

        ArrayAdapter<String> msgAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"LINE", "WhatsApp", "Telegram", "WeChat", "Signal"});
        msgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNdefMessengerType.setAdapter(msgAdapter);

        showFields(0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        currentTag = tag;

        String uid = Converter.hex(tag.getId());
        StringBuilder sb = new StringBuilder("卡片已偵測\nUID: ").append(uid);
        for (String t : tag.getTechList()) {
            String name = t.substring(t.lastIndexOf('.') + 1);
            sb.append("\n  ").append(name);
        }
        // Check NDEF
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            sb.append("\nNDEF 可用，大小: ").append(ndef.getMaxSize()).append(" bytes");
        }
        txtNdefInfo.setText(sb.toString());
    }

    private void showFields(int pos) {
        for (int i = 0; i < allFields.size(); i++) {
            allFields.get(i).setVisibility(i == pos ? View.VISIBLE : View.GONE);
        }
    }

    private void pickContact(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;

        Uri contactUri = data.getData();
        switch (requestCode) {
            case REQ_PICK_CONTACT_PHONE: {
                String phone = getContactPhone(contactUri);
                if (phone != null) editPhone.setText(phone);
                break;
            }
            case REQ_PICK_CONTACT_EMAIL: {
                String email = getContactEmail(contactUri);
                if (email != null) editEmailTo.setText(email);
                break;
            }
            case REQ_PICK_CONTACT_SMS: {
                String phone = getContactPhone(contactUri);
                if (phone != null) editSmsNum.setText(phone);
                break;
            }
            case REQ_PICK_CONTACT_VCARD: {
                String name = getContactName(contactUri);
                String phone = getContactPhone(contactUri);
                String email = getContactEmail(contactUri);
                if (name != null) editVCardName.setText(name);
                if (phone != null) editVCardPhone.setText(phone);
                if (email != null) editVCardEmail.setText(email);
                break;
            }
            case REQ_PICK_CONTACT_ICE: {
                String name = getContactName(contactUri);
                String phone = getContactPhone(contactUri);
                if (name != null) editIceName.setText(name);
                if (phone != null) editIcePhone.setText(phone);
                break;
            }
        }
    }

    private String getContactName(Uri contactUri) {
        try (Cursor c = getContentResolver().query(contactUri,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getContactPhone(Uri contactUri) {
        String id = null;
        try (Cursor c = getContentResolver().query(contactUri,
                new String[]{ContactsContract.Contacts._ID}, null, null, null)) {
            if (c != null && c.moveToFirst())
                id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
        } catch (Exception ignored) {}

        if (id == null) return null;
        try (Cursor c = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                new String[]{id}, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String getContactEmail(Uri contactUri) {
        String id = null;
        try (Cursor c = getContentResolver().query(contactUri,
                new String[]{ContactsContract.Contacts._ID}, null, null, null)) {
            if (c != null && c.moveToFirst())
                id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
        } catch (Exception ignored) {}

        if (id == null) return null;
        try (Cursor c = getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS},
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?",
                new String[]{id}, null)) {
            if (c != null && c.moveToFirst()) {
                return c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void doWrite() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }

        NdefRecord record = buildRecord();
        if (record == null) return;

        String result = NFCWriter.writeNdefMessage(currentTag, record);
        txtNdefResult.setText(result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    private void doRead() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                Ndef ndef = Ndef.get(currentTag);
                if (ndef == null) {
                    runOnUiThread(() -> txtNdefResult.setText("不支援 NDEF"));
                    return;
                }
                ndef.connect();
                NdefMessage msg = ndef.getNdefMessage();
                ndef.close();

                if (msg == null) {
                    runOnUiThread(() -> txtNdefResult.setText("標籤無 NDEF 資料"));
                    return;
                }

                StringBuilder sb = new StringBuilder("=== NDEF 內容 ===\n");
                sb.append("記錄數: ").append(msg.getRecords().length).append("\n\n");
                for (NdefRecord r : msg.getRecords()) {
                    sb.append("TNF: ").append(r.getTnf()).append("\n");
                    byte[] type = r.getType();
                    String typeStr = (type != null) ? new String(type, StandardCharsets.US_ASCII) : "";
                    sb.append("Type: ").append(typeStr).append("\n");
                    if (r.getTnf() == NdefRecord.TNF_WELL_KNOWN && "Sp".equals(typeStr)) {
                        sb.append("類型: Smart Poster\n");
                        decodeSmartPoster(r, sb);
                    } else if (r.getTnf() == NdefRecord.TNF_EXTERNAL_TYPE && "android.com:pkg".equals(typeStr)) {
                        sb.append("類型: Android App Record\n");
                        sb.append("  套件: ").append(new String(r.getPayload(), StandardCharsets.UTF_8)).append("\n");
                    } else {
                        byte[] payload = r.getPayload();
                        if (payload != null) {
                            String text = tryDecode(payload);
                            sb.append("資料: ").append(text).append("\n");
                        }
                    }
                    sb.append("---\n");
                }
                final String res = sb.toString();
                runOnUiThread(() -> txtNdefResult.setText(res));
            } catch (Exception e) {
                runOnUiThread(() -> txtNdefResult.setText("讀取錯誤: " + e.getMessage()));
            }
        }).start();
    }

    private NdefRecord buildRecord() {
        int type = spinnerType.getSelectedItemPosition();
        try {
            switch (type) {
                case 0: {
                    String text = getText(editText);
                    if (text.isEmpty()) { Toast.makeText(this, "請輸入文字", Toast.LENGTH_SHORT).show(); return null; }
                    return NdefRecord.createTextRecord("zh", text);
                }
                case 1: {
                    String url = getText(editUrl);
                    if (url.isEmpty()) { Toast.makeText(this, "請輸入網址", Toast.LENGTH_SHORT).show(); return null; }
                    return NFCWriter.createUrlRecord(url);
                }
                case 2: {
                    String to = getText(editEmailTo);
                    if (to.isEmpty()) { Toast.makeText(this, "請輸入 Email", Toast.LENGTH_SHORT).show(); return null; }
                    return NFCWriter.createEmailRecord(to, getText(editEmailSubj), getText(editEmailBody));
                }
                case 3: {
                    String phone = getText(editPhone);
                    if (phone.isEmpty()) { Toast.makeText(this, "請輸入電話", Toast.LENGTH_SHORT).show(); return null; }
                    return NFCWriter.createPhoneRecord(phone);
                }
                case 4: {
                    String num = getText(editSmsNum);
                    if (num.isEmpty()) { Toast.makeText(this, "請輸入號碼", Toast.LENGTH_SHORT).show(); return null; }
                    return NFCWriter.createSmsRecord(num, getText(editSmsMsg));
                }
                case 5: {
                    String ssid = getText(editSsid);
                    if (ssid.isEmpty()) { Toast.makeText(this, "請輸入 SSID", Toast.LENGTH_SHORT).show(); return null; }
                    return NFCWriter.createWifiConfigRecord(ssid, getText(editWifiPwd), "WPA");
                }
                case 6: {
                    String name = getText(editVCardName);
                    if (name.isEmpty()) { Toast.makeText(this, "請輸入姓名", Toast.LENGTH_SHORT).show(); return null; }
                    return NFCWriter.createVCardRecord(name, getText(editVCardPhone), getText(editVCardEmail));
                }
                case 7: {
                    String lat = getText(editLat);
                    String lng = getText(editLng);
                    if (lat.isEmpty() || lng.isEmpty()) { Toast.makeText(this, "請輸入經緯度", Toast.LENGTH_SHORT).show(); return null; }
                    return NFCWriter.createGeoLocationRecord(Double.parseDouble(lat), Double.parseDouble(lng));
                }
                case 8: {
                    String mac = getText(editBtMac);
                    if (mac.isEmpty()) { Toast.makeText(this, "請輸入藍牙 MAC", Toast.LENGTH_SHORT).show(); return null; }
                    return NFCWriter.createBluetoothRecord(mac, getText(editBtName));
                }
                case 9: {
                    String title = getText(editSpTitle);
                    String url = getText(editSpUrl);
                    if (title.isEmpty() || url.isEmpty()) {
                        Toast.makeText(this, "請輸入標題和 URL", Toast.LENGTH_SHORT).show(); return null;
                    }
                    int action = 0;
                    try { action = Integer.parseInt(getText(editSpAction)); } catch (Exception e) { action = 0; }
                    if (action < 0 || action > 2) action = 0;
                    return createSmartPosterRecord(title, url, (byte)action);
                }
                case 10: {
                    String pkg = getText(editAarPkg);
                    if (pkg.isEmpty()) { Toast.makeText(this, "請輸入套件名稱", Toast.LENGTH_SHORT).show(); return null; }
                    return createAarRecord(pkg);
                }
                case 11: {
                    String name = getText(editIceName);
                    String phone = getText(editIcePhone);
                    String rel = getText(editIceRelation);
                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "請輸入姓名和電話", Toast.LENGTH_SHORT).show(); return null;
                    }
                    String vcard = "BEGIN:VCARD\nVERSION:3.0\nFN:" + name
                            + "\nTEL:" + phone + "\nX-RELATION:" + rel + "\nX-ICE:Emergency\nEND:VCARD";
                    return new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                            "text/vcard".getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                            new byte[]{}, vcard.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                case 12: {
                    int tnf = spinnerNdefCustomTnf.getSelectedItemPosition();
                    String typeStr = getText(editCustomType);
                    String payloadHex = getText(editCustomData);
                    byte[] typeBytes = typeStr.isEmpty() ? new byte[0] : typeStr.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
                    byte[] payload;
                    try {
                        payload = payloadHex.isEmpty() ? new byte[0] : Converter.hexToBytes(payloadHex);
                    } catch (Exception e) {
                        Toast.makeText(this, "Payload 格式錯誤，請輸入 hex 字串", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                    return new NdefRecord((short) tnf, typeBytes, new byte[0], payload);
                }
                case 13: {
                    String ssid = getText(editWscSsid);
                    String pwd = getText(editWscPwd);
                    if (ssid.isEmpty()) {
                        Toast.makeText(this, "請輸入 SSID", Toast.LENGTH_SHORT).show(); return null;
                    }
                    String auth = spinnerWscAuth.getSelectedItem().toString();
                    String enc = spinnerWscEncrypt.getSelectedItem().toString();
                    return createWscRecord(ssid, pwd, auth, enc);
                }
                case 14: {
                    String addr = getText(editNdefBtcAddress);
                    if (addr.isEmpty()) { Toast.makeText(this, "請輸入比特幣地址", Toast.LENGTH_SHORT).show(); return null; }
                    String amt = getText(editNdefBtcAmount);
                    String lbl = getText(editNdefBtcLabel);
                    StringBuilder uri = new StringBuilder("bitcoin:").append(addr);
                    boolean first = true;
                    if (!amt.isEmpty()) { uri.append(first ? "?" : "&").append("amount=").append(android.net.Uri.encode(amt)); first = false; }
                    if (!lbl.isEmpty()) { uri.append(first ? "?" : "&").append("label=").append(android.net.Uri.encode(lbl)); }
                    return NFCWriter.createUrlRecord(uri.toString());
                }
                case 15: {
                    String platform = spinnerNdefSocialPlatform.getSelectedItem().toString();
                    String handle = getText(editNdefSocialHandle);
                    if (handle.isEmpty()) { Toast.makeText(this, "請輸入使用者名稱", Toast.LENGTH_SHORT).show(); return null; }
                    String url;
                    switch (platform) {
                        case "Facebook": url = "fb://profile/" + handle; break;
                        case "Twitter": url = "twitter://user?screen_name=" + handle; break;
                        case "Instagram": url = "instagram://user?username=" + handle; break;
                        case "LinkedIn": url = "https://linkedin.com/in/" + handle; break;
                        case "TikTok": url = "https://tiktok.com/@" + handle; break;
                        default: url = "https://" + handle; break;
                    }
                    return NFCWriter.createUrlRecord(url);
                }
                case 16: {
                    String mtype = spinnerNdefMessengerType.getSelectedItem().toString();
                    String id = getText(editNdefMessengerId);
                    if (id.isEmpty()) { Toast.makeText(this, "請輸入 ID", Toast.LENGTH_SHORT).show(); return null; }
                    String url;
                    switch (mtype) {
                        case "LINE": url = "https://lin.ee/" + id; break;
                        case "WhatsApp": url = "whatsapp://send?phone=" + id; break;
                        case "Telegram": url = "tg://resolve?domain=" + id; break;
                        case "WeChat": url = "https://weixin.qq.com/r/" + id; break;
                        case "Signal": url = "https://signal.me/#p/" + id; break;
                        default: url = "https://" + id; break;
                    }
                    return NFCWriter.createUrlRecord(url);
                }
                default: return NdefRecord.createTextRecord("zh", "HeliRFID");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "數值格式錯誤", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String tryDecode(byte[] payload) {
        try {
            // Try text record
            if (payload.length > 3) {
                int langLen = payload[0] & 0x3F;
                if (payload.length > langLen + 1)
                    return new String(payload, 1 + langLen, payload.length - 1 - langLen, StandardCharsets.UTF_8);
            }
            return new String(payload, StandardCharsets.UTF_8).replace('\n', ' ');
        } catch (Exception e) {
            return "(binary " + payload.length + " bytes)";
        }
    }

    private NdefRecord createSmartPosterRecord(String title, String url, byte action) {
        NdefRecord uriRecord = NdefRecord.createUri(url);
        byte[] langBytes = "zh".getBytes(StandardCharsets.US_ASCII);
        byte[] titleText = title.getBytes(StandardCharsets.UTF_8);
        byte[] titlePayload = new byte[1 + langBytes.length + titleText.length];
        titlePayload[0] = (byte)(langBytes.length & 0x3F);
        System.arraycopy(langBytes, 0, titlePayload, 1, langBytes.length);
        System.arraycopy(titleText, 0, titlePayload, 1 + langBytes.length, titleText.length);
        NdefRecord titleRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], titlePayload);
        NdefRecord actionRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                "act".getBytes(StandardCharsets.US_ASCII), new byte[0],
                new byte[]{action});
        NdefMessage innerMsg = new NdefMessage(new NdefRecord[]{titleRecord, uriRecord, actionRecord});
        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                "Sp".getBytes(StandardCharsets.US_ASCII), new byte[0], innerMsg.toByteArray());
    }

    private NdefRecord createAarRecord(String packageName) {
        return new NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE,
                "android.com:pkg".getBytes(StandardCharsets.US_ASCII),
                new byte[0], packageName.getBytes(StandardCharsets.UTF_8));
    }

    private void decodeSmartPoster(NdefRecord r, StringBuilder sb) {
        try {
            NdefMessage inner = new NdefMessage(r.getPayload());
            for (NdefRecord in : inner.getRecords()) {
                String type = new String(in.getType(), StandardCharsets.US_ASCII);
                byte[] pl = in.getPayload();
                if (in.getTnf() == NdefRecord.TNF_WELL_KNOWN && NdefRecord.RTD_TEXT.equals(in.getType())) {
                    int langLen = (pl[0] & 0x3F);
                    String t = new String(pl, 1 + langLen, pl.length - 1 - langLen, StandardCharsets.UTF_8);
                    sb.append("  標題: ").append(t).append("\n");
                } else if (in.getTnf() == NdefRecord.TNF_WELL_KNOWN && "act".equals(type)) {
                    int a = pl[0] & 0xFF;
                    String[] acts = {"執行", "儲存", "編輯"};
                    sb.append("  動作: ").append(a < 3 ? acts[a] : "未知").append("\n");
                } else if (type.equals("U") || type.startsWith("http")) {
                    String u = new String(pl, 1, pl.length - 1, StandardCharsets.UTF_8);
                    sb.append("  URL: ").append(u).append("\n");
                } else {
                    sb.append("  ").append(type).append(": ").append(tryDecode(pl)).append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("  (解碼失敗: ").append(e.getMessage()).append(")\n");
        }
    }

    private void doClear() {
        if (currentTag == null) {
            Toast.makeText(this, "請先掃描 NFC 標籤", Toast.LENGTH_SHORT).show();
            return;
        }
        String result = NFCWriter.clearNdef(currentTag);
        txtNdefResult.setText(result);
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    private NdefRecord createWscRecord(String ssid, String password, String auth, String encrypt) {
        try {
            byte[] ssidBytes = ssid.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] pwdBytes = password.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            os.write(new byte[]{(byte)0x10, (byte)0x45, 0, (byte)ssidBytes.length});
            os.write(ssidBytes);

            int authType;
            switch (auth) {
                case "WPA": authType = 0x0020; break;
                case "WPA2": authType = 0x0040; break;
                case "WPA2PSK": authType = 0x0048; break;
                default: authType = 0x0001;
            }
            os.write(new byte[]{(byte)0x10, (byte)0x03, 0, 2});
            os.write(new byte[]{(byte)(authType >> 8), (byte)authType});

            int encType;
            switch (encrypt) {
                case "TKIP": encType = 0x0008; break;
                case "AES": encType = 0x0010; break;
                case "AES/TKIP": encType = 0x0018; break;
                default: encType = 0x0001;
            }
            os.write(new byte[]{(byte)0x10, (byte)0x0F, 0, 2});
            os.write(new byte[]{(byte)(encType >> 8), (byte)encType});

            if (password != null && !password.isEmpty()) {
                os.write(new byte[]{(byte)0x10, (byte)0x27, 0, (byte)pwdBytes.length});
                os.write(pwdBytes);
            }

            byte[] wscData = os.toByteArray();
            return new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                    "application/vnd.wfa.wsc".getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                    new byte[]{}, wscData);
        } catch (Exception e) {
            Toast.makeText(this, "WSC 建構錯誤: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String resolveVariables(String input) {
        if (input == null || input.isEmpty()) return input;
        String result = input;
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateTimeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        result = result.replace("%DATE%", dateFmt.format(new Date()));
        result = result.replace("%TIME%", timeFmt.format(new Date()));
        result = result.replace("%DATETIME%", dateTimeFmt.format(new Date()));

        android.content.IntentFilter ifilter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
        android.content.Intent batteryStatus = registerReceiver(null, ifilter);
        int level = batteryStatus != null ? batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) : -1;
        result = result.replace("%BATTERY%", level >= 0 ? String.valueOf(level) : "N/A");

        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo wi = wm.getConnectionInfo();
                String ssid = wi != null ? wi.getSSID() : "N/A";
                if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\""))
                    ssid = ssid.substring(1, ssid.length() - 1);
                result = result.replace("%WIFI_SSID%", ssid != null ? ssid : "N/A");
            }
        } catch (Exception e) {
            result = result.replace("%WIFI_SSID%", "N/A");
        }

        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo wi = wm.getConnectionInfo();
                int ipInt = wi != null ? wi.getIpAddress() : 0;
                String ip = (ipInt & 0xFF) + "." + ((ipInt >> 8) & 0xFF) + "." + ((ipInt >> 16) & 0xFF) + "." + ((ipInt >> 24) & 0xFF);
                result = result.replace("%WIFI_IP%", ip);
            }
        } catch (Exception e) {
            result = result.replace("%WIFI_IP%", "N/A");
        }

        result = result.replace("%DEVICE_NAME%", Build.MODEL);

        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        androidId = androidId != null ? androidId : "N/A";
        result = result.replace("%ANDROID_ID%", androidId);

        Random rnd = new Random();
        result = result.replace("%RANDOM%", String.format("%06d", rnd.nextInt(1000000)));

        if (currentTag != null) {
            result = result.replace("%UID%", Converter.hex(currentTag.getId()));
        } else {
            result = result.replace("%UID%", "N/A");
        }

        SharedPreferences prefs = getSharedPreferences("dynamic_vars", Context.MODE_PRIVATE);
        int counter = prefs.getInt("counter", 0) + 1;
        prefs.edit().putInt("counter", counter).apply();
        result = result.replace("%COUNT%", String.valueOf(counter));

        return result;
    }

    private String getText(EditText et) {
        String raw = et.getText().toString().trim();
        String resolved = resolveVariables(raw);
        if (!raw.equals(resolved)) {
            txtNdefResult.setText("動態變數已解析:\n原始: " + raw + "\n解析後: " + resolved);
        }
        return resolved;
    }
}
