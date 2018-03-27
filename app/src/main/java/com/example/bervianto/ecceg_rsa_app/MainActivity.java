package com.example.bervianto.ecceg_rsa_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.rsa_button)
    void launchRSA() {
        Intent intent = new Intent(this, RSAActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.ecceg_button)
    void launchECCEG() {
        Intent intent = new Intent(this, ECCEGActivity.class);
        startActivity(intent);
    }
}
