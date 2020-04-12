package id.my.berviantoleo.ecceg_rsa_app.adapter;

import id.my.berviantoleo.ecceg_rsa_app.fragment.RSADecryptFragment;
import id.my.berviantoleo.ecceg_rsa_app.fragment.RSAEncryptFragment;
import id.my.berviantoleo.ecceg_rsa_app.fragment.RSAGenerateKeyFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Created by bervianto on 3/27/18.
 */

public class RSAPagerAdapter extends FragmentPagerAdapter {

    public RSAPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if (position == 0) {
            return RSAGenerateKeyFragment.newInstance();
        } else if (position == 1) {
            return RSAEncryptFragment.newInstance();
        } else {
            return RSADecryptFragment.newInstance();
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


