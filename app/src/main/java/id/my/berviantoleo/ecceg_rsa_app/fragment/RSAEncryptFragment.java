package id.my.berviantoleo.ecceg_rsa_app.fragment;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import id.my.berviantoleo.ecceg_rsa_app.R;
import id.my.berviantoleo.ecceg_rsa_app.lib.rsa.RSA;
import com.google.android.material.textfield.TextInputEditText;
import com.nareshchocha.filepickerlibrary.FilePickerResultContracts;
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig;
import com.nareshchocha.filepickerlibrary.models.FilePickerResult;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Objects;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RSAEncryptFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RSAEncryptFragment extends Fragment {


    @BindView(R.id.public_key_value)
    protected TextInputEditText publicKey;
    @BindView(R.id.n_value_encrypt)
    protected TextInputEditText nModulus;
    @BindView(R.id.file_encrypt_value)
    protected TextInputEditText encryptLoc;
    @BindView(R.id.file_encrypt_loc_value)
    protected TextInputEditText encryptLocValue;
    @BindView(R.id.outputValue)
    protected TextInputEditText outputValue;
    @BindView(R.id.InputValue)
    protected TextInputEditText inputValue;
    @BindView(R.id.InputSizeValue)
    protected TextInputEditText inputSize;
    @BindView(R.id.outputSizeValue)
    protected TextInputEditText outputSize;
    @BindView(R.id.timeValue)
    protected TextInputEditText timeValue;
    private String keyPath;
    private AlertDialog dialog;
    private long startTime;

    public RSAEncryptFragment() {
        // Required empty public constructor
    }

    public static RSAEncryptFragment newInstance() {
        return new RSAEncryptFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rsaencrypt, container, false);
        ButterKnife.bind(this, view);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setCancelable(false); // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog);
        dialog = builder.create();
        return view;
    }

    @OnClick(R.id.open_public_button)
    void openPublicKey() {
        ActivityResultLauncher<DocumentFilePickerConfig> launcher = registerForActivityResult(new FilePickerResultContracts.PickDocumentFile(), (ActivityResultCallback<FilePickerResult>) result -> {
            String errorMessage = result.getErrorMessage();
            if (errorMessage != null) {
                Log.e("Picker", errorMessage);
            } else {
                keyPath = result.getSelectedFilePath();
                dialog.show();
                new OpenKey(RSAEncryptFragment.this).execute(keyPath);
            }
        });
        DocumentFilePickerConfig config = new DocumentFilePickerConfig();
        launcher.launch(config);
    }

    @OnClick(R.id.select_encrypt_file_button)
    void openFileEncrypt() {
        ActivityResultLauncher<DocumentFilePickerConfig> launcher = registerForActivityResult(new FilePickerResultContracts.PickDocumentFile(), (ActivityResultCallback<FilePickerResult>) result -> {
            String errorMessage = result.getErrorMessage();
            if (errorMessage != null) {
                Log.e("Picker", errorMessage);
            } else {
                String path = result.getSelectedFilePath();
                if (path == null) return;
                encryptLoc.setText(path);
                inputSize.setText(String.valueOf(path.length()));
                dialog.show();
                new SetInput(RSAEncryptFragment.this).execute(path);
            }
        });
        DocumentFilePickerConfig config = new DocumentFilePickerConfig();
        launcher.launch(config);
    }

    @OnClick(R.id.encrypt_button)
    void encrypt() {
        if (!Objects.requireNonNull(encryptLoc.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(encryptLocValue.getText()).toString().equalsIgnoreCase("") &&
                !Objects.requireNonNull(nModulus.getText()).toString().equalsIgnoreCase("")
                && !Objects.requireNonNull(publicKey.getText()).toString().equalsIgnoreCase("")) {
            dialog.show();
            File file = Environment.getExternalStorageDirectory();
            File location = new File(file, "RSA/");
            if (!location.exists()) {
                location.mkdir();
            }
            new RSAEncryptFragment.Encrypt(RSAEncryptFragment.this).execute(encryptLoc.getText().toString(),
                    location.getAbsolutePath() + "/" + encryptLocValue.getText().toString(),
                    nModulus.getText().toString(),
                    publicKey.getText().toString());
        }
    }

    private class Encrypt extends AsyncTask<String, Integer, String> {

        // only retain a weak reference to the activity
        Encrypt(RSAEncryptFragment context) {
            new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... strings) {
            startTime = System.currentTimeMillis();
            RSA.encryptedFile(strings[0], strings[1], new BigInteger(strings[3]), new BigInteger(strings[2]));
            return RSA.showHexFromFile(strings[1]);
        }

        @Override
        protected void onPostExecute(String hex) {
            long endTime = System.currentTimeMillis();
            timeValue.setText(String.valueOf(endTime - startTime));
            outputValue.setText(hex);
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/RSA/" + Objects.requireNonNull(encryptLocValue.getText()).toString());
            outputSize.setText(String.valueOf(file.length()));
            dialog.dismiss();
            Toast.makeText(getActivity(), "Finished Encrypt", Toast.LENGTH_SHORT).show();
        }
    }

    private class OpenKey extends AsyncTask<String, Integer, String> {

        // only retain a weak reference to the activity
        OpenKey(RSAEncryptFragment context) {
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
            publicKey.setText(pubKey);
            nModulus.setText(N);
            dialog.dismiss();
        }
    }

    private class SetInput extends AsyncTask<String, Integer, String> {

        // only retain a weak reference to the activity
        SetInput(RSAEncryptFragment context) {
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
            inputValue.setText(s);
            dialog.dismiss();
        }
    }
}
