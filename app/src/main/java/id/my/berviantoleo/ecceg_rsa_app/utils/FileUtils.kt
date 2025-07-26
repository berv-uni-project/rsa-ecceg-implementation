package id.my.berviantoleo.ecceg_rsa_app.utils

import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.Pair
import id.my.berviantoleo.ecceg_rsa_app.lib.ecc.Point
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.ByteBuffer

object FileUtils {
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


    @JvmStatic
    fun saveFile(stringpath: String?, content: ByteArray?) {
        try {
            val fos = FileOutputStream(stringpath)
            fos.write(content)
            fos.close()
        } catch (ignored: IOException) {
        }
    }

    private fun intToBytes(x: Int): ByteArray {
        return ByteBuffer.allocate(4).putInt(x).array()
    }

    private fun bytesToInt(bytes: ByteArray): Int {
        return ByteBuffer.wrap(bytes).getInt()
    }

    fun savePointsToFile(path: String?, pairpoints: MutableList<Pair<Point?, Point?>>) {
        val b = ByteArray(pairpoints.size * 16)
        var j = 0
        for (ppoint in pairpoints) {
            var btemp = intToBytes(ppoint.left!!.x.toInt())
            for (aBtemp in btemp) {
                b[j] = aBtemp
                j++
            }
            btemp = intToBytes(ppoint.left!!.y.toInt())
            for (aBtemp in btemp) {
                b[j] = aBtemp
                j++
            }
            btemp = intToBytes(ppoint.right!!.x.toInt())
            for (aBtemp in btemp) {
                b[j] = aBtemp
                j++
            }
            btemp = intToBytes(ppoint.right!!.y.toInt())
            for (aBtemp in btemp) {
                b[j] = aBtemp
                j++
            }
        }
        saveFile(path, b)
    }

    private const val HEXES = "0123456789ABCDEF"

    private fun isNull(obj: Any?): Boolean {
        return obj == null
    }

    @JvmStatic
    fun showHexFromFile(file: String): String {
        val sourceBytes = getBytes(file)
        if (isNull(sourceBytes)) {
            return ""
        }
        return FileUtils.getHex(sourceBytes!!)
    }

    private fun getHex(raw: ByteArray): String {
        val hex = StringBuilder(2 * raw.size)
        for (b in raw) {
            hex.append(HEXES.get((b.toInt() and 0xF0) shr 4))
                .append(HEXES.get((b.toInt() and 0x0F)))
        }
        return hex.toString()
    }


    @JvmStatic
    fun loadPointsFromFile(stringpath: String): MutableList<Pair<Point?, Point?>?> {
        val rawData = getBytes(stringpath)
        val pair: MutableList<Pair<Point?, Point?>?> = ArrayList<Pair<Point?, Point?>?>()
        val btemp = ByteArray(4)
        var f = 0
        var s: Int
        var point1 = Point()
        point1.x = BigInteger.valueOf(1)
        point1.y = BigInteger.valueOf(1)
        var point2 = Point()
        point2.x = BigInteger.valueOf(1)
        point2.y = BigInteger.valueOf(1)
        for (i in 0..<(if (rawData != null) rawData.size else 0)) {
            btemp[i % 4] = rawData!![i]
            if (i % 4 == 3) {
                if ((i / 4) % 4 == 0) {
                    f = bytesToInt(btemp)
                }
                if ((i / 4) % 4 == 1) {
                    s = bytesToInt(btemp)
                    point1 = Point()
                    point1.x = BigInteger.valueOf(f.toLong())
                    point1.y = BigInteger.valueOf(s.toLong())
                }
                if ((i / 4) % 4 == 2) {
                    f = bytesToInt(btemp)
                }
                if ((i / 4) % 4 == 3) {
                    s = bytesToInt(btemp)
                    point2 = Point()
                    point2.x = BigInteger.valueOf(f.toLong())
                    point2.y = BigInteger.valueOf(s.toLong())
                    pair.add(Pair<Point?, Point?>(point1, point2))
                }
            }
        }
        return pair
    }
}
