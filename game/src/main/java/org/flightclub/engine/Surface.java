/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import org.flightclub.engine.camera.CameraMan;
import org.flightclub.engine.math.Vector3d;

public class Surface extends PolyLine {

  final int[] xs;
  final int[] ys;

  public Surface(Object3d object, int numPoints, Color color) {
    super(object, numPoints, color);

    isSolid = true;

    xs = new int[numPoints];
    ys = new int[numPoints];
  }

  @Override
  public void draw(Graphics g, CameraMan cameraMan) {
    boolean inFieldOfView = true;

    if (numPoints <= 1) {
      return;
    }

    g.setColor(getColor(cameraMan));

    for (int i = 0; i < numPoints; i++) {
      Vector3d a = object3d.cameraSpacePoints.elementAt(points[i]);
      xs[i] = (int) (a.posY);
      ys[i] = (int) (a.posZ);

      inFieldOfView = inFieldOfView && object3d.flagsInFieldOfView.elementAt(points[i]);
    }

    if (inFieldOfView) {
      g.fillPolygon(xs, ys, xs.length);
    }
  }

}
