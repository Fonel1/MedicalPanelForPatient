package com.test.medicalpanel2.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.test.medicalpanel2.Adapter.DoctorAdapter;
import com.test.medicalpanel2.Common.SpacesItemDecoration;
import com.test.medicalpanel2.Model.Event.DoctorDoneEvent;
import com.test.medicalpanel2.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class AppointmentStep2 extends Fragment {

    Unbinder unbinder;

    @BindView(R.id.recycler_doctor)
    RecyclerView recycler_doctor;

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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void setDoctorAdapter(DoctorDoneEvent event)
    {
        DoctorAdapter adapter = new DoctorAdapter(getContext(),event.getDoctorList());
        recycler_doctor.setAdapter(adapter);
    }

    static AppointmentStep2 instance;

    public static AppointmentStep2 getInstance() {
        if (instance == null)
            instance = new AppointmentStep2();
        return instance;
    }


    public AppointmentStep2() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_appointment_step2, container, false);

        unbinder = ButterKnife.bind(this, fragmentView);

        initView();

        return fragmentView;
    }

    private void initView() {
        recycler_doctor.setHasFixedSize(true);
        recycler_doctor.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recycler_doctor.addItemDecoration(new SpacesItemDecoration(4));
    }
}