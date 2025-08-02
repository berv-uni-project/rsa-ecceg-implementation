package id.my.berviantoleo.ecceg_rsa_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.my.berviantoleo.ecceg_rsa_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rsaButton.setOnClickListener {
            launchRSA()
        }

        binding.eccegButton.setOnClickListener {
            launchECCEG()
        }
    }

    private fun launchRSA() {
        val intent = Intent(this, RSAActivity::class.java)
        startActivity(intent)
    }

    private fun launchECCEG() {
        val intent = Intent(this, ECCEGActivity::class.java)
        startActivity(intent)
    }
}
