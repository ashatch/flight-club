/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class ObjectLayer extends Vector<Object3d> {

  /* sort each layer so furthest away obj is first in list */
  public void sort() {
    Collections.sort(this, COMPARATOR);
  }

  public static final Comparator<Object3d> COMPARATOR = (o1, o2) -> {
    if (o1.cameraSpacePoints.size() == 0 || o2.cameraSpacePoints.size() == 0) {
      return 0;
    }

    final Vector3d p1 = o1.cameraSpacePoints.get(0);
    final Vector3d p2 = o2.cameraSpacePoints.get(0);

    if (p1.posX > p2.posX) {
      return 1;
    } else if (p1.posX < p2.posX) {
      return -1;
    } else {
      return 0;
    }
  };
}
