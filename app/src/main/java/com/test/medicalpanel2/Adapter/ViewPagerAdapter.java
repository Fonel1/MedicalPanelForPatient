package com.test.medicalpanel2.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.test.medicalpanel2.Fragments.AppointmentStep1;
import com.test.medicalpanel2.Fragments.AppointmentStep2;
import com.test.medicalpanel2.Fragments.AppointmentStep3;
import com.test.medicalpanel2.Fragments.AppointmentStep4;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position)
        {
            case 0: return AppointmentStep1.getInstance();
            case 1: return AppointmentStep2.getInstance();
            case 2: return AppointmentStep3.getInstance();
            case 3: return AppointmentStep4.getInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
