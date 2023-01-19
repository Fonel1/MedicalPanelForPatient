package com.test.medicalpanel2.Interface;

import com.test.medicalpanel2.Model.Doctor;

import java.util.List;

public interface IDoctorsLoadListener {
    //void onDoctorsLoadSuccess(List<Doctor> doctorsList);
    void onDoctorsLoadFailed(String message);

    void onDoctorsLoadSuccess(List<String> doctorsList);
}
