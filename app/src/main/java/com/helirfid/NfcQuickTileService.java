/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.3.1
 */
package com.helirfid;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class NfcQuickTileService extends TileService {

    private NfcAdapter nfcAdapter;
    private boolean readerModeActive;

    @Override
    public void onCreate() {
        super.onCreate();
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setLabel("NFC Reader");
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Tile tile = getQsTile();
        if (tile != null) {
            tile.setLabel("NFC Reader");
            tile.setState(readerModeActive ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        if (nfcAdapter == null) return;

        readerModeActive = !readerModeActive;

        // TileService cannot use enableReaderMode (requires Activity); use foreground dispatch in ToolsActivity instead

        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(readerModeActive ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }
}
