/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;


public class Object3d {
  protected XcGame app;

  final Vector<Vector3d> points = new Vector<>();
  final Vector<Vector3d> cameraSpacePoints = new Vector<>();
  final Vector<PolyLine> wires = new Vector<>();

  // list of flags - is point within field of view
  final Vector<Boolean> flagsInFieldOfView = new Vector<>();

  boolean inFov = false;

  // default layer 1
  int layer;

  Object3d(XcGame theApp) {
    this(theApp, true);
  }

  Object3d(XcGame theApp, boolean register) {
    this(theApp, register, 1);
  }

  Object3d(XcGame theApp, boolean register, int inLayer) {
    app = theApp;
    layer = inLayer;
    if (register) {
      registerObject3d();
    }
  }

  public void destroyMe() {
    app.obj3dManager.removeObj(this);
  }

  void registerObject3d() {
    app.obj3dManager.addObj(this);
  }

  public void draw(Graphics g) {
    if (!inFov) {
      return;
    }

    for (int i = 0; i < wires.size(); i++) {
      PolyLine wire = wires.elementAt(i);

      if (!wire.isBackFace(app.cameraMan.getEye())) {
        wire.draw(g);
      }
    }
  }

  /* use camera to get from 3d to 2d */
  void film(CameraMan camera) {
    Vector3d vector;
    Vector3d vectorPrime;

    inFov = false; //set true if any points are in FOV

    for (int i = 0; i < points.size(); i++) {
      vector = points.elementAt(i);
      vectorPrime = cameraSpacePoints.elementAt(i);

      //translate, rotate and project (only if visible)
      vectorPrime.set(vector).subtract(camera.getFocus());
      Tools3d.applyTo(camera.getMatrix(), vectorPrime, vectorPrime);

      boolean rc = Tools3d.projectYz(vectorPrime, vectorPrime, camera.getDistance());
      inFov = inFov || rc;
      flagsInFieldOfView.setElementAt(rc, i);
      camera.scaleToScreen(vectorPrime);
    }

  }

  public void scaleBy(float s) {
    for (Vector3d v : points) {
      v.scaleBy(s);
    }
  }

  public void setColor(Color c) {
    for (PolyLine wire : wires) {
      wire.trueColor = c;
    }
  }

  protected int addPoint(Vector3d p) {
    Vector3d q;
    int index = 0;
    boolean found = false;

    for (int i = 0; i < points.size(); i++) {
      q = points.elementAt(i);
      if ((q.posX == p.posX) && (q.posY == p.posY) && (q.posZ == p.posZ)) {
        index = i;
        found = true;
        break;
      }
    }

    if (found) {
      return index;
    } else {
      points.addElement(p);
      cameraSpacePoints.addElement(new Vector3d());
      flagsInFieldOfView.addElement(false);
      return points.size() - 1;
    }
  }

  public int addWire(Vector<Vector3d> wirePoints, Color c) {
    //default - solid and has normal (ie only one side of surface is visible)
    return addWire(wirePoints, c, true, true);
  }

  public int addWire(Vector<Vector3d> wirePoints, Color c, boolean isSolid) {
    //default has normal (ie only one side of surface is visible)
    return addWire(wirePoints, c, isSolid, true);
  }

  public int addWire(Vector<Vector3d> wirePoints, Color c, boolean isSolid, boolean hasNormal) {
    PolyLine wire;
    if (isSolid) {
      wire = new Surface(this, wirePoints.size(), c);
    } else {
      wire = new PolyLine(this, wirePoints.size(), c);
    }

    for (Vector3d wirePoint : wirePoints) {
      int pointIndex = this.addPoint(wirePoint);
      wire.addPoint(pointIndex);
    }

    if (hasNormal) {
      wire.setNormal();
    }

    wires.addElement(wire);
    return wires.size() - 1;
  }

  public void addTile(Vector3d[] corners, Color c, boolean isSolid, boolean isConcave) {
    /*
     * special case of the above - we are passed four
     * points and make two triangles to tessalate the
     * surface defined by these four points - diagonal
     * may join either corners 0 and 2 or 1 and 3; this
     * makes tile either concave or convex.
     */

    Vector<Vector3d> wire1 = new Vector<>();
    Vector<Vector3d> wire2 = new Vector<>();

    float h1 = corners[0].posZ + corners[2].posZ;
    float h2 = corners[1].posZ + corners[3].posZ;

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

      Vector<Vector3d> toWire = new Vector<>();
      for (int k : fromWire.points) {
        Vector3d v = from.points.elementAt(k);
        toWire.addElement(new Vector3d(v));
      }

      boolean hasNorm = (fromWire.normal != null);
      to.addWire(toWire, fromWire.trueColor, fromWire.isSolid, hasNorm);
    }
  }

  void reverse() {
    for (int i = 0; i < wires.size() / 2 - 1; i++) {
      int j = wires.size() - 1 - i;
      PolyLine wire1 = wires.elementAt(i);
      PolyLine wire2 = wires.elementAt(j);

      wires.setElementAt(wire2, i);
      wires.setElementAt(wire1, j);

    }
  }

}

