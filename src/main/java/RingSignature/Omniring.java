package RingSignature;

import CurveOperation.G1Point;
import Bulletproofs.Bulletproofs;
import Utils.HashUtils;
import Utils.IntUtils;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;


import java.math.BigInteger;
import java.util.List;

public class Omniring {

    Pairing pairing = PairingFactory.getPairing("f.properties");

    Field G1 = pairing.getG1();
    BigInteger n = G1.getOrder();
    G1Point g = new G1Point(G1.newRandomElement());

    Field Z = pairing.getZr();

    G1Point[] pks;
    G1Point[] Rs;
    G1Point[] Hs;

    BigInteger[] sks;
    G1Point Q;
    G1Point F;
    G1Point K;

    int r_size;


    public Omniring(int size) {


        pks = new G1Point[size];
        Rs = new G1Point[size];
        Hs = new G1Point[size];
        sks = new BigInteger[size];
        r_size = size;


        Q = new G1Point(G1.newRandomElement());
        F = new G1Point(G1.newRandomElement());
        K = new G1Point(G1.newRandomElement());


        for (int i = 0; i < size; i++) {
            Rs[i] = new G1Point(G1.newRandomElement());
            Hs[i] = Rs[i];
            sks[i] = Z.newRandomElement().toBigInteger();
            pks[i] = g.mul(sks[i]);

        }
    }

    public static void main(String[] args) {
        Omniring ring = new Omniring(16);
        Signature sig = ring.sign("123", 1);
        System.out.println(ring.single_multi_verify("123",sig));


    }

