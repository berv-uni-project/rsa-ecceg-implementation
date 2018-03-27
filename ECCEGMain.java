import java.math.BigInteger;
import java.util.*;
import java.io.*;

public class ECCEGMain {
    public static void main(String[] args) throws Exception { // unhandled exception

        // Initiate Classes Component
        BigInteger a = new BigInteger("2"), b = new BigInteger("6"), p = new BigInteger("15485867"),
            k = new BigInteger("30");
        // y = x^3 + 2x + 6 mod 15485867
        // this is programmer defined, but needs to be consistent for encrypt and decrypt
        ECC ecc = new ECC();
        ecc.a = a;
        ecc.b = b;
        ecc.p = p;
        ecc.k = k;
        ECCEG ecceg = new ECCEG(ecc, ecc.getBasePoint());
        // Initiate ended

        String private_key_filepath = "key.pri";
        String public_key_filepath = "key.pub";
        ECCEGMain.generateKey(ecceg, private_key_filepath, public_key_filepath);

        String plainfile = "datatest.txt";
        String ciphfile = plainfile + ".ciph";
        ECCEGMain.encryptFile(ecc, ecceg, plainfile, ciphfile);

        // String private_key_filepath = "key.pri";
        // String public_key_filepath = "key.pub";
        // String plainfile = "datatest.txt";
        // String ciphfile = "datatest.txt.ciph";
        ECCEGMain.decryptFile(ecc, private_key_filepath, public_key_filepath, plainfile, ciphfile);
    }

    public static void generateKey(ECCEG ecceg, String private_key_filepath, String public_key_filepath) throws Exception {
        System.out.print("Private key = ");
        System.out.println(ecceg.getPrivateKey());
        ecceg.savePrivateKey(private_key_filepath);
        System.out.println("Saved in " + private_key_filepath);
        System.out.print("Public key = ");
        System.out.println(ecceg.getPublicKey());
        ecceg.savePublicKey(public_key_filepath);
        System.out.println("Saved in " + public_key_filepath);
    }

    public static void encryptFile(ECC ecc, ECCEG ecceg, String filepath_plain, String filepath_ciph) {
        FileReader fr = new FileReader();
        byte[] read = fr.fileToBytes(filepath_plain);
        System.out.println("---===Plainteks===---");
        System.out.println(new String(read));
        System.out.println("---======END======---");
        System.out.println();
        List<Pair<Point,Point>> enc = ecceg.encryptBytes(read);
        System.out.println("---===Cipherteks===---");
        for (Pair<Point,Point> pp: enc) {
            System.out.print(String.format("%02x%02x%02x%02x",
                pp.left.x.intValue(),
                pp.left.y.intValue(),
                pp.right.x.intValue(),
                pp.right.y.intValue()));
        }
        System.out.println();
        System.out.println("---======END======---");
        List<Point> dec = ecceg.decrypt(enc);
        for (Point pp: dec) System.out.print((char)ecc.pointToInt(pp).byteValue());
        System.out.println();
        fr.savePointsToFile(filepath_ciph, enc);
    }

    public static void decryptFile(ECC ecc, String private_key_filepath, String public_key_filepath, 
        String filepath_plain, String filepath_ciph) throws Exception {
            ECCEG ecceg = new ECCEG(ecc, ecc.getBasePoint());
            ecceg.loadPrivateKey(private_key_filepath);
            System.out.println("Private key loaded...");
            ecceg.loadPublicKey(public_key_filepath);
            System.out.println("Public key loaded...");
            FileReader fr = new FileReader();
            List<Pair<Point,Point>> read_enc = fr.loadPointsFromFile(filepath_ciph);
            List<Point> read_dec = ecceg.decrypt(read_enc);
            for (Point pp: read_dec) System.out.print((char)ecc.pointToInt(pp).byteValue());
            System.out.println();
    }
}