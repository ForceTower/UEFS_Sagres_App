package com.forcetower.uefs.view.connected.fragments;

import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.forcetower.uefs.R;
import com.forcetower.uefs.databinding.FragmentConnectedBinding;
import com.forcetower.uefs.di.Injectable;
import com.forcetower.uefs.util.AnimUtils;
import com.forcetower.uefs.util.VersionUtils;
import com.forcetower.uefs.view.connected.ActivityController;
import com.forcetower.uefs.view.connected.MainContentController;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import timber.log.Timber;

public class ConnectedFragment extends Fragment implements Injectable, MainContentController {
    public static final String FRAGMENT_INTENT_EXTRA = "notification_intent_extra";
    public static final String SCHEDULE_FRAGMENT = "ScheduleFragment";
    public static final String MESSAGES_FRAGMENT_SAGRES = "MessagesFragment_SAGRES";
    public static final String MESSAGES_FRAGMENT_UNES = "MessagesFragment_UNES";
    public static final String GRADES_FRAGMENT = "GradesFragment";
    public static final String DISCIPLINES_FRAGMENT = "DisciplinesFragment";
    public static final String CALENDAR_FRAGMENT = "CalendarFragment";
    public static final String BIG_TRAY_FRAGMENT = "BigTrayFragment";
    public static final String EVENT_FRAGMENT = "EventFragment";

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private FragmentManager fragmentManager;
    private SharedPreferences preferences;
    private ActivityController controller;
    private FragmentConnectedBinding binding;

    @IdRes
    private int containerId;
    private boolean showingTab;
    @IdRes
    private int selectedTab;
    private boolean newScheduleLayout = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            controller = (ActivityController) context;
            selectedTab = R.id.navigation_home;
        } catch (ClassCastException e) {
            Timber.d("Activity %s must implement ActivityController", context.getClass().getSimpleName());
        }
    }

    @Nullable
    @Override
    public View onCreateView (@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connected, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        newScheduleLayout = preferences.getBoolean("new_schedule_layout", true);
        newScheduleLayout = preferences.getBoolean("new_schedule_user_ready", false) && newScheduleLayout;
        Timber.d("New schedule (onCreate) %s", newScheduleLayout);

        fragmentManager = getChildFragmentManager();
        containerId = R.id.container;

        binding.navigation.setOnNavigationItemSelectedListener(this::onNavigationOptionSelected);

        if (savedInstanceState == null) {
            preferences.edit().putBoolean("show_not_connected_notification", false).apply();

            if (getArguments() != null) {
                String value = getArguments().getString(FRAGMENT_INTENT_EXTRA);
                if (value == null) {
                    navigateToSchedule();
                } else {
                    Timber.d("Action asks for: %s", value);
                    if (value.equalsIgnoreCase(MESSAGES_FRAGMENT_SAGRES)) {
                        MessagesFragment.SELECT_FRAGMENT_AUTO = 0;
                        binding.navigation.setSelectedItemId(R.id.navigation_messages);
                    } else if (value.equalsIgnoreCase(MESSAGES_FRAGMENT_UNES)) {
                        MessagesFragment.SELECT_FRAGMENT_AUTO = 1;
                        MessagesFragment.DO_SELECT_AUTO = true;
                        binding.navigation.setSelectedItemId(R.id.navigation_messages);
                    } else if (value.equalsIgnoreCase(SCHEDULE_FRAGMENT)) {
                        binding.navigation.setSelectedItemId(R.id.navigation_home);
                    } else if (value.equalsIgnoreCase(GRADES_FRAGMENT)) {
                        binding.navigation.setSelectedItemId(R.id.navigation_grades);
                    } else if (value.equalsIgnoreCase(DISCIPLINES_FRAGMENT)) {
                        binding.navigation.setSelectedItemId(R.id.navigation_disciplines);
                    } else if (value.equalsIgnoreCase(CALENDAR_FRAGMENT)) {
                        binding.navigation.setSelectedItemId(R.id.navigation_calendar);
                    } else {
                        navigateToSchedule();
                    }
                }
            } else {
                navigateToSchedule();
            }
        } else {
            showingTab = savedInstanceState.getBoolean("tab_showing", false);
            selectedTab = savedInstanceState.getInt("selected_tab", R.id.navigation_home);
            binding.navigation.setSelectedItemId(selectedTab);
            setTabShowing(showingTab);
        }

        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        newScheduleLayout = preferences.getBoolean("new_schedule_layout", true);
        newScheduleLayout = preferences.getBoolean("new_schedule_user_ready", false) && newScheduleLayout;
        binding.navigation.setSelectedItemId(selectedTab);
    }

    @MainThread
    private boolean onNavigationOptionSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();

        selectedTab = id;
        controller.selectItemFromNavigation(id);
        if      (id == R.id.navigation_home)        navigateToSchedule();
        else if (id == R.id.navigation_grades)      navigateToGrades();
        else if (id == R.id.navigation_messages)    navigateToMessages();
        else if (id == R.id.navigation_disciplines) navigateToDisciplines();
        else if (id == R.id.navigation_calendar)    navigateToCalendar();

        return true;
    }

    @Override
    public void navigateToSchedule() {
        changeTitle(R.string.title_schedule);
        setTabShowing(false);
        Timber.d("Show new schedule? %s", newScheduleLayout);
        if (newScheduleLayout) {
            changeFragment(new NewScheduleFragment());
            showNewScheduleWarning();
        }
        else {
            changeFragment(new ScheduleFragment());
        }
    }

    @Override
    public void navigateToMessages() {
        changeTitle(R.string.title_messages);
        setTabShowing(true);
        changeFragment(new MessagesFragment());
    }

    @Override
    public void navigateToGrades() {
        changeTitle(R.string.title_grades);
        setTabShowing(true);
        changeFragment(new AllSemestersGradeFragment());
    }

    @Override
    public void navigateToDisciplines() {
        changeTitle(R.string.title_disciplines);
        setTabShowing(false);
        changeFragment(new DisciplinesFragment());
    }

    @Override
    public void navigateToCalendar() {
        changeTitle(R.string.title_calendar);
        setTabShowing(false);
        changeFragment(new CalendarFragment());
    }

    @MainThread
    private void changeFragment(@NonNull Fragment fragment) {
        Fragment current = fragmentManager.findFragmentByTag(fragment.getClass().getSimpleName());
        if (current != null) fragment = current;

        fragmentManager.beginTransaction()
                .replace(containerId, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    private void changeTitle(@StringRes int idRes) {
        controller.changeTitle(idRes);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("tab_showing", showingTab);
        outState.putInt("selected_tab", selectedTab);
        super.onSaveInstanceState(outState);
    }

    private void setTabShowing(boolean b) {
        if (!b) controller.getTabLayout().setVisibility(View.GONE);
        else AnimUtils.fadeIn(requireContext(), controller.getTabLayout());

        showingTab = b;
    }

    private void showNewScheduleWarning() {
        if (!preferences.getBoolean("warnings_4.0.0_new_schedule", false)) {
            if (!VersionUtils.isFirstInstall(requireContext()))
                Toast.makeText(requireContext(), R.string.new_schedule_takes_an_update, Toast.LENGTH_LONG).show();
            preferences.edit().putBoolean("warnings_4.0.0_new_schedule", true).apply();
        }
    }
}
