/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;

/*
 * manager for 3d objects
 */
public class Obj3dManager {
  static final int MAX_LAYERS = 3;

  /*
      13/10/2001 - add layers (cf photoshop)...
          0 - at the back
          1 - default layer (used if none specified when adding an object3d)
  */
  final Vector<ObjectLayer> layers = new Vector<>(MAX_LAYERS);

  public Obj3dManager() {
    for (int i = 0; i < MAX_LAYERS; i++) {
      layers.add(new ObjectLayer());
    }
  }

  public void add(final Object3d obj) {
    layers.get(obj.getLayer()).add(obj);
  }

  public void remove(final Object3d obj) {
    layers.get(obj.getLayer()).remove(obj);
  }

  /* sort each layer so furthest away obj is first in list */
  public Vector<ObjectLayer> sort() {
    layers.forEach(ObjectLayer::sort);
    return layers;
  }
}
