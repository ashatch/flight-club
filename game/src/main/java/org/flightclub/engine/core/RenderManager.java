/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.IntStream;
import org.flightclub.engine.core.geometry.Object3d;
import org.flightclub.engine.math.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * manager for 3d objects
 */
public class RenderManager {
  private static final Logger LOG = LoggerFactory.getLogger(RenderManager.class);

  public static final int BACKGROUND_LAYER = 0;
  public static final int DEFAULT_LAYER = 1;

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

  private List<Renderable> renderableList = new ArrayList<>();

  public RenderManager() {
    IntStream.range(0, MAX_LAYERS)
        .forEach(i -> layers.add(new Vector<>()));
  }

  public void add(final Object3d obj) {
    layers.get(obj.getLayer()).add(obj);
  }

  public void remove(final Object3d obj) {
    layers.get(obj.getLayer()).remove(obj);
  }

  public void addRenderable(Renderable renderable) {
    if (!this.renderableList.contains(renderable)) {
      this.renderableList.add(renderable);
    } else {
      LOG.warn("Renderable item {} attempted to be added twice", renderable.getClass().getSimpleName());
    }
  }

  public Vector<Vector<Object3d>> sort() {
    layers.forEach(layer -> layer.sort(COMPARATOR));
    return layers;
  }

  public void render(final RenderContext context) {
    this.sort()
        .forEach(layer ->
            layer.forEach(obj -> obj.render(context))
        );

    this.renderableList.forEach(x -> x.render(context));
  }
}
