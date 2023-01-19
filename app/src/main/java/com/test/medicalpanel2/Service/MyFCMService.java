package com.test.medicalpanel2.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.test.medicalpanel2.Common.Common;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.paperdb.Paper;

public class MyFCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String t) {
        super.onNewToken(t);
        Common.updateToken(this, t);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Common.showNotifications(this,
                new Random().nextInt(),
                message.getData().get(Common.TITLE_KEY),
                message.getData().get(Common.CONTENT_KEY),
                null);

        //dataSend.put("update_done", "true");

        if (message.getData() != null)
        {
            if (message.getData().get("update_done") != null)
            {
                updateLastAppointment();
            }

            if (message.getData().get(Common.TITLE_KEY) != null &&
            message.getData().get(Common.CONTENT_KEY) != null)
            {
                Common.showNotifications(this,
                        new Random().nextInt(),
                        message.getData().get(Common.TITLE_KEY),
                        message.getData().get(Common.CONTENT_KEY),
                        null);
            }
        }

    }

    private void updateLastAppointment() {

        CollectionReference userAppointment;
        //if app running
        if (Common.currentUser != null)
        {
           userAppointment = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(Common.currentUser.getEmail())
                    .collection("Appointment");
        }
        else
        {
            //if app not running
            Paper.init(this);
            String user = Paper.book().read(Common.LOGGED_KEY);

            userAppointment = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(user)
                    .collection("Appointment");
        }

        //check if exist by get current date
        //only load appointment for current date and next 3 days
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.HOUR_OF_DAY,0);
        calendar.add(Calendar.MINUTE, 0);

        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

        userAppointment
                .whereGreaterThanOrEqualTo("timestamp", timestamp)
                .whereEqualTo("done", false)
                .limit(1)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    if (task.getResult().size() > 0)
                    {
                       //Update
                        DocumentReference userAppointmentCurrentDocument = null;
                        for (DocumentSnapshot documentSnapshot: task.getResult())
                        {
                            userAppointmentCurrentDocument = userAppointment.document(documentSnapshot.getId());

                        }
                        if (userAppointmentCurrentDocument != null)
                        {
                            Map<String, Object> dataUpdate = new HashMap<>();
                            dataUpdate.put("done", true);
                            userAppointmentCurrentDocument.update(dataUpdate)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MyFCMService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }
}