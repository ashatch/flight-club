/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.core.geometry;

import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.camera.CameraMan;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.Graphics;
import org.flightclub.engine.math.Vector3d;

import static org.flightclub.engine.camera.Camera.BACKGROUND;
import static org.flightclub.engine.camera.Camera.BACKGROUND_B;
import static org.flightclub.engine.camera.Camera.BACKGROUND_G;
import static org.flightclub.engine.camera.Camera.BACKGROUND_R;
import static org.flightclub.engine.camera.Camera.DEPTH_OF_VISION;

public class PolyLine {
  public final int numPoints;
  public final int[] points;
  final Object3d object3d;

  private Color trueColor;
  Color apparentColor;

  public boolean isSolid = false;

  public Vector3d normal;

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

  public boolean isBackFace(Vector3d eye) {
    if (normal == null) {
      return false;
    }

    setNormal();    //now ???

    Vector3d p = object3d.points.elementAt(points[0]);
    Vector3d ray = p.minus(eye);

    return normal.dot(ray) >= 0;
  }

  public void setNormal() {
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

  public void draw(Graphics g, Camera camera) {
    Vector3d a;
    Vector3d b;

    if (numPoints <= 1) {
      return;
    }
    g.setColor(this.getColor(camera));

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

  Color getColor(Camera camera) {
    if (normal == null) {
      return trueColor;
    }
    float light = camera.surfaceLight(normal);
    apparentColor = trueColor.mul(light);

    //fogging
    Vector3d p = object3d.cameraSpacePoints.elementAt(points[0]);
    return foggyColor(p.posX, apparentColor);
  }


  /*
   * mute distant colors.
   *
   * x is ~ distance of surface from camera
   * since we are using the transformed coords
   */
  public Color foggyColor(float x, Color c) {
    if (x >= 0) {
      return c;
    }

    x *= -1;

    if (x > DEPTH_OF_VISION) {
      return BACKGROUND;
    }

    float f = x / DEPTH_OF_VISION;
    int r = (int) (c.r() + f * (BACKGROUND_R - c.r()));
    int g = (int) (c.g() + f * (BACKGROUND_G - c.g()));
    int b = (int) (c.b() + f * (BACKGROUND_B - c.b()));

    return new Color(r, g, b);
  }
}
