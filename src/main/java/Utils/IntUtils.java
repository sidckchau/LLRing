package Utils;

import java.math.BigInteger;
import java.util.Random;

public class IntUtils {
    public static BigInteger[] decomposeBits(BigInteger number,int size) {

        int temp = number.intValue();
        BigInteger []bits = new BigInteger[size];
        for (int i = 0; i < size; i++) {

            // Shift the number i bits to the right and mask with 1 to get the i-th bit
            bits[i] = BigInteger.valueOf((temp >> i) & 1);
        }
        return bits;
    }
    public static BigInteger innerProd(BigInteger[] gs, BigInteger[] ks, BigInteger n) {

        BigInteger result = gs[0].multiply(ks[0]).mod(n);

        for (int i = 1; i < gs.length; i++) {
            result = result.add(gs[i].multiply(ks[i])).mod(n);
        }

        return result.mod(n);
    }

    public static BigInteger sum(BigInteger[] vs, BigInteger n) {

        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < vs.length; i++) {
            result = result.add(vs[i]);
        }

        return result.mod(n);
    }

    public static BigInteger sum(BigInteger[][] vs, BigInteger n) {

        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < vs.length; i++) {
            for (int j = 0; j <vs[i].length ; j++) {
                result = result.add(vs[i][j]);
            }

        }

        return result.mod(n);
    }


    public static BigInteger[] v_add_v(BigInteger[] vs, BigInteger[] ks, BigInteger n) {

        BigInteger[] ret = new BigInteger[vs.length];

        for (int i = 0; i < vs.length; i++) {
            ret[i] = (vs[i].add(ks[i])).mod(n);
        }

        return ret;
    }

    public static BigInteger[][] v_add_v(BigInteger[][] vs, BigInteger[][]ks, BigInteger n) {

        BigInteger[][] ret = new BigInteger[vs.length][vs[0].length];

        for (int i = 0; i < vs.length; i++) {
            for (int j = 0; j < vs[i].length; j++) {
                ret[i][j] = (vs[i][j].add(ks[i][j])).mod(n);
            }

        }

        return ret;
    }

    public static BigInteger[] v_sub_v(BigInteger[] vs, BigInteger[] ks, BigInteger n) {

        BigInteger[] ret = new BigInteger[vs.length];

        for (int i = 0; i < vs.length; i++) {
            ret[i] = (vs[i].subtract(ks[i])).mod(n);
        }

        return ret;
    }

    public static BigInteger[] v_mul_v(BigInteger[] vs, BigInteger[] ks, BigInteger n) {

        BigInteger[] ret = new BigInteger[vs.length];

        for (int i = 0; i < vs.length; i++) {
            ret[i] = (vs[i].multiply(ks[i])).mod(n);
        }

        return ret;
    }

    public static BigInteger[][] v_mul_v(BigInteger[][] vs, BigInteger[][] ks, BigInteger n) {

        BigInteger[][] ret = new BigInteger[vs.length][vs[0].length];

        for (int i = 0; i < vs.length; i++) {
            for (int j = 0; j < vs[i].length; j++) {
                ret[i][j] = (vs[i][j].multiply(ks[i][j])).mod(n);
            }

        }

        return ret;
    }


    public static BigInteger[] mul_v(BigInteger[] vs, BigInteger k, BigInteger n) {

        BigInteger[] ret = new BigInteger[vs.length];

        for (int i = 0; i < vs.length; i++) {
            ret[i] = (vs[i].multiply(k)).mod(n);
        }

        return ret;
    }

    public static BigInteger[][] mul_v(BigInteger[][] vs, BigInteger k, BigInteger n) {

        BigInteger[][] ret = new BigInteger[vs.length][vs[0].length];

        for (int i = 0; i < vs.length; i++) {
            for (int j = 0; j < vs[i].length; j++) {
                ret[i][j] = (vs[i][j].multiply(k)).mod(n);
            }

        }

        return ret;
    }

    public static BigInteger[] neg_v(BigInteger[] vs, BigInteger n) {

        BigInteger[] ret = new BigInteger[vs.length];

        for (int i = 0; i < vs.length; i++) {
            ret[i] = vs[i].negate().mod(n);
        }

        return ret;
    }

    public static int countOnes(int number) {
        int count = 0;
        while (number > 0) {
            count += number & 1; // Add 1 if the least significant bit is 1
            number = number >>> 1; // Unsigned right shift the bits of the number
        }
        return count;
    }

    public static BigInteger[] generateRandomBigIntegers(int count, int bitLength) {
        BigInteger[] result = new BigInteger[count];
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            result[i] = new BigInteger(bitLength, random);
        }

        return result;
    }

    public static void main(String[] args) {

//        BigInteger[] as = generateRandomBigIntegers(10, 3);
//        for (BigInteger bi : as) {
//            System.out.print(bi + " ,");
//        }
//        System.out.println();
//        System.out.println(" --------- ");
//
////        BigInteger[] bs = v_add_v(as,as, BigInteger.valueOf(100));
//        BigInteger[] bs = mul_v(as,BigInteger.TWO, BigInteger.valueOf(100));
//        for (BigInteger bi : bs) {
//            System.out.print(bi + " ,");
//        }
//        System.out.println();
//        System.out.println(" --------- ");
//        BigInteger[] cs = v_sub_v(bs,as, BigInteger.valueOf(100));
//        for (BigInteger bi : cs) {
//            System.out.print(bi + " ,");
//        }



    }
}
