package com.test.medicalpanel2.Model.Event;

import com.test.medicalpanel2.Model.AppointmentInformation;
import com.test.medicalpanel2.Model.SentTestInformation;

import java.util.List;

public class ReceivedTestSummaryEvent {
    private boolean success;
    private String message;
    private List<SentTestInformation> sentTestInformationList;

    public ReceivedTestSummaryEvent(boolean success, List<SentTestInformation> sentTestInformationList) {
        this.success = success;
        this.sentTestInformationList = sentTestInformationList;
    }

    public ReceivedTestSummaryEvent(boolean success, String message) {
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

    public List<SentTestInformation> getSentTestInformationList() {
        return sentTestInformationList;
    }

    public void setSentTestInformationList(List<SentTestInformation> sentTestInformationList) {
        this.sentTestInformationList = sentTestInformationList;
    }
}
