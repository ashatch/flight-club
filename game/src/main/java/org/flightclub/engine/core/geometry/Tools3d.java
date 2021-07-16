/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.core.geometry;

import org.joml.Vector3f;

/*
 * static methods for 3d geometry
 */
public class Tools3d {
  static Vector3f[] circleXy(int numPoints, float radius, Vector3f center) {
    float dtheta = (float) Math.PI * 2 / numPoints;
    Vector3f[] circle = new Vector3f[numPoints];

    for (int i = 0; i < numPoints; i++) {
      float theta = dtheta * i;
      float x = (float) Math.sin(theta) * radius;
      float z = (float) Math.cos(theta) * radius;

      circle[i] = new Vector3f(x, 0, z);
      circle[i].add(center);
    }
    return circle;
  }

  static float[][] identity() {
    return new float[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
  }

  static float[][] zero() {
    return new float[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
  }

  public static float[][] rotateX(Vector3f v) {
    /*
     * rotation matrix: rotate the given point so that it lies on the x axis
     * 1. rotate about z axis
     * 2. rotate about y axis
     * 3. rotate about x axis (so up stays up)
     */

    float r;
    float primeR;
    float[][] m1 = identity();
    float[][] m2 = identity();
    float[][] m4 = identity();

    primeR = (float) Math.sqrt(v.y * v.y + v.x * v.x);
    if (primeR != 0) {
      m1[0][0] = v.x / primeR;
      m1[0][1] = v.y / primeR;
      m1[1][0] = -m1[0][1];
      m1[1][1] = m1[0][0];
    }

    r = v.length();
    if (primeR != 0) {
      m2[0][0] = primeR / r;
      m2[0][2] = v.z / r;
      m2[2][0] = -m2[0][2];
      m2[2][2] = m2[0][0];
    }

    //keep z axis pointing up
    Vector3f up = new Vector3f(0, 0, 1);
    applyTo(m1, up, up);

    applyTo(m2, up, up);

    primeR = (float) Math.sqrt(v.y * v.y + v.z * v.z);
    if (primeR != 0) {
      m4[1][1] = up.z / primeR;
      m4[1][2] = up.y / primeR;
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

  public static void applyTo(float[][] m, Vector3f a, Vector3f b) {
    float[] inV = new float[3];
    float[] outV = {0, 0, 0};

    inV[0] = a.x;
    inV[1] = a.y;
    inV[2] = a.z;

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        outV[i] = outV[i] + m[i][j] * inV[j];
      }
    }
    b.x = outV[0];
    b.y = outV[1];
    b.z = outV[2];
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
   * view (FOV), true otherwise
   */
  public static boolean projectYz(
      final Vector3f a,
      final Vector3f b,
      final float d
  ) {
    b.x = a.x;
    if (a.x >= d) {
      return false; //point behind camera
    }

    float tan;
    float scale = (d - a.x);
    float tanMax = 25;

    tan = a.y / (d - a.x);
    if (tan * tan > tanMax) {
      return false;
    } else {
      b.y = a.y / scale;
    }

    tan = a.z / (d - a.x);
    if (tan * tan > tanMax) {
      return false;
    } else {
      b.z = a.z / scale;
    }

    return true;
  }

  public static double rnd(double lower, double upper) {
    return Math.random() * (upper - lower) + lower;
  }
}
