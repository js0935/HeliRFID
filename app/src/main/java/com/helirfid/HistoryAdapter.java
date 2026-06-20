/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {

    private List<HistoryEntry> entries;
    private LayoutInflater inflater;

    public HistoryAdapter(LayoutInflater inflater, List<HistoryEntry> entries) {
        this.inflater = inflater;
        this.entries = entries;
    }

    public void update(List<HistoryEntry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public Object getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.history_item, parent, false);
        }

        HistoryEntry entry = entries.get(position);

        TextView txtCard = convertView.findViewById(R.id.txtHistoryCard);
        TextView txtCard8 = convertView.findViewById(R.id.txtHistoryCard8);
        TextView txtUid = convertView.findViewById(R.id.txtHistoryUid);
        TextView txtTime = convertView.findViewById(R.id.txtHistoryTime);

        txtCard.setText(entry.getCard10());
        txtCard8.setText("8碼: " + entry.getCard8());

        String uid = entry.getUid();
        if (uid != null && !uid.isEmpty()) {
            txtUid.setText("UID: " + uid);
        } else {
            txtUid.setText("");
        }

        txtTime.setText(entry.getFormattedTime());

        return convertView;
    }
}
