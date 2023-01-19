package com.test.medicalpanel2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.Interface.IRecyclerItemSelectedListener;
import com.test.medicalpanel2.Model.DataSlot;
import com.test.medicalpanel2.Model.Event.EnableNextButton;
import com.test.medicalpanel2.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class DataSlotAdapter extends RecyclerView.Adapter<DataSlotAdapter.MyViewHolder> {

    Context context;
    List<DataSlot> dataSlotList;
    List<CardView> cardViewList;

    public DataSlotAdapter(Context context) {
        this.context = context;
        this.dataSlotList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    public DataSlotAdapter(Context context, List<DataSlot> dataSlotList) {
        this.context = context;
        this.dataSlotList = dataSlotList;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_data_slot, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int i) {
        holder.txt_data_slot.setText(new StringBuilder(Common.convertDataSlotToString(i)).toString());
        if (dataSlotList.size() == 0) //if all positions is available - show list
        {
            holder.card_data_slot.setEnabled(true);

            holder.card_data_slot.setCardBackgroundColor(context.getColor(android.R.color.white));
            holder.txt_data_slot_description.setText("Available");
            holder.txt_data_slot_description.setTextColor(context.getColor(android.R.color.black));
            holder.txt_data_slot.setTextColor(context.getColor(android.R.color.black));
        } else //if position is already full
        {
            for (DataSlot slotValue : dataSlotList) {
                //Loop all dates from server and set different color
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == i) //If slot == position
                {
                    holder.card_data_slot.setEnabled(false);
                    holder.card_data_slot.setTag(Common.DISABLE_TAG);
                    holder.card_data_slot.setCardBackgroundColor(context.getColor(android.R.color.darker_gray));
                    holder.txt_data_slot_description.setText("Not available");
                    holder.txt_data_slot_description.setTextColor(context.getColor(android.R.color.white));
                    holder.txt_data_slot.setTextColor(context.getColor(android.R.color.white));
                }
            }
        }

        //add all card to list
        if (!cardViewList.contains(holder.card_data_slot))
            cardViewList.add(holder.card_data_slot);

        //checking is slot available

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, final int position) {
                //loop card in List
                for (CardView cardView : cardViewList) {

                    if (cardView.getTag() == null) //available card will be change
                        cardView.setCardBackgroundColor(context.getColor(android.R.color.white));
                }
                //selected card color
                holder.card_data_slot.setCardBackgroundColor(context.getColor(android.R.color.holo_blue_light));

                EventBus.getDefault().postSticky(new EnableNextButton(3, position));
            }
        });
    }


    @Override
    public int getItemCount() {
        return Common.DATA_SLOT_TOTAL;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_data_slot, txt_data_slot_description;
        CardView card_data_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_data_slot = (CardView) itemView.findViewById(R.id.card_data_slot);
            txt_data_slot = (TextView) itemView.findViewById(R.id.txt_data_slot);
            txt_data_slot_description = (TextView) itemView.findViewById(R.id.txt_data_slot_description);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}