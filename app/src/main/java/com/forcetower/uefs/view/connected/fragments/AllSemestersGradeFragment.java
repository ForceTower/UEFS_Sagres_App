package com.forcetower.uefs.view.connected.fragments;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forcetower.uefs.R;
import com.forcetower.uefs.databinding.FragmentAllSemestersGradesBinding;
import com.forcetower.uefs.db.entity.Semester;
import com.forcetower.uefs.di.Injectable;
import com.forcetower.uefs.util.WordUtils;
import com.forcetower.uefs.view.connected.ActivityController;
import com.forcetower.uefs.vm.base.GradesViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by João Paulo on 07/03/2018.
 */

public class AllSemestersGradeFragment extends Fragment implements Injectable {
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private GradesFragmentAdapter fragmentAdapter;
    private ActivityController controller;
    private FragmentAllSemestersGradesBinding binding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            controller = (ActivityController) context;
        } catch (ClassCastException ignored) {
            Timber.d("Activity %s must implement MainContentController", context.getClass().getSimpleName());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_semesters_grades, container, false);
        controller.changeTitle(R.string.title_grades);
        fragmentAdapter = new GradesFragmentAdapter(getChildFragmentManager());
        configureViewPager();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GradesViewModel gradesViewModel = ViewModelProviders.of(this, viewModelFactory).get(GradesViewModel.class);
        gradesViewModel.getAllSemesters().observe(this, this::onSemestersReceived);
    }

    private void onSemestersReceived(List<Semester> semesters) {
        Timber.d("Received semester list %s", semesters);

        //This is a risky call, but since we are in a lifecycle aware method, this should be fine
        TabLayout tabLayout = controller.getTabLayout();
        if (tabLayout != null) {
            tabLayout.removeAllTabs();
            tabLayout.clearOnTabSelectedListeners();
            tabLayout.setupWithViewPager(binding.viewPager);
            binding.viewPager.clearOnPageChangeListeners();
            binding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(binding.viewPager));
        }
        fragmentAdapter.setSemesterList(semesters);
    }

    private void configureViewPager() {
        binding.viewPager.setAdapter(fragmentAdapter);
    }

    private class GradesFragmentAdapter extends FragmentPagerAdapter {
        private List<Semester> semesterList;
        private List<SemesterGradesFragment> gradesFragmentList;

        GradesFragmentAdapter(FragmentManager fm) {
            super(fm);
            gradesFragmentList = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return gradesFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return gradesFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return semesterList == null ?
                    super.getPageTitle(position) :
                    semesterList.get(position).getName().substring(0, 4) + "." +
                            semesterList.get(position).getName().substring(4);
        }

        void setSemesterList(List<Semester> semesterList) {
            if (this.semesterList == null) this.semesterList = new ArrayList<>();

            Collections.sort(semesterList);
            gradesFragmentList.clear();
            this.semesterList.clear();
            this.semesterList.addAll(semesterList);

            Collections.sort(this.semesterList);
            for (Semester semester : semesterList) {
                if (!WordUtils.validString(semester.getName())) {
                    Timber.d("It's a shame that this semester is invalid");
                    continue;
                }

                SemesterGradesFragment fragment = new SemesterGradesFragment();
                Bundle arguments = new Bundle();
                arguments.putString("semester", semester.getName());
                fragment.setArguments(arguments);

                gradesFragmentList.add(fragment);
            }

            notifyDataSetChanged();
        }
    }
}
