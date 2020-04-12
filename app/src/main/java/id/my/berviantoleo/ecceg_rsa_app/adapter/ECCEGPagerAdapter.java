package id.my.berviantoleo.ecceg_rsa_app.adapter;

import id.my.berviantoleo.ecceg_rsa_app.fragment.ECCEGDecryptFragment;
import id.my.berviantoleo.ecceg_rsa_app.fragment.ECCEGEncryptFragment;
import id.my.berviantoleo.ecceg_rsa_app.fragment.ECCEGGenerateKeyFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Created by bervianto on 3/27/18.
 */

public class ECCEGPagerAdapter extends FragmentPagerAdapter {

    public ECCEGPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 0) {
            return ECCEGGenerateKeyFragment.newInstance();
        } else if (position == 1) {
            return ECCEGEncryptFragment.newInstance();
        } else {
            return ECCEGDecryptFragment.newInstance();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Generate Key";
        } else if (position == 1) {
            return "Encrypt";
        } else {
            return "Decrypt";
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}


