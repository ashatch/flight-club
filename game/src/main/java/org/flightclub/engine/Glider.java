/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import org.flightclub.engine.camera.CameraMan;
import org.flightclub.engine.camera.CameraMode;
import org.flightclub.engine.camera.CameraSubject;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.models.GliderShape;
import org.flightclub.engine.models.Tail;
import org.joml.Vector3f;

import static org.flightclub.engine.core.RenderManager.DEFAULT_LAYER;

/*
 * a glider that sniffs out lift
 */
public class Glider extends FlyingBody {
  boolean landed = true;
  int tryLater = 0;

  // hack - only glider user should know about this
  public boolean demoMode = true;

  boolean reachedGoal;

  // where on the polar are we
  int polarIndex = 0;

  // 4 vars for hack to delay cuts
  boolean cutPending = false;
  int cutWhen;
  CameraSubject cutSubject = null;
  int cutCount = 0;

  // hack var for camera position - left or right depending
  int lastEyeX = 1;
  boolean triggerLoading = false;

  // units of dist (km) per time (minute)
  static final float SPEED = (float) 1;
  // i.e. glide angle of 8
  public static final float SINK_RATE = (float) (-1.0 / 8);
  static final float TURN_RADIUS = (float) 0.3;
  // polar curve
  static final float[][] POLAR = {{1, 1}, {(float) 1.5, (float) 2.1}};

  public static final int TAIL_LENGTH = 40;
  public static final Color TAIL_COLOR = Color.LIGHT_GRAY;

  protected Glider(
      final XcGame app,
      final Sky sky,
      final float speed,
      final float turnRadius,
      final Vector3f p,
      final boolean isUser,
      final GliderShape gliderShape
  ) {
    super(app, sky, speed, turnRadius, isUser);
    this.init(gliderShape, p);
    bodyHeight = GliderShape.HEIGHT;
    gotoNextLiftSource();
  }

  public static Glider regularNpcGlider(
      final XcGame app,
      final Sky sky
  ) {
    return new Glider(
        app,
        sky,
        SPEED,
        TURN_RADIUS,
        new Vector3f(),
        false,
        new GliderShape(Color.DEFAULT_GLIDER_COLOR)
    );
  }

  public static Glider rigidNpcGlider(
      final XcGame app,
      final Sky sky
  ) {
    return new Glider(
        app,
        sky,
        SPEED * (float) 1.5,
        TURN_RADIUS * (float) 1.2,
        new Vector3f(),
        false,
        new GliderShape(Color.PINK)
    );
  }

  public static Glider userGlider(
      final XcGame app,
      final Sky sky
  ) {
    return new Glider(
        app,
        sky,
        SPEED,
        TURN_RADIUS,
        new Vector3f(0, 0, 0),
        true,
        new GliderShape(Color.YELLOW)
    );
  }

  /*
   * fly downwind to next lift source
   * goto hill if there is one, otherwise goto a cloud
   * NB inform the cameraman of this event ??
   */
  void gotoNextLiftSource() {
    if (!demoMode) {
      return;
    }

    Hill hill = null;
    Cloud cloud = null;

    if (app.landscape != null) {
      hill = app.landscape.nextHill(new Vector3f(vectorP.x, vectorP.y + 2, vectorP.z));
    }

    if (app.sky != null) {
      cloud = app.sky.nextCloud(new Vector3f(vectorP.x, vectorP.y + 2, vectorP.z));
    }

    cutPending = false;

    if (hill != null) {
      this.moveManager.setCircuit(hill.getCircuit());

      cutPending = true;
      cutWhen = whenArrive(hill.x0, hill.y0) - CameraMan.CUT_LEN * 2; //??2
      cutSubject = hill;
      cutCount = 0;
      return;
    }

    // no hill - look at clouds
    if (cloud == null) {
      // backtrack upwind

      if (app.sky != null) {
        cloud = app.sky.prevCloud(new Vector3f(vectorP.x, vectorP.y - 2, vectorP.z));
      }

      if (cloud == null) {
        //try again later, fly downwind for now
        this.moveManager.setTargetPoint(new Vector3f(vectorP.x, vectorP.y + 8, vectorP.z));
        tryLater = 25;
        return;
      }
    }

    // glide to cloud
    this.moveManager.setCloud(cloud);

    cutPending = true;
    cutWhen = whenArrive(cloud.projection.x, cloud.projection.y) - CameraMan.CUT_LEN * 2;
    cutSubject = cloud;
    cutCount = 0;
  }

  @Override
  protected void createTail() {
    tail = new Tail(app, TAIL_LENGTH, TAIL_COLOR, DEFAULT_LAYER);
    tail.init(vectorP);
  }

