package id.my.berviantoleo.ecceg_rsa_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2 // Import ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator // Import TabLayoutMediator
import id.my.berviantoleo.ecceg_rsa_app.adapter.RSAPagerAdapter
import id.my.berviantoleo.ecceg_rsa_app.databinding.ActivityRsaBinding

class RSAActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRsaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRsaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // The adapter now takes the Activity as an argument
        val sectionsPagerAdapter = RSAPagerAdapter(this)
        
        // The container view is now a ViewPager2
        val viewPager: ViewPager2 = binding.container
        viewPager.adapter = sectionsPagerAdapter

        // Setup TabLayout with ViewPager2 using TabLayoutMediator
        val tabs: TabLayout = binding.tabs
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Generate Key"
                1 -> "Encrypt"
                else -> "Decrypt" // Position 2 or any other
            }
        }.attach()
    }
}
