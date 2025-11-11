package com.example.fapapplication.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fapapplication.entity.Class;
import com.example.fapapplication.fragment.ClassBasicInfoFragment;
import com.example.fapapplication.fragment.ClassStudentsFragment;
import com.example.fapapplication.fragment.ClassSubjectsFragment;

public class ClassDetailPagerAdapter extends FragmentStateAdapter {

    private Class currentClass;
    private ClassBasicInfoFragment basicInfoFragment;

    public ClassDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity, Class classObj) {
        super(fragmentActivity);
        this.currentClass = classObj;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                basicInfoFragment = ClassBasicInfoFragment.newInstance(currentClass);
                return basicInfoFragment;
            case 1:
                return ClassSubjectsFragment.newInstance(currentClass.getId());
            case 2:
                return ClassStudentsFragment.newInstance(currentClass.getId());
            default:
                return ClassBasicInfoFragment.newInstance(currentClass);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public ClassBasicInfoFragment getBasicInfoFragment() {
        return basicInfoFragment;
    }
}