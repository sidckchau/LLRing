package CurveOperation;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

import java.math.BigInteger;

public class G1Point {

    public Element point;

    public G1Point(Element point) {
        this.point = point;
    }

    public BigInteger getX(){
        CurveElement p = (CurveElement) point;
        return p.getX().toBigInteger();
    }

    public BigInteger getY(){
        CurveElement p = (CurveElement) point;
        return p.getY().toBigInteger();
    }

    public G1Point duplicate(){
        return new G1Point(point.duplicate());
    }

    public G1Point Zero(){
        return new G1Point(point.duplicate().setToZero());
    }

    public static G1Point add(G1Point p1, G1Point p2){
        Element e1 = p1.point.duplicate();
        Element e2 = p2.point.duplicate();

        return new G1Point(e1.add(e2));
    }

    public static G1Point[] add(G1Point[] ps1, G1Point[] ps2){
        G1Point[] ps = new G1Point[ps1.length];
        for (int i = 0; i < ps.length ; i++){
            Element e1 = ps1[i].point.duplicate();
            Element e2 = ps2[i].point.duplicate();
            ps[i] = new G1Point(e1.add(e2));
        }
        return ps;
    }

    public G1Point add(G1Point p){
        Element e1 = this.point.duplicate();
        Element e2 = p.point.duplicate();
        return new G1Point(e1.add(e2));
    }

    public G1Point negate() {
        Element e1 = this.point.duplicate();

        return new G1Point(e1.negate());
    }


    public G1Point subtract(G1Point p){
        Element e1 = this.point.duplicate();
        Element e2 = p.point.duplicate();
        return new G1Point(e1.sub(e2));
    }

    public static G1Point sum(G1Point[] ps ){
        G1Point res = ps[0].duplicate().Zero();
        for (int i = 0; i < ps.length ; i++){
            res = res.add(ps[i]);
        }
        return res;
    }

    public static G1Point sum(G1Point[][] ps ){
        G1Point res = ps[0][0].duplicate().Zero();
        for (int i = 0; i < ps.length ; i++){
            for (int j = 0; j < ps[i].length; j++) {
                res = res.add(ps[i][j]);
            }
        }
        return res;
    }

    public G1Point mul(BigInteger n){
        Element e1 = this.point.duplicate();
        return new G1Point(e1.mul(n));
    }

    public static G1Point mul(G1Point p, BigInteger i ){
        Element e = p.point.duplicate();
        return new G1Point(e.mul(i));
    }


    public static G1Point[] mul(G1Point[] ps, BigInteger c){
        G1Point[] ps_t = new G1Point[ps.length];
        for (int i = 0; i < ps.length ; i++){
            ps_t[i] = ps[i].mul(c);
        }
        return ps_t;
    }

    public static G1Point[] mul(G1Point[] ps, BigInteger[] cs){
        G1Point[] ps_t = new G1Point[ps.length];
        for (int i = 0; i < ps.length ; i++){
            ps_t[i] = ps[i].mul(cs[i]);
        }
        return ps_t;
    }


    public static G1Point mulAndSum(G1Point g, BigInteger[] cs){
        G1Point temp = g.Zero();
        for (BigInteger c : cs){
            temp = add(temp,mul(g,c));
        }
        return temp;
    }

    public static G1Point mulAndSum(G1Point[] g, BigInteger[] cs){
        G1Point temp =mul(g[0],cs[0]);
        for (int i = 1 ; i < g.length ; i ++){
            temp = add(temp,mul(g[i],cs[i]));
        }
        return temp;
    }

    public static G1Point mulAndSum(G1Point[][] gs, BigInteger[][] vs){
        G1Point temp = gs[0][0].subtract(gs[0][0]);
        for (int i = 0; i < gs.length; i++) {
            for (int j = 0; j < gs[i].length; j++) {
//                System.out.println(vs[i[]]);
                temp = temp.add(gs[i][j].mul(vs[i][j]));
            }
        }
        return temp;
    }

    public static G1Point PedersenVectorCom(G1Point[] Ps, BigInteger[] vs, G1Point Q, BigInteger r) {
        G1Point temp = mulAndSum(Ps,vs);
        temp = temp.add(Q.mul(r));
        return temp;
    }

    public static G1Point PedersenVectorCom(G1Point[][] Ps, BigInteger[][] vs, G1Point Q, BigInteger r) {
        G1Point temp = mulAndSum(Ps,vs);
        temp = temp.add(Q.mul(r));
        return temp;
    }

    public boolean isEqual( G1Point p){
        return this.point.isEqual(p.point);
    }


    @Override
    public String toString() {
        return "["+getX()+", "+getY()+"]";
    }


}

