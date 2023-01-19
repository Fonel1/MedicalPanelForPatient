package com.test.medicalpanel2.Interface;

import com.test.medicalpanel2.Model.AppointmentInformation;

public interface IAppointmentInfoLoadListener {
    void onAppointmentInfoLoadEmpty();
    void onAppointmentInfoLoadSuccess(AppointmentInformation appointmentInformation, String documentId);
    void onAppointmentInfoLoadFailed(String message);
}
