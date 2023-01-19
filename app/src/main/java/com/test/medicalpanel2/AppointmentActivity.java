package com.test.medicalpanel2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;
import com.test.medicalpanel2.Adapter.ViewPagerAdapter;
import com.test.medicalpanel2.Common.BlockSwipeViewPager;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.Model.Event.ConfirmAppointmentEvent;
import com.test.medicalpanel2.Model.Event.DisplayDataSlot;
import com.test.medicalpanel2.Model.Doctor;
import com.test.medicalpanel2.Model.Event.DoctorDoneEvent;
import com.test.medicalpanel2.Model.Event.EnableNextButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppointmentActivity extends AppCompatActivity {


    CollectionReference doctorRef;

    @BindView(R.id.step_view)
    StepView stepview;
    @BindView(R.id.view_pager)
    BlockSwipeViewPager viewPager;
    @BindView(R.id.btn_previous_step)
    Button btn_previous_step;
    @BindView(R.id.btn_next_step)
    Button btn_next_step;

    @OnClick(R.id.btn_next_step)
    void nextClick() {
        if (Common.step < 3 || Common.step == 0)
        {
            Common.step++;
            if (Common.step == 1)
            {
                if (Common.currentClinic != null)
                    loadDoctorByClinic(Common.currentClinic.getClinicId());
            }
            else if(Common.step == 2)
            {
                if (Common.currentDoctor != null)
                    loadTimeSlotOfDoctor(Common.currentDoctor.getDoctorId());
            }
            else if(Common.step == 3)
            {
                if (Common.currentDataSlot != -1)
                    confirmAppointment();
                btn_next_step.setEnabled(false);
            }
            viewPager.setCurrentItem(Common.step);
        }
    }

    @OnClick(R.id.btn_previous_step)
    void previousStep(){
        if (Common.step == 3 || Common.step > 0)
        {
            Common.step--;
            viewPager.setCurrentItem(Common.step);
            if (Common.step < 3)
            {
                btn_next_step.setEnabled(false);
                setColorButton();
            }
        }
    }

    private void confirmAppointment() {
        EventBus.getDefault().postSticky(new ConfirmAppointmentEvent(true));
    }

    private void loadTimeSlotOfDoctor(String doctorId) {
        EventBus.getDefault().postSticky(new DisplayDataSlot(true));
    }

    private void loadDoctorByClinic(String clinicId) {

        //select all doctor of Clinic
        //   /AllClinics/Krakow/Clinics/0oPxXYXchs7q6JigHmCc/Doctors
        if (!TextUtils.isEmpty(Common.city))
        {
            doctorRef = FirebaseFirestore.getInstance()
                    .collection("AllClinics")
                    .document(Common.city)
                    .collection("Clinics")
                    .document(clinicId)
                    .collection("Doctors");

            doctorRef.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<Doctor> doctors = new ArrayList<>();
                            for (QueryDocumentSnapshot doctorSnapShot:task.getResult())
                            {
                                Doctor doctor = doctorSnapShot.toObject(Doctor.class);
                                doctor.setPassword(""); //Remove password because in client App
                                doctor.setDoctorId(doctorSnapShot.getId()); //getting Id of doctor

                                doctors.add(doctor);
                            }

                            EventBus.getDefault().postSticky(new DoctorDoneEvent(doctors));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void buttonNextReceiver(EnableNextButton event)
    {
        int step = event.getStep();
        if (step == 1)
            Common.currentClinic = event.getClinic();
        else if (step == 2)
            Common.currentDoctor = event.getDoctor();
        else if (step == 3)
            Common.currentDataSlot = event.getDataSlot();

        btn_next_step.setEnabled(true);
        setColorButton();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);
        ButterKnife.bind(AppointmentActivity.this);

        startStepView();
        setColorButton();

        //View
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                stepview.go(position, true);
                if (position == 0)
                    btn_previous_step.setEnabled(false);
                else
                    btn_previous_step.setEnabled(true);

                setColorButton();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setColorButton() {
        if (btn_next_step.isEnabled())
        {
            btn_next_step.setBackgroundResource(R.color.lightblue);
        }
        else
        {
            btn_next_step.setBackgroundResource(android.R.color.darker_gray);
        }

        if (btn_previous_step.isEnabled())
        {
            btn_previous_step.setBackgroundResource(R.color.lightblue);
        }
        else
        {
            btn_previous_step.setBackgroundResource(android.R.color.darker_gray);
        }
    }

    private void startStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("Klinika");
        stepList.add("Lekarz");
        stepList.add("Data");
        stepList.add("Potwierdzenie");
        stepview.setSteps(stepList);
    }

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

}