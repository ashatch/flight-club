/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.core.geometry;

import java.util.Vector;
import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.RenderContext;
import org.flightclub.engine.core.Renderable;
import org.joml.Vector3f;


public class Object3d implements Renderable {
  private final int layer;
  private boolean inFov = false;

  public final Vector<Vector3f> points = new Vector<>();
  public final Vector<Vector3f> cameraSpacePoints = new Vector<>();
  public final Vector<PolyLine> wires = new Vector<>();

  // list of flags - is point within field of view
  public final Vector<Boolean> flagsInFieldOfView = new Vector<>();

  public Object3d(int layer) {
    this.layer = layer;
  }

  public int getLayer() {
    return layer;
  }


  @Override
  public void render(RenderContext context) {
    project(context.camera());

    if (!inFov) {
      return;
    }

    for (int i = 0; i < wires.size(); i++) {
      PolyLine wire = wires.elementAt(i);

      if (!wire.isBackFace(context.camera().getEye())) {
        wire.draw(context.graphics(), context.camera());
      }
    }
  }

  /* use camera to get from 3d to 2d */
  void project(final Camera camera) {
    inFov = false; //set true if any points are in FOV

    for (int i = 0; i < points.size(); i++) {
      final Vector3f vector = points.elementAt(i);
      final Vector3f vectorPrime = cameraSpacePoints.elementAt(i);

      //translate, rotate and project (only if visible)
      vectorPrime.set(vector).sub(camera.getFocus());
      Tools3d.applyTo(camera.getMatrix(), vectorPrime, vectorPrime);

      boolean rc = Tools3d.projectYz(vectorPrime, vectorPrime, camera.getDistance());
      inFov = inFov || rc;
      flagsInFieldOfView.setElementAt(rc, i);
      camera.scaleToScreen(vectorPrime);
    }
  }

  public void scaleBy(final float s) {
    points.forEach(v -> v.mul(s));
  }

  public void setColor(final Color color) {
    wires.forEach(wire -> wire.setTrueColor(color));
  }

  protected int addPoint(Vector3f p) {
    Vector3f q;
    int index = 0;
    boolean found = false;

    for (int i = 0; i < points.size(); i++) {
      q = points.elementAt(i);
      if ((q.x == p.x) && (q.y == p.y) && (q.z == p.z)) {
        index = i;
        found = true;
        break;
      }
    }

    if (found) {
      return index;
    } else {
      points.addElement(p);
      cameraSpacePoints.addElement(new Vector3f());
      flagsInFieldOfView.addElement(false);
      return points.size() - 1;
    }
  }

  public int addWire(Vector<Vector3f> wirePoints, Color c, boolean isSolid) {
    //default has normal (ie only one side of surface is visible)
    return addWire(wirePoints, c, isSolid, true);
  }

  public int addWire(Vector<Vector3f> wirePoints, Color c, boolean isSolid, boolean hasNormal) {
    PolyLine wire;
    if (isSolid) {
      wire = new Surface(this, wirePoints.size(), c);
    } else {
      wire = new PolyLine(this, wirePoints.size(), c);
    }

    for (Vector3f wirePoint : wirePoints) {
      int pointIndex = this.addPoint(wirePoint);
      wire.addPoint(pointIndex);
    }

    if (hasNormal) {
      wire.setNormal();
    }

    wires.addElement(wire);
    return wires.size() - 1;
  }

  public void addTile(Vector3f[] corners, Color c, boolean isSolid, boolean isConcave) {
    /*
     * special case of the above - we are passed four
     * points and make two triangles to tessalate the
     * surface defined by these four points - diagonal
     * may join either corners 0 and 2 or 1 and 3; this
     * makes tile either concave or convex.
     */

    Vector<Vector3f> wire1 = new Vector<>();
    Vector<Vector3f> wire2 = new Vector<>();

    float h1 = corners[0].z + corners[2].z;
    float h2 = corners[1].z + corners[3].z;

    if (h1 < h2 && isConcave || h1 > h2 && !isConcave) {

      wire1.addElement(corners[0]);
      wire1.addElement(corners[1]);
      wire1.addElement(corners[2]);
      wire1.addElement(corners[0]);

      wire2.addElement(corners[2]);
      wire2.addElement(corners[3]);
      wire2.addElement(corners[0]);
      wire2.addElement(corners[2]);

    } else {

      wire1.addElement(corners[0]);
      wire1.addElement(corners[1]);
      wire1.addElement(corners[3]);
      wire1.addElement(corners[0]);

      wire2.addElement(corners[2]);
      wire2.addElement(corners[3]);
      wire2.addElement(corners[1]);
      wire2.addElement(corners[2]);

    }

    addWire(wire1, c, isSolid, true);
    addWire(wire2, c, isSolid, true);
  }

  public static void clone(Object3d from, Object3d to) {
    for (PolyLine fromWire : from.wires) {
      Vector<Vector3f> toWire = new Vector<>();
      for (int k : fromWire.points) {
        Vector3f v = from.points.elementAt(k);
        toWire.addElement(new Vector3f(v));
      }

      boolean hasNorm = (fromWire.normal != null);
      to.addWire(toWire, fromWire.getTrueColor(), fromWire.isSolid, hasNorm);
    }
  }

  public void reverse() {
    for (int i = 0; i < wires.size() / 2 - 1; i++) {
      int j = wires.size() - 1 - i;
      PolyLine wire1 = wires.elementAt(i);
      PolyLine wire2 = wires.elementAt(j);

      wires.setElementAt(wire2, i);
      wires.setElementAt(wire1, j);
    }
  }
}
