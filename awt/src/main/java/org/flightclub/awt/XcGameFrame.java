/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt;

import javax.swing.JFrame;
import org.flightclub.engine.GameLoop;
import org.flightclub.engine.MouseOrbitCamera;
import org.flightclub.engine.Sky;
import org.flightclub.engine.XcGame;
import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.GameEnvironment;
import org.flightclub.engine.core.GameMode;
import org.flightclub.engine.core.GameModeHolder;
import org.flightclub.engine.core.RenderManager;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.events.MouseTracker;
import org.flightclub.engine.keyboard.KeyboardState;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.awt.AwtEventAdapter.registerEventListeners;

public class XcGameFrame extends JFrame {
  private static final Logger LOG = LoggerFactory.getLogger(XcGameFrame.class);

  public XcGameFrame(
      final String title,
      final Vector2i windowSize
  ) {
    super(title);

    final KeyboardState keyboardState = new KeyboardState();
    final EventManager eventManager = new EventManager();
    final MouseTracker mouseTracker = new MouseTracker();
    final RenderManager renderManager = new RenderManager();
    final Camera camera = new Camera(windowSize);

    final XcGame game = new XcGame(
        renderManager,
        camera,
        eventManager,
        keyboardState,
        new Sky(),
        new GameModeHolder(GameMode.DEMO),
        new GameEnvironment(windowSize, new JavaxAudioPlayer())
    );

    final ModelCanvas modelCanvas = new ModelCanvas(game);
    add(modelCanvas, "Center");
    setSize(windowSize.x(), windowSize.y());
    setVisible(true);
    modelCanvas.init();

    game.addGameObject(modelCanvas);

    final MouseOrbitCamera mouseOrbitCamera = new MouseOrbitCamera(camera, mouseTracker, 0.25f)
        .withLimitZ(true)
        .withSensitivity(20f);

    game.addGameObject(mouseOrbitCamera);

    registerEventListeners(this, keyboardState, eventManager, mouseTracker);
    new GameLoop(game, 25).gameLoop();
  }

  public static void main(final String... args) {
    LOG.info("Flight Club");
    final Vector2i windowSize = new Vector2i(1000, 600);

    new XcGameFrame(
        "Flight Club",
        windowSize
    );
  }
}
