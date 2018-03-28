package com.example.bervianto.ecceg_rsa_app.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
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


public class ECCEGDecryptFragment extends Fragment {

    @BindView(R.id.private_key_loc_ecceg_value)
    TextInputEditText privateKeyLoc;

    @BindView(R.id.cipher_decrypt_text_loc_value_ecceg)
    TextInputEditText cipherTextLoc;

    @BindView(R.id.decrypt_text_loc_ecceg)
    TextInputEditText decryptTextLoc;

    @BindView(R.id.input_file_decrypt_ecceg)
    TextInputEditText inputContent;

    @BindView(R.id.output_file_decrypt_ecceg)
    TextInputEditText outputContent;

    @BindView(R.id.input_file_size_decrypt_ecceg)
    TextInputEditText inputSize;

    @BindView(R.id.output_file_size_decrypt_ecceg)
    TextInputEditText outputSize;

    @BindView(R.id.time_decrypt_ecceg)
    TextInputEditText timeElapsed;

    @BindView(R.id.a_decrypt_ecceg_value)
    TextInputEditText a;

    @BindView(R.id.b_decrypt_ecceg_value)
    TextInputEditText b;

    @BindView(R.id.p_decrypt_ecceg_value)
    TextInputEditText p;

    private ACProgressFlower loadingView;
    private long startTime;
    private long endTime;

    public ECCEGDecryptFragment() {
        // Required empty public constructor
    }

    public static ECCEGDecryptFragment newInstance() {
        ECCEGDecryptFragment fragment = new ECCEGDecryptFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_eccegdecrypt, container, false);
        ButterKnife.bind(this, view);
        loadingView = new ACProgressFlower.Builder(getContext()).build();
        loadingView.setCanceledOnTouchOutside(false);
        loadingView.setCancelable(false);
        return view;
    }

    @OnClick(R.id.decrypt_button_ecceg)
    void decrypt() {
        if (!privateKeyLoc.getText().toString().equalsIgnoreCase("") &&
                !decryptTextLoc.getText().toString().equalsIgnoreCase("") &&
                !cipherTextLoc.getText().toString().equalsIgnoreCase("") &&
                !a.getText().toString().equalsIgnoreCase("") &&
                !b.getText().toString().equalsIgnoreCase("") &&
                !p.getText().toString().equalsIgnoreCase("")) {
            File file = Environment.getExternalStorageDirectory();
            File location = new File(file, "ECCEG/");
            if (!location.exists()) {
                location.mkdir();
            }
            String decryptLoc = location.getAbsolutePath() + "/" + decryptTextLoc.getText().toString();
            new ECCEGDecryptFragment.Decrypt(ECCEGDecryptFragment.this).execute(
                    a.getText().toString(),
                    b.getText().toString(),
                    p.getText().toString(),
                    privateKeyLoc.getText().toString(),
                    cipherTextLoc.getText().toString(),
                    decryptLoc
            );
        }
    }

    @OnClick(R.id.search_private_key_ecceg)
    void openPrivateKey() {
        new ChooserDialog().with(getActivity())
                .withFilter(false, false, "pri")
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        privateKeyLoc.setText(pathFile.getPath());
                    }
                })
                .build()
                .show();
    }

    @OnClick(R.id.search_cipher_text_ecceg)
    void searchCipherText() {
        new ChooserDialog().with(getActivity())
                .withStartFile(getExternalStorageDirectory().getAbsolutePath())
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        cipherTextLoc.setText(pathFile.getPath());
                        inputSize.setText(String.valueOf(pathFile.length()));
                        loadingView.show();
                        new ECCEGDecryptFragment.SetInput(ECCEGDecryptFragment.this).execute(pathFile.getPath());
                    }
                })
                .build()
                .show();
    }

    private class Decrypt extends AsyncTask<String, Integer, String> {

        private WeakReference<ECCEGDecryptFragment> activityReference;

        // only retain a weak reference to the activity
        Decrypt(ECCEGDecryptFragment context) {
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
                ecc.k = BigInteger.valueOf(30);
                ECCEG ecceg = new ECCEG(ecc, ecc.getBasePoint());
                ecceg.loadPrivateKey(strings[3]);
                List<Pair<Point,Point>> read_enc = FileUtils.loadPointsFromFile(strings[4]);
                List<Point> read_dec = ecceg.decrypt(read_enc);
                StringBuilder stringBuilder = new StringBuilder();
                for (Point pp: read_dec)
                    stringBuilder.append((char)ecc.pointToInt(pp).byteValue());
                endTime = System.currentTimeMillis();
                FileUtils.saveFile(strings[5],stringBuilder.toString().getBytes());
                return stringBuilder.toString();
            } catch (Exception e) {
                Log.e("Decrypt", e.getMessage());
                return "failed";
            }
        }

        @Override
        protected void onPostExecute(String hex) {
            timeElapsed.setText(String.valueOf(endTime-startTime));
            outputContent.setText(hex);
            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/ECCEG/"+decryptTextLoc.getText().toString());
            outputSize.setText(String.valueOf(file.length()));
            loadingView.dismiss();
            Toast.makeText(getActivity(),"Finished Encrypt",Toast.LENGTH_SHORT).show();
        }
    }

    private class SetInput extends AsyncTask<String, Integer, String> {

        private WeakReference<ECCEGDecryptFragment> activityReference;

        // only retain a weak reference to the activity
        SetInput(ECCEGDecryptFragment context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length == 1) {
                return FileUtils.showHexFromFile(strings[0]);
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
