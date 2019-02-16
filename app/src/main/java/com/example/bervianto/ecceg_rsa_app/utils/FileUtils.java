package com.example.bervianto.ecceg_rsa_app.utils;

import com.example.bervianto.ecceg_rsa_app.ecc.Pair;
import com.example.bervianto.ecceg_rsa_app.ecc.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static byte[] getBytes(String fileName) {
        File fIn = new File(fileName);
        if (!fIn.canRead()) {
            System.err.println("Can't read " + fileName);
            return null;
        }

        byte[] bytes = null;
        try (FileInputStream in = new FileInputStream(fIn)) {

            long fileSize = fIn.length();
            if (fileSize > Integer.MAX_VALUE) {
                System.out.println("Sorry, file was too large!");
            }

            bytes = new byte[(int) fileSize];

            int offset = 0;
            int numRead;
            while (offset < bytes.length && (numRead = in.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } catch (IOException ignored) {
        }

        return bytes;
    }


    public static void saveFile(String stringpath, byte[] content) {
        try {
            FileOutputStream fos = new FileOutputStream(stringpath);
            fos.write(content);
            fos.close();
        } catch (IOException ignored) {
        }
    }

    private static byte[] intToBytes(int x) {
        return ByteBuffer.allocate(4).putInt(x).array();
    }

    private static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static void savePointsToFile(String path, List<Pair<Point, Point>> pairpoints) {
        byte[] b = new byte[pairpoints.size() * 16];
        int j = 0;
        for (Pair<Point, Point> ppoint : pairpoints) {
            byte[] btemp = intToBytes(ppoint.left.x.intValue());
            for (byte aBtemp : btemp) {
                b[j] = aBtemp;
                j++;
            }
            btemp = intToBytes(ppoint.left.y.intValue());
            for (byte aBtemp : btemp) {
                b[j] = aBtemp;
                j++;
            }
            btemp = intToBytes(ppoint.right.x.intValue());
            for (byte aBtemp : btemp) {
                b[j] = aBtemp;
                j++;
            }
            btemp = intToBytes(ppoint.right.y.intValue());
            for (byte aBtemp : btemp) {
                b[j] = aBtemp;
                j++;
            }
        }
        saveFile(path, b);
    }

    private static final String HEXES = "0123456789ABCDEF";

    private static boolean isNull(Object obj) {
        return obj == null;
    }

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


    public static List<Pair<Point, Point>> loadPointsFromFile(String stringpath) {
        byte[] rawData = getBytes(stringpath);
        List<Pair<Point, Point>> pair = new ArrayList<>();
        byte[] btemp = new byte[4];
        int f = 0, s;
        Point point1 = new Point();
        point1.x = BigInteger.valueOf(1);
        point1.y = BigInteger.valueOf(1);
        Point point2 = new Point();
        point2.x = BigInteger.valueOf(1);
        point2.y = BigInteger.valueOf(1);
        for (int i = 0; i < (rawData != null ? rawData.length : 0); i++) {
            btemp[i % 4] = rawData[i];
            if (i % 4 == 3) {
                if ((i / 4) % 4 == 0) {
                    f = bytesToInt(btemp);
                }
                if ((i / 4) % 4 == 1) {
                    s = bytesToInt(btemp);
                    point1 = new Point();
                    point1.x = BigInteger.valueOf(f);
                    point1.y = BigInteger.valueOf(s);
                }
                if ((i / 4) % 4 == 2) {
                    f = bytesToInt(btemp);
                }
                if ((i / 4) % 4 == 3) {
                    s = bytesToInt(btemp);
                    point2 = new Point();
                    point2.x = BigInteger.valueOf(f);
                    point2.y = BigInteger.valueOf(s);
                    pair.add(new Pair<>(point1, point2));
                }
            }
        }
        return pair;
    }
}
