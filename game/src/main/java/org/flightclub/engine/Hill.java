/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import org.flightclub.engine.camera.CameraSubject;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.geometry.Object3d;
import org.joml.Vector3f;

import static org.flightclub.engine.core.RenderManager.DEFAULT_LAYER;

/*
 * a spine running parallel to x axis or y axis (orientation 0 or 1)
 * has a circuit for ridge soaring
 *
 * build surface from maths functions...
 * - taylor approx's of sin wave (cubic polynomial)
 */
public class Hill implements CameraSubject {
  //spine start point
  final float x0;
  final float y0;

  public enum Orientation {
    X,
    Y,
  }

  Orientation orientation = Orientation.X;
  final int spineLength;
  final float phase;
  final float h0;
  final int face;
  final XcGame app;
  final Object3d object3d;
  final Color color;

  //make smaller to increase curvy resolution
  final float tileWidth;

  int[] zorderedTiles;
  final boolean inForeGround;
  float maxH = 0;

  // width 1 - spiky
  static final int FACE_SPIKEY = 0;
  // width 2 - curvy
  static final int FACE_CURVY = 1;

  public Hill(
      final XcGame theApp,
      final int inX,
      final int inY,
      final Orientation inOr,
      final int inSpineLength,
      final float inPhase,
      final float inH0,
      final int inFace
  ) {
    app = theApp;
    x0 = inX;
    y0 = inY;
    orientation = inOr;
    spineLength = inSpineLength;
    phase = inPhase;
    h0 = inH0;
    face = inFace;
    object3d = new Object3d(DEFAULT_LAYER);
    app.renderManager.add(object3d);
    color = new Color(255, 255, 255);

    // higher resolution/sample rate if
    // on central tile
    if (x0 < Landscape.TILE_WIDTH / 2f
        && x0 > -Landscape.TILE_WIDTH / 2f) {
      tileWidth = (float) 0.5;
    } else {
      tileWidth = 1;
    }

    tileHill();

    inForeGround = (x0 < Landscape.TILE_WIDTH / 2f && x0 > -Landscape.TILE_WIDTH / 2f);
  }

  /*
   * default hill (curvy spine, curvy face)
   */
  public Hill(
      final XcGame theApp,
      final int inX,
      final int inY
  ) {
    this(theApp, inX, inY, Orientation.X, 2, 1, (float) 0.5, FACE_CURVY);
  }

  /*
   * run over hill adding tiles
   */
  void tileHill() {
    int numSlices = (int) ((spineLength + 2) / tileWidth);
    int numTiles;

    float frontFace;
    if (face == FACE_SPIKEY) {
      frontFace = 1;
    } else {
      frontFace = 2;
    }
    numTiles = numSlices * (1 + (int) (frontFace / tileWidth));
    zorderedTiles = new int[numTiles];

    for (float i = 0; i < spineLength + 2; i += tileWidth) {
      for (float j = -1 + tileWidth; j <= frontFace; j += tileWidth) {
        addTile(i, j);
      }
    }

    if (orientation == Orientation.Y) {
      object3d.reverse();
    }
  }

  /*
   * add a tile at x0 +i, y0 + j if OR_X else swap i and j
   */
  void addTile(final float i, final float j) {
    float x1;
    float x2;
    float y1;
    float y2;

    Vector3f[] corners = new Vector3f[4];

    if (orientation == Orientation.X) {
      x1 = x0 + i;
      x2 = x1 + tileWidth;
      y1 = y0 - j; //back face has j=-1 and y > y0
      y2 = y1 + tileWidth;
    } else {
      y1 = y0 + i;
      y2 = y1 + tileWidth;
      x1 = x0 + j - tileWidth; //back face has j=-1 and x < x0
      x2 = x1 + tileWidth;
    }

    //TODO make getZ transform to i/j coords
    if (orientation == Orientation.X) {
      corners[0] = new Vector3f(x1, y1, getZ(i, j));
      corners[1] = new Vector3f(x1, y2, getZ(i, j - tileWidth));
      corners[2] = new Vector3f(x2, y2, getZ(i + tileWidth, j - tileWidth));
      corners[3] = new Vector3f(x2, y1, getZ(i + tileWidth, j));
    } else {
      corners[0] = new Vector3f(x1, y1, getZ(i, j - tileWidth));
      corners[1] = new Vector3f(x1, y2, getZ(i + tileWidth, j - tileWidth));
      corners[2] = new Vector3f(x2, y2, getZ(i + tileWidth, j));
      corners[3] = new Vector3f(x2, y1, getZ(i, j));
    }

    object3d.addTile(corners, color, true, true);
    //object3d.addTile(corners, color, false, false);
  }

