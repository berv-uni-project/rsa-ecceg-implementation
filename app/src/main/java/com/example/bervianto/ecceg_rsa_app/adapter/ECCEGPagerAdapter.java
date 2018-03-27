package com.example.bervianto.ecceg_rsa_app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.bervianto.ecceg_rsa_app.fragment.ECCEGDecryptFragment;
import com.example.bervianto.ecceg_rsa_app.fragment.ECCEGEncryptFragment;
import com.example.bervianto.ecceg_rsa_app.fragment.ECCEGGenerateKeyFragment;
import com.example.bervianto.ecceg_rsa_app.fragment.RSADecryptFragment;
import com.example.bervianto.ecceg_rsa_app.fragment.RSAEncryptFragment;
import com.example.bervianto.ecceg_rsa_app.fragment.RSAGenerateKeyFragment;

/**
 * Created by bervianto on 3/27/18.
 */

public class ECCEGPagerAdapter extends FragmentPagerAdapter {

        public ECCEGPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return ECCEGGenerateKeyFragment.newInstance();
            } else if (position == 1){
                return ECCEGEncryptFragment.newInstance();
            } else if (position == 2) {
                return ECCEGDecryptFragment.newInstance();
            } else {
                return null;
            }
        }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Generate Key";
        } else if (position == 1){
            return "Encrypt";
        } else if (position == 2) {
            return "Decrypt";
        } else {
            return "Unknown";
        }
    }

    @Override
        public int getCount() {
            return 3;
        }
}


