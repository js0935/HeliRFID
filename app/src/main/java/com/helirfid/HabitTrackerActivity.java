package com.helirfid;

import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

public class HabitTrackerActivity extends BaseNfcActivity {

    TextView txtResult, txtTodayCount, txtWeeklyCount;
    EditText editHabitName, editDailyTarget;
    Button btnSetHabit, btnResetToday, btnClear;

    SharedPreferences prefs;
    String currentHabit = "運動";
    int dailyTarget = 3;

    static final String PREFS_NAME = "habit_tracker";
    static final String KEY_HABIT = "habit_name";
    static final String KEY_TARGET = "daily_target";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_tracker);

        txtResult = findViewById(R.id.txtHabitResult);
        txtTodayCount = findViewById(R.id.txtTodayCount);
        txtWeeklyCount = findViewById(R.id.txtWeeklyCount);
        editHabitName = findViewById(R.id.editHabitName);
        editDailyTarget = findViewById(R.id.editDailyTarget);
        btnSetHabit = findViewById(R.id.btnSetHabit);
        btnResetToday = findViewById(R.id.btnResetToday);
        btnClear = findViewById(R.id.btnClearHabit);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSettings();

        btnSetHabit.setOnClickListener(v -> {
            String name = editHabitName.getText().toString().trim();
            String targetStr = editDailyTarget.getText().toString().trim();
            if (!name.isEmpty()) {
                currentHabit = name;
                prefs.edit().putString(KEY_HABIT, name).apply();
            }
            if (!targetStr.isEmpty()) {
                try {
                    dailyTarget = Integer.parseInt(targetStr);
                    prefs.edit().putInt(KEY_TARGET, dailyTarget).apply();
                } catch (NumberFormatException e) {
                    txtResult.setText("請輸入有效的數字目標");
                    return;
                }
            }
            updateDisplay();
            txtResult.setText("✅ 習慣設定已更新\n習慣: " + currentHabit + "\n每日目標: " + dailyTarget);
        });

        btnResetToday.setOnClickListener(v -> {
            resetTodayCount();
            txtResult.setText("✅ 今日計數已重置");
        });

        btnClear.setOnClickListener(v -> txtResult.setText(""));

        txtResult.setText("歡迎使用 NFC 習慣追蹤器！\n請將 NFC 標籤靠近手機來記錄習慣\n\n目前習慣: " + currentHabit + "\n每日目標: " + dailyTarget + " 次");
        updateDisplay();
    }

    void loadSettings() {
        currentHabit = prefs.getString(KEY_HABIT, "運動");
        dailyTarget = prefs.getInt(KEY_TARGET, 3);
        editHabitName.setText(currentHabit);
        editDailyTarget.setText(String.valueOf(dailyTarget));
    }

    String getTodayKey() {
        Calendar cal = Calendar.getInstance();
        return "count_" + cal.get(Calendar.YEAR) + "_"
                + (cal.get(Calendar.MONTH) + 1) + "_"
                + cal.get(Calendar.DAY_OF_MONTH) + "_" + currentHabit;
    }

    int getTodayCount() {
        return prefs.getInt(getTodayKey(), 0);
    }

    void incrementCount() {
        String key = getTodayKey();
        int count = prefs.getInt(key, 0) + 1;
        prefs.edit().putInt(key, count).apply();
    }

    void resetTodayCount() {
        prefs.edit().putInt(getTodayKey(), 0).apply();
        updateDisplay();
    }

    int getWeeklyCount() {
        Calendar cal = Calendar.getInstance();
        int total = 0;
        for (int i = 0; i < 7; i++) {
            String key = "count_" + cal.get(Calendar.YEAR) + "_"
                    + (cal.get(Calendar.MONTH) + 1) + "_"
                    + cal.get(Calendar.DAY_OF_MONTH) + "_" + currentHabit;
            total += prefs.getInt(key, 0);
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        return total;
    }

    void updateDisplay() {
        int today = getTodayCount();
        txtTodayCount.setText("📊 今日進度: " + today + " / " + dailyTarget
                + " (" + (dailyTarget > 0 ? (today * 100 / dailyTarget) : 0) + "%)");
        txtWeeklyCount.setText("📈 本週總計: " + getWeeklyCount() + " 次");
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) handleTag(tag);
        }
    }

    private void handleTag(Tag tag) {
        incrementCount();
        int today = getTodayCount();

        StringBuilder sb = new StringBuilder();
        sb.append("✅ NFC 標籤感應成功！\n\n");
        sb.append("習慣: ").append(currentHabit).append("\n");
        sb.append("今日第 ").append(today).append(" 次記錄\n");
        sb.append("目標: ").append(today).append(" / ").append(dailyTarget);

        int pct = dailyTarget > 0 ? (today * 100 / dailyTarget) : 0;
        if (pct >= 100) {
            sb.append("\n\n🎉 太棒了！今日目標已達成！");
        } else if (pct >= 75) {
            sb.append("\n\n💪 快達成目標了，加油！");
        } else if (pct >= 50) {
            sb.append("\n\n👍 繼續保持！");
        } else if (pct >= 25) {
            sb.append("\n\n📝 不錯的開始！");
        } else {
            sb.append("\n\n🚀 開始行動！");
        }

        sb.append("\n\n標籤 UID: ").append(Converter.hex(tag.getId()));

        txtResult.setText(sb.toString());
        updateDisplay();
        vibrate();
    }
}
