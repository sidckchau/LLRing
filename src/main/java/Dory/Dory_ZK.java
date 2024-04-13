package Dory;

import CurveOperation.G1Point;
import CurveOperation.G2Point;
import CurveOperation.GtPoint;
import Utils.HashUtils;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dory_ZK {

    public Pairing pairing = PairingFactory.getPairing("f.properties");


    public Field G1 = pairing.getG1();
    public Field G2 = pairing.getG2();

    public Field Z = pairing.getZr();

    public BigInteger n = pairing.getZr().getOrder();
    public ArrayList<G2Point[]> L2s;
    public ArrayList<G1Point[]> L1s;
    public ArrayList<Para> pre_paras = new ArrayList<>();

    public GtPoint Q_t;

    GtPoint X;

    public BigInteger r_C = Z.newRandomElement().toBigInteger();
    public BigInteger r_D1 = Z.newRandomElement().toBigInteger();
    public BigInteger r_D2 = Z.newRandomElement().toBigInteger();
    public Dory_ZK(int size) throws Exception{
        if (!isPowerOfTwo(size)){
            throw new Exception("Illegal size");
        }
        GtPoint.setPairing(this.pairing);
        L2s = new ArrayList<>();
        L1s = new ArrayList<>();
        Q_t = new GtPoint(pairing.getGT().newRandomElement());

        while (size>0){
            G1Point[] arrG1 = new G1Point[size];
            G2Point[] arrG2 = new G2Point[size];
            for (int i = 0; i < size ; i++){
                arrG1[i]= new G1Point(G1.newRandomElement());
                arrG2[i] = new G2Point(G2.newRandomElement());
            }
            L2s.add(arrG2);
            L1s.add(arrG1);
            size = size/2;
        }
    }

    public Dory_ZK(int size, Pairing pairing) throws Exception{
        if (!isPowerOfTwo(size)){
            throw new Exception("Illegal size");
        }
        this.pairing = pairing;

        this.G1 = pairing.getG1();
        this.G2 = pairing.getG2();
        this.Z = pairing.getZr();

        this.n = pairing.getZr().getOrder();
        GtPoint.setPairing(this.pairing);
        L2s = new ArrayList<>();
        L1s = new ArrayList<>();
        Q_t = new GtPoint(pairing.getGT().newRandomElement());

        while (size>0){
            G1Point[] arrG1 = new G1Point[size];
            G2Point[] arrG2 = new G2Point[size];

            for (int i = 0; i < size ; i++){
                arrG1[i]= new G1Point(G1.newRandomElement());
                arrG2[i] = new G2Point(G2.newRandomElement());
            }
            L2s.add(arrG2);
            L1s.add(arrG1);
            size = size/2;
        }
    }

    public Dory_ZK(int size, Pairing pairing, GtPoint Q_t , G1Point[] g_1s, G2Point[] g_2s) {

        this.pairing = pairing;
        this.Q_t = Q_t;


        this.G1 = pairing.getG1();
        this.G2 = pairing.getG2();
        this.Z = pairing.getZr();

        this.n = pairing.getZr().getOrder();
        GtPoint.setPairing(this.pairing);
        L2s = new ArrayList<>();
        L1s = new ArrayList<>();
        L2s.add(g_2s);
        L1s.add(g_1s);
        size = size/2;
        while (size>0){
            G1Point[] arrG1 = new G1Point[size];
            G2Point[] arrG2 = new G2Point[size];

            for (int i = 0; i < size ; i++){
                arrG1[i]= new G1Point(G1.newRandomElement());
                arrG2[i] = new G2Point(G2.newRandomElement());
            }
            L2s.add(arrG2);
            L1s.add(arrG1);
            size = size/2;
        }
    }

    public static boolean isPowerOfTwo(int n) {
        return (n > 0) && ((n & (n - 1)) == 0);
    }


    public void pre_Process(){

        for ( int i = 0 ; i < L1s.size()-1; i++) {
            G1Point[][] L1 = splitIntoHalves(L1s.get(i));
            G2Point[][] L2 = splitIntoHalves(L2s.get(i));
            GtPoint Delta1L = GtPoint.innerProd(L1[0],L2s.get(i+1));
            GtPoint Delta1R = GtPoint.innerProd(L1[1], L2s.get(i+1));
            GtPoint Delta2L = GtPoint.innerProd(L1s.get(i+1),L2[0] );
            GtPoint Delta2R = GtPoint.innerProd(L1s.get(i+1),L2[1] );
            GtPoint X = GtPoint.innerProd(L1s.get(i),L2s.get(i));
            Para pa = new Para(Delta1L, Delta1R, Delta2L, Delta2R, X);
            pre_paras.add(pa);

        }

        X = GtPoint.innerProd(L1s.get(L1s.size()-1),L2s.get(L2s.size()-1));

    }

    public Proof prove(G1Point[] v_in , G2Point[] c_in){

        G1Point[] v = new G1Point[v_in.length];
        G2Point[] c = new G2Point[c_in.length];
        for (int i = 0 ; i< v_in.length; i++){
            v[i] = v_in[i].duplicate();
            c[i] = c_in[i].duplicate();
        }


        GtPoint C = GtPoint.innerProd(v,c).mul(Q_t.pow(r_C));
        GtPoint D1 = GtPoint.innerProd(v,L2s.get(0)).mul(Q_t.pow(r_D1));
        GtPoint D2 = GtPoint.innerProd(L1s.get(0),c).mul(Q_t.pow(r_D2));
        Proof pi = new Proof(C,D1,D2);

        for (int s = 1 ; s < L1s.size(); s++)
        {
            // P -> V
            G1Point[][] vs = splitIntoHalves(v);
            G2Point[][] cs = splitIntoHalves(c);

            BigInteger r_D1L = Z.newRandomElement().toBigInteger();
            BigInteger r_D1R = Z.newRandomElement().toBigInteger();
            BigInteger r_D2L = Z.newRandomElement().toBigInteger();
            BigInteger r_D2R = Z.newRandomElement().toBigInteger();

            GtPoint D1L = GtPoint.innerProd(vs[0], L2s.get(s)).mul(Q_t.pow(r_D1L));
            GtPoint D1R = GtPoint.innerProd(vs[1], L2s.get(s)).mul(Q_t.pow(r_D1R));
            GtPoint D2L = GtPoint.innerProd(L1s.get(s), cs[0]).mul(Q_t.pow(r_D2L));
            GtPoint D2R = GtPoint.innerProd(L1s.get(s), cs[1]).mul(Q_t.pow(r_D2R));

            GtPoint[] Ds = {D1L, D1R, D2L, D2R};
            //P
            BigInteger beta = HashUtils.hash(Ds).mod(n);
            BigInteger beta_inv = beta.modInverse(n);
            for (int i = 0; i < v.length; i++) {
                v[i] =G1Point.add(v[i],G1Point.mul(L1s.get(s-1)[i],beta));
                c[i] =G2Point.add(c[i],G2Point.mul(L2s.get(s-1)[i],beta_inv));
            }

            r_C = r_C.add(beta.multiply(r_D2)).add(beta_inv.multiply(r_D1)).mod(n);

            BigInteger r_C1 = Z.newRandomElement().toBigInteger();
            BigInteger r_C2 = Z.newRandomElement().toBigInteger();

            // P-> V
            G1Point[][] vs_new = splitIntoHalves(v);
            G2Point[][] cs_new = splitIntoHalves(c);

            GtPoint CL = GtPoint.innerProd(vs_new[0], cs_new[1]).mul(Q_t.pow(r_C1));
            GtPoint CR = GtPoint.innerProd(vs_new[1], cs_new[0]).mul(Q_t.pow(r_C2));




            GtPoint[] Cs = {CL, CR};

            BigInteger alpha = HashUtils.hash(Cs).mod(n);
            BigInteger alpha_inv = alpha.modInverse(n);
            v = fold(vs_new[0], vs_new[1], alpha);
            c = fold(cs_new[0], cs_new[1], alpha_inv);

            r_D1 = alpha.multiply(r_D1L).add(r_D1R).mod(n);
            r_D2 = alpha_inv.multiply(r_D2L).add(r_D2R).mod(n);
            r_C = r_C.add(alpha.multiply(r_C1)).add(alpha_inv.multiply(r_C2));

            pi.Ds.add(Ds);
            pi.Cs.add(Cs);

        }


        // =============== n=1 ===============
        BigInteger r_P1 = Z.newRandomElement().toBigInteger();
        BigInteger r_P2 = Z.newRandomElement().toBigInteger();
        BigInteger r_Q = Z.newRandomElement().toBigInteger();
        BigInteger r_R = Z.newRandomElement().toBigInteger();

        G1Point V_p =  new G1Point(G1.newRandomElement());
        G2Point K_p = new G2Point(G2.newRandomElement());

        G1Point V =  v[0];
        G2Point K = c[0];

        G1Point L1 = L1s.get(L1s.size()-1)[0];

        G2Point L2 = L2s.get(L2s.size()-1)[0];

        GtPoint P1 = GtPoint.pair(V_p,L2).mul(Q_t.pow(r_P1));
        GtPoint P2 = GtPoint.pair(L1,K_p).mul(Q_t.pow(r_P2));

        GtPoint Q = GtPoint.pair(V,K_p).mul(GtPoint.pair(V_p,K)).mul(Q_t.pow(r_Q));

        GtPoint R = GtPoint.pair(V_p,K_p).mul(Q_t.pow(r_R));

        BigInteger e = HashUtils.hash(P1,P2,Q,R).mod(n);


        G1Point E1 = V_p.add(V.mul(e));
        G2Point E2 = K_p.add(K.mul(e));

        r_D1 = r_P1.add(e.multiply(r_D1)).mod(n);
        r_D2 = r_P2.add(e.multiply(r_D2)).mod(n);
        r_C = r_R.add(e.multiply(r_Q)).add(e.pow(2).multiply(r_C)).mod(n);


        pi.E1 = E1;
        pi.E2 = E2;
        pi.P1 = P1;
        pi.P2 = P2;
        pi.Q = Q;
        pi.R = R;
        pi.r_C = r_C;
        pi.r_D1 = r_D1;
        pi.r_D2 = r_D2;

        return pi;
    }


    public boolean verify(Proof pi) {


        GtPoint C_prime = null;
        GtPoint D1_prime = null;
        GtPoint D2_prime = null;

        GtPoint C = pi.C.duplicate();
        GtPoint D1 = pi.D1.duplicate();
        GtPoint D2 = pi.D2.duplicate();

        for ( int i = 0 ; i < pi.Cs.size(); i++){

            GtPoint Delta1L = pre_paras.get(i).Delta1L;
            GtPoint Delta1R = pre_paras.get(i).Delta1R;
            GtPoint Delta2L = pre_paras.get(i).Delta2L;
            GtPoint Delta2R = pre_paras.get(i).Delta2R;
            GtPoint X = pre_paras.get(i).X;
            GtPoint CL = pi.Cs.get(i)[0];
            GtPoint CR = pi.Cs.get(i)[1];
            BigInteger beta = HashUtils.hash(pi.Ds.get(i)).mod(n);
            BigInteger alpha = HashUtils.hash(pi.Cs.get(i)).mod(n);
            BigInteger beta_inv = beta.modInverse(n);
            BigInteger alpha_inv = alpha.modInverse(n);
            C_prime = GtPoint.mul(GtPoint.mul(GtPoint.mul(GtPoint.mul(GtPoint.mul(C,X),GtPoint.pow(D2,beta)),GtPoint.pow(D1,beta_inv)),GtPoint.pow(CL,alpha)),GtPoint.pow(CR,alpha_inv));

            D1_prime = GtPoint.mul(GtPoint.mul(GtPoint.mul(GtPoint.pow(pi.Ds.get(i)[0],alpha),pi.Ds.get(i)[1]),GtPoint.pow(GtPoint.pow(Delta1L,alpha),beta)),GtPoint.pow(Delta1R,beta));

            D2_prime = GtPoint.mul(GtPoint.mul(GtPoint.mul(GtPoint.pow(pi.Ds.get(i)[2], alpha_inv),pi.Ds.get(i)[3]),GtPoint.pow(GtPoint.pow(Delta2L,alpha_inv),beta_inv)),GtPoint.pow(Delta2R,beta_inv));


            D1 = D1_prime;
            D2 = D2_prime;
            C = C_prime;

        }


        G1Point L1 = L1s.get(L1s.size()-1)[0];


        G2Point L2 = L2s.get(L2s.size()-1)[0];



        BigInteger e = HashUtils.hash(pi.P1,pi.P2,pi.Q,pi.R).mod(n);


        BigInteger r = pi.r_C.add( pi.r_D1.add( pi.r_D2)).mod(n);

        GtPoint target1 = GtPoint.pair(pi.E1.add(L1)
                , pi.E2.add(L2)).mul(Q_t.pow(r));


        GtPoint target2 = X.mul(pi.R).mul(pi.Q.pow(e)).mul(C_prime.pow(e.pow(2).mod(n)))
                .mul(pi.P2).mul(D2_prime.pow(e)).mul(pi.P1)
                .mul(D1_prime.pow(e));



        return GtPoint.isEquals(target1,target2);


    }

    public G1Point [] fold (G1Point[] l, G1Point[] r,BigInteger alpha){
        G1Point[] arr = new G1Point[l.length];
        for (int i = 0 ; i< arr.length; i++){
            arr[i] = l[i].mul(alpha).add(r[i]);
        }
        return arr;
    }

    public G2Point [] fold (G2Point[] l, G2Point[] r,BigInteger alpha_inv){
        G2Point[] arr = new G2Point[l.length];
        for (int i = 0 ; i< arr.length; i++){
            arr[i] = l[i].mul(alpha_inv).add(r[i]);
        }

        return arr;
    }
    public static G1Point[][] splitIntoHalves(G1Point[] arr) {
        int mid = (arr.length + 1) / 2;  // If the array length is odd, the first half will get the extra element

        G1Point[] firstHalf = Arrays.copyOfRange(arr, 0, mid);
        G1Point[] secondHalf = Arrays.copyOfRange(arr, mid, arr.length);

        return new G1Point[][] {firstHalf, secondHalf};
    }

    public static G2Point[][] splitIntoHalves(G2Point[] arr) {
        int mid = (arr.length + 1) / 2;  // If the array length is odd, the first half will get the extra element

        G2Point[] firstHalf = Arrays.copyOfRange(arr, 0, mid);
        G2Point[] secondHalf = Arrays.copyOfRange(arr, mid, arr.length);

        return new G2Point[][] {firstHalf, secondHalf};
    }



    public BigInteger innerProd (BigInteger[] gs, BigInteger[] ks ){

        BigInteger result = gs[0].multiply(ks[0]).mod(n);

        for (int i =1; i< gs.length; i++){
            result = result.add(gs[i].multiply(ks[i])).mod(n);
        }

        return result;
    }
    class Para {
        GtPoint Delta1L ;
        GtPoint Delta1R ;
        GtPoint Delta2L ;
        GtPoint Delta2R ;
        GtPoint X;

        public Para(GtPoint delta1L, GtPoint delta1R, GtPoint delta2L, GtPoint delta2R, GtPoint x) {
            Delta1L = delta1L;
            Delta1R = delta1R;
            Delta2L = delta2L;
            Delta2R = delta2R;
            X = x;
        }
    }
    public class Proof {
        public GtPoint C;
        public GtPoint D1;
        public GtPoint D2;


        GtPoint P1;
        GtPoint P2;
        GtPoint Q;
        GtPoint R;

        ArrayList<GtPoint[]> Ds;
        ArrayList<GtPoint[]> Cs;

        G1Point E1;
        G2Point E2;



        BigInteger r_C;
        BigInteger r_D1;
        BigInteger r_D2;

        public Proof(GtPoint c, GtPoint d1, GtPoint d2) {
            C = c;
            D1 = d1;
            D2 = d2;
            Ds = new ArrayList<>();
            Cs = new ArrayList<>();
        }
    }





    public static void main(String[] args) throws Exception {
        Dory_ZK dory = new Dory_ZK(16);

        dory.pre_Process();
        G1Point[] g1s = new G1Point[16];
        G2Point[] g2s = new G2Point[16];
        for (int i =0; i < g1s.length ; i++){
            g1s[i] = new G1Point(dory.pairing.getG1().newRandomElement());
            g2s[i] = new G2Point(dory.pairing.getG2().newRandomElement());
        }
        Proof pi = dory.prove(g1s,g2s);
        long startTime = System.nanoTime();
        System.out.println(dory.verify(pi));
        long endTime = System.nanoTime();

        long duration = endTime - startTime;  // Compute the elapsed time

//        System.out.println("Execution time in nanoseconds: " + duration);
        System.out.println("Execution time in milliseconds: " + duration / 1_000_000);

    }
}