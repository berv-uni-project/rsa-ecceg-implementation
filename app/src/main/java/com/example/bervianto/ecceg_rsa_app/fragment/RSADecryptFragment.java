package com.example.bervianto.ecceg_rsa_app.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.bervianto.ecceg_rsa_app.R;
import com.example.bervianto.ecceg_rsa_app.rsa.RSA;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.cloudist.acplibrary.ACProgressFlower;

import static android.os.Environment.getExternalStorageDirectory;


public class RSADecryptFragment extends Fragment {

    @BindView(R.id.private_key_value)
    TextInputEditText privateKey;

    @BindView(R.id.n_value_decrypt)
    TextInputEditText nModulus;

    @BindView(R.id.file_decrypt_value)
    TextInputEditText decryptFileLoc;

    @BindView(R.id.file_decrypt_loc_value)
    TextInputEditText decryptDestination;

    @BindView(R.id.outputValueDecrypt)
    TextInputEditText outputValue;

    @BindView(R.id.InputValueDecrypt)
    TextInputEditText inputValue;

    @BindView(R.id.outputSizeValueDecrypt)
    TextInputEditText outputSize;

    @BindView(R.id.InputSizeValueDecrypt)
    TextInputEditText inputSize;

    @BindView(R.id.timeValueDecrypt)
    TextInputEditText timeElapsedDecrypt;

    private String keyPath;
    private ACProgressFlower loadingView;
    private long startTime;

    public RSADecryptFragment() {
        // Required empty public constructor
    }

    public static RSADecryptFragment newInstance() {
        return new RSADecryptFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rsadecrypt, container, false);
        ButterKnife.bind(this, view);
        loadingView = new ACProgressFlower.Builder(getContext()).build();
        loadingView.setCanceledOnTouchOutside(false);
        loadingView.setCancelable(false);
        return view;
    }

    @OnClick(R.id.open_private_button)
    void openPrivateKey() {
        new ChooserDialog().with(getActivity())
                .withFilter(false, false, "pri")
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        keyPath = pathFile.getPath();
                        loadingView.show();
                        new RSADecryptFragment.OpenKey(RSADecryptFragment.this).execute(keyPath);
                    }
                })
                .build()
                .show();
    }

    @OnClick(R.id.select_decrypt_file_button)
    void openFileDecrypt() {
        new ChooserDialog().with(getActivity())
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        decryptFileLoc.setText(pathFile.getPath());
                        inputSize.setText(String.valueOf(pathFile.length()));
                        loadingView.show();
                        new RSADecryptFragment.SetInput(RSADecryptFragment.this).execute(pathFile.getPath());
                    }
                })
                .build()
                .show();
    }

    @OnClick(R.id.decrypt_button)
    void decrypt() {
        if (!decryptFileLoc.getText().toString().equalsIgnoreCase("") &&
                !decryptDestination.getText().toString().equalsIgnoreCase("") &&
                !nModulus.getText().toString().equalsIgnoreCase("")
                && !privateKey.getText().toString().equalsIgnoreCase("")) {
            loadingView.show();
            File file = Environment.getExternalStorageDirectory();
            File location = new File(file,"RSA/");
            if (!location.exists()) {
                location.mkdir();
            }
            new RSADecryptFragment.Decrypt(RSADecryptFragment.this).execute(decryptFileLoc.getText().toString(),
                    location.getAbsolutePath()+"/"+decryptDestination.getText().toString(),
                    nModulus.getText().toString(),
                    privateKey.getText().toString());
        }
    }

    private class Decrypt extends AsyncTask<String, Integer, String> {

        private WeakReference<RSADecryptFragment> activityReference;

        // only retain a weak reference to the activity
        Decrypt(RSADecryptFragment context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            startTime = System.currentTimeMillis();
            RSA.decryptFile(strings[0],strings[1],new BigInteger(strings[3]), new BigInteger(strings[2]));
            byte[] bytes = RSA.getBytes(strings[1]);
            if (bytes != null)
                return new String(bytes);
            else
                return "";
        }

        @Override
        protected void onPostExecute(String hex) {
            long endTime = System.currentTimeMillis();
            timeElapsedDecrypt.setText(String.valueOf(endTime-startTime));
            outputValue.setText(hex);
            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/RSA/"+decryptDestination.getText().toString());
            outputSize.setText(String.valueOf(file.length()));
            loadingView.dismiss();
            Toast.makeText(getActivity(),"Finished Decrypt", Toast.LENGTH_SHORT).show();
        }
    }

    private class OpenKey extends AsyncTask<String, Integer, String> {

        private WeakReference<RSADecryptFragment> activityReference;

        // only retain a weak reference to the activity
        OpenKey(RSADecryptFragment context) {
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
            privateKey.setText(pubKey);
            nModulus.setText(N);
            loadingView.dismiss();
        }
    }

    private class SetInput extends AsyncTask<String, Integer, String> {

        private WeakReference<RSADecryptFragment> activityReference;

        // only retain a weak reference to the activity
        SetInput(RSADecryptFragment context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length == 1) {
                return RSA.showHexFromFile(strings[0]);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            inputValue.setText(s);
            loadingView.dismiss();
        }
    }

}
