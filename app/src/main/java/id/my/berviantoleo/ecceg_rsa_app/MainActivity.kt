package id.my.berviantoleo.ecceg_rsa_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import butterknife.OnClick

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
    }

    @OnClick(R.id.rsa_button)
    fun launchRSA() {
        val intent = Intent(this, RSAActivity::class.java)
        startActivity(intent)
    }

    @OnClick(R.id.ecceg_button)
    fun launchECCEG() {
        val intent = Intent(this, ECCEGActivity::class.java)
        startActivity(intent)
    }
}
