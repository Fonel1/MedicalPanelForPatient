package com.test.medicalpanel2.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.medicalpanel2.Adapter.DataSlotAdapter;
import com.test.medicalpanel2.Adapter.DoctorAdapter;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.Common.SpacesItemDecoration;
import com.test.medicalpanel2.Interface.IDataSlotLoadListener;
import com.test.medicalpanel2.Model.DataSlot;
import com.test.medicalpanel2.Model.Event.DisplayDataSlot;
import com.test.medicalpanel2.Model.Event.DoctorDoneEvent;
import com.test.medicalpanel2.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;


public class AppointmentStep3 extends Fragment implements IDataSlotLoadListener {

    //Variable
    DocumentReference doctorDoc;
    IDataSlotLoadListener iDataSlotLoadListener;

    Unbinder unbinder;


    @BindView(R.id.recycler_data_slot)
    RecyclerView recycler_data_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void loadAllAvailableSlots(DisplayDataSlot event)
    {
        if (event.isDisplay())
        {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0); //Add current date
            loadAvailableDataSlotOfDoctor(Common.currentDoctor.getDoctorId(),
                    simpleDateFormat.format(date.getTimeInMillis()));
        }
    }

    private void loadAvailableDataSlotOfDoctor(String doctorId, String apptData) {

        // /AllClinics/Krakow/Clinics/0oPxXYXchs7q6JigHmCc/Doctors/8yCIErMvxH3i9UDq1MPJ

        doctorDoc = FirebaseFirestore.getInstance()
                .collection("AllClinics")
                .document(Common.city)
                .collection("Clinics")
                .document(Common.currentClinic.getClinicId())
                .collection("Doctors")
                .document(Common.currentDoctor.getDoctorId());

        //getting inf of doctor
        doctorDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) //if doctor is available
                    {
                        //getting inf about appointment
                        //if not created, return empty
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("AllClinics")
                                .document(Common.city)
                                .collection("Clinics")
                                .document(Common.currentClinic.getClinicId())
                                .collection("Doctors")
                                .document(Common.currentDoctor.getDoctorId())
                                .collection(apptData);

                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty()) //if dont have any appointment
                                        iDataSlotLoadListener.onDataSlotLoadEmpty();
                                    else
                                    {
                                        //If have appointment
                                        List<DataSlot> dataSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document:task.getResult())
                                            dataSlots.add(document.toObject(DataSlot.class));
                                        iDataSlotLoadListener.onDataSlotLoadSuccess(dataSlots);
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                iDataSlotLoadListener.onDataSlotLoadFailed(e.getMessage());
                            }
                        });
                    }
                }
            }
        });
    }

    static AppointmentStep3 instance;
    public static AppointmentStep3 getInstance() {
        if (instance == null)
            instance = new AppointmentStep3();
        return instance;
    }



    public AppointmentStep3() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iDataSlotLoadListener = this;

        simpleDateFormat =  new SimpleDateFormat("dd_MM_yyyy"); //day_month_year

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@NonNull ViewGroup container,
                             @NonNull Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_appointment_step3, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        init(itemView);

        return itemView;
    }

    private void init(View itemView) {
        recycler_data_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_data_slot.setLayoutManager(gridLayoutManager);
        recycler_data_slot.addItemDecoration(new SpacesItemDecoration(8));

        //Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2); // 2 day left

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(itemView, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.currentData.getTimeInMillis() != date.getTimeInMillis())
                {
                    Common.currentData = date; //won't load again if you select same day
                    loadAvailableDataSlotOfDoctor(Common.currentDoctor.getDoctorId(),simpleDateFormat.format(date.getTime()));
                }
            }
        });
    }

    @Override
    public void onDataSlotLoadSuccess(List<DataSlot> dataSlotList) {
        DataSlotAdapter adapter = new DataSlotAdapter(getContext(), dataSlotList);
        recycler_data_slot.setAdapter(adapter);
    }

    @Override
    public void onDataSlotLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataSlotLoadEmpty() {
        DataSlotAdapter adapter = new DataSlotAdapter(getContext());
        recycler_data_slot.setAdapter(adapter);
    }
}