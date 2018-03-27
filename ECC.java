import java.math.BigInteger;

public class ECC {
    public BigInteger a, b, p; // y = x^3 + ax + b mod p
    private BigInteger k = BigInteger.valueOf(20);
    private static final BigInteger BIG_ZERO = BigInteger.ZERO, BIG_ONE = BigInteger.valueOf(1),
        BIG_TWO = BigInteger.valueOf(2);

    public ECC() {
        this.a = BIG_ZERO;
        this.b = BIG_ZERO;
        this.p = BIG_ZERO; // p is a prime
    }

    public Point doubled(Point a) {
        BigInteger tigaXkuadrat = BigInteger.valueOf(3).multiply(a.x).multiply(a.x);
        BigInteger duaY = BigInteger.valueOf(2).multiply(a.y);
        BigInteger inverseDuaY = duaY.modInverse(this.p);
        BigInteger m = tigaXkuadrat.add(this.a).multiply(inverseDuaY).mod(this.p);

        BigInteger x = m.multiply(m).subtract(BigInteger.valueOf(2).multiply(a.x)).add(this.p).mod(this.p);
        BigInteger y = m.multiply(a.x.subtract(x)).subtract(a.y).add(this.p).mod(this.p);
        Point point = new Point();
        point.x = x;
        point.y = y;
        return point;
    }

    public Point add(Point p, Point q) {
        if (p.infinity && q.infinity) {
            Point point = new Point();
            point.infinity = true;
            return point;
        } else if (p.infinity) // identitas, mengembalikan q
            return q;
        else if (q.infinity) // identitas, mengembalikan p
            return p;
        else if (p.x.compareTo(q.x) == 0 && p.y.compareTo(q.y) == 0) // absis dan ordinat sama, titik yang sama
            return doubled(p);
        // else if (p.x.compareTo(q.x) == 0)
        //     return new Point(true);

        BigInteger m = p.y.subtract(q.y).multiply(p.x.subtract(q.x).modInverse(this.p)).add(this.p).mod(this.p);
        BigInteger x = m.multiply(m).subtract(p.x).subtract(q.x).add(this.p).mod(this.p);
        BigInteger y = m.multiply(p.x.subtract(x)).subtract(p.y).add(this.p).mod(this.p);
        Point point = new Point();
        point.x = x;
        point.y = y;
        return point;
    }

    public Point multiplyGenap(BigInteger n, Point p) { // rekursif, n diharapkan genap, n tidak 0
        // System.out.println(n);
        if (n.equals(BIG_ONE)) { // basis
            // System.out.println("cek1");
            return p;
        }
        else if (n.mod(BIG_TWO).equals(BIG_ZERO)) { // rekurens
            // System.out.println("cek2");
            return multiplyGenap(n.divide(BIG_TWO), doubled(p));
        }
        else return multiplyGanjil(n, p);
    }

    public Point multiplyGanjil(BigInteger n, Point p) { // rekursif, n ganjil tidak 0
        // System.out.println(n);
        if (n.equals(BIG_ONE)) // basis
            return p;
        else {
            // System.out.println("cek3");
            return add(multiplyGenap(n.subtract(BigInteger.valueOf(1)), p), p);
        }
    }

    public Point multiply(BigInteger n, Point p) {
        if (n.equals(BIG_ZERO))
            return new Point();
        else if (n.mod(BIG_TWO).equals(BIG_ZERO)) // rekurens
            return multiplyGenap(n.divide(BIG_TWO), doubled(p));
        else return multiplyGanjil(n, p);
    }

    public BigInteger cariY(BigInteger x) {
        BigInteger xPangkatTiga = x.multiply(x).multiply(x);
        BigInteger axPlusb = this.a.multiply(x).add(b);
        BigInteger y2 = xPangkatTiga.add(axPlusb).mod(p);
        return sqrtP(y2, p);
    }

    public Point intToPoint(BigInteger m) {
        BigInteger mk = m.multiply(k);
        for (BigInteger i = BIG_ONE; i.compareTo(k) < 0; i = i.add(BIG_ONE)) {
            BigInteger x = mk.add(i);
            BigInteger y = cariY(x);
            if (y != null) {
                Point point = new Point();
                point.x = x.mod(p);
                point.y = y.mod(p);
                return point;
            }
        }
        Point point = new Point();
        point.x = point.y = BigInteger.valueOf(-1);
        return point;
    }

    public BigInteger pointToInt(Point p) {
        return p.x.subtract(BIG_ONE).divide(this.k);
    }

    public Point getBasePoint() {
        for (BigInteger x = BIG_ZERO; x.compareTo(this.p) < 0; x = x.add(BIG_ONE)) {
            BigInteger y = cariY(x);
            if (y != null) {
                Point point = new Point();
                point.x = x;
                point.y = y;
                return point;
            }
        }
        return null;
    }

    private static BigInteger findNonResidue(BigInteger p) {
        BigInteger a = BIG_TWO;
        BigInteger q = p.subtract(BIG_ONE).divide(BIG_TWO);
        while (true) {
            if (a.modPow(q, p).equals(BIG_ONE))
                return a;

            a = a.add(BIG_ONE);
            if (a.compareTo(p) >= 0)
                return null;
        }
    }

    private static BigInteger complexSqrtP(BigInteger x, BigInteger q, BigInteger p) {
        BigInteger a = findNonResidue(p);
        if (a == null)
            return null;
        BigInteger t = p.subtract(BIG_ONE).divide(BIG_TWO);
        BigInteger minusPower = t;

        while (q.mod(BIG_TWO).equals(BIG_ZERO)) {
            q = q.divide(BIG_TWO);
            t = t.divide(BIG_TWO);
            if (x.modPow(q, p).compareTo(a.modPow(t, p)) != 0)
                t = t.add(minusPower);
        }
        BigInteger inverseX = x.modInverse(p);
        BigInteger partOne = inverseX.modPow(q.subtract(BIG_ONE).divide(BIG_TWO), p);
        BigInteger partTwo = a.modPow(t.divide(BIG_TWO), p);
        return partOne.multiply(partTwo).mod(p);
    }

    public BigInteger sqrtP(BigInteger x, BigInteger p) {
        if (p.mod(BIG_TWO).equals(BIG_ZERO))
            return null;
        BigInteger q = p.subtract(BIG_ONE).divide(BIG_TWO);
        if (!x.modPow(q, p).equals(BIG_ONE))
            return null;

        while (q.mod(BIG_TWO).equals(BIG_ZERO)) {
            q = q.divide(BIG_TWO);
            if (!x.modPow(q, p).equals(BIG_ONE))
                return complexSqrtP(x, q, p);
        }
        q = q.add(BIG_ONE).divide(BIG_TWO);
        return x.modPow(q, p);
    }


    public static void main(String[] args) {
        ECC ecc = new ECC();
        ecc.a = BigInteger.valueOf(2);
        ecc.b = BigInteger.valueOf(6);
        ecc.p = BigInteger.valueOf(15485867);
        Point point1 = new Point();
        point1.x = BigInteger.valueOf(2);
        point1.y = BigInteger.valueOf(6);
        System.out.println(point1.toString());
        BigInteger n = BigInteger.valueOf(5);
        point1 = ecc.multiply(n, point1);
        System.out.println(point1.toString());
    }
    
}
