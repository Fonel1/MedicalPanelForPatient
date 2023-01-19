package com.test.medicalpanel2.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.HistoryActivity;
import com.test.medicalpanel2.Interface.RecyclerViewInterface;
import com.test.medicalpanel2.Model.AppointmentInformation;
import com.test.medicalpanel2.R;
import com.test.medicalpanel2.ShowSummaryOfTheVisit;

import org.checkerframework.checker.units.qual.A;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;

    Context context;
    List<AppointmentInformation> appointmentInformationList;

    public HistoryAdapter(Context context, List<AppointmentInformation> appointmentInformationList,
                          RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.appointmentInformationList = appointmentInformationList;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_history, parent, false);
        return new MyViewHolder(itemView, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txt_appointment_data.setText(appointmentInformationList.get(position).getTime());
        holder.txt_appointment_doctor_text.setText(appointmentInformationList.get(position).getDoctorName());
        holder.txt_appointment_data_text.setText(appointmentInformationList.get(position).getTime());
        holder.txt_clinic_address.setText(appointmentInformationList.get(position).getClinicAddress());
        holder.txt_clinic_name.setText(appointmentInformationList.get(position).getClinicName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppointmentInformation appointmentInformation = appointmentInformationList.get(position);
                Intent intent = new Intent(context, ShowSummaryOfTheVisit.class);
                intent.putExtra("summaryText", appointmentInformation.getSummaryText());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentInformationList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        Unbinder unbinder;

        @BindView(R.id.txt_clinic_name)
        TextView txt_clinic_name;
        @BindView(R.id.txt_clinic_address)
        TextView txt_clinic_address;
        @BindView(R.id.txt_appointment_data_text)
        TextView txt_appointment_data_text;
        @BindView(R.id.txt_appointment_doctor_text)
        TextView txt_appointment_doctor_text;
        @BindView(R.id.txt_appointment_data)
        TextView txt_appointment_data;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            
        }
    }

}
