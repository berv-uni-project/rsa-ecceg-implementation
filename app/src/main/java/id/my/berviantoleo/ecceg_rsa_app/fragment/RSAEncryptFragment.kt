package id.my.berviantoleo.ecceg_rsa_app.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import id.my.berviantoleo.ecceg_rsa_app.R
import id.my.berviantoleo.ecceg_rsa_app.databinding.FragmentRsaencryptBinding
import id.my.berviantoleo.ecceg_rsa_app.lib.rsa.RSA
import id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.math.BigInteger

class RSAEncryptFragment : Fragment() {

    private var _binding: FragmentRsaencryptBinding? = null
    private val binding get() = _binding!!

    private var publicKeyPath: String? = null
    private var plainTextPath: String? = null
    private var publicKey: BigInteger? = null
    private var n: BigInteger? = null

    private lateinit var loadingDialog: AlertDialog

    private lateinit var publicKeyFilePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var plainTextFilePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        publicKeyFilePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        publicKeyPath = getPathFromUri(uri)
                        binding.publicKeyValue.setText(publicKeyPath)
                        publicKeyPath?.let {
                            lifecycleScope.launch {
                                loadKeyDetails(it)
                            }
                        }
                    }
                }
            }

        plainTextFilePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        plainTextPath = getPathFromUri(uri)
                        binding.fileEncryptValue.setText(plainTextPath)
                        plainTextPath?.let {
                            lifecycleScope.launch {
                                loadInputFileContent(it)
                            }
                        }
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRsaencryptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLoadingDialog()

        binding.openPublicButton.setOnClickListener {
            selectPublicKeyFile()
        }

        binding.selectEncryptFileButton.setOnClickListener {
            selectPlainTextFile()
        }

        binding.encryptButton.setOnClickListener {
            encryptData()
        }
    }

    private fun setupLoadingDialog() {
        loadingDialog = AlertDialog.Builder(requireContext())
            .setMessage("Processing...")
            .setCancelable(false)
            .create()
    }

    private fun selectPublicKeyFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Or a more specific MIME type if you expect certain key file types
        }
        publicKeyFilePickerLauncher.launch(intent)
    }

    private fun selectPlainTextFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Allow all file types
        }
        plainTextFilePickerLauncher.launch(intent)
    }

    private fun getPathFromUri(uri: Uri): String? {
        // This is a simplified way to get a path. For robust solution, especially for cloud files,
        // you might need to copy the file to cache and use its path.
        if (uri.scheme == "file") {
            return uri.path
        } else if (uri.scheme == "content") {
            var fileName: String? = null
            activity?.contentResolver?.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                         fileName = cursor.getString(displayNameIndex)
                    }
                }
            }
            if (fileName != null) {
                val cacheDir = requireContext().cacheDir
                val tempFile = File(cacheDir, fileName)
                try {
                    val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
                    val outputStream = FileOutputStream(tempFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    return tempFile.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error copying file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        return null
    }

    private suspend fun loadKeyDetails(filePath: String) {
        withContext(Dispatchers.Main) {
            loadingDialog.show()
        }
        try {
            val keyDetails = withContext(Dispatchers.IO) {
                RSA.readKey(filePath)
            }
            withContext(Dispatchers.Main) {
                publicKey = keyDetails.first
                n = keyDetails.second
                binding.publicKeyValue.setText(filePath) // Show path
                // binding.nValueEncrypt.setText(n.toString()) // Optionally display N if needed
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error reading public key: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } finally {
            withContext(Dispatchers.Main) {
                loadingDialog.dismiss()
            }
        }
    }

    private suspend fun loadInputFileContent(filePath: String) {
        withContext(Dispatchers.Main) {
            loadingDialog.show()
        }
        try {
            val fileContentHex = withContext(Dispatchers.IO) {
                RSA.showHexFromFile(filePath)
            }
            val fileSize = withContext(Dispatchers.IO) {
                File(filePath).length().toString()
            }
            withContext(Dispatchers.Main) {
                binding.inputValue.setText(fileContentHex)
                binding.inputSizeValue.setText(fileSize)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Error reading input file: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } finally {
            withContext(Dispatchers.Main) {
                loadingDialog.dismiss()
            }
        }
    }

    private fun encryptData() {
        val publicKeyFileLocation = binding.publicKeyValue.text.toString()
        val plainTextFileLocation = binding.fileEncryptValue.text.toString()
        val cipherTextSaveLocation = binding.fileEncryptLocValue.text.toString()

        if (publicKeyFileLocation.isBlank() || plainTextFileLocation.isBlank() || cipherTextSaveLocation.isBlank()) {
            Toast.makeText(context, "Please select public key, plain text file, and enter save location", Toast.LENGTH_SHORT).show()
            return
        }

        if (publicKey == null || n == null) {
            Toast.makeText(context, "Public key details not loaded correctly. Please re-select the key file.", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            performEncryption(plainTextFileLocation, cipherTextSaveLocation, publicKey!!, n!!)
        }
    }

    private suspend fun performEncryption(
        plainTextFilePath: String,
        cipherTextSaveName: String,
        keyE: BigInteger,
        keyN: BigInteger
    ) {
        withContext(Dispatchers.Main) {
            loadingDialog.show()
        }
        var resultMessage = "Encryption Failed"
        var cipherTextHex: String? = null
        var outputFileSize: String? = null
        var executionTime: Long? = null
        var fullSavePath: String? = null

        try {
            val plainBytes = withContext(Dispatchers.IO) {
                RSA.getBytes(plainTextFilePath)
            }

            val startTime = System.nanoTime()
            val encryptedBytes = withContext(Dispatchers.IO) {
                RSA.encryptBytes(plainBytes, keyE, keyN)
            }
            executionTime = (System.nanoTime() - startTime) / 1000000 // ms

            val newDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "RSA_ENCRYPTED")
            if (!newDir.exists()) {
                newDir.mkdirs()
            }
            fullSavePath = newDir.absolutePath + File.separator + cipherTextSaveName

            withContext(Dispatchers.IO) {
                val fos = FileOutputStream(fullSavePath)
                fos.write(encryptedBytes)
                fos.close()
            }

            cipherTextHex = withContext(Dispatchers.IO) {
                RSA.showHexFromFile(fullSavePath) // Assuming this can read any file for hex display
            }
            outputFileSize = withContext(Dispatchers.IO) {
                File(fullSavePath).length().toString()
            }
            resultMessage = "Encryption Successful! Saved to $fullSavePath"

        } catch (e: Exception) {
            resultMessage = "Encryption Error: ${e.message}"
            e.printStackTrace()
        } finally {
            withContext(Dispatchers.Main) {
                loadingDialog.dismiss()
                binding.outputValue.setText(cipherTextHex ?: "Error")
                binding.outputSizeValue.setText(outputFileSize ?: "N/A")
                binding.timeValue.setText(executionTime?.toString()?.plus(" ms") ?: "N/A")
                Toast.makeText(context, resultMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        _binding = null
    }

    companion object {
        fun newInstance(): RSAEncryptFragment {
            return RSAEncryptFragment()
        }
    }
}
