/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.engine.keyboard;

import org.flightclub.engine.Glider;
import org.flightclub.engine.Landscape;
import org.flightclub.engine.MovementManager;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.events.KeyEvent;
import org.flightclub.engine.events.KeyEventHandler;
import org.flightclub.engine.math.Vector3d;

public class UserGliderController implements KeyEventHandler, UpdatableGameObject {

  private final Glider glider;

  public UserGliderController(final Glider glider) {
    this.glider = glider;
  }

  @Override
  public void update(final UpdateContext context) {
    checkBounds();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    int key = e.code();
    switch (key) {
      case KeyEvent.VK_A:
      case KeyEvent.VK_LEFT:
        //hack
        this.glider.demoMode = false;
        this.glider.moveManager.setNextMove(MovementManager.LEFT);
        break;
      case KeyEvent.VK_D:
      case KeyEvent.VK_RIGHT:
        this.glider.demoMode = false;
        this.glider.moveManager.setNextMove(MovementManager.RIGHT);
        break;
      case KeyEvent.VK_SPACE:
        //slow down, if i was fast
        this.glider.setPolarIndex(0);
        this.glider.moveManager.workLift();
        break;

      case KeyEvent.VK_W:
      case KeyEvent.VK_UP:
        this.glider.setPolarIndex(1);
        break;

      case KeyEvent.VK_S:
      case KeyEvent.VK_DOWN:
        this.glider.setPolarIndex(0);
        break;

      default:
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
    int key = e.code();
    switch (key) {
      case KeyEvent.VK_A:
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_D:
      case KeyEvent.VK_RIGHT:
        this.glider.moveManager.setNextMove(MovementManager.STRAIGHT);
        break;
      default:
    }
  }

  private void checkBounds() {
    if (this.glider.vectorP.posX > Landscape.TILE_WIDTH / 2f) {
      this.glider.moveManager.setTargetPoint(new Vector3d(0, this.glider.vectorP.posY, 0));
    }
    if (this.glider.vectorP.posX < -Landscape.TILE_WIDTH / 2f) {
      this.glider.moveManager.setTargetPoint(new Vector3d(0, this.glider.vectorP.posY, 0));
    }
    if (this.glider.vectorP.posY < -Landscape.TILE_WIDTH / 2f) {
      this.glider.moveManager.setTargetPoint(new Vector3d(this.glider.vectorP.posX, 0, 0));
    }
  }
}
