/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

/*
  todo - seperate into two classes
  - generic 3d framework camera functionality
  - XCGame extension of above class
*/

public class CameraMan {
  public final Vector3d lightRay;
  private final GameModelHolder gameModeHolder;
  private final Landscape landscape;
  public float zoom = 1;

  private float distance = 0;
  private float[][] matrix;

  private final int screenWidth;
  private final int screenHeight;
  private final float theScale;

  private Vector3d eye;
  private Vector3d focus;

  private static final int BACKGROUND_R = 255;
  private static final int BACKGROUND_G = 255;
  private static final int BACKGROUND_B = 255;
  private static final Color BACKGROUND = new Color(BACKGROUND_R, BACKGROUND_G, BACKGROUND_B);

  private static final float DEPTH_OF_VISION = Landscape.TILE_WIDTH * (float) 2.5;
  private static final float AMBIENT_LIGHT = (float) 0.3;

  public static final float CAMERA_MOVEMENT_DELTA = (float) 0.1;

  CameraSubject cameraSubject; //populate using cutSetup
  CameraSubject subject1;
  CameraSubject subject2;

  private CameraMode mode = CameraMode.SELF;

  int cutCount = 0;
  int cut2Count = 0;

  /* number of steps we accelerate over */
  static final int CUT_RAMP = 12;

  /* steps to glide between POVs */
  static final int CUT_LEN = 75;

  Vector3d deye;
  Vector3d dfocus;
  Vector3d eyeGoto;
  Vector3d focusGoto;

  static final int PLAN_H = 20;
  static final int PLAN_Y_OFFSET = 4;

  CameraMan(
      final GameModelHolder gameModelHolder,
      final Landscape landscape,
      final IntPair windowSize
  ) {
    this.gameModeHolder = gameModelHolder;
    this.landscape = landscape;

    //get the canvas size
    screenWidth = windowSize.x();
    screenHeight = windowSize.y();
    theScale = screenHeight * (float) 1.1; //defines lens angle - smaller num -> wider angle

    //starting position and light
    eye = new Vector3d(3, 0, 0);
    focus = new Vector3d(0, 0, 0);

    lightRay = new Vector3d(1, 1, -3);
    //lightRay = new Vector3d(-2,2,-1);
    lightRay.makeUnit();
  }

  @SuppressWarnings("SuspiciousNameCombination")
  void setMode(final CameraMode mode) {
    this.mode = mode;

    if (mode == CameraMode.SELF && subject1 != null) {
      cutCount = 0;
      cutSetup(subject1, true);
      return;
    }

    if (mode == CameraMode.GAGGLE && subject2 != null) {
      cutCount = 0;
      cut2Count = 0;
      cutSetup(subject2, false);
      return;
    }

    if (mode == CameraMode.PLAN) {

      //hack - should extend generic cameraman
      boolean user = (this.gameModeHolder.getMode() == GameMode.USER);

      if (subject1 != null && user) {
        focus = subject1.getFocus();
        focus.posX = 0;
        focus.posY += PLAN_Y_OFFSET;
        eye = new Vector3d(Landscape.TILE_WIDTH / 2, focus.posY, PLAN_H);
        cameraSubject = subject1;
      } else if (subject2 != null) {
        focus = subject2.getFocus();
        focus.posX = 0;
        focus.posY += PLAN_Y_OFFSET;
        eye = new Vector3d(Landscape.TILE_WIDTH / 2, focus.posY, PLAN_H);
        cameraSubject = subject2;
      } else {
        focus = new Vector3d(0, Landscape.TILE_WIDTH, 0);
        eye = new Vector3d(10, Landscape.TILE_WIDTH, PLAN_H);
        cameraSubject = null;
      }
      cutCount = 0;
    }


    if (mode == CameraMode.TILE && this.landscape != null) {
      cutCount = 0;
      cut2Count = 0;
      cutSetup(this.landscape, true);
    }
  }

  /*
   * how much light falls on a surface with this normal - take dot product
   */
  float surfaceLight(Vector3d inNormal) {
    float dot = lightRay.dot(inNormal);
    dot = (-dot + 1) / 2;

    //fri 1 mar 2002 - some under lighting for clouds
    if (inNormal.posZ < -0.99) {
      dot += 0.3;
    }

    return dot * (1 - AMBIENT_LIGHT) + AMBIENT_LIGHT;
  }

  /*
   * update camera position
   */
  public void tick() {
    if (cameraSubject == null) {
      return;
    }

    if (cut2Count > 0) {
      cut2Count--; //for watch mode 2
    }

    if (cutCount > 0) {
      cutStep();
    }

    followSubject();
  }

  void followSubject() {
    //add in movement of our subject
    if (mode != CameraMode.PLAN) {
      Vector3d eyeVector = cameraSubject.getEye();
      Vector3d focusVector = cameraSubject.getFocus();

      Vector3d deltaEyeVector = eyeVector.minus(eyeGoto);
      Vector3d deltaFocusVector = focusVector.minus(focusGoto);

      eye.add(deltaEyeVector);
      focus.add(deltaFocusVector);

      //note subjects new position for next iteration
      eyeGoto = eyeVector;
      focusGoto = focusVector;
    } else {
      //track whilst maintaining a constant camera angle
      Vector3d f = cameraSubject.getFocus();
      f.posX = 0;
      f.posY += PLAN_Y_OFFSET;
      moveFocus(f);
    }
  }

