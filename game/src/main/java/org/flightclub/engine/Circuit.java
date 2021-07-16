/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import org.joml.Vector3f;

/**
 * A list of points to fly round when ridge soaring.
 * Usually two points, unless ridge is snakey.
 * NB We use the hill's local coord system
 */
public class Circuit {
  private final Hill hill;
  private final Vector3f[] points;
  private int numPoints = 0;
  private int next = 0;
  private final Vector3f fallLine;

  public Circuit(
      final Hill hill,
      final Vector3f fallLine,
      final int n
  ) {
    this.hill = hill;
    points = new Vector3f[n];
    this.fallLine = fallLine;
  }

  public Vector3f getFallLine() {
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

  void add(final Vector3f inP) {
    // hills build circuits using their local coords
    points[numPoints] = inP;
    numPoints++;
  }

  Vector3f next() {
    Vector3f p = points[next];
    next++;
    if (next > numPoints - 1) {
      next = 0;
    }

    //transform from local to global coords
    Vector3f q = new Vector3f();
    q.x = p.x + hill.x0;
    q.y = p.y + hill.y0;

    return q;
  }
}
