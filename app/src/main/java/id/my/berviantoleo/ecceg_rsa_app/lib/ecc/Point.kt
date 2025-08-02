package id.my.berviantoleo.ecceg_rsa_app.lib.ecc

import java.math.BigInteger

class Point {
    var x: BigInteger = BigInteger.ZERO
    var y: BigInteger = BigInteger.ZERO // x = absis, y = ordinat
    var infinity: Boolean = false // titik O, elemen identitas

    override fun toString(): String {
        return if (infinity) "Point(O)" else "($x, $y)"
    }
}
