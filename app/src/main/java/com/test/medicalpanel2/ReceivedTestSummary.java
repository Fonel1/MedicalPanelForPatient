package com.test.medicalpanel2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.medicalpanel2.Adapter.HistoryAdapter;
import com.test.medicalpanel2.Adapter.ReceivedTestSummaryAdapter;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.Model.Event.ReceivedTestSummaryEvent;
import com.test.medicalpanel2.Model.Event.UserAppointmentLoadEvent;
import com.test.medicalpanel2.Model.SentTestInformation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReceivedTestSummary extends AppCompatActivity {

    @BindView(R.id.recycler_receivedTestSummary)
    RecyclerView recycler_receivedTestSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_test_summary);

        ButterKnife.bind(this);

        initView();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            receivedSummaryTest();
        }

    }

    private void receivedSummaryTest() {
        CollectionReference receivedTestSummary = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentUser.getEmail())
                .collection("SendTestResults");

        receivedTestSummary
                .whereEqualTo("done", true)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        EventBus.getDefault().postSticky(new ReceivedTestSummaryEvent(false, e.getMessage()));
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    List<SentTestInformation> informationList = new ArrayList<>();
                    for (DocumentSnapshot receivedTestSnapShot:task.getResult())
                    {
                        SentTestInformation information = receivedTestSnapShot.toObject(SentTestInformation.class);
                        informationList.add(information);
                    }
                    EventBus.getDefault().postSticky(new ReceivedTestSummaryEvent(true, informationList));
                }
            }
        });
    }

    private void initView() {
        recycler_receivedTestSummary.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_receivedTestSummary.setLayoutManager(layoutManager);
        recycler_receivedTestSummary.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void displayData(ReceivedTestSummaryEvent event)
    {
        if (event.isSuccess())
        {
            ReceivedTestSummaryAdapter adapter = new ReceivedTestSummaryAdapter(this, event.getSentTestInformationList());
            recycler_receivedTestSummary.setAdapter(adapter);
        }
        else
        {
            Toast.makeText(this, ""+event.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}