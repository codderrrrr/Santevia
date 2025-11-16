package com.example.medilink.OnBoarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnBoradingViewPagerAdaptor extends FragmentStateAdapter {
    public OnBoradingViewPagerAdaptor(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 1) {
            return new OnBoardingFragment02();
        }
        return new OnBoardingFragment01();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
