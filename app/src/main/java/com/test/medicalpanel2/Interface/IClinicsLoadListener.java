package com.test.medicalpanel2.Interface;

import com.test.medicalpanel2.Model.Clinic;
import com.test.medicalpanel2.Model.Clinic;

import java.util.List;

public interface IClinicsLoadListener {
    void onClinicsLoadSuccess(List<Clinic> clinicsList);
    void onClinicsLoadFailed(String message);
}