  /*
   * take glider's sink rate and add any
   * ridge or thermal lift
   */
  @Override
  protected void sink() {
    //float lift = SINK_RATE;
    float lift = POLAR[polarIndex][1] * SINK_RATE;
    /*
      if (true) {
      v.z=  0;
      return;
      }
    */

    if (app.landscape != null) {
      Hill hill = app.landscape.getHillAt(vectorP);
      if (hill != null) {
        if (vectorP.z < hill.maxH + (float) 0.1) {
          //lift += (float) 1.5 * - SINK_RATE;
          lift += hill.getLift(vectorP);
        } else {
          //climbed to top of ridge
          //lift = 0;
          lift += hill.getLift(vectorP);
          if (demoMode && moveManager.joinedCircuit()) {
            moveManager.clearControllers();
            gotoNextLiftSource();
          }
        }
      }
    }

    if (app.sky != null) {
      Cloud cloud = app.sky.getCloudAt(vectorP);
      if (cloud != null) {
        if (vectorP.z < this.sky.getCloudBase() - this.getBodyHeight()) {
          lift += cloud.getLift(vectorP);
        } else {
          //stick to base of cloud and f*** off downwind
          lift = 0;
          if (demoMode) {
            moveManager.clearControllers();
            gotoNextLiftSource();
          }
        }
      }
    }

    vector.z = lift * app.timePerFrame;

    //check not going underground
    if (vectorP.z <= 0 && vector.z < 0) {
      vector.z = 0;
      if (!landed) {
        landed();
      }
    }

    if (tryLater > 0) {
      tryLater--;
      if (tryLater == 0) {
        gotoNextLiftSource();
      }
    }

    if (moveManager.cloud != null && moveManager.cloud.decaying) {
      //System.out.println("Decaying cloud - move on");
      moveManager.clearControllers();
      if (isUser) {
        app.cameraMan.cutSetup(this, isUser);
      } else {
        gotoNextLiftSource();
      }
    }
  }

  void landed() {
    roll = 0;
    tail.reset(vectorP);
    landed = true;
    //app.cameraMan.cutSetup(this, isUser);
  }

  @Override
  public void update(final UpdateContext context) {
    if (isUser) {
      if (demoMode) {
        return;
      }

      if (reachedGoal) {
        app.getTextMessage().setMessage("Well done ! You have reached goal. You flew "
            + (int) (vectorP.y / 2)
            + "km in "
            + (int) app.getTime() / 2
            + " mins. Press <y> to fly again.");
      } else if (landed) {
        app.getTextMessage().setMessage("You have landed - you flew "
            + (int) (vectorP.y / 2)
            + "km. Press <y> to fly again.");
      } else {
        app.getTextMessage().setMessage("D: "
            + (int) (vectorP.y / 2)
            + "km  T: " + (int) app.getTime() / 2
            + "mins  H: "
            + (int) ((vectorP.z / 2) * 1500) + "m ");
      }
    }

    if (landed || reachedGoal) {
      return;
    }

    super.update(context);

    //delayed cut hack 5/10
    if (cutPending) {
      cutCount++;
      if (cutCount >= cutWhen) {
        cutNow();
      }
    }

    if (app.landscape != null && triggerLoading) {
      app.landscape.loadTilesAround(vectorP);
    }

    if (!reachedGoal) {
      reachedGoal = app.landscape.reachedGoal(vectorP);
    }
  }

  /**
   * return number of ticks till we get there
   * nb. this assumes stationary target and no wind
   */
  int whenArrive(
      final float x,
      final float y
  ) {
    float d = (vectorP.x - x) * (vectorP.x - x) + (vectorP.y - y) * (vectorP.y - y);
    d = (float) Math.sqrt(d);
    return (int) (d / ds);
  }

  private void cutNow() {
    /*
      this call used to be in gotoliftsource
      moved for timing - wait until glider is
      close to the hill/thermal so the camera
      does not get ahead of it

      todo - camera decide how to track without
      losing site of the glider
    */
    app.cameraMan.cutSetup(cutSubject, isUser);
    cutPending = false;
    cutCount = 0;
    cutSubject = null;
  }

  @Override
  public Vector3f getFocus() {
    float z;
    if (vectorP.z < 0.5) {
      z = (float) 0.5;
    } else {
      z = (float) 0.5 + (vectorP.z - (float) 0.5) / 2;
    }
    return new Vector3f(vectorP.x, vectorP.y + 1, z);
  }

  @Override
  public Vector3f getEye() {
    return new Vector3f(
        vectorP.x + (float) 0.2,
        vectorP.y - 2,
        vectorP.z + (float) 0.3); //(float)0.8
  }

  void takeOff(final Vector3f inP) {
    /*
      hack ? should be in glider user ?
    */

    //if (!landed) return;

    setPolarIndex(0);
    vector = new Vector3f(0, ds, 0);
    vectorP = new Vector3f(inP.x, inP.y, inP.z);

    landed = false;
    reachedGoal = false;
    tail.reset(vectorP);

    demoMode = true;
    gotoNextLiftSource();

    if (isUser) {
      app.cameraMan.setMode(CameraMode.SELF);
      demoMode = false;
    }
  }

  /*
   * choose a point on the polar
   * determines speed and sink rate
   *
   * bit of a mess as this class knows about sink rate
   * and super class manages horizontal speed
   */
  public void setPolarIndex(final int i) {
    polarIndex = i;
    super.setSpeed(POLAR[polarIndex][0] * SPEED);
  }
}
