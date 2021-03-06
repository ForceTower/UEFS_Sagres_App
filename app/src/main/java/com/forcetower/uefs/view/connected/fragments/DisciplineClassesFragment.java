package com.forcetower.uefs.view.connected.fragments;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.forcetower.uefs.AppExecutors;
import com.forcetower.uefs.R;
import com.forcetower.uefs.databinding.FragmentDisciplineClassesBinding;
import com.forcetower.uefs.db.entity.DisciplineClassItem;
import com.forcetower.uefs.db.entity.DisciplineClassMaterialLink;
import com.forcetower.uefs.di.Injectable;
import com.forcetower.uefs.util.NetworkUtils;
import com.forcetower.uefs.view.connected.ActivityController;
import com.forcetower.uefs.view.connected.adapters.ClassesAdapter;
import com.forcetower.uefs.vm.base.DisciplinesViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class DisciplineClassesFragment extends Fragment implements Injectable {
    public static final String INTENT_GROUP_ID = "group_id";
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    AppExecutors executors;

    private ClassesAdapter classesAdapter;
    private ActivityController controller;
    private DisciplinesViewModel disciplinesVM;

    public static Fragment getFragment(int groupId) {
        Bundle bundle = new Bundle();
        bundle.putInt(INTENT_GROUP_ID, groupId);

        Fragment fragment = new DisciplineClassesFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        controller = (ActivityController) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentDisciplineClassesBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_discipline_classes, container, false);

        controller.changeTitle(R.string.discipline_classes);
        controller.getTabLayout().setVisibility(View.GONE);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        classesAdapter = new ClassesAdapter(requireContext(), new ArrayList<>());
        classesAdapter.setOnClassClickListener(this::onClassClickListener);
        binding.recyclerView.setAdapter(classesAdapter);


        if (getArguments() != null) {
            int groupId = getArguments().getInt(INTENT_GROUP_ID, 0);

            disciplinesVM = ViewModelProviders.of(this, viewModelFactory).get(DisciplinesViewModel.class);
            disciplinesVM.getDisciplineItems(groupId).observe(this, this::onItemsUpdate);
        }

        return binding.getRoot();
    }

    private void onClassClickListener(DisciplineClassItem classItem, int position) {
        executors.others().execute(() -> {
            Timber.d("Class Item name: %s - %d", classItem.getSubject(), classItem.getNumber());
            List<DisciplineClassMaterialLink> materials = disciplinesVM.getClassMaterialsDirect(classItem.getUid());
            executors.mainThread().execute(() -> {
                //noinspection ConstantConditions
                if (materials.size() == 0) {
                    Timber.d("Well that's odd");
                } else {
                    Timber.d("This class has %d materials", materials.size());
                    Timber.d("The materials are %s", materials);
                    showSelectGroupDialog(materials);
                }
            });
        });
    }

    private void showSelectGroupDialog(List<DisciplineClassMaterialLink> materials) {
        AlertDialog.Builder selectDialog = new AlertDialog.Builder(requireContext());
        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_book_open_black_24dp);
        if (icon != null) {
            icon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            selectDialog.setIcon(icon);
        } else {
            selectDialog.setIcon(R.drawable.ic_book_open_black_24dp);
        }
        selectDialog.setTitle(R.string.select_a_material);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.select_dialog_item);
        for (DisciplineClassMaterialLink material : materials) {
            String text = material.getName();
            arrayAdapter.add(text);
        }

        selectDialog.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        selectDialog.setAdapter(arrayAdapter, (dialog, which) -> {
            String strName = arrayAdapter.getItem(which);
            int position = arrayAdapter.getPosition(strName);
            DisciplineClassMaterialLink material = materials.get(position);
            Timber.d("Link selected: " + material.getLink());
            NetworkUtils.openLink(requireContext(), material.getLink());
            dialog.dismiss();
        });
        executors.mainThread().execute(selectDialog::show);
    }

    private void onItemsUpdate(List<DisciplineClassItem> disciplineClassItems) {
        Timber.d("Class items update!");
        classesAdapter.setClasses(disciplineClassItems);
    }
}
