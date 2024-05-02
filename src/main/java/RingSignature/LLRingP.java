package RingSignature;

import CurveOperation.G1Point;
import CurveOperation.G2Point;
import CurveOperation.GtPoint;
import Dory.Dory_ZK;
import Utils.HashUtils;
import Utils.IntUtils;
import Utils.TagDL;
import Utils.TagP;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;

public class LLRingP {


    Pairing pairing = PairingFactory.getPairing("f.properties");

    Field G1 = pairing.getG1();
    Field G2 = pairing.getG2();

    Field Gt = pairing.getGT();
    BigInteger n = G1.getOrder();
    G1Point P = new G1Point(G1.newRandomElement());

    Field Z = pairing.getZr();

    String prefix = "f";

    TagP tag;

    G1Point[] pks;
    G1Point[] Rs;

    G1Point[] Gs;

    G2Point[] Hs;

    BigInteger[] sks;
    GtPoint Q_t;

    G1Point R;

    Dory_ZK dory;


    G1Point L1;

    G1Point[] L1s;


    G2Point L2;

    G2Point[] L2s;

    GtPoint Pre_LL;
    GtPoint Pre_GL;

    GtPoint Pre_LH;

    GtPoint Pre_RL;



    int r_size;

    public LLRingP(int size){
        GtPoint.setPairing(pairing);

        pks = new G1Point[size];
        Rs = new G1Point[size];
        Gs = new G1Point[size];
        Hs = new G2Point[size];
        sks = new BigInteger[size];
        L1s = new G1Point[size];
        L2s = new G2Point[size];
        r_size = size;

        L1 = new G1Point(G1.newRandomElement());

        L2 = new G2Point(G2.newRandomElement());

        R = new G1Point(G1.newRandomElement());


        Q_t = new GtPoint(Gt.newRandomElement());

        tag = new TagP(pairing,P,Q_t,L2);




        for (int i = 0; i < size; i++) {

            sks[i] = Z.newRandomElement().toBigInteger();
            pks[i] = P.mul(sks[i]);
            Rs[i] = R.mul(HashUtils.hash(pks[i]).mod(n));
            Hs[i] = new G2Point(G2.newRandomElement());
            Gs[i] = Rs[i].add(pks[i]);
            L1s[i] = L1;
            L2s[i] = L2;
        }
        long time1 = System.nanoTime();
        Pre_LL = GtPoint.pair(L1,L2);
        Pre_GL = GtPoint.innerProd(Gs,L2s);
        Pre_LH = GtPoint.innerProd(L1s,Hs);
        Pre_RL = GtPoint.pair(R,L2);
        for (int i = 0; i < size; i++) {
            Rs[i] = R.mul(HashUtils.hash(pks[i]).mod(n));
            Gs[i] = Rs[i].add(pks[i]);
        }
        long time2 = System.nanoTime();
        System.out.println("Single-Key MLRing-P Test with size " + size);

        System.out.println("PreProcessing time in milliseconds: " + (time2-time1) / 1_000_000);

        System.out.println("--------------");


        dory= new Dory_ZK(r_size,pairing,Q_t,Gs,Hs);
        dory.pre_Process();



    }

