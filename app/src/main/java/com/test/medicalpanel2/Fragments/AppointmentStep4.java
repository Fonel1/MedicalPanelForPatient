package com.test.medicalpanel2.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.HomeActivity;
import com.test.medicalpanel2.Model.AppointmentInformation;
import com.test.medicalpanel2.Model.Event.ConfirmAppointmentEvent;
import com.test.medicalpanel2.Model.FCMResponse;
import com.test.medicalpanel2.Model.FCMSendData;
import com.test.medicalpanel2.Model.MyToken;
import com.test.medicalpanel2.Model.Notification;
import com.test.medicalpanel2.R;
import com.test.medicalpanel2.Retrofit.IFCMApi;
import com.test.medicalpanel2.Retrofit.RetrofitClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class AppointmentStep4 extends Fragment {

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    SimpleDateFormat simpleDateFormat;
    Unbinder unbinder;
    FirebaseUser user;

    IFCMApi ifcmApi;

    @BindView(R.id.txt_appointment_doctor_text)
    TextView txt_appointment_doctor_text;
    @BindView(R.id.txt_appointment_data_text)
    TextView txt_appointment_data_text;
    @BindView(R.id.txt_clinic_address)
    TextView txt_clinic_address;
    @BindView(R.id.txt_clinic_name)
    TextView txt_clinic_name;
    @BindView(R.id.txt_clinic_open_hours)
    TextView txt_clinic_open_hours;
    @BindView(R.id.txt_clinic_website)
    TextView txt_clinic_website;
    @BindView(R.id.btn_confirm)
    Button btn_confirm;



    @OnClick(R.id.btn_confirm)
    void confirmAppointment() {
        long startTimeDB = System.nanoTime();

        //Process Timestamp - use for filter all booking with date is greater today; to display all future appointments
        String startTime = Common.convertDataSlotToString(Common.currentDataSlot);
        String[] convertTime = startTime.split("-");
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); //9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // 00

        Calendar appointmentDateWithHours = Calendar.getInstance();
        appointmentDateWithHours.setTimeInMillis(Common.currentData.getTimeInMillis());
        appointmentDateWithHours.set(Calendar.HOUR_OF_DAY, startHourInt);
        appointmentDateWithHours.set(Calendar.MINUTE, startMinInt);

        //timestamp object apply to AppointmentInformation
        Timestamp timestamp = new Timestamp(appointmentDateWithHours.getTime());

        //booking information
        final AppointmentInformation appointmentInformation = new AppointmentInformation();

        appointmentInformation.setCityApt(Common.city);
        appointmentInformation.setTimestamp(timestamp);
        appointmentInformation.setDone(false); //false, because we will use this field to filter for display user
        appointmentInformation.setDoctorId(Common.currentDoctor.getDoctorId());
        appointmentInformation.setDoctorName(Common.currentDoctor.getName());
        appointmentInformation.setPatientName(Common.currentUser.getName());
        appointmentInformation.setPatientEmail(Common.currentUser.getEmail());
        appointmentInformation.setClinicAddress(Common.currentClinic.getAddress());
        appointmentInformation.setClinicId(Common.currentClinic.getClinicId());
        appointmentInformation.setClinicName(Common.currentClinic.getName());
        appointmentInformation.setTime(new StringBuilder(Common.convertDataSlotToString(Common.currentDataSlot))
                .append(" dnia ")
                .append(simpleDateFormat.format(Common.currentData.getTime())).toString());
        appointmentInformation.setSlot(Long.valueOf(Common.currentDataSlot));

        //Submit to doctor
        DocumentReference appointmentData = FirebaseFirestore.getInstance()
                .collection("AllClinics")
                .document(Common.city)
                .collection("Clinics")
                .document(Common.currentClinic.getClinicId())
                .collection("Doctors")
                .document(Common.currentDoctor.getDoctorId())
                .collection(Common.simpleDateFormat.format(Common.currentData.getTime()))
                .document(String.valueOf(Common.currentDataSlot));

        //Write data
        appointmentData.set(appointmentInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //function to check already existing booking, to prevent new booking

                        long endTime = System.nanoTime();
                        long duration = (endTime - startTimeDB)/1000000;
                        System.out.println("Czas wysyłania danych: " + duration + " ms");

                        addToUserAppointment(appointmentInformation);
                        Toast.makeText(getActivity(), "Wizyta umówiona!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), HomeActivity.class));

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToUserAppointment(AppointmentInformation appointmentInformation) {

        //creating appointment collection
        CollectionReference userAppointment = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Objects.requireNonNull(user.getEmail()))
                .collection("Appointment");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTime = new Timestamp(calendar.getTime());

        userAppointment
                .whereGreaterThanOrEqualTo("timestamp", toDayTime)
                .whereEqualTo("done", false)
                .limit(2)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().isEmpty())
                {
                    //set data
                    userAppointment.document()
                            .set(appointmentInformation)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    //Create notification
                                    Notification notification = new Notification();
                                    notification.setUid(UUID.randomUUID().toString());
                                    notification.setTitle("Nowa wizyta");
                                    notification.setContent("Nowa rezerwacja wizyty od " +Common.currentUser.getName());
                                    notification.setRead(false);
                                    notification.setServerTimestamp(FieldValue.serverTimestamp());

                                    //Submit notification to "Notification" collection of doctor
                                    FirebaseFirestore.getInstance()
                                            .collection("AllClinics")
                                            .document(Common.city)
                                            .collection("Clinics")
                                            .document(Common.currentClinic.getClinicId())
                                            .collection("Doctors")
                                            .document(Common.currentDoctor.getDoctorId())
                                            .collection("Notifications")
                                            .document(notification.getUid())
                                            .set(notification)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    FirebaseFirestore.getInstance()
                                                            .collection("Tokens")
                                                            .whereEqualTo("email", Common.currentDoctor.getUsername())
                                                            .limit(2)
                                                            .get()
                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful() && task.getResult().size() > 0)
                                                                    {
                                                                        MyToken myToken = new MyToken();
                                                                        for (DocumentSnapshot tokenSnapShot:task.getResult())
                                                                            myToken = tokenSnapShot.toObject(MyToken.class);

                                                                        FCMSendData sendRequest = new FCMSendData();
                                                                        Map<String,String> dataSend = new HashMap<>();
                                                                        dataSend.put(Common.TITLE_KEY, "Nowa wizyta");
                                                                        dataSend.put(Common.CONTENT_KEY, "Masz nowa rezerwacje wizyty od pacjenta " + Common.currentUser.getName());

                                                                        sendRequest.setTo(myToken.getToken());
                                                                        sendRequest.setData(dataSend);

                                                                        compositeDisposable.add(ifcmApi.sendNotification(sendRequest)
                                                                                .subscribeOn(Schedulers.io())
                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                .subscribe(new Consumer<FCMResponse>() {
                                                                                    @Override
                                                                                    public void accept(FCMResponse fcmResponse) throws Exception {
                                                                                        resetStaticData();
                                                                                        getActivity().finish();
                                                                                        Toast.makeText(getContext(), "Sukces!", Toast.LENGTH_SHORT).show();


                                                                                    }
                                                                                }, new Consumer<Throwable>() {
                                                                                    @Override
                                                                                    public void accept(Throwable throwable) throws Exception {
                                                                                        Log.d("NOTIFICATION_ERROR", throwable.getMessage());

                                                                                        resetStaticData();
                                                                                        getActivity().finish();
                                                                                        Toast.makeText(getContext(), "Sukces!", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }));

                                                                    }
                                                                }
                                                            });

                                                }
                                            });



                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {

                    resetStaticData();
                    getActivity().finish(); //close activity
                }
            }
        });
    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentDataSlot = -1;
        Common.currentClinic = null;
        Common.currentDoctor = null;
        Common.currentAppointment = null;
        Common.currentAppointmentId = "";
        Common.city = "";
        Common.currentData.add(Calendar.DATE, 0);
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



    private void setData() {
        txt_appointment_doctor_text.setText(Common.currentDoctor.getName());
        txt_appointment_data_text.setText(new StringBuilder(Common.convertDataSlotToString(Common.currentDataSlot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.currentData.getTime())));

        txt_clinic_address.setText(Common.currentClinic.getAddress());
        txt_clinic_name.setText(Common.currentClinic.getName());
        txt_clinic_website.setText(Common.currentClinic.getWebsite());
        txt_clinic_open_hours.setText(Common.currentClinic.getOpenHours());


    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void setDataAppointment(ConfirmAppointmentEvent event)
    {
        if (event.isConfirm())
        {
            setData();
        }
    }

    static AppointmentStep4 instance;
    public static AppointmentStep4 getInstance() {
        if (instance == null)
            instance = new AppointmentStep4();
        return instance;
    }


    public AppointmentStep4() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ifcmApi = RetrofitClient.getInstance().create(IFCMApi.class);

        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_appointment_step4, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        user = FirebaseAuth.getInstance().getCurrentUser();

        return itemView;
    }
}