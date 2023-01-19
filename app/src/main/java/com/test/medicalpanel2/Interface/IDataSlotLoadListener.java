package com.test.medicalpanel2.Interface;

import com.test.medicalpanel2.Model.DataSlot;

import java.util.List;

public interface IDataSlotLoadListener {
    void onDataSlotLoadSuccess(List<DataSlot> dataSlotList);
    void onDataSlotLoadFailed(String message);
    void onDataSlotLoadEmpty();

}