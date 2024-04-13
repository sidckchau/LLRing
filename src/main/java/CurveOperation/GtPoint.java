package CurveOperation;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;

import java.math.BigInteger;

public class GtPoint {

    public Element point;
    static Pairing pairing;

    public static void setPairing(Pairing p){

        pairing =p;
    }

    public byte[] getBytes(){
        return  point.toBytes();
    }




    public GtPoint(Element point) {
        this.point = point;
    }

    public GtPoint duplicate(){
        return new GtPoint(point.duplicate());
    }

    public static GtPoint mul(GtPoint p1, GtPoint p2 ){
        Element e1 = p1.point.duplicate();
        Element e2 = p2.point.duplicate();
        return new GtPoint(e1.mul(e2));
    }

    public GtPoint mul( GtPoint p2 ){
        Element e1 = point.duplicate();
        Element e2 = p2.point.duplicate();
        return new GtPoint(e1.mul(e2));
    }

    public static GtPoint div(GtPoint p1, GtPoint p2 ){
        Element e1 = p1.point.duplicate();
        Element e2 = p2.point.duplicate();
        return new GtPoint(e1.div(e2));
    }

    public GtPoint div( GtPoint p2 ){
        Element e1 = point.duplicate();
        Element e2 = p2.point.duplicate();
        return new GtPoint(e1.div(e2));
    }

    public static GtPoint pow(GtPoint p, BigInteger i ){
        Element e = p.point.duplicate();
        return new GtPoint(e.pow(i));
    }

    public GtPoint pow( BigInteger i ){
        Element e = point.duplicate();
        return new GtPoint(e.pow(i));
    }

    public static GtPoint pair(G1Point p1, G2Point p2){
        Element e1 = p1.point.duplicate();
        Element e2 = p2.point.duplicate();

        return new GtPoint(pairing.pairing(e1,e2));
    }

    public static GtPoint innerProd (G1Point[] g1s, G2Point[] g2s ){

        GtPoint result = GtPoint.pair(g1s[0],g2s[0]);

        for (int i =1; i< g1s.length; i++){
            result = GtPoint.mul(result,GtPoint.pair(g1s[i],g2s[i]));
        }

        return result;
    }

    public static boolean isEquals(GtPoint p1 , GtPoint p2){
        return p1.point.isEqual(p2.point);
    }

    public boolean isEqual( GtPoint p){
        return this.point.isEqual(p.point);
    }
    @Override
    public String toString() {
        return super.toString();
    }



}
