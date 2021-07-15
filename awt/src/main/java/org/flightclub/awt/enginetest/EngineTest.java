/*
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 */

package org.flightclub.awt.enginetest;

import javax.swing.JFrame;
import org.flightclub.awt.JavaxAudioPlayer;
import org.flightclub.awt.ModelCanvas;
import org.flightclub.engine.GameLoop;
import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.GameEnvironment;
import org.flightclub.engine.core.RenderManager;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.events.MouseTracker;
import org.flightclub.engine.keyboard.KeyboardState;
import org.flightclub.engine.math.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.awt.AwtEventAdapter.registerEventListeners;

public class EngineTest extends JFrame {
  private static final Logger LOG = LoggerFactory.getLogger(EngineTest.class);

  public EngineTest(
      final String title,
      final Pair<Integer, Integer> windowSize
  ) {
    super(title);

    final Camera camera = new Camera(windowSize);
    final EventManager eventManager = new EventManager();
    final KeyboardState keyboardState = new KeyboardState();
    final RenderManager renderManager = new RenderManager();
    final GameEnvironment gameEnvironment = new GameEnvironment(windowSize, new JavaxAudioPlayer());
    final MouseTracker mouseTracker = new MouseTracker();

    final TestGame game = new TestGame(
        camera,
        renderManager,
        keyboardState,
        gameEnvironment,
        mouseTracker
    );

    final ModelCanvas modelCanvas = new ModelCanvas(game);
    add(modelCanvas, "Center");
    setSize(windowSize.x(), windowSize.y());
    setVisible(true);
    modelCanvas.init();
    game.addGameObject(modelCanvas);

    registerEventListeners(this, keyboardState, eventManager, mouseTracker);
    new GameLoop(game, 25).gameLoop();
  }

  public static void main(final String ...args) {
    LOG.info("Flight Club Engine Test");
    final Pair<Integer, Integer> windowSize =  new Pair<>(1000, 600);
    new EngineTest("Flight Club Engine Test", windowSize);
  }
}
