import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.Random;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.BufferedOutputStream;

class RSA {
    BigInteger p;
    BigInteger q;
    BigInteger n;
    BigInteger phi;
    BigInteger pubExp;
    BigInteger priExp;

    public static void main(String[] args) {
        RSA rsa = new RSA();
        rsa.generateKey();
        rsa.encryptedFile("README.md", "README-ENCRYPTED", rsa.pubExp, rsa.n);
        rsa.decryptFile("README-ENCRYPTED", "README-DECRYPTED.md", rsa.priExp, rsa.n);
    }

    /** Returns true when the argument is null. */
    public final boolean isNull(Object obj) {
        return !(obj != null);
    }

    public void generateKey() {
        int bitlength = 1024;
        SecureRandom rnd = new SecureRandom();
        p = BigInteger.probablePrime(75 * bitlength / 100, rnd);
        q = BigInteger.probablePrime(25 * bitlength / 100, rnd);
        System.out.println("P:" + p);
        System.out.println("Q:" + q);
        n = p.multiply(q);
        phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        System.out.println("n:" + n);
        System.out.println("phi:" + phi);
        BigInteger i;
        for (i = BigInteger.probablePrime(bitlength / 10, rnd); i.compareTo(n) < 0; i = i.nextProbablePrime()) {
            if (i.gcd(phi).equals(BigInteger.ONE)) {
                pubExp = i;
                break;
            }
        }
        priExp = pubExp.modInverse(phi);
    }

    public boolean decryptFile(String source, String destination, BigInteger d, BigInteger n) {
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
                if (isNull(out))
                    out.close();
            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }

    /** Extracts the data portion of the byte array. */
    protected byte[] extractData(byte[] EB) {
        if (EB.length < 12 || EB[0] != 0x00 || EB[1] != 0x02) {
            return null;
        }
        int index = 2;
        do {
        } while (EB[index++] != 0x00);

        return getSubArray(EB, index, EB.length);
    }

    /** Performs the classical RSA computation. */
    protected BigInteger decrypt(BigInteger c, BigInteger d, BigInteger n) {
        return c.modPow(d, n);
    }

    protected byte[] getBytes(String fileName) {
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
    protected BigInteger encrypt(BigInteger m, BigInteger e, BigInteger n) {
        return m.modPow(e, n);
    }

    /** Uses the key and returns true if encryption was successful. */
    public boolean encryptedFile(String source, String destination, BigInteger e, BigInteger n) {
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

    protected byte[][] reshape(byte[] inBytes, int colSize) {
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
    protected byte[] getSubArray(byte[] inBytes, int start, int end) {
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
    protected byte[] toByteArray(BigInteger x, int numBytes) {
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
    protected byte[] makePaddingString(int len) {
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