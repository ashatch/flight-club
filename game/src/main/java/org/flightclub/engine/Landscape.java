/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;
import org.flightclub.engine.camera.CameraSubject;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.geometry.Object3d;
import org.flightclub.engine.core.geometry.Tools3d;
import org.flightclub.engine.math.Vector3d;

import static org.flightclub.engine.core.RenderManager.BACKGROUND_LAYER;

/*
 * Some hills, triggers and a road arranged onto tiles
 */
public class Landscape implements CameraSubject {
  // hills in order heading downwind
  final Tile[] tiles;
  static XcGame app;
  private final Sky sky;
  int currentTile;
  // cycle through the different hill shapes
  public static final int TILE_WIDTH = 20;
  public static final int MAX_TILES = 10;

  public Landscape(XcGame theApp, Sky sky) {
    app = theApp;
    this.sky = sky;
    tiles = new Tile[MAX_TILES];
    for (int t = 0; t < MAX_TILES; t++) {
      tiles[t] = new Tile();
    }

    currentTile = 0;
    loadTile(currentTile);
    road();
    goalLine();
  }

  /*
   * load a chunk of landscape mostly thermal triggers, some tiles have hills
   */
  void loadTile(int t) {
    if (tiles[t].loaded) {
      return;
    }

    tiles[t].hills.clear();
    tiles[t].triggers.clear();

    if (t == 0) {
      loadFlatLand(t);
      //loadHill_1(t);
    } else if (t == MAX_TILES - 1) {
      loadFinalTile(t);
    } else if (t == 1) {
      loadFlatLand(t);
    } else if (t == 2) {
      loadHill_2(t);
    } else if (t == 3) {
      loadStorm(t);
    } else if (t == 4) {
      loadHill_1(t);
    } else if (t == 5) {
      loadFlatLand(t);
    } else if (t == 6) {
      loadMountain_1(t);
    } else if (t == 7) {
      loadBlueHole(t);
    } else if (t == 8) {
      loadMountain_2(t);
    } else {
      loadFlatLand(t);
    }

    tiles[t].loaded = true;
  }

  void addFrame(int tile, int x) {
    //Vector wire;

    //int y0 = tile * TILE_WIDTH;
    //int x0 = - TILE_WIDTH/2 + x * TILE_WIDTH;

    //two cross hairs
    //crossHair(x0, y0);
    //crossHair(x0 + TILE_WIDTH, y0);
  }

  /*
   * add a cross hair (show your working + LED !)
   * - use for tile corners and triggers
   */
  static void crossHair(float x, float y) {
    float hair = 1;

    Vector<Vector3d> wire;
    wire = new Vector<>();
    wire.addElement(new Vector3d(x, y - hair, 0));
    wire.addElement(new Vector3d(x, y + hair, 0));

    Object3d o = new Object3d(BACKGROUND_LAYER);    //layer zero !!
    app.renderManager.add(o);
    o.addWire(wire, new Color(230, 230, 230), false, false);

    wire = new Vector<>();
    wire.addElement(new Vector3d(x - hair, y, 0));
    wire.addElement(new Vector3d(x + hair, y, 0));
    o.addWire(wire, new Color(230, 230, 230), false, false);

  }

  /*
   * hack a long straight pink road - refine later - ha
   */
  static void road() {
    float atom = 2;

    Object3d o = new Object3d(BACKGROUND_LAYER);
    app.renderManager.add(o);
    Vector<Vector3d> wire;

    for (int i = 0; i < MAX_TILES * TILE_WIDTH; i += atom * 1) {
      wire = new Vector<>();

      float x1 = (float) Math.sin((double) i * Math.PI / TILE_WIDTH);
      float x2 = (float) Math.sin((double) (i + atom) * Math.PI / TILE_WIDTH);
      wire.addElement(new Vector3d(x1 * 1, i, 0));
      wire.addElement(new Vector3d(x2 * 1, i + atom, 0));
      o.addWire(wire, new Color(220, 220, 220), false, false);
    }
  }