  /*
   * soaring circuit - treat x0,y0 as origin
   */
  Circuit getCircuit() {
    final Vector3f fallLine = orientation == Orientation.X
        //default fall line - climb one unit -> move north (+y) one unit
        ? new Vector3f(0, 1, 0)
        : new Vector3f(-1, 0, 0);

    final Circuit circuit = new Circuit(this, fallLine, 2);

    float frontFace;
    if (face == FACE_CURVY) {
      frontFace = 2;
    } else {
      frontFace = 1;
    }

    if (orientation == Orientation.X) {
      circuit.add(new Vector3f(1, -frontFace, 0));
      circuit.add(new Vector3f(1 + spineLength, -frontFace, 0));
    } else {
      circuit.add(new Vector3f(frontFace, 1, 0));
      circuit.add(new Vector3f(frontFace, 1 + spineLength, 0));
    }

    return circuit;
  }

  @Override
  public Vector3f getEye() {
    if (orientation == Orientation.X) {
      return new Vector3f(x0 + 2 + spineLength, y0 - 2 - spineLength, (float) 0.8);
    } else {
      return new Vector3f(x0 + 2 + spineLength, y0, (float) 0.8);
    }
  }

  @Override
  public Vector3f getFocus() {
    if (orientation == Orientation.X) {
      return new Vector3f(x0 + (2 + spineLength) / 2f, y0, h0 / 2);
    } else {
      return new Vector3f(x0, y0 + (2 + spineLength) / 2f, h0 / 2);
    }
  }

  boolean contains(final float inX, final float inY) {
    float frontFace;
    if (face == FACE_CURVY) {
      frontFace = 2;
    } else {
      frontFace = 1;
    }

    if (orientation == Orientation.X) {
      return (inY >= y0 - frontFace && inY <= y0 + 1
          && inX > x0 && inX < x0 + 2 + spineLength);
    } else {
      return (inX <= x0 + frontFace && inX >= x0 - 1
          && inY > y0 && inY < y0 + 2 + spineLength);
    }
  }

  /*
   * return h at point i along spine and j away from spine
   *
   * slice perp to spine gives f1...
   * f1 = 1+j, j < 0 backface
   * f1 = (1-j) * (1-j), j > 0 and spiky
   * f1 = sin , j > 0 and curvy
   * then, scale f1 by f2, h at this point on spine
   *
   */
  private float getZ(final float i, final float j) {
    float f1 = 1; //tmp
    float f2 = spineHeight(i);

    if (j < 0) {
      f1 = 1 + j;
    } else {
      if (face == FACE_CURVY) {
        f1 = sin(2 - j);
      } else {
        f1 = (1 - j) * (1 - j);
      }
    }

    float h = f1 * f2;
    if (h > maxH) {
      maxH = h;
    }
    return h;
  }

  /*
   * convert to local coords then call getZ
   */
  public float getHeight(final float x, final float y) {
    float i;
    float j;
    if (orientation == Orientation.X) {
      i = x - x0;
      j = y0 - y;
    } else {
      i = y - y0;
      j = x - x0;
    }
    return getZ(i, j);
  }

  /*
   * spine h a distance i along it
   */
  float spineHeight(final float i) {
    if (i < 0 || i > spineLength + 2) {
      return 0;
    }

    if (i <= 1) {
      return i * i * h0;
    }

    if (i > 1 + spineLength) {
      float ii = spineLength + 2 - i;

      //beware - recursion would be infinite if
      //'>' changed to '>=' above
      float h1 = spineHeight(1 + spineLength);
      return ii * ii * h1;
    }

    return h0 + sin(i - 1 + phase) - sin(phase);
  }

  /*
   * cubic approx to a sin wave with
   * wave length 4, going between 0
   * and 1
   * 24/10 try halving amplitude
   */
  float sin(float x) {
    while (x >= 4) {
      x -= 4;
    }
    while (x < 0) {
      x += 4;
    }

    float xx;
    if (x <= 2) {
      xx = x / 2;
    } else {
      xx = 2 - x / 2;
    }
    return 3 * xx * xx - 2 * xx * xx * xx;
  }

  /*
   * lift twice sink rate close to hill, falling to zero
   * as we get further away
   */
  float getLift(final Vector3f p) {
    float lmax = -3 * Glider.SINK_RATE;
    float dh = (float) 0.1;
    //if (p.z > maxH + (float) 0.2) return 0;
    if (p.y < y0) {
      float z = getHeight(p.x, p.y);
      float h = p.z - z;
      //System.out.println("ground: " + z);
      if (h < dh) {
        return lmax;
      } else if (h < (float) 1 + dh) {
        float f = (2 + dh - h) / 2;
        return (f * f * f) * lmax;
      } else {
        return 0;
      }
    } else {
      return 0;
    }
  }
}
