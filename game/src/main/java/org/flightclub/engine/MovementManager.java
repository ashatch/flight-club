/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

/*
  Manage the motion of flying dots - thermalling, ridge soaring etc.
*/

import org.joml.Vector3f;

public class MovementManager {
  final XcGame app;
  FlyingDot flyingDot;

  // point to fly towards
  private Vector3f targetPoint = null;

  // point to circle around
  private Vector3f circlePoint = null;

  // cloud to thermal
  Cloud cloud = null;

  // list of points to fly round
  private Circuit circuit = null;
  private Vector3f circuitPoint = null;

  int nextMoveUser = 0;
  boolean joinedCircuit = false;

  int wiggleCount = 0;
  final int wiggleSize = 5;

  public static final int LEFT = -1;
  public static final int STRAIGHT = 0;
  public static final int RIGHT = 1;

  public MovementManager(
      final XcGame theApp,
      final FlyingDot theFlyingDot
  ) {
    app = theApp;
    flyingDot = theFlyingDot;
  }

  float wiggle() {
    wiggleCount--;
    if (wiggleCount > wiggleSize * 3) {
      return -2;
    }
    if (wiggleCount > wiggleSize * 2) {
      return 2;
    }
    if (wiggleCount > wiggleSize) {
      return 2;
    }
    if (wiggleCount > 0) {
      return -2;
    }
    return 0;
  }

  /*
   * called by flyingdot each tick - turn left (-1)
   * right (+1) or straight on (0)
   */
  float nextMove() {
    if (wiggleCount > 0) {
      return wiggle();
    }

    if (nextMoveUser != 0) {
      return nextMoveUser;
    }

    if (targetPoint != null) {
      return headForTarget();
    }

    if (circuit != null) {
      return followCircuit();
    }

    if (cloud != null) {
      return thermal();
    }

    if (circlePoint != null) {
      return circleAroundPoint();
    }

    // otherwise fly straight on
    return 0;
  }

  void setCircuit(final Circuit inCircuit) {
    clearControllers();
    circuit = inCircuit;
    joinedCircuit = false;
    circuitPoint = circuit.next();
  }

  public void setTargetPoint(final Vector3f t) {
    clearControllers();
    targetPoint = new Vector3f(t.x, t.y, t.z);
  }

  void setCloud(final Cloud c) {
    clearControllers();
    cloud = c;
  }

  public void setNextMove(final int dir) {
    // user pressed key to turn
    // clear all controllers
    clearControllers();
    nextMoveUser = dir;
    app.cameraMan.cutSetup(flyingDot, flyingDot.isUser);
  }

  void clearControllers() {
    circlePoint = null;
    targetPoint = null;
    cloud = null;
    circuit = null;
    joinedCircuit = false;
  }

  boolean joinedCircuit() {
    return circuit != null && joinedCircuit;
  }

  float headForTarget() {
    return headTowards(targetPoint.x, targetPoint.y);
  }

  private float followCircuit() {

    float x = circuitPoint.x;
    float y = circuitPoint.y;

    // hack - the circuit should do this leaning calc!
    // use fall line to calc change due to height
    x += flyingDot.vectorP.z * circuit.getFallLine().x;
    y += flyingDot.vectorP.z * circuit.getFallLine().y;

    return headTowards(x, y);
  }

  private float headTowards(final float x, final float y) {
    /*
     * use cross product to see if we need
     * to turn left or right. return true if
     * we are 'at' the point.
     */
    Vector3f u = new Vector3f(x - flyingDot.vectorP.x, y - flyingDot.vectorP.y, 0);
    Vector3f v = new Vector3f(flyingDot.vector.x, flyingDot.vector.y, 0);
    float d = u.length();

    if (d < flyingDot.myTurnRadius / 4) {
      //we are there
      targetPoint = null;
      if (circuit != null) {
        reachedCircuitPoint();
      }
      return 0;
    }

    // are we flying ~ staight towards target ?
    float dot = u.dot(v) / (flyingDot.ds * d);
    if (dot > 0.99) {
      return 0;
    }

    Vector3f c = new Vector3f(v).cross(u);
    float sin = c.length() / (flyingDot.ds * d);
    float sin1 = flyingDot.ds / flyingDot.myTurnRadius;

    if (sin <= sin1 * 2 && sin >= -sin1 * 2) {
      /*
       * maintain ~ current heading (with
       * a bit of fine tuning to eliminate wobble)
       * eq1: ds = r * dtheta
       * eq2: dtheta ~ sin(dtheta) ( for small dtheta )
       */

      if (c.z > 0) {
        sin *= -1;    //left
      }
      //System.out.println("sin: " + sin);
      float r = flyingDot.ds / sin;
      //System.out.println("Fine tune MTR/r:" + flyingDot.my_turn_radius/r);

      //convert so that 1/2 my turn radius goes to 2
      return flyingDot.myTurnRadius / r;
    }

    if (c.z > 0) {
      if (circuit == null) {
        return -1;
      } else {
        return -2; //left
      }
    } else {
      if (circuit == null) {
        return 1;
      } else {
        return 2; //right
      }
    }
  }

  void reachedCircuitPoint() {
    nextMoveUser = circuit.turnDir();    //hack
    circuitPoint = circuit.next();
    joinedCircuit = true;
  }

  private float circleAroundPoint() {
    return circleAround(circlePoint.x, circlePoint.y);
  }

  private float circleAround(final float x, final float y) {
    /*
     * use cross product of v and r
     */
    Vector3f r = new Vector3f(flyingDot.vectorP.x - x, flyingDot.vectorP.y - y, 0);
    float d = r.length();

    //are we close ?
    if (d > flyingDot.myTurnRadius * 3) {
      return headTowards(x, y);
    }

    Vector3f cross = new Vector3f(r).cross(flyingDot.vector);

    float dperp = cross.length() / flyingDot.ds;
    float dot = r.dot(flyingDot.vector);

    if (cross.z >= 0) {
      //circling the right way
      if (dot > 0) {
        return -1;
      } else {
        if (dperp <= flyingDot.myTurnRadius) {
          return 0; //was 0 on 26th whem it worked
        } else {
          return -1;
        }
      }
    } else {

      //circling the wrong way
      if (d < flyingDot.myTurnRadius) {
        return -1;
      } else {
        if (dot > 0) {
          return -1;
        } else {
          return 1;
        }
      }
    }
  }

  public void workLift() {
    /*
     * this replaces togglelift - after hugh's helpful comments
     */

    if (cloud != null || circuit != null) {
      return;
    }

    Hill h = null;
    if (app.landscape != null) {
      h = app.landscape.getHillAt(flyingDot.vectorP);
    }

    if (h != null) {
      setCircuit(h.getCircuit());
      app.cameraMan.cutSetup(h, flyingDot.isUser);
      return;
    }

    Cloud c = null;
    if (app.sky != null) {
      c = app.sky.getCloudAt(flyingDot.vectorP);
    }

    if (c != null) {
      setCloud(c);
      app.cameraMan.cutSetup(cloud, flyingDot.isUser);
      return;
    }

    // no cloud or ridge - do a wiggle
    wiggleCount = wiggleSize * 4 + 1;
  }

  float thermal() {
    return circleAround(cloud.getX(), cloud.getY(flyingDot.vectorP.z));
  }
}