  /*
   * finish line at start of last tile
   */
  static void goalLine() {
    float y0 = TILE_WIDTH * MAX_TILES;

    Vector<Vector3d> wire;
    float x1 = (float) -TILE_WIDTH / 8;
    float x2 = (float) TILE_WIDTH / 8;

    wire = new Vector<>();
    wire.addElement(new Vector3d(x1, y0, 0));
    wire.addElement(new Vector3d(x2, y0, 0));

    Object3d o = new Object3d(BACKGROUND_LAYER);    //layer zero !!
    app.renderManager.add(o);
    o.addWire(wire, new Color(220, 220, 100), false, false);

    wire = new Vector<>();
    wire.addElement(new Vector3d(x1, 0, 0));
    wire.addElement(new Vector3d(x2, 0, 0));
    o.addWire(wire, new Color(220, 220, 100), false, false);
  }

  /*
   * one cloud before goal line and one after
   */
  void loadFinalTile(int tile) {
    addFrame(tile, 0);
    ThermalTrigger trigger;
    int y0 = tile * TILE_WIDTH;
    int x0 = 0;

    trigger = new ThermalTrigger(app, this.sky, x0, y0 + 3, 2, 1, 1);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0, y0 + 15, 2, 1, 1);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0, y0 + TILE_WIDTH + 2, 1, (float) 0.2, (float) 0.5);
    tiles[tile].triggers.addElement(trigger);
  }

  /*
   * bruce's hexagon of thermal triggers
   */
  void loadFlatLand(int tile) {
    addFrame(tile, 0);
    ThermalTrigger trigger;
    int y0 = tile * TILE_WIDTH;
    int x0 = 0;

    trigger = new ThermalTrigger(app, this.sky, x0, y0 + 3, 2, 1, 1);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0, y0 + 15, 1, 1, (float) 0.5);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0 + 5, y0 + 5, 1, 1, 1);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0 + 5, y0 + 13, 2, (float) 0.5, (float) 0.5);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0 - 5, y0 + 5, 1, 1, 1);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0 - 5, y0 + 13, 1, 1, 1);
    tiles[tile].triggers.addElement(trigger);

    loadBackTriggers(tile);
  }

  /*
   * load some cloud sources off track just for looks
   */
  void loadBackTriggers(int tile) {
    int y0 = tile * TILE_WIDTH;
    int x0 = (int) Tools3d.rnd(2, -2);
    ThermalTrigger trigger;
    int dx = TILE_WIDTH / 2 + 3;

    trigger = new ThermalTrigger(app, this.sky, x0 - dx, y0 + 3, 2, 1, 1);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0 + dx, y0 + 15, 2, 1, 1);
    tiles[tile].triggers.addElement(trigger);
  }

  void loadHill_1(int tile) {

    int y0 = tile * TILE_WIDTH;
    Hill hill;

    addFrame(tile, 0);

    hill = new Hill(
        app,
        2,
        y0 + TILE_WIDTH / 4,
        Hill.Orientation.X,
        2,
        (float) 0.3,
        (float) 0.5,
        Hill.FACE_CURVY);

    tiles[tile].hills.addElement(hill);

    //distant hills
    hill = new Hill(app,
        -TILE_WIDTH / 2 - 4,
        y0 + TILE_WIDTH * 3 / 4 + 3,
        Hill.Orientation.Y,
        6,
        1,
        (float) 1,
        Hill.FACE_SPIKEY);
    tiles[tile].hills.addElement(hill);

    hill = new Hill(app,
        +TILE_WIDTH / 2 + 6,
        y0 + TILE_WIDTH * 3 / 4,
        Hill.Orientation.Y,
        3,
        0,
        (float) 0.5,
        Hill.FACE_SPIKEY);

    tiles[tile].hills.addElement(hill);

    int x0 = 0;
    //triggers
    ThermalTrigger trigger = new ThermalTrigger(app, this.sky, x0, y0 + 15, 1, 1, (float) 0.5);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, 5, y0 + 5, 1, (float) 0.1, (float) 0.1);
    tiles[tile].triggers.addElement(trigger);

    loadBackTriggers(tile);
  }

  void loadHill_2(int tile) {

    int y0 = tile * TILE_WIDTH;
    Hill hill;

    hill = new Hill(app,
        -TILE_WIDTH / 4,
        y0 + TILE_WIDTH / 2,
        Hill.Orientation.X,
        4,
        2,
        (float) 1,
        Hill.FACE_CURVY);
    tiles[tile].hills.addElement(hill);

    //background
    hill = new Hill(app,
        TILE_WIDTH / 2 + 4,
        y0 + TILE_WIDTH * 3 / 4 + 3,
        Hill.Orientation.Y,
        6,
        1,
        (float) 1,
        Hill.FACE_SPIKEY);

    tiles[tile].hills.addElement(hill);

    hill = new Hill(app,
        -TILE_WIDTH / 2 - 6,
        y0 + TILE_WIDTH * 3 / 4 - 3,
        Hill.Orientation.Y,
        3,
        0,
        (float) 0.5,
        Hill.FACE_SPIKEY);

    tiles[tile].hills.addElement(hill);

    int x0 = 0;
    //triggers
    ThermalTrigger trigger = new ThermalTrigger(app, this.sky, x0, y0 + 10, 1, (float) 0.2, (float) 0.1);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0 - 4, y0 + 10, 1, (float) 0.2, (float) 0.1);
    tiles[tile].triggers.addElement(trigger);

    loadBackTriggers(tile);
  }

  /*
   * one trigger with short cycle
   */
  void loadStorm(int tile) {
    ThermalTrigger trigger;
    int y0 = tile * TILE_WIDTH;

    //trigger with half the cycle length and 3 times the cloud duration
    trigger = new ThermalTrigger(app, this.sky, -4, y0 + TILE_WIDTH / 4, 2, (float) 0.3, 3);
    tiles[tile].triggers.addElement(trigger);
  }

  /*
   * 1d - no triggers - just tumble weed !
   * well, just one slow one
   */
  void loadBlueHole(int tile) {
    ThermalTrigger trigger;
    int y0 = tile * TILE_WIDTH;

    trigger = new ThermalTrigger(app, this.sky, 0, y0 + TILE_WIDTH / 2, 1, 2, 1);
    tiles[tile].triggers.addElement(trigger);
  }

  void loadMountain_1(int tile) {

    int y0 = tile * TILE_WIDTH;
    Hill hill;

    hill = new Hill(app,
        2,
        y0 + TILE_WIDTH / 2,
        Hill.Orientation.X,
        3,
        3,
        (float) 1.1,
        Hill.FACE_CURVY);

    tiles[tile].hills.addElement(hill);

    //distant hills
    hill = new Hill(app,
        -TILE_WIDTH / 2,
        y0,
        Hill.Orientation.Y,
        6,
        1,
        (float) 1,
        Hill.FACE_SPIKEY);

    tiles[tile].hills.addElement(hill);

    hill = new Hill(app,
        +TILE_WIDTH / 2 + 6,
        y0 + TILE_WIDTH * 3 / 4,
        Hill.Orientation.Y,
        3,
        0,
        (float) 0.5,
        Hill.FACE_SPIKEY);

    tiles[tile].hills.addElement(hill);

    int x0 = 0;
    //triggers
    ThermalTrigger trigger = new ThermalTrigger(app, this.sky, x0, y0 + 15, 1, 1, (float) 0.5);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0 + 4, y0 + 10, 1, (float) 0.1, (float) 0.1);
    tiles[tile].triggers.addElement(trigger);

    loadBackTriggers(tile);

  }

  void loadMountain_2(int tile) {

    int y0 = tile * TILE_WIDTH;
    Hill hill;

    hill = new Hill(app,
        -7,
        y0 + TILE_WIDTH / 2,
        Hill.Orientation.X,
        4,
        2,
        (float) 1.5,
        Hill.FACE_CURVY);

    tiles[tile].hills.addElement(hill);

    //distant hills
    hill = new Hill(app,
        -TILE_WIDTH / 2,
        y0 + TILE_WIDTH * 3 / 4,
        Hill.Orientation.Y,
        4,
        1,
        (float) 1,
        Hill.FACE_SPIKEY);

    tiles[tile].hills.addElement(hill);

    hill = new Hill(app,
        +TILE_WIDTH / 2 + 2,
        y0,
        Hill.Orientation.Y,
        3,
        0,
        (float) 0.5,
        Hill.FACE_SPIKEY);

    tiles[tile].hills.addElement(hill);

    int x0 = 0;
    //triggers
    ThermalTrigger trigger = new ThermalTrigger(app, this.sky, x0 - 6, y0 + 10, 1, (float) 0.2, (float) 0.1);
    tiles[tile].triggers.addElement(trigger);

    trigger = new ThermalTrigger(app, this.sky, x0 - 2, y0 + 10, 1, (float) 0.2, (float) 0.1);
    tiles[tile].triggers.addElement(trigger);
  }

  /*
   * load tile user is over
   * and the next tile downwind
   * unload the upwind tile
   */
  public void loadTilesAround(Vector3d p) {
    currentTile = getTile(p);
    if (currentTile < MAX_TILES) {
      loadTile(currentTile);
    }
    if (currentTile + 1 < MAX_TILES) {
      loadTile(currentTile + 1);
    }
    if (currentTile > 0) {
      removeTile(currentTile - 1);
    }
  }

  public boolean reachedGoal(Vector3d p) {
    return p.posY >= MAX_TILES * TILE_WIDTH;
  }

  void removeTile(int tileNum) {
    removeTile(tileNum, false);
  }

  void removeTile(int tileNum, boolean really) {
    if (!tiles[tileNum].loaded) {
      return;
    }

    for (Hill hill : tiles[tileNum].hills) {
      app.renderManager.remove(hill.object3d);
    }

    for (ThermalTrigger trigger : tiles[tileNum].triggers) {
      trigger.destroyMe(really);
    }

    tiles[tileNum].hills.clear();
    tiles[tileNum].triggers.clear();
    tiles[tileNum].loaded = false;
  }

  void removeAll() {

    for (int i = 0; i < MAX_TILES; i++) {
      //pass really flag so clouds disappear aswell
      removeTile(i, true);
    }
  }

  /*
   * return first hill downwind (+y) of p within glide
   */
  Hill nextHill(Vector3d p) {
    int tile = getTile(p);
    if (!tiles[tile].loaded) {
      return null;
    }

    float range = 8;

    for (Hill hill : tiles[tile].hills) {
      if (hill.y0 >= p.posY && hill.y0 - p.posY < range * p.posZ && hill.inForeGround) {
        return hill;
      }
    }

    //try next tile
    tile++;
    if (tile > MAX_TILES - 1) {
      return null;
    }

    if (!tiles[tile].loaded) {
      return null;
    }

    for (Hill hill : tiles[tile].hills) {
      if (hill.y0 >= p.posY && hill.y0 - p.posY < range * p.posZ && hill.inForeGround) {
        return hill;
      }
    }

    return null;
  }

  /*
   * which tile does this point fall in
   */
  int getTile(Vector3d p) {
    for (int i = 0; i < MAX_TILES; i++) {
      if ((i + 1) * TILE_WIDTH > p.posY)  {
        return i;
      }
    }
    return MAX_TILES - 1;
  }

  /*
   * find local hill to point, if any
   */
  public Hill getHillAt(Vector3d p) {
    int tile = getTile(p);
    if (!tiles[tile].loaded) {
      return null;
    }

    for (Hill hill : tiles[tile].hills) {
      if (hill.contains(p.posX, p.posY)) {
        return hill;
      }
    }

    return null;
  }

  public float getHeight(float x, float y) {
    Hill hill = getHillAt(new Vector3d(x, y, 0));
    return (hill == null) ? 0 : hill.getHeight(x, y);
  }

  @Override
  public Vector3d getFocus() {
    float centerY = currentTile * TILE_WIDTH + TILE_WIDTH / 2;
    return new Vector3d(0, centerY, 0);
  }

  /*
   * look in from near corner (at cloudbase)
   */
  @Override
  public Vector3d getEye() {
    float centerY = currentTile * TILE_WIDTH + TILE_WIDTH / 2;
    return new Vector3d(TILE_WIDTH / 2, centerY - TILE_WIDTH / 2, 2);
  }
}
