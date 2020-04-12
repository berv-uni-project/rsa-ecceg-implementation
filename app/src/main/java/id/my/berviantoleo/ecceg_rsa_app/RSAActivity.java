package id.my.berviantoleo.ecceg_rsa_app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.my.berviantoleo.ecceg_rsa_app.adapter.RSAPagerAdapter;

public class RSAActivity extends AppCompatActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container)
    protected ViewPager mViewPager;
    @BindView(R.id.tabs)
    protected TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsa);
        ButterKnife.bind(this);
        RSAPagerAdapter mSectionsPagerAdapter = new RSAPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout.setupWithViewPager(mViewPager);
    }


}
