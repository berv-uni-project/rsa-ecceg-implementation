package com.example.bervianto.ecceg_rsa_app.rsa;

import android.util.Log;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.Random;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;

public class RSA {

    /** Returns true when the argument is null. */
    private static boolean isNull(Object obj) {
        return obj == null;
    }

    public static void generateKey(int bitLength, String privateName, String publicName) {
        SecureRandom rnd = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(75 * bitLength / 100, rnd);
        BigInteger q = BigInteger.probablePrime(25 * bitLength / 100, rnd);
        BigInteger n = p.multiply(q);
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        BigInteger i;
        BigInteger pubExp = BigInteger.ONE;
        for (i = BigInteger.probablePrime(bitLength / 10, rnd); i.compareTo(n) < 0; i = i.nextProbablePrime()) {
            if (i.gcd(phi).equals(BigInteger.ONE)) {
                pubExp = i;
                break;
            }
        }
        BigInteger priExp = pubExp.modInverse(phi);
        writeKeyToFile(privateName, n, priExp);
        writeKeyToFile(publicName, n, pubExp);
    }

    private static final String HEXES = "0123456789ABCDEF";

    public static String showHexFromFile(String file) {
        byte[] sourceBytes = getBytes(file);
        if (isNull(sourceBytes)) {
            return "";
        }
        return getHex(sourceBytes);
    }

    private static String getHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    private static void writeKeyToFile(String name, BigInteger n, BigInteger d) {
        String output = n.toString() + ":" + d.toString();
        try {
            FileOutputStream writer = new FileOutputStream(name);
            OutputStreamWriter outWriter = new OutputStreamWriter(writer);
            outWriter.write(output);
            outWriter.close();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Log.e("RSA",e.getMessage());
        }
    }

    public static String readKey(String location) {
        String value = ":";
        try (BufferedReader br = new BufferedReader(new FileReader(location))) {
            String sCurrentLine = br.readLine();
            if (sCurrentLine != null) {
                value = sCurrentLine;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return value;
        }
        return value;
    }

    public static boolean decryptFile(String source, String destination, BigInteger d, BigInteger n) {
        byte[] sourceBytes = getBytes(source);
        if (isNull(sourceBytes)) {
            return false;
        }

        int k = (int) Math.ceil(n.bitLength() / 8.0);
        BigInteger c, m;
        byte[] EB, M;
        byte[][] C = reshape(sourceBytes, k);
        BufferedOutputStream out = null;

        try {
            out = new BufferedOutputStream(new FileOutputStream(destination));
            for (int i = 0; i < C.length; i++) {
                if (C[i].length != k)
                    return false;
                c = new BigInteger(C[i]);
                m = decrypt(c, d, n);
                EB = toByteArray(m, k);
                M = extractData(EB);
                out.write(M);
            }
            out.close();
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (!isNull(out))
                    out.close();
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    /** Extracts the data portion of the byte array. */
    private static byte[] extractData(byte[] EB) {
        if (EB.length < 12 || EB[0] != 0x00 || EB[1] != 0x02) {
            return null;
        }
        int index = 2;
        do {
        } while (EB[index++] != 0x00);

        return getSubArray(EB, index, EB.length);
    }

    /** Performs the classical RSA computation. */
    private static BigInteger decrypt(BigInteger c, BigInteger d, BigInteger n) {
        return c.modPow(d, n);
    }

    public static byte[] getBytes(String fileName) {
        File fIn = new File(fileName);
        if (!fIn.canRead()) {
            System.err.println("Can't read " + fileName);
            return null;
        }

        FileInputStream in = null;
        byte[] bytes = null;
        try {
            in = new FileInputStream(fIn);

            long fileSize = fIn.length();
            if (fileSize > Integer.MAX_VALUE) {
                System.out.println("Sorry, file was too large!");
            }

            bytes = new byte[(int) fileSize];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = in.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } catch (IOException e) {
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
            }
        }

        return bytes;
    }

    /** Performs the classical RSA computation. */
    private static BigInteger encrypt(BigInteger m, BigInteger e, BigInteger n) {
        return m.modPow(e, n);
    }

    /** Uses the key and returns true if encryption was successful. */
    public static boolean encryptedFile(String source, String destination, BigInteger e, BigInteger n) {
        byte[] sourceBytes = getBytes(source);
        if (isNull(sourceBytes)) {
            System.err.println(String.format("%s contained nothing.", source));
            return false;
        }

        int k = (int) Math.ceil(n.bitLength() / 8.0);
        byte BT = 0x02;
        byte[] C, M;
        byte[][] D = reshape(sourceBytes, k - 11);
        ByteArrayOutputStream EB = new ByteArrayOutputStream(k);
        FileOutputStream out = null;
        BigInteger m, c;

        try {
            out = new FileOutputStream(destination);
            for (int i = 0; i < D.length; i++) {
                EB.reset();
                EB.write(0x00);
                EB.write(BT);
                EB.write(makePaddingString(k - D[i].length - 3));
                EB.write(0x00);
                EB.write(D[i]);
                M = EB.toByteArray();
                m = new BigInteger(M);
                c = encrypt(m, e, n);
                C = toByteArray(c, k);
                out.write(C);
            }
            out.close();
        } catch (Exception ex) {
            String errMsg = "An exception occured!%n%s%n%s%n%s";
            System.err.println(String.format(errMsg, ex.getClass(), ex.getMessage(), ex.getStackTrace()));
            return false;
        }

        return true;
    }

    private static byte[][] reshape(byte[] inBytes, int colSize) {
        if (colSize < 1) {
            colSize = 1;
        }

        int rowSize = (int) Math.ceil((double) inBytes.length / (double) colSize);

        if (rowSize == 0) {
            return null;
        }

        byte[][] outBytes = new byte[rowSize][];

        for (int i = 0; i < rowSize; i++) {
            outBytes[i] = getSubArray(inBytes, i * colSize, (i + 1) * colSize);
        }
        return outBytes;
    }

    /** Returns a portion of the array argument. */
    private static byte[] getSubArray(byte[] inBytes, int start, int end) {
        if (start >= inBytes.length) {
            return null;
        }
        if (end > inBytes.length) {
            end = inBytes.length;
        }
        int bytesToGet = end - start;
        if (bytesToGet < 1) {
            return null;
        }

        byte[] outBytes = new byte[bytesToGet];
        for (int i = start; i < end; i++) {
            outBytes[i - start] = inBytes[i];
        }

        return outBytes;
    }

    /** Converts a BigInteger into a byte array of the specified length. */
    private static byte[] toByteArray(BigInteger x, int numBytes) {
        if (x.compareTo(BigInteger.valueOf(256).pow(numBytes)) >= 0) {
            return null; // number is to big to fit in the byte array
        }

        byte[] ba = new byte[numBytes--];
        BigInteger[] divAndRem = new BigInteger[2];

        for (int power = numBytes; power >= 0; power--) {
            divAndRem = x.divideAndRemainder(BigInteger.valueOf(256).pow(power));
            ba[numBytes - power] = (byte) divAndRem[0].intValue();
            x = divAndRem[1];
        }

        return ba;
    }

    /** Generates an array of pseudo-random nonzero bytes. */
    private static byte[] makePaddingString(int len) {
        if (len < 8)
            return null;
        Random rndm = new Random();

        byte[] PS = new byte[len];
        for (int i = 0; i < len; i++) {
            PS[i] = (byte) (rndm.nextInt(255) + 1);
        }

        return PS;
    }

}