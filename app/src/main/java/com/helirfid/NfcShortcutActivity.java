package com.helirfid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class NfcShortcutActivity extends Activity {

    private LinearLayout layoutProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_shortcut);

        TextView txtHeader = findViewById(R.id.txtShortcutHeader);
        layoutProfiles = findViewById(R.id.layoutShortcutProfiles);

        loadProfiles();
    }

    private void loadProfiles() {
        layoutProfiles.removeAllViews();
        List<TaskProfile> profiles = TaskProfile.listAll(this);

        if (profiles.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("無已儲存的任務設定檔。\n請先至「任務自動化」建立設定檔。");
            tv.setPadding(16, 16, 16, 16);
            tv.setTextSize(14);
            layoutProfiles.addView(tv);
            return;
        }

        for (final TaskProfile p : profiles) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(8, 8, 8, 8);
            row.setBackgroundResource(android.R.drawable.list_selector_background);

            TextView name = new TextView(this);
            String label = (p.tagName != null && !p.tagName.isEmpty()) ? p.tagName : "UID: " + p.uid;
            name.setText(label);
            name.setTextSize(16);
            name.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView info = new TextView(this);
            info.setText(p.getActionSummary() + " (" + p.actions.size() + " 動作)");
            info.setTextSize(12);
            info.setTextColor(0xFF888888);

            Button btn = new Button(this);
            btn.setText("建立桌面捷徑");
            btn.setAllCaps(false);
            btn.setOnClickListener(v -> createShortcut(p));

            row.addView(name);
            row.addView(info);
            row.addView(btn);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFCCCCCC);

            layoutProfiles.addView(row);
            layoutProfiles.addView(divider);
        }
    }

    private void createShortcut(TaskProfile profile) {
        String label = (profile.tagName != null && !profile.tagName.isEmpty()) ? profile.tagName : "HeliRFID 任務";

        Intent shortcutIntent = new Intent(this, ShortcutExecuteActivity.class);
        shortcutIntent.putExtra("profile_uid", profile.uid);
        shortcutIntent.setAction(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager sm = getSystemService(ShortcutManager.class);
            if (sm != null && sm.isRequestPinShortcutSupported()) {
                ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "shortcut_" + profile.uid)
                        .setShortLabel(label)
                        .setLongLabel(label)
                        .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                        .setIntent(shortcutIntent)
                        .build();
                sm.requestPinShortcut(shortcut, null);
                Toast.makeText(this, "已請求建立桌面捷徑", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher));
        sendBroadcast(intent);
        Toast.makeText(this, "已發送建立捷徑請求", Toast.LENGTH_SHORT).show();
    }
}
