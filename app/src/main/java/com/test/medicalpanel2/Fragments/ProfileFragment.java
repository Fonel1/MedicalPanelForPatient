package com.test.medicalpanel2.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.test.medicalpanel2.Common.Common;
import com.test.medicalpanel2.R;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class ProfileFragment extends Fragment {

    public Unbinder unbinder;

    @BindView(R.id.txt_user_name)
    TextView txt_user_name;
    @BindView(R.id.txt_user_city)
    TextView txt_user_city;
    @BindView(R.id.txt_user_mail)
    TextView txt_user_mail;
    @BindView(R.id.txt_user_phone)
    TextView txt_user_phone;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, view);

        showUserInfo();

        return view;
    }

    private void showUserInfo() {
        txt_user_city.setText(Common.currentUser.getAddress());
        txt_user_mail.setText(Common.currentUser.getEmail());
        txt_user_phone.setText(Common.currentUser.getPhoneNumber());
        txt_user_name.setText(Common.currentUser.getName());
    }
}