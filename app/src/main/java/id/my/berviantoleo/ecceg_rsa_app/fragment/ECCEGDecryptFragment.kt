package id.my.berviantoleo.ecceg_rsa_app.fragment

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cc.cloudist.acplibrary.ACProgressFlower
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import id.my.berviantoleo.ecceg_rsa_app.R
import id.my.berviantoleo.ecceg_rsa_app.databinding.FragmentEccegdecryptBinding
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.ECC
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.ECCEG
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.Point
import id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.math.BigInteger

class ECCEGDecryptFragment : Fragment() {

    private var _binding: FragmentEccegdecryptBinding? = null
    private val binding get() = _binding!!

    private var loadingView: ACProgressFlower? = null
    private var privateKeyPath: String? = null
    private var cipherTextPath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEccegdecryptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingView = ACProgressFlower.Builder(context)
            .direction(ACProgressFlower.Direction.CLOCKWISE)
            .themeColor(resources.getColor(R.color.colorPrimary, requireActivity().theme))
            .fadeColor(resources.getColor(R.color.colorAccent, requireActivity().theme))
            .build()
        loadingView?.setCancelable(false)
        loadingView?.setCanceledOnTouchOutside(false)

        binding.searchPrivateKeyEccegDecrypt.setOnClickListener {
            selectPrivateKeyFile()
        }

        binding.searchCipherTextEccegDecrypt.setOnClickListener {
            selectCipherTextFile()
        }

