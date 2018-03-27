package com.example.bervianto.ecceg_rsa_app.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bervianto.ecceg_rsa_app.R;
import com.example.bervianto.ecceg_rsa_app.rsa.RSA;
import com.gun0912.tedpermission.PermissionListener;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.cloudist.acplibrary.ACProgressFlower;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RSAEncryptFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RSAEncryptFragment extends Fragment {

    @BindView(R.id.public_key_value)
    TextInputEditText publicKey;

    @BindView(R.id.n_value_encrypt)
    TextInputEditText nModulus;

    @BindView(R.id.file_encrypt_value)
    TextInputEditText encryptLoc;

    @BindView(R.id.file_encrypt_loc_value)
    TextInputEditText encryptLocValue;

    @BindView(R.id.outputValue)
    EditText output;

    private String keyPath;
    private ACProgressFlower loadingView;

    public RSAEncryptFragment() {
        // Required empty public constructor
    }

    public static RSAEncryptFragment newInstance() {
        RSAEncryptFragment fragment = new RSAEncryptFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rsaencrypt, container, false);
        ButterKnife.bind(this, view);
        loadingView = new ACProgressFlower.Builder(getContext()).build();
        loadingView.setCanceledOnTouchOutside(false);
        loadingView.setCancelable(false);
        return view;
    }

    @OnClick(R.id.open_public_button)
    void openPublicKey() {
        new ChooserDialog().with(getActivity())
                .withFilter(false, false, "pub")
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        keyPath = pathFile.getPath();
                        loadingView.show();
                        new RSAEncryptFragment.OpenKey(RSAEncryptFragment.this).execute(keyPath);
                    }
                })
                .build()
                .show();
    }

    @OnClick(R.id.select_encrypt_file_button)
    void openFileEncrypt() {
        new ChooserDialog().with(getActivity())
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        encryptLoc.getText().insert(0,pathFile.getPath());
                    }
                })
                .build()
                .show();
    }

    @OnClick(R.id.encrypt_button)
    void encrypt() {
        if (!encryptLoc.getText().toString().equalsIgnoreCase("") &&
                !encryptLocValue.getText().toString().equalsIgnoreCase("") &&
                !nModulus.getText().toString().equalsIgnoreCase("")
                && !publicKey.getText().toString().equalsIgnoreCase("")) {
            loadingView.show();
            File file = Environment.getExternalStorageDirectory();
            File location = new File(file,"RSA/");
            if (!location.exists()) {
                location.mkdir();
            }
            new RSAEncryptFragment.Encrypt(RSAEncryptFragment.this).execute(encryptLoc.getText().toString(),
                    location.getAbsolutePath()+encryptLocValue.getText().toString(),
                    nModulus.getText().toString(),
                    publicKey.getText().toString());
        }
    }

    private class Encrypt extends AsyncTask<String, Integer, String> {

        private WeakReference<RSAEncryptFragment> activityReference;

        // only retain a weak reference to the activity
        Encrypt(RSAEncryptFragment context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            RSA.encryptedFile(strings[0],strings[1],new BigInteger(strings[3]), new BigInteger(strings[2]));
            return RSA.showHexFromFile(strings[1]);
        }

        @Override
        protected void onPostExecute(String hex) {
            output.setText(hex);
            loadingView.dismiss();
            Toast.makeText(getActivity(),"Finished Encrypt",Toast.LENGTH_SHORT).show();
        }
    }

    private class OpenKey extends AsyncTask<String, Integer, String> {

        private WeakReference<RSAEncryptFragment> activityReference;

        // only retain a weak reference to the activity
        OpenKey(RSAEncryptFragment context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            return RSA.readKey(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            String[] key = s.split(":");
            String N = key[0];
            String pubKey = key[1];
            publicKey.getText().insert(0, pubKey);
            nModulus.getText().insert(0, N);
            loadingView.dismiss();
        }
    }
}
