package id.my.berviantoleo.ecceg_rsa_app.lib.ecc;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ECCEG {
    private Point publicKey;
    private BigInteger privateKey;
    private Point basePoint;
    private ECC ECC;

    public ECCEG(ECC ECC, Point basePoint) {
        this.ECC = ECC;
        this.basePoint = basePoint;
        this.privateKey = new BigInteger(ECC.p.bitLength(), new Random())
            .mod(ECC.p.subtract(BigInteger.ONE))
            .add(BigInteger.ONE);
        this.publicKey = ECC.multiply(privateKey, basePoint);
    }

    public ECCEG(ECC ECC, Point basePoint, BigInteger privateKey) {
        this.ECC = ECC;
        this.basePoint = basePoint;
        this.privateKey = privateKey;
        this.publicKey = ECC.multiply(privateKey, basePoint);
    }

    public Point getPublicKey() { return this.publicKey; }
    public BigInteger getPrivateKey() { return this.privateKey; }
    public ECC getECC() { return this.ECC; }
    public Point getBasePoint() { return this.basePoint; }

    public void setPublicKey(Point publicKey) { this.publicKey = publicKey; }
    public void setPrivateKey(BigInteger privateKey) { this.privateKey = privateKey; }

    public void savePublicKey(String fileName) throws Exception {
        // disimpan x dan y, dipisahkan dengan spasi
        File file = new File(fileName);
        if (!file.exists()) file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        out.write(publicKey.x.toString().getBytes());
        out.write(' ');
        out.write(publicKey.y.toString().getBytes());
        out.flush();
        out.close();
    }

    public void savePrivateKey(String fileName) throws Exception {
        File file = new File(fileName);
        if (!file.exists()) file.createNewFile();
        FileOutputStream out = new FileOutputStream(file);
        out.write(privateKey.toString().getBytes());
        out.flush();
        out.close();
    }

    public void loadPublicKey(String fileName) throws Exception {
        File file = new File(fileName);
        Scanner sc = new Scanner(file);
        BigInteger x = null, y = null;
        if (sc.hasNextBigInteger()) x = sc.nextBigInteger();
        if (sc.hasNextBigInteger()) y = sc.nextBigInteger();
        sc.close();
        if (x != null && y != null) {
            Point point = new Point();
            point.x = x;
            point.y = y;
            this.publicKey = point;
        }
    }

    public void loadPrivateKey(String fileName) throws Exception {
        File file = new File(fileName);
        Scanner sc = new Scanner(file);
        BigInteger i = null;
        if (sc.hasNextBigInteger()) i = sc.nextBigInteger();
        sc.close();
        if (i != null) {
            this.privateKey = i;
        }
    }

    public Pair<Point, Point> encrypt(Point p) {
        BigInteger k = new BigInteger(ECC.p.bitLength(), new SecureRandom())
            .mod(ECC.p.subtract(BigInteger.ONE))
            .add(BigInteger.ONE);
        Point left = ECC.multiply(k, basePoint);
        Point right = ECC.add(p, ECC.multiply(k, publicKey));
        return new Pair<Point, Point>(left, right);
    }

    public List<Pair<Point, Point>> encryptBytes(byte[] bytes) {
        List<Pair<Point, Point>> ret = new ArrayList<>();
        for (byte aByte : bytes) ret.add(encrypt(ECC.intToPoint(BigInteger.valueOf(aByte))));
        return ret;
    }

    public Point decrypt(Pair<Point, Point> p) {
        Point m = ECC.multiply(privateKey, p.left);

        Point minusM = new Point();
        minusM.x = m.x;
        minusM.y = m.y.negate().mod(ECC.p);
        return ECC.add(p.right, minusM);
    }

    public List<Point> decrypt(List<Pair<Point, Point>> l) {
        List<Point> ret = new ArrayList<>();
        for (Pair<Point, Point> p: l)
            ret.add(decrypt(p));
        return ret;
    }
}
