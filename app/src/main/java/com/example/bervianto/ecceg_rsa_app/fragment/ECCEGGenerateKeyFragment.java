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
import com.example.bervianto.ecceg_rsa_app.ecc.ECCEGMain;
import com.google.android.material.textfield.TextInputEditText;

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


public class ECCEGGenerateKeyFragment extends Fragment {


    @BindView(R.id.eccegA)
    protected TextInputEditText a;
    @BindView(R.id.eccegB)
    protected TextInputEditText b;
    @BindView(R.id.eccegP)
    protected TextInputEditText p;
    @BindView(R.id.eccegPrivatePath)
    protected TextInputEditText pri;
    @BindView(R.id.eccegPubPath)
    protected TextInputEditText pub;
    private ACProgressFlower loadingView;

    public ECCEGGenerateKeyFragment() {
        // Required empty public constructor
    }

    public static ECCEGGenerateKeyFragment newInstance() {
        return new ECCEGGenerateKeyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ecceggenerate_key, container, false);
        ButterKnife.bind(this, view);
        loadingView = new ACProgressFlower.Builder(getContext()).build();
        loadingView.setCanceledOnTouchOutside(false);
        loadingView.setCancelable(false);
        return view;
    }

    @OnClick(R.id.generate_key_ecceg)
    void generateKeyECCEG() {
        if (!Objects.requireNonNull(a.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(b.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(p.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(pub.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(pri.getText()).toString().equalsIgnoreCase("")) {
            loadingView.show();
            new ECCEGGenerateKeyFragment.GenerateKey(ECCEGGenerateKeyFragment.this).execute(
                    a.getText().toString(),
                    b.getText().toString(),
                    p.getText().toString(),
                    pub.getText().toString(),
                    pri.getText().toString()
            );
        }
    }

    private class GenerateKey extends AsyncTask<String, Integer, String> {

        private WeakReference<ECCEGGenerateKeyFragment> activityReference;

        // only retain a weak reference to the activity
        GenerateKey(ECCEGGenerateKeyFragment context) {
            activityReference = new WeakReference<>(context);
        }


        @Override
        protected String doInBackground(String... strings) {
            BigInteger a = new BigInteger(strings[0]);
            BigInteger b = new BigInteger(strings[1]);
            BigInteger p = new BigInteger(strings[2]);
            ECC ecc = new ECC();
            ecc.a = a;
            ecc.b = b;
            ecc.p = p;
            File file = Environment.getExternalStorageDirectory();
            File newDir = new File(file, "/ECCEG/");
            if (!newDir.exists()) {
                newDir.mkdir();
            }
            String privateLocation = newDir.getAbsolutePath() + "/" + strings[4];
            String publicLocation = newDir.getAbsolutePath() + "/" + strings[3];
            try {
                ECCEGMain.generateKey(ecc, privateLocation, publicLocation);
                return "finished";
            } catch (Exception e) {
                return "exception";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equalsIgnoreCase("finished")) {
                loadingView.dismiss();
                Toast.makeText(getActivity(), "Finished", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
