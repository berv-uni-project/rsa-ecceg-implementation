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
        while (i < n) {
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
        return getHex(sourceBytes!!)
    }

    private fun getHex(raw: ByteArray): String {
        val hex = StringBuilder(2 * raw.size)
        for (b in raw) {
            hex.append(HEXES[(b.toInt() and 0xF0) shr 4])
                .append(HEXES[b.toInt() and 0x0F])
        }
        return hex.toString()
    }

    private fun writeKeyToFile(name: String?, n: BigInteger, d: BigInteger) {
        val output = "$n:$d"
        try {
            FileOutputStream(name).use { fileStream ->
                OutputStreamWriter(fileStream).use { outWriter ->
                    outWriter.write(output)
                }
            }
        } catch (e: IOException) {
            Log.e("RSA", e.message ?: "Unknown error writing key to file")
        }
    }

    @JvmStatic
    fun readKey(location: String?): String {
        var value = ":" // Default value if read fails or file is empty
        try {
            BufferedReader(FileReader(location)).use { br ->
                value = br.readLine() ?: ":"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // value remains as default
        }
        return value
    }

    @JvmStatic
    fun decryptFile(source: String, destination: String?, d: BigInteger, n: BigInteger): Boolean {
        val sourceBytes = getBytes(source)
        if (isNull(sourceBytes)) {
            return false
        }

        val k = ceil(n.bitLength() / 8.0).toInt()
        var cBigInt: BigInteger
        var mBigInt: BigInteger
        var ebBlock: ByteArray?
        var messageBlock: ByteArray?
        val ciphertextBlocks = reshape(sourceBytes!!, k) ?: return false // If reshape returns null

        try {
            BufferedOutputStream(FileOutputStream(destination)).use { outStream ->
                for (block in ciphertextBlocks) {
                    if (block.size != k) return false
                    cBigInt = BigInteger(1, block) // Ensure positive BigInteger
                    mBigInt = decrypt(cBigInt, d, n)
                    ebBlock = toByteArray(mBigInt, k)
                    if (ebBlock != null) {
                        messageBlock = extractData(ebBlock)
                        if (messageBlock != null) {
                            outStream.write(messageBlock)
                        } else {
                            // Decryption padding/format error
                            Log.e("RSA", "Error extracting data from decrypted block.")
                            return false
                        }
                    } else {
                         // Error converting decrypted BigInteger to byte array
                        Log.e("RSA", "Error converting BigInteger to byte array after decryption.")
                        return false
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("RSA", "IO Exception during decryption: ${e.message}")
            return false
        }
        return true
    }

    /**
     * Extracts the data portion of the byte array.
     * EB = 00 || BT || PS || 00 || D
     * BT must be 02 for decryption.
     */
    private fun extractData(ebBlock: ByteArray): ByteArray? {
        if (ebBlock.size < 12 || ebBlock[0].toInt() != 0x00 || ebBlock[1].toInt() != 0x02) {
            Log.w("RSA", "Decryption Error: EB format check failed. Size: ${ebBlock.size}, Byte0: ${ebBlock[0]}, Byte1: ${ebBlock[1]}")
            return null
        }
        var index = 2
        // Scan for the 00 separator byte after PS
        while (index < ebBlock.size && ebBlock[index].toInt() != 0x00) {
            index++
        }
        if (index == ebBlock.size || index < 10) { // PS must be at least 8 bytes, so 00 || BT || PS(8) || 00 implies index at least 2 + 8 + 1 = 11
            Log.w("RSA", "Decryption Error: Separator 0x00 not found after PS or PS too short.")
            return null
        }
        index++ // Move past the 00 separator

        return getSubArray(ebBlock, index, ebBlock.size)
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
            System.err.println("Can't read $fileName")
            return null
        }

        var bytes: ByteArray? = null
        try {
            FileInputStream(fIn).use { `in` ->
                val fileSize = fIn.length()
                if (fileSize > Int.MAX_VALUE) {
                    println("Sorry, file was too large!")
                    return null // Return null if file is too large
                }
                if (fileSize == 0L) {
                    return ByteArray(0) // Return empty array if file is empty
                }

                bytes = ByteArray(fileSize.toInt())

                var offset = 0
                var numRead: Int
                while (offset < bytes.size) {
                    numRead = `in`.read(bytes, offset, bytes.size - offset)
                    if (numRead < 0) break // End of stream
                    offset += numRead
                }
                 // Check if all bytes were read
                if (offset < bytes.size) {
                    Log.w("RSA", "Could not read the entire file $fileName. Expected ${bytes.size}, got $offset")
                    // Optionally, return a truncated array or null
                    return bytes.copyOf(offset) // Or return null if incomplete read is an error
                }
            }
        } catch (_: IOException) {
             // Ignored, bytes will remain null or partially filled if error occurred mid-read
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
     * EB = 00 || BT || PS || 00 || D
     * For encryption, BT = 02
     */
    @JvmStatic
    fun encryptedFile(source: String, destination: String?, e: BigInteger, n: BigInteger): Boolean {
        val sourceBytes = getBytes(source)
        if (isNull(sourceBytes)) {
            System.err.println("$source contained nothing.")
            return false
        }
        if (sourceBytes!!.isEmpty()){
            System.err.println("$source is empty, nothing to encrypt.")
            // Depending on desired behavior, could return true (empty file encrypted is empty file)
            // or false (nothing was actually encrypted). Let's say false for now.
            return false
        }


        val k = ceil(n.bitLength() / 8.0).toInt() // Size of the RSA modulus in bytes
        val btValue: Byte = 0x02
        
        // Data D must be at most k - 11 bytes long
        // (00 || BT || PS || 00 || D) requires 1 byte for 00, 1 for BT, at least 8 for PS, 1 for 00 separator. Total 11.
        val maxDataBlockLength = k - 11 
        if (maxDataBlockLength <= 0) {
            System.err.println("Key size is too small for PKCS#1 v1.5 padding.")
            return false
        }

        val dataBlocks = reshape(sourceBytes, maxDataBlockLength) ?: return false

        try {
            FileOutputStream(destination).use { outStream ->
                for (dataBlock in dataBlocks) {
                    if (dataBlock.isEmpty()) continue // Should not happen with reshape logic but defensive

                    val paddingStringLength = k - 3 - dataBlock.size // PS = k - mLen - 3
                    val psBytes = makePaddingString(paddingStringLength)
                    if (psBytes == null) {
                        System.err.println("Failed to generate padding string. Check paddingStringLength: $paddingStringLength")
                        return false
                    }

                    val ebStream = ByteArrayOutputStream(k)
                    ebStream.write(0x00)
                    ebStream.write(btValue.toInt())
                    ebStream.write(psBytes)
                    ebStream.write(0x00)
                    ebStream.write(dataBlock)
                    
                    val ebBlock = ebStream.toByteArray()
                    if (ebBlock.size != k) {
                        System.err.println("Encoded block size ${ebBlock.size} does not match k ($k).")
                        return false // Should not happen if logic is correct
                    }
                    val mBigInt = BigInteger(1, ebBlock) // Prepend 1 to ensure positive
                    val cBigInt = encrypt(mBigInt, e, n)
                    val ciphertextBlock = toByteArray(cBigInt, k)
                    if (ciphertextBlock != null) {
                        outStream.write(ciphertextBlock)
                    } else {
                        System.err.println("Failed to convert encrypted BigInteger to byte array.")
                        return false
                    }
                }
            }
        } catch (ex: Exception) {
            System.err.println("An exception occurred during encryption!\n${ex.javaClass}\n${ex.message}\n${ex.stackTraceToString()}")
            return false
        }
        return true
    }

    private fun reshape(inBytes: ByteArray, colSize: Int): Array<ByteArray>? {
        var effectiveColSize = colSize
        if (effectiveColSize < 1) {
            effectiveColSize = 1
        }

        if (inBytes.isEmpty()) {
            return emptyArray() // Or null, depending on desired contract. Empty array seems more robust.
        }

        val rowSize = ceil(inBytes.size.toDouble() / effectiveColSize.toDouble()).toInt()
        if (rowSize == 0 && inBytes.isNotEmpty()) { // Should not happen if inBytes is not empty
             return null
        }
        
        // Correct initialization for Array<ByteArray> (Array of Non-Nullable ByteArray)
        val outBytes = Array(rowSize) { i ->
            getSubArray(inBytes, i * effectiveColSize, (i + 1) * effectiveColSize)!!
        }
        return outBytes
    }

    /**
     * Returns a portion of the array argument.
     */
    private fun getSubArray(inBytes: ByteArray, start: Int, end: Int): ByteArray? {
        var effectiveEnd = end
        if (start >= inBytes.size) {
            // This case should ideally be prevented by reshape logic if rowSize is calculated correctly.
            // If start can be >= inBytes.size, it means an empty block is requested past the end.
            return ByteArray(0) // Return empty array for segments beyond input
        }
        if (effectiveEnd > inBytes.size) {
            effectiveEnd = inBytes.size
        }
        val bytesToGet = effectiveEnd - start
        // bytesToGet can be 0 if start == effectiveEnd, e.g. for the last block if inBytes.size is a multiple of colSize.
        // Or if start >= inBytes.size, bytesToGet can be negative if effectiveEnd remains inBytes.size.
        // The check start >= inBytes.size handles cases where the start is already out of bounds.

        if (bytesToGet <= 0) { // If bytesToGet is 0 or negative (though start >= inBytes.size handles this)
             return ByteArray(0) // Return empty array for zero-length or invalid segments
        }

        val outBytes = ByteArray(bytesToGet)
        System.arraycopy(inBytes, start, outBytes, 0, bytesToGet)
        return outBytes
    }

    /**
     * Converts a BigInteger into a byte array of the specified length.
     * The BigInteger is assumed to be positive.
     */
    private fun toByteArray(x: BigInteger, numBytes: Int): ByteArray? {
        if (x.signum() < 0) { // BigInteger should be positive as it represents an octet string
            Log.e("RSA", "Cannot convert negative BigInteger to byte array for RSA.")
            return null
        }
        // x should be less than 256^numBytes
        // A simple check if x is too large for numBytes: x.bitLength() > numBytes * 8
        if (x.bitLength() > numBytes * 8) {
            // This case can happen if n (modulus for decryption) or c (ciphertext for encryption)
            // is larger than k bytes allows, or if m (message for encryption) results in c >= 256^k
             Log.e("RSA", "BigInteger $x is too large to fit in $numBytes bytes (bitLength: ${x.bitLength()}).")
            return null
        }

        val ba = x.toByteArray() // Standard BigInteger to byte array

        if (ba.size == numBytes) {
            return ba
        } else if (ba.size < numBytes) {
            // Pad with leading zeros if shorter
            val result = ByteArray(numBytes)
            System.arraycopy(ba, 0, result, numBytes - ba.size, ba.size)
            return result
        } else { // ba.size > numBytes
            // This happens if the BigInteger has a leading zero byte in its minimal representation (e.g. positive number whose MSB is 1)
            // And toByteArray() adds a leading 0 byte to indicate positive.
            // Or if the number is simply too big and the bitLength check wasn't sufficient (unlikely for RSA numbers if k is correct)
            if (ba[0].toInt() == 0 && ba.size == numBytes + 1) { // Common case: extra leading zero byte
                return ba.copyOfRange(1, ba.size)
            }
            Log.e("RSA", "Conversion to $numBytes bytes failed. Source array size: ${ba.size}.")
            // This indicates an issue, possibly x was too large and the initial check was insufficient,
            // or x.toByteArray() produced an unexpected result.
            return null
        }
    }

    /**
     * Generates an array of pseudo-random nonzero bytes. Length must be at least 8.
     */
    private fun makePaddingString(len: Int): ByteArray? {
        if (len < 8) {
            Log.e("RSA", "Padding string length $len is less than minimum 8.")
            return null
        }
        val random = SecureRandom() // Use SecureRandom for cryptographic padding

        val psBytes = ByteArray(len)
        for (i in 0..<len) {
            var byteVal: Byte
            do {
                byteVal = (random.nextInt(256) - 128).toByte() // Generate a random byte
            } while (byteVal.toInt() == 0x00) // Ensure it's non-zero
            psBytes[i] = byteVal
        }
        return psBytes
    }
}
