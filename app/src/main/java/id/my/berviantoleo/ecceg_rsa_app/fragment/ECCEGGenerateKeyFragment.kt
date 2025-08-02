package id.my.berviantoleo.ecceg_rsa_app.fragment

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import id.my.berviantoleo.ecceg_rsa_app.R
import id.my.berviantoleo.ecceg_rsa_app.databinding.FragmentEcceggenerateKeyBinding
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.ECC
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.ECCEG
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.Point
import id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigInteger

class ECCEGGenerateKeyFragment : Fragment() {

    private var _binding: FragmentEcceggenerateKeyBinding? = null
    private val binding get() = _binding!!

    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEcceggenerateKeyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLoadingDialog()

        binding.generateKeyEcceg.setOnClickListener {
            validateAndRequestPermissions()
        }
    }

    private fun setupLoadingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        builder.setView(R.layout.layout_loading_dialog)
        loadingDialog = builder.create()
    }

    private fun validateAndRequestPermissions() {
        val aStr = binding.eccegA.text.toString()
        val bStr = binding.eccegB.text.toString()
        val pStr = binding.eccegP.text.toString()
        val baseXStr = binding.eccegBasePointX.text.toString()
        val baseYStr = binding.eccegBasePointY.text.toString()
        val kStr = binding.eccegKVal.text.toString()
        val pubPath = binding.eccegPubPath.text.toString()
        val privPath = binding.eccegPrivatePath.text.toString()

        if (aStr.isBlank() || bStr.isBlank() || pStr.isBlank() ||
            baseXStr.isBlank() || baseYStr.isBlank() || kStr.isBlank() ||
            pubPath.isBlank() || privPath.isBlank()
        ) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            BigInteger(aStr)
            BigInteger(bStr)
            BigInteger(pStr)
            BigInteger(baseXStr)
            BigInteger(baseYStr)
            BigInteger(kStr)
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Invalid number format for ECC parameters or K", Toast.LENGTH_SHORT).show()
            return
        }

        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    generateAndSaveKeys(aStr, bStr, pStr, baseXStr, baseYStr, kStr, pubPath, privPath)
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Toast.makeText(
                        requireContext(),
                        "Permission Denied\n" + deniedPermissions.joinToString("\n"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check()
    }

    private fun generateAndSaveKeys(
        aStr: String, bStr: String, pStr: String,
        baseXStr: String, baseYStr: String, kStr: String,
        pubPath: String, privPath: String
    ) {
        loadingDialog?.show()
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val a = BigInteger(aStr)
                    val b = BigInteger(bStr)
                    val p = BigInteger(pStr)
                    val baseX = BigInteger(baseXStr)
                    val baseY = BigInteger(baseYStr)
                    val privateKeyK = BigInteger(kStr) // This is the private key

                    val ecc = ECC()
                    ecc.a = a
                    ecc.b = b
                    ecc.p = p
                    ecc.basePoint = Point(baseX, baseY)
                    // k is used for point multiplication factor in standard ECC,
                    // but here kStr is directly the private key.
                    // The ECCEG library might generate the public key from this private key.

                    // Assuming ECCEG constructor takes ECC object and a base point for operations.
                    // And there's a way to set or use the private key `privateKeyK`
                    // to generate the corresponding public key.

                    val ecceg = ECCEG(ecc, ecc.basePoint)
                    
                    // This part is crucial and depends on your ECCEG library:
                    // How do you get the public key if you provide the private key `privateKeyK`?
                    // Option 1: A method to generate public key from private key
                    // val publicKey = ecceg.generatePublicKeyFromPrivateKey(privateKeyK)

                    // Option 2: If ECCEG directly uses a private key you set to derive public key.
                    // For example, if your ECCEG class has a method like `setPrivateKeyAndGeneratePublicKey()`
                    // or if the public key is generated when you set the private key.
                    // For now, let's assume `ecceg.generateKey()` is smart enough or there's another method.
                    // If `generateKey()` randomly generates a new key pair, that's not what we want here as K is given.

                    // Let's assume your ECCEG class can take a private key and expose the public key.
                    // This is a placeholder for how your library might work.
                    // You might need to directly use `privateKeyK` with `ecc.multiply`
                    // to get the public key point if `ECCEG` doesn't handle it.
                    val publicKeyPoint = ecc.multiply(ecc.basePoint, privateKeyK)
                    
                    if (publicKeyPoint == null) {
                        return@withContext "Failed to generate public key point. Point at infinity?"
                    }

                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val eccKeysDir = File(downloadsDir, "ECCEG_KEYS")
                    if (!eccKeysDir.exists()) {
                        eccKeysDir.mkdirs()
                    }

                    val privateKeyFile = File(eccKeysDir, privPath)
                    val publicKeyFile = File(eccKeysDir, pubPath)

                    // Save the private key (BigInteger k)
                    FileUtils.savePrivateKeyToFile(privateKeyFile.absolutePath, privateKeyK.toString()) // Assuming it saves string
                    // Save the public key (Point)
                    FileUtils.savePublicKeyToFile(publicKeyFile.absolutePath, publicKeyPoint) // Assuming it saves Point

                    "Keys generated and saved successfully\nPrivate Key: ${privateKeyFile.absolutePath}\nPublic Key: ${publicKeyFile.absolutePath}"
                }
                Toast.makeText(context, result, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                loadingDialog?.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog?.dismiss() // Dismiss dialog to prevent leaks
        _binding = null
    }

    companion object {
        fun newInstance(): ECCEGGenerateKeyFragment {
            return ECCEGGenerateKeyFragment()
        }
    }
}
