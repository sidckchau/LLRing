package Utils;

import CurveOperation.G1Point;
import CurveOperation.G2Point;
import CurveOperation.GtPoint;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;

public class TagP {


    Pairing pairing = PairingFactory.getPairing("f.properties");

    Field G1 = pairing.getG1();
    Field G2 = pairing.getG2();

    Field Gt = pairing.getGT();

    G1Point P = new G1Point(G1.newRandomElement());

    BigInteger n = G1.getOrder();

    Field Z = pairing.getZr();

    GtPoint Q = new GtPoint(Gt.newRandomElement());

    G2Point L = new G2Point(G2.newRandomElement());

    public TagP(Pairing pairing, G1Point p, GtPoint q , G2Point l) {
        this.pairing = pairing;
        G1 =  pairing.getG1();
        G2 = pairing.getG2();
        Gt = pairing.getGT();
        n = G1.getOrder();
        P = p;
        Z = pairing.getZr();
        Q = q;
        L = l;
    }

    public TagP() {
        GtPoint.setPairing(pairing);
    }

    public Proof tagGen(String prefix, BigInteger sk, BigInteger r){
        GtPoint cm = GtPoint.pair(P.mul(sk),L).mul(Q.pow(r));
        BigInteger r_B = Z.newRandomElement().toBigInteger();
        BigInteger a = Z.newRandomElement().toBigInteger();
        GtPoint tag = GtPoint.pair(P,L).pow(HashUtils.hash(prefix)).pow(sk);
        GtPoint A = GtPoint.pair(P,L).pow(HashUtils.hash(prefix)).pow(a);
        GtPoint B = GtPoint.pair(P.mul(a),L).mul(Q.pow(r_B));
        BigInteger rho = HashUtils.hash(tag,A,B);
        BigInteger a_p = a.add(rho.multiply(sk)).mod(n);
        BigInteger r_p = r_B.add(rho.multiply(r)).mod(n);

        return new Proof(cm, tag,B,A, a_p,r_p);

    }

    public boolean verify(Proof pi, String prefix){
        BigInteger rho = HashUtils.hash(pi.tag,pi.A,pi.B);
        boolean b1 = pi.A.mul(pi.tag.pow(rho)).isEqual( GtPoint.pair(P,L).pow(HashUtils.hash(prefix)).pow(pi.a_p));
        boolean b2 = pi.B.mul(pi.cm.pow(rho)).isEqual( GtPoint.pair(P.mul(pi.a_p),L).mul(Q.pow(pi.r_p)));
        return  b1&&b2;
    }

    public static void main(String[] args) {

        TagP t = new TagP();
        Proof pi = t.tagGen("www", t.Z.newRandomElement().toBigInteger(), t.Z.newRandomElement().toBigInteger());
        System.out.println(t.verify(pi,"www"));
    }


    public class Proof{
        GtPoint cm;
        GtPoint tag;



        GtPoint B;

        GtPoint A;

        BigInteger a_p;

        BigInteger r_p;


        public Proof(GtPoint cm, GtPoint tag, GtPoint b, GtPoint a, BigInteger a_p, BigInteger r_p) {
            this.cm = cm;
            this.tag = tag;
            B = b;
            A = a;
            this.a_p = a_p;
            this.r_p = r_p;
        }
    }


}
