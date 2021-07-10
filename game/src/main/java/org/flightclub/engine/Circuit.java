/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import org.flightclub.engine.math.Vector3d;

/**
 * A list of points to fly round when ridge soaring.
 * Usually two points, unless ridge is snakey.
 * NB We use the hill's local coord system
 */
public class Circuit {
  private final Hill hill;
  private final Vector3d[] points;
  private int numPoints = 0;
  private int next = 0;
  private final Vector3d fallLine;

  public Circuit(
      final Hill hill,
      final Vector3d fallLine,
      final int n
  ) {
    this.hill = hill;
    points = new Vector3d[n];
    this.fallLine = fallLine;
  }

  public Vector3d getFallLine() {
    return fallLine;
  }

  int turnDir() {
    if (next == 0) {
      return MovementManager.RIGHT;
    } else if (next == 1) {
      return MovementManager.LEFT;
    } else {
      return 0;
    }
  }

  void add(Vector3d inP) {
    // hills build circuits using their local coords
    points[numPoints] = inP;
    numPoints++;
  }

  Vector3d next() {
    Vector3d p = points[next];
    next++;
    if (next > numPoints - 1) {
      next = 0;
    }

    //transform from local to global coords
    Vector3d q = new Vector3d();
    q.posX = p.posX + hill.x0;
    q.posY = p.posY + hill.y0;

    return q;
  }
}
