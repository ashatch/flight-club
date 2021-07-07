/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import java.util.Vector;

public class Cloud implements CameraSubject, UpdatableGameObject {
  final XcGame app;
  final Object3dWithShadow object3d;
  Vector3d projection = new Vector3d();
  float radius;
  final float maxRadius;
  final boolean solid = true;
  final Color color;
  final Vector3d[] corners = new Vector3d[8];
  final boolean inForeGround;
  ThermalTrigger trigger = null;

  boolean decaying = false;
  float age = 0;
  final int nose;
  int mature;
  final int tail;

  final double[] theta = new double[4];
  final double[] landa = new double[4];

  final float liftMax;
  final float myRnd;    //see getEye

  final float windSlope = (float) 0.5; // 0.1 //lean towards +y due to wind (1 equals 45 degrees)
  float ds;

  static final float LIFT_FN_OUTER = 1;
  static final float LIFT_FN_INNER = (float) 0.5;
  static final int CLOUD_COLOR = 230;
  static final int CLOUD_COLOR_STEP = 20; //how much darker are strong clouds

  public Cloud(XcGame inApp, float x, float y, int inDuration, int inStrength) {
    app = inApp;
    object3d = new Object3dWithShadow();
    inApp.obj3dManager.add(object3d);

    for (int i = 0; i < 8; i++) {
      corners[i] = new Vector3d();
    }

    //units of time - model minutes
    nose = 10;
    mature = inDuration;
    tail = 8;

    age = (float) 0.1; //small but non zero
    //ds = Sky.getWind()/app.getFrameRate();
    myRnd = (float) (Tools3d.rnd(0, 1)); //for camera angle
    projection = new Vector3d(x, y, Sky.getCloudBase());

    /*
     * cloud strength measured in multiples of glider (min) sink rate
     * e.g. 1 > climb at sink rate, 2 > climb at twice sink rate etc
     * also, stronger clouds are bigger and darker
     */
    liftMax = -(1 + inStrength) * Glider.SINK_RATE;
    int c = CLOUD_COLOR - (inStrength - 1) * CLOUD_COLOR_STEP;
    if (inStrength == 1) {
      color = new Color(c, c, c);
    } else {
      //darker, but keep some blue
      color = new Color(c, c, c);
    }

    maxRadius = inStrength;  // (float) Math.sqrt(inStrength); was 1

    setSphericals();
    setCorners();
    buildSurfaces();
    app.addGameObject(this);

    //add to lift profile ?
    inForeGround = (projection.posX < Landscape.TILE_WIDTH / 2
        && projection.posX > -Landscape.TILE_WIDTH / 2);

    if (inForeGround) {
      app.sky.addCloud(this);
    }
  }

  void destroyMe() {
    app.obj3dManager.remove(object3d);
    if (inForeGround) {
      app.sky.removeCloud(this);
    }
    app.removeGameObject(this);
    if (trigger != null) {
      trigger.clouds.removeElement(this);
    }
  }

  private void buildSurfaces() {
    Vector<Vector3d> wire;

    //front
    wire = new Vector<>();
    wire.addElement(corners[1]);
    wire.addElement(corners[0]);
    wire.addElement(corners[3]);
    wire.addElement(corners[2]);
    wire.addElement(corners[1]);
    object3d.addWire(wire, color, solid);

    //back
    wire = new Vector<>();
    wire.addElement(corners[4]);
    wire.addElement(corners[5]);
    wire.addElement(corners[6]);
    wire.addElement(corners[7]);
    wire.addElement(corners[4]);
    object3d.addWire(wire, color, solid);

    //bot
    wire = new Vector<>();
    wire.addElement(corners[0]);
    wire.addElement(corners[4]);
    wire.addElement(corners[7]);
    wire.addElement(corners[3]);
    wire.addElement(corners[0]);
    object3d.addWireWithShadow(wire, color, solid, true);

    //top - use a convex tile
    Vector3d[] topCorners = new Vector3d[4];

    topCorners[0] = corners[1];
    topCorners[1] = corners[2];
    topCorners[2] = corners[6];
    topCorners[3] = corners[5];

    object3d.addTile(topCorners, color, true, false);

    //right
    wire = new Vector<>();
    wire.addElement(corners[0]);
    wire.addElement(corners[1]);
    wire.addElement(corners[5]);
    wire.addElement(corners[4]);
    wire.addElement(corners[0]);
    object3d.addWire(wire, color, solid);

    //left
    wire = new Vector<>();
    wire.addElement(corners[2]);
    wire.addElement(corners[3]);
    wire.addElement(corners[7]);
    wire.addElement(corners[6]);
    wire.addElement(corners[2]);
    object3d.addWire(wire, color, solid);

  }

  public boolean isUnder(Vector3d inP) {
    // only compute lift if within bounding box

    //if (age > t_nose + t_mature) return false;

    if (inP.posX > projection.posX + LIFT_FN_OUTER) {
      return false;
    }
    if (inP.posX < projection.posX - LIFT_FN_OUTER) {
      return false;
    }
    if (inP.posY > getY(inP.posZ) + LIFT_FN_OUTER) {
      return false;
    }
    if (inP.posY < getY(inP.posZ) - LIFT_FN_OUTER) {
      return false;
    }

    return (getLift(inP) > 0);
  }

