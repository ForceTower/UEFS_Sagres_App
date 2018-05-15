package com.forcetower.uefs.view.connected.fragments;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.forcetower.uefs.R;
import com.forcetower.uefs.db.entity.CalendarItem;
import com.forcetower.uefs.di.Injectable;
import com.forcetower.uefs.rep.helper.Resource;
import com.forcetower.uefs.rep.helper.Status;
import com.forcetower.uefs.view.connected.ActivityController;
import com.forcetower.uefs.view.connected.adapters.CalendarAdapter;
import com.forcetower.uefs.vm.base.CalendarViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by João Paulo on 08/03/2018.
 */

public class CalendarFragment extends Fragment implements Injectable {
    @BindView(R.id.recycler_view)
    RecyclerView rvCalendar;
    @BindView(R.id.swipe_refresh)
    androidx.swiperefreshlayout.widget.SwipeRefreshLayout refreshLayout;

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private CalendarAdapter calendarAdapter;
    private CalendarViewModel calendarViewModel;
    private ActivityController controller;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        controller = (ActivityController) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        ButterKnife.bind(this, view);
        controller.getTabLayout().setVisibility(View.GONE);
        controller.changeTitle(R.string.title_calendar);
        setupRecycler();
        setupRefreshLayout();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        calendarViewModel = ViewModelProviders.of(this, viewModelFactory).get(CalendarViewModel.class);
        calendarViewModel.getCalendar().observe(this, this::onReceiveCalendar);
        refreshLayout.setRefreshing(calendarViewModel.isRefreshing());
        calendarViewModel.refreshManual(false).observe(this, this::onUpdateReceived);
    }

    private void onReceiveCalendar(List<CalendarItem> calendarItems) {
        calendarAdapter.setCalendar(calendarItems);
    }

    private void setupRecycler() {
        calendarAdapter = new CalendarAdapter(getContext(), new ArrayList<>());
        rvCalendar.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        rvCalendar.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(requireContext(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));
        rvCalendar.setAdapter(calendarAdapter);
    }

    private void setupRefreshLayout() {
        refreshLayout.setOnRefreshListener(() -> {
            if (calendarViewModel.isRefreshing()) return;

            calendarViewModel.refreshManual(true).observe(this, this::onUpdateReceived);
            refreshLayout.setRefreshing(true);
            calendarViewModel.setRefreshing(true);
        });
    }

    private void onUpdateReceived(Resource<Integer> resource) {
        if (resource == null) return;

        if (resource.status == Status.SUCCESS) {
            refreshLayout.setRefreshing(false);
            calendarViewModel.setRefreshing(false);
        } else if (resource.status == Status.ERROR) {
            refreshLayout.setRefreshing(false);
            calendarViewModel.setRefreshing(false);
            if (resource.data != null)
                Toast.makeText(getContext(), resource.data, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), resource.message, Toast.LENGTH_SHORT).show();
        } else {
            //noinspection ConstantConditions
            Timber.d("Updating.. Received Status: %s", getString(resource.data));
        }
    }
}
