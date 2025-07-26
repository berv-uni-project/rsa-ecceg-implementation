package id.my.berviantoleo.ecceg_rsa_app.lib.ecc

import java.math.BigInteger

class ECC {
    @JvmField
    var a: BigInteger
    @JvmField
    var b: BigInteger
    @JvmField
    var p: BigInteger // y = x^3 + ax + b mod p
    @JvmField
    var k: BigInteger = BIG_ZERO

    init {
        this.a = BIG_ZERO
        this.b = BIG_ZERO
        this.p = BIG_ZERO // p is a prime
    }

    private fun doubled(a: Point): Point {
        val tigaXkuadrat = BigInteger.valueOf(3).multiply(a.x).multiply(a.x)
        val duaY = BigInteger.valueOf(2).multiply(a.y)
        val inverseDuaY = duaY.modInverse(this.p)
        val m = tigaXkuadrat.add(this.a).multiply(inverseDuaY).mod(this.p)

        val x = m.multiply(m).subtract(BigInteger.valueOf(2).multiply(a.x)).add(this.p).mod(this.p)
        val y = m.multiply(a.x?.subtract(x)).subtract(a.y).add(this.p).mod(this.p)
        val point = Point()
        point.x = x
        point.y = y
        return point
    }

    fun add(p: Point, q: Point): Point {
        if (p.infinity && q.infinity) {
            val point = Point()
            point.infinity = true
            return point
        } else if (p.infinity)  // identitas, mengembalikan q
            return q
        else if (q.infinity)  // identitas, mengembalikan p
            return p
        else if (p.x.compareTo(q.x) == 0 && p.y.compareTo(q.y) == 0)  // absis dan ordinat sama, titik yang sama
            return doubled(p)

        // else if (p.x.compareTo(q.x) == 0)
        //     return new Point(true);
        val m =
            p.y.subtract(q.y).multiply(p.x.subtract(q.x).modInverse(this.p)).add(this.p).mod(this.p)
        val x = m.multiply(m).subtract(p.x).subtract(q.x).add(this.p).mod(this.p)
        val y = m.multiply(p.x.subtract(x)).subtract(p.y).add(this.p).mod(this.p)
        val point = Point()
        point.x = x
        point.y = y
        return point
    }

    private fun multiplyGenap(
        n: BigInteger,
        p: Point
    ): Point { // rekursif, n diharapkan genap, n tidak 0
        // System.out.println(n);
        if (n == BIG_ONE) { // basis
            // System.out.println("cek1");
            return p
        } else if (n.mod(BIG_TWO) == BIG_ZERO) { // rekurens
            // System.out.println("cek2");
            return multiplyGenap(n.divide(BIG_TWO), doubled(p))
        } else return multiplyGanjil(n, p)
    }

    private fun multiplyGanjil(n: BigInteger, p: Point): Point { // rekursif, n ganjil tidak 0
        // System.out.println(n);
        if (n == BIG_ONE)  // basis
            return p
        else {
            // System.out.println("cek3");
            return add(multiplyGenap(n.subtract(BigInteger.valueOf(1)), p), p)
        }
    }

    fun multiply(n: BigInteger, p: Point): Point {
        if (n == BIG_ZERO) return Point()
        else if (n.mod(BIG_TWO) == BIG_ZERO)  // rekurens
            return multiplyGenap(n.divide(BIG_TWO), doubled(p))
        else return multiplyGanjil(n, p)
    }

    private fun cariY(x: BigInteger): BigInteger? {
        val xPangkatTiga = x.multiply(x).multiply(x)
        val axPlusb = this.a.multiply(x).add(b)
        val y2 = xPangkatTiga.add(axPlusb).mod(p)
        return sqrtP(y2, p)
    }

    fun intToPoint(m: BigInteger): Point {
        val mk = m.multiply(k)
        var i: BigInteger = BIG_ONE
        while (i.compareTo(k) < 0) {
            val x = mk.add(i)
            val y = cariY(x)
            if (y != null) {
                val point = Point()
                point.x = x.mod(p)
                point.y = y.mod(p)
                return point
            }
            i = i.add(BIG_ONE)
        }
        val point = Point()
        point.y = BigInteger.valueOf(-1)
        point.x = point.y
        return point
    }

    fun pointToInt(p: Point): BigInteger {
        return p.x.subtract(BIG_ONE).divide(this.k)
    }

    val basePoint: Point?
        get() {
            var x: BigInteger = BIG_ZERO
            while (x.compareTo(this.p) < 0) {
                val y = cariY(x)
                if (y != null) {
                    val point =
                        Point()
                    point.x = x
                    point.y = y
                    return point
                }
                x = x.add(BIG_ONE)
            }
            return null
        }

    private fun sqrtP(x: BigInteger, p: BigInteger): BigInteger? {
        if (p.mod(BIG_TWO) == BIG_ZERO) return null
        var q = p.subtract(BIG_ONE).divide(BIG_TWO)
        if (x.modPow(q, p) != BIG_ONE) return null

        while (q.mod(BIG_TWO) == BIG_ZERO) {
            q = q.divide(BIG_TWO)
            if (x.modPow(q, p) != BIG_ONE) return complexSqrtP(x, q, p)
        }
        q = q.add(BIG_ONE).divide(BIG_TWO)
        return x.modPow(q, p)
    }


    companion object {
        private val BIG_ZERO: BigInteger = BigInteger.ZERO
        private val BIG_ONE: BigInteger = BigInteger.valueOf(1)
        private val BIG_TWO: BigInteger = BigInteger.valueOf(2)

        private fun findNonResidue(p: BigInteger): BigInteger? {
            var a: BigInteger? = BIG_TWO
            val q = p.subtract(BIG_ONE).divide(BIG_TWO)
            while (true) {
                if (a!!.modPow(q, p) == BIG_ONE) return a

                a = a.add(BIG_ONE)
                if (a.compareTo(p) >= 0) return null
            }
        }

        private fun complexSqrtP(x: BigInteger, q: BigInteger, p: BigInteger): BigInteger? {
            var q = q
            val a: BigInteger? = findNonResidue(p)
            if (a == null) return null
            var t = p.subtract(BIG_ONE).divide(BIG_TWO)
            val minusPower = t

            while (q.mod(BIG_TWO) == BIG_ZERO) {
                q = q.divide(BIG_TWO)
                t = t.divide(BIG_TWO)
                if (x.modPow(q, p).compareTo(a.modPow(t, p)) != 0) t = t.add(minusPower)
            }
            val inverseX = x.modInverse(p)
            val partOne = inverseX.modPow(q.subtract(BIG_ONE).divide(BIG_TWO), p)
            val partTwo = a.modPow(t.divide(BIG_TWO), p)
            return partOne.multiply(partTwo).mod(p)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val ecc = ECC()
            ecc.a = BigInteger.valueOf(2)
            ecc.b = BigInteger.valueOf(6)
            ecc.p = BigInteger.valueOf(15485867)
            var point1 = Point()
            point1.x = BigInteger.valueOf(2)
            point1.y = BigInteger.valueOf(6)
            println(point1.toString())
            val n = BigInteger.valueOf(5)
            point1 = ecc.multiply(n, point1)
            println(point1.toString())
        }
    }
}
