package com.test.medicalpanel2.Model.Event;

import com.test.medicalpanel2.Model.AppointmentInformation;

import java.util.List;

public class UserAppointmentLoadEvent {
    private boolean success;
    private String message;
    private List<AppointmentInformation> appointmentInformationList;

    public UserAppointmentLoadEvent(boolean success, List<AppointmentInformation> appointmentInformationList) {
        this.success = success;
        this.appointmentInformationList = appointmentInformationList;
    }

    public UserAppointmentLoadEvent(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AppointmentInformation> getAppointmentInformationList() {
        return appointmentInformationList;
    }

    public void setAppointmentInformationList(List<AppointmentInformation> appointmentInformationList) {
        this.appointmentInformationList = appointmentInformationList;
    }
}
