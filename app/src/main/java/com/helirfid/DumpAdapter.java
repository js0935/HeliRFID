/*
 * HeliRFID - 智慧門禁管理系統
 * 禾秝軟體開發團隊 / 代碼：洪俊士 / 版本：4.0.1
 */
package com.helirfid;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DumpAdapter extends RecyclerView.Adapter<DumpAdapter.DumpViewHolder> {

    private List<DumpItem> dumpList;

    public DumpAdapter(List<DumpItem> dumpList) {
        this.dumpList = dumpList;
    }

    @NonNull
    @Override
    public DumpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dump_item, parent, false);
        return new DumpViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DumpViewHolder holder, int position) {
        DumpItem item = dumpList.get(position);

        if (item.getSector() == -1) {
            holder.txtSector.setText("Error");
            holder.txtBlock.setText("-");
            holder.txtData.setText(item.getDescription());
            holder.txtDesc.setText("-");
            holder.txtAscii.setText("");
            CardView card = (CardView) holder.itemView;
            card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            holder.txtSector.setText("S" + item.getSector());
            holder.txtBlock.setText("B" + item.getBlock());
            holder.txtData.setText(item.getData());
            holder.txtDesc.setText(item.getDescription());
            String ascii = MifareUtils.hexToAscii7Bit(item.getData());
            holder.txtAscii.setText(ascii);

            CardView card = (CardView) holder.itemView;
            int sector = item.getSector();
            int block = item.getBlock();
            if (sector == 0 && block == 0) {
                card.setCardBackgroundColor(Color.parseColor("#BBDEFB"));
            } else if ((block + 1) % 4 == 0) {
                card.setCardBackgroundColor(Color.parseColor("#E0E0E0"));
            } else {
                card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return dumpList.size();
    }

    static class DumpViewHolder extends RecyclerView.ViewHolder {
        TextView txtSector, txtBlock, txtData, txtDesc, txtAscii;

        public DumpViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSector = itemView.findViewById(R.id.txtSector);
            txtBlock = itemView.findViewById(R.id.txtBlock);
            txtData = itemView.findViewById(R.id.txtData);
            txtDesc = itemView.findViewById(R.id.txtDesc);
            txtAscii = itemView.findViewById(R.id.txtAscii);
        }
    }
}
