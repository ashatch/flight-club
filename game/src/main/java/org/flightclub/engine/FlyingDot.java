/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

/*
  a dot with a position and velocity. also...
  - a local coord system (v points along +y, z has roll effect)
  - turn radius to use when circling. halve this for ridge sooaring.
  - factory method for a tail
  - ds (horizontal distance moved per tick)
*/

import org.flightclub.engine.camera.CameraSubject;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.models.Tail;
import org.joml.Vector3f;

import static org.flightclub.engine.core.RenderManager.DEFAULT_LAYER;

public class FlyingDot implements UpdatableGameObject, CameraSubject {
  final XcGame app;
  protected final Sky sky;
  public Vector3f vector;
  public Vector3f vectorP = new Vector3f();
  float speed;
  float ds; //distance per frame - hack
  final float myTurnRadius;

  boolean isUser = false; //TODO - tidy this hack ! classname operator ?

  final Vector3f axisX = new Vector3f();
  final Vector3f axisY = new Vector3f();
  final Vector3f axisZ = new Vector3f();

  Tail tail = null;
  public MovementManager moveManager = null;

  public static final int TAIL_LENGTH = 25;
  public static final Color TAIL_COLOR = Color.LIGHT_GRAY;

  int roll = 0;
  static final int ROLL_STEPS = 15;
  static final float ROLL_MAX_ANGLE = (float) (Math.PI / 4);
  static final Vector3f[] AXIS_ZS = new Vector3f[ROLL_STEPS * 2 + 1];

  /*
   * generate an array of unit 'up' vectors for
   * different angles of bank (v points along the y axis)
   */
  static {
    for (int i = -ROLL_STEPS; i < ROLL_STEPS + 1; i++) {
      double theta = ((double) i / (double) ROLL_STEPS) * ROLL_MAX_ANGLE;
      Vector3f axisZ = new Vector3f();

      axisZ.x = (float) Math.sin(theta);
      axisZ.z = (float) Math.cos(theta);

      AXIS_ZS[i + ROLL_STEPS] = axisZ;
    }
  }

  public FlyingDot(
      final XcGame theApp,
      final Sky sky,
      final float inSpeed,
      final float inTurnRadius
  ) {
    app = theApp;
    this.sky = sky;
    app.addGameObject(this);

    speed = inSpeed;
    ds = speed * app.timePerFrame;
    vector = new Vector3f(0, ds, 0);
    myTurnRadius = inTurnRadius;
  }

  public FlyingDot(
      final XcGame theApp,
      final Sky sky,
      final float inSpeed,
      final float inTurnRadius,
      final boolean inIsUser
  ) {
    this(theApp, sky, inSpeed, inTurnRadius);
    isUser = inIsUser;
  }

  public void init(Vector3f inP) {
    vectorP = new Vector3f(inP);
    moveManager = new MovementManager(app, this);
    setLocalFrame();
    createTail();
  }

  protected void createTail() {
    tail = new Tail(app, TAIL_LENGTH, TAIL_COLOR, DEFAULT_LAYER);
    tail.init(vectorP);
  }

  void setSpeed(float s) {
    //call eg if user moved to faster point on the polar
    speed = s;
    ds = speed * app.timePerFrame;
  }

  /*
   * update position, velocity and local frame
   */
  @Override
  public void update(final UpdateContext context) {
    vectorP.add(vector);
    vectorP.y += this.sky.getWind() * app.timePerFrame;

    //hack - may have changed game speed (otherwise ds is constant)
    ds = speed * app.timePerFrame;

    // if circle point or target point...
    makeTurn(moveManager.nextMove());

    //otherwise...
    //makeTurn(0);

    avoidHills();

    sink();
    setLocalFrame();
    if (tail != null) {
      tail.moveTo(vectorP);
    }
  }

  protected void sink() {
    // overrider this method for different flying machines
    vector.z = 0;
  }

  /*
   * Set i, j and k vectors so v is along the y axis - ie do pitch and yaw
   */
  private void setLocalFrame() {
    axisX.set(vector).cross(new Vector3f(0, 0, 1)).normalize();
    axisY.set(vector).normalize();
    axisZ.set(axisX).cross(axisY);

    //now apply roll, if any
    if (roll == 0) {
      return;
    }

    Vector3f up = AXIS_ZS[roll + ROLL_STEPS];

    Vector3f axisX0 = new Vector3f(axisX);
    Vector3f axisZ0 = new Vector3f(axisZ);

    Vector3f dx = new Vector3f(axisX0).mul(up.x);
    Vector3f dz = new Vector3f(axisZ0).mul(up.z);

    axisZ.set(dx).add(dz);

    dx.set(axisX0).mul(up.z);
    dz.set(axisZ0).mul(-up.x);

    axisX.set(dx).add(dz);
  }

  void roll(final float dir) {
    if (dir != 0) {
      //turning
      roll += dir;
      if (roll > ROLL_STEPS) {
        roll = ROLL_STEPS;
      }
      if (roll < -ROLL_STEPS) {
        roll = -ROLL_STEPS;
      }
    } else {
      //roll level
      if (roll > 1) {
        roll--;
      } else if (roll < -1) {
        roll++;
      } else {
        roll = 0;
      }
    }
  }

  @Override
  public Vector3f getFocus() {
    // mid height and 'ahead'
    return new Vector3f(vectorP.x, vectorP.y + 1, 1);
  }

  @Override
  public Vector3f getEye() {
    float x = (vectorP.x > 0) ? (vectorP.x + 2) : (vectorP.x - 2);
    return new Vector3f(x, vectorP.y - 2, (float) 0.8);
  }

  /*
   * turn to the left or right.
   * for moving in a circle dv is always normal
   * to v and dv = v * v/r. take cross product
   * with unit vertical & scale by v/r.
   *
   * @param dir
   * > 0 turn right, < 0 turn left,
   * 1 - my turn radius
   * 2 - halve that etc.
   */
  void makeTurn(final float dir) {
    vector.z = 0;    //work in xy plane
    Vector3f w = new Vector3f(0, 0, 1).cross(vector).mul(-dir * ds / myTurnRadius);
    vector.add(w).normalize(ds); //ds is in xy only
    roll(dir);
  }

  /*
   * look at ground clearence both now and at next proposed point
   */
  void avoidHills() {
    if (moveManager.joinedCircuit()) {
      return;
    }
    if (app.landscape == null) {
      return;
    }

    Vector3f vectorPrime = vectorP.add(vector);

    float height = vectorP.z - app.landscape.getHeight(vectorP.x, vectorP.y);
    float heightPrime = vectorP.z - app.landscape.getHeight(vectorPrime.x, vectorPrime.y);
    float deltaHeight = heightPrime - height;

    if (height < 0) {
      return; //too late !
    }

    if (deltaHeight < 0 && height < myTurnRadius) {
      //float ONE_WING = (float) 0.2;
      //float r = (h - ONE_WING) * (ds/dh) * (ds/dh);

      // turn left or right ? see if moving right a bit gives a greater h than straight on
      Vector3f w = new Vector3f(vector).cross(new Vector3f(0, 0, 1)).mul(ds / myTurnRadius);
      Vector3f vectorPrimePlusW = vectorPrime.add(w);
      float heightAfterCalculation = vectorP.z - app.landscape.getHeight(
          vectorPrimePlusW.x, vectorPrimePlusW.y);
      if (heightAfterCalculation >= heightPrime) {
        makeTurn(1); //turn right
      } else {
        makeTurn(-1); //turn left
      }
    }
  }
}
