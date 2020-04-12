package id.my.berviantoleo.ecceg_rsa_app.lib.ecc;

import java.math.BigInteger;

import androidx.annotation.NonNull;

public class Point {
    public BigInteger x, y; // x = absis, y = ordinat
    boolean infinity; // titik O, elemen identitas

    public Point() {
        x = BigInteger.ZERO;
        y = BigInteger.ZERO;
        infinity = false;
    }

    @NonNull
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
