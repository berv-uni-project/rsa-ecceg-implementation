package com.example.bervianto.ecceg_rsa_app.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.bervianto.ecceg_rsa_app.R;
import com.example.bervianto.ecceg_rsa_app.rsa.RSA;
import com.google.android.material.textfield.TextInputEditText;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.cloudist.acplibrary.ACProgressFlower;

import static android.os.Environment.getExternalStorageDirectory;


public class RSADecryptFragment extends Fragment {

    @BindView(R.id.private_key_value)
    protected TextInputEditText privateKey;
    @BindView(R.id.n_value_decrypt)
    protected TextInputEditText nModulus;
    @BindView(R.id.file_decrypt_value)
    protected TextInputEditText decryptFileLoc;
    @BindView(R.id.file_decrypt_loc_value)
    protected TextInputEditText decryptDestination;
    @BindView(R.id.outputValueDecrypt)
    protected TextInputEditText outputValue;
    @BindView(R.id.InputValueDecrypt)
    protected TextInputEditText inputValue;
    @BindView(R.id.outputSizeValueDecrypt)
    protected TextInputEditText outputSize;
    @BindView(R.id.InputSizeValueDecrypt)
    protected TextInputEditText inputSize;
    @BindView(R.id.timeValueDecrypt)
    protected TextInputEditText timeElapsedDecrypt;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
        new ChooserDialog(getActivity())
                .withFilter(false, false, "pri")
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener((path, pathFile) -> {
                    keyPath = pathFile.getPath();
                    loadingView.show();
                    new OpenKey(RSADecryptFragment.this).execute(keyPath);
                })
                .build()
                .show();
    }

    @OnClick(R.id.select_decrypt_file_button)
    void openFileDecrypt() {
        new ChooserDialog(getActivity())
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener((path, pathFile) -> {
                    decryptFileLoc.setText(pathFile.getPath());
                    inputSize.setText(String.valueOf(pathFile.length()));
                    loadingView.show();
                    new SetInput(RSADecryptFragment.this).execute(pathFile.getPath());
                })
                .build()
                .show();
    }

    @OnClick(R.id.decrypt_button)
    void decrypt() {
        if (!Objects.requireNonNull(decryptFileLoc.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(decryptDestination.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(nModulus.getText()).toString().equalsIgnoreCase("")
                && !Objects.requireNonNull(privateKey.getText()).toString().equalsIgnoreCase("")) {
            loadingView.show();
            File file = Environment.getExternalStorageDirectory();
            File location = new File(file, "RSA/");
            if (!location.exists()) {
                location.mkdir();
            }
            new RSADecryptFragment.Decrypt(RSADecryptFragment.this).execute(decryptFileLoc.getText().toString(),
                    location.getAbsolutePath() + "/" + decryptDestination.getText().toString(),
                    nModulus.getText().toString(),
                    privateKey.getText().toString());
        }
    }

    private class Decrypt extends AsyncTask<String, Integer, String> {

        // only retain a weak reference to the activity
        Decrypt(RSADecryptFragment context) {
            new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            startTime = System.currentTimeMillis();
            RSA.decryptFile(strings[0], strings[1], new BigInteger(strings[3]), new BigInteger(strings[2]));
            byte[] bytes = RSA.getBytes(strings[1]);
            if (bytes != null)
                return new String(bytes);
            else
                return "";
        }

        @Override
        protected void onPostExecute(String hex) {
            long endTime = System.currentTimeMillis();
            timeElapsedDecrypt.setText(String.valueOf(endTime - startTime));
            outputValue.setText(hex);
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/RSA/" + decryptDestination.getText().toString());
            outputSize.setText(String.valueOf(file.length()));
            loadingView.dismiss();
            Toast.makeText(getActivity(), "Finished Decrypt", Toast.LENGTH_SHORT).show();
        }
    }

    private class OpenKey extends AsyncTask<String, Integer, String> {

        // only retain a weak reference to the activity
        OpenKey(RSADecryptFragment context) {
            new WeakReference<>(context);
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

        // only retain a weak reference to the activity
        SetInput(RSADecryptFragment context) {
            new WeakReference<>(context);
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
