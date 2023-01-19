package com.test.medicalpanel2.Model.Event;

import com.test.medicalpanel2.Model.Clinic;
import com.test.medicalpanel2.Model.Doctor;

public class EnableNextButton {
    private  int step;
    private Doctor doctor;
    private Clinic clinic;
    private int dataSlot;
    private String test;

    public EnableNextButton(int step, String test) {
        this.step = step;
        this.test = test;
    }

    public EnableNextButton(int step, Doctor doctor) {
        this.step = step;
        this.doctor = doctor;
    }

    public EnableNextButton(int step, Clinic clinic) {
        this.step = step;
        this.clinic = clinic;
    }

    public EnableNextButton(int step, int dataSlot) {
        this.step = step;
        this.dataSlot = dataSlot;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public int getDataSlot() {
        return dataSlot;
    }

    public void setDataSlot(int dataSlot) {
        this.dataSlot = dataSlot;
    }

}
