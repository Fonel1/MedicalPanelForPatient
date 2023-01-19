package com.test.medicalpanel2.Model.Event;

public class ConfirmAppointmentEvent {
    private boolean isConfirm;

    public ConfirmAppointmentEvent(boolean isConfirm) {
        this.isConfirm = isConfirm;
    }

    public boolean isConfirm() {
        return isConfirm;
    }

    public void setConfirm(boolean confirm) {
        isConfirm = confirm;
    }
}
