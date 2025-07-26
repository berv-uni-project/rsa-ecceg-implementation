package id.my.berviantoleo.ecceg_rsa_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import id.my.berviantoleo.ecceg_rsa_app.adapter.RSAPagerAdapter

class RSAActivity : AppCompatActivity() {
    /**
     * The [ViewPager] that will host the section contents.
     */
    @BindView(R.id.container)
    var mViewPager: ViewPager? = null

    @BindView(R.id.tabs)
    var tabLayout: TabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rsa)
        ButterKnife.bind(this)
        val mSectionsPagerAdapter = RSAPagerAdapter(supportFragmentManager)
        mViewPager!!.adapter = mSectionsPagerAdapter
        tabLayout!!.setupWithViewPager(mViewPager)
    }
}
