package org.flightclub.engine.models;

import java.util.Vector;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.geometry.Object3d;
import org.flightclub.engine.math.Vector3d;

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
  public Vector<Vector3d> square(
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

  private Vector<Vector3d> unitSquare(int face, float d) {
    float dx = (float) 0.5;
    return square(face, -dx, -dx, dx, dx, d);
  }
}
