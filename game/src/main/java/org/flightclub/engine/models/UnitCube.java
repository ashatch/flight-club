package org.flightclub.engine.models;

import java.util.Vector;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.geometry.Object3d;
import org.joml.Vector3f;

public class UnitCube extends Object3d {
  public static final int YZF = 0;
  public static final int YZB = 1;
  public static final int ZXF = 2;
  public static final int ZXB = 3;
  public static final int XYF = 4;
  public static final int XYB = 5;

  public UnitCube(int layer, boolean isSolid) {
    super(layer);
    init(isSolid);
  }

  private void init(boolean isSolid) {
    float d = (float) 0.5;
    Color c = Color.GREEN;

    this.addWire(unitSquare(XYF, d), c, isSolid);
    c = Color.RED;
    this.addWire(unitSquare(XYB, -d), c, isSolid);
    c = Color.BLUE;

    this.addWire(unitSquare(YZF, d), c, isSolid);
    c = Color.MAGENTA;
    this.addWire(unitSquare(YZB, -d), c, isSolid);
    c = Color.ORANGE;

    this.addWire(unitSquare(ZXF, d), c, isSolid);
    c = Color.PINK;
    this.addWire(unitSquare(ZXB, -d), c, isSolid);
    c = Color.YELLOW;
  }

  /* get points for unit square in given plane */
  public Vector<Vector3f> square(
      int face,
      float bottom,
      float left,
      float top,
      float right,
      float d
  ) {
    Vector<Vector3f> sq = new Vector<>();
    Vector3f[] ps = new Vector3f[5];

    for (int i = 0; i < 5; i++) {
      ps[i] = new Vector3f(d, d, d);
      sq.addElement(ps[i]);
    }

    switch (face) {
      case YZF:
        ps[0].y = left;
        ps[0].z = bottom;
        ps[1].y = left;
        ps[1].z = top;
        ps[2].y = right;
        ps[2].z = top;
        ps[3].y = right;
        ps[3].z = bottom;
        break;

      case YZB:
        ps[0].y = left;
        ps[0].z = bottom;
        ps[3].y = left;
        ps[3].z = top;
        ps[2].y = right;
        ps[2].z = top;
        ps[1].y = right;
        ps[1].z = bottom;
        break;

      case ZXB:
        ps[0].x = left;
        ps[0].z = bottom;
        ps[1].x = left;
        ps[1].z = top;
        ps[2].x = right;
        ps[2].z = top;
        ps[3].x = right;
        ps[3].z = bottom;
        break;

      case ZXF:
        ps[0].x = left;
        ps[0].z = bottom;
        ps[3].x = left;
        ps[3].z = top;
        ps[2].x = right;
        ps[2].z = top;
        ps[1].x = right;
        ps[1].z = bottom;
        break;

      case XYF:
        ps[0].x = left;
        ps[0].y = bottom;
        ps[1].x = right;
        ps[1].y = bottom;
        ps[2].x = right;
        ps[2].y = top;
        ps[3].x = left;
        ps[3].y = top;
        break;

      case XYB:
        ps[0].x = left;
        ps[0].y = bottom;
        ps[3].x = left;
        ps[3].y = top;
        ps[2].x = right;
        ps[2].y = top;
        ps[1].x = right;
        ps[1].y = bottom;
        break;

      default:
        throw new RuntimeException("unexpected");
    }

    ps[4].set(ps[0]);
    return sq;
  }

  private Vector<Vector3f> unitSquare(int face, float d) {
    float dx = (float) 0.5;
    return square(face, -dx, -dx, dx, dx, d);
  }
}
