package com.example.bervianto.ecceg_rsa_app.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.bervianto.ecceg_rsa_app.R;
import com.example.bervianto.ecceg_rsa_app.ecc.ECC;
import com.example.bervianto.ecceg_rsa_app.ecc.ECCEG;
import com.example.bervianto.ecceg_rsa_app.ecc.Pair;
import com.example.bervianto.ecceg_rsa_app.ecc.Point;
import com.example.bervianto.ecceg_rsa_app.rsa.RSA;
import com.example.bervianto.ecceg_rsa_app.utils.FileUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.cloudist.acplibrary.ACProgressFlower;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ECCEGEncryptFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ECCEGEncryptFragment extends Fragment {

    @BindView(R.id.public_key_loc_ecceg_value)
    protected TextInputEditText publicKeyLoc;

    @BindView(R.id.plain_text_loc_value_ecceg)
    protected TextInputEditText plainTextLoc;

    @BindView(R.id.cipher_text_loc_ecceg)
    protected TextInputEditText cipherTextLoc;

    @BindView(R.id.input_file_encrypt_ecceg)
    protected TextInputEditText inputContent;

    @BindView(R.id.output_file_encrypt_ecceg)
    protected TextInputEditText outputContent;

    @BindView(R.id.input_file_size_encrypt_ecceg)
    protected TextInputEditText inputSize;

    @BindView(R.id.output_file_size_encrypt_ecceg)
    protected TextInputEditText outputSize;

    @BindView(R.id.time_encrypt_ecceg)
    protected TextInputEditText timeElapsed;

    @BindView(R.id.a_encrypt_ecceg_value)
    protected TextInputEditText a;

    @BindView(R.id.b_encrypt_ecceg_value)
    protected TextInputEditText b;

    @BindView(R.id.p_encrypt_ecceg_value)
    protected TextInputEditText p;

    private ACProgressFlower loadingView;
    private long startTime;
    private long endTime;

    public ECCEGEncryptFragment() {
        // Required empty public constructor
    }

    public static ECCEGEncryptFragment newInstance() {
        return new ECCEGEncryptFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_eccegencrypt, container, false);
        ButterKnife.bind(this, view);
        loadingView = new ACProgressFlower.Builder(getContext()).build();
        loadingView.setCanceledOnTouchOutside(false);
        loadingView.setCancelable(false);
        return view;
    }

    @OnClick(R.id.encrypt_button_ecceg)
    void encrypt() {
        if (!Objects.requireNonNull(publicKeyLoc.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(plainTextLoc.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(cipherTextLoc.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(a.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(b.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(p.getText()).toString().equalsIgnoreCase("")) {
            File file = Environment.getExternalStorageDirectory();
            File location = new File(file, "ECCEG/");
            if (!location.exists()) {
                location.mkdir();
            }
            String cipherLoc = location.getAbsolutePath() + "/" + cipherTextLoc.getText().toString();
            new ECCEGEncryptFragment.Encrypt(ECCEGEncryptFragment.this).execute(
                    a.getText().toString(),
                    b.getText().toString(),
                    p.getText().toString(),
                    publicKeyLoc.getText().toString(),
                    plainTextLoc.getText().toString(),
                    cipherLoc
            );
        }
    }

    @OnClick(R.id.search_public_key_ecceg)
    void openPublicKey() {
        new ChooserDialog(getActivity())
                .withFilter(false, false, "pub")
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener((path, pathFile) -> publicKeyLoc.setText(pathFile.getPath()))
                .build()
                .show();
    }

    @OnClick(R.id.search_plain_text_ecceg)
    void searchPlainText() {
        new ChooserDialog(getActivity())
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener((path, pathFile) -> {
                    plainTextLoc.setText(pathFile.getPath());
                    inputSize.setText(String.valueOf(pathFile.length()));
                    loadingView.show();
                    new SetInput(ECCEGEncryptFragment.this).execute(pathFile.getPath());
                })
                .build()
                .show();
    }

    private class Encrypt extends AsyncTask<String, Integer, String> {

        // only retain a weak reference to the activity
        Encrypt(ECCEGEncryptFragment context) {
            new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            startTime = System.currentTimeMillis();
            try {
                ECC ecc = new ECC();
                ecc.a = new BigInteger(strings[0]);
                ecc.b = new BigInteger(strings[1]);
                ecc.p = new BigInteger(strings[2]);
                ecc.k = BigInteger.valueOf(30);
                ECCEG ecceg = new ECCEG(ecc, ecc.getBasePoint());
                ecceg.loadPublicKey(strings[3]);
                byte[] read = FileUtils.getBytes(strings[4]);
                List<Pair<Point, Point>> enc = ecceg.encryptBytes(read);
                FileUtils.savePointsToFile(strings[5], enc);
                endTime = System.currentTimeMillis();
                return FileUtils.showHexFromFile(strings[5]);
            } catch (Exception e) {
                return "failed";
            }
        }

        @Override
        protected void onPostExecute(String hex) {
            timeElapsed.setText(String.valueOf(endTime - startTime));
            outputContent.setText(hex);
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/ECCEG/" + Objects.requireNonNull(cipherTextLoc.getText()).toString());
            outputSize.setText(String.valueOf(file.length()));
            loadingView.dismiss();
            Toast.makeText(getActivity(), "Finished Encrypt", Toast.LENGTH_SHORT).show();
        }
    }

    private class SetInput extends AsyncTask<String, Integer, String> {

        // only retain a weak reference to the activity
        SetInput(ECCEGEncryptFragment context) {
            new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length == 1) {
                byte[] bytes = RSA.getBytes(strings[0]);
                if (bytes != null) {
                    return new String(bytes);
                }
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            inputContent.setText(s);
            loadingView.dismiss();
        }
    }

}
