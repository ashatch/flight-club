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

public class FlyingDot implements UpdatableGameObject, CameraSubject {
  final XcGame app;
  Vector3d vector;
  Vector3d vectorP = new Vector3d();
  float speed;
  float ds; //distance per frame - hack
  final float myTurnRadius;

  boolean isUser = false; //TODO - tidy this hack ! classname operator ?

  final Vector3d axisX = new Vector3d();
  final Vector3d axisY = new Vector3d();
  final Vector3d axisZ = new Vector3d();

  Tail tail = null;
  MovementManager moveManager = null;

  public static final int TAIL_LENGTH = 25;
  public static final Color TAIL_COLOR = Color.LIGHT_GRAY;

  int roll = 0;
  static final int ROLL_STEPS = 15;
  static final float ROLL_MAX_ANGLE = (float) (Math.PI / 4);
  static final Vector3d[] AXIS_ZS = new Vector3d[ROLL_STEPS * 2 + 1];

  /*
   * generate an array of unit 'up' vectors for
   * different angles of bank (v points along the y axis)
   */
  static {
    for (int i = -ROLL_STEPS; i < ROLL_STEPS + 1; i++) {
      double theta = ((double) i / (double) ROLL_STEPS) * ROLL_MAX_ANGLE;
      Vector3d axisZ = new Vector3d();

      axisZ.posX = (float) Math.sin(theta);
      axisZ.posZ = (float) Math.cos(theta);

      AXIS_ZS[i + ROLL_STEPS] = axisZ;
    }
  }

  public FlyingDot(XcGame theApp, float inSpeed, float inTurnRadius) {
    app = theApp;
    app.addObserver(this);

    speed = inSpeed;
    ds = speed * app.timePerFrame;
    vector = new Vector3d(0, ds, 0);
    myTurnRadius = inTurnRadius;
  }

  public FlyingDot(XcGame theApp, float inSpeed, float inTurnRadius, boolean inIsUser) {
    this(theApp, inSpeed, inTurnRadius);
    isUser = inIsUser;
  }

  public void init(Vector3d inP) {
    vectorP = new Vector3d(inP);
    moveManager = new MovementManager(app, this);
    setLocalFrame();
    createTail();
  }

  protected void createTail() {
    tail = new Tail(app, TAIL_LENGTH, TAIL_COLOR);
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
  public void update(float delta) {
    vectorP.add(vector);
    vectorP.posY += Sky.getWind() * app.timePerFrame;

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
    vector.posZ = 0;
  }

  /*
   * Set i, j and k vectors so v is along the y axis - ie do pitch and yaw
   */
  private void setLocalFrame() {
    axisX.set(vector).cross(new Vector3d(0, 0, 1)).makeUnit();
    axisY.set(vector).makeUnit();
    axisZ.set(axisX).cross(axisY);

    //now apply roll, if any
    if (roll == 0) {
      return;
    }

    Vector3d up = AXIS_ZS[roll + ROLL_STEPS];

    Vector3d axisX0 = new Vector3d(axisX);
    Vector3d axisZ0 = new Vector3d(axisZ);

    Vector3d dx = new Vector3d(axisX0).scaleBy(up.posX);
    Vector3d dz = new Vector3d(axisZ0).scaleBy(up.posZ);

    axisZ.set(dx).add(dz);

    dx.set(axisX0).scaleBy(up.posZ);
    dz.set(axisZ0).scaleBy(-up.posX);

    axisX.set(dx).add(dz);
  }

  void roll(float dir) {
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
  public Vector3d getFocus() {
    // mid height and 'ahead'
    return new Vector3d(vectorP.posX, vectorP.posY + 1, 1);
  }

  @Override
  public Vector3d getEye() {
    float x = (vectorP.posX > 0) ? (vectorP.posX + 2) : (vectorP.posX - 2);
    return new Vector3d(x, vectorP.posY - 2, (float) 0.8);
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
  void makeTurn(float dir) {
    vector.posZ = 0;    //work in xy plane
    Vector3d w = new Vector3d(0, 0, 1).cross(vector).scaleBy(-dir * ds / myTurnRadius);
    vector.add(w).scaleToLength(ds); //ds is in xy only
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

    Vector3d vectorPrime = vectorP.plus(vector);

    float height = vectorP.posZ - app.landscape.getHeight(vectorP.posX, vectorP.posY);
    float heightPrime = vectorP.posZ - app.landscape.getHeight(vectorPrime.posX, vectorPrime.posY);
    float deltaHeight = heightPrime - height;

    if (height < 0) {
      return; //too late !
    }

    if (deltaHeight < 0 && height < myTurnRadius) {
      //float ONE_WING = (float) 0.2;
      //float r = (h - ONE_WING) * (ds/dh) * (ds/dh);

      // turn left or right ? see if moving right a bit gives a greater h than straight on
      Vector3d w = vector.crossed(new Vector3d(0, 0, 1)).scaleBy(ds / myTurnRadius);
      Vector3d vectorPrimePlusW = vectorPrime.plus(w);
      float heightAfterCalculation = vectorP.posZ - app.landscape.getHeight(
          vectorPrimePlusW.posX, vectorPrimePlusW.posY);
      if (heightAfterCalculation >= heightPrime) {
        makeTurn(1); //turn right
      } else {
        makeTurn(-1); //turn left
      }
    }
  }
}