  /**
   * glide eye and focus to new positions using N steps. here we set it up and
   * it'll unwind over the next N ticks
   *
   * @param isUser true: call from user glider, false: one of the gagggle
   */
  void cutSetup(CameraSubject subject, boolean isUser) {
    if (mode == CameraMode.PLAN) {
      return;
    }

    if (cutCount > 0 && mode == CameraMode.GAGGLE) {
      //ignore this call if already doing a cut
      return;
    }

    if (!isUser && mode != CameraMode.GAGGLE) {
      //filter out all calls from sniffers unless watching gaggle
      return;
    }

    //gaggle mode too jumpy
    if (mode == CameraMode.GAGGLE && cut2Count > 0) {
      return;
    }

    cameraSubject = subject;
    cutCount = CUT_LEN;
    cut2Count = cutCount * 2;

    eyeGoto = cameraSubject.getEye();
    focusGoto = cameraSubject.getFocus();

    deye = eyeGoto.minus(eye);
    dfocus = focusGoto.minus(focus);

    /*
     * eye accelerates from 0 upto velocity
     * deye over cutRamp steps, tracks at deye then
     * slows to zero over cutRamp steps. similarly for focus.
     */
    deye.scaleBy((float) 1.0 / (cutCount - CUT_RAMP));
    dfocus.scaleBy((float) 1.0 / (cutCount - CUT_RAMP));
  }

  /**
   * iterate the cut. nb. the point we are cutting to may be on the move
   */
  @SuppressWarnings("StatementWithEmptyBody")
  void cutStep() {
    Vector3d deyePrime = new Vector3d(deye);
    Vector3d dfocusPrime = new Vector3d(dfocus);
    float s;

    if (cutCount > CUT_LEN - CUT_RAMP) {
      //accelerating
      s = (float) (CUT_LEN - cutCount) / CUT_RAMP;
      //System.out.println("Cut acc, s: " + s);
      deyePrime.scaleBy(s);
      dfocusPrime.scaleBy(s);
    } else if (cutCount < CUT_RAMP) {
      //decelerating
      s = (float) cutCount / CUT_RAMP;
      //System.out.println("Cut dec, s: " + s);
      deyePrime.scaleBy(s);
      dfocusPrime.scaleBy(s);
    } else {
      //const speed - no need to scale
    }

    eye.add(deyePrime);
    focus.add(dfocusPrime);

    cutCount--;
  }


  public Vector3d getEye() {
    return eye;
  }

  public void setEye(float x, float y, float z) {
    eye.set(x, y, z);
  }

  public Vector3d getFocus() {
    return focus;
  }

  public void setFocus(float x, float y, float z) {
    focus.set(x, y, z);
  }

  public float getDistance() {
    return distance;
  }

  public float[][] getMatrix() {
    return matrix;
  }

  /*
   * rotate eye about z axis by xy radians and up/down by z
   */
  public void rotateEyeAboutFocus(float dtheta) {
    Vector3d ray = eye.minus(focus);

    //transform ray
    float[][] m = Tools3d.rotateX(new Vector3d(1, dtheta, 0));
    Tools3d.applyTo(m, ray, ray);

    //reposition eye
    eye.set(focus).add(ray);
  }

  public void translateZ(float dz) {
    Vector3d ray = eye.minus(focus);

    ray.posZ += distance * dz;

    eye.set(focus).add(ray);

    if (eye.posZ < 0) {
      eye.posZ = 0;
    }
  }

  /*
   * move focus, maintaining angle of view
   */
  void moveFocus(Vector3d f) {
    Vector3d ray = eye.minus(focus);
    focus.set(f);
    eye.set(ray).add(focus);
  }

  /*
   * rotation such that eye is looking down +x axis at origin
   */
  void setMatrix() {
    Vector3d ray = eye.minus(focus);
    matrix = Tools3d.rotateX(ray);
    distance = ray.length();
  }

  /*
   * scale the y and z co-ords so a 1 by 1 square
   * fills the screen when viewed from a distance of ??
   *
   * origin appears center screen.
   * nb flip z as screen coords have origin at top left !
   *
   * 1/10 try double scale (ie. half camera angle)
   */
  public void scaleToScreen(Vector3d vec) {
    vec.posY *= theScale;    //preserve aspect ratio ? screenWidth;
    vec.posY += screenWidth / 2;

    vec.posZ *= -theScale;
    vec.posZ += screenHeight / 2;
  }

  /*
   * mute distant colors.
   *
   * x is ~ distance of surface from camera
   * since we are using the transformed coords
   */
  Color foggyColor(float x, Color c) {
    if (x >= 0) {
      return c;
    }

    x *= -1;

    if (x > DEPTH_OF_VISION) {
      return BACKGROUND;
    }

    float f = x / DEPTH_OF_VISION;
    int r = (int) (c.r() + f * (BACKGROUND_R - c.r()));
    int g = (int) (c.g() + f * (BACKGROUND_G - c.g()));
    int b = (int) (c.b() + f * (BACKGROUND_B - c.b()));

    return new Color(r, g, b);
  }

  public void move(float dx, float dy) {
    eye.posX += dx;
    eye.posY += dy;
    focus.posX += dx;
    focus.posY += dy;
  }
}


