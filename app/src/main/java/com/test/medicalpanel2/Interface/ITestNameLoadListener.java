package com.test.medicalpanel2.Interface;

import java.util.List;

public interface ITestNameLoadListener {
    void onTestResultLoadSuccess(List<String> testNamesList);
    void onTestResultLoadFailed(String message);
}
