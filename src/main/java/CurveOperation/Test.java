package CurveOperation;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

public class Test {

    public static void main(String[] args) {
        Pairing pairing = PairingFactory.getPairing("f.properties");


        Field G1 = pairing.getG1();
        Field G2 = pairing.getG2();

        Element  g1 = G1.newZeroElement();
        Element g2 = G2.newRandomElement();

        Element gt = pairing.pairing(g1,g2);
        System.out.println(gt.isZero());
    }
}