        binding.decryptButtonEcceg.setOnClickListener {
            decryptData()
        }
    }

    private fun selectPrivateKeyFile() {
        val properties = DialogProperties().apply {
            selection_mode = DialogConfigs.SINGLE_MODE
            selection_type = DialogConfigs.FILE_SELECT
            root = Environment.getExternalStorageDirectory()
            error_dir = File(DialogConfigs.DEFAULT_DIR)
            offset = File(DialogConfigs.DEFAULT_DIR)
            extensions = null // Allow all file types or specify if needed
            show_hidden_files = false
        }
        val dialog = FilePickerDialog(context, properties)
        dialog.setTitle("Select Private Key File")
        dialog.setDialogSelectionListener { files ->
            if (files.isNotEmpty()) {
                privateKeyPath = files[0]
                binding.privateKeyLocEccegValue.setText(privateKeyPath)
            }
        }
        dialog.show()
    }

    private fun selectCipherTextFile() {
        val properties = DialogProperties().apply {
            selection_mode = DialogConfigs.SINGLE_MODE
            selection_type = DialogConfigs.FILE_SELECT
            root = Environment.getExternalStorageDirectory()
            error_dir = File(DialogConfigs.DEFAULT_DIR)
            offset = File(DialogConfigs.DEFAULT_DIR)
            extensions = null // Allow all file types or specify if needed
            show_hidden_files = false
        }
        val dialog = FilePickerDialog(context, properties)
        dialog.setTitle("Select Cipher Text File")
        dialog.setDialogSelectionListener { files ->
            if (files.isNotEmpty()) {
                cipherTextPath = files[0]
                binding.cipherTextLocEccegValue.setText(cipherTextPath)
                lifecycleScope.launch {
                    loadCipherTextFileContent(cipherTextPath!!)
                }
            }
        }
        dialog.show()
    }

    private suspend fun loadCipherTextFileContent(filePath: String) {
        withContext(Dispatchers.Main) {
            loadingView?.show()
        }
        try {
            val fileContentHex = withContext(Dispatchers.IO) {
                FileUtils.showHexFromFile(filePath)
            }
            val fileSize = withContext(Dispatchers.IO) {
                File(filePath).length().toString()
            }
            withContext(Dispatchers.Main) {
                binding.inputFileDecryptEcceg.setText(fileContentHex)
                binding.inputFileSizeDecryptEcceg.setText(fileSize)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error reading cipher text file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } finally {
            withContext(Dispatchers.Main) {
                loadingView?.dismiss()
            }
        }
    }

    private fun decryptData() {
        val privateKeyLocation = binding.privateKeyLocEccegValue.text.toString()
        val cipherTextLocation = binding.cipherTextLocEccegValue.text.toString()
        val plainTextSaveLocation = binding.plainTextLocEccegValue.text.toString()
        val aStr = binding.aDecryptEccegValue.text.toString()
        val bStr = binding.bDecryptEccegValue.text.toString()
        val pStr = binding.pDecryptEccegValue.text.toString()

        if (privateKeyLocation.isBlank() || cipherTextLocation.isBlank() || plainTextSaveLocation.isBlank() ||
            aStr.isBlank() || bStr.isBlank() || pStr.isBlank()
        ) {
            Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val a = BigInteger(aStr)
            val b = BigInteger(bStr)
            val p = BigInteger(pStr)

            lifecycleScope.launch {
                performDecryption(privateKeyLocation, cipherTextLocation, plainTextSaveLocation, a, b, p)
            }

        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Invalid ECC parameters (a, b, or p). Please enter valid numbers.", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun performDecryption(
        privateKeyPath: String,
        cipherTextPath: String,
        plainTextSavePath: String,
        a: BigInteger,
        b: BigInteger,
        p: BigInteger
    ) {
        withContext(Dispatchers.Main) {
            loadingView?.show()
        }
        var resultMessage = "Decryption Failed"
        var plainTextHex: String? = null
        var outputFileSize: String? = null
        var executionTime: String? = null

        try {
            val ecc = ECC().apply {
                this.a = a
                this.b = b
                this.p = p
            }

            val privateKey = withContext(Dispatchers.IO) {
                FileUtils.getPrivateKeyFromFile(privateKeyPath)
            }
            if (privateKey == null) {
                resultMessage = "Failed to read private key."
                throw Exception(resultMessage)
            }

            val cipherPoints = withContext(Dispatchers.IO) {
                FileUtils.getPointsFromFile(cipherTextPath)
            }
            if (cipherPoints == null || cipherPoints.isEmpty()) {
                resultMessage = "Failed to read cipher text or cipher text is empty."
                throw Exception(resultMessage)
            }

            val startTime = System.nanoTime()
            val decryptedBytes = withContext(Dispatchers.IO) {
                ECCEG.decrypt(cipherPoints, privateKey, ecc)
            }
            val endTime = System.nanoTime()
            executionTime = ((endTime - startTime) / 1e6).toString() + " ms"


            val newDir = File(Environment.getExternalStorageDirectory(), "/ECCEG_DECRYPTED/")
            if (!newDir.exists()) {
                newDir.mkdirs()
            }
            val fullSavePath = newDir.absolutePath + "/" + plainTextSavePath

            withContext(Dispatchers.IO) {
                FileUtils.saveBytesToFile(decryptedBytes, fullSavePath)
            }

            plainTextHex = withContext(Dispatchers.IO) {
                FileUtils.showHexFromFile(fullSavePath)
            }
            outputFileSize = withContext(Dispatchers.IO) {
                File(fullSavePath).length().toString()
            }
            resultMessage = "Decryption Successful! Saved to $fullSavePath"

        } catch (e: Exception) {
            resultMessage = "Decryption Error: ${e.message}"
            e.printStackTrace()
        } finally {
            withContext(Dispatchers.Main) {
                loadingView?.dismiss()
                binding.outputFileDecryptEcceg.setText(plainTextHex ?: "Error")
                binding.outputFileSizeDecryptEcceg.setText(outputFileSize ?: "N/A")
                binding.timeDecryptEcceg.setText(executionTime ?: "N/A")
                Toast.makeText(context, resultMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingView?.dismiss() // Ensure dialog is dismissed
        _binding = null
    }

    companion object {
        fun newInstance(): ECCEGDecryptFragment {
            return ECCEGDecryptFragment()
        }
    }
}
