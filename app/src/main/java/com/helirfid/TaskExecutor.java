package com.helirfid;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.nfc.Tag;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class TaskExecutor {

    // === Original 18 actions (0-17) ===
    public static final int ACTION_TOGGLE_WIFI = 0;
    public static final int ACTION_TOGGLE_BLUETOOTH = 1;
    public static final int ACTION_SET_SOUND_PROFILE = 2;
    public static final int ACTION_SET_VOLUME = 3;
    public static final int ACTION_SET_BRIGHTNESS = 4;
    public static final int ACTION_LAUNCH_APP = 5;
    public static final int ACTION_SEND_SMS = 6;
    public static final int ACTION_OPEN_URL = 7;
    public static final int ACTION_SET_ALARM = 8;
    public static final int ACTION_MAKE_CALL = 9;
    public static final int ACTION_LOCK_SCREEN = 10;
    public static final int ACTION_OPEN_SETTINGS = 11;
    public static final int ACTION_SET_WIFI_CONFIG = 12;
    public static final int ACTION_ADD_CALENDAR_EVENT = 13;
    public static final int ACTION_TTS_SPEAK = 14;
    public static final int ACTION_START_TIMER = 15;
    public static final int ACTION_SET_AIRPLANE_MODE = 16;
    public static final int ACTION_TOGGLE_AUTO_ROTATE = 17;

    // === Connectivity (18-25) ===
    public static final int ACTION_TOGGLE_WIFI_HOTSPOT = 18;
    public static final int ACTION_TOGGLE_MOBILE_DATA = 19;
    public static final int ACTION_TOGGLE_NFC = 20;
    public static final int ACTION_TOGGLE_LOCATION = 21;
    public static final int ACTION_SET_WIFI_TETHERING = 22;
    public static final int ACTION_TOGGLE_VPN = 23;
    public static final int ACTION_CONNECT_BLUETOOTH_DEVICE = 24;

    // === Display (25-29) ===
    public static final int ACTION_SET_SCREEN_TIMEOUT = 25;
    public static final int ACTION_SET_SCREEN_BRIGHTNESS_MODE = 26;
    public static final int ACTION_TOGGLE_DARK_MODE = 27;
    public static final int ACTION_SET_FONT_SIZE = 28;

    // === Audio (29-36) ===
    public static final int ACTION_SET_MEDIA_VOLUME = 29;
    public static final int ACTION_SET_RING_VOLUME = 30;
    public static final int ACTION_SET_ALARM_VOLUME = 31;
    public static final int ACTION_SET_NOTIFICATION_VOLUME = 32;
    public static final int ACTION_TOGGLE_DND = 33;
    public static final int ACTION_PLAY_SOUND = 34;
    public static final int ACTION_SET_RINGTONE = 35;

    // === Media (36-42) ===
    public static final int ACTION_PLAY_MUSIC = 36;
    public static final int ACTION_PAUSE_MUSIC = 37;
    public static final int ACTION_NEXT_TRACK = 38;
    public static final int ACTION_PREV_TRACK = 39;
    public static final int ACTION_TAKE_PHOTO = 40;
    public static final int ACTION_OPEN_GALLERY = 41;

    // === Notifications (42-45) ===
    public static final int ACTION_SHOW_NOTIFICATION = 42;
    public static final int ACTION_CLEAR_ALL_NOTIFICATIONS = 43;
    public static final int ACTION_SET_DND_MODE = 44;

    // === Input (45-46) ===
    public static final int ACTION_TOGGLE_KEYBOARD = 45;
    public static final int ACTION_TYPE_TEXT = 46;

    // === System (47-58) ===
    public static final int ACTION_SHOW_TOAST = 47;
    public static final int ACTION_SET_WALLPAPER = 48;
    public static final int ACTION_REBOOT = 49;
    public static final int ACTION_SHUTDOWN = 50;
    public static final int ACTION_OPEN_APP_DRAWER = 51;
    public static final int ACTION_GO_HOME = 52;
    public static final int ACTION_GO_BACK = 53;
    public static final int ACTION_TOGGLE_SPLIT_SCREEN = 54;
    public static final int ACTION_OPEN_HOTSPOT_SETTINGS = 55;
    public static final int ACTION_OPEN_WIFI_SETTINGS = 56;
    public static final int ACTION_OPEN_BLUETOOTH_SETTINGS = 57;
    public static final int ACTION_OPEN_NFC_SETTINGS = 58;
    public static final int ACTION_OPEN_DATA_USAGE = 59;

    // === Sensors (60-61) ===
    public static final int ACTION_TOGGLE_FLASHLIGHT = 60;
    public static final int ACTION_VIBRATE = 61;

    // === Accounts (62-70) ===
    public static final int ACTION_OPEN_CONTACTS = 62;
    public static final int ACTION_OPEN_DIALER = 63;
    public static final int ACTION_OPEN_MESSAGES = 64;
    public static final int ACTION_OPEN_CALENDAR = 65;
    public static final int ACTION_OPEN_CAMERA = 66;
    public static final int ACTION_OPEN_EMAIL = 67;
    public static final int ACTION_OPEN_MAPS = 68;
    public static final int ACTION_OPEN_PLAY_STORE = 69;

    // === Device Control (70-75) ===
    public static final int ACTION_TOGGLE_DOCK = 70;
    public static final int ACTION_TOGGLE_CAR_MODE = 71;
    public static final int ACTION_TOGGLE_WIFI_CALLING = 72;
    public static final int ACTION_TOGGLE_VOLTE = 73;
    public static final int ACTION_SET_SCREEN_ROTATION = 74;

    // === File Operations (75-79) ===
    public static final int ACTION_CREATE_FILE = 75;
    public static final int ACTION_DELETE_FILE = 76;
    public static final int ACTION_WRITE_FILE = 77;
    public static final int ACTION_READ_FILE = 78;

    // === Connectivity Extended (79-87) ===
    public static final int ACTION_TOGGLE_USB_TETHERING = 79;
    public static final int ACTION_TOGGLE_BLUETOOTH_TETHERING = 80;
    public static final int ACTION_TOGGLE_AUTO_SYNC = 81;
    public static final int ACTION_TOGGLE_NEARBY_SHARING = 82;
    public static final int ACTION_TOGGLE_ANDROID_BEAM = 83;
    public static final int ACTION_OPEN_APN_SETTINGS = 84;
    public static final int ACTION_OPEN_CAST_SETTINGS = 85;
    public static final int ACTION_OPEN_WIFI_P2P_SETTINGS = 86;
    public static final int ACTION_OPEN_DATA_ROAMING = 87;

    // === Display Extended (88-97) ===
    public static final int ACTION_TOGGLE_COLOR_INVERSION = 88;
    public static final int ACTION_TOGGLE_AMBIENT_DISPLAY = 89;
    public static final int ACTION_TOGGLE_SCREENSAVER = 90;
    public static final int ACTION_TOGGLE_EXTRA_DIM = 91;
    public static final int ACTION_TOGGLE_READING_MODE = 92;
    public static final int ACTION_TOGGLE_GESTURE_NAV = 93;
    public static final int ACTION_TOGGLE_NAV_BAR = 94;
    public static final int ACTION_SET_DISPLAY_SIZE = 95;
    public static final int ACTION_TOGGLE_HIGH_CONTRAST_TEXT = 96;
    public static final int ACTION_TOGGLE_DISPLAY_CUTOUT = 97;

    // === Audio Extended (98-108) ===
    public static final int ACTION_SET_VOLUME_MUTE = 98;
    public static final int ACTION_TOGGLE_MONO_AUDIO = 99;
    public static final int ACTION_TOGGLE_LIVE_CAPTION = 100;
    public static final int ACTION_TOGGLE_HEARING_AID = 101;
    public static final int ACTION_TOGGLE_ADAPTIVE_SOUND = 102;
    public static final int ACTION_SET_AUDIO_BALANCE = 103;
    public static final int ACTION_TOGGLE_SOUND_AMPLIFIER = 104;
    public static final int ACTION_TOGGLE_HAPTIC_FEEDBACK = 105;
    public static final int ACTION_TOGGLE_DTMF_TONE = 106;
    public static final int ACTION_TOGGLE_SOUND_EFFECTS = 107;
    public static final int ACTION_TOGGLE_VIBRATE_ON_NOTIF = 108;

    // === Media Extended (109-115) ===
    public static final int ACTION_OPEN_YOUTUBE = 109;
    public static final int ACTION_OPEN_SPOTIFY = 110;
    public static final int ACTION_RECORD_AUDIO = 111;
    public static final int ACTION_STOP_RECORDING = 112;
    public static final int ACTION_TOGGLE_PLAYBACK = 113;
    public static final int ACTION_TOGGLE_SHUFFLE = 114;
    public static final int ACTION_TOGGLE_REPEAT = 115;

    // === Notifications Extended (116-122) ===
    public static final int ACTION_OPEN_NOTIF_HISTORY = 116;
    public static final int ACTION_TOGGLE_NOTIFICATION_DOT = 117;
    public static final int ACTION_TOGGLE_LOCKSCREEN_NOTIF = 118;
    public static final int ACTION_TOGGLE_VIBRATE_MODE = 119;
    public static final int ACTION_OPEN_NOTIF_SETTINGS = 120;
    public static final int ACTION_TOGGLE_ALARM_ONLY = 121;
    public static final int ACTION_OPEN_STATUS_BAR = 122;

    // === System Settings (123-142) ===
    public static final int ACTION_OPEN_DEVELOPER_OPTIONS = 123;
    public static final int ACTION_OPEN_ACCESSIBILITY = 124;
    public static final int ACTION_OPEN_BATTERY_SETTINGS = 125;
    public static final int ACTION_OPEN_STORAGE_SETTINGS = 126;
    public static final int ACTION_OPEN_SECURITY_SETTINGS = 127;
    public static final int ACTION_OPEN_APP_INFO = 128;
    public static final int ACTION_OPEN_DATE_SETTINGS = 129;
    public static final int ACTION_OPEN_LANGUAGE_SETTINGS = 130;
    public static final int ACTION_OPEN_PRIVACY_SETTINGS = 131;
    public static final int ACTION_OPEN_LOCATION_SETTINGS = 132;
    public static final int ACTION_OPEN_APP_PERMISSIONS = 133;
    public static final int ACTION_OPEN_USER_SETTINGS = 134;
    public static final int ACTION_OPEN_PRINT_SETTINGS = 135;
    public static final int ACTION_OPEN_DEFAULT_APPS = 136;
    public static final int ACTION_OPEN_BATTERY_OPTIMIZATION = 137;
    public static final int ACTION_TOGGLE_POWER_SAVE = 138;
    public static final int ACTION_OPEN_MEMORY_SETTINGS = 139;
    public static final int ACTION_OPEN_SOUND_SETTINGS = 140;
    public static final int ACTION_OPEN_DISPLAY_SETTINGS = 141;
    public static final int ACTION_OPEN_NETWORK_SETTINGS = 142;

    // === System Control (143-150) ===
    public static final int ACTION_SCREENSHOT = 143;
    public static final int ACTION_OPEN_RECENT_APPS = 144;
    public static final int ACTION_TOGGLE_PIN_SCREEN = 145;
    public static final int ACTION_LOCK_DOWN = 146;
    public static final int ACTION_OPEN_POWER_MENU = 147;
    public static final int ACTION_SHOW_IME_PICKER = 148;
    public static final int ACTION_TOGGLE_CAPS_LOCK = 149;
    public static final int ACTION_TOGGLE_NUM_LOCK = 150;

    // === Share & Launch (151-160) ===
    public static final int ACTION_SHARE_TEXT = 151;
    public static final int ACTION_OPEN_FILES = 152;
    public static final int ACTION_OPEN_DOWNLOADS = 153;
    public static final int ACTION_OPEN_CHROME = 154;
    public static final int ACTION_OPEN_CLOCK = 155;
    public static final int ACTION_OPEN_CALCULATOR = 156;
    public static final int ACTION_COPY_TO_CLIPBOARD = 157;
    public static final int ACTION_PASTE_FROM_CLIPBOARD = 158;
    public static final int ACTION_OPEN_WEATHER = 159;
    public static final int ACTION_OPEN_PHOTOS = 160;

    // === Flow Control (161-165) ===
    public static final int ACTION_WAIT = 161;
    public static final int ACTION_RUN_PROFILE = 162;
    public static final int ACTION_EXIT_TASK = 163;
    public static final int ACTION_SET_VARIABLE = 164;
    public static final int ACTION_ADD_VARIABLE = 165;

    // === Connectivity Advanced (166-172) ===
    public static final int ACTION_TOGGLE_ADAPTIVE_CONNECTIVITY = 166;
    public static final int ACTION_TOGGLE_BLUETOOTH_SCAN = 167;
    public static final int ACTION_TOGGLE_WIFI_SCAN = 168;
    public static final int ACTION_OPEN_NFC_READER_MODE = 169;
    public static final int ACTION_OPEN_P2P_SETTINGS = 170;
    public static final int ACTION_OPEN_WIFI_SAVED_NETWORKS = 171;
    public static final int ACTION_OPEN_BLUETOOTH_PAIRING = 172;

    // === Display Advanced (173-178) ===
    public static final int ACTION_TOGGLE_NIGHT_LIGHT = 173;
    public static final int ACTION_TOGGLE_COLOR_CORRECTION = 174;
    public static final int ACTION_TOGGLE_REDUCE_ANIMATION = 175;
    public static final int ACTION_TOGGLE_SCREEN_ROTATION_LOCK = 176;
    public static final int ACTION_SET_WALLPAPER_LOCK = 177;
    public static final int ACTION_TOGGLE_ADAPTIVE_BRIGHTNESS = 178;

    // === Audio Advanced (179-182) ===
    public static final int ACTION_TOGGLE_ABSOLUTE_VOLUME = 179;
    public static final int ACTION_SWITCH_AUDIO_OUTPUT = 180;
    public static final int ACTION_TOGGLE_DOLBY_ATMOS = 181;
    public static final int ACTION_TOGGLE_VOLUME_KEY_MEDIA = 182;

    // === More System Settings (183-192) ===
    public static final int ACTION_OPEN_WALLPAPER_SETTINGS = 183;
    public static final int ACTION_OPEN_LOCKSCREEN_SETTINGS = 184;
    public static final int ACTION_OPEN_FONT_SETTINGS = 185;
    public static final int ACTION_OPEN_DND_SETTINGS = 186;
    public static final int ACTION_OPEN_NOTIF_SETTINGS_MAIN = 187;
    public static final int ACTION_OPEN_GESTURE_SETTINGS = 188;
    public static final int ACTION_OPEN_ABOUT_PHONE = 189;
    public static final int ACTION_OPEN_SIM_SETTINGS = 190;
    public static final int ACTION_OPEN_HOTSPOT_AND_TETHERING = 191;
    public static final int ACTION_OPEN_BIOMETRIC_SETTINGS = 192;

    // === Quick Launch Apps (193-199) ===
    public static final int ACTION_LAUNCH_GMAIL = 193;
    public static final int ACTION_LAUNCH_DRIVE = 194;
    public static final int ACTION_LAUNCH_TWITTER = 195;
    public static final int ACTION_LAUNCH_FACEBOOK = 196;
    public static final int ACTION_LAUNCH_INSTAGRAM = 197;
    public static final int ACTION_LAUNCH_LINE = 198;
    public static final int ACTION_LAUNCH_TELEGRAM = 199;

    // === Tags / NDEF (200-214) ===
    public static final int ACTION_TAG_INVENTORY = 200;
    public static final int ACTION_TAG_INFO = 201;
    public static final int ACTION_TAG_UID = 202;
    public static final int ACTION_TAG_TECH = 203;
    public static final int ACTION_TAG_SIZE = 204;
    public static final int ACTION_READ_NDEF = 205;
    public static final int ACTION_WRITE_NDEF_TEXT = 206;
    public static final int ACTION_WRITE_NDEF_URI = 207;
    public static final int ACTION_FORMAT_TAG = 208;
    public static final int ACTION_LOCK_TAG = 209;
    public static final int ACTION_TAG_CYCLES = 210;
    public static final int ACTION_CHECK_ORIGINALITY = 211;
    public static final int ACTION_SET_TAG_PASSWORD = 212;
    public static final int ACTION_REMOVE_TAG_PASSWORD = 213;
    public static final int ACTION_PROTECT_TAG = 214;

    // === Batch / CSV (215-219) ===
    public static final int ACTION_BATCH_WRITE = 215;
    public static final int ACTION_BATCH_LOCK = 216;
    public static final int ACTION_BATCH_FORMAT = 217;
    public static final int ACTION_CSV_IMPORT = 218;
    public static final int ACTION_CSV_EXPORT = 219;

    // === QR / Code (220-223) ===
    public static final int ACTION_GENERATE_QR = 220;
    public static final int ACTION_SCAN_QR = 221;
    public static final int ACTION_QR_TO_NDEF = 222;
    public static final int ACTION_NDEF_TO_QR = 223;

    // === Webhook / Network (224-228) ===
    public static final int ACTION_WEBHOOK_GET = 224;
    public static final int ACTION_WEBHOOK_POST = 225;
    public static final int ACTION_HTTP_REQUEST = 226;
    public static final int ACTION_CHECK_CONNECTIVITY = 227;
    public static final int ACTION_GET_PUBLIC_IP = 228;

    // === TTS / Audio (229-232) ===
    public static final int ACTION_TTS_SAY = 229;
    public static final int ACTION_TTS_STOP = 230;
    public static final int ACTION_ANNOUNCE_TIME = 231;
    public static final int ACTION_ANNOUNCE_DATE = 232;

    // === Conditional / Flow Control (233-244) ===
    public static final int ACTION_IF_TAG_PRESENT = 233;
    public static final int ACTION_IF_WIFI_CONNECTED = 234;
    public static final int ACTION_IF_BT_CONNECTED = 235;
    public static final int ACTION_IF_TIME_BETWEEN = 236;
    public static final int ACTION_IF_DAY_OF_WEEK = 237;
    public static final int ACTION_IF_VARIABLE_EQUALS = 238;
    public static final int ACTION_IF_VARIABLE_GREATER = 239;
    public static final int ACTION_IF_VARIABLE_LESS = 240;
    public static final int ACTION_ELSE = 241;
    public static final int ACTION_ENDIF = 242;
    public static final int ACTION_WHILE = 243;
    public static final int ACTION_BREAK = 244;

    // === Variable Operations (245-249) ===
    public static final int ACTION_VARIABLE_INCREMENT = 245;
    public static final int ACTION_VARIABLE_DECREMENT = 246;
    public static final int ACTION_VARIABLE_CONCAT = 247;
    public static final int ACTION_VARIABLE_CLEAR = 248;
    public static final int ACTION_VARIABLE_RANDOM = 249;

    // === File Operations (250-254) ===
    public static final int ACTION_COPY_FILE = 250;
    public static final int ACTION_MOVE_FILE = 251;
    public static final int ACTION_RENAME_FILE = 252;
    public static final int ACTION_LIST_FILES = 253;
    public static final int ACTION_FILE_EXISTS = 254;

    // === App Control (255-264) ===
    public static final int ACTION_KILL_APP = 255;
    public static final int ACTION_CLEAR_APP_CACHE = 256;
    public static final int ACTION_CLEAR_APP_DATA = 257;
    public static final int ACTION_FORCE_STOP = 258;
    public static final int ACTION_DISABLE_APP = 259;
    public static final int ACTION_ENABLE_APP = 260;
    public static final int ACTION_BLOCK_APP = 261;
    public static final int ACTION_UNBLOCK_APP = 262;
    public static final int ACTION_LOCK_APP = 263;
    public static final int ACTION_UNLOCK_APP = 264;

    // === Vault / Encryption (265-269) ===
    public static final int ACTION_SAFE_SAVE = 265;
    public static final int ACTION_SAFE_READ = 266;
    public static final int ACTION_ENCRYPT_TEXT = 267;
    public static final int ACTION_DECRYPT_TEXT = 268;
    public static final int ACTION_GENERATE_PASSWORD = 269;

    // === Time Tracking (270-274) ===
    public static final int ACTION_CLOCK_IN = 270;
    public static final int ACTION_CLOCK_OUT = 271;
    public static final int ACTION_REPORT_HOURS = 272;
    public static final int ACTION_SET_WEEKDAY_ALARM = 273;
    public static final int ACTION_STOPWATCH = 274;

    // === Media / UI (275-279) ===
    public static final int ACTION_SET_MEDIA_TRACK = 275;
    public static final int ACTION_ROTATE_ACTIONS = 276;
    public static final int ACTION_SHOW_DIALOG = 277;
    public static final int ACTION_VIBRATE_PATTERN = 278;
    public static final int ACTION_SHOW_CONFIRM_DIALOG = 279;

    public static final int ACTION_COUNT = 280;

    public static final String[] ACTION_NAMES = {
            "切換 WiFi", "切換 藍牙", "設定聲音模式", "設定音量",
            "設定亮度", "啟動 App", "發送簡訊", "開啟網址",
            "設定鬧鐘", "撥打電話", "鎖定螢幕", "開啟設定頁",
            "設定 WiFi 連線", "新增行事曆活動", "語音朗讀",
            "啟動計時器", "切換飛航模式", "切換自動旋轉",
            // Connectivity
            "切換 WiFi 熱點", "切換行動數據", "切換 NFC", "切換 GPS",
            "WiFi Tethering", "切換 VPN", "連接藍牙裝置",
            // Display
            "設定螢幕逾時", "設定亮度模式", "切換深色模式", "設定字體大小",
            // Audio
            "設定媒體音量", "設定鈴聲音量", "設定鬧鐘音量", "設定通知音量",
            "切換勿擾模式", "播放聲音", "設定鈴聲",
            // Media
            "播放音樂", "暫停音樂", "下一首", "上一首",
            "拍照", "開啟相簿",
            // Notifications
            "顯示通知", "清除所有通知", "設定 DND 模式",
            // Input
            "切換鍵盤", "輸入文字",
            // System
            "顯示提示", "設定桌布", "重新啟動 (需 Root)", "關機 (需 Root)",
            "開啟 App 抽屜", "回到主頁", "返回", "切換分割畫面",
            "開啟熱點設定", "開啟 WiFi 設定", "開啟藍牙設定", "開啟 NFC 設定", "開啟數據用量",
            // Sensors
            "切換手電筒", "震動",
            // Accounts
            "開啟聯絡人", "開啟撥號", "開啟訊息", "開啟日曆",
            "開啟相機", "開啟 Email", "開啟地圖", "開啟 Play 商店",
            // Device Control
            "切換 Dock 模式", "切換車用模式", "切換 WiFi 通話", "切換 VoLTE", "設定螢幕旋轉",
            // File Operations
            "建立檔案", "刪除檔案", "寫入檔案", "讀取檔案",
            // Connectivity Extended
            "切換 USB 網路共用", "切換藍牙網路共用", "切換自動同步", "切換 Nearby Share",
            "切換 Android Beam", "開啟 APN 設定", "開啟投放設定", "開啟 WiFi Direct", "開啟數據漫遊",
            // Display Extended
            "切換色彩反轉", "切換螢幕節電", "切換螢幕保護", "切換額外調暗",
            "切換閱讀模式", "切換手勢導覽", "切換導覽列", "設定顯示大小",
            "切換高對比文字", "切換瀏海隱藏",
            // Audio Extended
            "靜音/取消靜音", "切換單聲道音訊", "切換即時字幕", "切換助聽器",
            "切換自適應音效", "設定音訊平衡", "切換聲音增強", "切換觸覺回饋",
            "切換按鍵音", "切換觸控音效", "切換通知震動",
            // Media Extended
            "開啟 YouTube", "開啟 Spotify", "開始錄音", "停止錄音",
            "播放/暫停", "隨機播放", "循環播放",
            // Notifications Extended
            "開啟通知紀錄", "切換通知圓點", "切換鎖定螢幕通知", "切換震動模式",
            "開啟通知設定", "僅限鬧鐘", "開啟狀態列",
            // System Settings
            "開發人員選項", "輔助使用", "電池設定", "儲存空間",
            "安全性設定", "應用程式資訊", "日期與時間", "語言與輸入",
            "隱私設定", "位置設定", "應用程式權限", "多使用者設定",
            "列印設定", "預設應用程式", "電池最佳化", "省電模式",
            "記憶體設定", "音效設定", "顯示設定", "網路設定",
            // System Control
            "截圖", "最近應用程式", "固定螢幕", "鎖定模式",
            "電源選單", "切換輸入法", "切換大寫鎖定", "切換數字鎖定",
            // Share & Launch
            "分享文字", "開啟檔案管理", "開啟下載", "開啟 Chrome",
            "開啟時鐘", "開啟計算機", "複製到剪貼簿", "從剪貼簿貼上",
            "開啟天氣", "開啟相簿 (Google)",
            // Flow Control
            "等待", "執行設定檔", "結束任務", "設定變數", "變數加減",
            // Connectivity Advanced
            "切換自適應連線", "藍牙掃描", "WiFi 掃描", "NFC 讀取器模式",
            "P2P 設定", "已儲存 WiFi 網路", "藍牙配對",
            // Display Advanced
            "切換夜覽模式", "切換色彩校正", "減少動畫", "旋轉鎖定",
            "鎖定螢幕桌布", "自適應亮度",
            // Audio Advanced
            "絕對音量", "切換音訊輸出", "Dolby Atmos", "音量鍵控制媒體",
            // More System Settings
            "桌布設定", "鎖定螢幕設定", "字型設定", "勿擾設定",
            "通知設定", "手勢設定", "關於手機", "SIM 卡設定",
            "熱點與網路共用", "生物辨識設定",
            // Quick Launch Apps
            "開啟 Gmail", "開啟雲端硬碟", "開啟 Twitter", "開啟 Facebook",
             "開啟 Instagram", "開啟 LINE", "開啟 Telegram",
             // Tags / NDEF
             "標籤盤點", "標籤資訊", "標籤 UID", "標籤技術", "標籤容量",
             "讀取 NDEF", "寫入 NDEF 文字", "寫入 NDEF 網址", "格式化標籤", "鎖定標籤",
             "標籤週期", "原廠檢查", "設定標籤密碼", "移除標籤密碼", "啟用標籤保護",
             // Batch / CSV
             "批次寫入 NDEF", "批次鎖定", "批次格式化", "CSV 匯入", "CSV 匯出",
             // QR / Code
             "產生 QR 碼", "掃描 QR 碼", "QR 轉 NDEF", "NDEF 轉 QR",
             // Webhook / Network
             "Webhook GET", "Webhook POST", "HTTP 請求", "檢查網路連線", "取得對外 IP",
             // TTS / Audio
             "語音朗讀自訂", "停止朗讀", "朗讀目前時間", "朗讀目前日期",
             // Conditional
             "如果標籤存在", "如果 WiFi 連線", "如果藍牙連線", "如果時間在區間", "如果是星期幾",
             "如果變數等於", "如果變數大於", "如果變數小於",
             // Flow Control
             "否則", "結束如果", "重複迴圈", "中斷迴圈",
             // Variable
             "變數遞增", "變數遞減", "變數串接", "清除變數", "隨機變數",
             // File Operations
             "複製檔案", "移動檔案", "重新命名檔案", "列出檔案", "檢查檔案存在",
             // App Control
             "關閉應用程式", "清除應用快取", "清除應用資料", "強制停止",
             "停用應用程式", "啟用應用程式", "封鎖應用程式", "解除封鎖",
             "鎖定應用程式", "解鎖應用程式",
             // Vault
             "儲存至保險庫", "從保險庫讀取", "加密文字", "解密文字", "產生密碼",
             // Time Tracking
             "打卡上班", "打卡下班", "工時報表", "工作日鬧鐘", "碼表計時",
             // Media / UI
             "指定播放曲目", "輪播動作", "顯示對話框", "震動模式", "確認對話框"
    };

    private static TextToSpeech tts;

    public static void execute(Context context, int actionType, String param1, String param2, int intParam, boolean boolParam) {
        try {
            switch (actionType) {
                case ACTION_TOGGLE_WIFI: toggleWifi(context, boolParam); break;
                case ACTION_TOGGLE_BLUETOOTH: toggleBluetooth(context, boolParam); break;
                case ACTION_SET_SOUND_PROFILE: setSoundProfile(context, param1); break;
                case ACTION_SET_VOLUME: setVolume(context, param1, intParam); break;
                case ACTION_SET_BRIGHTNESS: setBrightness(context, intParam); break;
                case ACTION_LAUNCH_APP: launchApp(context, param1); break;
                case ACTION_SEND_SMS: sendSms(context, param1, param2); break;
                case ACTION_OPEN_URL: openUrl(context, param1); break;
                case ACTION_SET_ALARM: setAlarm(context, param1, param2); break;
                case ACTION_MAKE_CALL: makeCall(context, param1); break;
                case ACTION_LOCK_SCREEN: lockScreen(context); break;
                case ACTION_OPEN_SETTINGS: openSettings(context, param1); break;
                case ACTION_SET_WIFI_CONFIG: setWifiConfig(context, param1, param2); break;
                case ACTION_ADD_CALENDAR_EVENT: addCalendarEvent(context, param1, param2); break;
                case ACTION_TTS_SPEAK: ttsSpeak(context, param1); break;
                case ACTION_START_TIMER: startTimer(context, intParam); break;
                case ACTION_SET_AIRPLANE_MODE: setAirplaneMode(context, boolParam); break;
                case ACTION_TOGGLE_AUTO_ROTATE: toggleAutoRotate(context, boolParam); break;

                // Connectivity
                case ACTION_TOGGLE_WIFI_HOTSPOT: toggleWifiHotspot(context, boolParam); break;
                case ACTION_TOGGLE_MOBILE_DATA: toggleMobileData(context, boolParam); break;
                case ACTION_TOGGLE_NFC: openNfcSettings(context); break;
                case ACTION_TOGGLE_LOCATION: toggleLocation(context); break;
                case ACTION_SET_WIFI_TETHERING: setWifiTethering(context, boolParam); break;
                case ACTION_TOGGLE_VPN: openVpnSettings(context); break;
                case ACTION_CONNECT_BLUETOOTH_DEVICE: connectBluetoothDevice(context, param1); break;

                // Display
                case ACTION_SET_SCREEN_TIMEOUT: setScreenTimeout(context, intParam); break;
                case ACTION_SET_SCREEN_BRIGHTNESS_MODE: setBrightnessMode(context, boolParam); break;
                case ACTION_TOGGLE_DARK_MODE: toggleDarkMode(context); break;
                case ACTION_SET_FONT_SIZE: setFontSize(context, intParam); break;

                // Audio
                case ACTION_SET_MEDIA_VOLUME: setVolume(context, "media", intParam); break;
                case ACTION_SET_RING_VOLUME: setVolume(context, "ring", intParam); break;
                case ACTION_SET_ALARM_VOLUME: setVolume(context, "alarm", intParam); break;
                case ACTION_SET_NOTIFICATION_VOLUME: setVolume(context, "notification", intParam); break;
                case ACTION_TOGGLE_DND: toggleDnd(context); break;
                case ACTION_PLAY_SOUND: playSound(context, param1); break;
                case ACTION_SET_RINGTONE: setRingtone(context, param1); break;

                // Media
                case ACTION_PLAY_MUSIC: playMusic(context, param1); break;
                case ACTION_PAUSE_MUSIC: pauseMusic(context); break;
                case ACTION_NEXT_TRACK: nextTrack(context); break;
                case ACTION_PREV_TRACK: prevTrack(context); break;
                case ACTION_TAKE_PHOTO: takePhoto(context); break;
                case ACTION_OPEN_GALLERY: openGallery(context); break;

                // Notifications
                case ACTION_SHOW_NOTIFICATION: showNotification(context, param1, param2); break;
                case ACTION_CLEAR_ALL_NOTIFICATIONS: clearAllNotifications(context); break;
                case ACTION_SET_DND_MODE: setDndMode(context, intParam); break;

                // Input
                case ACTION_TOGGLE_KEYBOARD: toggleKeyboard(context); break;
                case ACTION_TYPE_TEXT: typeText(context, param1); break;

                // System
                case ACTION_SHOW_TOAST: showToast(context, param1); break;
                case ACTION_SET_WALLPAPER: setWallpaper(context, param1); break;
                case ACTION_REBOOT: requireRootToast(context, "重新啟動"); break;
                case ACTION_SHUTDOWN: requireRootToast(context, "關機"); break;
                case ACTION_OPEN_APP_DRAWER: openAppDrawer(context); break;
                case ACTION_GO_HOME: goHome(context); break;
                case ACTION_GO_BACK: goBack(context); break;
                case ACTION_TOGGLE_SPLIT_SCREEN: toggleSplitScreen(context); break;
                case ACTION_OPEN_HOTSPOT_SETTINGS: openSettings(context, "hotspot"); break;
                case ACTION_OPEN_WIFI_SETTINGS: openSettings(context, "wifi"); break;
                case ACTION_OPEN_BLUETOOTH_SETTINGS: openSettings(context, "bluetooth"); break;
                case ACTION_OPEN_NFC_SETTINGS: openSettings(context, "nfc"); break;
                case ACTION_OPEN_DATA_USAGE: openDataUsage(context); break;

                // Sensors
                case ACTION_TOGGLE_FLASHLIGHT: toggleFlashlight(context); break;
                case ACTION_VIBRATE: vibrateDevice(context, intParam); break;

                // Accounts
                case ACTION_OPEN_CONTACTS: openApp(context, "com.android.contacts"); break;
                case ACTION_OPEN_DIALER: openApp(context, "com.android.dialer"); break;
                case ACTION_OPEN_MESSAGES: openApp(context, "com.android.mms"); break;
                case ACTION_OPEN_CALENDAR: openApp(context, "com.android.calendar"); break;
                case ACTION_OPEN_CAMERA: openApp(context, "com.android.camera"); break;
                case ACTION_OPEN_EMAIL: openEmail(context); break;
                case ACTION_OPEN_MAPS: openMaps(context); break;
                case ACTION_OPEN_PLAY_STORE: openPlayStore(context); break;

                // Device Control
                case ACTION_TOGGLE_DOCK: toggleDock(context); break;
                case ACTION_TOGGLE_CAR_MODE: toggleCarMode(context); break;
                case ACTION_TOGGLE_WIFI_CALLING: toggleWifiCalling(context); break;
                case ACTION_TOGGLE_VOLTE: toggleVoLTE(context); break;
                case ACTION_SET_SCREEN_ROTATION: setScreenRotation(context, intParam); break;

                // File Operations
                case ACTION_CREATE_FILE: createFile(context, param1); break;
                case ACTION_DELETE_FILE: deleteFile(context, param1); break;
                case ACTION_WRITE_FILE: writeFile(context, param1, param2); break;
                case ACTION_READ_FILE: readFile(context, param1); break;

                // Connectivity Extended
                case ACTION_TOGGLE_USB_TETHERING: openTetherSettings(context); break;
                case ACTION_TOGGLE_BLUETOOTH_TETHERING: openTetherSettings(context); break;
                case ACTION_TOGGLE_AUTO_SYNC: toggleAutoSync(context, boolParam); break;
                case ACTION_TOGGLE_NEARBY_SHARING: openSettings(context, "nearby"); break;
                case ACTION_TOGGLE_ANDROID_BEAM: openSettings(context, "nfc"); break;
                case ACTION_OPEN_APN_SETTINGS: openSystemSettings(context, Settings.ACTION_APN_SETTINGS); break;
                case ACTION_OPEN_CAST_SETTINGS: openSystemSettings(context, Settings.ACTION_CAST_SETTINGS); break;
                case ACTION_OPEN_WIFI_P2P_SETTINGS: openSettings(context, "wifi"); break;
                case ACTION_OPEN_DATA_ROAMING: openSystemSettings(context, Settings.ACTION_DATA_ROAMING_SETTINGS); break;

                // Display Extended
                case ACTION_TOGGLE_COLOR_INVERSION: toggleColorInversion(context); break;
                case ACTION_TOGGLE_AMBIENT_DISPLAY: openSettings(context, "display"); break;
                case ACTION_TOGGLE_SCREENSAVER: openSystemSettings(context, Settings.ACTION_DREAM_SETTINGS); break;
                case ACTION_TOGGLE_EXTRA_DIM: openSettings(context, "accessibility"); break;
                case ACTION_TOGGLE_READING_MODE: openSettings(context, "display"); break;
                case ACTION_TOGGLE_GESTURE_NAV: openSystemSettings(context, "android.settings.SYSTEM_NAVIGATION_SETTINGS"); break;
                case ACTION_TOGGLE_NAV_BAR: openSystemSettings(context, "android.settings.SYSTEM_NAVIGATION_SETTINGS"); break;
                case ACTION_SET_DISPLAY_SIZE: openSettings(context, "display"); break;
                case ACTION_TOGGLE_HIGH_CONTRAST_TEXT: toggleHighContrastText(context); break;
                case ACTION_TOGGLE_DISPLAY_CUTOUT: openSystemSettings(context, "android.settings.DISPLAY_CUTOUT_EMULATION_SETTINGS"); break;

                // Audio Extended
                case ACTION_SET_VOLUME_MUTE: setVolumeMute(context, boolParam); break;
                case ACTION_TOGGLE_MONO_AUDIO: openSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS); break;
                case ACTION_TOGGLE_LIVE_CAPTION: openSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS); break;
                case ACTION_TOGGLE_HEARING_AID: openSettings(context, "bluetooth"); break;
                case ACTION_TOGGLE_ADAPTIVE_SOUND: openSettings(context, "sound"); break;
                case ACTION_SET_AUDIO_BALANCE: setAudioBalance(context, intParam); break;
                case ACTION_TOGGLE_SOUND_AMPLIFIER: openSettings(context, "accessibility"); break;
                case ACTION_TOGGLE_HAPTIC_FEEDBACK: toggleHapticFeedback(context, boolParam); break;
                case ACTION_TOGGLE_DTMF_TONE: toggleDtmfTone(context, boolParam); break;
                case ACTION_TOGGLE_SOUND_EFFECTS: toggleSoundEffects(context, boolParam); break;
                case ACTION_TOGGLE_VIBRATE_ON_NOTIF: toggleVibrateOnNotification(context, boolParam); break;

                // Media Extended
                case ACTION_OPEN_YOUTUBE: openApp(context, "com.google.android.youtube"); break;
                case ACTION_OPEN_SPOTIFY: openApp(context, "com.spotify.music"); break;
                case ACTION_RECORD_AUDIO: startRecording(context); break;
                case ACTION_STOP_RECORDING: stopRecording(context); break;
                case ACTION_TOGGLE_PLAYBACK: togglePlayback(context); break;
                case ACTION_TOGGLE_SHUFFLE: toggleShuffle(context); break;
                case ACTION_TOGGLE_REPEAT: toggleRepeat(context); break;

                // Notifications Extended
                case ACTION_OPEN_NOTIF_HISTORY: openNotificationHistory(context); break;
                case ACTION_TOGGLE_NOTIFICATION_DOT: openSystemSettings(context, Settings.ACTION_APP_NOTIFICATION_SETTINGS); break;
                case ACTION_TOGGLE_LOCKSCREEN_NOTIF: openSettings(context, "security"); break;
                case ACTION_TOGGLE_VIBRATE_MODE: setSoundProfile(context, "vibrate"); break;
                case ACTION_OPEN_NOTIF_SETTINGS: openAppNotificationSettings(context, param1); break;
                case ACTION_TOGGLE_ALARM_ONLY: setDndMode(context, 1); break;
                case ACTION_OPEN_STATUS_BAR: openSystemSettings(context, Settings.ACTION_QUICK_LAUNCH_SETTINGS); break;

                // System Settings
                case ACTION_OPEN_DEVELOPER_OPTIONS: openSystemSettings(context, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS); break;
                case ACTION_OPEN_ACCESSIBILITY: openSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS); break;
                case ACTION_OPEN_BATTERY_SETTINGS: openBatterySettings(context); break;
                case ACTION_OPEN_STORAGE_SETTINGS: openSystemSettings(context, Settings.ACTION_INTERNAL_STORAGE_SETTINGS); break;
                case ACTION_OPEN_SECURITY_SETTINGS: openSystemSettings(context, Settings.ACTION_SECURITY_SETTINGS); break;
                case ACTION_OPEN_APP_INFO: openAppInfoSettings(context, param1); break;
                case ACTION_OPEN_DATE_SETTINGS: openSystemSettings(context, Settings.ACTION_DATE_SETTINGS); break;
                case ACTION_OPEN_LANGUAGE_SETTINGS: openSystemSettings(context, Settings.ACTION_LOCALE_SETTINGS); break;
                case ACTION_OPEN_PRIVACY_SETTINGS: openPrivacySettings(context); break;
                case ACTION_OPEN_LOCATION_SETTINGS: openSystemSettings(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS); break;
                case ACTION_OPEN_APP_PERMISSIONS: openAppPermissions(context, param1); break;
                case ACTION_OPEN_USER_SETTINGS: openSystemSettings(context, "android.settings.USER_SETTINGS"); break;
                case ACTION_OPEN_PRINT_SETTINGS: openSystemSettings(context, Settings.ACTION_PRINT_SETTINGS); break;
                case ACTION_OPEN_DEFAULT_APPS: openSystemSettings(context, Settings.ACTION_APPLICATION_SETTINGS); break;
                case ACTION_OPEN_BATTERY_OPTIMIZATION: openSystemSettings(context, Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS); break;
                case ACTION_TOGGLE_POWER_SAVE: openBatterySettings(context); break;
                case ACTION_OPEN_MEMORY_SETTINGS: openSystemSettings(context, Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS); break;
                case ACTION_OPEN_SOUND_SETTINGS: openSystemSettings(context, Settings.ACTION_SOUND_SETTINGS); break;
                case ACTION_OPEN_DISPLAY_SETTINGS: openSystemSettings(context, Settings.ACTION_DISPLAY_SETTINGS); break;
                case ACTION_OPEN_NETWORK_SETTINGS: openSystemSettings(context, Settings.ACTION_NETWORK_OPERATOR_SETTINGS); break;

                // System Control
                case ACTION_SCREENSHOT: takeScreenshot(context); break;
                case ACTION_OPEN_RECENT_APPS: openRecentApps(context); break;
                case ACTION_TOGGLE_PIN_SCREEN: togglePinScreen(context); break;
                case ACTION_LOCK_DOWN: openSystemSettings(context, Settings.ACTION_SECURITY_SETTINGS); break;
                case ACTION_OPEN_POWER_MENU: openPowerMenu(context); break;
                case ACTION_SHOW_IME_PICKER: showImePicker(context); break;
                case ACTION_TOGGLE_CAPS_LOCK: toastNotAvailable(context, "切換大寫鎖定"); break;
                case ACTION_TOGGLE_NUM_LOCK: toastNotAvailable(context, "切換數字鎖定"); break;

                // Share & Launch
                case ACTION_SHARE_TEXT: shareText(context, param1, param2); break;
                case ACTION_OPEN_FILES: openApp(context, "com.android.documentsui"); break;
                case ACTION_OPEN_DOWNLOADS: openSystemSettings(context, Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS); break;
                case ACTION_OPEN_CHROME: openApp(context, "com.android.chrome"); break;
                case ACTION_OPEN_CLOCK: openApp(context, "com.android.deskclock"); break;
                case ACTION_OPEN_CALCULATOR: openApp(context, "com.android.calculator2"); break;
                case ACTION_COPY_TO_CLIPBOARD: copyToClipboard(context, param1); break;
                case ACTION_PASTE_FROM_CLIPBOARD: pasteFromClipboard(context); break;
                case ACTION_OPEN_WEATHER: openSystemSettings(context, Intent.ACTION_MAIN); break;
                case ACTION_OPEN_PHOTOS: openApp(context, "com.google.android.apps.photos"); break;

                // Flow Control
                case ACTION_WAIT: waitMs(context, intParam); break;
                case ACTION_RUN_PROFILE: runProfile(context, param1); break;
                case ACTION_EXIT_TASK: exitTask(context); break;
                case ACTION_SET_VARIABLE: setVariable(context, param1, param2); break;
                case ACTION_ADD_VARIABLE: addVariable(context, param1, param2); break;

                // Connectivity Advanced
                case ACTION_TOGGLE_ADAPTIVE_CONNECTIVITY: openSystemSettings(context, Settings.ACTION_WIFI_SETTINGS); break;
                case ACTION_TOGGLE_BLUETOOTH_SCAN: openSystemSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS); break;
                case ACTION_TOGGLE_WIFI_SCAN: openSystemSettings(context, Settings.ACTION_WIFI_SETTINGS); break;
                case ACTION_OPEN_NFC_READER_MODE: openSystemSettings(context, "android.settings.NFC_SETTINGS"); break;
                case ACTION_OPEN_P2P_SETTINGS: openSystemSettings(context, "android.settings.WIFI_DIRECT_SETTINGS"); break;
                case ACTION_OPEN_WIFI_SAVED_NETWORKS: openSystemSettings(context, Settings.ACTION_WIFI_SETTINGS); break;
                case ACTION_OPEN_BLUETOOTH_PAIRING: openSystemSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS); break;

                // Display Advanced
                case ACTION_TOGGLE_NIGHT_LIGHT: openSystemSettings(context, Settings.ACTION_DISPLAY_SETTINGS); break;
                case ACTION_TOGGLE_COLOR_CORRECTION: openSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS); break;
                case ACTION_TOGGLE_REDUCE_ANIMATION: openSystemSettings(context, Settings.ACTION_ACCESSIBILITY_SETTINGS); break;
                case ACTION_TOGGLE_SCREEN_ROTATION_LOCK: toggleAutoRotate(context, false); break;
                case ACTION_SET_WALLPAPER_LOCK: openSystemSettings(context, Settings.ACTION_SETTINGS); break;
                case ACTION_TOGGLE_ADAPTIVE_BRIGHTNESS: setBrightnessMode(context, true); break;

                // Audio Advanced
                case ACTION_TOGGLE_ABSOLUTE_VOLUME: openSystemSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS); break;
                case ACTION_SWITCH_AUDIO_OUTPUT: openSystemSettings(context, Settings.ACTION_SOUND_SETTINGS); break;
                case ACTION_TOGGLE_DOLBY_ATMOS: openSystemSettings(context, Settings.ACTION_SOUND_SETTINGS); break;
                case ACTION_TOGGLE_VOLUME_KEY_MEDIA: openSystemSettings(context, Settings.ACTION_SOUND_SETTINGS); break;

                // More System Settings
                case ACTION_OPEN_WALLPAPER_SETTINGS: setWallpaper(context, param1); break;
                case ACTION_OPEN_LOCKSCREEN_SETTINGS: openSystemSettings(context, Settings.ACTION_SECURITY_SETTINGS); break;
                case ACTION_OPEN_FONT_SETTINGS: openSystemSettings(context, Settings.ACTION_DISPLAY_SETTINGS); break;
                case ACTION_OPEN_DND_SETTINGS: openSystemSettings(context, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS); break;
                case ACTION_OPEN_NOTIF_SETTINGS_MAIN: openSystemSettings(context, Settings.ACTION_APPLICATION_SETTINGS); break;
                case ACTION_OPEN_GESTURE_SETTINGS: openSystemSettings(context, "android.settings.SYSTEM_NAVIGATION_SETTINGS"); break;
                case ACTION_OPEN_ABOUT_PHONE: openSystemSettings(context, Settings.ACTION_DEVICE_INFO_SETTINGS); break;
                case ACTION_OPEN_SIM_SETTINGS: openSystemSettings(context, Settings.ACTION_NETWORK_OPERATOR_SETTINGS); break;
                case ACTION_OPEN_HOTSPOT_AND_TETHERING: openSystemSettings(context, "android.settings.TETHER_SETTINGS"); break;
                case ACTION_OPEN_BIOMETRIC_SETTINGS: openSystemSettings(context, Settings.ACTION_SECURITY_SETTINGS); break;

                // Quick Launch Apps
                case ACTION_LAUNCH_GMAIL: openApp(context, "com.google.android.gm"); break;
                case ACTION_LAUNCH_DRIVE: openApp(context, "com.google.android.apps.docs"); break;
                case ACTION_LAUNCH_TWITTER: openApp(context, "com.twitter.android"); break;
                case ACTION_LAUNCH_FACEBOOK: openApp(context, "com.facebook.katana"); break;
                case ACTION_LAUNCH_INSTAGRAM: openApp(context, "com.instagram.android"); break;
                case ACTION_LAUNCH_LINE: openApp(context, "jp.naver.line.android"); break;
                case ACTION_LAUNCH_TELEGRAM: openApp(context, "org.telegram.messenger"); break;

                // Tags / NDEF
                case ACTION_TAG_INVENTORY: tagInventory(context); break;
                case ACTION_TAG_INFO: tagInfo(context); break;
                case ACTION_TAG_UID: tagUid(context); break;
                case ACTION_TAG_TECH: tagTech(context); break;
                case ACTION_TAG_SIZE: tagSize(context); break;
                case ACTION_READ_NDEF: readNdef(context); break;
                case ACTION_WRITE_NDEF_TEXT: writeNdefText(context, param1); break;
                case ACTION_WRITE_NDEF_URI: writeNdefUri(context, param1); break;
                case ACTION_FORMAT_TAG: formatTag(context); break;
                case ACTION_LOCK_TAG: lockTag(context); break;
                case ACTION_TAG_CYCLES: tagCycles(context); break;
                case ACTION_CHECK_ORIGINALITY: checkOriginality(context); break;
                case ACTION_SET_TAG_PASSWORD: setTagPassword(context, param1); break;
                case ACTION_REMOVE_TAG_PASSWORD: removeTagPassword(context); break;
                case ACTION_PROTECT_TAG: protectTag(context, boolParam); break;

                // Batch / CSV
                case ACTION_BATCH_WRITE: batchWriteNdef(context, param1); break;
                case ACTION_BATCH_LOCK: batchLock(context); break;
                case ACTION_BATCH_FORMAT: batchFormat(context); break;
                case ACTION_CSV_IMPORT: csvImport(context, param1); break;
                case ACTION_CSV_EXPORT: csvExport(context, param1); break;

                // QR / Code
                case ACTION_GENERATE_QR: generateQrCode(context, param1); break;
                case ACTION_SCAN_QR: scanQrCode(context); break;
                case ACTION_QR_TO_NDEF: qrToNdef(context, param1); break;
                case ACTION_NDEF_TO_QR: ndefToQr(context, param1); break;

                // Webhook / Network
                case ACTION_WEBHOOK_GET: webhookGet(context, param1); break;
                case ACTION_WEBHOOK_POST: webhookPost(context, param1, param2); break;
                case ACTION_HTTP_REQUEST: httpRequest(context, param1, param2); break;
                case ACTION_CHECK_CONNECTIVITY: checkConnectivity(context); break;
                case ACTION_GET_PUBLIC_IP: getPublicIp(context); break;

                // TTS / Audio
                case ACTION_TTS_SAY: ttsSay(context, param1); break;
                case ACTION_TTS_STOP: ttsStop(context); break;
                case ACTION_ANNOUNCE_TIME: announceTime(context); break;
                case ACTION_ANNOUNCE_DATE: announceDate(context); break;

                // Conditional
                case ACTION_IF_TAG_PRESENT: ifTagPresent(context, boolParam); break;
                case ACTION_IF_WIFI_CONNECTED: ifWifiConnected(context, boolParam); break;
                case ACTION_IF_BT_CONNECTED: ifBluetoothConnected(context, boolParam); break;
                case ACTION_IF_TIME_BETWEEN: ifTimeBetween(context, param1, param2); break;
                case ACTION_IF_DAY_OF_WEEK: ifDayOfWeek(context, param1); break;
                case ACTION_IF_VARIABLE_EQUALS: ifVariableEquals(context, param1, param2); break;
                case ACTION_IF_VARIABLE_GREATER: ifVariableGreater(context, param1, param2); break;
                case ACTION_IF_VARIABLE_LESS: ifVariableLess(context, param1, param2); break;

                // Flow Control
                case ACTION_ELSE: flowElse(context); break;
                case ACTION_ENDIF: flowEndIf(context); break;
                case ACTION_WHILE: flowWhile(context); break;
                case ACTION_BREAK: flowBreak(context); break;

                // Variable
                case ACTION_VARIABLE_INCREMENT: variableIncrement(context, param1); break;
                case ACTION_VARIABLE_DECREMENT: variableDecrement(context, param1); break;
                case ACTION_VARIABLE_CONCAT: variableConcat(context, param1, param2); break;
                case ACTION_VARIABLE_CLEAR: variableClear(context, param1); break;
                case ACTION_VARIABLE_RANDOM: variableRandom(context, param1); break;

                // File Operations
                case ACTION_COPY_FILE: copyFile(context, param1, param2); break;
                case ACTION_MOVE_FILE: moveFile(context, param1, param2); break;
                case ACTION_RENAME_FILE: renameFile(context, param1, param2); break;
                case ACTION_LIST_FILES: listFiles(context, param1); break;
                case ACTION_FILE_EXISTS: fileExists(context, param1); break;

                // App Control
                case ACTION_KILL_APP: killApp(context, param1); break;
                case ACTION_CLEAR_APP_CACHE: clearAppCache(context, param1); break;
                case ACTION_CLEAR_APP_DATA: clearAppData(context, param1); break;
                case ACTION_FORCE_STOP: forceStopApp(context, param1); break;
                case ACTION_DISABLE_APP: disableApp(context, param1); break;
                case ACTION_ENABLE_APP: enableApp(context, param1); break;
                case ACTION_BLOCK_APP: blockApp(context, param1, boolParam); break;
                case ACTION_UNBLOCK_APP: unblockApp(context, param1); break;
                case ACTION_LOCK_APP: lockApp(context, param1); break;
                case ACTION_UNLOCK_APP: unlockApp(context, param1); break;

                // Vault
                case ACTION_SAFE_SAVE: safeSave(context, param1, param2); break;
                case ACTION_SAFE_READ: safeRead(context, param1); break;
                case ACTION_ENCRYPT_TEXT: encryptText(context, param1); break;
                case ACTION_DECRYPT_TEXT: decryptText(context, param1); break;
                case ACTION_GENERATE_PASSWORD: generatePassword(context, intParam); break;

                // Time Tracking
                case ACTION_CLOCK_IN: clockIn(context); break;
                case ACTION_CLOCK_OUT: clockOut(context); break;
                case ACTION_REPORT_HOURS: reportHours(context); break;
                case ACTION_SET_WEEKDAY_ALARM: setWeekdayAlarm(context, param1, param2); break;
                case ACTION_STOPWATCH: stopwatch(context, boolParam); break;

                // Media / UI
                case ACTION_SET_MEDIA_TRACK: setMediaTrack(context, param1); break;
                case ACTION_ROTATE_ACTIONS: rotateActions(context); break;
                case ACTION_SHOW_DIALOG: showDialog(context, param1); break;
                case ACTION_VIBRATE_PATTERN: vibratePattern(context, param1); break;
                case ACTION_SHOW_CONFIRM_DIALOG: showConfirmDialog(context, param1, param2); break;
            }
        } catch (Exception e) {
            Toast.makeText(context, "執行失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void toggleWifi(Context ctx, boolean on) {
        WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null) wm.setWifiEnabled(on);
    }

    private static void toggleBluetooth(Context ctx, boolean on) {
        Intent intent = new Intent(on ?
                "android.bluetooth.adapter.action.REQUEST_ENABLE" :
                "android.bluetooth.adapter.action.REQUEST_DISABLE");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void setSoundProfile(Context ctx, String profile) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;
        switch (profile) {
            case "silent":
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
            case "vibrate":
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;
            case "normal":
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;
        }
    }

    private static void setVolume(Context ctx, String stream, int level) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;
        int streamType;
        switch (stream) {
            case "ring": streamType = AudioManager.STREAM_RING; break;
            case "media": streamType = AudioManager.STREAM_MUSIC; break;
            case "alarm": streamType = AudioManager.STREAM_ALARM; break;
            case "notification": streamType = AudioManager.STREAM_NOTIFICATION; break;
            default: streamType = AudioManager.STREAM_RING;
        }
        int max = am.getStreamMaxVolume(streamType);
        am.setStreamVolume(streamType, level * max / 100, 0);
    }

    private static void setBrightness(Context ctx, int level) {
        int lp = level * 255 / 100;
        Settings.System.putInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, lp);
    }

    private static void launchApp(Context ctx, String packageName) {
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } else {
            Toast.makeText(ctx, "未安裝 " + packageName, Toast.LENGTH_SHORT).show();
        }
    }

    private static void sendSms(Context ctx, String number, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
        intent.putExtra("sms_body", message);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void openUrl(Context ctx, String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void setAlarm(Context ctx, String time, String label) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        String[] parts = time.split(":");
        if (parts.length == 2) {
            intent.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(parts[0]));
            intent.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(parts[1]));
        }
        if (label != null && !label.isEmpty()) intent.putExtra(AlarmClock.EXTRA_MESSAGE, label);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void makeCall(Context ctx, String number) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void lockScreen(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        ctx.sendBroadcast(intent);
    }

    private static void setWifiConfig(Context ctx, String ssid, String password) {
        Intent intent = new Intent("android.settings.WIFI_IP_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void addCalendarEvent(Context ctx, String title, String description) {
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, System.currentTimeMillis() + 60000);
        values.put(CalendarContract.Events.DTEND, System.currentTimeMillis() + 3600000);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Taipei");
        Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis() + 60000);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, System.currentTimeMillis() + 3600000);
        intent.putExtra(CalendarContract.Events.TITLE, title);
        intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void ttsSpeak(Context ctx, String text) {
        if (tts == null) {
            tts = new TextToSpeech(ctx, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.TRADITIONAL_CHINESE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    else
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            else
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private static void startTimer(Context ctx, int seconds) {
        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER);
        intent.putExtra(AlarmClock.EXTRA_LENGTH, seconds);
        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void setAirplaneMode(Context ctx, boolean on) {
        Settings.Global.putInt(ctx.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, on ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", on);
        ctx.sendBroadcast(intent);
    }

    private static void toggleAutoRotate(Context ctx, boolean on) {
        Settings.System.putInt(ctx.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, on ? 1 : 0);
    }

    // === Connectivity ===

    private static void toggleWifiHotspot(Context ctx, boolean on) {
        Intent intent = new Intent();
        intent.setAction("android.settings.WIFI_AP_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
        Toast.makeText(ctx, "請手動設定 Wi-Fi 熱點", Toast.LENGTH_SHORT).show();
    }

    private static void toggleMobileData(Context ctx, boolean on) {
        Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void toggleLocation(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void setWifiTethering(Context ctx, boolean on) {
        Intent intent = new Intent("android.settings.TETHER_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void openVpnSettings(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_VPN_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void connectBluetoothDevice(Context ctx, String mac) {
        if (mac == null || mac.isEmpty()) {
            Toast.makeText(ctx, "請輸入藍牙 MAC 位址", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
        Toast.makeText(ctx, "請在藍牙設定中連接 " + mac, Toast.LENGTH_SHORT).show();
    }

    private static void openNfcSettings(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    // === Display ===

    private static void setScreenTimeout(Context ctx, int seconds) {
        int millis = Math.max(15000, seconds * 1000);
        Settings.System.putInt(ctx.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, millis);
        Toast.makeText(ctx, "螢幕逾時設為 " + seconds + " 秒", Toast.LENGTH_SHORT).show();
    }

    private static void setBrightnessMode(Context ctx, boolean auto) {
        Settings.System.putInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                auto ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    private static void toggleDarkMode(Context ctx) {
        Toast.makeText(ctx, "深色模式需手動設定 (系統設定顯示)", Toast.LENGTH_LONG).show();
    }

    private static void setFontSize(Context ctx, int percent) {
        float scale = Math.max(0.5f, Math.min(2.0f, percent / 100.0f));
        android.content.res.Configuration config = ctx.getResources().getConfiguration();
        config.fontScale = scale;
        android.content.res.Resources res = ctx.getResources();
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    // === Audio ===

    private static void toggleDnd(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void playSound(Context ctx, String param) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (param != null && param.startsWith("content://") || (param != null && param.startsWith("file://"))) {
            intent.setDataAndType(Uri.parse(param), "audio/*");
            ctx.startActivity(intent);
        } else {
            Toast.makeText(ctx, "播放: " + (param != null ? param : "未指定"), Toast.LENGTH_SHORT).show();
        }
    }

    private static void setRingtone(Context ctx, String uri) {
        if (uri != null && !uri.isEmpty()) {
            Intent intent = new Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
    }

    // === Media ===

    private static void playMusic(Context ctx, String title) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setType("audio/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (title != null && !title.isEmpty()) {
            Toast.makeText(ctx, "播放: " + title, Toast.LENGTH_SHORT).show();
        }
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "請先安裝音樂播放器", Toast.LENGTH_SHORT).show();
        }
    }

    private static void pauseMusic(Context ctx) {
        Intent intent = new Intent("com.android.music.musicservicecommand.pause");
        ctx.sendBroadcast(intent);
    }

    private static void nextTrack(Context ctx) {
        Intent intent = new Intent("com.android.music.musicservicecommand.next");
        ctx.sendBroadcast(intent);
    }

    private static void prevTrack(Context ctx) {
        Intent intent = new Intent("com.android.music.musicservicecommand.previous");
        ctx.sendBroadcast(intent);
    }

    private static void takePhoto(Context ctx) {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "無法開啟相機", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openGallery(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "無法開啟相簿", Toast.LENGTH_SHORT).show();
        }
    }

    // === Notifications ===

    private static void showNotification(Context ctx, String title, String text) {
        if (title == null) title = "HeliRFID";
        if (text == null) text = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "task_notifications";
            android.app.NotificationChannel ch = new android.app.NotificationChannel(channelId,
                    "Task Notifications", android.app.NotificationManager.IMPORTANCE_DEFAULT);
            android.app.NotificationManager nm = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
            android.app.Notification.Builder builder = new android.app.Notification.Builder(ctx, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true);
            if (nm != null) nm.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private static void clearAllNotifications(Context ctx) {
        android.app.NotificationManager nm = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancelAll();
    }

    private static void setDndMode(Context ctx, int mode) {
        android.app.NotificationManager nm = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && nm.isNotificationPolicyAccessGranted()) {
            int interruptionFilter;
            switch (mode) {
                case 0: interruptionFilter = android.app.NotificationManager.INTERRUPTION_FILTER_NONE; break;
                case 1: interruptionFilter = android.app.NotificationManager.INTERRUPTION_FILTER_ALARMS; break;
                case 2: interruptionFilter = android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY; break;
                default: interruptionFilter = android.app.NotificationManager.INTERRUPTION_FILTER_ALL; break;
            }
            nm.setInterruptionFilter(interruptionFilter);
        } else {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        }
    }

    // === Input ===

    private static void toggleKeyboard(Context ctx) {
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(0, 0);
    }

    private static void typeText(Context ctx, String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(ctx, "請輸入要輸入的文字", Toast.LENGTH_SHORT).show();
            return;
        }
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("HeliRFID", text));
            Toast.makeText(ctx, "已複製到剪貼簿: " + text, Toast.LENGTH_SHORT).show();
        }
    }

    // === System ===

    private static void showToast(Context ctx, String text) {
        if (text != null && !text.isEmpty())
            Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
    }

    private static void setWallpaper(Context ctx, String uri) {
        Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void requireRootToast(Context ctx, String feature) {
        Toast.makeText(ctx, feature + " 功能需要 Root 權限\n此為展示，未實際執行", Toast.LENGTH_LONG).show();
    }

    private static void openAppDrawer(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void goHome(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void goBack(Context ctx) {
        ctx.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private static void toggleSplitScreen(Context ctx) {
        Intent intent = new Intent("android.intent.action.SPLIT_SCREEN");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "裝置不支援分割畫面", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openDataUsage(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    // === Sensors ===

    private static void toggleFlashlight(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ctx.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_FLASH)) {
                android.hardware.camera2.CameraManager cm = (android.hardware.camera2.CameraManager)
                        ctx.getSystemService(Context.CAMERA_SERVICE);
                if (cm != null) {
                    try {
                        String cameraId = cm.getCameraIdList()[0];
                        cm.setTorchMode(cameraId, true);
                        Toast.makeText(ctx, "手電筒已開啟 (請手動關閉)", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(ctx, "無法控制手電筒", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(ctx, "裝置無閃光燈", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctx, "Android 6+ 才支援", Toast.LENGTH_SHORT).show();
        }
    }

    private static void vibrateDevice(Context ctx, int durationMs) {
        if (durationMs <= 0) durationMs = 200;
        android.os.Vibrator v = (android.os.Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(android.os.VibrationEffect.createOneShot(durationMs, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(durationMs);
            }
        }
    }

    // === Accounts ===

    private static void openApp(Context ctx, String packageName) {
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } else {
            try {
                Intent fallback = new Intent();
                switch (packageName) {
                    case "com.android.contacts": fallback.setAction(Intent.ACTION_VIEW).setData(android.provider.ContactsContract.Contacts.CONTENT_URI); break;
                    case "com.android.dialer": fallback.setAction(Intent.ACTION_DIAL); break;
                    case "com.android.mms": fallback.setAction(Intent.ACTION_SENDTO).setData(Uri.parse("smsto:")); break;
                    case "com.android.calendar": fallback.setAction(Intent.ACTION_VIEW).setData(android.provider.CalendarContract.CONTENT_URI); break;
                    case "com.android.camera": fallback.setAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); break;
                    default: return;
                }
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(fallback);
            } catch (Exception e) {
                Toast.makeText(ctx, "無法開啟", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void openEmail(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "未安裝郵件應用", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openMaps(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "未安裝地圖應用", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openPlayStore(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "無法開啟 Play 商店", Toast.LENGTH_SHORT).show();
        }
    }

    // === Device Control ===

    private static void toggleDock(Context ctx) {
        Intent intent = new Intent("android.intent.action.DOCK_EVENT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.sendBroadcast(intent);
        Toast.makeText(ctx, "切換 Dock 模式 (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void toggleCarMode(Context ctx) {
        Intent intent = new Intent("android.intent.action.CAR_MODE");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.sendBroadcast(intent);
        Toast.makeText(ctx, "切換車用模式 (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void toggleWifiCalling(Context ctx) {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    private static void toggleVoLTE(Context ctx) {
        Intent intent = new Intent("android.settings.IMS_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "無法開啟 VoLTE 設定", Toast.LENGTH_SHORT).show();
        }
    }

    private static void setScreenRotation(Context ctx, int mode) {
        int rotation;
        switch (mode) {
            case 1: rotation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; break;
            case 2: rotation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; break;
            default: rotation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED; break;
        }
        if (ctx instanceof android.app.Activity) {
            ((android.app.Activity) ctx).setRequestedOrientation(rotation);
        } else {
            Settings.System.putInt(ctx.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION,
                    mode == 0 ? 1 : 0);
        }
    }

    // === File Operations ===

    private static void createFile(Context ctx, String path) {
        if (path != null && !path.isEmpty()) {
            try {
                java.io.File file = new java.io.File(path);
                if (file.createNewFile()) {
                    Toast.makeText(ctx, "已建立: " + path, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "檔案已存在: " + path, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(ctx, "建立失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctx, "請輸入檔案路徑", Toast.LENGTH_SHORT).show();
        }
    }

    private static void deleteFile(Context ctx, String path) {
        if (path != null && !path.isEmpty()) {
            java.io.File file = new java.io.File(path);
            if (file.delete()) {
                Toast.makeText(ctx, "已刪除: " + path, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, "刪除失敗或檔案不存在", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctx, "請輸入檔案路徑", Toast.LENGTH_SHORT).show();
        }
    }

    private static void writeFile(Context ctx, String path, String content) {
        if (path == null || path.isEmpty()) { Toast.makeText(ctx, "請輸入檔案路徑", Toast.LENGTH_SHORT).show(); return; }
        try {
            java.io.FileWriter fw = new java.io.FileWriter(path);
            fw.write(content != null ? content : "");
            fw.close();
            Toast.makeText(ctx, "已寫入: " + path, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "寫入失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void readFile(Context ctx, String path) {
        if (path == null || path.isEmpty()) { Toast.makeText(ctx, "請輸入檔案路徑", Toast.LENGTH_SHORT).show(); return; }
        try {
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
            br.close();
            String content = sb.toString();
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                    ctx.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("HeliRFID", content));
            }
            Toast.makeText(ctx, "已讀取 " + content.length() + " 字元，已複製到剪貼簿", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "讀取失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // === Settings action with hotspot support ===

    private static void openSettings(Context ctx, String page) {
        Intent intent = new Intent();
        switch (page) {
            case "wifi": intent.setAction(Settings.ACTION_WIFI_SETTINGS); break;
            case "bluetooth": intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS); break;
            case "sound": intent.setAction(Settings.ACTION_SOUND_SETTINGS); break;
            case "display": intent.setAction(Settings.ACTION_DISPLAY_SETTINGS); break;
            case "nfc": intent.setAction(Settings.ACTION_NFC_SETTINGS); break;
            case "hotspot": intent.setAction("android.settings.WIFI_AP_SETTINGS"); break;
            case "accessibility": intent.setAction(Settings.ACTION_ACCESSIBILITY_SETTINGS); break;
            case "security": intent.setAction(Settings.ACTION_SECURITY_SETTINGS); break;
            case "nearby": intent.setAction("com.android.settings.NEARBY_SHARE_SETTINGS"); break;
            case "tether": intent.setAction("android.settings.TETHER_SETTINGS"); break;
            default: intent.setAction(Settings.ACTION_SETTINGS);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }

    // === New Connectivity Extended Methods ===

    private static void openTetherSettings(Context ctx) {
        Intent intent = new Intent("android.settings.TETHER_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
        Toast.makeText(ctx, "請在網路共用設定中操作", Toast.LENGTH_SHORT).show();
    }

    private static void toggleAutoSync(Context ctx, boolean on) {
        try {
            ContentResolver.setMasterSyncAutomatically(on);
            Toast.makeText(ctx, on ? "自動同步已開啟" : "自動同步已關閉", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "無法切換自動同步", Toast.LENGTH_SHORT).show();
        }
    }

    // === New Display Extended Methods ===

    private static void toggleColorInversion(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                boolean current = Settings.Secure.getInt(ctx.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, 0) == 1;
                Settings.Secure.putInt(ctx.getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED, current ? 0 : 1);
                Toast.makeText(ctx, current ? "色彩反轉已關閉" : "色彩反轉已開啟", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                openSystemSettings(ctx, Settings.ACTION_ACCESSIBILITY_SETTINGS);
            }
        } else {
            openSettings(ctx, "accessibility");
        }
    }

    private static void toggleHighContrastText(Context ctx) {
        try {
            openSystemSettings(ctx, Settings.ACTION_ACCESSIBILITY_SETTINGS);
            Toast.makeText(ctx, "請在輔助使用中設定高對比文字", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            openSettings(ctx, "accessibility");
        }
    }

    // === New Audio Extended Methods ===

    private static void setVolumeMute(Context ctx, boolean mute) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return;
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                mute ? AudioManager.ADJUST_MUTE : AudioManager.ADJUST_UNMUTE, 0);
        Toast.makeText(ctx, mute ? "媒體音訊已靜音" : "媒體音訊已取消靜音", Toast.LENGTH_SHORT).show();
    }

    private static void setAudioBalance(Context ctx, int balance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                float balValue = Math.max(-1.0f, Math.min(1.0f, balance / 100.0f));
                Settings.System.putFloat(ctx.getContentResolver(), "master_balance", balValue);
                Toast.makeText(ctx, "音訊平衡設為 " + balance, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(ctx, "裝置不支援設定音訊平衡", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctx, "Android 9+ 才支援", Toast.LENGTH_SHORT).show();
        }
    }

    private static void toggleHapticFeedback(Context ctx, boolean on) {
        try {
            Settings.System.putInt(ctx.getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, on ? 1 : 0);
            Toast.makeText(ctx, on ? "觸覺回饋已開啟" : "觸覺回饋已關閉", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            toastNotAvailable(ctx, "切換觸覺回饋");
        }
    }

    private static void toggleDtmfTone(Context ctx, boolean on) {
        try {
            Settings.System.putInt(ctx.getContentResolver(),
                    Settings.System.DTMF_TONE_WHEN_DIALING, on ? 1 : 0);
            Toast.makeText(ctx, on ? "按鍵音已開啟" : "按鍵音已關閉", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            toastNotAvailable(ctx, "切換按鍵音");
        }
    }

    private static void toggleSoundEffects(Context ctx, boolean on) {
        try {
            Settings.System.putInt(ctx.getContentResolver(),
                    Settings.System.SOUND_EFFECTS_ENABLED, on ? 1 : 0);
            Toast.makeText(ctx, on ? "觸控音效已開啟" : "觸控音效已關閉", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            toastNotAvailable(ctx, "切換觸控音效");
        }
    }

    private static void toggleVibrateOnNotification(Context ctx, boolean on) {
        try {
            Settings.System.putInt(ctx.getContentResolver(),
                    "vibrate_when_ringing", on ? 1 : 0);
            Toast.makeText(ctx, on ? "通知震動已開啟" : "通知震動已關閉", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            toastNotAvailable(ctx, "切換通知震動");
        }
    }

    // === New Media Extended Methods ===

    private static void startRecording(Context ctx) {
        Intent intent = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } else {
            Toast.makeText(ctx, "未安裝錄音應用", Toast.LENGTH_SHORT).show();
        }
    }

    private static void stopRecording(Context ctx) {
        Intent intent = new Intent("com.android.music.musicservicecommand.stop");
        ctx.sendBroadcast(intent);
        Toast.makeText(ctx, "已停止錄音", Toast.LENGTH_SHORT).show();
    }

    private static void togglePlayback(Context ctx) {
        Intent intent = new Intent("com.android.music.musicservicecommand.toggle");
        intent.putExtra("command", "toggle");
        ctx.sendBroadcast(intent);
    }

    private static void toggleShuffle(Context ctx) {
        Intent intent = new Intent("com.android.music.musicservicecommand.toggle");
        intent.putExtra("command", "shuffle");
        ctx.sendBroadcast(intent);
    }

    private static void toggleRepeat(Context ctx) {
        Intent intent = new Intent("com.android.music.musicservicecommand.toggle");
        intent.putExtra("command", "repeat");
        ctx.sendBroadcast(intent);
    }

    // === New Notifications Extended Methods ===

    private static void openNotificationHistory(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent("android.settings.NOTIFICATION_HISTORY");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }
        openSystemSettings(ctx, Settings.ACTION_APPLICATION_SETTINGS);
    }

    private static void openAppNotificationSettings(Context ctx, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Toast.makeText(ctx, "請輸入套件名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
            intent.putExtra("android.provider.extra.APP_UID", android.os.Process.myUid());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { ctx.startActivity(intent); return; } catch (Exception ignored) {}
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "無法開啟通知設定", Toast.LENGTH_SHORT).show();
        }
    }

    // === New System Settings Methods ===

    private static void openSystemSettings(Context ctx, String action) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent fallback = new Intent(Settings.ACTION_SETTINGS);
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(fallback);
            } catch (Exception e2) {
                Toast.makeText(ctx, "無法開啟設定", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void openBatterySettings(Context ctx) {
        try {
            Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } catch (Exception e) {
            openSystemSettings(ctx, Settings.ACTION_BATTERY_SAVER_SETTINGS);
        }
    }

    private static void openAppInfoSettings(Context ctx, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Toast.makeText(ctx, "請輸入套件名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "無法開啟應用程式資訊", Toast.LENGTH_SHORT).show();
        }
    }

    private static void openPrivacySettings(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_PRIVACY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }
        openSystemSettings(ctx, Settings.ACTION_SECURITY_SETTINGS);
    }

    private static void openAppPermissions(Context ctx, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Toast.makeText(ctx, "請輸入套件名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + packageName));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
                return;
            } catch (Exception ignored) {}
        }
        Toast.makeText(ctx, "無法開啟權限設定", Toast.LENGTH_SHORT).show();
    }

    // === New System Control Methods ===

    private static void takeScreenshot(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent("android.intent.action.SCREENSHOT");
                ctx.sendBroadcast(intent);
                Toast.makeText(ctx, "請使用實體按鍵截圖", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(ctx, "無法自動截圖，請使用電源+音量下鍵", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(ctx, "請使用電源+音量下鍵截圖", Toast.LENGTH_LONG).show();
        }
    }

    private static void openRecentApps(Context ctx) {
        Intent intent = new Intent("android.intent.action.RECENT_APPS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { ctx.startActivity(intent); } catch (Exception e) {
            Toast.makeText(ctx, "無法開啟最近應用", Toast.LENGTH_SHORT).show();
        }
    }

    private static void togglePinScreen(Context ctx) {
        Toast.makeText(ctx, "請在安全設定中開啟螢幕固定", Toast.LENGTH_LONG).show();
    }

    private static void openPowerMenu(Context ctx) {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        ctx.sendBroadcast(intent);
        Toast.makeText(ctx, "請長按電源鍵開啟電源選單", Toast.LENGTH_LONG).show();
    }

    private static void showImePicker(Context ctx) {
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showInputMethodPicker();
    }

    // === New Share & Launch Methods ===

    private static void shareText(Context ctx, String text, String title) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(ctx, "請輸入要分享的文字", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        if (title != null && !title.isEmpty()) intent.putExtra(Intent.EXTRA_SUBJECT, title);
        Intent chooser = Intent.createChooser(intent, "分享到");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(chooser);
    }

    private static void copyToClipboard(Context ctx, String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(ctx, "請輸入要複製的文字", Toast.LENGTH_SHORT).show();
            return;
        }
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("HeliRFID", text));
            Toast.makeText(ctx, "已複製到剪貼簿", Toast.LENGTH_SHORT).show();
        }
    }

    private static void pasteFromClipboard(Context ctx) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            android.content.ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    Toast.makeText(ctx, "剪貼簿內容: " + text, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        Toast.makeText(ctx, "剪貼簿為空", Toast.LENGTH_SHORT).show();
    }

    // === Flow Control Methods ===

    private static void waitMs(Context ctx, int ms) {
        if (ms <= 0) ms = 1000;
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private static void runProfile(Context ctx, String profileName) {
        if (profileName == null || profileName.isEmpty()) {
            Toast.makeText(ctx, "請輸入設定檔名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            java.io.File dir = new java.io.File(ctx.getFilesDir(), "task_profiles");
            java.io.File f = new java.io.File(dir, profileName + ".json");
            if (!f.exists()) {
                Toast.makeText(ctx, "設定檔不存在: " + profileName, Toast.LENGTH_SHORT).show();
                return;
            }
            TaskProfile p = TaskProfile.load(ctx, profileName);
            if (p != null && p.actions != null) {
                for (TaskProfile.TaskAction a : p.actions)
                    execute(ctx, a.type, a.param1, a.param2, a.intParam, a.boolParam);
            }
        } catch (Exception e) {
            Toast.makeText(ctx, "執行設定檔失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void exitTask(Context ctx) {
        Toast.makeText(ctx, "任務已終止", Toast.LENGTH_SHORT).show();
    }

    private static void setVariable(Context ctx, String name, String value) {
        Toast.makeText(ctx, "變數 " + name + " = " + value + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void addVariable(Context ctx, String name, String value) {
        Toast.makeText(ctx, "變數 " + name + " += " + value + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    // === Utility ===

    private static void toastNotAvailable(Context ctx, String feature) {
        Toast.makeText(ctx, feature + " 功能在此裝置上無法使用", Toast.LENGTH_SHORT).show();
    }

    // ======== Tags / NDEF (200-214) ========

    private static void tagInventory(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            showToast(ctx, "標籤盤點: UID=" + Converter.hex(tag.getId()) + " (請在專屬功能中查看詳情)");
        } else {
            showToast(ctx, "標籤盤點：請先在標籤盤點 Activity 中使用");
        }
    }

    private static void tagInfo(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            showToast(ctx, "標籤資訊: UID=" + Converter.hex(tag.getId()) + " (請在標籤資訊 Activity 中查看)");
        } else {
            showToast(ctx, "標籤資訊：請先在標籤資訊 Activity 中使用");
        }
    }

    private static void tagUid(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            showToast(ctx, "UID: " + Converter.hex(tag.getId()));
        } else {
            showToast(ctx, "無標籤資訊");
        }
    }

    private static void tagTech(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            String[] techs = tag.getTechList();
            StringBuilder sb = new StringBuilder();
            for (String t : techs) {
                String name = t.substring(t.lastIndexOf('.') + 1);
                sb.append(name).append(" ");
            }
            showToast(ctx, "技術: " + sb.toString().trim());
        } else {
            showToast(ctx, "無標籤技術資訊");
        }
    }

    private static void tagSize(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            int size = 0;
            String type = "未知";
            for (String t : tag.getTechList()) {
                if (t.contains("MifareClassic")) {
                    try {
                        android.nfc.tech.MifareClassic mfc = android.nfc.tech.MifareClassic.get(tag);
                        size = mfc.getSize();
                        type = "MIFARE " + size + " bytes";
                    } catch (Exception ignored) {}
                } else if (t.contains("MifareUltralight")) {
                    type = "NTAG/Ultralight";
                    size = 1024;
                }
            }
            showToast(ctx, type + " (UID: " + Converter.hex(tag.getId()) + ")");
        } else {
            showToast(ctx, "無標籤容量資訊");
        }
    }

    private static void readNdef(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            try {
                android.nfc.tech.Ndef ndef = android.nfc.tech.Ndef.get(tag);
                if (ndef != null) {
                    ndef.connect();
                    android.nfc.NdefMessage msg = ndef.getNdefMessage();
                    ndef.close();
                    if (msg != null) {
                        String text = new String(msg.getRecords()[0].getPayload(), "UTF-8");
                        showToast(ctx, "NDEF: " + text.substring(0, Math.min(50, text.length())) + "...");
                    } else {
                        showToast(ctx, "標籤無 NDEF 資料");
                    }
                } else {
                    showToast(ctx, "此標籤不支援 NDEF");
                }
            } catch (Exception e) {
                showToast(ctx, "讀取 NDEF 失敗: " + e.getMessage());
            }
        } else {
            showToast(ctx, "請先在 NDEF 編輯器中感應標籤");
        }
    }

    private static void writeNdefText(Context ctx, String text) {
        if (text == null || text.isEmpty()) {
            showToast(ctx, "請先設定 NDEF 文字內容");
            return;
        }
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            try {
                android.nfc.tech.Ndef ndef = android.nfc.tech.Ndef.get(tag);
                if (ndef != null) {
                    ndef.connect();
                    ndef.writeNdefMessage(new android.nfc.NdefMessage(
                        android.nfc.NdefRecord.createTextRecord("zh-TW", text)));
                    ndef.close();
                    showToast(ctx, "NDEF 文字寫入成功");
                } else {
                    showToast(ctx, "此標籤不支援 NDEF 寫入");
                }
            } catch (Exception e) {
                showToast(ctx, "NDEF 寫入失敗: " + e.getMessage());
            }
        } else {
            showToast(ctx, "請先在標籤寫入 Activity 中感應標籤");
        }
    }

    private static void writeNdefUri(Context ctx, String uri) {
        if (uri == null || uri.isEmpty()) {
            showToast(ctx, "請先設定 NDEF URI 內容");
            return;
        }
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            try {
                android.nfc.tech.Ndef ndef = android.nfc.tech.Ndef.get(tag);
                if (ndef != null) {
                    ndef.connect();
                    ndef.writeNdefMessage(new android.nfc.NdefMessage(
                        android.nfc.NdefRecord.createUri(uri)));
                    ndef.close();
                    showToast(ctx, "NDEF URI 寫入成功");
                } else {
                    showToast(ctx, "此標籤不支援 NDEF 寫入");
                }
            } catch (Exception e) {
                showToast(ctx, "NDEF 寫入失敗: " + e.getMessage());
            }
        } else {
            showToast(ctx, "請先在標籤寫入 Activity 中感應標籤");
        }
    }

    private static void formatTag(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            try {
                android.nfc.tech.NdefFormatable nf = android.nfc.tech.NdefFormatable.get(tag);
                if (nf != null) {
                    nf.connect();
                    nf.format(new android.nfc.NdefMessage(android.nfc.NdefRecord.createTextRecord("zh-TW", "")));
                    nf.close();
                    showToast(ctx, "標籤格式化成功");
                } else {
                    showToast(ctx, "此標籤不支援格式化 (請在格式化 Activity 中操作)");
                }
            } catch (Exception e) {
                showToast(ctx, "格式化失敗: " + e.getMessage());
            }
        } else {
            showToast(ctx, "請先在格式化 Activity 中感應標籤");
        }
    }

    private static void lockTag(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            try {
                android.nfc.tech.Ndef ndef = android.nfc.tech.Ndef.get(tag);
                if (ndef != null) {
                    ndef.connect();
                    ndef.makeReadOnly();
                    ndef.close();
                    showToast(ctx, "標籤已鎖定 (唯讀)");
                } else {
                    showToast(ctx, "此標籤不支援 NDEF 鎖定");
                }
            } catch (Exception e) {
                showToast(ctx, "鎖定失敗: " + e.getMessage());
            }
        } else {
            showToast(ctx, "請先在鎖定標籤 Activity 中操作");
        }
    }

    private static void tagCycles(Context ctx) {
        Tag tag = NFCReader.getLastTag();
        if (tag != null) {
            showToast(ctx, "標籤週期: UID=" + Converter.hex(tag.getId()) + " (請在標籤週期 Activity 中查看)");
        } else {
            showToast(ctx, "標籤週期：請在標籤週期 Activity 中使用");
        }
    }

    private static void checkOriginality(Context ctx) {
        showToast(ctx, "原廠檢查：請在原廠簽章檢查 Activity 中操作");
    }

    private static void setTagPassword(Context ctx, String pwd) {
        if (pwd != null && !pwd.isEmpty()) {
            showToast(ctx, "設定密碼: " + pwd + " (請在 NTAG 密碼 Activity 中操作)");
        } else {
            showToast(ctx, "請在 NTAG 密碼設定 Activity 中操作");
        }
    }

    private static void removeTagPassword(Context ctx) {
        showToast(ctx, "移除密碼：請在 NTAG 密碼設定 Activity 中操作");
    }

    private static void protectTag(Context ctx, boolean on) {
        showToast(ctx, (on ? "啟用" : "停用") + "保護 (請在標籤鎖定 Activity 中操作)");
    }

    // ======== Batch / CSV (215-219) ========

    private static void batchWriteNdef(Context ctx, String data) {
        showToast(ctx, "批次寫入 NDEF：請在批次寫入 Activity 中使用 CSV 匯入功能");
    }

    private static void batchLock(Context ctx) {
        showToast(ctx, "批次鎖定：請在批次寫入 Activity 中操作");
    }

    private static void batchFormat(Context ctx) {
        showToast(ctx, "批次格式化：請在標籤格式化 Activity 中操作");
    }

    private static void csvImport(Context ctx, String path) {
        showToast(ctx, "CSV 匯入：請在報告匯出 Activity 中使用");
    }

    private static void csvExport(Context ctx, String path) {
        showToast(ctx, "CSV 匯出：請在報告匯出 Activity 中使用");
    }

    // ======== QR / Code (220-223) ========

    private static void generateQrCode(Context ctx, String data) {
        if (data == null || data.isEmpty()) {
            Toast.makeText(ctx, "請輸入 QR 碼內容", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, data);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(Intent.createChooser(intent, "分享 QR 碼內容"));
    }

    private static void scanQrCode(Context ctx) {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
            ctx.startActivity(intent);
        } else {
            Toast.makeText(ctx, "請安裝 QR Code 掃描器", Toast.LENGTH_SHORT).show();
        }
    }

    private static void qrToNdef(Context ctx, String data) {
        toastNotAvailable(ctx, "QR 轉 NDEF");
    }

    private static void ndefToQr(Context ctx, String data) {
        toastNotAvailable(ctx, "NDEF 轉 QR");
    }

    // ======== Webhook / Network (224-228) ========

    private static void webhookGet(Context ctx, String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(ctx, "請輸入 Webhook URL", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                String msg = "Webhook GET " + code;
                showToast(ctx, msg);
            } catch (Exception e) {
                showToast(ctx, "Webhook 失敗: " + e.getMessage());
            }
        }).start();
    }

    private static void webhookPost(Context ctx, String url, String body) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(ctx, "請輸入 Webhook URL", Toast.LENGTH_SHORT).show();
            return;
        }
        String data = body != null ? body : "";
        new Thread(() -> {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.getOutputStream().write(data.getBytes("UTF-8"));
                int code = conn.getResponseCode();
                String msg = "Webhook POST " + code;
                showToast(ctx, msg);
            } catch (Exception e) {
                showToast(ctx, "Webhook 失敗: " + e.getMessage());
            }
        }).start();
    }

    private static void httpRequest(Context ctx, String url, String method) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(ctx, "請輸入 URL", Toast.LENGTH_SHORT).show();
            return;
        }
        String m = (method != null && !method.isEmpty()) ? method.toUpperCase() : "GET";
        new Thread(() -> {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                conn.setRequestMethod(m);
                conn.setConnectTimeout(5000);
                int code = conn.getResponseCode();
                showToast(ctx, "HTTP " + m + " → " + code);
            } catch (Exception e) {
                showToast(ctx, "HTTP 請求失敗: " + e.getMessage());
            }
        }).start();
    }

    private static void checkConnectivity(Context ctx) {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            android.net.NetworkInfo active = cm.getActiveNetworkInfo();
            boolean connected = active != null && active.isConnected();
            Toast.makeText(ctx, connected ? "網路已連線" : "網路未連線", Toast.LENGTH_SHORT).show();
        }
    }

    private static void getPublicIp(Context ctx) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://api.ipify.org");
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream()));
                String ip = br.readLine();
                showToast(ctx, "對外 IP: " + ip);
            } catch (Exception e) {
                showToast(ctx, "取得 IP 失敗");
            }
        }).start();
    }

    // ======== TTS / Audio (229-232) ========

    private static void ttsSay(Context ctx, String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(ctx, "請輸入朗讀內容", Toast.LENGTH_SHORT).show();
            return;
        }
        ttsSpeak(ctx, text);
    }

    private static void ttsStop(Context ctx) {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }

    private static void announceTime(Context ctx) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.TAIWAN);
        String time = sdf.format(new java.util.Date());
        ttsSpeak(ctx, "現在時間 " + time);
    }

    private static void announceDate(Context ctx) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy年M月d日 EEEE", java.util.Locale.TAIWAN);
        String date = sdf.format(new java.util.Date());
        ttsSpeak(ctx, "今天是 " + date);
    }

    // ======== Conditional / Flow Control (233-244) ========

    private static void ifTagPresent(Context ctx, boolean expected) {
        Toast.makeText(ctx, "條件判斷：標籤存在 = " + expected + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void ifWifiConnected(Context ctx, boolean expected) {
        WifiManager wm = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean isConnected = wm != null && wm.isWifiEnabled();
        Toast.makeText(ctx, "WiFi 連線狀態 = " + isConnected + " (期望 " + expected + ")", Toast.LENGTH_SHORT).show();
    }

    private static void ifBluetoothConnected(Context ctx, boolean expected) {
        android.bluetooth.BluetoothAdapter ba = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
        boolean isConnected = ba != null && ba.isEnabled();
        Toast.makeText(ctx, "藍牙連線狀態 = " + isConnected + " (期望 " + expected + ")", Toast.LENGTH_SHORT).show();
    }

    private static void ifTimeBetween(Context ctx, String start, String end) {
        Toast.makeText(ctx, "時間區間條件: " + start + " ~ " + end + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void ifDayOfWeek(Context ctx, String day) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int today = cal.get(java.util.Calendar.DAY_OF_WEEK);
        String[] days = {"", "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        String todayStr = today >= 1 && today <= 7 ? days[today] : "";
        boolean match = day != null && todayStr.contains(day);
        Toast.makeText(ctx, "今天是 " + todayStr + " (條件: " + day + ") = " + match, Toast.LENGTH_SHORT).show();
    }

    private static void ifVariableEquals(Context ctx, String name, String value) {
        Toast.makeText(ctx, "變數 " + name + " == " + value + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void ifVariableGreater(Context ctx, String name, String value) {
        Toast.makeText(ctx, "變數 " + name + " > " + value + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void ifVariableLess(Context ctx, String name, String value) {
        Toast.makeText(ctx, "變數 " + name + " < " + value + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void flowElse(Context ctx) {
        Toast.makeText(ctx, "否則分支 (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void flowEndIf(Context ctx) {
        Toast.makeText(ctx, "結束條件 (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void flowWhile(Context ctx) {
        Toast.makeText(ctx, "重複迴圈 (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void flowBreak(Context ctx) {
        Toast.makeText(ctx, "中斷迴圈 (僅展示)", Toast.LENGTH_SHORT).show();
    }

    // ======== Variable Operations (245-249) ========

    private static void variableIncrement(Context ctx, String name) {
        Toast.makeText(ctx, "變數 " + name + "++ (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void variableDecrement(Context ctx, String name) {
        Toast.makeText(ctx, "變數 " + name + "-- (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void variableConcat(Context ctx, String name, String value) {
        Toast.makeText(ctx, "變數 " + name + " += " + value + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void variableClear(Context ctx, String name) {
        Toast.makeText(ctx, "清除變數 " + name + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void variableRandom(Context ctx, String name) {
        int rand = (int)(Math.random() * 10000);
        Toast.makeText(ctx, "隨機變數 " + name + " = " + rand + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    // ======== File Operations (250-254) ========

    private static void copyFile(Context ctx, String src, String dst) {
        if (src == null || dst == null) {
            Toast.makeText(ctx, "請輸入來源與目標路徑", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            java.io.File srcFile = new java.io.File(src);
            java.io.File dstFile = new java.io.File(dst);
            java.nio.file.Files.copy(srcFile.toPath(), dstFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Toast.makeText(ctx, "已複製: " + dst, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "複製失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void moveFile(Context ctx, String src, String dst) {
        if (src == null || dst == null) {
            Toast.makeText(ctx, "請輸入來源與目標路徑", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            java.io.File srcFile = new java.io.File(src);
            java.io.File dstFile = new java.io.File(dst);
            java.nio.file.Files.move(srcFile.toPath(), dstFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Toast.makeText(ctx, "已移動: " + dst, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "移動失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void renameFile(Context ctx, String oldPath, String newName) {
        if (oldPath == null || newName == null) {
            Toast.makeText(ctx, "請輸入路徑與新名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            java.io.File file = new java.io.File(oldPath);
            java.io.File newFile = new java.io.File(file.getParent(), newName);
            if (file.renameTo(newFile)) {
                Toast.makeText(ctx, "已重新命名: " + newName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, "重新命名失敗", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(ctx, "重新命名失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void listFiles(Context ctx, String path) {
        if (path == null || path.isEmpty()) {
            path = ctx.getFilesDir().getAbsolutePath();
        }
        try {
            java.io.File dir = new java.io.File(path);
            java.io.File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                Toast.makeText(ctx, "目錄為空", Toast.LENGTH_SHORT).show();
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (java.io.File f : files) {
                sb.append(f.getName()).append(" ");
            }
            Toast.makeText(ctx, sb.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "讀取目錄失敗", Toast.LENGTH_SHORT).show();
        }
    }

    private static void fileExists(Context ctx, String path) {
        if (path == null || path.isEmpty()) {
            Toast.makeText(ctx, "請輸入檔案路徑", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean exists = new java.io.File(path).exists();
        Toast.makeText(ctx, "檔案 " + (exists ? "存在" : "不存在"), Toast.LENGTH_SHORT).show();
    }

    // ======== App Control (255-264) ========

    private static void killApp(Context ctx, String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            Toast.makeText(ctx, "請輸入套件名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            android.app.ActivityManager am = (android.app.ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.killBackgroundProcesses(pkg);
                Toast.makeText(ctx, "已關閉: " + pkg, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        toastNotAvailable(ctx, "關閉應用程式");
    }

    private static void clearAppCache(Context ctx, String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            showToast(ctx, "請輸入套件名稱");
            return;
        }
        try {
            android.os.Process sendSignal;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                android.os.storage.StorageManager sm = (android.os.storage.StorageManager) ctx.getSystemService(Context.STORAGE_SERVICE);
                if (sm != null) {
                    // Use app cache clearing via package manager
                    ctx.getPackageManager().getPackageInfo(pkg, 0);
                    showToast(ctx, "清除快取: " + pkg + " (需系統權限，請手動清除)");
                    openSystemSettings(ctx, Settings.ACTION_APPLICATION_DETAILS_SETTINGS + ":" + pkg);
                    return;
                }
            }
            showToast(ctx, "清除快取: " + pkg + " (請在應用程式資訊中操作)");
        } catch (Exception e) {
            showToast(ctx, "清除快取失敗: " + e.getMessage());
        }
    }

    private static void clearAppData(Context ctx, String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            showToast(ctx, "請輸入套件名稱");
            return;
        }
        showToast(ctx, "清除資料: " + pkg + " (需 Root 權限，請在應用程式資訊中操作)");
        openAppInfoSettings(ctx, pkg);
    }

    private static void forceStopApp(Context ctx, String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            showToast(ctx, "請輸入套件名稱");
            return;
        }
        android.app.ActivityManager am = (android.app.ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            try {
                java.lang.reflect.Method method = am.getClass().getMethod("forceStopPackage", String.class);
                method.invoke(am, pkg);
                showToast(ctx, "已強制停止: " + pkg);
                return;
            } catch (Exception e) {
                showToast(ctx, "強制停止需要 Root 或系統權限");
            }
        }
        killApp(ctx, pkg);
    }

    private static void disableApp(Context ctx, String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            Toast.makeText(ctx, "請輸入套件名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ctx.getPackageManager().setApplicationEnabledSetting(pkg,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            Toast.makeText(ctx, "已停用: " + pkg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "停用失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void enableApp(Context ctx, String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            Toast.makeText(ctx, "請輸入套件名稱", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ctx.getPackageManager().setApplicationEnabledSetting(pkg,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            Toast.makeText(ctx, "已啟用: " + pkg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "啟用失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void blockApp(Context ctx, String pkg, boolean block) {
        if (block) {
            disableApp(ctx, pkg);
        } else {
            enableApp(ctx, pkg);
        }
    }

    private static void lockApp(Context ctx, String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            showToast(ctx, "請輸入套件名稱");
            return;
        }
        showToast(ctx, "鎖定: " + pkg + " (請使用 App 封鎖功能)");
    }

    private static void unblockApp(Context ctx, String pkg) {
        enableApp(ctx, pkg);
    }

    private static void unlockApp(Context ctx, String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            showToast(ctx, "請輸入套件名稱");
            return;
        }
        enableApp(ctx, pkg);
    }

    // ======== Vault / Encryption (265-269) ========

    private static void safeSave(Context ctx, String key, String value) {
        ctx.getSharedPreferences("nfc_vault", Context.MODE_PRIVATE).edit().putString(key, value).apply();
        Toast.makeText(ctx, "已儲存至保險庫", Toast.LENGTH_SHORT).show();
    }

    private static void safeRead(Context ctx, String key) {
        String value = ctx.getSharedPreferences("nfc_vault", Context.MODE_PRIVATE).getString(key, null);
        if (value != null) {
            Toast.makeText(ctx, "保險庫 [" + key + "] = " + value, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ctx, "保險庫無此項目: " + key, Toast.LENGTH_SHORT).show();
        }
    }

    private static void encryptText(Context ctx, String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(ctx, "請輸入要加密的文字", Toast.LENGTH_SHORT).show();
            return;
        }
        String encoded = android.util.Base64.encodeToString(text.getBytes(), android.util.Base64.DEFAULT);
        Toast.makeText(ctx, "加密結果: " + encoded.trim(), Toast.LENGTH_LONG).show();
    }

    private static void decryptText(Context ctx, String text) {
        if (text == null || text.isEmpty()) {
            Toast.makeText(ctx, "請輸入要解密的 Base64", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] decoded = android.util.Base64.decode(text, android.util.Base64.DEFAULT);
            Toast.makeText(ctx, "解密結果: " + new String(decoded, "UTF-8"), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ctx, "解密失敗: Base64 格式錯誤", Toast.LENGTH_SHORT).show();
        }
    }

    private static void generatePassword(Context ctx, int length) {
        if (length <= 0) length = 12;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        StringBuilder sb = new StringBuilder();
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        copyToClipboard(ctx, sb.toString());
        Toast.makeText(ctx, "已產生密碼並複製: " + sb.toString(), Toast.LENGTH_LONG).show();
    }

    // ======== Time Tracking (270-274) ========

    private static void clockIn(Context ctx) {
        String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.TAIWAN).format(new java.util.Date());
        ctx.getSharedPreferences("nfc_timetrack", Context.MODE_PRIVATE).edit().putString("last_clock_in", time).apply();
        // Append to CSV log
        NfcBackgroundService.saveLogToCsv(ctx, "打卡上班", time);
        Toast.makeText(ctx, "已打卡上班: " + time, Toast.LENGTH_SHORT).show();
    }

    private static void clockOut(Context ctx) {
        String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.TAIWAN).format(new java.util.Date());
        ctx.getSharedPreferences("nfc_timetrack", Context.MODE_PRIVATE).edit().putString("last_clock_out", time).apply();
        NfcBackgroundService.saveLogToCsv(ctx, "打卡下班", time);
        Toast.makeText(ctx, "已打卡下班: " + time, Toast.LENGTH_SHORT).show();
    }

    private static void reportHours(Context ctx) {
        String in = ctx.getSharedPreferences("nfc_timetrack", Context.MODE_PRIVATE).getString("last_clock_in", "無紀錄");
        String out = ctx.getSharedPreferences("nfc_timetrack", Context.MODE_PRIVATE).getString("last_clock_out", "無紀錄");
        Toast.makeText(ctx, "上班: " + in + " 下班: " + out, Toast.LENGTH_LONG).show();
    }

    private static void setWeekdayAlarm(Context ctx, String time, String label) {
        if (time == null || time.isEmpty()) {
            Toast.makeText(ctx, "請輸入鬧鐘時間 (HH:mm)", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        try {
            String[] parts = time.split(":");
            intent.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(parts[0]));
            intent.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(parts[1]));
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, label != null ? label : "工作日鬧鐘");
            intent.putExtra(AlarmClock.EXTRA_DAYS, new java.util.ArrayList<Integer>() {{
                add(java.util.Calendar.MONDAY);
                add(java.util.Calendar.TUESDAY);
                add(java.util.Calendar.WEDNESDAY);
                add(java.util.Calendar.THURSDAY);
                add(java.util.Calendar.FRIDAY);
            }});
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(ctx, "設定鬧鐘失敗", Toast.LENGTH_SHORT).show();
        }
    }

    private static void stopwatch(Context ctx, boolean start) {
        Toast.makeText(ctx, start ? "碼表開始 (僅展示)" : "碼表停止 (僅展示)", Toast.LENGTH_SHORT).show();
    }

    // ======== Media / UI (275-279) ========

    private static void setMediaTrack(Context ctx, String track) {
        Toast.makeText(ctx, "指定播放: " + track + " (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void rotateActions(Context ctx) {
        Toast.makeText(ctx, "輪播動作 (僅展示)", Toast.LENGTH_SHORT).show();
    }

    private static void showDialog(Context ctx, String message) {
        Toast.makeText(ctx, "對話框: " + message, Toast.LENGTH_SHORT).show();
    }

    private static void vibratePattern(Context ctx, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            vibrateDevice(ctx, 200);
            return;
        }
        try {
            String[] parts = pattern.split(",");
            long[] timings = new long[parts.length];
            for (int i = 0; i < parts.length; i++) {
                timings[i] = Long.parseLong(parts[i].trim());
            }
            android.os.Vibrator v = (android.os.Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null && v.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    v.vibrate(android.os.VibrationEffect.createWaveform(timings, -1));
                } else {
                    v.vibrate(timings, -1);
                }
            }
        } catch (Exception e) {
            vibrateDevice(ctx, 200);
        }
    }

    private static void showConfirmDialog(Context ctx, String title, String message) {
        Toast.makeText(ctx, "確認: " + title + " - " + message, Toast.LENGTH_LONG).show();
    }
}
