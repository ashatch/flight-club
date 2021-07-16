/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.camera;

/*
  todo - seperate into two classes
  - generic 3d framework camera functionality
  - XCGame extension of above class
*/

import org.flightclub.engine.Landscape;
import org.flightclub.engine.core.GameMode;
import org.flightclub.engine.core.GameModeHolder;
import org.joml.Vector3f;

public class CameraMan {
  /* number of steps we accelerate over */
  private static final int CUT_RAMP = 12;
  private static final int PLAN_H = 20;
  private static final int PLAN_Y_OFFSET = 4;

  private final GameModeHolder gameModeHolder;
  private final Landscape landscape;
  private final Camera camera;

  private CameraSubject cameraSubject; //populate using cutSetup

  private CameraSubject subject1;
  private CameraSubject subject2;

  private CameraMode mode = CameraMode.SELF;

  private int cutCount = 0;
  private int cut2Count = 0;

  private Vector3f deye;
  private Vector3f dfocus;
  private Vector3f eyeGoto;
  private Vector3f focusGoto;

  /* steps to glide between POVs */
  public static final int CUT_LEN = 75;

  public CameraMan(
      final Camera camera,
      final GameModeHolder gameModeHolder,
      final Landscape landscape
  ) {
    this.camera = camera;
    this.gameModeHolder = gameModeHolder;
    this.landscape = landscape;
  }

  @SuppressWarnings("SuspiciousNameCombination")
  public void setMode(final CameraMode mode) {
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
        camera.setFocus(subject1.getFocus());
        camera.focusOffset(0, PLAN_Y_OFFSET);
        camera.setEye(new Vector3f(Landscape.TILE_WIDTH / 2f, camera.getFocus().y, PLAN_H));
        cameraSubject = subject1;
      } else if (subject2 != null) {
        camera.setFocus(subject2.getFocus());
        camera.focusOffset(0, PLAN_Y_OFFSET);
        camera.setEye(new Vector3f(Landscape.TILE_WIDTH / 2f, camera.getFocus().y, PLAN_H));
        cameraSubject = subject2;
      } else {
        camera.setFocus(new Vector3f(0, Landscape.TILE_WIDTH, 0));
        camera.setEye(new Vector3f(10, Landscape.TILE_WIDTH, PLAN_H));
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
      Vector3f eyeVector = cameraSubject.getEye();
      Vector3f focusVector = cameraSubject.getFocus();

      Vector3f deltaEyeVector = new Vector3f(eyeVector).sub(eyeGoto);
      Vector3f deltaFocusVector = new Vector3f(focusVector).sub(focusGoto);

      camera.getEye().add(deltaEyeVector);
      camera.getFocus().add(deltaFocusVector);

      //note subjects new position for next iteration
      eyeGoto = eyeVector;
      focusGoto = focusVector;
    } else {
      //track whilst maintaining a constant camera angle
      Vector3f f = cameraSubject.getFocus();
      f.x = 0;
      f.y += PLAN_Y_OFFSET;
      camera.moveFocus(f);
    }
  }

  /**
   * glide eye and focus to new positions using N steps. here we set it up and
   * it'll unwind over the next N ticks
   *
   * @param isUser true: call from user glider, false: one of the gagggle
   */
  public void cutSetup(CameraSubject subject, boolean isUser) {
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

    deye = new Vector3f(eyeGoto).sub(camera.getEye());
    dfocus = new Vector3f(focusGoto).sub(camera.getFocus());

    /*
     * eye accelerates from 0 upto velocity
     * deye over cutRamp steps, tracks at deye then
     * slows to zero over cutRamp steps. similarly for focus.
     */
    deye.mul((float) 1.0 / (cutCount - CUT_RAMP));
    dfocus.mul((float) 1.0 / (cutCount - CUT_RAMP));
  }

  /**
   * iterate the cut. nb. the point we are cutting to may be on the move
   */
  @SuppressWarnings("StatementWithEmptyBody")
  void cutStep() {
    Vector3f deyePrime = new Vector3f(deye);
    Vector3f dfocusPrime = new Vector3f(dfocus);
    float s;

    if (cutCount > CUT_LEN - CUT_RAMP) {
      //accelerating
      s = (float) (CUT_LEN - cutCount) / CUT_RAMP;
      //System.out.println("Cut acc, s: " + s);
      deyePrime.mul(s);
      dfocusPrime.mul(s);
    } else if (cutCount < CUT_RAMP) {
      //decelerating
      s = (float) cutCount / CUT_RAMP;
      //System.out.println("Cut dec, s: " + s);
      deyePrime.mul(s);
      dfocusPrime.mul(s);
    } else {
      //const speed - no need to scale
    }

    camera.getEye().add(deyePrime);
    camera.getFocus().add(dfocusPrime);

    cutCount--;
  }

  public void setSubject1(final CameraSubject subject) {
    this.subject1 = subject;
  }

  public void setSubject2(final CameraSubject subject) {
    this.subject2 = subject;
  }
}
