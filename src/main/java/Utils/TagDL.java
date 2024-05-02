package Utils;

import CurveOperation.G1Point;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;

public class TagDL {


    Pairing pairing = PairingFactory.getPairing("f.properties");

    Field G1 = pairing.getG1();
    G1Point P = new G1Point(G1.newRandomElement());

    BigInteger n = G1.getOrder();

    Field Z = pairing.getZr();

    G1Point Q = new G1Point(G1.newRandomElement());

    public TagDL(Pairing pairing,G1Point p, G1Point q) {
        this.pairing = pairing;
        G1 = pairing.getG1();
        n = G1.getOrder();
        P = p;
        Z = pairing.getZr();
        Q = q;
    }

    public TagDL() {
    }

    public Proof tagGen(String prefix, BigInteger sk, BigInteger r){
        G1Point cm = P.mul(sk).add(Q.mul(r));
        BigInteger r_B = Z.newRandomElement().toBigInteger();
        BigInteger a = Z.newRandomElement().toBigInteger();
        G1Point tag = P.mul(HashUtils.hash(prefix)).mul(sk);
        G1Point A = P.mul(HashUtils.hash(prefix)).mul(a);
        G1Point B = P.mul(a).add(Q.mul(r_B));
        BigInteger rho = HashUtils.hash(tag,A,B);
        BigInteger a_p = a.add(rho.multiply(sk)).mod(n);
        BigInteger r_p = r_B.add(rho.multiply(r)).mod(n);

        return new Proof(cm, tag,B,A, a_p,r_p);

    }

    public boolean verify(Proof pi, String prefix){
        BigInteger rho = HashUtils.hash(pi.tag,pi.A,pi.B);
        boolean b1 = pi.A.add(pi.tag.mul(rho)).isEqual( P.mul(HashUtils.hash(prefix)).mul(pi.a_p));
        boolean b2 = pi.B.add(pi.cm.mul(rho)).isEqual( P.mul(pi.a_p).add(Q.mul(pi.r_p)));
        return  b1&&b2;
    }

    public static void main(String[] args) {

        TagDL t = new TagDL();
        Proof pi = t.tagGen("www", t.Z.newRandomElement().toBigInteger(), t.Z.newRandomElement().toBigInteger());
        System.out.println(t.verify(pi,"www"));
    }


    public class Proof{
        G1Point cm;
        G1Point tag;



        G1Point B;

        G1Point A;

        BigInteger a_p;

        BigInteger r_p;

        public Proof(G1Point cm, G1Point tag, G1Point b, G1Point a, BigInteger a_p, BigInteger r_p) {
            this.cm = cm;
            this.tag = tag;
            B = b;
            A = a;
            this.a_p = a_p;
            this.r_p = r_p;
        }
    }


}
