package com.test.medicalpanel2.Model.Event;

import com.test.medicalpanel2.Model.Doctor;

import java.util.List;

public class DoctorDoneEvent {
    List<Doctor> doctorList;

    public DoctorDoneEvent(List<Doctor> doctorList) {
        this.doctorList = doctorList;
    }

    public List<Doctor> getDoctorList() {
        return doctorList;
    }

    public void setDoctorList(List<Doctor> doctorList) {
        this.doctorList = doctorList;
    }
}
