package id.my.berviantoleo.ecceg_rsa_app.fragment

import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cc.cloudist.acplibrary.ACProgressFlower
import com.obsez.android.lib.filechooser.ChooserDialog
import id.my.berviantoleo.ecceg_rsa_app.R
import id.my.berviantoleo.ecceg_rsa_app.databinding.FragmentRsadecryptBinding
import id.my.berviantoleo.ecceg_rsa_app.lib.rsa.RSA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigInteger

class RSADecryptFragment : Fragment() {

    private var _binding: FragmentRsadecryptBinding? = null
    private val binding get() = _binding!!

    private var keyPath: String? = null
    private var loadingView: ACProgressFlower? = null
    private var startTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRsadecryptBinding.inflate(inflater, container, false)
        loadingView = ACProgressFlower.Builder(context).build()
        loadingView?.setCanceledOnTouchOutside(false)
        loadingView?.setCancelable(false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.openPrivateButton.setOnClickListener {
            openPrivateKeyChooser()
        }

        binding.selectDecryptFileButton.setOnClickListener {
            openFileToDecryptChooser()
        }

        binding.decryptButton.setOnClickListener {
            decryptAndDisplay()
        }
    }

    private fun openPrivateKeyChooser() {
        activity?.let {
            ChooserDialog(it)
                .withFilter(false, false, "pri")
                .withStartFile(Environment.getExternalStorageDirectory().absolutePath)
                .withResources(
                    R.string.title_choose_file,
                    R.string.title_choose,
                    R.string.dialog_cancel
                )
                .withChosenListener { _, pathFile ->
                    keyPath = pathFile.path
                    loadingView?.show()
                    lifecycleScope.launch {
                        loadKeyDetails(keyPath)
                    }
                }
                .build()
                .show()
        }
    }

    private suspend fun loadKeyDetails(filePath: String?) {
        if (filePath == null) {
            withContext(Dispatchers.Main) {
                loadingView?.dismiss()
                Toast.makeText(context, "Key path is null", Toast.LENGTH_SHORT).show()
            }
            return
        }
        try {
            val keyContent = withContext(Dispatchers.IO) {
                RSA.readKey(filePath)
            }
            withContext(Dispatchers.Main) {
                val keyParts: Array<String?> =
                    keyContent.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (keyParts.size >= 2) {
                    binding.nValueDecrypt.setText(keyParts[0])
                    binding.privateKeyValue.setText(keyParts[1])
                } else {
                    Toast.makeText(context, "Invalid key format", Toast.LENGTH_SHORT).show()
                }
                loadingView?.dismiss()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                loadingView?.dismiss()
                Toast.makeText(context, "Error loading key: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun openFileToDecryptChooser() {
        activity?.let {
            ChooserDialog(it)
                .withStartFile(Environment.getExternalStorageDirectory().absolutePath)
                .withResources(
                    R.string.title_choose_file,
                    R.string.title_choose,
                    R.string.dialog_cancel
                )
                .withChosenListener { _, pathFile ->
                    binding.fileDecryptValue.setText(pathFile.path)
                    binding.InputSizeValueDecrypt.setText(pathFile.length().toString())
                    loadingView?.show()
                    lifecycleScope.launch {
                        loadInputFileContent(pathFile.path)
                    }
                }
                .build()
                .show()
        }
    }

    private suspend fun loadInputFileContent(filePath: String?) {
         if (filePath == null) {
            withContext(Dispatchers.Main) {
                loadingView?.dismiss()
                Toast.makeText(context, "File path is null", Toast.LENGTH_SHORT).show()
            }
            return
        }
        try {
            val hexContent = withContext(Dispatchers.IO) {
                RSA.showHexFromFile(filePath)
            }
            withContext(Dispatchers.Main) {
                binding.InputValueDecrypt.setText(hexContent)
                loadingView?.dismiss()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                loadingView?.dismiss()
                Toast.makeText(context, "Error loading file content: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun decryptAndDisplay() {
        val fileToDecryptPath = binding.fileDecryptValue.text.toString()
        val destinationFileName = binding.fileDecryptLocValue.text.toString()
        val nModulusStr = binding.nValueDecrypt.text.toString()
        val privateKeyStr = binding.privateKeyValue.text.toString()

        if (fileToDecryptPath.isBlank() || destinationFileName.isBlank() || nModulusStr.isBlank() || privateKeyStr.isBlank()) {
            Toast.makeText(context, "Please fill all fields and select a file.", Toast.LENGTH_SHORT).show()
            return
        }

        loadingView?.show()

        val rsaDir = File(Environment.getExternalStorageDirectory(), "RSA")
        if (!rsaDir.exists()) {
            rsaDir.mkdirs()
        }
        val destinationPath = File(rsaDir, destinationFileName).absolutePath

        lifecycleScope.launch {
            try {
                startTime = System.currentTimeMillis()
                withContext(Dispatchers.IO) {
                    RSA.decryptFile(
                        fileToDecryptPath,
                        destinationPath,
                        BigInteger(privateKeyStr),
                        BigInteger(nModulusStr)
                    )
                }
                val decryptedBytes = withContext(Dispatchers.IO) {
                    RSA.getBytes(destinationPath)
                }
                val endTime = System.currentTimeMillis()

                withContext(Dispatchers.Main) {
                    binding.timeValueDecrypt.setText((endTime - startTime).toString())
                    binding.outputValueDecrypt.setText(decryptedBytes?.let { String(it) } ?: "")
                    val outFile = File(destinationPath)
                    binding.outputSizeValueDecrypt.setText(outFile.length().toString())
                    loadingView?.dismiss()
                    Toast.makeText(context, "Finished Decrypt", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadingView?.dismiss()
                    Toast.makeText(context, "Decryption failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear the binding when the view is destroyed
        loadingView?.dismiss() // Dismiss dialog to prevent leaks
    }

    companion object {
        fun newInstance(): RSADecryptFragment {
            return RSADecryptFragment()
        }
    }
}
