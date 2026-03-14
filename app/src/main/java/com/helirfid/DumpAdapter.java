package com.helirfid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        } else {
            holder.txtSector.setText("S" + item.getSector());
            holder.txtBlock.setText("B" + item.getBlock());
            holder.txtData.setText(item.getData());
            holder.txtDesc.setText(item.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return dumpList.size();
    }

    static class DumpViewHolder extends RecyclerView.ViewHolder {
        TextView txtSector, txtBlock, txtData, txtDesc;

        public DumpViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSector = itemView.findViewById(R.id.txtSector);
            txtBlock = itemView.findViewById(R.id.txtBlock);
            txtData = itemView.findViewById(R.id.txtData);
            txtDesc = itemView.findViewById(R.id.txtDesc);
        }
    }
}