    public Signature sign(String m, int index) {
        BigInteger[] cs = new BigInteger[r_size];
        BigInteger[] c_ps = new BigInteger[r_size];
        for (int i = 0; i < r_size; i++) {
            if (i == index) {
                cs[i] = BigInteger.ONE;

            } else {
                cs[i] = BigInteger.ZERO;
            }

            c_ps[i] = cs[i].subtract(BigInteger.ONE).mod(n);

        }


        BigInteger r_cm = Z.newRandomElement().toBigInteger();
        BigInteger r_A_hat = Z.newRandomElement().toBigInteger();

        G1Point cm = pks[index].add(Q.mul(r_cm));
        G1Point A_hat = Q.mul(r_A_hat).add(Rs[index]).add(G1Point.mulAndSum(Hs, c_ps));

        BigInteger w = HashUtils.hash(
                HashUtils.hash(cm, A_hat),
                HashUtils.hash(m)
        );

        G1Point[] Gs = new G1Point[r_size];

        for (int i = 0; i < r_size; i++) {
            Gs[i] = pks[i].mul(w).add(Rs[i]);
        }

        ///////////////////////////////////////////

        BigInteger r_S = Z.newRandomElement().toBigInteger();

        BigInteger[] s_1s = new BigInteger[r_size];
        BigInteger[] s_2s = new BigInteger[r_size];

        for (int i = 0; i < r_size; i++) {
            s_1s[i] = Z.newRandomElement().toBigInteger();
            s_2s[i] = Z.newRandomElement().toBigInteger();
        }

        BigInteger r_A = w.multiply(r_cm).add(r_A_hat).mod(this.n);

        G1Point A = Q.mul(r_A).
                add(G1Point.mulAndSum(Gs, cs))
                .add(G1Point.mulAndSum(Hs, c_ps));

        G1Point S = Q.mul(r_S)
                .add(G1Point.mulAndSum(Gs, s_1s))
                .add(G1Point.mulAndSum(Hs, s_2s));

        BigInteger z = HashUtils.hash(A, S).mod(this.n);
        BigInteger y = HashUtils.hash(z).mod(this.n);


        BigInteger[] zs = new BigInteger[r_size];
        BigInteger[] z2s = new BigInteger[r_size];
        BigInteger[] ys = new BigInteger[r_size];
        BigInteger[] ones = new BigInteger[r_size];

        BigInteger[] y_c_z = new BigInteger[r_size];
        BigInteger[] y_s2 = new BigInteger[r_size];

        for (int i = 0; i < r_size; i++) {
            zs[i] = z;
            z2s[i] = z.pow(2).mod(n);
            ys[i] = y.pow(i).mod(n);
            ones[i] = BigInteger.ONE;

            y_c_z[i] = ys[i].multiply(c_ps[i].add(z)).mod(n);
            y_s2[i] = ys[i].multiply(s_2s[i]).mod(n);
        }

        BigInteger t_1 = IntUtils.innerProd(y_c_z, s_1s, n).
                add(IntUtils.innerProd(z2s, s_1s, n))
                .add(IntUtils.innerProd(cs, y_s2, n))
                .subtract(IntUtils.innerProd(zs, y_s2, n)).mod(n);
        BigInteger t_2 = IntUtils.innerProd(s_1s, y_s2, n).mod(n);

        BigInteger tau_1 = Z.newRandomElement().toBigInteger();
        BigInteger tau_2 = Z.newRandomElement().toBigInteger();


        G1Point T_1 = F.mul(t_1).add(Q.mul(tau_1));
        G1Point T_2 = F.mul(t_2).add(Q.mul(tau_2));

        BigInteger x = HashUtils.hash(T_1, T_2).mod(n);

        BigInteger[] ls = new BigInteger[r_size];
        BigInteger[] rs = new BigInteger[r_size];

        for (int i = 0; i < r_size; i++) {
            ls[i] = cs[i].subtract(z).add(s_1s[i].multiply(x)).mod(n);
            rs[i] = ys[i].multiply(c_ps[i].add(z).add(s_2s[i].multiply(x))).add(z2s[i]).mod(n);
        }

        BigInteger t_hat = IntUtils.innerProd(ls, rs, n).mod(n);


        BigInteger tau_x = tau_2.multiply(x.pow(2).mod(n)).add(tau_1.multiply(x)).mod(n);
        BigInteger r_W = r_A.add(r_S.multiply(x)).mod(n);

        G1Point[] H_ys = new G1Point[r_size];

        for (int i = 0; i < r_size; i++) {
            H_ys[i] = Hs[i].mul((y.pow(i)).modInverse(n));
        }


        G1Point K_x = K;

        Bulletproofs bp = new Bulletproofs(r_size, pairing, Gs, H_ys, K_x);

        Bulletproofs.Proof pi = bp.prove(ls, rs);


        G1Point W = G1Point.mulAndSum(Gs, ls).add(G1Point.mulAndSum(H_ys, rs));




        return new Signature(cm, A_hat, A, S, T_1, T_2, t_hat, tau_x, r_W, W, pi);





    }

