/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.instruments;

import org.flightclub.engine.FlyingDot;
import org.flightclub.engine.Glider;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.XcGame;

public class Variometer {
  public static final float LIFT_MAX = -2 * Glider.SINK_RATE;

  //how many different sounds
  private static final int NUM_BEEPS = 4;
  private static final float SECONDS_PER_BEEP = 0.2f;

  //different beeps as we go up the steps
  private static final float[] STEPS = new float[NUM_BEEPS];

  static {
    // Calculate the steps at which the beep changes. The strongest lift
    // in this game is twice the glider's sink rate (under big clouds)
    for (int i = 0; i < NUM_BEEPS; i++) {
      STEPS[i] = i * LIFT_MAX / NUM_BEEPS;
    }
  }

  final XcGame app;
  private final FlyingDot flyingDot;

  private float time = 0;

  public Variometer(final XcGame app, final FlyingDot flyingDot) {
    this.flyingDot = flyingDot;
    this.app = app;
  }

  public void tick(final UpdateContext context) {
    time += context.deltaTime();
    if (time >= SECONDS_PER_BEEP) {
      time = 0.0f;
      this.beep(context);
    }
  }

  /**
   * Beep if we are going up. Which beep depends on
   * how strong the lift is. Note, we must convert v from
   * dist per frame to dist per unit time.
   */
  private void beep(final UpdateContext context) {
    float lift = flyingDot.vector.posZ / (context.timeMultiplier() * XcGame.TIME_PER_FRAME);

    String filename = filenameForLift(lift);
    if (filename != null) {
      app.envGameEnvironment.audioPlayer().play(filename);
    }
  }

  private String filenameForLift(final float lift) {
    for (int i = NUM_BEEPS - 1; i >= 0; i--) {
      if (lift > STEPS[i]) {
        return String.format("beep%d.wav", i);
      }
    }

    return null;
  }
}
