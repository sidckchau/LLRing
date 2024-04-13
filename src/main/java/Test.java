import Utils.HashUtils;
import Utils.IntUtils;

import java.math.BigInteger;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) {
        BigInteger i = BigInteger.valueOf(2);

        System.out.println(Arrays.toString(IntUtils.decomposeBits(i,2)));

        System.out.println(HashUtils.hash(BigInteger.valueOf(2)));
        System.out.println(HashUtils.hash(BigInteger.valueOf(2)));
    }
}