    public boolean verify(String m, Signature sig) {

        G1Point cm = sig.cm;
        G1Point A_hat = sig.A_hat;
        G1Point A = sig.A;
        G1Point S = sig.S;

        G1Point T_1 = sig.T_1;
        G1Point T_2 = sig.T_2;
        BigInteger t_hat = sig.t_hat;

        BigInteger tau_x = sig.tau_x;
        BigInteger r_W = sig.rW;

        G1Point W =sig.W;

        Bulletproofs.Proof pi =sig.pi;


        BigInteger w = HashUtils.hash(
                HashUtils.hash(cm, A_hat),
                HashUtils.hash(m)
        );
        BigInteger z = HashUtils.hash(A, S).mod(this.n);
        BigInteger y = HashUtils.hash(z).mod(this.n);
        BigInteger x = HashUtils.hash(T_1, T_2).mod(n);

        if (!A.isEqual(cm.mul(w).add(A_hat))){
            return false;
        }



        BigInteger[] zs = new BigInteger[r_size];
        BigInteger[] z2s = new BigInteger[r_size];
        BigInteger[] ys = new BigInteger[r_size];
        BigInteger[] ones = new BigInteger[r_size];
        BigInteger[] v = new BigInteger[r_size];
        BigInteger[] mz = new BigInteger[r_size];



        for (int i = 0; i < r_size; i++) {
            zs[i] = z;
            z2s[i] = z.pow(2).mod(n);
            ys[i] = y.pow(i).mod(n);
            ones[i] = BigInteger.ONE;
            v[i] = z.multiply(y.pow(i)).add(z.pow(2)).mod(n);
            mz[i] = z.negate().mod(n);

        }


        BigInteger delta =(
                (z.subtract(z.pow(2)).
                        multiply(IntUtils.innerProd(ones,ys,n)
                        )).subtract(z.pow(3).multiply(IntUtils.innerProd(ones,ones,n)))).add(z.pow(2)).mod(n);

        G1Point ret1 = F.mul(t_hat).add(Q.mul(tau_x));
        G1Point ret2 = F.mul(delta).add(T_1.mul(x)).add(T_2.mul(x.pow(2).mod(n)));

        if (!ret1.isEqual(ret2)){
            return false;
        }
        System.out.println("pass1");


        G1Point[] H_ys = new G1Point[r_size];
        G1Point[] Gs = new G1Point[r_size];


        for (int i = 0; i < r_size; i++) {
            Gs[i] = pks[i].mul(w).add(Rs[i]);
            H_ys[i] = Hs[i].mul((y.pow(i)).modInverse(n));
        }

        G1Point ret3 = A.add(S.mul(x))
                .add(G1Point.mulAndSum(Gs,mz))
                .add(G1Point.mulAndSum(H_ys,v));


        G1Point K_x = K;
        Bulletproofs bp = new Bulletproofs(r_size, pairing, Gs, H_ys, K_x);
        pi.P = ret3.subtract(Q.mul(r_W));
        pi.p = sig.t_hat;


        return bp.verify(pi);



    }


