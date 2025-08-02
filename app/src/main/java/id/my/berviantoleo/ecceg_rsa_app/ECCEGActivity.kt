package id.my.berviantoleo.ecceg_rsa_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
// import butterknife.BindView // Removed ButterKnife import
// import butterknife.ButterKnife // Removed ButterKnife import
import com.google.android.material.tabs.TabLayout
import id.my.berviantoleo.ecceg_rsa_app.adapter.ECCEGPagerAdapter
import id.my.berviantoleo.ecceg_rsa_app.databinding.ActivityEccegBinding // Import ViewBinding class

class ECCEGActivity : AppCompatActivity() {
    // @JvmField
    // @BindView(R.id.container_ecceg) // Removed ButterKnife annotation
    // var mViewPager: ViewPager? = null

    // @JvmField
    // @BindView(R.id.tabs_ecceg) // Removed ButterKnife annotation
    // var tabLayout: TabLayout? = null

    private lateinit var binding: ActivityEccegBinding // Declare binding variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEccegBinding.inflate(layoutInflater) // Inflate layout
        setContentView(binding.root) // Set content view to binding's root
        // ButterKnife.bind(this) // Removed ButterKnife bind call

        val mSectionsPagerAdapter = ECCEGPagerAdapter(supportFragmentManager)
        // Access views via binding object. Note: ViewPager and TabLayout are non-null from binding
        binding.containerEcceg.adapter = mSectionsPagerAdapter
        binding.tabsEcceg.setupWithViewPager(binding.containerEcceg)
    }
}
