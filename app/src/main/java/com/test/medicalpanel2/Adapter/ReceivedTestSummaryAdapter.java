package com.test.medicalpanel2.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.medicalpanel2.Model.SentTestInformation;
import com.test.medicalpanel2.R;
import com.test.medicalpanel2.ShowTestSummary;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ReceivedTestSummaryAdapter extends RecyclerView.Adapter<ReceivedTestSummaryAdapter.MyViewHolder> {

    Context context;
    List<SentTestInformation> sentTestInformationList;


    public ReceivedTestSummaryAdapter(Context context, List<SentTestInformation> sentTestInformationList) {
        this.context = context;
        this.sentTestInformationList = sentTestInformationList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_received_test_summary, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.txt_doctor_name.setText(sentTestInformationList.get(position).getDoctorName());
        holder.txt_test_name.setText(sentTestInformationList.get(position).getTestName());
        holder.btn_showSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SentTestInformation testInformation = sentTestInformationList.get(position);

                Intent intent = new Intent(context, ShowTestSummary.class);
                intent.putExtra("photo", testInformation.getPhoto());
                intent.putExtra("summaryTxt", testInformation.getSummaryTxt());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return sentTestInformationList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        Unbinder unbinder;

        @BindView(R.id.txt_test_name)
        TextView txt_test_name;
        @BindView(R.id.txt_doctor_name)
        TextView txt_doctor_name;
        @BindView(R.id.btn_showSummary)
        Button btn_showSummary;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}