  @Override
  public void update(final UpdateContext context) {
    age += context.deltaTime() * app.timeMultiplier / 2.0f;
    if (age > mature + nose + tail * 0.5) {
      decaying = true;
    }

    if (age > mature + nose + tail) {
      destroyMe();
      return;
    }

    projection.posY += Sky.getWind() * context.deltaTime() * app.timeMultiplier / 2.0f;
    projection.posZ = Sky.getCloudBase();
    setCorners();
    object3d.updateShadow(app.landscape);
  }

  float getRadius() {
    /*
     * cloud radius is a function of it's age.
     * here's the model...
     *
     * volume of rising air, dv ~ constant.
     * decay ~ surface area.
     * dynamic equilibrium, dv = decay, at maturity.
     * dv = 0 at old age.
     */

    float fn;
    if (age <= nose) {
      fn = (float) Math.sqrt((double) age / nose);
    } else if (age > nose && age <= nose + mature) {
      fn = 1;
    } else if (age > mature + nose && age <= mature + nose + tail) {
      fn = (float) Math.sqrt(1 - (double) (age - mature - nose) / tail);
    } else {
      fn = 0;
    }
    return fn * maxRadius;
  }

  float getRadiusBase(float radius) {
    // make base decay quicker than top of cloud
    if (age > mature + nose) {
      float fn = (float) Math.sqrt(1 - (double) (age - mature - nose) / tail);
      fn = 2 * (fn - (float) 0.5);
      if (fn < 0.2) {
        fn = (float) 0.2; //0
      }
      return radius * fn;
    } else {
      return radius;
    }
  }

  void setCorners() {
    Vector3d v = new Vector3d();
    float radius = getRadius();
    float radiusBase = getRadiusBase(radius);

    //front face
    sphToXxy(radiusBase, theta[0], 0.0, v);
    corners[0].set(v).add(projection);

    sphToXxy(radiusBase, theta[3], 0, v);
    corners[3].set(v).add(projection);

    sphToXxy(radius, theta[3], landa[3], v);
    corners[2].set(v).add(projection);

    sphToXxy(radius, theta[0], landa[0], v);
    corners[1].set(v).add(projection);

    //back face
    sphToXxy(radiusBase, theta[1], 0.0, v);
    corners[4].set(v).add(projection);

    sphToXxy(radius, theta[1], landa[1], v);
    corners[5].set(v).add(projection);

    sphToXxy(radius, theta[2], landa[2], v);
    corners[6].set(v).add(projection);

    sphToXxy(radiusBase, theta[2], 0.0, v);
    corners[7].set(v).add(projection);
  }

  void setSphericals() {
    double lower;
    double upper;

    //thetas
    for (int quad = 0; quad < 4; quad++) {
      lower = quad * 90.0;
      upper = lower + 80.0;
      theta[quad] = Tools3d.rnd(lower, upper);
    }

    //landas
    for (int quad = 0; quad < 4; quad++) {
      landa[quad] = Tools3d.rnd(20.0, 50.0); //70
    }
  }

  void sphToXxy(float r, double a, double b, Vector3d v) {
    //convert degrees to radians, a - theta, b -landa
    a *= (Math.PI / 180);
    b *= (Math.PI / 180);

    v.posX = (float) (r * Math.cos(b) * Math.cos(a));
    v.posY = (float) (r * Math.cos(b) * Math.sin(a));
    v.posZ = (float) (r * Math.sin(b));

    //wind slope - wind blows cloud tops downwind
    v.posY += windSlope * v.posZ;
  }

  @Override
  public Vector3d getFocus() {
    return new Vector3d(projection.posX, projection.posY + 2, (float) 1);
  }

  @Override
  public Vector3d getEye() {
    int dx;
    if (projection.posX > 0) {
      dx = 1;
    } else {
      dx = -1;
    }
    if (myRnd > 0.7) {
      return new Vector3d(projection.posX + 3 * dx, projection.posY - 3, (float) 0.1);
    } else if (myRnd > 0.3) {
      return new Vector3d(projection.posX + dx, projection.posY - 5, (float) 1.5);
    } else {
      return new Vector3d(projection.posX, projection.posY - (float) 2.5, (float) 1.2);
    }
  }

  float getX(float z) {
    //no cross wind
    return projection.posX;
  }

  float getY(float z) {
    float d = Sky.getCloudBase() - z;
    return projection.posY - d * windSlope;
  }

  float getLift(Vector3d inP) {
    // lift is a function of r (dist from thermal center)

    float dx = projection.posX - inP.posX;
    float dy = getY(inP.posZ) - inP.posY;
    float r = (float) Math.sqrt(dx * dx + dy * dy);
    float lift;

    if (inP.posZ > Sky.getCloudBase()) {
      return 0;
    }
    //if (age > t_nose + t_mature) return 1;

    if (r >= LIFT_FN_OUTER) {
      lift = 0;
    } else if (r > LIFT_FN_INNER) {
      lift = liftMax * (1 - (r - LIFT_FN_INNER) / (LIFT_FN_OUTER - LIFT_FN_INNER));
    } else {
      //we are in the core
      lift = liftMax;
    }

    return lift;
  }

}

