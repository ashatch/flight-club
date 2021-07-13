/**
 * This code is covered by the GNU General Public License
 * detailed at http://www.gnu.org/copyleft/gpl.html
 * Flight Club docs located at http://www.danb.dircon.co.uk/hg/hg.htm
 * Dan Burton , Nov 2001
 */

package org.flightclub.awt;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import org.flightclub.engine.GameLoop;
import org.flightclub.engine.MouseOrbitCamera;
import org.flightclub.engine.camera.Camera;
import org.flightclub.engine.core.GameEnvironment;
import org.flightclub.engine.core.GameMode;
import org.flightclub.engine.core.GameModeHolder;
import org.flightclub.engine.events.EventManager;
import org.flightclub.engine.events.MouseTracker;
import org.flightclub.engine.math.Pair;
import org.flightclub.engine.core.RenderManager;
import org.flightclub.engine.Sky;
import org.flightclub.engine.XcGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.flightclub.awt.AwtKeyEventMapper.toEngineKeyEvent;

public class XcGameFrame extends JFrame {
  private static final Logger LOG = LoggerFactory.getLogger(XcGameFrame.class);

  public XcGameFrame(
      final EventManager eventManager,
      final MouseTracker mouseTracker,
      final XcGame game,
      final String title,
      final Pair<Integer, Integer> windowSize
  ) {
    super(title);

    final ModelCanvas modelCanvas = new ModelCanvas(eventManager, mouseTracker, game);
    add(modelCanvas, "Center");
    setSize(windowSize.x(), windowSize.y());
    setVisible(true);
    modelCanvas.init();
    game.addGameObject(modelCanvas);

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        System.exit(0);
      }
    });

    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        game.eventManager.addEvent(toEngineKeyEvent(e));
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        game.eventManager.addEvent(toEngineKeyEvent(e));
      }
    });
  }

  public static void main(final String ...args) {
    LOG.info("Flight Club");

    final Pair windowSize = new Pair(1000, 600);
    final EventManager eventManager = new EventManager();
    final MouseTracker mouseTracker = new MouseTracker();
    final RenderManager renderManager = new RenderManager();
    final Camera camera = new Camera(windowSize);

    final XcGame game = new XcGame(
        renderManager,
        camera,
        eventManager,
        new Sky(),
        new GameModeHolder(GameMode.DEMO),
        new GameEnvironment(windowSize, new JavaxAudioPlayer())
    );

    final MouseOrbitCamera mouseOrbitCamera = new MouseOrbitCamera(camera, mouseTracker);
    game.addGameObject(mouseOrbitCamera);

    new XcGameFrame(eventManager, mouseTracker, game, "Flight Club", windowSize);
    new GameLoop(game, 25).gameLoop();
  }
}
