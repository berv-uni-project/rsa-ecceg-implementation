package com.example.bervianto.ecceg_rsa_app.fragment;

import android.Manifest;
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
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.cloudist.acplibrary.ACProgressFlower;


public class RSAGenerateKeyFragment extends Fragment {

    ACProgressFlower loadingView;

    @BindView(R.id.public_location_save)
    TextInputEditText publicLocation;

    @BindView(R.id.private_location_save)
    TextInputEditText privateLocation;

    @BindView(R.id.byte_size)
    TextInputEditText byteSize;
    private PermissionListener extract;

    public RSAGenerateKeyFragment() {
        // Required empty public constructor
    }

    public static RSAGenerateKeyFragment newInstance() {
        return new RSAGenerateKeyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rsagenerate_key, container, false);
        ButterKnife.bind(this, view);
        loadingView = new ACProgressFlower.Builder(getContext()).build();
        loadingView.setCanceledOnTouchOutside(false);
        loadingView.setCancelable(false);
        extract = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                File location = Environment.getExternalStorageDirectory();
                File newLocation = new File(location, "RSA/");
                if (!newLocation.exists()) {
                    newLocation.mkdir();
                }
                loadingView.show();
                new GenerateKey(RSAGenerateKeyFragment.this).execute(byteSize.getText().toString(), newLocation.getAbsolutePath() +"/"+ privateLocation.getText().toString(),
                        newLocation.getAbsolutePath()+"/"+publicLocation.getText().toString());
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(getContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        return view;
    }

    @OnClick(R.id.generate_button)
    void generateKey() {
        if (!byteSize.getText().toString().equalsIgnoreCase("") && !privateLocation.getText().toString().equalsIgnoreCase("")  && !publicLocation.getText().toString().equalsIgnoreCase("")) {
            if (Integer.valueOf(byteSize.getText().toString()) >= 1024) {
                TedPermission.with(getContext())
                        .setPermissionListener(extract)
                        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();
            }
        }
    }

    private class GenerateKey extends AsyncTask<String, Integer, Void> {

        private WeakReference<RSAGenerateKeyFragment> activityReference;

        // only retain a weak reference to the activity
        GenerateKey(RSAGenerateKeyFragment context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(String... strings) {
            int byteSize = Integer.valueOf(strings[0]);
            RSA.generateKey(byteSize,strings[1], strings[2]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            RSAGenerateKeyFragment activity = activityReference.get();
            if (activity == null) return;
            activity.loadingView.dismiss();
            Toast.makeText(getContext(),"Finished", Toast.LENGTH_SHORT).show();
        }
    }

}
