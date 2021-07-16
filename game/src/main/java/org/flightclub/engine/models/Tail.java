/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.models;

import java.util.Vector;
import org.flightclub.engine.XcGame;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.geometry.Object3d;
import org.joml.Vector3f;

/*
 * a tail of length n may be attached to a flying dot
 */
public class Tail extends Object3d {
  final int length;
  final Color color;
  private Vector3f[] tail;
  public int wireEvery = 4;    //default add a wire for every 5 points

  public Tail(XcGame theApp, int length, Color color, int layer) {
    super(layer);
    theApp.renderManager.add(this);
    this.length = length;
    this.color = color;
  }

  public void init(Vector3f p) {
    tail = new Vector3f[length];

    for (int i = 0; i < length; i++) {
      tail[i] = new Vector3f(p.x, p.y - (float) i / 1000, p.z);
    }

    Vector<Vector3f> tailWire = new Vector<>();
    int j = 0;
    for (int i = wireEvery; i < length; i++) {
      if (j < 2) {
        tailWire.addElement(tail[i]);
      }
      j++;

      if (j == wireEvery + 1) {
        super.addWire(tailWire, color, false);
        tailWire = new Vector<>();
        tailWire.addElement(tail[i]);
        j = 1;
      }
    }
  }

  public void moveTo(Vector3f newP) {
    //newP is the current position
    for (int i = 0; i < length - 1; i++) {
      int j = length - 1 - i;
      tail[j].set(tail[j - 1]);
    }
    tail[0].set(newP);
  }

  /*
   * move entire tail to newP (e.g. after glider
   * has landed and we move it to a new position
   * to resume play
   */
  public void reset(Vector3f newP) {
    for (int i = 0; i < length - 1; i++) {
      tail[i].set(newP);
    }
  }
}
