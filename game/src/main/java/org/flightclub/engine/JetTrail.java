/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine;

import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.models.Tail;
import org.joml.Vector3f;

import static org.flightclub.engine.core.RenderManager.BACKGROUND_LAYER;

/*
 * a jet in the upper atmosphere - leaves a long trail
 */
public class JetTrail extends FlyingDot {
  static final float SPEED = 5;
  static final float ALTITUDE = 6;
  static final float TURN_RADIUS = 16;
  FlyingDot buzzThis;
  static final float RANGE = 40;

  public static final int TAIL_LENGTH = 240;
  public static final Color TAIL_COLOR = new Color(200, 200, 200);

  public JetTrail(
      final XcGame app,
      final Sky sky,
      final float x,
      final float y
  ) {
    //set flag so camera will follow my cuts when in mode 1
    //(see glider.gotoNextLiftSource)
    super(app, sky, SPEED, TURN_RADIUS);
    super.init(new Vector3f(x, y, ALTITUDE));
    vector.y = SPEED;
  }

  void makeFlyX() {
    //override the default of flying down y axix
    vector.x = -SPEED;
    vector.y = 0;
  }

  @Override
  protected void createTail() {
    tail = new Tail(app, TAIL_LENGTH, TAIL_COLOR, BACKGROUND_LAYER);
    tail.wireEvery = 1;
    tail.init(vectorP);
  }

  void checkBounds() {
    if (buzzThis != null) {
      Vector3f t = new Vector3f(buzzThis.vectorP.x, buzzThis.vectorP.y + TURN_RADIUS, 0);
      if (vectorP.x > buzzThis.vectorP.x + RANGE
          || vectorP.x < buzzThis.vectorP.x - RANGE) {
        moveManager.setTargetPoint(t);
      }
      if (vectorP.y > buzzThis.vectorP.y + RANGE
          || vectorP.y < buzzThis.vectorP.y - RANGE) {
        moveManager.setTargetPoint(t);
      }
    }
  }

  @Override
  public void update(final UpdateContext context) {
    super.update(context);
    checkBounds();
  }
}