    public boolean single_multi_verify(String m, Signature sig){
        G1Point cm = sig.cm;
        G1Point A_hat = sig.A_hat;
        G1Point A = sig.A;
        G1Point S = sig.S;

        G1Point T_1 = sig.T_1;
        G1Point T_2 = sig.T_2;
        BigInteger t_hat = sig.t_hat;

        BigInteger tau_x = sig.tau_x;
        BigInteger r_W = sig.rW;

        G1Point W =sig.W;

        Bulletproofs.Proof pi =sig.pi;


        BigInteger w = HashUtils.hash(
                HashUtils.hash(cm, A_hat),
                HashUtils.hash(m)
        );
        BigInteger z = HashUtils.hash(A, S).mod(this.n);
        BigInteger y = HashUtils.hash(z).mod(this.n);
        BigInteger x = HashUtils.hash(T_1, T_2).mod(n);

        if (!A.isEqual(cm.mul(w).add(A_hat))){
            System.out.println("break1");
            return false;
        }



        BigInteger[] zs = new BigInteger[r_size];
        BigInteger[] z2s = new BigInteger[r_size];
        BigInteger[] ys = new BigInteger[r_size];
        BigInteger[] ones = new BigInteger[r_size];
        BigInteger[] v = new BigInteger[r_size];
        BigInteger[] mz = new BigInteger[r_size];



        for (int i = 0; i < r_size; i++) {
            zs[i] = z;
            z2s[i] = z.pow(2).mod(n);
            ys[i] = y.pow(i).mod(n);
            ones[i] = BigInteger.ONE;
            v[i] = z.multiply(y.pow(i)).add(z.pow(2)).mod(n);
            mz[i] = z.negate().mod(n);

        }


        BigInteger delta =((z.subtract(z.pow(2)).
                        multiply(IntUtils.innerProd(ones,ys,n)
                        )).subtract(z.pow(3).multiply(IntUtils.innerProd(ones,ones,n)))).add(z.pow(2)).mod(n);

        G1Point ret1 = F.mul(t_hat).add(Q.mul(tau_x));
        G1Point ret2 = F.mul(delta).add(T_1.mul(x)).add(T_2.mul(x.pow(2).mod(n)));

        if (!ret1.isEqual(ret2)){
            System.out.println("break2");
            return false;
        }



        G1Point Pprime = W.add(K.mul(t_hat));
        G1Point[] Gs = new G1Point[r_size];


        for (int i = 0; i < r_size; i++) {
            Gs[i] = pks[i].mul(w).add(Rs[i]);
        }


        for (int i = 0 ; i < pi.LRs.size(); i ++){

            G1Point[] LR = pi.LRs.get(i);

            G1Point L = LR[0];
            G1Point R = LR[1];

            BigInteger c = HashUtils.hash(LR);
            BigInteger c2 = c.multiply(c).mod(n);
            BigInteger cinv = c.modInverse(n);
            BigInteger cinv2 = cinv.multiply(cinv).mod(n);
            Pprime = L.mul(c2).add(Pprime).add(R.mul(cinv2));


        }

        BigInteger[] es1_1 = new BigInteger[r_size];
        BigInteger[] es2_1 = new BigInteger[r_size];
        BigInteger[] es1_2 = new BigInteger[r_size];
        BigInteger[] es2_2 = new BigInteger[r_size];


        for (int i = 0; i < r_size; i++) {
            BigInteger sum1 = BigInteger.ONE;
            BigInteger sum2 = BigInteger.ONE;

            for (int j = 0; j < pi.LRs.size(); j++) {
                G1Point[] LR = pi.LRs.get(pi.LRs.size()-j-1);
                BigInteger c = HashUtils.hash(LR);
                BigInteger cinv = c.modInverse(n);
                sum1 = sum1.multiply(check_bit(i,j,cinv)).mod(n);
                sum2 = sum2.multiply(check_bit(i,j,c)).mod(n);
            }
            es1_1[i] = sum1.multiply(pi.a).mod(n);
            es2_1[i] = sum2.multiply(pi.b).multiply(ys[i].modInverse(n)).mod(n);;
            es1_2[i] = BigInteger.ZERO.subtract(z).mod(n);
            es2_2[i] = BigInteger.ZERO.add(z.pow(2)).multiply(ys[i].modInverse(n)).add(z).mod(n);
        }



        es1_1 = IntUtils.v_add_v(es1_1,es1_2,n);
        es2_1 = IntUtils.v_add_v(es2_1,es2_2,n);


        G1Point ret3 =G1Point.mulAndSum(Gs,es1_1).add(G1Point.mulAndSum(Hs,es2_1)).add(K.mul(pi.a.multiply(pi.b)));

        G1Point ret5 = ret3.add(A).add(S.mul(x));

        return ret5.isEqual(W.add(Q.mul(r_W).add(Pprime)));
    }
    BigInteger check_bit(int i, int j, BigInteger a ){
        return ((i >> j) & 1) == 1 ? a.modInverse(n) : a;
    }





    public class Signature {
        G1Point cm;
        G1Point A_hat;
        G1Point A;
        G1Point S;

        G1Point T_1;
        G1Point T_2;
        BigInteger t_hat;

        BigInteger tau_x;
        BigInteger rW;

        G1Point W;

        Bulletproofs.Proof pi;

        public Signature(G1Point cm, G1Point a_hat, G1Point a, G1Point s, G1Point t_1, G1Point t_2, BigInteger t_hat, BigInteger tau_x, BigInteger rW, G1Point w, Bulletproofs.Proof pi) {
            this.cm = cm;
            A_hat = a_hat;
            A = a;
            S = s;
            T_1 = t_1;
            T_2 = t_2;
            this.t_hat = t_hat;
            this.tau_x = tau_x;
            this.rW = rW;
            W = w;
            this.pi = pi;
        }
    }

}
