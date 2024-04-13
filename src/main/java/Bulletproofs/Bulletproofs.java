package Bulletproofs;

import CurveOperation.G1Point;
import CurveOperation.GtPoint;
import Utils.HashUtils;
import Utils.IntUtils;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bulletproofs {
    Pairing pairing = PairingFactory.getPairing("f.properties");
    Field G1 = pairing.getG1();

    G1Point u =new G1Point(G1.newRandomElement());
    public BigInteger n = G1.getOrder();
    G1Point[] hs;
    G1Point[] gs;

    int size;



    public Bulletproofs(int size, Pairing pairing, G1Point[] gs, G1Point[] hs, G1Point u) {
        G1 = pairing.getG1();
        this.gs=gs;
        this.hs=hs;
        this.u= u;
        this.size = size;

    }


    public Bulletproofs(int size) {

        G1Point g = new G1Point(G1.newRandomElement());
        G1Point h = new G1Point(G1.newRandomElement());
        hs = new G1Point[size];
        gs = new G1Point[size];

        for (int i = 0 ; i< size; i++){
            hs[i] = h.mul(BigInteger.valueOf(i+1));
            gs[i] = g.mul(BigInteger.valueOf(i+1));
        }

        this.size = size;

    }

    public Proof prove(BigInteger[] as, BigInteger[] bs ){

        G1Point gC = G1Point.mulAndSum(gs,as);
        G1Point hC = G1Point.mulAndSum(hs,bs);

        Proof pi = new Proof();

        pi.P = gC.add(hC);

        pi.p = innerProd(as,bs);


        compress(gs,hs,as,bs,pi);

        return pi;

    }

    public void compress(G1Point[] g, G1Point[] h, BigInteger[] a, BigInteger[] b, Proof pi) {

        if (g.length == 1){
            pi.a = a[0];
            pi.b = b[0];

            return;
        }

        BigInteger[][] as = splitIntoHalves(a);
        BigInteger[] aL = as[0];
        BigInteger[] aR = as[1];

        BigInteger[][]bs = splitIntoHalves(b);
        BigInteger[] bL = bs[0];
        BigInteger[] bR = bs[1];

        BigInteger cL = innerProd(aL,bR);
        BigInteger cR = innerProd(aR,bL);

        G1Point[][] gs = splitIntoHalves(g);
        G1Point[] gL = gs[0];
        G1Point[] gR = gs[1];

        G1Point[][] hs = splitIntoHalves(h);
        G1Point[] hL = hs[0];
        G1Point[] hR = hs[1];


        G1Point L = G1Point.mulAndSum(gR,aL).add(G1Point.mulAndSum(hL,bR)).add(u.mul(cL));
        G1Point R = G1Point.mulAndSum(gL,aR).add(G1Point.mulAndSum(hR,bL)).add(u.mul(cR));


        G1Point[] LR = {L,R};

        pi.LRs.add(LR);

        BigInteger c = HashUtils.hash(LR);
//        c = BigInteger.ONE;
        BigInteger cinv = c.modInverse(n);


        G1Point[] gR_ = new G1Point[gR.length];
        G1Point[] gL_ = new G1Point[gR.length];
        G1Point[] hR_ = new G1Point[hR.length];
        G1Point[] hL_ = new G1Point[hR.length];
        BigInteger[] aprime = new BigInteger[gR.length];
        BigInteger[] bprime = new BigInteger[gR.length];

        for (int i = 0 ; i < gR.length; i++){
            gL_[i] = gL[i].mul(cinv);
            hL_[i] = hL[i].mul(c);
            gR_[i] = gR[i].mul(c);
            hR_[i] = hR[i].mul(cinv);
            aprime[i] = aL[i].multiply(c).add(aR[i].multiply(cinv)).mod(n);
            bprime[i] = bL[i].multiply(cinv).add(bR[i].multiply(c)).mod(n);
        }

        G1Point[] gprime = G1Point.add(gL_,gR_);
        G1Point[] hprime = G1Point.add(hL_,hR_);

        compress(gprime,hprime,aprime,bprime,pi);


    }


    public boolean verify(Proof pi ){

        G1Point C = pi.P;
        G1Point P = C.add(u.mul(pi.p));

        G1Point Pprime = P;
        G1Point[] gprime = gs;
        G1Point[] hprime = hs;

        for (int i = 0 ; i < pi.LRs.size(); i ++){

            G1Point[] LR = pi.LRs.get(i);

            G1Point L = LR[0];
            G1Point R = LR[1];

            BigInteger c = HashUtils.hash(LR);
//            BigInteger c = BigInteger.ONE;
            BigInteger c2 = c.multiply(c).mod(n);
            BigInteger cinv = c.modInverse(n);
            BigInteger cinv2 = cinv.multiply(cinv).mod(n);

            Pprime = L.mul(c2).add(Pprime).add(R.mul(cinv2));

            G1Point[][] gps = splitIntoHalves(gprime);
            G1Point[] gL = gps[0];
            G1Point[] gR = gps[1];

            G1Point[][] hps = splitIntoHalves(hprime);
            G1Point[] hL = hps[0];
            G1Point[] hR = hps[1];

            G1Point[] gR_ = new G1Point[gR.length];
            G1Point[] gL_ = new G1Point[gR.length];
            G1Point[] hR_ = new G1Point[hR.length];
            G1Point[] hL_ = new G1Point[hR.length];

            for (int j = 0 ; j < gR.length; j++){
                gL_[j] = gL[j].mul(cinv);
                hL_[j] = hL[j].mul(c);
                gR_[j] = gR[j].mul(c);
                hR_[j] = hR[j].mul(cinv);

            }

            gprime = G1Point.add(gL_,gR_);
            hprime = G1Point.add(hL_,hR_);

        }

        G1Point result = gprime[0].mul(pi.a).add(hprime[0].mul(pi.b)).add(u.mul(pi.a.multiply(pi.b)));

        return result.isEqual(Pprime);

    }


    public boolean verify_fast(Proof pi ){

        G1Point C = pi.P;
        G1Point P = C.add(u.mul(pi.p));

        G1Point Pprime = P;
        G1Point[] gprime = gs;
        G1Point[] hprime = hs;

        for (int i = 0 ; i < pi.LRs.size(); i ++){

            G1Point[] LR = pi.LRs.get(i);

            G1Point L = LR[0];
            G1Point R = LR[1];

            BigInteger c = HashUtils.hash(LR);
//            c = BigInteger.ONE;
            BigInteger c2 = c.multiply(c).mod(n);
            BigInteger cinv = c.modInverse(n);
            BigInteger cinv2 = cinv.multiply(cinv).mod(n);

            Pprime = L.mul(c2).add(Pprime).add(R.mul(cinv2));


        }

        BigInteger[] es1 = new BigInteger[size];
        BigInteger[] es2 = new BigInteger[size];


        for (int i = 0; i < size; i++) {
            BigInteger sum1 = BigInteger.ONE;
            BigInteger sum2 = BigInteger.ONE;

            for (int j = 0; j < pi.LRs.size(); j++) {
                G1Point[] LR = pi.LRs.get(pi.LRs.size()-j-1);
                BigInteger c = HashUtils.hash(LR);
//                c = BigInteger.ONE;
                BigInteger cinv = c.modInverse(n);
                sum1 = sum1.multiply(check_bit(i,j,cinv)).mod(n);
                sum2 = sum2.multiply(check_bit(i,j,c)).mod(n);
            }
            es1[i] = sum1;
            es2[i] = sum2;
        }

        es1 = IntUtils.mul_v(es1,pi.a,n);
        es2 = IntUtils.mul_v(es2,pi.b,n);



        G1Point result = G1Point.mulAndSum(gs,es1).add(G1Point.mulAndSum(hs,es2)).add(u.mul(pi.a.multiply(pi.b)));

        return result.isEqual(Pprime);

    }

    BigInteger check_bit(int i, int j, BigInteger a ){
        return ((i >> j) & 1) == 1 ? a.modInverse(n) : a;
    }

    public static G1Point[][] splitIntoHalves(G1Point[] arr) {
        int mid = (arr.length + 1) / 2;  // If the array length is odd, the first half will get the extra element

        G1Point[] firstHalf = Arrays.copyOfRange(arr, 0, mid);
        G1Point[] secondHalf = Arrays.copyOfRange(arr, mid, arr.length);

        return new G1Point[][] {firstHalf, secondHalf};
    }

    public static BigInteger[][] splitIntoHalves(BigInteger[] arr) {
        int mid = (arr.length + 1) / 2;  // If the array length is odd, the first half will get the extra element

        BigInteger[] firstHalf = Arrays.copyOfRange(arr, 0, mid);
        BigInteger[] secondHalf = Arrays.copyOfRange(arr, mid, arr.length);

        return new BigInteger[][] {firstHalf, secondHalf};
    }

    public BigInteger innerProd (BigInteger[] gs, BigInteger[] ks ){

        BigInteger result = gs[0].multiply(ks[0]).mod(n);

        for (int i =1; i< gs.length; i++){
            result = result.add(gs[i].multiply(ks[i])).mod(n);
        }

        return result;
    }


    public class Proof{

        public G1Point P ;

        public BigInteger p;
        public List<G1Point[]> LRs = new ArrayList<>();

        public BigInteger a;
        public BigInteger b;

    }

    public static void main(String[] args) {

        int size =16;
        Bulletproofs bp = new Bulletproofs(size);
        BigInteger z =BigInteger.valueOf(9);
        BigInteger y = BigInteger.valueOf(2);
        BigInteger[] t = new BigInteger[size];
        BigInteger[] zs = new BigInteger[size];
        BigInteger[] ys = new BigInteger[size];
        BigInteger[] t1 = new BigInteger[size];
        BigInteger[] ones = new BigInteger[size];
        for (int i = 0; i < size; i++){
            t[i] = BigInteger.ONE;
            t1[i] = BigInteger.ZERO;
        }
        t[0] = BigInteger.ZERO;
        t1[0] = BigInteger.valueOf(-1).mod(bp.n);


        for (int i = 0; i < size; i++){
            t[i] = t[i].subtract(z).mod(bp.n);
            t1[i] = ((t1[i].add(z)).multiply(y.pow(i))).add(z.pow(2)).mod(bp.n);;
            ys[i] = y.pow(i);
            zs[i] = z;
            ones[i] = BigInteger.ONE;

        }

//        System.out.println(BigInteger.valueOf(3).pow(0));
//        BigInteger [] t2 = swift.hadamardProd(t,t1,swift.n,BigInteger.ONE.negate());

        System.out.println(
                ((z.pow(2).multiply(BigInteger.valueOf(size-1)))
                        .subtract(z.pow(2).multiply(bp.innerProd(ys,ones)))
                        .subtract(z.pow(3).multiply(bp.innerProd(ones,ones)))
                        .add(z.multiply(bp.innerProd(ones,ys)))).mod(bp.n)
        );



        Proof pi = bp.prove(t,t1);
//        System.out.println(pi.p);
        System.out.println(bp.pairing.getZr().getOrder());
        System.out.println(bp.verify_fast(pi));
    }
}
