package com.test.medicalpanel2.Adapter;

import com.test.medicalpanel2.Common.Common;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.test.medicalpanel2.Interface.IRecyclerItemSelectedListener;
import com.test.medicalpanel2.Model.Clinic;
import com.test.medicalpanel2.Model.Event.EnableNextButton;
import com.test.medicalpanel2.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ClinicAdapter extends RecyclerView.Adapter<ClinicAdapter.MyViewHolder> {

    Context context;
    List<Clinic> clinicsList;
    List<CardView> cardViewList;

    public ClinicAdapter(Context context, List<Clinic> clinicList) {
        this.context = context;
        this.clinicsList = clinicList;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_clinic, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_clinic_name.setText(clinicsList.get(position).getName());
        holder.txt_clinic_address.setText(clinicsList.get(position).getAddress());

        if (!cardViewList.contains(holder.card_clinic))
            cardViewList.add(holder.card_clinic);

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                //set white BG for all card not be selected
                for (CardView cardView:cardViewList)
                    cardView.setCardBackgroundColor(context.getColor(android.R.color.white)); //moze spowodowac blad

                //set selected BG for only selected item
                holder.card_clinic.setCardBackgroundColor(context.getColor(android.R.color.holo_blue_dark));

                EventBus.getDefault().postSticky(new EnableNextButton(1, clinicsList.get(pos)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return clinicsList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_clinic_name, txt_clinic_address;
        CardView card_clinic;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            card_clinic = (CardView)itemView.findViewById(R.id.card_clinic);
            txt_clinic_address = (TextView) itemView.findViewById(R.id.txt_clinic_address);
            txt_clinic_name = (TextView) itemView.findViewById(R.id.txt_clinic_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}