    public Signature sign (String m , int index){

        BigInteger[] cs = new BigInteger[r_size];


        for (int i = 0; i < r_size; i++) {

            cs[i] = BigInteger.ZERO;

        }

        cs[index] = BigInteger.ONE;

        BigInteger r_cm = Z.newRandomElement().toBigInteger();

        BigInteger r_A_hat= Z.newRandomElement().toBigInteger();

        BigInteger r_B= Z.newRandomElement().toBigInteger();

        GtPoint cm = GtPoint.pair(pks[index],L2 ).mul(Q_t.pow(r_cm));

        TagP.Proof tag_pi = tag.tagGen(prefix, sks[index], r_cm);

        GtPoint A_hat = GtPoint.pair(Rs[index],L2).mul(Q_t.pow(r_A_hat));

        GtPoint B = GtPoint.pair(Gs[index],Hs[index]).mul(Q_t.pow(r_B));

        BigInteger r_C = Z.newRandomElement().toBigInteger();

        BigInteger c = Z.newRandomElement().toBigInteger();
        GtPoint C = Pre_RL.pow(c).mul(Q_t.pow(r_C));
        BigInteger phi = HashUtils.hash(HashUtils.hash(m),HashUtils.hash(C));

        BigInteger c_p = c.add(phi.multiply(HashUtils.hash(pks[index]))).mod(n);
        BigInteger r_Cp = r_C.add(phi.multiply(r_A_hat)).mod(n);

        BigInteger r_A = r_cm.add(r_A_hat).mod(n);

        // ======================== Batch 1 and 2 =======================

        BigInteger r_X1 = Z.newRandomElement().toBigInteger();

        GtPoint X1  = GtPoint.pair(L1,Hs[index]).mul(GtPoint.pair(Gs[index],L2)).mul(Q_t.pow(r_X1));

        BigInteger gamma1 = HashUtils.hash(cm,A_hat,B,X1).mod(n);

        G1Point[] v_1 = G1Point.add(G1Point.mul(L1s,gamma1),G1Point.mul(Gs,cs));

        G2Point[] v_2 = G2Point.add(G2Point.mul(L2s,IntUtils.mul_v(cs,gamma1,n))
                ,G2Point.mul(Hs,cs));

        BigInteger r_D0 = gamma1.pow(2).multiply(BigInteger.ZERO).add(gamma1.multiply(r_X1)).add(r_B).mod(n);
        BigInteger r_D1 = gamma1.multiply(BigInteger.ZERO).add(r_B).mod(n);
        BigInteger r_D2 = gamma1.multiply(r_A).add(r_B).mod(n);

        // ======================== Batch the 3rd =======================

        BigInteger r_X2 = Z.newRandomElement().toBigInteger();

        GtPoint X2  = GtPoint.innerProd(v_1,L2s).mul(GtPoint.innerProd(G1Point.mul(Gs,cs),v_2)).mul(Q_t.pow(r_X2));

        BigInteger gamma2 = HashUtils.hash(cm,A_hat,B,X1,X2).mod(n);

        v_1 = G1Point.add(G1Point.mul(v_1,gamma2),G1Point.mul(Gs,cs));

        v_2 = G2Point.add(G2Point.mul(v_2,gamma2),L2s);

        r_D0 = gamma2.pow(2).multiply(r_D0).add(gamma2.multiply(r_X2)).add(r_A).mod(n);
        r_D1 = gamma2.multiply(r_D1).add(r_B).mod(n);
        r_D2 = gamma2.multiply(r_D2).mod(n);

        dory.r_C = r_D0;
        dory.r_D1 = r_D1;
        dory.r_D2 = r_D2;
        Dory_ZK.Proof pi1 = dory.prove(v_1,v_2);




        return new Signature(cm,A_hat,B,C,X1,X2,c_p,r_Cp,pi1, tag_pi);


    }

    public boolean verify(String m, Signature sig){

        BigInteger phi = HashUtils.hash(HashUtils.hash(m),HashUtils.hash(sig.C));
        GtPoint T0 = Pre_RL.pow(sig.c_p).mul(Q_t.pow(sig.r_Cp));

        if (!T0.isEqual(sig.C.mul(sig.A_hat.pow(phi)))){
            System.out.println("break sch");
            return false;
        }

        if (!tag.verify(sig.tag, prefix)) {
            System.out.println("break0");
            return false;
        }


        GtPoint A = sig.cm.mul(sig.A_hat);


        BigInteger gamma1 = HashUtils.hash(sig.cm,sig.A_hat,sig.B,sig.X1).mod(n);





        GtPoint D0 = Pre_LL.pow(gamma1.pow(2).mod(n)).mul(sig.X1.pow(gamma1)).mul(sig.B);
        GtPoint D1 = Pre_LH.pow(gamma1).mul(sig.B);
        GtPoint D2 = A.pow(gamma1).mul(sig.B);

        BigInteger gamma2 = HashUtils.hash(sig.cm,sig.A_hat,sig.B,sig.X1,sig.X2).mod(n);

        sig.pi1.C = D0.pow(gamma2.pow(2).mod(n)).mul(sig.X2.pow(gamma2)).mul(A);
        sig.pi1.D1= D1.pow(gamma2).mul(sig.B);
        sig.pi1.D2 = D2.pow(gamma2).mul(Pre_GL);


        if (!dory.verify(sig.pi1)){
            System.out.println("break1");
            return false;
        }



        return true;


    }



    public boolean test(){
        GtPoint cm = GtPoint.pair(pks[0],L2);
        GtPoint cmR = GtPoint.pair(Rs[0],L2);

        GtPoint t1 = cm.mul(cmR);

        GtPoint t2 = GtPoint.pair(Gs[0],L2);

        return  (t1.isEqual(t2));

    }

    public class Signature{
        GtPoint cm;
        GtPoint A_hat;

        GtPoint B;

        GtPoint C;

        GtPoint X1;

        GtPoint X2;

        BigInteger c_p;

        BigInteger r_Cp;


        Dory_ZK.Proof pi1;

        TagP.Proof tag;


        public Signature(GtPoint cm, GtPoint a_hat, GtPoint b,GtPoint c,GtPoint x1,GtPoint x2, BigInteger c_p, BigInteger r_Cp, Dory_ZK.Proof pi1, TagP.Proof tag) {
            this.cm = cm;
            A_hat = a_hat;
            B = b;
            C = c;
            X1 = x1;
            X2 = x2;
            this.c_p = c_p;
            this.r_Cp = r_Cp;
            this.pi1 = pi1;
            this.tag = tag;
        }
    }

    public static void main(String[] args) throws Exception {
        LLRingP ring = new LLRingP(4);

        Signature sig = ring.sign("123",0);

        System.out.println(ring.verify("123",sig ));



    }


}
