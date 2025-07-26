package id.my.berviantoleo.ecceg_rsa_app.lib.rsa

import android.util.Log
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.OutputStreamWriter
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Random
import kotlin.math.ceil

object RSA {
    /**
     * Returns true when the argument is null.
     */
    private fun isNull(obj: Any?): Boolean {
        return obj == null
    }

    fun generateKey(bitLength: Int, privateName: String?, publicName: String?) {
        val rnd = SecureRandom()
        val p = BigInteger.probablePrime(75 * bitLength / 100, rnd)
        val q = BigInteger.probablePrime(25 * bitLength / 100, rnd)
        val n = p.multiply(q)
        val phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE))
        var i: BigInteger
        var pubExp = BigInteger.ONE
        i = BigInteger.probablePrime(bitLength / 10, rnd)
        while (i.compareTo(n) < 0) {
            if (i.gcd(phi) == BigInteger.ONE) {
                pubExp = i
                break
            }
            i = i.nextProbablePrime()
        }
        val priExp = pubExp.modInverse(phi)
        writeKeyToFile(privateName, n, priExp)
        writeKeyToFile(publicName, n, pubExp)
    }

    private const val HEXES = "0123456789ABCDEF"

    @JvmStatic
    fun showHexFromFile(file: String): String {
        val sourceBytes = getBytes(file)
        if (isNull(sourceBytes)) {
            return ""
        }
        return RSA.getHex(sourceBytes!!)
    }

    private fun getHex(raw: ByteArray): String {
        val hex = StringBuilder(2 * raw.size)
        for (b in raw) {
            hex.append(HEXES.get((b.toInt() and 0xF0) shr 4))
                .append(HEXES.get((b.toInt() and 0x0F)))
        }
        return hex.toString()
    }

    private fun writeKeyToFile(name: String?, n: BigInteger, d: BigInteger) {
        val output = n.toString() + ":" + d.toString()
        try {
            val writer = FileOutputStream(name)
            val outWriter = OutputStreamWriter(writer)
            outWriter.write(output)
            outWriter.close()
            writer.flush()
            writer.close()
        } catch (e: IOException) {
            Log.e("RSA", e.message!!)
        }
    }

    @JvmStatic
    fun readKey(location: String?): String {
        var value: String? = ":"
        try {
            BufferedReader(FileReader(location)).use { br ->
                val sCurrentLine = br.readLine()
                if (sCurrentLine != null) {
                    value = sCurrentLine
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return value!!
        }
        return value!!
    }

    @JvmStatic
    fun decryptFile(source: String, destination: String?, d: BigInteger, n: BigInteger): Boolean {
        val sourceBytes = getBytes(source)
        if (isNull(sourceBytes)) {
            return false
        }

        val k = ceil(n.bitLength() / 8.0).toInt()
        var c: BigInteger?
        var m: BigInteger?
        var EB: ByteArray?
        var M: ByteArray?
        val C = RSA.reshape(sourceBytes!!, k)
        val out: BufferedOutputStream?

        try {
            if (C != null) {
                out = BufferedOutputStream(FileOutputStream(destination))
                for (aC in C) {
                    if (aC.size != k) return false
                    c = BigInteger(aC)
                    m = decrypt(c, d, n)
                    EB = toByteArray(m, k)
                    if (EB != null) {
                        M = extractData(EB)
                        out.write(M)
                    }
                }
                out.close()
            } else {
                return false
            }
            out.close()
        } catch (e: IOException) {
            return false
        }
        return true
    }

    /**
     * Extracts the data portion of the byte array.
     */
    private fun extractData(EB: ByteArray): ByteArray? {
        if (EB.size < 12 || EB[0].toInt() != 0x00 || EB[1].toInt() != 0x02) {
            return null
        }
        var index = 2
        do {
        } while (EB[index++].toInt() != 0x00)

        return getSubArray(EB, index, EB.size)
    }

    /**
     * Performs the classical RSA computation.
     */
    private fun decrypt(c: BigInteger, d: BigInteger, n: BigInteger): BigInteger {
        return c.modPow(d, n)
    }

    @JvmStatic
    fun getBytes(fileName: String): ByteArray? {
        val fIn = File(fileName)
        if (!fIn.canRead()) {
            System.err.println("Can't read " + fileName)
            return null
        }

        var bytes: ByteArray? = null
        try {
            FileInputStream(fIn).use { `in` ->
                val fileSize = fIn.length()
                if (fileSize > Int.Companion.MAX_VALUE) {
                    println("Sorry, file was too large!")
                }

                bytes = ByteArray(fileSize.toInt())

                var offset = 0
                var numRead = 0
                while (offset < bytes.size && (`in`.read(bytes, offset, bytes.size - offset)
                        .also { numRead = it }) >= 0
                ) {
                    offset += numRead
                }
            }
        } catch (ignored: IOException) {
        }

        return bytes
    }

    /**
     * Performs the classical RSA computation.
     */
    private fun encrypt(m: BigInteger, e: BigInteger, n: BigInteger): BigInteger {
        return m.modPow(e, n)
    }

    /**
     * Uses the key and returns true if encryption was successful.
     */
    @JvmStatic
    fun encryptedFile(source: String, destination: String?, e: BigInteger, n: BigInteger): Boolean {
        val sourceBytes = getBytes(source)
        if (isNull(sourceBytes)) {
            System.err.println(String.format("%s contained nothing.", source))
            return false
        }

        val k = ceil(n.bitLength() / 8.0).toInt()
        val BT: Byte = 0x02
        var C: ByteArray?
        var M: ByteArray?
        val D = RSA.reshape(sourceBytes!!, k - 11)
        val EB = ByteArrayOutputStream(k)
        val out: FileOutputStream?
        var m: BigInteger?
        var c: BigInteger?

        try {
            if (D != null) {
                out = FileOutputStream(destination)
                for (aD in D) {
                    EB.reset()
                    EB.write(0x00)
                    EB.write(BT.toInt())
                    EB.write(makePaddingString(k - aD.size - 3))
                    EB.write(0x00)
                    EB.write(aD)
                    M = EB.toByteArray()
                    m = BigInteger(M)
                    c = encrypt(m, e, n)
                    C = toByteArray(c, k)
                    out.write(C)
                }

                out.close()
            } else {
                return false
            }
        } catch (ex: Exception) {
            val errMsg = "An exception occured!%n%s%n%s%n%s"
            System.err.println(
                String.format(
                    errMsg,
                    ex.javaClass,
                    ex.message,
                    ex.getStackTrace().contentToString()
                )
            )
            return false
        }

        return true
    }

    private fun reshape(inBytes: ByteArray, colSize: Int): Array<ByteArray>? {
        var colSize = colSize
        if (colSize < 1) {
            colSize = 1
        }

        val rowSize = ceil(inBytes.size.toDouble() / colSize.toDouble()).toInt()

        if (rowSize == 0) {
            return null
        }

        val outBytes: Array<ByteArray> = arrayOfNulls<ByteArray>(rowSize)

        for (i in 0..<rowSize) {
            outBytes[i] = id.my.berviantoleo.ecceg_rsa_app.lib.rsa.RSA.getSubArray(
                inBytes,
                i * colSize,
                (i + 1) * colSize
            )!!
        }
        return outBytes
    }

    /**
     * Returns a portion of the array argument.
     */
    private fun getSubArray(inBytes: ByteArray, start: Int, end: Int): ByteArray? {
        var end = end
        if (start >= inBytes.size) {
            return null
        }
        if (end > inBytes.size) {
            end = inBytes.size
        }
        val bytesToGet = end - start
        if (bytesToGet < 1) {
            return null
        }

        val outBytes = ByteArray(bytesToGet)
        if (end - start >= 0) System.arraycopy(inBytes, start, outBytes, 0, end - start)

        return outBytes
    }

    /**
     * Converts a BigInteger into a byte array of the specified length.
     */
    private fun toByteArray(x: BigInteger, numBytes: Int): ByteArray? {
        var x = x
        var numBytes = numBytes
        if (x.compareTo(BigInteger.valueOf(256).pow(numBytes)) >= 0) {
            return null // number is to big to fit in the byte array
        }

        val ba = ByteArray(numBytes--)
        var divAndRem: Array<BigInteger?>?

        for (power in numBytes downTo 0) {
            divAndRem = x.divideAndRemainder(BigInteger.valueOf(256).pow(power))
            ba[numBytes - power] = divAndRem[0]!!.toInt().toByte()
            x = divAndRem[1]!!
        }

        return ba
    }

    /**
     * Generates an array of pseudo-random nonzero bytes.
     */
    private fun makePaddingString(len: Int): ByteArray? {
        if (len < 8) return null
        val random = Random()

        val PS = ByteArray(len)
        for (i in 0..<len) {
            PS[i] = (random.nextInt(255) + 1).toByte()
        }

        return PS
    }
}