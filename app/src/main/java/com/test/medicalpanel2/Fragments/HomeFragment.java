package com.test.medicalpanel2.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.medicalpanel2.AppointmentActivity;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.HistoryActivity;
import com.test.medicalpanel2.Interface.IAppointmentInfoLoadListener;
import com.test.medicalpanel2.Interface.IAppointmentInformationChangeListener;
import com.test.medicalpanel2.Model.AppointmentInformation;
import com.test.medicalpanel2.R;
import com.test.medicalpanel2.ReceivedTestSummary;
import com.test.medicalpanel2.SendTestResultToDoctor;

import java.util.Calendar;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HomeFragment extends Fragment implements IAppointmentInfoLoadListener, IAppointmentInformationChangeListener {

    public Unbinder unbinder;
    FirebaseUser user;

    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;
    @BindView(R.id.txt_member_type)
    TextView txt_member_type;

    @BindView(R.id.card_appointment_info)
    CardView card_appointment_info;
    @BindView(R.id.txt_info_address)
    TextView txt_info_address;
    @BindView(R.id.txt_doctor)
    TextView txt_doctor;
    @BindView(R.id.txt_time)
    TextView txt_time;


    @OnClick(R.id.btn_delete_appointment)
    void deleteAppointment()
    {
        deleteAppointmentFromDoctor(false);
    }

    @OnClick(R.id.btn_change_appointment)
    void changeAppointment() {
        changeAppointmentFromUser();
    }

    private void changeAppointmentFromUser() {
        deleteAppointmentFromDoctor(true);
    }

    private void deleteAppointmentFromDoctor(boolean isChange) {
        if (Common.currentAppointment != null)
        {
            //get apt info
            DocumentReference doctorAppointmentInfo = FirebaseFirestore.getInstance()
                    .collection("AllClinics")
                    .document(Common.currentAppointment.getCityApt())
                    .collection("Clinics")
                    .document(Common.currentAppointment.getClinicId())
                    .collection("Doctors")
                    .document(Common.currentAppointment.getDoctorId())
                    .collection(Common.convertTimeStampToStringKey(Common.currentAppointment.getTimestamp())) //collection(Common.currentAppointmentId) //.collection(Common.convertTimeStampToStringKey(Common.currentAppointment.getTimestamp()))
                    .document(Common.currentAppointment.getSlot().toString());

            //delete document
            doctorAppointmentInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Błąd", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    deleteAppointmentFromUser(isChange);
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Nie ma takiej rezerwacji", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAppointmentFromUser(boolean isChange) {
        if (!TextUtils.isEmpty(Common.currentAppointmentId))
        {
            DocumentReference userAppointmentInfo = FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(Common.currentUser.getEmail())
                    .collection("Appointment")
                    .document(Common.currentAppointmentId);

            //delete
            userAppointmentInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    Toast.makeText(getActivity(),"Wizyta odwołana!", Toast.LENGTH_SHORT).show();
                    //refersh
                    loadUserAppointment();

                    if (isChange)
                        iAppointmentInformationChangeListener.onAppointmentInformationChange();

                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "ID rezerwacji musi być znane", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.card_view_appointment)
    void appointment()
    {
        startActivity(new Intent(getContext(), AppointmentActivity.class));
    }

    @OnClick(R.id.card_view_history)
    void openHistoryActivity()
    {
        startActivity(new Intent(getContext(), HistoryActivity.class));
    }

    @OnClick(R.id.card_view_test_result)
    void openSendTestResultToDoctor()
    {
        startActivity(new Intent(getContext(), SendTestResultToDoctor.class));
    }

    @OnClick(R.id.card_view_notification)
    void openReceivedTestSummary()
    {
        startActivity(new Intent(getContext(), ReceivedTestSummary.class));
    }

    //interface
    IAppointmentInformationChangeListener iAppointmentInformationChangeListener;
    IAppointmentInfoLoadListener iAppointmentInfoLoadListener;


    ListenerRegistration userAppointmentListener = null;
    EventListener<QuerySnapshot> userAppointmentEvent = null;

    public HomeFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserAppointment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        iAppointmentInfoLoadListener = this;
        iAppointmentInformationChangeListener = this;

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            setUserInformation();
            initRealTimeUserAppointment();
            loadUserAppointment();
        }

        return view;
    }

    private void initRealTimeUserAppointment() {

        if (userAppointmentEvent == null)
        {
            userAppointmentEvent = new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                }
            };
        }

    }

    private void loadUserAppointment() {
        CollectionReference userAppointment = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(Objects.requireNonNull(user.getEmail()))
                .collection("Appointment");

        //current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        Timestamp toDayTime = new Timestamp(calendar.getTime());

        //information from firebase
        userAppointment
                .whereGreaterThanOrEqualTo("timestamp", toDayTime)
                .whereEqualTo("done", false)
                .limit(2)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            if (!task.getResult().isEmpty())
                            {
                                for (QueryDocumentSnapshot queryDocumentSnapshot:task.getResult())
                                {
                                    AppointmentInformation appointmentInformation = queryDocumentSnapshot.toObject(AppointmentInformation.class);
                                    iAppointmentInfoLoadListener.onAppointmentInfoLoadSuccess(appointmentInformation, queryDocumentSnapshot.getId());
                                    break;
                                }
                            } else
                                iAppointmentInfoLoadListener.onAppointmentInfoLoadEmpty();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAppointmentInfoLoadListener.onAppointmentInfoLoadFailed(e.getMessage());
            }
        });

        if (userAppointmentEvent != null)
        {
            if (userAppointmentListener == null)
            {
                userAppointmentListener = userAppointment
                        .addSnapshotListener(userAppointmentEvent);
            }
        }
    }



    private void setUserInformation() {
        layout_user_information.setVisibility(View.VISIBLE);
        txt_user_name.setText(user.getEmail());

    }

    @Override
    public void onAppointmentInfoLoadEmpty() {
        card_appointment_info.setVisibility(View.GONE);
    }

    @Override
    public void onAppointmentInfoLoadSuccess(AppointmentInformation appointmentInformation, String appointmentId) {

        Common.currentAppointment = appointmentInformation;
        Common.currentAppointmentId = appointmentId;

        txt_info_address.setText(appointmentInformation.getClinicAddress());
        txt_doctor.setText(appointmentInformation.getDoctorName());
        txt_time.setText(appointmentInformation.getTime());

        card_appointment_info.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAppointmentInfoLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAppointmentInformationChange() {
        startActivity(new Intent(getActivity(), AppointmentActivity.class));
    }

    @Override
    public void onDestroy() {
        if (userAppointmentListener != null)
            userAppointmentListener.remove();
        super.onDestroy();
    }
}