package id.my.berviantoleo.ecceg_rsa_app.lib.ecc

import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.SecureRandom
import java.util.Random
import java.util.Scanner

class ECCEG {
    @JvmField
    var publicKey: Point
    private var privateKey: BigInteger?
    val basePoint: Point
    val eCC: ECC

    constructor(ecc: ECC, basePoint: Point) {
        this.eCC = ecc
        this.basePoint = basePoint
        this.privateKey = BigInteger(ecc.p.bitLength(), Random())
            .mod(ecc.p.subtract(BigInteger.ONE))
            .add(BigInteger.ONE)
        this.publicKey = ecc.multiply(privateKey!!, basePoint)
    }

    constructor(ecc: ECC, basePoint: Point, privateKey: BigInteger) {
        this.eCC = ecc
        this.basePoint = basePoint
        this.privateKey = privateKey
        this.publicKey = ecc.multiply(privateKey, basePoint)
    }

    fun getPrivateKey(): BigInteger {
        return this.privateKey!!
    }

    fun setPrivateKey(privateKey: BigInteger) {
        this.privateKey = privateKey
    }

    @Throws(Exception::class)
    fun savePublicKey(fileName: String) {
        // disimpan x dan y, dipisahkan dengan spasi
        val file = File(fileName)
        if (!file.exists()) file.createNewFile()
        val out = FileOutputStream(file)
        out.write(publicKey.x.toString().toByteArray())
        out.write(' '.code)
        out.write(publicKey.y.toString().toByteArray())
        out.flush()
        out.close()
    }

    @Throws(Exception::class)
    fun savePrivateKey(fileName: String) {
        val file = File(fileName)
        if (!file.exists()) file.createNewFile()
        val out = FileOutputStream(file)
        out.write(privateKey.toString().toByteArray())
        out.flush()
        out.close()
    }

    @Throws(Exception::class)
    fun loadPublicKey(fileName: String) {
        val file = File(fileName)
        val sc = Scanner(file)
        var x: BigInteger? = null
        var y: BigInteger? = null
        if (sc.hasNextBigInteger()) x = sc.nextBigInteger()
        if (sc.hasNextBigInteger()) y = sc.nextBigInteger()
        sc.close()
        if (x != null && y != null) {
            val point = Point()
            point.x = x
            point.y = y
            this.publicKey = point
        }
    }

    @Throws(Exception::class)
    fun loadPrivateKey(fileName: String) {
        val file = File(fileName)
        val sc = Scanner(file)
        var i: BigInteger? = null
        if (sc.hasNextBigInteger()) i = sc.nextBigInteger()
        sc.close()
        if (i != null) {
            this.privateKey = i
        }
    }

    fun encrypt(p: Point): Pair<Point?, Point?> {
        val k = BigInteger(eCC.p.bitLength(), SecureRandom())
            .mod(eCC.p.subtract(BigInteger.ONE))
            .add(BigInteger.ONE)
        val left: Point = eCC.multiply(k, basePoint)
        val right: Point = eCC.add(p, eCC.multiply(k, publicKey))
        return Pair<Point?, Point?>(left, right) // This Pair is non-null
    }

    // Changed return type here
    fun encryptBytes(bytes: ByteArray): MutableList<Pair<Point?, Point?>> {
        // The list itself is non-null. It contains non-null Pair objects.
        val ret = ArrayList<Pair<Point?, Point?>>() 
        for (aByte in bytes) {
            // encrypt() returns a non-null Pair, so we can add it directly.
            ret.add(encrypt(eCC.intToPoint(BigInteger.valueOf(aByte.toLong()))))
        }
        return ret
    }

    fun decrypt(p: Pair<Point?, Point?>): Point {
        val m: Point = eCC.multiply(privateKey!!, p.left!!)

        val minusM = Point()
        minusM.x = m.x
        minusM.y = m.y.negate().mod(eCC.p)
        return eCC.add(p.right!!, minusM)
    }

    fun decrypt(l: MutableList<Pair<Point?, Point?>>): MutableList<Point?> {
        val ret = ArrayList<Point?>()
        for (p in l) ret.add(decrypt(p))
        return ret
    }
}
