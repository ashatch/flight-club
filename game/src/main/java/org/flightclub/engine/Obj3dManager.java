/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Comparator;
import java.util.Vector;
import java.util.stream.IntStream;

/*
 * manager for 3d objects
 */
public class Obj3dManager {
  private static final int MAX_LAYERS = 3;

  private static final Comparator<Object3d> COMPARATOR = (o1, o2) -> {
    /* furthest away obj is first in list */
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

  /*
      13/10/2001 - add layers (cf photoshop)...
          0 - at the back
          1 - default layer (used if none specified when adding an object3d)
  */
  final Vector<Vector<Object3d>> layers = new Vector<>(MAX_LAYERS);

  public Obj3dManager() {
    IntStream.range(0, MAX_LAYERS)
        .forEach(i -> layers.add(new Vector<>()));
  }

  public void add(final Object3d obj) {
    layers.get(obj.getLayer()).add(obj);
  }

  public void remove(final Object3d obj) {
    layers.get(obj.getLayer()).remove(obj);
  }

  public Vector<Vector<Object3d>> sort() {
    layers.forEach(layer -> layer.sort(COMPARATOR));
    return layers;
  }
}
