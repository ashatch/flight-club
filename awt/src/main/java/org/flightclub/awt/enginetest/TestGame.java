/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 */

package org.flightclub.awt.enginetest;

import java.util.Vector;
import org.flightclub.engine.GameLoopTarget;
import org.flightclub.engine.GameRenderer;
import org.flightclub.engine.MouseOrbitCamera;
import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.Color;
import org.flightclub.engine.core.GameEnvironment;
import org.flightclub.engine.core.Graphics;
import org.flightclub.engine.core.RenderContext;
import org.flightclub.engine.core.RenderManager;
import org.flightclub.engine.core.UpdatableGameObject;
import org.flightclub.engine.core.UpdateContext;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.events.KeyEvent;
import org.flightclub.engine.events.MouseTracker;
import org.flightclub.engine.keyboard.KeyboardState;
import org.flightclub.engine.math.Vector3d;
import org.flightclub.engine.models.GliderShape;
import org.flightclub.engine.models.UnitCube;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestGame implements GameLoopTarget, GameRenderer {
  private static final Logger LOG = LoggerFactory.getLogger(TestGame.class);

  private final RenderManager renderManager;
  private final GameEnvironment gameEnvironment;
  private final Camera camera;
  private final KeyboardState keyboardState;
  private RenderContext renderContext;

  private final Vector<UpdatableGameObject> gameObjects = new Vector<>();

  public TestGame(
      final Camera camera,
      final RenderManager renderManager,
      final KeyboardState keyboardState,
      final GameEnvironment gameEnvironment,
      final MouseTracker mouseTracker
  ) {
    this.camera = camera.withLightRay(-3, 2, 3);
    this.renderManager = renderManager;
    this.keyboardState = keyboardState;
    this.gameEnvironment = gameEnvironment;

    final GliderShape someGlider = new GliderShape(Color.BLUE);
    this.renderManager.add(someGlider);
    this.renderManager.add(new UnitCube(1, true));
    camera.setEye(1, 1, 0);
    camera.setFocus(0, 0, 0);
    someGlider.updateShadow((posX, posY) -> -0.2f);

    this.addGameObject(new MouseOrbitCamera(this.camera, mouseTracker, 0.5f).withSensitivity(1f));
  }

  public void addGameObject(final UpdatableGameObject gameObject) {
    this.gameObjects.add(gameObject);
  }

  @Override
  public void updateGameState(final float delta) {
    processKeyboardState();

    final UpdateContext context = new UpdateContext(delta, 1.0f, this.renderManager);
    this.gameObjects.forEach(obj -> obj.update(context));
  }

  private void processKeyboardState() {
    if (this.keyboardState.isKeyDown(KeyEvent.VK_UP)) {
      camera.getEye().add(new Vector3d(0, -0.1, 0));
    } else if (this.keyboardState.isKeyDown(KeyEvent.VK_DOWN)) {
      camera.getEye().add(new Vector3d(0, +0.1, 0));
    }
  }

  @Override
  public void setGameGraphics(final Graphics graphics) {
    this.renderContext = new RenderContext(graphics, camera, this.gameEnvironment.windowSize(), false);
  }

  @Override
  public void render() {
    if (this.renderContext == null) {
      LOG.warn("No render context");
      return;
    }

    camera.setMatrix();
    this.renderManager.render(this.renderContext);
  }
}
