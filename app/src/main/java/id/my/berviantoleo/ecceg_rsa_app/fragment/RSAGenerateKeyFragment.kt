package id.my.berviantoleo.ecceg_rsa_app.fragment

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import id.my.berviantoleo.ecceg_rsa_app.R
import id.my.berviantoleo.ecceg_rsa_app.databinding.FragmentRsagenerateKeyBinding
import id.my.berviantoleo.ecceg_rsa_app.lib.rsa.RSA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RSAGenerateKeyFragment : Fragment() {

    private var _binding: FragmentRsagenerateKeyBinding? = null
    private val binding get() = _binding!!

    private var loadingDialog: AlertDialog? = null
    private var permissionListener: PermissionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRsagenerateKeyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLoadingDialog()
        setupPermissionListener()

        binding.generateButton.setOnClickListener {
            validateAndGenerateKeys()
        }
    }

    private fun setupLoadingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        builder.setView(R.layout.layout_loading_dialog)
        loadingDialog = builder.create()
    }

    private fun setupPermissionListener() {
        permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val byteSizeStr = binding.byteSize.text.toString()
                val privateKeyFileName = binding.privateLocationSave.text.toString()
                val publicKeyFileName = binding.publicLocationSave.text.toString()

                val byteSizeInt = byteSizeStr.toIntOrNull()

                if (byteSizeInt == null) {
                    Toast.makeText(context, "Invalid byte size entered.", Toast.LENGTH_SHORT).show()
                    return
                }

                lifecycleScope.launch {
                    generateAndSaveKeys(byteSizeInt, privateKeyFileName, publicKeyFileName)
                }
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(context, "Permission Denied\n$deniedPermissions", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateAndGenerateKeys() {
        val byteSizeStr = binding.byteSize.text.toString()
        val privateKeyFileName = binding.privateLocationSave.text.toString()
        val publicKeyFileName = binding.publicLocationSave.text.toString()

        if (publicKeyFileName.isBlank() || privateKeyFileName.isBlank() || byteSizeStr.isBlank()) {
            Toast.makeText(context, "All fields must be filled.", Toast.LENGTH_SHORT).show()
            return
        }

        val byteSizeInt = byteSizeStr.toIntOrNull()
        if (byteSizeInt == null || byteSizeInt < 1024) {
            Toast.makeText(context, "Key size must be a number and at least 1024.", Toast.LENGTH_SHORT).show()
            return
        }

        TedPermission.create()
            .setPermissionListener(permissionListener)
            .setDeniedMessage("If you reject permission, you cannot use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
    }

    private suspend fun generateAndSaveKeys(byteSize: Int, privateKeyFileName: String, publicKeyFileName: String) {
        withContext(Dispatchers.Main) {
            loadingDialog?.show()
        }

        try {
            val location = Environment.getExternalStorageDirectory()
            val rsaDir = File(location, "RSA")
            if (!rsaDir.exists()) {
                rsaDir.mkdirs()
            }

            val privateKeyPath = File(rsaDir, privateKeyFileName).absolutePath
            val publicKeyPath = File(rsaDir, publicKeyFileName).absolutePath

            withContext(Dispatchers.IO) {
                RSA.generateKey(byteSize, privateKeyPath, publicKeyPath)
            }

            withContext(Dispatchers.Main) {
                loadingDialog?.dismiss()
                Toast.makeText(context, "Keys generated successfully!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                loadingDialog?.dismiss()
                Toast.makeText(context, "Error generating keys: ${e.message}", Toast.LENGTH_LONG).show()
            }
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss() // Ensure dialog is dismissed to prevent leaks
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): RSAGenerateKeyFragment {
            return RSAGenerateKeyFragment()
        }
    }
}
