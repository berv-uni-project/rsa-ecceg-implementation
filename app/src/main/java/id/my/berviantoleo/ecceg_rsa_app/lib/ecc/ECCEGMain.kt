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

        if (chosen == 1) {
            val private_key_filepath = "key.pri"
            val public_key_filepath = "key.pub"
            generateKey(ecc, private_key_filepath, public_key_filepath)
        } else if (chosen == 2) {
            val public_key_filepath = "key.pub"
            val plainfile = "datatest.txt"
            val ciphfile = plainfile + ".ciph"
            encryptFile(ecc, public_key_filepath, plainfile, ciphfile)
        } else if (chosen == 3) {
            val private_key_filepath = "key.pri"
            val plainfile = "datatest.txt"
            val ciphfile = "datatest.txt.ciph"
            decryptFile(ecc, private_key_filepath, ciphfile, plainfile)
        } else println("Wrong choice!!")
    }

    @JvmStatic
    @Throws(Exception::class)
    fun generateKey(ecc: ECC, private_key_filepath: String, public_key_filepath: String) {
        val ecceg = ECCEG(ecc, ecc.basePoint!!)
        print("Private key = ")
        println(ecceg.getPrivateKey())
        ecceg.savePrivateKey(private_key_filepath)
        println("Saved in " + private_key_filepath)
        print("Public key = ")
        println(ecceg.publicKey)
        ecceg.savePublicKey(public_key_filepath)
        println("Saved in " + public_key_filepath)
    }

    @Throws(Exception::class)
    private fun encryptFile(
        ecc: ECC, public_key_filepath: String, filepath_plain: String,
        filepath_ciph: String?
    ) {
        val ecceg = ECCEG(ecc, ecc.basePoint!!)
        ecceg.loadPublicKey(public_key_filepath)
        println("Public key loaded...")
        val read = getBytes(filepath_plain)
        println("---===Plainteks===---")
        println(kotlin.text.String(read!!))
        println("---======END======---")
        println()
        val enc: MutableList<Pair<Point?, Point?>> = ecceg.encryptBytes(read)
        println("---===Cipherteks===---")
        for (pp in enc) {
            print(
                String.format(
                    "%02x%02x%02x%02x",
                    pp.left!!.x.toInt(),
                    pp.left!!.y.toInt(),
                    pp.right!!.x.toInt(),
                    pp.right!!.y.toInt()
                )
            )
        }
        println()
        println("---======END======---")
        savePointsToFile(filepath_ciph, enc)
    }

    @Throws(Exception::class)
    private fun decryptFile(
        ecc: ECC, private_key_filepath: String,
        filepath_ciph: String, destination: String?
    ) {
        val ecceg = ECCEG(ecc, ecc.basePoint!!)
        ecceg.loadPrivateKey(private_key_filepath)
        println("Private key loaded...")
        val read_enc = loadPointsFromFile(filepath_ciph)
        val read_dec: MutableList<Point> = ecceg.decrypt(read_enc)
        println("---===Plainteks===---")
        for (pp in read_dec) print(Char(ecc.pointToInt(pp).toByte().toUShort()))
        println()
        println("---======END======---")
    }
}