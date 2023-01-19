package com.test.medicalpanel2.Model.Event;

public class DisplayDataSlot {
    private boolean isDisplay;

    public DisplayDataSlot(boolean isDisplay) {
        this.isDisplay = isDisplay;
    }

    public boolean isDisplay() {
        return isDisplay;
    }

    public void setDisplay(boolean display) {
        isDisplay = display;
    }
}
