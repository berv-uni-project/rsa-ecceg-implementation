package id.my.berviantoleo.ecceg_rsa_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import id.my.berviantoleo.ecceg_rsa_app.adapter.ECCEGPagerAdapter

class ECCEGActivity : AppCompatActivity() {
    @JvmField
    @BindView(R.id.container_ecceg)
    var mViewPager: ViewPager? = null

    @JvmField
    @BindView(R.id.tabs_ecceg)
    var tabLayout: TabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ecceg)
        ButterKnife.bind(this)
        val mSectionsPagerAdapter = ECCEGPagerAdapter(supportFragmentManager)
        mViewPager!!.adapter = mSectionsPagerAdapter
        tabLayout!!.setupWithViewPager(mViewPager)
    }
}
