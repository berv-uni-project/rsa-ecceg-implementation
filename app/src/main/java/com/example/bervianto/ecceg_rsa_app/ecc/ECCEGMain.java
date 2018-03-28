package com.example.bervianto.ecceg_rsa_app.ecc;

import com.example.bervianto.ecceg_rsa_app.utils.FileUtils;

import java.math.BigInteger;
import java.util.*;

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
        
        // Initiate ended

        System.out.println("Choose an action : ");
        System.out.println("1. Generate Key");
        System.out.println("2. Encrypt File");
        System.out.println("3. Decrypt File");
        System.out.print("Input 1/2/3 = ");
        Scanner s = new Scanner(System.in);
        int chosen = s.nextInt();

        if (chosen == 1) {
            String private_key_filepath = "key.pri";
            String public_key_filepath = "key.pub";
            ECCEGMain.generateKey(ecc, private_key_filepath, public_key_filepath);
        }
        else if (chosen == 2) {
            String public_key_filepath = "key.pub";
            String plainfile = "datatest.txt";
            String ciphfile = plainfile + ".ciph";
            ECCEGMain.encryptFile(ecc, public_key_filepath, plainfile, ciphfile);
        }
        else if (chosen == 3) {
            String private_key_filepath = "key.pri";
            String public_key_filepath = "key.pub";
            String plainfile = "datatest.txt";
            String ciphfile = "datatest.txt.ciph";
            ECCEGMain.decryptFile(ecc, private_key_filepath, ciphfile, plainfile);
        }
        else System.out.println("Wrong choice!!");
    }

    public static void generateKey(ECC ecc, String private_key_filepath, String public_key_filepath) throws Exception {
        ECCEG ecceg = new ECCEG(ecc, ecc.getBasePoint());
        System.out.print("Private key = ");
        System.out.println(ecceg.getPrivateKey());
        ecceg.savePrivateKey(private_key_filepath);
        System.out.println("Saved in " + private_key_filepath);
        System.out.print("Public key = ");
        System.out.println(ecceg.getPublicKey());
        ecceg.savePublicKey(public_key_filepath);
        System.out.println("Saved in " + public_key_filepath);
    }

    public static void encryptFile(ECC ecc, String public_key_filepath, String filepath_plain, 
        String filepath_ciph) throws Exception {
            ECCEG ecceg = new ECCEG(ecc, ecc.getBasePoint());
            ecceg.loadPublicKey(public_key_filepath);
            System.out.println("Public key loaded...");
            byte[] read = FileUtils.getBytes(filepath_plain);
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
            FileUtils.savePointsToFile(filepath_ciph, enc);
    }

    public static void decryptFile(ECC ecc, String private_key_filepath,
                                   String filepath_ciph, String destination) throws Exception {
            ECCEG ecceg = new ECCEG(ecc, ecc.getBasePoint());
            ecceg.loadPrivateKey(private_key_filepath);
            System.out.println("Private key loaded...");
            List<Pair<Point,Point>> read_enc = FileUtils.loadPointsFromFile(filepath_ciph);
            List<Point> read_dec = ecceg.decrypt(read_enc);
            System.out.println("---===Plainteks===---");
            for (Point pp: read_dec)
                System.out.print((char)ecc.pointToInt(pp).byteValue());
            System.out.println();
            System.out.println("---======END======---");
    }
}