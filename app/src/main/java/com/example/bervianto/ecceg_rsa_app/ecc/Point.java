package com.example.bervianto.ecceg_rsa_app.ecc;

import java.math.BigInteger;

public class Point {
	public BigInteger x, y; // x = absis, y = ordinat
	public boolean infinity; // titik O, elemen identitas

	public Point() {
		x = BigInteger.ZERO;
		y = BigInteger.ZERO;
		infinity = false;
	}

	public String toString(){
		return "(" + x + ", " + y + ")";
	}
}
