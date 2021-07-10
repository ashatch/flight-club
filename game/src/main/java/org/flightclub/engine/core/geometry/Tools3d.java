/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.core.geometry;

import java.util.Vector;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.geometry.Object3d;
import org.flightclub.engine.math.Vector3d;

import static org.flightclub.engine.core.RenderManager.DEFAULT_LAYER;

/*
 * static methods for 3d geometry
 */
public class Tools3d {
  public static final float INFINITY = 999999;

  public static final int YZF = 0;
  public static final int YZB = 1;
  public static final int ZXF = 2;
  public static final int ZXB = 3;
  public static final int XYF = 4;
  public static final int XYB = 5;

  static int depth = 50;

  static Vector3d[] circleXy(int numPoints, float radius, Vector3d center) {
    float dtheta = (float) Math.PI * 2 / numPoints;
    Vector3d[] circle = new Vector3d[numPoints];

    for (int i = 0; i < numPoints; i++) {
      float theta = dtheta * i;
      float x = (float) Math.sin(theta) * radius;
      float z = (float) Math.cos(theta) * radius;

      circle[i] = new Vector3d(x, 0, z);
      circle[i].add(center);
    }
    return circle;
  }

  /* get points for unit square in given plane */
  static Vector<Vector3d> square(
      int face,
      float bottom,
      float left,
      float top,
      float right,
      float d
  ) {
    Vector<Vector3d> sq = new Vector<>();
    Vector3d[] ps = new Vector3d[5];

    for (int i = 0; i < 5; i++) {
      ps[i] = new Vector3d(d, d, d);
      sq.addElement(ps[i]);
    }

    switch (face) {
      case YZF:
        ps[0].posY = left;
        ps[0].posZ = bottom;
        ps[1].posY = left;
        ps[1].posZ = top;
        ps[2].posY = right;
        ps[2].posZ = top;
        ps[3].posY = right;
        ps[3].posZ = bottom;
        break;

      case YZB:
        ps[0].posY = left;
        ps[0].posZ = bottom;
        ps[3].posY = left;
        ps[3].posZ = top;
        ps[2].posY = right;
        ps[2].posZ = top;
        ps[1].posY = right;
        ps[1].posZ = bottom;
        break;

      case ZXB:
        ps[0].posX = left;
        ps[0].posZ = bottom;
        ps[1].posX = left;
        ps[1].posZ = top;
        ps[2].posX = right;
        ps[2].posZ = top;
        ps[3].posX = right;
        ps[3].posZ = bottom;
        break;

      case ZXF:
        ps[0].posX = left;
        ps[0].posZ = bottom;
        ps[3].posX = left;
        ps[3].posZ = top;
        ps[2].posX = right;
        ps[2].posZ = top;
        ps[1].posX = right;
        ps[1].posZ = bottom;
        break;

      case XYF:
        ps[0].posX = left;
        ps[0].posY = bottom;
        ps[1].posX = right;
        ps[1].posY = bottom;
        ps[2].posX = right;
        ps[2].posY = top;
        ps[3].posX = left;
        ps[3].posY = top;
        break;

      case XYB:
        ps[0].posX = left;
        ps[0].posY = bottom;
        ps[3].posX = left;
        ps[3].posY = top;
        ps[2].posX = right;
        ps[2].posY = top;
        ps[1].posX = right;
        ps[1].posY = bottom;
        break;

      default:
        throw new RuntimeException("unexpected");
    }

    ps[4].set(ps[0]);
    return sq;
  }

  static Vector<Vector3d> unitSquare(int face, float d) {
    float dx = (float) 0.5;
    return square(face, -dx, -dx, dx, dx, d);
  }

  static Object3d unitCube(boolean isSolid) {
    Object3d cube = new Object3d(DEFAULT_LAYER);
    float d = (float) 0.5;
    Color c = Color.GREEN;

    cube.addWire(unitSquare(XYF, d), c, isSolid);
    c = Color.RED;
    cube.addWire(unitSquare(XYB, -d), c, isSolid);
    c = Color.BLUE;

    cube.addWire(unitSquare(YZF, d), c, isSolid);
    c = Color.MAGENTA;
    cube.addWire(unitSquare(YZB, -d), c, isSolid);
    c = Color.ORANGE;

    cube.addWire(unitSquare(ZXF, d), c, isSolid);
    c = Color.PINK;
    cube.addWire(unitSquare(ZXB, -d), c, isSolid);
    c = Color.YELLOW;

    return cube;
  }

  static float[][] identity() {
    return new float[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
  }

  static float[][] zero() {
    return new float[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
  }

  public static float[][] rotateX(Vector3d v) {
    /*
     * rotation matix: rotate the given point so that it lies on the x axis
     * 1. rotate about z axis
     * 2. rotate about y axis
     * 3. rotate about x axis (so up stays up)
     */

    float r;
    float primeR;
    float[][] m1 = identity();
    float[][] m2 = identity();
    float[][] m4 = identity();

    primeR = (float) Math.sqrt(v.posY * v.posY + v.posX * v.posX);
    if (primeR != 0) {
      m1[0][0] = v.posX / primeR;
      m1[0][1] = v.posY / primeR;
      m1[1][0] = -m1[0][1];
      m1[1][1] = m1[0][0];
    }

    r = v.length();
    if (primeR != 0) {
      m2[0][0] = primeR / r;
      m2[0][2] = v.posZ / r;
      m2[2][0] = -m2[0][2];
      m2[2][2] = m2[0][0];
    }

    //keep z azis pointing up
    Vector3d up = new Vector3d(0, 0, 1);
    applyTo(m1, up, up);

    applyTo(m2, up, up);

    primeR = (float) Math.sqrt(v.posY * v.posY + v.posZ * v.posZ);
    if (primeR != 0) {
      m4[1][1] = up.posZ / primeR;
      m4[1][2] = up.posY / primeR;
      m4[2][1] = -m4[1][2];
      m4[2][2] = m4[1][1];
    }

    float[][] m3 = identity();
    float[][] m5 = identity();
    m3 = applyTo(m2, m1);
    m5 = applyTo(m4, m3);
    //return m5;
    return m3;
  }

  public static void applyTo(float[][] m, Vector3d a, Vector3d b) {
    float[] inV = new float[3];
    float[] outV = {0, 0, 0};

    inV[0] = a.posX;
    inV[1] = a.posY;
    inV[2] = a.posZ;

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        outV[i] = outV[i] + m[i][j] * inV[j];
      }
    }
    b.posX = outV[0];
    b.posY = outV[1];
    b.posZ = outV[2];
  }

  public static float[][] applyTo(float[][] m1, float[][] m2) {
    float[][] m3 = zero();

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        for (int k = 0; k < 3; k++) {
          m3[i][j] = m3[i][j] + m1[i][k] * m2[k][j];
        }
      }
    }
    return m3;
  }

  /*
   * return false if point falls outside field of
   *view (FOV), true otherwise
   */
  public static boolean projectYz(Vector3d a, Vector3d b, float d) {

    b.posX = a.posX;
    if (a.posX >= d) {
      return false; //point behind camera
    }

    float tan;
    float scale = (d - a.posX);
    float tanMax = 25;

    tan = a.posY / (d - a.posX);
    if (tan * tan > tanMax) {
      return false;
    } else {
      b.posY = a.posY / scale;
    }

    tan = a.posZ / (d - a.posX);
    if (tan * tan > tanMax) {
      return false;
    } else {
      b.posZ = a.posZ / scale;
    }

    return true;
  }

  public static double rnd(double lower, double upper) {
    return Math.random() * (upper - lower) + lower;
  }
}
