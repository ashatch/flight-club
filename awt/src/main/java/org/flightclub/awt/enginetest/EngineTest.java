/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt.enginetest;

import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.flightclub.awt.JavaxAudioPlayer;
import org.flightclub.awt.ModelCanvas;
import org.flightclub.engine.GameLoop;
import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.GameEnvironment;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.events.MouseTracker;
import org.flightclub.engine.math.Pair;
import org.flightclub.engine.core.RenderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.awt.AwtKeyEventMapper.toEngineKeyEvent;

public class EngineTest extends Frame {
  private static final Logger LOG = LoggerFactory.getLogger(EngineTest.class);

  public EngineTest(
      final String title,
      final Pair<Integer, Integer> windowSize
  ) {
    super(title);

    final Camera camera = new Camera(windowSize);
    final EventManager eventManager = new EventManager();
    final RenderManager renderManager = new RenderManager();
    final GameEnvironment gameEnvironment = new GameEnvironment(windowSize, new JavaxAudioPlayer());
    final MouseTracker mouseTracker = new MouseTracker();

    final TestGame app = new TestGame(
        camera,
        renderManager,
        eventManager,
        gameEnvironment,
        mouseTracker
    );

    final ModelCanvas modelCanvas = new ModelCanvas(eventManager, mouseTracker, app);
    add(modelCanvas, "Center");
    setSize(windowSize.x(), windowSize.y());
    setVisible(true);
    modelCanvas.init();
    app.addGameObject(modelCanvas);

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });

    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        eventManager.addEvent(toEngineKeyEvent(e));
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        eventManager.addEvent(toEngineKeyEvent(e));
      }
    });

    new GameLoop(app, 25).gameLoop();
  }

  public static void main(final String ...args) {
    LOG.info("Flight Club Engine Test");
    final Pair windowSize =  new Pair(1000, 600);
    new EngineTest("Flight Club Engine Test", windowSize);
  }
}
