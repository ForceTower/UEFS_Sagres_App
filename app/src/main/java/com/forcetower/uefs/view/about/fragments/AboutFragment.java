package com.forcetower.uefs.view.about.fragments;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forcetower.uefs.R;
import com.forcetower.uefs.db.entity.CreditsMention;
import com.forcetower.uefs.util.MockUtils;
import com.forcetower.uefs.util.VersionUtils;
import com.forcetower.uefs.view.about.adapters.CreditsAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.forcetower.uefs.util.NetworkUtils.openLink;
import static com.forcetower.uefs.util.WordUtils.validString;

/**
 * Created by João Paulo on 18/04/2018.
 */
public class AboutFragment extends androidx.fragment.app.Fragment {
    @BindView(R.id.version_info)
    TextView versionInfo;
    @BindView(R.id.rv_credits)
    RecyclerView rvCredits;
    @BindView(R.id.cv_about_me)
    androidx.cardview.widget.CardView cvAboutMe;
    @BindView(R.id.cv_enjoy)
    androidx.cardview.widget.CardView cvEnjoy;
    @BindView(R.id.cv_faq)
    CardView cvFaq;

    @SuppressWarnings("FieldCanBeLocal")
    private CreditsAdapter creditsAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, view);

        setupVersion();
        setupCreditsRecycler();

        cvAboutMe.setOnClickListener(v -> openLink(requireContext(), "https://github.com/ForceTower/UEFS_Sagres_App"));
        cvEnjoy.setOnClickListener(v -> openLink(requireContext(), "https://facebook.com/ForceTower"));
        cvFaq.setOnClickListener(v -> navigateToFAQ());

        return view;
    }

    private void navigateToFAQ() {
        Fragment fragment = new FAQFragment();
        if (VersionUtils.isLollipop()) {
            fragment.setEnterTransition(new Slide(GravityCompat.getAbsoluteGravity(GravityCompat.END, getResources().getConfiguration().getLayoutDirection())));
            fragment.setAllowEnterTransitionOverlap(false);
            fragment.setAllowReturnTransitionOverlap(false);
        }

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setupVersion() {
        String version = "0.0u";
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {}
        versionInfo.setText(getString(R.string.creator, version));
    }

    private void setupCreditsRecycler() {
        List<CreditsMention> mentions = MockUtils.getCredits();

        creditsAdapter = new CreditsAdapter(mentions);
        creditsAdapter.setOnMentionClickListener(mention -> {
            if (validString(mention.getLink())) openLink(requireContext(), mention.getLink());
        });
        rvCredits.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        rvCredits.setAdapter(creditsAdapter);
        rvCredits.setNestedScrollingEnabled(false);
    }
}
