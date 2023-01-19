package com.test.medicalpanel2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.medicalpanel2.Adapter.HistoryAdapter;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.Interface.RecyclerViewInterface;
import com.test.medicalpanel2.Model.AppointmentInformation;
import com.test.medicalpanel2.Model.Event.UserAppointmentLoadEvent;
import com.test.medicalpanel2.Model.SentTestInformation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class HistoryActivity extends AppCompatActivity implements RecyclerViewInterface {

    @BindView(R.id.recycler_history)
    RecyclerView recycler_history;
    @BindView(R.id.txt_history)
    TextView txt_history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ButterKnife.bind(this);

        initz();
        initView();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
        loadUserAppointmentInformation();
        }
    }

    private void loadUserAppointmentInformation() {

        CollectionReference userAppointment = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Common.currentUser.getEmail())
                .collection("Appointment");

        userAppointment.whereEqualTo("done", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        EventBus.getDefault().postSticky(new UserAppointmentLoadEvent(false, e.getMessage()));
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    List<AppointmentInformation> appointmentInformationList = new ArrayList<>();
                    for (DocumentSnapshot userAppointmentSnapShot:task.getResult())
                    {
                        AppointmentInformation appointmentInformation = userAppointmentSnapShot.toObject(AppointmentInformation.class);
                        appointmentInformationList.add(appointmentInformation);
                    }
                    EventBus.getDefault().postSticky(new UserAppointmentLoadEvent(true, appointmentInformationList));
                }
            }
        });
    }

    private void initView() {
        recycler_history.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_history.setLayoutManager(layoutManager);
        recycler_history.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
    }

    private void initz() {
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
    public void displayData(UserAppointmentLoadEvent event)
    {
        if (event.isSuccess())
        {
            HistoryAdapter adapter = new HistoryAdapter(this, event.getAppointmentInformationList(), this);
            recycler_history.setAdapter(adapter);

            txt_history.setText(new StringBuilder("Historia (")
            .append(event.getAppointmentInformationList().size())
            .append(")"));
        }
        else
        {
            Toast.makeText(this, ""+event.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClickListener(int position) {
    }
}