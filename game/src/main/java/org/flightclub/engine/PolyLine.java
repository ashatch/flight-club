/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

public class PolyLine {
  final int numPoints;
  final int[] points;
  final Object3d object3d;

  private Color trueColor;
  Color apparentColor;

  boolean isSolid = false;

  Vector3d normal;

  private int nextIndex = 0;

  public PolyLine(Object3d o, int inNumPoints, Color inColor) {
    numPoints = inNumPoints;
    points = new int[numPoints];
    object3d = o;
    trueColor = inColor;
    apparentColor = inColor;
  }

  public Color getTrueColor() {
    return trueColor;
  }

  public void setTrueColor(Color trueColor) {
    this.trueColor = trueColor;
  }

  public void addPoint(int point) {
    points[nextIndex] = point;
    nextIndex++;
  }

  boolean isBackFace(Vector3d eye) {
    if (normal == null) {
      return false;
    }

    setNormal();    //now ???

    Vector3d p = object3d.points.elementAt(points[0]);
    Vector3d ray = p.minus(eye);

    return normal.dot(ray) >= 0;
  }

  void setNormal() {
    if (numPoints < 3) {
      return;
    }

    Vector3d[] ps = new Vector3d[3];
    for (int i = 0; i < 3; i++) {
      ps[i] = object3d.points.elementAt(points[i]);
    }

    Vector3d e1 = ps[0].minus(ps[1]);
    Vector3d e2 = ps[2].minus(ps[1]);

    normal = new Vector3d(e1).cross(e2).makeUnit();
  }

  public void draw(Graphics g, CameraMan cameraMan) {
    Vector3d a;
    Vector3d b;

    if (numPoints <= 1) {
      return;
    }
    g.setColor(this.getColor(cameraMan));

    for (int i = 0; i < numPoints - 1; i++) {
      a = object3d.cameraSpacePoints.elementAt(points[i]);
      b = object3d.cameraSpacePoints.elementAt(points[i + 1]);

      boolean inFieldOfView1 = object3d.flagsInFieldOfView.elementAt(points[i]);
      boolean inFieldOfView2 = object3d.flagsInFieldOfView.elementAt(points[i + 1]);

      if (inFieldOfView1 && inFieldOfView2) {
        g.drawLine((int) a.posY, (int) a.posZ, (int) b.posY, (int) b.posZ);
      }
    }
  }

  Color getColor(CameraMan cameraMan) {
    if (normal == null) {
      return trueColor;
    }
    float light = cameraMan.surfaceLight(normal);
    apparentColor = trueColor.mul(light);

    //fogging
    Vector3d p = object3d.cameraSpacePoints.elementAt(points[0]);
    return cameraMan.foggyColor(p.posX, apparentColor);
  }
}
