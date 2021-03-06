package com.forcetower.uefs.view.universe.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.forcetower.uefs.R;
import com.forcetower.uefs.databinding.FragmentUniverseCreateAccountBinding;
import com.forcetower.uefs.db_service.entity.AccessToken;
import com.forcetower.uefs.db_service.entity.Account;
import com.forcetower.uefs.di.Injectable;
import com.forcetower.uefs.rep.helper.Resource;
import com.forcetower.uefs.rep.helper.Status;
import com.forcetower.uefs.util.AnimUtils;
import com.forcetower.uefs.util.VersionUtils;
import com.forcetower.uefs.view.universe.UniverseNavigationController;
import com.forcetower.uefs.vm.UEFSViewModelFactory;
import com.forcetower.uefs.vm.universe.UAccountViewModel;

import java.util.Collections;

import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by João Paulo on 11/05/2018.
 */
public class UniverseCreateAccountFragment extends Fragment implements Injectable {
    @Inject
    UniverseNavigationController navigation;
    @Inject
    UEFSViewModelFactory viewModelFactory;

    private UAccountViewModel accountViewModel;
    private FragmentUniverseCreateAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_universe_create_account, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        beginAnimations();
        accountViewModel = ViewModelProviders.of(this, viewModelFactory).get(UAccountViewModel.class);
        accountViewModel.login(true).observe(this, this::onLoginProgress);
        accountViewModel.getCreateAccountSrc().observe(this, this::onCreateAccountProgress);
        accountViewModel.getAccessToken().observe(this, this::onAccessTokenReceived);
    }

    private void onLoginProgress(Resource<AccessToken> tokenResource) {
        if (tokenResource == null) return;

        if (tokenResource.status == Status.SUCCESS) {
            Timber.d("Resource Success: Token received!");
            navigation.navigateToCompleted(Collections.singletonList(
                    new Pair<>(getString(R.string.transition_logo), binding.ivLogo)
            ), requireContext());
        } else if (tokenResource.status == Status.ERROR) {
            Timber.d("Resource error code: %d", tokenResource.code);
            Timber.d("Resource error message: %s", tokenResource.message);
        } else {
            Timber.d("Loading resource...");
        }
    }

    private void onAccessTokenReceived(AccessToken token) {
        if (token != null && token.isValid() && !token.isExpired()) {
            Timber.d("Received token... App can proceed");
        }
    }

    private void onCreateAccountProgress(Resource<Account> responseAccount) {
        if (responseAccount == null) {
            Timber.d("responseAccount is null");
            return;
        }
        if (responseAccount.status == Status.LOADING) {
            Timber.d("Loading message: %s", responseAccount.message);
            Timber.d("Loading data:    %s", responseAccount.data);
        } else if (responseAccount.status == Status.ERROR) {
            Timber.d("Error message: %s", responseAccount.message);
            Timber.d("Error code: %d", responseAccount.code);
            Timber.d("Error Action: %s", responseAccount.actionError != null ? responseAccount.actionError.getErrors() : "Action Error is null");

            if (responseAccount.code == 422) {
                Toast.makeText(requireContext(), R.string.universe_account_already_exists, Toast.LENGTH_SHORT).show();
                if (VersionUtils.isLollipop()) {
                    setExitTransition(new Fade());
                }

                navigation.navigateToLogin(Collections.singletonList(
                        new Pair<>(getString(R.string.transition_logo), binding.ivLogo))
                );
            } else {
                Toast.makeText(requireContext(), R.string.universe_connection_error, Toast.LENGTH_SHORT).show();
            }
            binding.pbProgress.setVisibility(View.INVISIBLE);
        } else {
            Timber.d("Success!");
            Timber.d("Account: %s", responseAccount.data);
            accountViewModel.login(true);
        }
    }

    private void beginAnimations() {
        if (!VersionUtils.isLollipop()) {
            binding.llAnimations.setVisibility(View.VISIBLE);
            int children = binding.llAnimations.getChildCount();
            for (int i = 0; i < children; i++) {
                View view = binding.llAnimations.getChildAt(i);
                view.setVisibility(View.VISIBLE);
            }
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                TransitionManager.beginDelayedTransition(binding.viewRoot, new TransitionSet()
                        .addTransition(new ChangeBounds())
                        .addTransition(new ChangeImageTransform()));

                binding.llAnimations.setVisibility(View.VISIBLE);
                fadeInViews(0);
            }, 750);
        }
    }

    private void fadeInViews(int pos) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (pos < binding.llAnimations.getChildCount()) {
                if (isAdded() && isVisible() && isResumed() && !isHidden()) {
                    AnimUtils.slideIn(getContext(), binding.llAnimations.getChildAt(pos));
                    fadeInViews(pos + 1);
                }
            }
        }, 400);
    }
}
