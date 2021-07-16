/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.core.geometry;

import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.Graphics;
import org.joml.Vector3f;

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

  public Vector3f normal;

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

  public boolean isBackFace(final Vector3f eye) {
    if (normal == null) {
      return false;
    }

    setNormal();

    Vector3f p = object3d.points.elementAt(points[0]);
    Vector3f ray = new Vector3f(p).sub(eye);

    return normal.dot(ray) >= 0;
  }

  public void setNormal() {
    if (numPoints < 3) {
      return;
    }

    Vector3f[] ps = new Vector3f[3];
    for (int i = 0; i < 3; i++) {
      ps[i] = object3d.points.elementAt(points[i]);
    }

    Vector3f e1 = new Vector3f(ps[0]).sub(ps[1]);
    Vector3f e2 = new Vector3f(ps[2]).sub(ps[1]);

    normal = new Vector3f(e1).cross(e2).normalize();
  }

  public void draw(final Graphics g, final Camera camera) {
    Vector3f a;
    Vector3f b;

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
        g.drawLine((int) a.y, (int) a.z, (int) b.y, (int) b.z);
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
    Vector3f p = object3d.cameraSpacePoints.elementAt(points[0]);
    return foggyColor(p.x, apparentColor);
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

    return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
  }
}
