package CurveOperation;

import it.unisa.dia.gas.jpbc.Element;

import java.math.BigInteger;

public class G2Point {

    public Element point;

    public G2Point(Element point) {
        this.point = point;
    }

    public G2Point duplicate(){
        return new G2Point(point.duplicate());
    }

    public static G2Point add(G2Point p1, G2Point p2){
        Element e1 = p1.point.duplicate();
        Element e2 = p2.point.duplicate();

        return new G2Point(e1.add(e2));
    }

    public static G2Point mul(G2Point p, BigInteger i ){
        Element e = p.point.duplicate();
        return new G2Point(e.mul(i));
    }

    public G2Point add(G2Point p){
        Element e1 = this.point.duplicate();
        Element e2 = p.point.duplicate();
        return new G2Point(e1.add(e2));
    }

    public static G2Point[] add(G2Point[] ps1, G2Point[] ps2){
        G2Point[] ps = new G2Point[ps1.length];
        for (int i = 0; i < ps.length ; i++){
            Element e1 = ps1[i].point.duplicate();
            Element e2 = ps2[i].point.duplicate();
            ps[i] = new G2Point(e1.add(e2));
        }
        return ps;
    }


    public G2Point subtract(G2Point p){
        Element e1 = this.point.duplicate();
        Element e2 = p.point.duplicate();
        return new G2Point(e1.sub(e2));
    }

    public G2Point mul(BigInteger n){
        Element e1 = this.point.duplicate();
        return new G2Point(e1.mul(n));
    }

    public static G2Point[] mul(G2Point[] ps, BigInteger[] c){
        G2Point[] ps_t = new G2Point[ps.length];
        for (int i = 0; i < ps.length ; i++){
            ps_t[i] = ps[i].mul(c[i]);
        }
        return ps_t;
    }

    public static G2Point[] mul(G2Point[] ps, BigInteger c){
        G2Point[] ps_t = new G2Point[ps.length];
        for (int i = 0; i < ps.length ; i++){
            ps_t[i] = ps[i].mul(c);
        }
        return ps_t;
    }

    public static G2Point mulAndSum(G2Point[] g, BigInteger[] cs){
        G2Point temp =mul(g[0],cs[0]);
        for (int i = 1 ; i < g.length ; i ++){
            temp = add(temp,mul(g[i],cs[i]));
        }
        return temp;
    }


    @Override
    public String toString() {
        return super.toString();
    }
}
