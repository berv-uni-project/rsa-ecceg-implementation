package com.example.bervianto.ecceg_rsa_app;

import android.os.Bundle;

import com.example.bervianto.ecceg_rsa_app.adapter.ECCEGPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ECCEGActivity extends AppCompatActivity {

    @BindView(R.id.container_ecceg)
    protected ViewPager mViewPager;
    @BindView(R.id.tabs_ecceg)
    protected TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecceg);
        ButterKnife.bind(this);
        ECCEGPagerAdapter mSectionsPagerAdapter = new ECCEGPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }
}
