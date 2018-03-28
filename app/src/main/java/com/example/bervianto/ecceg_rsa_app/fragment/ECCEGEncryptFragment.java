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
import com.example.bervianto.ecceg_rsa_app.ecc.ECC;
import com.example.bervianto.ecceg_rsa_app.ecc.ECCEG;
import com.example.bervianto.ecceg_rsa_app.ecc.Pair;
import com.example.bervianto.ecceg_rsa_app.ecc.Point;
import com.example.bervianto.ecceg_rsa_app.rsa.RSA;
import com.example.bervianto.ecceg_rsa_app.utils.FileUtils;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.List;

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
    TextInputEditText publicKeyLoc;

    @BindView(R.id.plain_text_loc_value_ecceg)
    TextInputEditText plainTextLoc;

    @BindView(R.id.cipher_text_loc_ecceg)
    TextInputEditText cipherTextLoc;

    @BindView(R.id.input_file_encrypt_ecceg)
    TextInputEditText inputContent;

    @BindView(R.id.output_file_encrypt_ecceg)
    TextInputEditText outputContent;

    @BindView(R.id.input_file_size_encrypt_ecceg)
    TextInputEditText inputSize;

    @BindView(R.id.output_file_size_encrypt_ecceg)
    TextInputEditText outputSize;

    @BindView(R.id.time_encrypt_ecceg)
    TextInputEditText timeElapsed;

    @BindView(R.id.a_encrypt_ecceg_value)
    TextInputEditText a;

    @BindView(R.id.b_encrypt_ecceg_value)
    TextInputEditText b;

    @BindView(R.id.p_encrypt_ecceg_value)
    TextInputEditText p;

    @BindView(R.id.k_encrypt_ecceg_value)
    TextInputEditText k;

    private ACProgressFlower loadingView;
    private long startTime;
    private long endTime;

    public ECCEGEncryptFragment() {
        // Required empty public constructor
    }

    public static ECCEGEncryptFragment newInstance() {
        ECCEGEncryptFragment fragment = new ECCEGEncryptFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
        if (!publicKeyLoc.getText().toString().equalsIgnoreCase("") &&
                !plainTextLoc.getText().toString().equalsIgnoreCase("") &&
                !cipherTextLoc.getText().toString().equalsIgnoreCase("") &&
                !a.getText().toString().equalsIgnoreCase("") &&
                !b.getText().toString().equalsIgnoreCase("") &&
                !p.getText().toString().equalsIgnoreCase("")) {
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
                    k.getText().toString(),
                    publicKeyLoc.getText().toString(),
                    plainTextLoc.getText().toString(),
                    cipherLoc
                    );
        }
    }

    @OnClick(R.id.search_public_key_ecceg)
    void openPublicKey() {
        new ChooserDialog().with(getActivity())
                .withFilter(false, false, "pub")
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        publicKeyLoc.setText(pathFile.getPath());
                    }
                })
                .build()
                .show();
    }

    @OnClick(R.id.search_plain_text_ecceg)
    void searchPlainText() {
        new ChooserDialog().with(getActivity())
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        plainTextLoc.setText(pathFile.getPath());
                        inputSize.setText(String.valueOf(pathFile.length()));
                        loadingView.show();
                        new ECCEGEncryptFragment.SetInput(ECCEGEncryptFragment.this).execute(pathFile.getPath());
                    }
                })
                .build()
                .show();
    }

    private class Encrypt extends AsyncTask<String, Integer, String> {

        private WeakReference<ECCEGEncryptFragment> activityReference;

        // only retain a weak reference to the activity
        Encrypt(ECCEGEncryptFragment context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            startTime = System.currentTimeMillis();
            try {
                ECC ecc = new ECC();
                ecc.a = new BigInteger(strings[0]);
                ecc.b = new BigInteger(strings[1]);
                ecc.p = new BigInteger(strings[2]);
                ecc.k = new BigInteger(strings[3]);
                ECCEG ecceg = new ECCEG(ecc, ecc.getBasePoint());
                ecceg.loadPublicKey(strings[4]);
                byte[] read = FileUtils.getBytes(strings[5]);
                List<Pair<Point,Point>> enc = ecceg.encryptBytes(read);
                FileUtils.savePointsToFile(strings[6], enc);
                endTime = System.currentTimeMillis();
                return FileUtils.showHexFromFile(strings[6]);
            } catch (Exception e) {
                return "failed";
            }
        }

        @Override
        protected void onPostExecute(String hex) {
            timeElapsed.setText(String.valueOf(endTime-startTime));
            outputContent.setText(hex);
            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/ECCEG/"+cipherTextLoc.getText().toString());
            outputSize.setText(String.valueOf(file.length()));
            loadingView.dismiss();
            Toast.makeText(getActivity(),"Finished Encrypt",Toast.LENGTH_SHORT).show();
        }
    }

    private class SetInput extends AsyncTask<String, Integer, String> {

        private WeakReference<ECCEGEncryptFragment> activityReference;

        // only retain a weak reference to the activity
        SetInput(ECCEGEncryptFragment context) {
            activityReference = new WeakReference<>(context);
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
