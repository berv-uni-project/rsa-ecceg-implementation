package id.my.berviantoleo.ecceg_rsa_app.fragment


import android.view.View
import androidx.fragment.app.Fragment
import com.obsez.android.lib.filechooser.ChooserDialog
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.Pair
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.Point
import java.io.File
import java.lang.String
import java.lang.ref.WeakReference
import java.math.BigInteger
import java.util.Objects
import kotlin.ByteArray
import kotlin.Exception
import kotlin.Int
import kotlin.Long
import kotlin.collections.MutableList
import kotlin.collections.plus
import kotlin.plus
import kotlin.sequences.plus
import kotlin.text.equals
import kotlin.text.plus
import kotlin.toString

/**
 * A simple [Fragment] subclass.
 * Use the [ECCEGEncryptFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ECCEGEncryptFragment : Fragment() {
    @JvmField
    @BindView(R.id.public_key_loc_ecceg_value)
    var publicKeyLoc: TextInputEditText? = null

    @JvmField
    @BindView(R.id.plain_text_loc_value_ecceg)
    var plainTextLoc: TextInputEditText? = null

    @JvmField
    @BindView(R.id.cipher_text_loc_ecceg)
    var cipherTextLoc: TextInputEditText? = null

    @JvmField
    @BindView(R.id.input_file_encrypt_ecceg)
    var inputContent: TextInputEditText? = null

    @JvmField
    @BindView(R.id.output_file_encrypt_ecceg)
    var outputContent: TextInputEditText? = null

    @JvmField
    @BindView(R.id.input_file_size_encrypt_ecceg)
    var inputSize: TextInputEditText? = null

    @JvmField
    @BindView(R.id.output_file_size_encrypt_ecceg)
    var outputSize: TextInputEditText? = null

    @JvmField
    @BindView(R.id.time_encrypt_ecceg)
    var timeElapsed: TextInputEditText? = null

    @JvmField
    @BindView(R.id.a_encrypt_ecceg_value)
    var a: TextInputEditText? = null

    @JvmField
    @BindView(R.id.b_encrypt_ecceg_value)
    var b: TextInputEditText? = null

    @JvmField
    @BindView(R.id.p_encrypt_ecceg_value)
    var p: TextInputEditText? = null

    private var loadingView: ACProgressFlower? = null
    private var startTime: Long = 0
    private var endTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_eccegencrypt, container, false)
        ButterKnife.bind(this, view)
        loadingView = Builder(getContext()).build()
        loadingView.setCanceledOnTouchOutside(false)
        loadingView.setCancelable(false)
        return view
    }

    @OnClick(R.id.encrypt_button_ecceg)
    fun encrypt() {
        if (!Objects.requireNonNull<Editable?>(publicKeyLoc.getText()).toString().equals(
                "",
                ignoreCase = true
            ) && !Objects.requireNonNull<Editable?>(plainTextLoc.getText()).toString().equals(
                "",
                ignoreCase = true
            ) && !Objects.requireNonNull<Editable?>(cipherTextLoc.getText()).toString()
                .equals("", ignoreCase = true) && !Objects.requireNonNull<Editable?>(a.getText())
                .toString()
                .equals("", ignoreCase = true) && !Objects.requireNonNull<Editable?>(b.getText())
                .toString()
                .equals("", ignoreCase = true) && !Objects.requireNonNull<Editable?>(p.getText())
                .toString().equals("", ignoreCase = true)
        ) {
            val file: File? = Environment.getExternalStorageDirectory()
            val location = File(file, "ECCEG/")
            if (!location.exists()) {
                location.mkdir()
            }
            val cipherLoc = location.getAbsolutePath() + "/" + cipherTextLoc.getText().toString()
            id.my.berviantoleo.ecceg_rsa_app.fragment.ECCEGEncryptFragment.Encrypt(this@ECCEGEncryptFragment)
                .execute(
                    a.getText().toString(),
                    b.getText().toString(),
                    p.getText().toString(),
                    publicKeyLoc.getText().toString(),
                    plainTextLoc.getText().toString(),
                    cipherLoc
                )
        }
    }

    @OnClick(R.id.search_public_key_ecceg)
    fun openPublicKey() {
        ChooserDialog(getActivity())
            .withFilter(false, false, "pub")
            .withStartFile(Environment.getExternalStorageDirectory().getAbsolutePath())
            .withResources(
                R.string.title_choose_file,
                R.string.title_choose,
                R.string.dialog_cancel
            )
            .withChosenListener({ path, pathFile -> publicKeyLoc.setText(pathFile.getPath()) })
            .build()
            .show()
    }

    @OnClick(R.id.search_plain_text_ecceg)
    fun searchPlainText() {
        ChooserDialog(getActivity())
            .withStartFile(Environment.getExternalStorageDirectory().getAbsolutePath())
            .withResources(
                R.string.title_choose_file,
                R.string.title_choose,
                R.string.dialog_cancel
            )
            .withChosenListener({ path, pathFile ->
                plainTextLoc.setText(pathFile.getPath())
                inputSize.setText(String.valueOf(pathFile.length()))
                loadingView.show()
                id.my.berviantoleo.ecceg_rsa_app.fragment.ECCEGEncryptFragment.SetInput(this@ECCEGEncryptFragment)
                    .execute(pathFile.getPath())
            })
            .build()
            .show()
    }

    private inner class Encrypt(context: ECCEGEncryptFragment?) :
        AsyncTask<kotlin.String?, Int?, kotlin.String?>() {
        // only retain a weak reference to the activity
        init {
            WeakReference<ECCEGEncryptFragment?>(context)
        }

        override fun doInBackground(vararg strings: kotlin.String?): kotlin.String {
            startTime = System.currentTimeMillis()
            try {
                val ecc: ECC = ECC()
                ecc.a = BigInteger(strings[0])
                ecc.b = BigInteger(strings[1])
                ecc.p = BigInteger(strings[2])
                ecc.k = BigInteger.valueOf(30)
                val ecceg: ECCEG = ECCEG(ecc, ecc.basePoint)
                ecceg.loadPublicKey(strings[3])
                val read: ByteArray? =
                    id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils.getBytes(strings[4])
                val enc: MutableList<Pair<Point?, Point?>?> = ecceg.encryptBytes(read)
                id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils.savePointsToFile(strings[5], enc)
                endTime = System.currentTimeMillis()
                return id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils.showHexFromFile(strings[5])
            } catch (e: Exception) {
                return "failed"
            }
        }

        override fun onPostExecute(hex: kotlin.String?) {
            timeElapsed.setText((endTime - startTime).toString())
            outputContent.setText(hex)
            val file: File = File(
                Environment.getExternalStorageDirectory()
                    .getPath() + "/ECCEG/" + Objects.requireNonNull<Editable?>(cipherTextLoc.getText())
                    .toString()
            )
            outputSize.setText(file.length().toString())
            loadingView.dismiss()
            Toast.makeText(getActivity(), "Finished Encrypt", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class SetInput(context: ECCEGEncryptFragment?) :
        AsyncTask<kotlin.String?, Int?, kotlin.String?>() {
        // only retain a weak reference to the activity
        init {
            WeakReference<ECCEGEncryptFragment?>(context)
        }

        override fun doInBackground(vararg strings: kotlin.String?): kotlin.String {
            if (strings.size == 1) {
                val bytes: ByteArray? =
                    id.my.berviantoleo.ecceg_rsa_app.lib.rsa.RSA.getBytes(strings[0])
                if (bytes != null) {
                    return String(bytes)
                }
            }
            return ""
        }

        override fun onPostExecute(s: kotlin.String?) {
            inputContent.setText(s)
            loadingView.dismiss()
        }
    }

    companion object {
        fun newInstance(): ECCEGEncryptFragment {
            return ECCEGEncryptFragment()
        }
    }
}
