package Utils;

import CurveOperation.G1Point;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import CurveOperation.GtPoint;
import org.bouncycastle.jcajce.provider.digest.Keccak;
public class HashUtils {

    public static BigInteger hash(G1Point... cs) {
        if (cs.length > 0) {
            List<BigInteger> list = new LinkedList<>();
            for (G1Point p : cs) {
                list.add(p.getX());
                list.add(p.getY());
            }
            Keccak.DigestKeccak kecc = new Keccak.Digest256();
            return new BigInteger(1, kecc.digest(encoded(list)));

        }

        return BigInteger.ZERO;
    }

    public static BigInteger hash(GtPoint... ps) {
        if (ps.length > 0) {
            List<BigInteger> list = new LinkedList<>();
            for (GtPoint p : ps) {
                list.add(p.point.toBigInteger());
            }
            Keccak.DigestKeccak kecc = new Keccak.Digest256();
            return new BigInteger(1, kecc.digest(encoded(list)));

        }

        return BigInteger.ZERO;
    }

    private static byte[] encoded(List<BigInteger> cs) {
        byte[] buf = new byte[cs.size() * 32];
        int lastPos = 0;
        for (int i = 0; i < cs.size(); i++) {
            String binary = leftPadZero(cs.get(i).toString(2), 256);

            byte[] tmp = new byte[32];
            for (int j = 0; j < 32; j++) {
                tmp[j] = (byte) Integer.parseInt(binary.substring(j * 8, (j + 1) * 8), 2);
            }

            System.arraycopy(tmp, 0, buf, lastPos, tmp.length);
            lastPos += tmp.length;
        }

        return buf;
    }

    public static BigInteger hash(BigInteger... bs) {
        if (bs.length > 0) {
            Keccak.DigestKeccak kecc = new Keccak.Digest256();
            return new BigInteger(1, kecc.digest(encoded(bs)));
        }

        return BigInteger.ZERO;
    }

    private static byte[] encoded(BigInteger... cs) {
        return encoded(Arrays.asList(cs));
    }

    private static String leftPadZero(String binary, int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length - binary.length(); i++) {
            sb.append("0");
        }

        return sb.append(binary).toString();
    }


    public static BigInteger hash(String str){
        Keccak.DigestKeccak kecc = new Keccak.Digest256();
        return new BigInteger(1, kecc.digest(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static BigInteger hash(List<byte[]> byteArrayList) {
        if (!byteArrayList.isEmpty()) {
            Keccak.DigestKeccak kecc = new Keccak.Digest256();

            // Iterate through each byte array and process them
            for (byte[] byteArray : byteArrayList) {
                kecc.update(byteArray);
            }

            // After processing all byte arrays, perform the hash
            return new BigInteger(1, kecc.digest());
        }

        return BigInteger.ZERO;
    }

}
