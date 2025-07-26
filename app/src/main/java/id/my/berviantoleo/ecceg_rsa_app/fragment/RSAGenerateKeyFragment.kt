package id.my.berviantoleo.ecceg_rsa_app.fragment

import android.Manifest
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.material.textfield.TextInputEditText
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import id.my.berviantoleo.ecceg_rsa_app.R
import id.my.berviantoleo.ecceg_rsa_app.fragment.RSAGenerateKeyFragment
import java.io.File
import java.lang.ref.WeakReference
import java.util.Objects

class RSAGenerateKeyFragment : Fragment() {
    @JvmField
    @BindView(R.id.public_location_save)
    var publicLocation: TextInputEditText? = null

    @JvmField
    @BindView(R.id.private_location_save)
    var privateLocation: TextInputEditText? = null

    @JvmField
    @BindView(R.id.byte_size)
    var byteSize: TextInputEditText? = null
    private var dialog: AlertDialog? = null
    private var extract: PermissionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_rsagenerate_key, container, false)
        ButterKnife.bind(this, view)
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()
        extract = object : PermissionListener {
            override fun onPermissionGranted() {
                val location = Environment.getExternalStorageDirectory()
                val newLocation = File(location, "RSA/")
                if (!newLocation.exists()) {
                    newLocation.mkdir()
                }
                dialog!!.show()
                id.my.berviantoleo.ecceg_rsa_app.fragment.RSAGenerateKeyFragment.GenerateKey(this@RSAGenerateKeyFragment)
                    .execute(
                        Objects.requireNonNull<Editable?>(
                            byteSize!!.text
                        ).toString(),
                        newLocation.absolutePath + "/" + privateLocation!!.text.toString(),
                        newLocation.absolutePath + "/" + Objects.requireNonNull<Editable?>(
                            publicLocation!!.text
                        ).toString()
                    )
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(context, "Permission Denied\n$deniedPermissions", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return view
    }

    @OnClick(R.id.generate_button)
    fun generateKey() {
        if (!Objects.requireNonNull(byteSize!!.text).toString()
                .equals("", ignoreCase = true) && !Objects.requireNonNull(
                privateLocation!!.text
            ).toString().equals("", ignoreCase = true) && !Objects.requireNonNull(
                publicLocation!!.text
            ).toString().equals("", ignoreCase = true)
        ) {
            if (byteSize!!.text.toString().toInt() >= 1024) {
                TedPermission.create()
                    .setPermissionListener(extract)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check()
            }
        }
    }

    private inner class GenerateKey(context: RSAGenerateKeyFragment) :
        AsyncTask<String?, Int?, Void?>() {
        private val activityReference =
            WeakReference(context)

        override fun doInBackground(vararg strings: String): Void? {
            val byteSize = strings[0].toInt()
            id.my.berviantoleo.ecceg_rsa_app.lib.rsa.RSA.generateKey(
                byteSize, strings[1],
                strings[2]
            )
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            dialog!!.dismiss()
            Toast.makeText(context, "Finished", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): RSAGenerateKeyFragment {
            return RSAGenerateKeyFragment()
        }
    }
}
