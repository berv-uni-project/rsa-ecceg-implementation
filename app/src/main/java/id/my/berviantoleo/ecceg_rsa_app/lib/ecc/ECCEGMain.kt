package id.my.berviantoleo.ecceg_rsa_app.lib.ecc

import id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils.getBytes
import id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils.loadPointsFromFile
import id.my.berviantoleo.ecceg_rsa_app.utils.FileUtils.savePointsToFile
import java.math.BigInteger
import java.util.Scanner

object ECCEGMain {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) { // unhandled exception

        // Initiate Classes Component

        val a = BigInteger("2")
        val b = BigInteger("6")
        val p = BigInteger("15485867")
        val k = BigInteger("30")
        // y = x^3 + 2x + 6 mod 15485867
        // this is programmer defined, but needs to be consistent for encrypt and decrypt
        val ecc = ECC()
        ecc.a = a
        ecc.b = b
        ecc.p = p
        ecc.k = k

        // Initiate ended
        println("Choose an action : ")
        println("1. Generate Key")
        println("2. Encrypt File")
        println("3. Decrypt File")
        print("Input 1/2/3 = ")
        val s = Scanner(System.`in`)
        val chosen = s.nextInt()

        when (chosen) {
            1 -> {
                val privateKeyFilepath = "key.pri"
                val publicKeyFilepath = "key.pub"
                generateKey(ecc, privateKeyFilepath, publicKeyFilepath)
            }
            2 -> {
                val publicKeyFilepath = "key.pub"
                val plainfile = "datatest.txt"
                val ciphfile = "$plainfile.ciph"
                encryptFile(ecc, publicKeyFilepath, plainfile, ciphfile)
            }
            3 -> {
                val privateKeyFilepath = "key.pri"
                val plainfile = "datatest.txt" // This seems to be the intended destination for the decrypted file
                val ciphfile = "datatest.txt.ciph"
                decryptFile(ecc, privateKeyFilepath, ciphfile, plainfile)
            }
            else -> println("Wrong choice!!")
        }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun generateKey(ecc: ECC, privateKeyFilepath: String, publicKeyFilepath: String) {
        val ecceg = ECCEG(ecc, ecc.basePoint!!)
        print("Private key = ")
        println(ecceg.getPrivateKey())
        ecceg.savePrivateKey(privateKeyFilepath)
        println("Saved in $privateKeyFilepath")
        print("Public key = ")
        println(ecceg.publicKey)
        ecceg.savePublicKey(publicKeyFilepath)
        println("Saved in $publicKeyFilepath")
    }

    @Throws(Exception::class)
    private fun encryptFile(
        ecc: ECC, publicKeyFilepath: String, filepathPlain: String,
        filepathCiph: String?
    ) {
        val ecceg = ECCEG(ecc, ecc.basePoint!!)
        ecceg.loadPublicKey(publicKeyFilepath)
        println("Public key loaded...")
        val read = getBytes(filepathPlain)
        println("---===Plainteks===---")
        println(String(read!!))
        println("---======END======---")
        println()
        // Changed to MutableList<Pair<Point?, Point?>> based on error messages
        val enc: MutableList<Pair<Point?, Point?>> = ecceg.encryptBytes(read)
        println("---===Cipherteks===---")
        for (ppPair in enc) { // ppPair is Pair<Point?, Point?> (non-null Pair)
            // Assuming if a pair exists, its components (left and right points) should exist.
            // And if points exist, their coordinates x, y should also exist.
            // If this assumption is incorrect, NullPointerExceptions can occur here.
            // Proper error handling or a redesign of ECCEG.encryptBytes might be needed.
            print(
                String.format(
                    "%02x%02x%02x%02x",
                    ppPair.left!!.x.toInt(),
                    ppPair.left!!.y.toInt(),
                    ppPair.right!!.x.toInt(),
                    ppPair.right!!.y.toInt()
                )
            )
        }
        println()
        println("---======END======---")
        // savePointsToFile expects MutableList<Pair<Point?, Point?>>
        savePointsToFile(filepathCiph, enc)
    }

    @Throws(Exception::class)
    private fun decryptFile(
        ecc: ECC, privateKeyFilepath: String,
        filepathCiph: String, destinationPath: String? // Renamed for clarity, original 'destination'
    ) {
        val ecceg = ECCEG(ecc, ecc.basePoint!!)
        ecceg.loadPrivateKey(privateKeyFilepath)
        println("Private key loaded...")
        // Changed to MutableList<Pair<Point?, Point?>> based on error messages
        val readEnc: MutableList<Pair<Point?, Point?>> = loadPointsFromFile(filepathCiph)
        // Align with actual signature of ecceg.decrypt
        val readDec: MutableList<Point?> = ecceg.decrypt(readEnc)
        println("---===Plainteks===---")
        // TODO: The decrypted content is printed to console. If it should be saved to 'destinationPath', implement file writing here.
        for (pp in readDec) {
            // Assuming if a point exists in the decrypted list, it should be a non-null Point.
            // If pp can be null, then pp!! will throw NPE.
            // If ecc.pointToInt can't handle a Point that might be intrinsically "null" (e.g. point at infinity), that's another issue.
            pp?.let {
                 print(Char(ecc.pointToInt(it).toByte().toUShort()))
            }
        }
        println()
        println("---======END======---")
    }
}