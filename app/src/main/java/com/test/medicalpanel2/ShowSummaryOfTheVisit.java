package com.test.medicalpanel2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ShowSummaryOfTheVisit extends AppCompatActivity {

    Unbinder unbinder;

    @BindView(R.id.txt_summaryVisit)
    TextView txt_summaryVisit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_summary_of_the_visit);

        unbinder = ButterKnife.bind(this);

        Intent intent = getIntent();

        String summaryText = intent.getStringExtra("summaryText");

        txt_summaryVisit.setText(summaryText);
    }
}