package com.example.bervianto.ecceg_rsa_app;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.bervianto.ecceg_rsa_app.adapter.ECCEGPagerAdapter;
import com.example.bervianto.ecceg_rsa_app.adapter.RSAPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ECCEGActivity extends AppCompatActivity {

    @BindView(R.id.container_ecceg)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecceg);
        ButterKnife.bind(this);
        ECCEGPagerAdapter mSectionsPagerAdapter = new ECCEGPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs_ecceg);
        tabLayout.setupWithViewPager(mViewPager);
    }
}
