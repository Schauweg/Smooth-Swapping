package schauweg.smoothswapping.config;

import org.jetbrains.annotations.NotNull;

public class Vec2 implements Comparable<Vec2>{
    static double Resolution = 72;
    static int xoffset = 120;
    static int yoffset = 120;
    public double[] v; // coordinates of the Point

    // No-argument constructor
//    public Vec2() {
//        this(0, 0);
//    }


    // Constructor
    public Vec2(double x, double y) {
        v = new double[2];
        set(x, y);
    }

    // copy constructor
    public Vec2(Vec2 src) {
        this(src.v[0], src.v[1]);
    }

    // Constructor (angle theta)
    public Vec2(double theta) {
        theta *= radg;
        v = new double[2];
        set(Math.cos(theta), Math.sin(theta));
    }


    public int length() {
        return v.length;
    }


    // Set x and y coordinates of Point
    public Vec2 set(double x, double y) {
        v[0] = x;
        v[1] = y;
        return this;
    }

    public int[] convert() {
        int[] iv = new int[2];
        convert(iv);
        return iv;
    }

    public void convert(int[] iv) {
        iv[0] = (int) (xoffset + v[0] * Resolution);
        iv[1] = (int) (yoffset - v[1] * Resolution);
    }

    static public Vec2 sum(Vec2 a, Vec2 b) {
        return new Vec2(a.v[0] + b.v[0], a.v[1] + b.v[1]);
    }

    public Vec2 sum(Vec2 b){
        this.v[0] += b.v[0];
        this.v[1] += b.v[1];
        return this;
    }

    public Vec2 sumScalar(double b) {
        this.v[0] += b;
        this.v[1] += b;
        return this;
    }

    public Vec2 diffScalar(double b) {
        this.v[0] -= b;
        this.v[1] -= b;
        return this;
    }

    public Vec2 divideScalar(double b) {
        this.v[0] /= b;
        this.v[1] /= b;
        return this;
    }

    public Vec2 multiplyScalar(double b) {
        this.v[0] *= b;
        this.v[1] *= b;
        return this;
    }

    public Vec2 diff(Vec2 b){
        this.v[0] =- b.v[0];
        this.v[1] =- b.v[1];
        return this;
    }

    static public Vec2 diff(Vec2 a, Vec2 b) {
        return new Vec2(a.v[0] - b.v[0], a.v[1] - b.v[1]);
    }

    /**
     * returns magnitude of 2D vector
     */
    public double magn() {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1]);
    }

    // multiply point by a scalar
    public Vec2 scale(double s) {
        v[0] *= s;
        v[1] *= s;
        return this;
    }

    public Vec2 copy(){
        return new Vec2(this);
    }


    // offset (translate) point by the amount (tx, ty)
    public Vec2 translate(double tx, double ty) {
        v[0] += tx;
        v[1] += ty;
        return this;
    }

    // offset (translate) point by a Vec2
    public Vec2 translate(Vec2 b) {
        v[0] += b.v[0];
        v[1] += b.v[1];
        return this;
    }

    final static double radg = Math.atan(1) / 45.0;

    // rotate point about the origin
    public Vec2 rotate(double theta) {
        double c, s, t;
        theta *= radg;
        c = Math.cos(theta);
        s = Math.sin(theta);
        t = v[1] * c + v[0] * s;
        v[0] = v[0] * c - v[1] * s;
        v[1] = t;
        return this;
    }

    public Vec2 perp() {
        double t = v[0];
        v[0] = -v[1];
        v[1] = t;
        return this;
    }

    static public Vec2 perp(Vec2 a) {
        return new Vec2(-a.v[1], a.v[0]);
    }

    static public double perpdot(Vec2 a, Vec2 b) {
        return (a.v[0] * b.v[1] - a.v[1] * b.v[0]);
    }

    static public double dot(Vec2 a, Vec2 b) {
        return (a.v[0] * b.v[0] + a.v[1] * b.v[1]);
    }

    static public double distance(Vec2 a, Vec2 b) {
        return Vec2.diff(a, b).magn();
    }

    //! returns angle (in degrees) between two vectors
    static public double angle(Vec2 a, Vec2 b) {
        double c, s;
        c = dot(a, b);
        s = perpdot(a, b);
        return Math.atan2(s, c) / radg;
    }

    public void setSize(double sz) {
        double vnorm = magn();
        if (vnorm == 0.0) return;
        sz /= vnorm;
        v[0] *= sz;
        v[1] *= sz;
    }

    // convert the point into a String representation
    public String toString() {
        return String.format("[%.3g %.3g]", v[0], v[1]);
    }

    @Override
    public int compareTo(@NotNull Vec2 o) {
        return Double.compare(this.v[0], o.v[0]);
    }
}